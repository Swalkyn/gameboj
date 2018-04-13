package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.*;
import static ch.epfl.gameboj.bits.BitVectorTest.integerArrayToBinaryString;

import org.junit.jupiter.api.Test;

class BitVectorBuilderTest {

	static final int DEFAULT_SIZE = 128;
	
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
		int[] sizes = {32, 64, 256, 1024}; 
		
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
		int[][] vectors = {
			{0, 0, 0, 0x0000_00FF},
			{0, 0, 0, 0x0001_0000},
			{0, 0xFF00_FFED, 0, 0},
			{0, 0, 0x1010_1010, 0},
			{0x12345678, 0, 0, 0}
		};
		
		BitVector.Builder bvb1 = new BitVector.Builder(DEFAULT_SIZE);
		assertEquals(integerArrayToBinaryString(vectors[0]), bvb1.setByte(0, (byte)0xFF).build().toString());
		
		BitVector.Builder bvb2 = new BitVector.Builder(DEFAULT_SIZE);
		assertEquals(integerArrayToBinaryString(vectors[1]), bvb2.setByte(2, (byte)0x01).build().toString());
		
		BitVector bv3 = new BitVector.Builder(DEFAULT_SIZE).setByte(8, (byte)0xED).setByte(9, (byte)0xFF)
				.setByte(11, (byte)0xFF).build();
		assertEquals(integerArrayToBinaryString(vectors[2]), bv3.toString());
		
		BitVector bv4 = new BitVector.Builder(DEFAULT_SIZE).setByte(4, (byte)0x10).setByte(5, (byte)0x10)
				.setByte(6, (byte)0x10).setByte(7, (byte)0x10).build();
		assertEquals(integerArrayToBinaryString(vectors[3]), bv4.toString());

		BitVector bv5 = new BitVector.Builder(DEFAULT_SIZE).setByte(12, (byte)0x78).setByte(13, (byte)0x56)
				.setByte(14, (byte)0x34).setByte(15, (byte)0x12).build();
		assertEquals(integerArrayToBinaryString(vectors[4]), bv5.toString());
	}

}
