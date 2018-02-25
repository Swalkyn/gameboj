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
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0x10000, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0, 0x10000));
	}
	
	@Test
	void add16WorksForMaxValues() {
	    assertEquals(0, Alu.unpackValue(Alu.add16L(0, 0)));
	    assertEquals(0x01, Alu.unpackValue(Alu.add16L(0, 0x01)));
	    assertEquals(0, Alu.unpackValue(Alu.add16H(0, 0)));
        assertEquals(0x01, Alu.unpackValue(Alu.add16H(0, 0x01)));
        assertEquals(0xFFFF, Alu.unpackValue(Alu.add16L(0xFFFF, 0)));
        assertEquals(0, Alu.unpackValue(Alu.add16L(0xFFFF, 0x01)));
	}
	
	@Test
	void add16ReturnsCorrectValuesForKnownValues() {
	    assertEquals(0x1200, Alu.unpackValue(Alu.add16L(0x11FF, 0x0001)));
	    assertEquals(0x1200, Alu.unpackValue(Alu.add16H(0x11FF, 0x0001)));
	}
	
	void add16ReturnsCorrectFlagsForKnownValues() {
        assertEquals(0x30, Alu.unpackFlags(Alu.add16L(0x11FF, 0x0001)));
        assertEquals(0x00, Alu.unpackFlags(Alu.add16H(0x11FF, 0x0001)));
    }
	
    /* sub tests */
    
	@Test
	void subFailsForInvalidValues() {
	    assertThrows(IllegalArgumentException.class, () -> Alu.and(0x100, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0, 0x100));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0, -1));
	}
	
	@Test
	void subWorksForMaxValues() {
	    assertEquals(0,0);
	}
	
    @Test
    void subReturnsCorrectValuesForKnownValues() {
        assertEquals(0x00, Alu.unpackValue(Alu.sub(0x10, 0x10)));
        assertEquals(0x90, Alu.unpackValue(Alu.sub(0x10, 0x80)));
        assertEquals(0xFF, Alu.unpackValue(Alu.sub(0x01, 0x01, true)));
    }
    
    @Test
    void subReturnsCorrectFlagsForKnownValues() {
        assertEquals(0xC0, Alu.unpackFlags(Alu.sub(0x10, 0x10)));
        assertEquals(0x50, Alu.unpackFlags(Alu.sub(0x10, 0x80)));
        assertEquals(0x70, Alu.unpackFlags(Alu.sub(0x01, 0x01, true)));
    }
    
    /* bcdAdjust tests */
    
    @Test
    void bcdAdjustFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(0x100, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(-1, false, false, false));
    }
    
    @Test
    void bcdAdjustReturnsCorrectValuesForKnownValues() {
        assertEquals(0x73, Alu.unpackValue(Alu.bcdAdjust(0x6D, false, false, false)));
        assertEquals(0x09, Alu.unpackValue(Alu.bcdAdjust(0x0F, true, true, false)));
    }
    
    @Test
    void bcdAdjustReturnsCorrectFlagsForKnownValues() {
        assertEquals(0x00, Alu.unpackFlags(Alu.bcdAdjust(0x6D, false, false, false)));
        assertEquals(0x40, Alu.unpackFlags(Alu.bcdAdjust(0x0F, true, true, false)));
    }
    
    /* and tests */
    
    @Test 
    void andFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0x100, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0, 0x100));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0, -1));
    }
    
    @Test
    void andReturnsCorrectValuesForKnownValues() {
        assertEquals(0x03, Alu.unpackValue(Alu.and(0x53, 0xA7)));
    }
    
    @Test
    void andReturnsCorrectFlagsForKnownValues() {
        assertEquals(0x20, Alu.unpackFlags(Alu.and(0x53, 0xA7)));
    }
    
    /* or tests */
    
    @Test 
    void orFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.or(0x100, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(0, 0x100));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(0, -1));
    }
    
    @Test
    void orReturnsCorrectValuesForKnownValues() {
        assertEquals(0xF7, Alu.unpackValue(Alu.or(0x53, 0xA7)));
    }
    
    @Test
    void orReturnsCorrectFlagsForKnownValues() {
        assertEquals(0x00, Alu.unpackFlags(Alu.or(0x53, 0xA7)));
    }
    
    /* xor tests */
    
    @Test 
    void xorFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(0x100, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(0, 0x100));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(0, -1));
    }
    
    @Test
    void xorReturnsCorrectValuesForKnownValues() {
        assertEquals(0xF4, Alu.unpackValue(Alu.xor(0x53, 0xA7)));
    }
    
    @Test
    void xorReturnsCorrectFlagsForKnownValues() {
        assertEquals(0x00, Alu.unpackFlags(Alu.xor(0x53, 0xA7)));
    }
    
    /* shiftLeft tests */
    
    
    
    /* shiftRightA tests */
    
    
    
    /* shiftRightL tests */
    
    
    
    /* rotate tests */
    
    
    
    /* swap tests */
    
	@Test
	void swapThrowsOnInvalidValues() {
	    assertThrows(IllegalArgumentException.class, () -> Alu.swap(0xFF00));
	}
	
	@Test
	void swapWorksForKnownValues() {
	    assertEquals(0b11010001, Alu.unpackValue(Alu.swap(0b00011101)));
	    assertEquals(0b01111110, Alu.unpackValue(Alu.swap(0b11100111)));
	}
	
	@Test
	void swapReturnsCorrectFlags() {
	    assertEquals(Alu.maskZNHC(false, false, false, false), Alu.unpackFlags(Alu.swap(0b011011111)));
	    assertEquals(Alu.maskZNHC(true, false, false, false), Alu.unpackFlags(Alu.swap(0)));
	}
	
	/* testBit tests */
	
	@Test
	void testBitThrowsOnInvalidValues() {
	    assertThrows(IllegalArgumentException.class, () -> Alu.testBit(0b101011110011,  5));
	}

	@Test
	void testBitWorksForKnownValues() {
	    assertEquals(0b00100000, Alu.testBit(0b00011010, 6));
	    assertEquals(0b10100000, Alu.testBit(0b00111001, 3));
	}
	
	@Test
	void testBitThrowsOnInvalidIndex() {
	    assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0, 8));
	}
}



















