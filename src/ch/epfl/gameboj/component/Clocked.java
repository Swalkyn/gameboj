package ch.epfl.gameboj.component;


/**
 * Represents a component controlled by system clock
 * @author sylvainkuchen
 */
public interface Clocked {
    
    /**
     * Evolves state of component
     * @param cycle : the number of cycles since start of clock
     */
    void cycle(long cycle);
}
