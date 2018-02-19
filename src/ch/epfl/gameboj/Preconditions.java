package ch.epfl.gameboj;

/**
 * Collection of utility functions used to check common preconditions
 * @author sylvainkuchen
 *
 */
public interface Preconditions {
    
    /**
     * Throws an exception if the given expression is false
     * @param b : expression to be checked
     * @throws : IllegalArgumentException
     */
    static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Verifies if an integer is between 0 and 0xFF, if not throws an exception
     * @param v : the integer to be checked
     * @throws : IllegalArgumentException
     * @return the integer if correct
     */
    static int checkBits8(int v) {
        if (v < 0 || v > 0xFF) {
            throw new IllegalArgumentException();
        }
        
        return v;
    }
    
    /**
     * Verifies if an integer is between 0 and 0xFFFF, if not throws an exception
     * @param v : the integer to be checked
     * @throws : IllegalArgumentException
     * @return the integer if correct
     */
    static int checkBits16(int v) {
        if (v < 0 || v > 0xFFFF) {
            throw new IllegalArgumentException();
        }
        
        return v;
    }
}
