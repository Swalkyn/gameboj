package ch.epfl.gameboj.component.cartridge;

import ch.epfl.gameboj.component.Component;

/**
 * Represents a memory bank of any type
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public abstract class MBC implements Component {
    
    private final int ramSize;
    
    /**
     * Creates a new memory bank with a ram of given size
     * 
     * @param ramSize : size of ram
     */
    public MBC(int ramSize) {
        this.ramSize = ramSize;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public abstract int read(int address);

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public abstract void write(int address, int data);
    
    /**
     * @return the size of the ram managed by the memory bank (0 if no ram)
     */
    public int ramSize() {
        return ramSize;
    }
}
