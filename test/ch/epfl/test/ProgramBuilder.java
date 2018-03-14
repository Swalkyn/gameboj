package ch.epfl.test;

import java.util.ArrayList;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Opcode;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class ProgramBuilder {
    
    private ArrayList<Integer> program;
    private Cpu cpu;
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
    
    private int[] build() {
        int[] prog = new int[program.size()];
        
        for (int i = 0; i < program.size(); i++) {
            prog[i] = program.get(i);
        }
        
        return prog;
    }
    
    private Cpu newCpu(int[] program) {
        Ram ram = new Ram(program.length);
        RamController rc = new RamController(ram, 0);
        Bus bus = new Bus();        
        Cpu cpu = new Cpu();
        
        // Fill ram
        for (int i = 0; i < program.length; i++) {
            rc.write(i, program[i]);
        }
        
        rc.attachTo(bus);
        cpu.attachTo(bus);
        
        return cpu;
    }
    
    private void runCpu(Cpu cpu, int numberOfCycles) {
        for (long i = 0; i < numberOfCycles; i++) {
            cpu.cycle(i);
        }
    }
    
    public void run(int cycle) {
        int[] prog = build();
        cpu = newCpu(prog);
        runCpu(cpu, cycle);
    }
    
    public void run() {
        run(program.size());
    }
    
    public int[] getResult() {
        return cpu._testGetPcSpAFBCDEHL();
    }
}
