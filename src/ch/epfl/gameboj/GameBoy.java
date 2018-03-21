package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gamboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Represents a gameboy
 * 
 *  @author Sylvain Kuchen (282380)
 *  @author Luca Bataillard (282152)
 */
public class GameBoy {
    
    private Bus mBus;
    private Cpu mCpu;
    private Timer mTimer;
    private RamController workRam;
    private RamController echoRam;
    private BootRomController bootRom;
    
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
        
        mCpu.attachTo(mBus);
        mTimer.attachTo(mBus);
        workRam.attachTo(mBus);
        echoRam.attachTo(mBus);
        bootRom.attachTo(mBus);
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
     * Runs the gameboy up to passed cycle
     * @param cycle : cycle up to which the processor will run
     * @throws IllegalArgumentException if the gameboy has already run up to given cycle
     */
    public void runUntil(long cycle) {
        if (cycle < cycles()) {
            throw new IllegalArgumentException("Gameboy has already run up to this cycle");
        }
        
        for (long i = 0; i < cycle; i++) {
            mTimer.cycle(i);
            mCpu.cycle(i);
        }
        
        numberOfCycles = cycle;
    }
    
    /**
     * @return returns the number of cycles the cpu has run
     */
    public long cycles() {
        return numberOfCycles;
    }
}
