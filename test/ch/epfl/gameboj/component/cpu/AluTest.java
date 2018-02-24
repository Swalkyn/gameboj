package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AluTest {

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
	
}
