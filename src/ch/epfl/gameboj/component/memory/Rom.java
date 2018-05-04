package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Read-only memory that stores a byte array
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Rom {
    
    private final byte[] memory;
    
    /**
     * Creates a new ROM
     * @param data : byte array to be copied into memory
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        
        memory = Arrays.copyOf(data, data.length);
    }
    
    /**
     * @return the size of the ROM
     */
    public int size() {
        return memory.length;
    }
    
    /**
     * Reads memory at specified index
     * @param index: index of memory to be accessed
     * @throws IndexOutOfBoundsException
     * @return the unsigned byte at specified index
     */
    public int read(int index) {
        Objects.checkIndex(index, size());
        
        return Byte.toUnsignedInt(memory[index]);
    }
    
}
