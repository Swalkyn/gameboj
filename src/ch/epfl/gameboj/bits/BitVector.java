package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {
    
    private static final int BLOCK_SIZE = 32;
    private final int[] blocks;
    private final int size; 

    public static final class Builder {
        private final int[] bytes;
        private final int fullSize;
        private boolean enabled = true;
        
        public Builder(int size) {
            Preconditions.checkArgument(size % BLOCK_SIZE == 0 && size >= 0);
            
            fullSize = size;
            bytes = new int[size / Byte.SIZE];
        }
        
        public Builder setByte(int index, byte b) {
            if (!enabled) {
                throw new IllegalStateException();
            }
            Objects.checkIndex(index, fullSize);
            
            int byteIndex = index / Byte.SIZE;
            bytes[byteIndex] = Byte.toUnsignedInt(b);
            return this;
        }
        
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
    
    public BitVector(int size) {
        this(size, false);
    }
    
    public BitVector(int size, boolean initialValue) {
        Preconditions.checkArgument(size % BLOCK_SIZE == 0 && size >= 0);
        
        this.size = size;
        this.blocks = new int[size / BLOCK_SIZE];
        int v = initialValue ? ~0 : 0;
        Arrays.fill(blocks, v);
        
    }
    
    private BitVector(int[] blocks) {
        this.size = BLOCK_SIZE * blocks.length;
        this.blocks = blocks;
    }
    
    public int size() {
        return size;
    }
    
    public boolean testBit(int index) {
        return Bits.test(extractBlock(index), extractSubIndex(index));
    }
    
    public BitVector not() {
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);
        
        for (int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = ~blocksCopy[i];
        }
        
        return new BitVector(blocksCopy);
    }
    
    public BitVector and(BitVector bv) {
        Preconditions.checkArgument(size == bv.size());
        
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);

        for(int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = blocksCopy[i] & bv.extractBlock(i);
        }
        
        return new BitVector(blocksCopy);
    }
    
    public BitVector or(BitVector bv) {
        Preconditions.checkArgument(size == bv.size());
        
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);

        for(int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = blocksCopy[i] | bv.extractBlock(i);
        }
        
        return new BitVector(blocksCopy);
    }
    
    public BitVector extractZeroExtended(int start, int size) {
        Builder builder = new Builder(size);
        
        
        return null;
    }
    
    public BitVector extractWrapped(int start, int size) {
        return null;
    }
    
    public BitVector shift(int distance) {
        return null;
    }
    
    @Override
    public boolean equals(Object that) {
        return false;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public String toString() {
        return null;
    }
    
    private int extractBlock(int index) {
        Objects.checkIndex(index, size);
        return blocks[Math.floorDiv(index, BLOCK_SIZE)];
    }
    
    private int extractSubIndex(int index) {
        Objects.checkIndex(index, size);
        return Math.floorMod(index, BLOCK_SIZE);
    }
}
