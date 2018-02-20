package ch.epfl.gameboj.component.memory;

import java.util.Arrays;

/**
 * Read-only memory that stores a byte array
 * @author luca
 */
public final class Rom {
    
    private final byte[] memory;
    
    /**
     * Creates a new ROM
     * @param data : byte array to be copied into memory
     */
    public Rom(byte[] data) {
        if (data == null) {
            throw new NullPointerException("data cannot be null");
        }
        
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
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        
        return Byte.toUnsignedInt(memory[index]);
    }    
}
