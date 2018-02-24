package ch.epfl.gameboj;

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
    
    /**
     * Creates a new gameboy, with a cartrige inserted
     * @param cartrige :
     */
    public GameBoy(Object cartrige) {
        Ram ram = new Ram(AddressMap.WORK_RAM_SIZE);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        
        mBus = new Bus();
        mBus.attach(workRam);
        mBus.attach(echoRam);
    }
    
    /**
     * @return the main bus of the gameboy
     */
    public Bus bus() {
        return mBus;
    }
    
}
