package ch.epfl.gameboj;

/**
 * Interface implemented by enum representing registers of the same register file
 * @author luca
 *
 */
public interface Register {
    
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
    
}
