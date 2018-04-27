package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * LCD Controller, which combines different images and displays them
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final int FULL_LINE_CYCLES = 114;
    
    private static final int NUMBER_OF_LINES_IN_VBLANK = 10;
    
    private static final int TILE_BYTES = 16;

    private final Cpu cpu;
    private final RamController vRam;

    private long nextNonIdleCycle = 0;
    private Mode nextMode;
    
    private LcdImage.Builder nextImageBuilder;
    private LcdImage image;

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    private enum Stat implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    }

    private enum Lcdc implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum Mode implements Bit {
        M0_HBLANK(51, Stat.INT_MODE0), 
        M1_VBLANK(FULL_LINE_CYCLES, Stat.INT_MODE1), 
        M2_SPRITE_MEM(20, Stat.INT_MODE2), 
        M3_VIDEO_MEM(43, null);

        public final int cycles;
        public final Stat intMode;

        private Mode(int cycles, Stat intMode) {
            this.cycles = cycles;
            this.intMode = intMode;
        }
    }

    private final RegisterFile<Reg> rf = new RegisterFile<>(Reg.values());

    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        Ram ram = new Ram(AddressMap.VIDEO_RAM_SIZE);
        vRam = new RamController(ram, AddressMap.VIDEO_RAM_START);

        nextMode = Mode.M2_SPRITE_MEM;
        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (AddressMap.VIDEO_RAM_START <= address
                && address < AddressMap.VIDEO_RAM_END) {
            return vRam.read(address);
        } else if (AddressMap.REGS_LCDC_START <= address
                && address < AddressMap.REGS_LCDC_END) {
            return rf.get(indexToReg(address - AddressMap.REGS_LCDC_START));
        }

        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END) {
            vRam.write(address, data);
        } else if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END) {
            Reg r = indexToReg(address - AddressMap.REGS_LCDC_START);

            if (r == Reg.LCDC) {
                if (!Bits.test(data, Lcdc.LCD_STATUS)) {
                    rf.set(Reg.LY, 0);
                    setMode(Mode.M0_HBLANK);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }

                rf.set(r, data);
            } else if (r == Reg.STAT) {
                int lsb = Bits.clip(3, rf.get(Reg.STAT));
                int msb = Bits.extract(data, 3, 5) << 3;
                rf.set(r, msb | lsb);
            } else if (r == Reg.LY || r == Reg.LYC) {
                // TODO : make this prettier
                // LY is read-only
            } else {
                rf.set(r, data);
            }
        }
    }

    /**
     * Returns the current lcd image. If the controller has not drawn an image
     * yet, returns a blank image (all zero)
     * 
     * @return an LcdImage
     */
    public LcdImage currentImage() {
        return image;
    }

    @Override
    public void cycle(long cycle) {
        if (cycle == nextNonIdleCycle) {
            setMode(nextMode);
            reallyCycle();
        }
        
        if (nextNonIdleCycle == Long.MAX_VALUE && rf.testBit(Reg.LCDC, Lcdc.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            reallyCycle();
        }
    }

    /* Private methods */

    private void reallyCycle() {
        Mode mode = currentMode();
        
        // TODO System.out.println(currentLine() + ": " + mode);
        
        switch (mode) {
            case M1_VBLANK: {
                if (enteringVBlank()) {
                    cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
                    
                    image = nextImageBuilder.build();
                    nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);   
                } else if (exitingVBlank()) {
                    nextMode = Mode.M2_SPRITE_MEM;                    
                }
                
                updateLineIndex();
            } break;
            case M2_SPRITE_MEM: {
                nextMode = Mode.M3_VIDEO_MEM;
                nextImageBuilder.setLine(currentLine(), computeLine());
            } break;
            case M3_VIDEO_MEM: {
                nextMode = Mode.M0_HBLANK;
            } break;
            case M0_HBLANK: {                
                
                if (allLinesDrawn()) {
                    nextMode = Mode.M1_VBLANK;
                } else {
                    nextMode = Mode.M2_SPRITE_MEM;
                }
                
                updateLineIndex();
            } break;
        }
        
        nextNonIdleCycle += mode.cycles;
    }

    private void raiseStatIfModeFlagOn(Mode mode) {
        if (mode.intMode != null) {
            if (rf.testBit(Reg.STAT, mode.intMode)) {
                cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
            }
        }
    }

    private Mode currentMode() {
        int msb = rf.testBit(Reg.STAT, Stat.MODE1) ? 0b10 : 0b00;
        int lsb = rf.testBit(Reg.STAT, Stat.MODE0) ? 0b01 : 0b00;

        return Mode.values()[msb | lsb];
    }
    
    private int currentLine() {
        return rf.get(Reg.LY);
    }
    
    private boolean allLinesDrawn() {
        return currentLine() >= LCD_HEIGHT - 1;
    }
    
    private void updateLineIndex() {
        int numberOfLines = LCD_HEIGHT + NUMBER_OF_LINES_IN_VBLANK;
        // TODO System.out.println("Updated:" + (rf.get(Reg.LY) + 1));
        writeToLyLyc(Reg.LY, (currentLine() + 1) % numberOfLines);
    }
    
    private boolean enteringVBlank() {
        return currentLine() == LCD_HEIGHT;
    }
    
    private boolean exitingVBlank() {
        return currentLine() == LCD_HEIGHT + NUMBER_OF_LINES_IN_VBLANK - 1;
    }
    
    private LcdImageLine computeLine() {
        int startX = rf.get(Reg.SCX);
        int startY = rf.get(Reg.SCY);
        
        LcdImageLine line = extractLine(startY + currentLine());
        line = line.extractWrapped(startX, LCD_WIDTH).mapColors(rf.get(Reg.BGP));
               
        return line;
    }

    private void setMode(Mode mode) {
        int modeCode = mode.index();

        raiseStatIfModeFlagOn(mode);
        
        rf.setBit(Reg.STAT, Stat.MODE1, Bits.test(modeCode, Stat.MODE1));
        rf.setBit(Reg.STAT, Stat.MODE0, Bits.test(modeCode, Stat.MODE0));        
    }

    private void writeToLyLyc(Reg r, int data) {
        Preconditions.checkArgument(r == Reg.LY || r == Reg.LYC);
        Preconditions.checkBits8(data);
        
        rf.set(r, data);
        rf.setBit(Reg.STAT, Stat.LYC_EQ_LY, rf.get(Reg.LY) == rf.get(Reg.LYC));

        if (rf.testBit(Reg.STAT, Stat.INT_LYC) && rf.testBit(Reg.STAT, Stat.LYC_EQ_LY)) {
            cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
        }        
    }

    private static LcdImage emptyImage() {
        BitVector zero = new BitVector(LCD_WIDTH, false);
        LcdImageLine emptyLine = new LcdImageLine(zero, zero, zero);
        LcdImage.Builder lcdBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);

        for (int i = 0; i < LCD_HEIGHT; i++) {
            lcdBuilder.setLine(i, emptyLine);
        }

        return lcdBuilder.build();
    }
    
    private LcdImageLine extractLine(int lineIndex) {
        int startTileIndex = (lineIndex / 8) * 32;
        int memoryStart = rf.testBit(Reg.LCDC, Lcdc.BG_AREA) ? AddressMap.BG_DISPLAY_DATA[1] : AddressMap.BG_DISPLAY_DATA[0];
        
        LcdImageLine.Builder lb = new LcdImageLine.Builder(256);
       
        for(int i = 0; i < 32; i++) {
            int tileIndex = vRam.read(memoryStart + startTileIndex + i);
            int lsb = Bits.reverse8(vRam.read(tileAddressBG(tileIndex) + (lineIndex % 8) * 2));
            int msb = Bits.reverse8(vRam.read(tileAddressBG(tileIndex) + (lineIndex % 8) * 2 + 1));
            lb.setBytes(i, msb, lsb);
        }
        
        return lb.build();
    }
    
    private int tileAddressBG(int index) {
        if (rf.testBit(Reg.LCDC, Lcdc.TILE_SOURCE)) {
            return AddressMap.TILE_SOURCE[1] + index * TILE_BYTES;
        } else {
            int shiftedIndex = Bits.clip(8, index + 0x80);
            return AddressMap.TILE_SOURCE[0] + shiftedIndex * TILE_BYTES;
        }
        
        /*
        
        if (isBetweenIncl(index, 0x80, 0xFF)) {
            return AddressMap.TILE_SOURCE[1] + index * TILE_BYTES;
        } else if (isBetweenIncl(index, 0x00, 0x7F)) {
            int address = AddressMap.TILE_SOURCE[0] + index * TILE_BYTES;
            return address;//rf.testBit(Reg.LCDC, Lcdc.TILE_SOURCE) ? address : address + 0x1000;
        } else {
            throw new IllegalArgumentException();
        }*/
    }
    
    private boolean isBetweenIncl(int x, int lower, int upper) {
        return x >= lower ? x <= upper : false;
    }
    
    private Reg indexToReg(int index) {
        return Reg.values()[index];
    }
}
