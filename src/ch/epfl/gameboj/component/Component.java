package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * Interface representing a component of the gameboy, connected to a bus
 * @author luca
 */
public interface Component {
    static final int NO_DATA = 0x100;
    
    /**
     * Reads memory at specified address 
     * @param address
     * @return the read data or NO_DATA otherwise
     * @throws IllegalArgumentException if address is not 16 bits
     */
    int read(int address);
    
    /**
     * Stores given data at specified address, does nothing if component cannot store data at that address
     * @param address : 16-bit address
     * @param data : 8-bit value to be stored
     * @throws IllegalArgumentException if address is not 16 bits or data is not 8 bits
     */
    void write(int address, int data);
    
    /**
     * Attaches component to bus
     * @param bus
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }
}
