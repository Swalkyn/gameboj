package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * Represents an immutable vector of bits, in groups of 32
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class BitVector {

	private static final int BLOCK_SIZE = 32;
    private final int[] blocks;
    private final int numberOfBits;
    
    private static enum Extraction {
        ZERO, WRAP;
    }
    
    public static final class Builder {
        private final int[] bytes;
        private final int fullSize;
        private boolean enabled = true;
        
        /**
         * Creates a new builder for a vector of "size" bits
         * @param size : the vector's number of bits
         * @throws IllegalArgumentException if size is negative or not a multiple of 32
         */
        public Builder(int size) {
            Preconditions.checkArgument(size % BLOCK_SIZE == 0 && size >= 0);
            
            fullSize = size;
            bytes = new int[size / Byte.SIZE];
        }
        
        /**
         * Changes a byte at specified index
         * @param index : index of the byte to be modified
         * @param b : byte to be set
         * @throws IllegalStateException if the builder has been disabled
         * @return this (to be able to chain build instructions)
         */
        public Builder setByte(int byteIndex, byte b) {
            if (!enabled) {
                throw new IllegalStateException();
            }
            Objects.checkIndex(byteIndex, fullSize);
            
            bytes[byteIndex] = Byte.toUnsignedInt(b);
            return this;
        }
        
        /**
         * Constructs a new BitVector with the instructions that were given to the builder
         * @throws IllegalStateException if the builder has been disabled
         * @return the new BitVector
         */
        public BitVector build() {
            if (!enabled) {
                throw new IllegalStateException();
            }
            
            int[] vector = new int[fullSize / BLOCK_SIZE];
            
            for(int i = 0; i < vector.length; i++) {
                vector[i] = combineFourBytes(4 * i);
            }
            
            enabled = false;
            return new BitVector(vector);
        }
        
        private int combineFourBytes(int index) {
            int value = 0;
            
            for (int i = 0; i < 4; i++) {
                value = value | (bytes[index + i] << i * Byte.SIZE);
            }
            
            return value;
        }
    }
    
    /**
     * Creates a new vector of given size, with all of its bits set to 0
     * @param size : size of the new vector
     * @throws IllegalArgumentException if size is negative or not a multiple of 32
     */
    public BitVector(int size) {
        this(size, false);
    }
    
    /**
     * Creates a new vector of given size, with all of its bits set to the specified value
     * @param size : size of the new vector
     * @param initialValue : initial value of the bits
     * @throws IllegalArgumentException if size is negative or not a multiple of 32
     */
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size % BLOCK_SIZE == 0 && size >= 0);
        
        this.numberOfBits = size;
        this.blocks = new int[size / BLOCK_SIZE];
        int v = initialValue ? ~0 : 0;
        Arrays.fill(blocks, v);     
    }
    
    private BitVector(int[] blocks) {
        this.numberOfBits = BLOCK_SIZE * blocks.length;
        this.blocks = blocks;
    }
    
    /**
     * @return the size of the vector in bits
     */
    public int size() {
        return numberOfBits;
    }
    
    /**
     * Gives the value of the bits at specified index
     * @param index : index of the bit to be tested
     * @return the value of the bit
     */
    public boolean testBit(int index) {
        return Bits.test(blocks[index / BLOCK_SIZE], index % BLOCK_SIZE);
    }
    
    /**
     * @return the complement of the vector
     */
    public BitVector not() {
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);
        
        for (int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = ~blocksCopy[i];
        }
        
        return new BitVector(blocksCopy);
    }
    
    /**
     * Bitwise AND between two vectors
     * @param bv : second vector
     * @throws IllegalArgumentException if vectors have a different size
     * @return a new BitVector, the result of the operation
     */
    public BitVector and(BitVector bv) {
        Preconditions.checkArgument(numberOfBits == bv.size());
        
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);

        for(int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = blocksCopy[i] & bv.blocks[i];
        }
        
        return new BitVector(blocksCopy);
    }
    
    /**
     * Bitwise OR between two vectors
     * @param bv : second vector
     * @throws IllegalArgumentException if vectors have a different size
     * @return a new BitVector, the result of the operation
     */
    public BitVector or(BitVector bv) {
        Preconditions.checkArgument(numberOfBits == bv.size());
        
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);

        for(int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = blocksCopy[i] | bv.blocks[i];
        }
        
        return new BitVector(blocksCopy);
    }
    
    /**
     * Extract a given number of bits at specified index, with a zero extension
     * @param start : start of the extraction
     * @param numberOfBits : number of bits to be extracted
     * @return a new BitVector of the extracted bits
     */
    public BitVector extractZeroExtended(int start, int numberOfBits) {
        return new BitVector(extract(start, numberOfBits, Extraction.ZERO));
    }
    
    /**
     * Extract a given number of bits at specified index, with a wrapped extension
     * @param start : start of the extraction
     * @param numberOfBits : number of bits to be extracted
     * @return a new BitVector of the extracted bits
     */
    public BitVector extractWrapped(int start, int numberOfBits) {
        return new BitVector(extract(start, numberOfBits, Extraction.WRAP));
    }
    
    /**
     * Shifts the vector by a given distance
     * @param distance : distance of shift
     * @return a new BitVector of the shifted bits
     */
    public BitVector shift(int distance) {         
        return extractZeroExtended(distance, numberOfBits);
    }
    
    @Override
    public boolean equals(Object that) {
        if (that instanceof BitVector && this.numberOfBits == ((BitVector) that).numberOfBits) {
            return Arrays.equals(this.blocks, ((BitVector) that).blocks);
        }
     
        return false;     
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(blocks);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for(int i : blocks) {
            sb.append(String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0'));
        }
        
        return sb.toString();
    }
    
    private int[] extract(int start, int numberOfBits, Extraction e) {
        Preconditions.checkArgument(numberOfBits >= 0 && numberOfBits % BLOCK_SIZE == 0);
        
        if (Math.floorMod(start, BLOCK_SIZE) == 0) {
            return extractFullBlocks(start, numberOfBits, e);
        } else {
            return extractCombinedBlocks(start, numberOfBits, e);    		
        }
    }
    
    private int[] extractFullBlocks(int startIndex, int numberOfBits, Extraction e) {
        int startBlock = Math.floorDiv(startIndex, BLOCK_SIZE);
        int[] blocks = new int[Math.floorDiv(numberOfBits, BLOCK_SIZE)];
        
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = extractBlock(i - startBlock, e);
        }
        
        return blocks;
    }
    
    private int[] extractCombinedBlocks(int startIndex, int numberOfBits, Extraction e) {
        int[] blocks = new int[Math.floorDiv(numberOfBits, BLOCK_SIZE)];
        
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = extractCombinedBlock(BLOCK_SIZE*i + startIndex, e);
        }
        
        return blocks;
    }
    
    private int extractBlock(int blockIndex, Extraction e) {
        	if (0 <= blockIndex && blockIndex < blocks.length) {
        		return blocks[blockIndex];
        	} else if (e == Extraction.WRAP) {
        		return blocks[Math.floorMod(blockIndex, blocks.length)];
        	} else {
        		return 0;
        	}
    }
    
    private int extractCombinedBlock(int startIndex, Extraction e) {
        	int blockIndex = Math.floorDiv(startIndex, BLOCK_SIZE);
        	int lsbBlock = extractBlock(blockIndex, e);
        	int msbBlock = extractBlock(blockIndex + 1, e);
        	
        	int subIndex = Math.floorMod(startIndex, BLOCK_SIZE);
        	int lsb = Bits.extract(lsbBlock, subIndex, BLOCK_SIZE - subIndex);
        	int msb = Bits.extract(msbBlock, 0, subIndex);
        	
        	return (msb << (BLOCK_SIZE - subIndex)) | lsb;
    }
}
