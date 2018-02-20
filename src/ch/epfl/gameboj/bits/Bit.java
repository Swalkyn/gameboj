package ch.epfl.gameboj.bits;

/**
 * To be implemented by enums representing a set of bits
 * @author sylvainkuchen
 *
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
        return 1 << ordinal();
    }
}
