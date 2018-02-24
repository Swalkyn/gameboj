package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AluTest {

	/* maskZHCN tests */
	
	@Test
	void maskZHCNWorksForAllFalse() {
		assertEquals(0, Alu.maskZNHC(false, false, false, false));
	}

	@Test
	void maskZHCNWorksForAllTrue() {
		assertEquals(0b11110000, Alu.maskZNHC(true, true, true, true));
	}
	
	@Test
	void maskZHCNWorksForKnownValue() {
		assertEquals(0x70, Alu.maskZNHC(false, true, true, true));
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
	void unpackFlagsWorkdForKnownValues() {
		int[] values = {0x1270, 0x0707, 0x1111, 0xAC12, 0xFFAA07, 0x321FF, 0x3F276, 0xFF1111};
		int[] expected = {0x70, 0x07, 0x11, 0x12, 0x07, 0xFF, 0x76, 0x11};
		
		for (int i = 0; i < values.length; i++) {
			assertEquals(expected[i], Alu.unpackFlags(values[i]));
		}
	}
	
	/* add tests */
	
	@Test
	void addFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.add(0x100, 0, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(0, 0x100, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(0, 0x100));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(0, 0x100));
	}
	
	@Test
    void addReturnsCorrectValuesForMaxValues() {
        assertEquals(0x00, Alu.unpackValue(Alu.add(0, 0)));
        assertEquals(0xFF, Alu.unpackValue(Alu.add(0xFF, 0)));
        assertEquals(0xFE, Alu.unpackValue(Alu.add(0xFF, 0xFF)));
        assertEquals(0x01, Alu.unpackValue(Alu.add(0, 0, true)));
        assertEquals(0x00, Alu.unpackValue(Alu.add(0xFF, 0, true)));
        assertEquals(0xFF, Alu.unpackValue(Alu.add(0xFF, 0xFF, true)));
    }
	
	@Test
    void addReturnsCorrectFlagsForMaxValues() {
        assertEquals(0x80, Alu.unpackFlags(Alu.add(0, 0)));
        assertEquals(0x00, Alu.unpackFlags(Alu.add(0xFF, 0)));
        assertEquals(0x30, Alu.unpackFlags(Alu.add(0xFF, 0xFF)));
        assertEquals(0x00, Alu.unpackFlags(Alu.add(0, 0, true)));
        assertEquals(0xB0, Alu.unpackFlags(Alu.add(0xFF, 0, true)));
        assertEquals(0x30, Alu.unpackFlags(Alu.add(0xFF, 0xFF, true)));
    }
	
	@Test
	void addReturnsCorrectValuesForKnownValues() {
	    assertEquals(0x25, Alu.unpackValue(Alu.add(0x10, 0x15)));
	    assertEquals(0x10, Alu.unpackValue(Alu.add(0x08, 0x08)));
	    assertEquals(0x00, Alu.unpackValue(Alu.add(0x80, 0x7F, true)));
	}
	
	@Test
    void addReturnsCorrectFlagsForKnownValues() {
        assertEquals(0x00, Alu.unpackFlags(Alu.add(0x10, 0x15)));
        assertEquals(0x20, Alu.unpackFlags(Alu.add(0x08, 0x08)));
        assertEquals(0xB0, Alu.unpackFlags(Alu.add(0x80, 0x7F, true)));
    }
	
	/* add16 tests */
	
	@Test 
	void add16FailsForInvalidValues() {
	    assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0x10000, 0));
	    assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0, 0x10000));
	    assertThrows(IllegalArgumentException.class, () -> Alu.add16H(0x10000, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16H(0, 0x10000));
	}
}



















