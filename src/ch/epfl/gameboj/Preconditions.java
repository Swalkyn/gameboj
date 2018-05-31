package ch.epfl.gameboj;

/**
 * Collection of utility functions used to check common preconditions
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public interface Preconditions {
    
    /**
     * Throws an exception if the given expression is false
     * @param b : expression to be checked
     * @throws : IllegalArgumentException
     */
    static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException("Condition failed");
        }
    }
    
    /**
     * Verifies if an integer is between 0 and 0xFF, if not throws an exception
     * @param v : the integer to be checked
     * @throws : IllegalArgumentException
     * @return the integer if correct
     */
    static int checkBits8(int v) {
        //System.out.println(v);
        checkArgument(0 <= v && v <= 0xFF);
        return v;
    }
    
    /**
     * Verifies if an integer is between 0 and 0xFFFF, if not throws an exception
     * @param v : the integer to be checked
     * @throws : IllegalArgumentException
     * @return the integer if correct
     */
    static int checkBits16(int v) {
        checkArgument(0 <= v && v <= 0xFFFF);
        return v;
    }
}
