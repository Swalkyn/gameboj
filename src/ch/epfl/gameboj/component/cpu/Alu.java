package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Represents the ALU of the gameboy
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Alu {
    
    /**
     * Represents the bits for the flags of the ALU
     * For example, flag C is at index 4 in a packed output int
     */
    public static enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z
    }
    
    /**
     * Represents the direction of rotation
     * dirCode gives access to integer value (1 or -1)
     */
    public static enum RotDir {
        LEFT(1), RIGHT(-1);
        
        public final int dirCode;
        
        private RotDir(int dirCode) {
            this.dirCode = dirCode;
        }
    }
    
    private Alu() {}
    
    /**
     * Creates a new 8 bit mask, the 4 lsb are 0, the rest depend on value of flag (1 = true, 0 = false)
     * Pattern : ZHNC0000
     * @param z
     * @param n
     * @param h
     * @param c
     * @return the bit mask
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return Bits.set(0, Flag.Z.index(), z) | Bits.set(0, Flag.N.index(), n)
               | Bits.set(0, Flag.H.index(), h) | Bits.set(0, Flag.C.index(), c);
    }
    
    /**
     * Returns the value part from packed integer
     * @param valueFlags : packed int
     * @return only the value, 16 bits
     */
    public static int unpackValue(int valueFlags) {
        return Bits.extract(valueFlags, 8, 16);
    }
    
    /**
     * Returns the flag part from packed integer (ZNHC0000)
     * @param valueFlags : packed int
     * @return only the flag section, 8 bits
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags);
    }
    
    /**
     * Adds two 8-bit integers, with initial carry
     * Flag pattern : Z0HC
     * @param l : 8-bits
     * @param r : 8-bits
     * @param c0 : initial carry (true = 1, false = 0)
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags 
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l + r + (c0 ? 1 : 0);
        boolean n = false;
        boolean h = Bits.clip(4, l) + Bits.clip(4, r) + (c0 ? 1 : 0) > 0xF;
        boolean c = result > 0xFF;
        
        result = Bits.clip(8, result);
        boolean z = result == 0;
        
        return packValueZNHC(result, z, n, h, c);
    }
    
    /**
     * Adds two 8-bit integers
     * Flag pattern : Z0HC
     * @param l : 8-bits
     * @param r : 8-bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }
    
    /**
     * Adds two 16-bit integers, with the flags representing the addition of the 8 lsb 
     * Flag pattern : 00HC
     * @param l : 16-bits
     * @param r : 16-bits
     * @throws IllegalArgumentException if not 16 bits
     * @return the packed value and flags
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int resultLSB = add(Bits.clip(8, l), Bits.clip(8, r));
        int resultMSB = add(Bits.extract(l, 8, 8), Bits.extract(r, 8, 8), Bits.test(resultLSB, Flag.C));
        
        int result = Bits.make16(unpackValue(resultMSB), unpackValue(resultLSB));
        boolean h = Bits.test(resultLSB, Flag.H.index());
        boolean c = Bits.test(resultLSB, Flag.C.index());
        
        return packValueZNHC(result, false, false, h, c);
    }
    
    /**
     * Adds two 16-bit integers, with the flags representing the addition of the 8 msb 
     * Flag pattern : 00HC
     * @param l : 16-bits
     * @param r : 16-bits
     * @throws IllegalArgumentException if not 16 bits
     * @return the packed value and flags
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int resultLSB = add(Bits.clip(8, l), Bits.clip(8, r));
        int resultMSB = add(Bits.extract(l, 8, 8), Bits.extract(r, 8, 8), Bits.test(resultLSB, Flag.C));
        
        int result = Bits.make16(unpackValue(resultMSB), unpackValue(resultLSB));
        boolean h = Bits.test(resultMSB, Flag.H.index());
        boolean c = Bits.test(resultMSB, Flag.C.index());
        
        return packValueZNHC(result, false, false, h, c);
    }
    
    /**
     * Subtracts two 8-bit numbers, with initial borrow
     * Flag pattern : Z1HC
     * @param l : 8 bits
     * @param r : 8 bits
     * @param b0 : initial borrow (true = 1, false = 0)
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        
        int result = l - r - (b0 ? 1 : 0);
        boolean h = Bits.clip(4, l) < Bits.clip(4, r) + (b0 ? 1 : 0);
        boolean n = true;
        boolean c = l < r + (b0 ? 1 : 0);
        
        result = Bits.clip(8, result);
        boolean z = result == 0;
        
        return packValueZNHC(result, z, n, h, c);
    }
    
    /**
     * Subtracts two 8-bit numbers
     * Flag pattern : Z1HC
     * @param l : 8 bits
     * @param r : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }
        
    /**
     * Adjusts an 8 bit value into binary coded decimal
     * Flag Pattern : ZN0C
     * @param v : 8-bits
     * @param n
     * @param h
     * @param c
     * @throws IllegalArgumentException if value not 8 bits
     * @return the packed bcd value and flags
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        
        int fixL = (h || (!n && Bits.clip(4, v) > 9)) ? 1 : 0;
        int fixH = (c || (!n && v > 0x99)) ? 1 : 0;
        int fix = Bits.clip(8, 0x60 * fixH + 0x06 * fixL);
        
        int va;
        if (n) {
            va = v - fix;
        } else {
            va = v + fix;
        }
        
        return packValueZNHC(Bits.clip(8, va), Bits.clip(8, va) == 0, n, false, fixH == 1);
    }
    
    /**
     * Bitwise and on two 8 bit ints
     * Flag pattern : Z010
     * @param l : 8 bits
     * @param r : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l & r;
        return packValueZNHC(result, result == 0, false, true, false);
    }
    
    /**
     * Bitwise or on two 8 bit ints
     * Flag pattern : Z010
     * @param l : 8 bits
     * @param r : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l | r;
        return packValueZNHC(result, result == 0, false, false, false);
    }
    
    /**
     * Bitwise xor on two 8 bit ints
     * Flag pattern : Z000
     * @param l : 8 bits
     * @param r : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and flags
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        
        int result = l ^ r;
        return packValueZNHC(result, result == 0, false, false, false);
    }
    
    /**
     * Shifts value to left
     * Flag pattern : Z00C
     * C = value of bit which was shifted out

     * @param v : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and value
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        
        boolean c = Bits.test(v, 7);
        int result = Bits.clip(8, v << 1);
        boolean z = result == 0;
        
        return packValueZNHC(result, z, false, false, c);
    }
    
    /**
     * Shifts value to right, with arithmetic shift
     * Flag pattern : Z00C
     * C = value of bit which was shifted out

     * @param v : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and value
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        
        boolean c = Bits.test(v, 0);
        int result = Bits.clip(8, Bits.signExtend8(v) >> 1);
        boolean z = result == 0;
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, c);
    }
    
    /**
     * Shifts value to right, with logic shift
     * Flag pattern : Z00C
     * C = value of bit which was shifted out
     * @param v : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and value
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        
        boolean c = Bits.test(v, 0);
        int result = v >>> 1;
        boolean z = result == 0;
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, c);
    }
    
    /**
     * Rotates value one step to the left or right
     * Flag pattern : Z00C
     * C = value of bit which switched sides
     * @param d : direction of rotation, LEFT or RIGHT
     * @param v : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed result and value
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        
        int index = (d == RotDir.LEFT) ? 7 : 0;
        boolean c = Bits.test(v, index);
        int result = Bits.rotate(8, v, d.dirCode);
        boolean z = result == 0;
        
        return packValueZNHC(result, z, false, false, c);
    }
    
    /**
     * Rotates value one step to the left or right, using 9 bits
     * Uses the carry flag as the msb to enable 9 bit rotation
     * The carry flag returned is the msb of the result
     * Flag pattern : Z00C
     * C = value of bit which switched sides
     * @param d : direction of rotation, LEFT or RIGHT
     * @param v : 8 bits
     * @param c : used as 9th bit of rotation (true = 1)
     * @throws IllegalArgumentException if value not 8 bits
     * @return the packed result and value
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        
        int cMask = c ? Bits.mask(8) : 0;
        int result = Bits.rotate(9, cMask | v, d.dirCode);
        boolean z = Bits.clip(8, result) == 0;
        boolean finalC = Bits.test(result, 8);
        
        return packValueZNHC(Bits.clip(8, result), z, false, false, finalC);
    }
    
    /**
     * Swaps the 4 lsb with the 4 msb
     * Flag pattern : Z000
     * @param v : 8 bits
     * @throws IllegalArgumentException if not 8 bits
     * @return the packed value and results
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        
        int result = Bits.clip(4, v) << 4 | Bits.extract(v, 4, 4);
        return packValueZNHC(result, result == 0, false, false, false);
    }
    
    /**
     * Returns value 0 and flag z where z is true iff the value's bit at given index is 0
     * Flag pattern : Z010
     * @param v : 8 bits
     * @param bitIndex : the index of the bit to be tested
     * @throws IllegalArgumentException if value not 8 bits
     * @return
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        
        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }
    
    /**
     * Packs the value and flags into a single int, the 8 lsb is the flag section, the next 8 or 16 bits is the value
     * Pattern VALUE | FLAG where FLAG = ZNHC0000
     */
    private static int packValueZNHC(int value, boolean z, boolean n, boolean h, boolean c) {
        Preconditions.checkBits16(value);
        
        return (value << 8) | maskZNHC(z, n, h, c);
    }
}
