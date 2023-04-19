package ch.epfl.gameboj.bits;

/**
 * To be implemented by enums representing a set of bits
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public interface Bit {
    /**
     * Method automatically implemented by enum
     * @return the ordinal of the enumeration constant
     */
    int ordinal();
    
    /**
     * Same as ordinal, but with a more understandable name
     * @return the index of the enumeration constant
     */
    default int index() {
        return ordinal();
    }
    
    /**
     * @return the mask of the ordinal
     */
    default int mask() {
        return Bits.mask(ordinal());
    }
}
