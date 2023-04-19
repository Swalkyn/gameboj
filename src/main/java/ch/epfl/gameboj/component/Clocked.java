package ch.epfl.gameboj.component;


/**
 * Represents a component controlled by system clock
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public interface Clocked {
    
    /**
     * Evolves state of component
     * @param cycle : the number of cycles since start of clock
     */
    void cycle(long cycle);
}
