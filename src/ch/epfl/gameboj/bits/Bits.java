package ch.epfl.gameboj.bits;

import java.util.Objects;

public final class Bits {
    private Bits() {}
    
    public int mask(int index) {
        return 1 << Objects.checkIndex(index, Integer.SIZE);
    }
    
    public boolean test(int bits, int index) {
        Objects.checkIndex(index, Integer.SIZE);
        
        int bitmask = mask(index);
        return bitmask == (bitmask & bits);
    }
    
    
    
    
}
