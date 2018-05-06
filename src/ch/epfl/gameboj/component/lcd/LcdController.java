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

    private static final int NUMBER_OF_TILES = 32;
    private static final int LINES_PER_TILE = 8;
    private static final int FULL_LINE_SIZE = 256;
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final int FULL_LINE_CYCLES = 114;
    
    private static final int LINES_IN_VBLANK = 10;
    
    private static final int TILE_BYTES = 16;
    private static final int SPRITE_BYTES = 4;
    private static final int SPRITES_IN_MEMORY = AddressMap.OAM_RAM_SIZE / SPRITE_BYTES;
    private static final int SPRITES_PER_LINE = 10;

    
    private static final int X_OFFSET = 8;
    private static final int Y_OFFSET = 16;

    private final Cpu cpu;
    private final RamController vRam;
    private final RamController oamRam;
    private Bus bus;
    
    private final RegisterFile<Reg> rf = new RegisterFile<>(Reg.values());

    private long nextNonIdleCycle = 0;
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
    
    private enum Sprite implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    
    /**
     * Creates a new LcdController
     * @param cpu : cpu of the gameboy, needed to raise interrupts
     */
    public LcdController(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);

        Ram vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        this.vRam = new RamController(vRam, AddressMap.VIDEO_RAM_START);

        Ram oamRam = new Ram(AddressMap.OAM_RAM_SIZE);
        this.oamRam = new RamController(oamRam,  AddressMap.OAM_START);
        
        nextMode = Mode.M2_SPRITE_MEM;
        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
    }
    
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END) {
            return vRam.read(address);
        } else if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END) {
            return rf.get(indexToReg(address - AddressMap.REGS_LCDC_START));
        } else if (AddressMap.OAM_START <= address && address < AddressMap.OAM_END) {
            return oamRam.read(address);
        }

        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END) {
            vRam.write(address, data);
        } else if (AddressMap.OAM_START <= address && address < AddressMap.OAM_END) {
            oamRam.write(address, data);
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

    @Override
    public void cycle(long cycle) {
        if (cycle == nextNonIdleCycle) {
            setMode(nextMode);
            reallyCycle();
        }
        
        if (quickCopyEnabled) {
            copyByte();
        }
        
        if (nextNonIdleCycle == Long.MAX_VALUE && rf.testBit(Reg.LCDC, Lcdc.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            reallyCycle();
        }
    }

    private void reallyCycle() {
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
    
    
    /* IO methods */
    
    private void writeToReg(int address, int data) {
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
            
        } else if (r == Reg.DMA) {
            rf.set(r, data);
            quickCopyEnabled = true;
        } else if (!(r == Reg.LY || r == Reg.LYC)) {
            rf.set(r, data);
        }
    }
    
    private void copyByte() {
        if (quickCopyIndex < AddressMap.OAM_RAM_SIZE) {
            int sourceAddress = Bits.make16(rf.get(Reg.DMA), 0x00);
            oamRam.write(AddressMap.OAM_START + quickCopyIndex, bus.read(sourceAddress + quickCopyIndex));
            quickCopyIndex++;
        } else {
            quickCopyEnabled = false;
            quickCopyIndex = 0;
        }
    }
    
    
    /* Mode control */

    private void raiseStatIfModeFlagOn(Mode mode) {
        if (mode.intMode != null && rf.testBit(Reg.STAT, mode.intMode)) {
            cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
        }
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
        int numberOfLines = LCD_HEIGHT + LINES_IN_VBLANK;
        writeToLyLyc(Reg.LY, (currentLine() + 1) % numberOfLines);
    }
    
    private boolean enteringVBlank() {
        return currentLine() == LCD_HEIGHT;
    }
    
    private boolean exitingVBlank() {
        return currentLine() == LCD_HEIGHT + LINES_IN_VBLANK - 1;
    }
    
    
    /* Line drawing */
    
    private LcdImageLine computeLine() {
        int lineIndex = (scy() + currentLine()) % FULL_LINE_SIZE;
        
        LcdImageLine line = backgroundLine(lineIndex);
        LcdImageLine bgSpritesLine = spritesLine(currentLine(), true);
        LcdImageLine fgStritesLine = spritesLine(currentLine(), false);
        
        line = mergeSpritesWithBackround(line, bgSpritesLine, fgStritesLine);
        
        line = addWindowLineIfNecessary(line);
                
        return line;
    }

    private LcdImageLine backgroundLine(int lineIndex) {
        if (rf.testBit(Reg.LCDC, Lcdc.BG)) {
            LcdImageLine backLine = extractLine(lineIndex, memoryStart(Lcdc.BG_AREA));
            return backLine.mapColors(rf.get(Reg.BGP)).extractWrapped(scx(), LCD_WIDTH);
        } else {
            return emptyLine().extractWrapped(scx(), LCD_WIDTH);
        }
    }
    
    private LcdImageLine windowLine(int lineIndex) {
        LcdImageLine winLine = extractLine(lineIndex, memoryStart(Lcdc.WIN_AREA));
        return winLine.mapColors(rf.get(Reg.BGP)).extractWrapped(0, LCD_WIDTH).shift(wx());
    }
    
    private LcdImageLine spritesLine(int lineIndex, boolean background) {
        LcdImageLine line = new LcdImageLine.Builder(LCD_WIDTH).build();
        
        for (int spriteData : spritesIntersectingLine()) {
            int spriteIndex = Bits.clip(8, spriteData);
            
            if (spriteAttr(spriteIndex, Sprite.BEHIND_BG) == background) {
                int msb = spriteByte(spriteIndex, lineIndex, true);
                int lsb = spriteByte(spriteIndex, lineIndex, false);
                
                LcdImageLine oneSpriteLine = new LcdImageLine.Builder(LCD_WIDTH).setBytes(0, msb, lsb).build();
                
                oneSpriteLine = oneSpriteLine.shift(spriteX(spriteIndex)).mapColors(spritePalette(spriteIndex));
                line = oneSpriteLine.below(line);
            }
        }
        return line;
    }
    
    private LcdImageLine extractLine(int lineIndex, int memoryStart) {
        LcdImageLine.Builder lb = new LcdImageLine.Builder(FULL_LINE_SIZE);
        int startTileIndex = (lineIndex / LINES_PER_TILE) * NUMBER_OF_TILES;
        
        for(int i = 0; i < NUMBER_OF_TILES; i++) {
            int tileIndex = vRam.read(memoryStart + startTileIndex + i);
            int lsb = Bits.reverse8(vRam.read(tileAddress(tileIndex) + (lineIndex % LINES_PER_TILE) * 2));
            int msb = Bits.reverse8(vRam.read(tileAddress(tileIndex) + (lineIndex % LINES_PER_TILE) * 2 + 1));
            
            lb.setBytes(i, msb, lsb);
        }
        
        return lb.build();
    }
    
    private LcdImageLine mergeSpritesWithBackround(LcdImageLine background, LcdImageLine bgSprites, LcdImageLine fgSprites) {
        BitVector opacityMask = background.opacity().and(bgSprites.opacity().not()); // TODO : correct opacity ?
        
        return bgSprites.below(background, opacityMask).below(fgSprites);
    }
    
    private LcdImageLine addWindowLineIfNecessary(LcdImageLine line) {
        if (currentLine() >= rf.get(Reg.WY) && windowOn()) {
            int winLineIndex = currentLine() - rf.get(Reg.WY);
            LcdImageLine winLine = windowLine(winLineIndex);
            
            line = line.join(winLine, wx());
        }
        
        return line;
    }
    
    
    /* General utilities */
    
    private static LcdImage emptyImage() {
        LcdImageLine emptyLine = new LcdImageLine.Builder(LCD_WIDTH).build();
        LcdImage.Builder lcdBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);

        for (int i = 0; i < LCD_HEIGHT; i++) {
            lcdBuilder.setLine(i, emptyLine);
        }

        return lcdBuilder.build();
    }
    
    private static LcdImageLine emptyLine() {
        return new LcdImageLine.Builder(FULL_LINE_SIZE).build();
    }
    
    private int[] spritesIntersectingLine() {
        int spriteHeight = spritesHeight();
        int[] lineSprites = new int[SPRITES_PER_LINE];
        int i = 0; // TODO come up with better names
        int j = 0;
        
        while (i < SPRITES_IN_MEMORY && j < SPRITES_PER_LINE) {
            int distance = currentLine() - spriteY(i);
            if (0 <= distance && distance < spriteHeight) {
                lineSprites[j] = Bits.make16(spriteX(i), i);
                j++;
            }
            i++;
        }
        
        Arrays.sort(lineSprites, 0, j);
        
        return Arrays.copyOfRange(lineSprites, 0, j);
    }
    
    private int memoryStart(Bit b) {
        int areaIndex = rf.testBit(Reg.LCDC, b) ? 1 : 0;
        return AddressMap.BG_DISPLAY_DATA[areaIndex];
    }
    
    private int tileAddress(int index) {
        final int OFFSET = 0x80; // TODO en haut ?
        
        if (rf.testBit(Reg.LCDC, Lcdc.TILE_SOURCE)) {
            return AddressMap.TILE_SOURCE[1] + index * TILE_BYTES;
        } else {
            int shiftedIndex = Bits.clip(8, index + OFFSET);
            return AddressMap.TILE_SOURCE[0] + shiftedIndex * TILE_BYTES;
        }
    }
    
    private boolean windowOn() {
        final int WX_HIGH = 160; // TODO en haut ?
        final int WX_LOW = 0;
        
        return WX_LOW <= wx() && wx() < WX_HIGH && rf.testBit(Reg.LCDC, Lcdc.WIN);
    }

    
    /* Registers utilities */
    
    private Reg indexToReg(int index) {
        return Reg.values()[index];
    }
    
    private int wx() {
        final int WX_OFFSET = 7;
        return rf.get(Reg.WX) - WX_OFFSET;
    }
    
    private int scx() {
        return rf.get(Reg.SCX);
    }
    
    private int scy() {
        return rf.get(Reg.SCY);
    }
    
    private int spritesHeight() {
        return rf.testBit(Reg.LCDC, Lcdc.OBJ_SIZE) ? 16 : 8;
    }

    
    /* Sprites utilities */
    
    private int spriteY(int index) {
        Objects.checkIndex(index, SPRITES_IN_MEMORY);
        
        return oamRam.read(AddressMap.OAM_START + SPRITE_BYTES * index) - Y_OFFSET;
    }
    
    private int spriteX(int index) {
        Objects.checkIndex(index, SPRITES_IN_MEMORY);
        
        return oamRam.read(AddressMap.OAM_START + SPRITE_BYTES * index + 1) - X_OFFSET;
    }
    
    private int spriteTileAddress(int index) {
        Objects.checkIndex(index, SPRITES_IN_MEMORY);
        
        return AddressMap.TILE_SOURCE[1] + oamRam.read(AddressMap.OAM_START + SPRITE_BYTES * index + 2) * TILE_BYTES; // TODO : signal RamController
    }
    
    private boolean spriteAttr(int index, Bit bit) {
        Objects.checkIndex(index, SPRITES_IN_MEMORY);
        
        return Bits.test(oamRam.read(AddressMap.OAM_START + SPRITE_BYTES * index + 3), bit);
    }
    
    private int spriteByte(int index, int lineIndex, boolean msb) {
        boolean flipH = spriteAttr(index, Sprite.FLIP_H);
        boolean flipV = spriteAttr(index, Sprite.FLIP_V);
        
        int byteIndex;
        if (flipV) {
            byteIndex = 2 * (spritesHeight() - (lineIndex - spriteY(index))) + (msb ? 1 : 0);
        } else {
            byteIndex = 2 * (lineIndex - spriteY(index)) + (msb ? 1 : 0);
        }
        
        int readByte = vRam.read(spriteTileAddress(index) + byteIndex);
        
        if (!flipH) {
            readByte = Bits.reverse8(readByte);
        }
        
        return readByte;
    }
    
    private int spritePalette(int index) {
        return spriteAttr(index, Sprite.PALETTE) ? rf.get(Reg.OBP1) : rf.get(Reg.OBP0);
    }
    
    
    /* Tests */
    
    /**
     * @return image of the tiles used for drawing the sprites
     */
    public LcdImage spriteTiles() {
        LcdImage.Builder ib = new LcdImage.Builder(8 * SPRITES_IN_MEMORY, LINES_PER_TILE);
        
        for (int i = 0; i < LINES_PER_TILE; i++) {
            LcdImageLine.Builder lb = new LcdImageLine.Builder(8 * SPRITES_IN_MEMORY);
                    
            for (int j = 0; j < SPRITES_IN_MEMORY; j++) {
                int msb = Bits.reverse8(vRam.read(spriteTileAddress(j) + (2 * i) + 1));
                int lsb = Bits.reverse8(vRam.read(spriteTileAddress(j) + (2 * i)));
                lb.setBytes(j, msb, lsb);
            }
            
            ib.setLine(i, lb.build());
        }
        
        return ib.build();
    }
}
