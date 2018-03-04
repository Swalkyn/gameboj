package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * Represents a register file, where the value of the registers are stored in a byte array
 * @author sylvainkuchen
 *
 * @param <E> : the type of the registers
 */
public final class RegisterFile<E extends Register> {

    private int size;
    private byte[] registerFile;
    
    /**
     * Creates a new register file given a array of registers
     * @param allRegs : the array of registers
     */
    public RegisterFile(E[] allRegs) {
        size = allRegs.length;
        registerFile = new byte[size];
    }
    
    /**
     * @param reg : the register in which the value is stored
     * @return the value stored in the register, in the form of an unsigned integer
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(registerFile[reg.index()]);
    }
    
    /**
     * Stores a given value into the specified register
     * @param reg : the register where the value will be stored
     * @param newValue : the value that will be stored
     */
    public void set(E reg, int newValue) {        
        Preconditions.checkBits8(newValue);
        
        registerFile[reg.index()] = (byte) newValue;
    }
    
    /**
     * Tests a specified bit of the value stored in the given register
     * @param reg : the register where the value is stored
     * @param b : the bit which will be tested
     * @return true if the bit is 1, false otherwise
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }
    
    /**
     * Sets a specified bit of the value stored in the given register to a new value
     * @param reg : the register where the value is stored
     * @param bit : the bit which will be set
     * @param newValue : the new value of the given bit
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}
