package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Represents a gameboy
 * 
 *  @author Sylvain Kuchen (282380)
 *  @author Luca Bataillard (282152)
 */
public final class GameBoy {
	
	public static final long CYCLES_PER_SECOND = (long) Math.pow(2, 20);
	public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND * 1E-9;
    
    private final Bus mBus;
    private final Cpu mCpu;
    private final Timer mTimer;
    private final RamController workRam;
    private final RamController echoRam;
    private final BootRomController bootRom;
    private final LcdController lcd;
    private final Joypad joypad;
    
    private long numberOfCycles = 0;
    
    /**
     * Creates a new gameboy, with a cartridge inserted
     * @param cartridge :
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        
        mBus = new Bus();
        mCpu = new Cpu();
        
        mTimer = new Timer(mCpu);

        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        bootRom = new BootRomController(cartridge);
        
        lcd = new LcdController(mCpu);
        joypad = new Joypad(mCpu);
        
        mCpu.attachTo(mBus);
        mTimer.attachTo(mBus);
        workRam.attachTo(mBus);
        echoRam.attachTo(mBus);
        bootRom.attachTo(mBus);
        lcd.attachTo(mBus);
        joypad.attachTo(mBus);
    }
    
    /**
     * @return the main bus of the gameboy
     */
    public Bus bus() {
        return mBus;
    }
    
    /**
     * @return the cpu of the gameboy
     */
    public Cpu cpu() {
        return mCpu;
    }
    
    /**
     * @return the timer of the gameboy
     */
    public Timer timer() {
        return mTimer;
    }
    
    /**
     * @return the lcd controller of the gameboy
     */
    public LcdController lcdController() {
        return lcd;
    }
    
    /**
     * @return the joypad of the gameboy
     */
    public Joypad joypad() {
    		return joypad;
    }
    
    /**
     * @return returns the number of cycles the cpu has run
     */
    public long cycles() {
        return numberOfCycles;
    }
    
    /**
     * Runs the gameboy up to passed cycle
     * @param cycle : cycle up to which the processor will run
     * @throws IllegalArgumentException if the gameboy has already run up to given cycle
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycle >= numberOfCycles);
        
        while(cycles() < cycle) {
            mTimer.cycle(numberOfCycles);
            lcd.cycle(numberOfCycles);
            mCpu.cycle(numberOfCycles);
            numberOfCycles++;
        }
    }
}
