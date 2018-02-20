package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;

/**
 * Random access memory that stores a byte array
 * @author sylvainkuchen
 *
 */
public final class Ram {
    
    private final byte[] memory;
    
    /**
     * Creates a new Ram object
     * @param size : the size of memory in bytes
     */
    public Ram(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be strictly negative");
        }
        
        memory = new byte[size];
    }
    
    /**
     * @return the size of the memory in bytes
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
    
    /**
     * Writes a byte value to memory at specified index
     * @param index : index of memory to be written
     * @param value : value to be inserted
     * @throws IndexOutOfBoundsException
     * @throws IllegalArgumentException if value is not a byte
     */
    public void write(int index, int value) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        
        memory[index] = (byte) Preconditions.checkBits8(value);
    }
}
