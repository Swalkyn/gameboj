package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * Random access memory that stores a byte array
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
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
        Objects.checkIndex(index, size());
        
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
        Objects.checkIndex(index, size());
        
        memory[index] = (byte) Preconditions.checkBits8(value);
    }
}
