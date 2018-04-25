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
    
    private final Cpu cpu;
    private final RamController vRam;
    
    private long nextNonIdleCycle = 0;
    private long lcdOnCycle = 0;
    
    private Mode nextMode;
    
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
        M0_HBLANK(51, Stat.INT_MODE0), M1_VBLANK(1140, Stat.INT_MODE1), M2_SPRITE_MEM(20, Stat.INT_MODE2), M3_VIDEO_MEM(43, null);
        // TODO Do these count as magic numbers ?
        
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
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END) {
            return vRam.read(address);
        } else if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END) {
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
                    nextNonIdleCycle  = Long.MAX_VALUE;
                }
                
                rf.set(r, data);
            } else if (r == Reg.STAT) {
                int lsb = Bits.clip(3, rf.get(Reg.STAT));
                int msb = Bits.extract(data, 3, 5) << 3;
                rf.set(r, msb | lsb);
            } else if (r == Reg.LY || r == Reg.LYC) {
                writeToLyLyc(r, data);
            } else {
                rf.set(r, data);
            }
        }
    }
    
    /**
     * Returns the current lcd image. If the controller has not drawn an image yet,
     * returns a blank image (all zero)
     * @return an LcdImage
     */
    public LcdImage currentImage() {
    	return emptyImage();
    }
    
    @Override
    public void cycle(long cycle) {
        if (cycle == nextNonIdleCycle) {
        	// TODO System.out.println((nextNonIdleCycle - lcdOnCycle) / FULL_LINE_CYCLES + currentMode().name());
        	setMode(nextMode);
            reallyCycle();
        }
        
        if (nextNonIdleCycle == Long.MAX_VALUE && rf.testBit(Reg.LCDC, Lcdc.LCD_STATUS)) {
            lcdOnCycle = cycle;
            nextNonIdleCycle = cycle;
            // TODO Is this correct ?
            reallyCycle();
        }
    }

    /* Private methods */
    
    private void reallyCycle() {
        Mode mode = currentMode();
        raiseStatIfModeFlagOn(mode);
        
        // TODO : LY what to do ?
        
        switch (mode) {
	        case M1_VBLANK: {
	        	cpu.requestInterrupt(Cpu.Interrupt.VBLANK);
	        	queueMode(Mode.M0_HBLANK);
	        	lcdOnCycle = nextNonIdleCycle + mode.cycles;
	        } break;
	        case M2_SPRITE_MEM: {
	        	queueMode(Mode.M3_VIDEO_MEM);
	        } break;
	        case M3_VIDEO_MEM: {
	        	queueMode(Mode.M0_HBLANK);
	        } break;
	        case M0_HBLANK:{
	        	if (allLinesDrawn()) {
	        		queueMode(Mode.M1_VBLANK);
	        	} else {
	        		queueMode(Mode.M2_SPRITE_MEM);
	        	}
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
    
    private boolean allLinesDrawn() {
    	long duration = nextNonIdleCycle - lcdOnCycle;
    	long numberOfLinesDrawn = duration / FULL_LINE_CYCLES;
    	
    	return numberOfLinesDrawn >= LCD_HEIGHT;
    }
    
    private Reg indexToReg(int index) {
        return Reg.values()[index];
    }
    
    private Mode currentMode() {
        int msb = rf.testBit(Reg.STAT, Stat.MODE1) ? 0b10 : 0b00;
        int lsb = rf.testBit(Reg.STAT, Stat.MODE0) ? 0b01 : 0b00;
        
        return Mode.values()[msb | lsb];
    }
    
    private void setMode(Mode mode) {
        int modeCode = mode.index();
        
        rf.setBit(Reg.STAT, Stat.MODE1, Bits.test(modeCode, Stat.MODE1));
        rf.setBit(Reg.STAT, Stat.MODE0, Bits.test(modeCode, Stat.MODE0));
    }
    
    private void queueMode(Mode mode) {
    	nextMode = mode;
    }
    
    private void writeToLyLyc(Reg r, int data) {
        Preconditions.checkArgument(r == Reg.LY || r == Reg.LYC);
        Preconditions.checkBits8(data);
        
        if (rf.testBit(Reg.STAT, Stat.INT_LYC)) {
            cpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
        }
        
        boolean equal = rf.get(Reg.LY) == rf.get(Reg.LYC);
        rf.setBit(Reg.STAT, Stat.LYC_EQ_LY, equal);
        rf.set(r, data);
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
    
    
    
}
