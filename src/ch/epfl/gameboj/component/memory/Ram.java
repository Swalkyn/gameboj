package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Random access memory that stores a byte array
 * @author sylvainkuchen
 *
 */
public final class Ram implements Component {
    
    private final byte[] memory;
    
    /**
     * Creates a new Ram object
     * @param size : the size of memory in bytes
     * @throws IllegalArgumentException if size strictly negative
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
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
    @Override
    public int read(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(index + " is out of bounds");
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
    @Override
    public void write(int index, int value) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException(index + " is out of bounds");
        }
        
        memory[index] = (byte) Preconditions.checkBits8(value);
    }
}
