package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Ram Controller, allows to assign an address space to given ram
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class RamController implements Component {

    private final Ram RAM;
    private final int START_ADDRESS;
    private final int END_ADDRESS;
    
    /**
     * Creates a new RamController accessed at specified address range
     * @param ram : non-null
     * @param startAddress : the starting address at which the ram can be accessed
     * @param endAddress : the end address
     * @throws NullPointerException if ram is null
     * @throws IllegalArgumentException if addresses are invalid or address range is too large or negative
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        START_ADDRESS = Preconditions.checkBits16(startAddress);
        END_ADDRESS = Preconditions.checkBits16(endAddress);
        RAM = Objects.requireNonNull(ram);

        Preconditions.checkArgument(startAddress <= endAddress);
        Preconditions.checkArgument(endAddress - startAddress <= RAM.size());
    }
    
    /**
     * Creates a new RamController with biggest possible range (depends on size of ram)
     * @param ram : non-null
     * @param startAddress : the starting address at which the ram can be accessed
     * @throws NullPointerException if ram is null
     * @throws IllegalArgumentException if addresses are invalid or address range is too large
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }

    /**
     * Reads data at specified address
     * @param address, 16-bits, in range
     * @throws IllegalArgumentException if not 16-bits
     * @return the data
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (address < START_ADDRESS || address >= END_ADDRESS) {
            return Component.NO_DATA;
        }
        
        return RAM.read(address - START_ADDRESS);
    }

    /**
     * If address is valid, writes data to that address
     * @param address : 16-bits
     * @param data : 8-bits
     * @throws IllegalArgumentException if address not 16 bits or data not 8 bits
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        
        if (address >= START_ADDRESS && address < END_ADDRESS) {
            RAM.write(address - START_ADDRESS, data);
        }
    }
}
