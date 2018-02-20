package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Ram Controller, allows to assign an address space to given ram
 * @author luca
 *
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
     * @throws IllegalArgumentException if addresses are invalid or address range is too large
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        RAM = Objects.requireNonNull(ram);
        START_ADDRESS = Preconditions.checkBits16(startAddress);
        END_ADDRESS = Preconditions.checkBits16(endAddress);
        
        if (END_ADDRESS - START_ADDRESS > RAM.size()) {
            throw new IllegalArgumentException("Ram is not big enough for given address range");
        }
    }
    
    /**
     * Creates a new RamController with biggest possible range (depends on size of ram)
     * @param ram : non-null
     * @param startAddress : the starting address at which the ram can be accessed
     * @throws NullPointerException if ram is null
     * @throws IllegalArgumentException if addresses are invalid or address range is too large
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, ram.size());
    }

    /**
     * Reads data at specified address
     * @param address, 16-bits, in range
     * @throws IllegalArgumentException if not 16-bits
     * @throws IndexOutOfBoundsException if not in range
     * @return the data
     */
    @Override
    public int read(int address) {
        return RAM.read(checkAddress(address) - START_ADDRESS);
    }

    /**
     * If address is valid, writes data to that address
     * @param address : 16-bits
     * @param data : 8-bits
     * @throws IllegalArgumentException if address not 16 bits or data not 8 bits
     * @throws IndexOutOfBoundsException if address not in range
     */
    @Override
    public void write(int address, int data) {
        RAM.write(checkAddress(address) - START_ADDRESS, Preconditions.checkBits8(data));
    }
    
    /**
     * Checks validity of address and if it is in range
     * @param address : 16-bits, between START_ADDRESS (included) and END_ADDRESS (excluded)
     * @throws IllegalArgumentException if not 16-bits
     * @throws IndexOutOfBoundsException if not in range
     * @return the address if valid
     */
    private int checkAddress(int address) {
        Preconditions.checkBits16(address);
        
        if (address < START_ADDRESS || address >= END_ADDRESS) {
            throw new IndexOutOfBoundsException();
        }
        
        return address;
    }
    
}
