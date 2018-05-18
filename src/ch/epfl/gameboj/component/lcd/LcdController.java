package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
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

    private static final int TILES_PER_LINE = 32;
    private static final int LINES_IN_TILE = 8;
    private static final int FULL_LINE_SIZE = 256;

    private static final int FULL_LINE_CYCLES = 114;
    private static final int LINES_IN_VBLANK = 10;
    private static final int LINES_PER_FRAME = LCD_HEIGHT + LINES_IN_VBLANK;

    private static final int TILE_ADDRESS_OFFSET = 0x80;
    private static final int TILE_BYTES = 16;
    private static final int SPRITE_BYTES = 4;
    private static final int SPRITES_IN_MEMORY = AddressMap.OAM_RAM_SIZE / SPRITE_BYTES;
    private static final int SPRITES_PER_LINE = 10;

    private static final int X_OFFSET = 8;
    private static final int Y_OFFSET = 16;
    private static final int WX_HIGH = 160;
    private static final int WX_LOW = 0;
    private static final int WX_OFFSET = 7;


    private final Cpu cpu;
    private final RamController vRam;
    private final Ram oamRam;
    private Bus bus;

    private final RegisterFile<Reg> rf = new RegisterFile<>(Reg.values());

    private long nextNonIdleCycle = Long.MAX_VALUE;
    private int nextLineIndex = 0;
    private Mode nextMode;

    private boolean quickCopyEnabled = false;
    private int quickCopyIndex = 0;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage image = emptyImage();

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

    private enum SpriteData {
        Y, X, TILE, ATTR
    }
    
    private enum SpriteAttr implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    /**
     * Creates a new LcdController
     * 
     * @param cpu : cpu of the gameboy, needed to raise interrupts
     * @throws NullPointerException if cpu is null
     */
    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        Ram vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        this.vRam = new RamController(vRam, AddressMap.VIDEO_RAM_START);

        this.oamRam = new Ram(AddressMap.OAM_RAM_SIZE);

        nextMode = Mode.M2_SPRITE_MEM;
        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END) {
            return vRam.read(address);
        } else if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END) {
            return rf.get(addressToReg(address));
        } else if (AddressMap.OAM_START <= address && address < AddressMap.OAM_END) {
            return oamRam.read(address - AddressMap.OAM_START);
        }

        return Component.NO_DATA;
    }

    /*
     * (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END) {
            vRam.write(address, data);
        } else if (AddressMap.OAM_START <= address && address < AddressMap.OAM_END) {
            oamRam.write(address - AddressMap.OAM_START, data);
        } else if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END) {
            writeToReg(address, data);
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

    /*
     * (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (cycle == nextNonIdleCycle) {
            reallyCycle();
        }

        if (quickCopyEnabled) {
            copyByte();
        }

        if (isWokenUp()) {
            nextNonIdleCycle = cycle;
            nextMode = Mode.M2_SPRITE_MEM;
            nextLineIndex = 0;
            reallyCycle();
        }
    }

    private void reallyCycle() {
        updateLineIndex();
        setMode(nextMode);

        Mode mode = currentMode();

        switch (mode) {
        case M1_VBLANK: {
            if (enteringVBlank()) {
                cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
                image = nextImageBuilder.build();
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            } else if (exitingVBlank()) {
                nextMode = Mode.M2_SPRITE_MEM;
            }

            incrLineIndex();
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

            incrLineIndex();
        } break;
        }

        nextNonIdleCycle += mode.cycles;
    }

    
    /* IO methods */

    private void writeToReg(int address, int data) {
        Reg r = addressToReg(address);
        
        switch (r) {
        case LCDC:
            if (!Bits.test(data, Lcdc.LCD_STATUS)) {
                writeToLyLyc(Reg.LY, 0);
                setMode(Mode.M0_HBLANK);
                nextNonIdleCycle = Long.MAX_VALUE;
            }
            rf.set(r, data);
            break;
        case STAT:
            int lsb = Bits.clip(3, rf.get(Reg.STAT));
            int msb = Bits.extract(data, 3, 5) << 3;
            rf.set(r, msb | lsb);
            break;
        case DMA:
            rf.set(r, data);
            quickCopyEnabled = true;
            break;
        case LYC:
            writeToLyLyc(r, data);
            break;
            
        default:
            if (r != Reg.LY) {
                rf.set(r, data);
            }
            break;
        }
    }

    private void copyByte() {
        if (quickCopyIndex < AddressMap.OAM_RAM_SIZE) {
            int sourceAddress = Bits.make16(rf.get(Reg.DMA), 0x00);
            oamRam.write(quickCopyIndex, bus.read(sourceAddress + quickCopyIndex));
            quickCopyIndex++;
        } else {
            quickCopyEnabled = false;
            quickCopyIndex = 0;
        }
    }
    

    /* Mode control */
    
    private void updateLineIndex() {
        if (currentLine() != nextLineIndex) {
            writeToLyLyc(Reg.LY, nextLineIndex);
        }
    }

    private void raiseStatIfModeFlagOn(Mode mode) {
        if (mode.intMode != null && rf.testBit(Reg.STAT, mode.intMode)) {
            cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
        }
    }

    private void setMode(Mode mode) {
        int modeCode = mode.index();

        rf.setBit(Reg.STAT, Stat.MODE1, Bits.test(modeCode, Stat.MODE1));
        rf.setBit(Reg.STAT, Stat.MODE0, Bits.test(modeCode, Stat.MODE0));

        raiseStatIfModeFlagOn(mode);
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

    private void incrLineIndex() {
        nextLineIndex = (currentLine() + 1) % LINES_PER_FRAME;
    }

    private boolean enteringVBlank() {
        return currentLine() == LCD_HEIGHT;
    }

    private boolean exitingVBlank() {
        return currentLine() == LINES_PER_FRAME - 1;
    }
    
    private boolean isWokenUp() {
        return nextNonIdleCycle == Long.MAX_VALUE && rf.testBit(Reg.LCDC, Lcdc.LCD_STATUS);
    }
    

    /* Line drawing */

    private LcdImageLine computeLine() {
        int lineIndex = (rf.get(Reg.SCY) + currentLine()) % FULL_LINE_SIZE;

        LcdImageLine line;
        
        line = backgroundLine(lineIndex);
        line = addWindowLine(line);
        line = addSpritesLines(line);

        return line;
    }

    private LcdImageLine backgroundLine(int lineIndex) {
        if (rf.testBit(Reg.LCDC, Lcdc.BG)) {
            LcdImageLine backLine = extractLine(lineIndex, memoryStart(Lcdc.BG_AREA));
            return backLine.mapColors(rf.get(Reg.BGP)).extractWrapped(rf.get(Reg.SCX), LCD_WIDTH);
        } else {
            return emptyLine(LCD_WIDTH);
        }
    }

    private LcdImageLine windowLine(int lineIndex) {
        LcdImageLine winLine = extractLine(lineIndex, memoryStart(Lcdc.WIN_AREA));
        return winLine.mapColors(rf.get(Reg.BGP)).extractWrapped(0, LCD_WIDTH).shift(wx());
    }

    private LcdImageLine spritesLine(int lineIndex, boolean background) {
        LcdImageLine line = new LcdImageLine.Builder(LCD_WIDTH).build();

        for (int spriteIndex : spritesIntersectingLine()) {
            if (spriteAttr(spriteIndex, SpriteAttr.BEHIND_BG) == background) {
                int msb = spriteByte(spriteIndex, lineIndex, true);
                int lsb = spriteByte(spriteIndex, lineIndex, false);

                LcdImageLine singleSpriteLine = new LcdImageLine.Builder(LCD_WIDTH).setBytes(0, msb, lsb).build();
                singleSpriteLine = singleSpriteLine.shift(spriteX(spriteIndex)).mapColors(spritePalette(spriteIndex));
                line = singleSpriteLine.below(line);
            }
        }
        return line;
    }

    private LcdImageLine extractLine(int lineIndex, int memoryStart) {
        LcdImageLine.Builder lb = new LcdImageLine.Builder(FULL_LINE_SIZE);
        int startTileIndex = (lineIndex / LINES_IN_TILE) * TILES_PER_LINE;

        for (int i = 0; i < TILES_PER_LINE; i++) {
            int tileIndex = vRam.read(memoryStart + startTileIndex + i);
            int address = tileAddress(tileIndex) + 2*(lineIndex % LINES_IN_TILE);
            
            int lsb = Bits.reverse8(vRam.read(address));
            int msb = Bits.reverse8(vRam.read(address + 1));

            lb.setBytes(i, msb, lsb);
        }

        return lb.build();
    }

    private LcdImageLine addSpritesLines(LcdImageLine line) {
        if (rf.testBit(Reg.LCDC, Lcdc.OBJ)) {
            LcdImageLine bgSpritesLine = spritesLine(currentLine(), true);
            LcdImageLine fgSpritesLine = spritesLine(currentLine(), false);
            
            BitVector opacityMask = line.opacity().or(bgSpritesLine.opacity().not());
            
            return bgSpritesLine.below(line, opacityMask).below(fgSpritesLine);
        }
        
        return line;
    }

    private LcdImageLine addWindowLine(LcdImageLine line) {
        if (currentLine() >= rf.get(Reg.WY) && windowOn()) {
            int winLineIndex = currentLine() - rf.get(Reg.WY);
            LcdImageLine winLine = windowLine(winLineIndex);

            line = line.join(winLine, wx());
        }

        return line;
    }

    /* General utilities */

    private static LcdImage emptyImage() {
        LcdImageLine emptyLine = emptyLine(LCD_WIDTH);
        LcdImage.Builder lcdBuilder = new LcdImage.Builder(LCD_WIDTH,
                LCD_HEIGHT);

        for (int i = 0; i < LCD_HEIGHT; i++) {
            lcdBuilder.setLine(i, emptyLine);
        }

        return lcdBuilder.build();
    }

    private static LcdImageLine emptyLine(int size) {
        return new LcdImageLine.Builder(size).build();
    }
    

    private int[] spritesIntersectingLine() {
        int spriteHeight = spritesHeight();
        int[] lineSprites = new int[SPRITES_PER_LINE];
        int total = 0;
        int selected = 0;

        while (total < SPRITES_IN_MEMORY && selected < SPRITES_PER_LINE) {
            int distance = currentLine() - spriteY(total);
            if (0 <= distance && distance < spriteHeight) {
                lineSprites[selected] = Bits.make16(spriteX(total) + X_OFFSET, total);
                selected++;
            }
            total++;
        }

        Arrays.sort(lineSprites, 0, selected);
        
        for (int i = 0; i < selected; i++) {
            lineSprites[i] = Bits.clip(8, lineSprites[i]);
        }
        
        return Arrays.copyOfRange(lineSprites, 0, selected);
    }

    private int memoryStart(Bit b) {
        int areaIndex = rf.testBit(Reg.LCDC, b) ? 1 : 0;
        return AddressMap.BG_DISPLAY_DATA[areaIndex];
    }

    private int tileAddress(int index) {
        boolean isFirstArea = rf.testBit(Reg.LCDC, Lcdc.TILE_SOURCE);
        int shiftedIndex = isFirstArea ? index : Bits.clip(8, index + TILE_ADDRESS_OFFSET);
        int areaIndex = isFirstArea ? 1 : 0;
        
        return AddressMap.TILE_SOURCE[areaIndex] + shiftedIndex * TILE_BYTES;
    }

    private boolean windowOn() {
        return WX_LOW <= wx() && wx() < WX_HIGH && rf.testBit(Reg.LCDC, Lcdc.WIN);
    }

    /* Registers utilities */

    private Reg addressToReg(int address) {
        return Reg.values()[address - AddressMap.REGS_LCDC_START];
    }

    private int wx() {
        return Math.max(0, rf.get(Reg.WX) - WX_OFFSET);
    }


    /* Sprites utilities */
    
    private int spritesHeight() {
        return rf.testBit(Reg.LCDC, Lcdc.OBJ_SIZE) ? 16 : 8;
    }
    
    private int spriteData(int index, SpriteData s) {
        Objects.checkIndex(index, SPRITES_IN_MEMORY);
        return Bits.clip(8,  oamRam.read(SPRITE_BYTES * index + s.ordinal()));
    }

    private int spriteY(int index) {
        return spriteData(index, SpriteData.Y) - Y_OFFSET;
    }

    private int spriteX(int index) {
        return spriteData(index, SpriteData.X) - X_OFFSET;
    }

    private int spriteTileAddress(int index) {
        return AddressMap.TILE_SOURCE[1] + spriteData(index, SpriteData.TILE) * TILE_BYTES;
    }

    private boolean spriteAttr(int index, Bit bit) {
        return Bits.test(spriteData(index, SpriteData.ATTR), bit);
    } 

    private int spriteByte(int index, int lineIndex, boolean msb) {
        boolean flipH = spriteAttr(index, SpriteAttr.FLIP_H);
        boolean flipV = spriteAttr(index, SpriteAttr.FLIP_V);

        int line = lineIndex - spriteY(index);
        line = flipV ? spritesHeight() - line : line;
        
        int byteIndex = 2 * line + (msb ? 1 : 0);

        int readByte = vRam.read(spriteTileAddress(index) + byteIndex);
        return flipH ? readByte : Bits.reverse8(readByte);
    }

    private int spritePalette(int index) {
        return spriteAttr(index, SpriteAttr.PALETTE) ? rf.get(Reg.OBP1) : rf.get(Reg.OBP0);
    }
}
