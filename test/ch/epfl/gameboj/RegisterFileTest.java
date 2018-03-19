package ch.epfl.gameboj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.Bit;

public class RegisterFileTest {
    
    private enum R implements Register {
        A, B, C, D
    }
    
    private enum B implements Bit {
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN
    }
    
    private static RegisterFile<R> newRF() {
        return new RegisterFile<>(R.values());
    }
    
    @Test
    void setFailsForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> newRF().set(R.A, 0xFF1));
        assertThrows(IllegalArgumentException.class, () -> newRF().set(R.A, 0x100));
        assertThrows(IllegalArgumentException.class, () -> newRF().set(R.A, -1));
    }
    
    @Test
    void setThenGetForEdgeCases() {
        RegisterFile<R> rf = newRF();
        
        // Test 0x00
        for (R r : R.values()) {
            rf.set(r, 0x00);
            assertEquals(0x00, rf.get(r));
        }
        
        // Test 0x01
        for (R r : R.values()) {
            rf.set(r, 0x01);
            assertEquals(0x01, rf.get(r));
        }
        
        // Test 0xFF
        for (R r : R.values()) {
            rf.set(r, 0xFF);
            assertEquals(0xFF, rf.get(r));
        }
    }
    
    @Test
    void setThenGetForKnownValues() {
        RegisterFile<R> rf = newRF();
        
        for (R r : R.values()) {
            rf.set(r, 0xA1);
            assertEquals(0xA1, rf.get(r));
        }
    }
    
    @Test
    void testBitWorksForKnownValues() {
        RegisterFile<R> rf = newRF();
        
        for (R r : R.values()) {
            rf.set(r, 0b1011_0101);
            assertTrue(rf.testBit(r, B.ZERO));
            assertFalse(rf.testBit(r, B.ONE));
            assertFalse(rf.testBit(r, B.SIX));
            assertTrue(rf.testBit(r, B.SEVEN));
        }
    }
    
    @Test
    void setBitSetCorrectBitToTrue() {
        RegisterFile<R> rf = newRF();
        
        for (R r : R.values()) {
            for (B b : B.values()) {
                rf.set(r, 0x00);
                rf.setBit(r, b, true);
                
                assertTrue(rf.testBit(r, b));
            }
        }
    }
    
    @Test
    void setBitSetCorrectBitToFalse() {
        RegisterFile<R> rf = newRF();
        
        for (R r : R.values()) {
            for (B b : B.values()) {
                rf.set(r, 0xFF);
                rf.setBit(r, b, false);
                
                assertFalse(rf.testBit(r, b));
            }
        }
    }
    
    @Test
    void setBitWorksForKnownValues() {
        RegisterFile<R> rf = newRF();
        
        for (R r : R.values()) {
            rf.set(r, 0b1000_1101);
            rf.setBit(r, B.FOUR, true);
            rf.setBit(r, B.TWO, false);
            assertEquals(0b1001_1001, rf.get(r));
        }
    }
    
    
    
    
    
    
    
    
    
    
    

}
