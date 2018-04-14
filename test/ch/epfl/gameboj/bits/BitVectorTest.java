package ch.epfl.gameboj.bits;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
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
        BitVector v2 = new BitVector.Builder(DEFAULT_SIZE).setByte(1, (byte) 0xFF).setByte(3, (byte) 0xDD).setByte(11, (byte) 0xAA).build();
        
        int[] check = {0, 0, 0, 0 ,0, 0xAA000000, 0, 0xDD00FF00};
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
        BitVector v2 = new BitVector.Builder(DEFAULT_SIZE).setByte(1, (byte) 0xFF).setByte(3, (byte) 0xDD).setByte(11, (byte) 0xAA).build();
        
        int[] check = {0, 0, 0, 0 ,0, 0xAA000000, 0, 0xDD00FF00};
        assertEquals(integerArrayToBinaryString(check), v1.or(v2).toString());
    }
    
    @Test
    void orThrowsForInvalidVector() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, true);
        BitVector v2 = new BitVector(32, true);
        
        assertThrows(IllegalArgumentException.class, () -> v1.or(v2));
    }
    
    @Test
    void equalsReturnsTrueForKnownValues() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, false);
        BitVector v2 = new BitVector(DEFAULT_SIZE, false);
        
        BitVector v3 = new BitVector.Builder(32).setByte(2, (byte) 0xAA).build();
        BitVector v4 = new BitVector.Builder(32).setByte(2, (byte) 0xAA).build();

        assertTrue(v1.equals(v2));
        assertTrue(v3.equals(v4));
    }
    
    @Test
    void equalsReturnsFalseForKnownValues() {
        BitVector v1 = new BitVector(DEFAULT_SIZE, false);
        BitVector v2 = new BitVector(64, false);
        
        BitVector v3 = new BitVector.Builder(64).setByte(5, (byte) 0xA1).build();
        BitVector v4 = new BitVector.Builder(64).setByte(5, (byte) 0xAA).build();
        
        assertFalse(v1.equals(v2));
        assertFalse(v3.equals(v4));
    }
    
    @Test
    void hashCodeAndEqualsAreCompatible() {
        BitVector v1 = new BitVector.Builder(DEFAULT_SIZE).setByte(3, (byte) 0xDD).setByte(18, (byte) 0xFF).setByte(23, (byte) 0xAA).build();
        BitVector v2 = new BitVector.Builder(DEFAULT_SIZE).setByte(3, (byte) 0xDD).setByte(18, (byte) 0xFF).setByte(23, (byte) 0xAA).build();
        BitVector v3 = new BitVector.Builder(DEFAULT_SIZE).setByte(3, (byte) 0xDA).setByte(18, (byte) 0xFF).setByte(23, (byte) 0xAA).build();

        assertTrue(v1.hashCode() == v2.hashCode());
        assertFalse(v1.hashCode() == v3.hashCode());
    }
    
    @Test
    void toStringWorksForKnownValues() {
        BitVector v1 = new BitVector.Builder(DEFAULT_SIZE).setByte(3, (byte) 0xDD).setByte(18, (byte) 0xFF).setByte(23, (byte) 0xAA).build();
        BitVector v2 = new BitVector.Builder(64).setByte(6, (byte) 0xDA).build();
        
        int[] check1 = {0, 0, 0xAA000000, 0x00FF0000, 0, 0, 0, 0xDD000000};
        int[] check2 = {0x00DA0000, 0};
        
        assertTrue(integerArrayToBinaryString(check1).equals(v1.toString()));
        assertTrue(integerArrayToBinaryString(check2).equals(v2.toString()));
    }
    
    @Test
    void shiftWorksForKnownValues() {
        int[] int1 = {0xA0B1C2D3, 0x1F2E3D4C};
        
        BitVector v1 = fill(int1);
        
        assertEquals("0000010100000101100011100001011010011000111110010111000111101010", v1.shift(5).toString());
        assertEquals("0001011000111000010110100110001111100101110001111010100110000000", v1.shift(-5).toString());
    }
    
    public static String integerArrayToBinaryString(int[] ints) {
        StringBuilder sb = new StringBuilder();
        
        for(int i : ints) {
            sb.append(String.format("%32s", Integer.toBinaryString(i)).replace(' ', '0'));
        }
        
        return sb.toString();
    }
    
    private BitVector fill(int[] blocks) {
        BitVector.Builder bvb = new BitVector.Builder(32*blocks.length);

        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < 4; j++) {
                byte b = (byte)((blocks[i] >> 8*j) & 0xFF);
                bvb.setByte(4*(blocks.length - i - 1) + j, b);
            }
        }

        return bvb.build();
    }
}
