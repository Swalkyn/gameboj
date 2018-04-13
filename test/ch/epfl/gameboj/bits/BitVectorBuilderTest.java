package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BitVectorBuilderTest {

	static final int DEFAULT_SIZE = 256;
	
	@Test
	void constructorFailsForInvalidSize() {
		int[] sizes = {-1, -32, 4, 31, 33, 145, 1025};
		
		for (int size: sizes) {
			assertThrows(IllegalArgumentException.class,
						 () -> new BitVector.Builder(size));
		}
	}
	
	@Test
	void setByteFailsForInvalidIndex() {
		int[] indexes = {-1, -4, 64, 65, 256};
		
		BitVector.Builder bvb = new BitVector.Builder(DEFAULT_SIZE);
		
		for (int index: indexes) {
			assertThrows(IndexOutOfBoundsException.class,
					() -> bvb.setByte(index, (byte)0));
		}
	}
	
	@Test
	void buildFailsWhenCalledTwice() {
		BitVector.Builder bvb = new BitVector.Builder(DEFAULT_SIZE);
		bvb.build();
		
		assertThrows(IllegalStateException.class, () -> bvb.build());
	}
	
	@Test
	void bitVectorAreCorrectSizes() {
		int[] sizes = {0, 32, 64, 256, 1024}; 
		
		for (int size: sizes) {
			BitVector.Builder bvb = new BitVector.Builder(size);
			BitVector bv = bvb.build();
			
			assertEquals(size, bv.size());
		}
	}
	
	@Test
	void bitVectorsAreAllZeroIntially() {
		BitVector.Builder builder = new BitVector.Builder(DEFAULT_SIZE);
		BitVector bv = builder.build();
		
		for (int i = 0; i < DEFAULT_SIZE; i++) {
			assertFalse(bv.testBit(i));
		}
	}
	
	@Test
	void bitVectorCreatesCorrectVectors() {
		String zero8 = "00000000";
		String[] vectors = {
			zero8 + zero8 + zero8 + "11110000",
			zero8 + "00000001" + zero8 + zero8,
		};
		
		BitVector.Builder bvb1 = new BitVector.Builder(32);
		assertEquals(vectors[0], bvb1.setByte(0, (byte)0xF0).build().toString());
		
		BitVector.Builder bvb2 = new BitVector.Builder(32);
		assertEquals(vectors[1], bvb2.setByte(2, (byte)0x01).build().toString());
	}

}
