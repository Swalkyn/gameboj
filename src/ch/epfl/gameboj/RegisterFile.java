package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {

    private int size;
    private int[] registerFile;
    
    public RegisterFile(E[] allRegs) {
        size = allRegs.length;
        registerFile = new int[size];
    }
    
    public int get(E reg) {
        return registerFile[reg.index()];
    }
    
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        
        registerFile[reg.index()] = newValue;
    }
    
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }
    
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}
