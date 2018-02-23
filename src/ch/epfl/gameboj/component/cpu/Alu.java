package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assumptions.assumingThat;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {
    
    private enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNESED_2, UNUSED_3, C, H, N, Z
    }
    
    public enum RotDir {
        LEFT(1), RIGHT(-1);
        
        private final int dirCode;
        
        private RotDir(int dirCode) {
            this.dirCode = dirCode;
        }
    }
    
    private Alu() {}
    
    public static int maskZHNC(boolean z, boolean n, boolean h, boolean c) {
        return Bits.set(0, Flag.Z.index(), z) | Bits.set(0, Flag.N.index(), n)
               | Bits.set(0, Flag.H.index(), h) | Bits.set(0, Flag.C.index(), c);
    }
    
    public static int unpackValue(int valueFlags) {
        return Bits.extract(valueFlags, 8, 16);
    }
    
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags);
    }
    
    private static int packValueZNHC(int value, boolean z, boolean n, boolean h, boolean c) {
        return (value << 8) | maskZHNC(z, n, h, c);
    }
    
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l + r + (c0 ? 1 : 0);
        boolean z = result == 0 ? true : false;
        boolean n = false;
        boolean h = Bits.clip(4, l) + Bits.clip(4, r) > 0xF;
        boolean c = result > 0xFF;
        
        return packValueZNHC(result, z, n, h, c);
    }
    
    public static int add(int l, int r) {
        return add(l, r, false);
    }
    
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int resultLSB = add(Bits.clip(8, l), Bits.clip(8, r));
        int resultMSB = add(Bits.extract(l, 8, 8), Bits.extract(r, 8, 8), Bits.test(resultLSB, Flag.C));
        return Bits.clip(16, resultLSB) | Bits.extract(resultMSB, 16, 8) << 8;        
    }
    
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int resultLSB = add(Bits.clip(8, l), Bits.clip(8, r));
        int resultMSB = add(Bits.extract(l, 8, 8), Bits.extract(r, 8, 8), Bits.test(resultLSB, Flag.C));
        return Bits.extract(resultMSB, 8, 8) << 16 | Bits.extract(resultLSB, 8, 8) << 8 | Bits.clip(8, resultMSB);
    }
    
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int result = l - r - (b0 ? 1 : 0);
        boolean z = result == 0 ? true : false;
        boolean n = true;
        boolean h = Bits.clip(4, l) < Bits.clip(4, r);
        boolean c = l < r;
        
        return packValueZNHC(result, z, n, h, c);
    }
    
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }
    
    public static int bcdAjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        
        int fixL = (h || (!n && Bits.clip(4, v) > 9)) ? 1 : 0;
        int fixH = (c || (!n && v > 0x99)) ? 1 : 0;
        int fix = 0x60 * fixH + 0x06 * fixL;
        
        int va;
        if (n) {
            va = v - fix;
        } else {
            va = v + fix;
        }
        
        return packValueZNHC(va, va == 0, n, false, fixH == 1);
    }
    
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l & r;
        return packValueZNHC(result, result == 0, false, true, false);
    }
    
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l | r;
        return packValueZNHC(result, result == 0, false, false, false);
    }
    
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l ^ r;
        return packValueZNHC(result, result == 0, false, false, false);
    }
    
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        
        int result = v << 1;
        boolean z = result == 0;
        boolean c = Bits.test(v, 7);
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, c);
    }
    
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        
        boolean c = Bits.test(v, 0);
        int result = v >> 1;
        boolean z = result == 0;
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, c);
    }
    
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        
        boolean c = Bits.test(v, 0);
        int result = v >>> 1;
        boolean z = result == 0;
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, c);
    }
    
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        
        int result = Bits.rotate(8, v, d.dirCode);
        boolean z = result == 0;
        int index = (d == RotDir.LEFT) ? 7 : 0;
        boolean c = Bits.test(result, index);
        
        return packValueZNHC(result, z, false, false, c);
    }
    
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        
        int cMask = c ? Bits.mask(9) : 0;
        int result = Bits.rotate(9, cMask | v, d.dirCode);
        boolean z = result == 0;
        boolean finalC = Bits.test(result, 8);
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, finalC);
    }
    
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        
        int result = Bits.clip(4, v) << 4 | Bits.extract(v, 4, 4);
        return packValueZNHC(result, result == 0, false, false, false);
    }
    
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        
        if (bitIndex < 0 || bitIndex > 7) {
            throw new IndexOutOfBoundsException("Index must be between 0 and 7 (included)");
        }
        
        return packValueZNHC(0, Bits.test(v, bitIndex), false, true, false);
    }
}

























