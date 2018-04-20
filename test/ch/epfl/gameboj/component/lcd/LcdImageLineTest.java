package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

class LcdImageLineTest {

	private static final int SIZE = 32;
	private static final BitVector MSB = buildMsb();
	private static final BitVector LSB = buildLsb();
	private static final BitVector OP = buildOpactiy();
	
	@Test
	void constructorFailsForInvalidArguments() {
		BitVector zero = new BitVector(SIZE);
		
		assertThrows(NullPointerException.class, () -> 
				new LcdImageLine(null, zero, zero));
		assertThrows(NullPointerException.class, () -> 
				new LcdImageLine(zero, null, zero));
		assertThrows(NullPointerException.class, () -> 
				new LcdImageLine(zero, zero, null));
		
		BitVector size1 = new BitVector(SIZE*2);
		BitVector size2 = new BitVector(SIZE);
		
		assertThrows(IllegalArgumentException.class, () -> 
				new LcdImageLine(size1, size2, zero));
	}
	
	@Test
	void constructorInitalizesCorrectly() {
		LcdImageLine l = new LcdImageLine(MSB, LSB, OP);
		assertEquals(MSB.size(), l.size());
		assertEquals(MSB, l.msb());
		assertEquals(LSB, l.lsb());
		assertEquals(OP, l.opacity());
	}
	
	@Test
	void shiftWorks() {
		LcdImageLine l = new LcdImageLine(MSB, LSB, OP);

		for(int i = 0; i < SIZE; i++) {
			assertEquals(MSB.shift(i), l.shift(i).msb());
			assertEquals(LSB.shift(i), l.shift(i).lsb());
			assertEquals(OP.shift(i), l.shift(i).opacity());
		}
	}
	
	@Test
	void extractWrappedWorks() {
		LcdImageLine l = new LcdImageLine(MSB, LSB, OP);
		
		for (int i = -SIZE; i < 2*SIZE; i++) {
			assertEquals(MSB.extractWrapped(i, SIZE), l.extractWrapped(i, SIZE).msb());
			assertEquals(LSB.extractWrapped(i, SIZE), l.extractWrapped(i, SIZE).lsb());
			assertEquals(OP.extractWrapped(i, SIZE), l.extractWrapped(i, SIZE).opacity());
		}
	}
	
	@Test
	void joinWorksForComplexLines() {
		BitVector one = new BitVector(SIZE, true);
		LcdImageLine oneLine = new LcdImageLine(one, one, one);
		LcdImageLine main = new LcdImageLine(MSB, LSB, OP);
		LcdImageLine combined = main.join(oneLine, 21);
		
		assertEquals(Integer.toBinaryString(0xFFEF32AA), combined.msb().toString());
	}
	

	private static BitVector buildMsb() {
		BitVector.Builder bvb = new BitVector.Builder(32);
		return bvb.setByte(0, 0xAA).setByte(1, 0x32).setByte(2, 0x0F).setByte(3, 0x69).build();
	}
	
	private static BitVector buildLsb() {
		BitVector.Builder bvb = new BitVector.Builder(32);
		return bvb.setByte(0, 0xDD).setByte(1, 0x71).setByte(2, 0x0F).setByte(3, 0x68).build();
	}
	
	private static BitVector buildOpactiy() {
		BitVector.Builder bvb = new BitVector.Builder(32);
		return bvb.setByte(0, 0x66).setByte(1, 0xFF).setByte(2, 0x00).setByte(3, 0x0F).build();
	}
	
	
}
