package ch.epfl.gameboj;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * Represents a gameboy
 * @author luca
 *
 */
public class GameBoy {
    
    private Bus mBus;
    private RamController workRam;
    private RamController echoRam;
    private Cpu mCpu;
    private long numberOfCycles = 0;
    
    /**
     * Creates a new gameboy, with a cartrige inserted
     * @param cartrige :
     */
    public GameBoy(Object cartrige) {
        mBus = new Bus();

        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        mCpu = new Cpu();
        
        mCpu.attachTo(mBus);
        workRam.attachTo(mBus);
        echoRam.attachTo(mBus);
    }
    
    /**
     * @return the cpu of the gameboy
     */
    public Cpu cpu() {
        return mCpu;
    }
    
    /**
     * @return the main bus of the gameboy
     */
    public Bus bus() {
        return mBus;
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
