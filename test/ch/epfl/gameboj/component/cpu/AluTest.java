package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AluTest {

	/* maskZHCN tests */
	
	@Test
	void maskZHCNWorksForAllFalse() {
		assertEquals(0, Alu.maskZHNC(false, false, false, false));
	}

	@Test
	void maskZHCNWorksForAllTrue() {
		assertEquals(0b11110000, Alu.maskZHNC(true, true, true, true));
	}
	
	@Test
	void maskZHCNWorksForGivenValue() {
		assertEquals(0x70, Alu.maskZHNC(false, true, true, true));
	}
	
	/* unpackValue tests */
	
	@Test
	void unpackValueWorksForMaxValues() {
		int[] values = {0x0000, 0x0007, 0x00FF, 0xFF00, 0xFF07, 0xFFFF};
		int[] expected = {0, 0, 0, 0xFF, 0xFF, 0xFF};
		
		for (int i = 0; i < values.length; i++) {
			assertEquals(expected[i], Alu.unpackValue(values[i]));
		}
	}
	
	@Test
	void unpackValueWorksFor8BitValues() {
		int[] values = {0x1200, 0x0707, 0x1111, 0xAC12};
		int[] expected = {0x12, 0x07, 0x11, 0xAC};
		
		for (int i = 0; i < values.length; i++) {
			assertEquals(expected[i], Alu.unpackValue(values[i]));
		}
	}
	
	@Test
	void unpackValueWorksFor16BitValues() {
		int[] values = {0xFFAA07, 0x321FF, 0x3F276, 0xFF1111};
		int[] expected = {0xFFAA, 0x321, 0x3F2, 0xFF11};
		
		for (int i = 0; i < values.length; i++) {
			assertEquals(expected[i], Alu.unpackValue(values[i]));
		}
	}
	
	/* unpackFlags tests */
	
	@Test
	void unpackFlagsWorkdForMaxValues() {
		int[] values = {0x0000, 0x0700, 0xFF00, 0x00FF, 0x07FF, 0xFFFF};
		int[] expected = {0, 0, 0, 0xFF, 0xFF, 0xFF};
		
		for (int i = 0; i < values.length; i++) {
			assertEquals(expected[i], Alu.unpackFlags(values[i]));
		}
	}
	
	@Test
	void unpackFlagsWorkdForArbitraryValues() {
		int[] values = {0x1270, 0x0707, 0x1111, 0xAC12, 0xFFAA07, 0x321FF, 0x3F276, 0xFF1111};
		int[] expected = {0x70, 0x07, 0x11, 0x12, 0x07, 0xFF, 0x76, 0x11};
		
		for (int i = 0; i < values.length; i++) {
			assertEquals(expected[i], Alu.unpackFlags(values[i]));
		}
	}
}
