package ch.epfl.gameboj.component.memory;

import java.util.Arrays;

import ch.epfl.gameboj.component.Component;

/**
 * Read-only memory that stores a byte array
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Rom implements Component {
    
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
    @Override
    public int read(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        
        return Byte.toUnsignedInt(memory[index]);
    }

    @Override
    public void write(int address, int data) {
        // Does nothing
    }    
}
