package ch.epfl.test;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Opcode;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class ProgramBuilder {
    
    private List<Integer> program;
    private List<Integer> memory;
    private Cpu cpu;
    public static final int PREFIX = 0xCB;
    private static final int MEMORY_INDEX = 0x20;
    private int totalCycles = 0;
    
    public ProgramBuilder() {
        program = new ArrayList<>();
        memory = new ArrayList<>();
    }
    
    public ProgramBuilder(int[] program, int cycles) {
        this();
        
        for (int i : program) {
            addInt(i);
        }
        
        totalCycles = cycles;
    }
    
    private boolean isPrefixed(Opcode op) {
        return op.kind == Opcode.Kind.PREFIXED;
    }
    
    public void execOp(Opcode op) {
        if (isPrefixed(op)) {
            program.add(PREFIX);
        }
        
        program.add(op.encoding);
        totalCycles += op.cycles;
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
        int initialSize = program.size();
        for (int i = 0; i < MEMORY_INDEX - initialSize; i++) {
            program.add(0);
        }
        program.addAll(memory);
        
        int[] data = new int[program.size()];
        for (int i = 0; i < program.size(); i++) {
            data[i] = program.get(i);
        }
        
        return data;
    }
    
    public void storeInt(int value) {
        memory.add(value);
    }
    
    public int getMemoryAddress(int address) {
        return MEMORY_INDEX + address;
    }
    
    public void execManualOpcode(int encoding, int cycles, Kind kind) {
        Preconditions.checkBits8(encoding);
        
        if (kind == Opcode.Kind.PREFIXED) {
            addInt(PREFIX);
        }
        
        program.add(encoding);
        totalCycles += cycles;
    }
    
    public void execManualOpcode(int encoding, int cycles) {
        execManualOpcode(encoding, cycles, Opcode.Kind.DIRECT);
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
        int[] data = build();
        cpu = newCpu(data);
        runCpu(cpu, cycle);
    }
    
    public void run() {
        // run(program.size());
        run(totalCycles);
    }
    
    public int[] getResult() {
        return cpu._testGetPcSpAFBCDEHL();
    }
}
