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
    
    private static enum Extraction {
    	ZERO, WRAP;
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
        return Bits.test(blocks[index / BLOCK_SIZE], index % BLOCK_SIZE);
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
            blocksCopy[i] = blocksCopy[i] & bv.blocks[i];
        }
        
        return new BitVector(blocksCopy);
    }
    
    public BitVector or(BitVector bv) {
        Preconditions.checkArgument(size == bv.size());
        
        int[] blocksCopy = Arrays.copyOf(blocks, blocks.length);

        for(int i = 0; i < blocksCopy.length; i++) {
            blocksCopy[i] = blocksCopy[i] | bv.blocks[i];
        }
        
        return new BitVector(blocksCopy);
    }
    
    public BitVector extractZeroExtended(int start, int size) {
        return new BitVector(extract(start, size, Extraction.ZERO));
    }
    
    public BitVector extractWrapped(int start, int size) {
        return new BitVector(extract(start, size, Extraction.WRAP));
    }
    
    private int[] extract(int start, int size, Extraction e) {
    	Preconditions.checkArgument(size >= 0 && size % BLOCK_SIZE == 0);
    	
    	if (Math.floorMod(start, BLOCK_SIZE) == 0) {
    		return extractFullBlocks(start, size, e);
    	} else {
    		return extractCombinedBlocks(start, size, e);    		
    	}
    }
    
    private int[] extractFullBlocks(int startIndex, int size, Extraction e) {
    	int startBlock = Math.floorDiv(startIndex, BLOCK_SIZE);
    	int[] blocks = new int[Math.floorDiv(size, BLOCK_SIZE)];
    	
    	for (int i = 0; i < blocks.length; i++) {
    		blocks[i] = extractBlock(i - startBlock, e);
    	}
    	
    	return blocks;
    }
    
    private int[] extractCombinedBlocks(int startIndex, int size, Extraction e) {
    	int[] blocks = new int[Math.floorDiv(size, BLOCK_SIZE)];
    	
    	for (int i = 0; i < blocks.length; i++) {
    		blocks[i] = extractCombinedBlock(BLOCK_SIZE*i + startIndex, e);
    	}
    	
    	return blocks;
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
    
    private int extractBlock(int index, Extraction e) {
    	if (0 <= index && index < blocks.length) {
    		return blocks[index];
    	} else if (e == Extraction.WRAP) {
    		return blocks[Math.floorMod(index, blocks.length)];
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
