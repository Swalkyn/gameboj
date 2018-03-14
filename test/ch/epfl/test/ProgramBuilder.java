package ch.epfl.test;

import java.util.ArrayList;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Opcode;

public class ProgramBuilder {
    
    private ArrayList<Integer> program;
    public static final int PREFIX = 0xCB;
    
    public ProgramBuilder() {
        program = new ArrayList<>();
    }
    
    private boolean isPrefixed(Opcode op) {
        return op.kind == Opcode.Kind.PREFIXED;
    }
    
    public void execOp(Opcode op) {
        if (isPrefixed(op)) {
            program.add(PREFIX);
        }
        
        program.add(op.encoding);
    }
    
    public void execOpAnd8(Opcode op, int value) {
        Preconditions.checkBits8(value);
        execOp(op);
        program.add(value);
    }
    
    public void execOpAnd16(Opcode op, int value) {
        Preconditions.checkBits16(value);
        execOp(op);
        addInt(Bits.clip(8, value));
        addInt(Bits.extract(value, 8, 8));
    }
    
    public void addInt(int value) {
        Preconditions.checkBits8(value);
        program.add(value);
    }
    
    public int[] build() {
        int[] prog = new int[program.size()];
        
        for (int i = 0; i < program.size(); i++) {
            prog[i] = program.get(i);
        }
        
        return prog;
    }
}
