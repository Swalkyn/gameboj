package ch.epfl.gameboj.bits;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BitVectorTest {
    
    private int DEFAULT_SIZE = 256;
    
    @Test
    void constructorsAreCompatible() {
        BitVector v1 = new BitVector(DEFAULT_SIZE);
        BitVector v2 = new BitVector(DEFAULT_SIZE, false);
        
        assertTrue(v1.equals(v2));
    }
    
    @Test
    void constructorThrowsForNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> new BitVector(-1));
    }
    
    @Test
    void constructorThrowsForInvalidSize() {
        assertThrows(IllegalArgumentException.class, () -> new BitVector(1));
    }
    
    @Test
    void sizeWorks() {
        assertEquals(DEFAULT_SIZE, new BitVector(DEFAULT_SIZE).size());
        assertEquals(32, new BitVector(32).size());
    }
    
    @Test 
    void notWorksOnKnownValues() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, true);
        BitVector v2 = new BitVector(DEFAULT_SIZE, false);
        BitVector v3 = new BitVector.Builder(32).setByte(2, (byte) 0xFF).build();
        
        assertTrue(v1.not().equals(v2));
        assertTrue(v2.not().equals(v1));
        assertEquals("11111111000000001111111111111111", v3.not().toString());
    }
    
    @Test
    void andWorksOnKnownValues() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, true);
        BitVector v2 = new BitVector.Builder(DEFAULT_SIZE).setByte(1, (byte) 0xFF).build();
        
        int[] check = {0xFF00, 0, 0, 0, 0 ,0, 0, 0};
        assertEquals(integerArrayToBinaryString(check), v1.and(v2).toString());
    }
    
    @Test
    void andThrowsForInvalidVector() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, true);
        BitVector v2 = new BitVector(32, true);
        
        assertThrows(IllegalArgumentException.class, () -> v1.and(v2));
    }
    
    @Test
    void orWorksOnKnownValues() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, false);
        BitVector v2 = new BitVector.Builder(DEFAULT_SIZE).setByte(1, (byte) 0xFF).setByte(3, (byte) 0xDD).build();
        
        int[] check = {0xDD00FF00, 0, 0, 0, 0 ,0, 0, 0};
        assertEquals(integerArrayToBinaryString(check), v1.or(v2).toString());
    }
    
    @Test
    void orThrowsForInvalidVector() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, true);
        BitVector v2 = new BitVector(32, true);
        
        assertThrows(IllegalArgumentException.class, () -> v1.or(v2));
    }
    
    private String integerArrayToBinaryString(int[] ints) {
        StringBuilder sb = new StringBuilder();
        
        for(int i : ints) {
            sb.insert(0, String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0'));
        }
        
        return sb.toString();
    }
    
    
}
