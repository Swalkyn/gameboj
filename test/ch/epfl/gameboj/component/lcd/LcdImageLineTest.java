package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;

class LcdImageLineTest {

	private static final int SIZE = 32;
	private static final BitVector MSB = buildMsb();
	private static final BitVector LSB = buildLsb();
	private static final BitVector OP = buildOpactiy();
	private static final BitVector ONE = new BitVector(SIZE, true);
	private static final BitVector ZERO = new BitVector(SIZE, false);
	
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
		LcdImageLine oneLine = new LcdImageLine(ONE, ONE, ONE);
		LcdImageLine main = new LcdImageLine(MSB, LSB, OP);
		LcdImageLine combined = main.join(oneLine, 21);
		
		assertEquals(Integer.toBinaryString(0xFFEF32AA), combined.msb().toString());
		assertEquals(Integer.toBinaryString(0xFFEF71DD), combined.lsb().toString());
		assertEquals(Integer.toBinaryString(0xFFE03F66), combined.opacity().toString());
		
		assertEquals(oneLine, main.join(oneLine, 0));
		assertEquals(main, main.join(oneLine, SIZE));
	}
	
	@Test
	void belowWorks() {
	    LcdImageLine above = new LcdImageLine(repeat(0x93), repeat(0x5D), repeat(0x0F));
	    LcdImageLine below = new LcdImageLine(MSB, LSB, OP);
	    LcdImageLine combined = below.below(above);
	    
	    assertEquals(format(0x630333A3), combined.msb().toString());
	    assertEquals(format(0x6D0D7DDD), combined.lsb().toString());
	    assertEquals(format(0x0F0F3F6F), combined.opacity().toString());
	    
	    assertEquals(new LcdImageLine(ONE, ONE, ONE), below.below(new LcdImageLine(ONE, ONE, ONE)));
	    assertEquals(below, below.below(new LcdImageLine(ZERO, ZERO, ZERO)));
	}
	
	private static BitVector repeat(int pattern) {
	    Preconditions.checkBits8(pattern);
	    
	    BitVector.Builder bvb = new BitVector.Builder(32);
	    
	    for (int i = 0; i < 4; i++) {
	        bvb = bvb.setByte(i, pattern);
	    }
	    
	    return bvb.build();
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
		return bvb.setByte(0, 0x66).setByte(1, 0x3F).setByte(2, 0x00).setByte(3, 0x0F).build();
	}
	
	private String format(int hex) {
	    return String.format("%32s", Integer.toBinaryString(hex)).replace(' ', '0');
	}
	
}
