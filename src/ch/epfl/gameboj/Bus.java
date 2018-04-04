package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * Represents a bus connecting components
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Bus {
    private ArrayList<Component> components = new ArrayList<>();
    
    /**
     * Attaches a component to the bus
     * @param component : non-null
     * @throws NullPointerException
     */
    public void attach(Component component) {     
        components.add(Objects.requireNonNull(component));
    }
    
    /**
     * Reads data at specified address. If address does not exist or there is no data, returns 0xFF
     * @param address : 16-bit address
     * @throws IllegalArgumentException if address invalid
     * @return the data if it exists, 0xFF otherwise
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        for (Component component : components) {
            int data = component.read(address);
            if (data != Component.NO_DATA) {
                return data;
            }
        }
        
        return 0xFF;
    }
    
    /**
     * Writes data to all attached components with given address
     * @param address : 16-bit address
     * @param data : 8-bit value
     * @throws IllegalArgumentException if address or data invalid
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        for (Component component : components) {
            component.write(address, data);
        }
    }
}
