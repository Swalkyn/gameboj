package ch.epfl.gameboj.component.cpu;


import java.util.ArrayList;
import java.util.Arrays;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;

public final class Cpu implements Component, Clocked {
    
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT); 
    private long nextNonIdleCycle = 0;
    private Bus bus;
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    
    private enum Reg16 implements Register{
        AF, BC, DE, HL, PC, SP
    }
    
    private RegisterFile<Reg> rf = new RegisterFile<>(Reg.values());
    private RegisterFile<Reg16> rf16 = new RegisterFile<>(Reg16.values());
    
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    
    private int read8(int address) {
        Preconditions.checkBits8(address);
        
        return bus.read(address);
    }
    
    private int read8AtHl() {
        return bus.read(rf16.get(Reg16.HL));
    }
    
    private int read8AfterOpcode() {
        return bus.read(rf16.get(Reg16.PC) + 1);
        // TODO set PC to 0 ?
    }
    
    
    
    @Override
    public int read(int address) {
        return Component.NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        // Does nothing
    }
    
    @Override
    public void cycle(long cycle) {
        
        // If processor has something to do
        if (cycle == nextNonIdleCycle) {
            
        }
    }
    
    private static Opcode[] buildOpcodeTable(Kind kind) {
        ArrayList<Opcode> table = new ArrayList<>();
        
        for (Opcode o : Opcode.values()) {
            if (o.kind == Kind.DIRECT) {
                table.add(o);
            }
        }
        
        return table.toArray(new Opcode[table.size()]);
    }
    
    private Opcode searchOpcodeTable(int opcodeEncoding) {
        Preconditions.checkBits8(opcodeEncoding);
        
        for (Opcode o : DIRECT_OPCODE_TABLE) {
            if (o.encoding == opcodeEncoding) {
                return o;
            }
        }
        
        throw new IllegalArgumentException("Opcode encoding does not exist");
    }
    
    public void dispatch(int opcodeEncoding) {
        Preconditions.checkBits8(opcodeEncoding);
        
        Opcode opcode = searchOpcodeTable(opcodeEncoding); 
        
        switch (opcode.family) {
            case NOP: {
            } break;
            case LD_R8_HLR: {
            } break;
            case LD_A_HLRU: {
            } break;
            case LD_A_N8R: {
            } break;
            case LD_A_CR: {
            } break;
            case LD_A_N16R: {
            } break;
            case LD_A_BCR: {
            } break;
            case LD_A_DER: {
            } break;
            case LD_R8_N8: {
            } break;
            case LD_R16SP_N16: {
            } break;
            case POP_R16: {
            } break;
            case LD_HLR_R8: {
            } break;
            case LD_HLRU_A: {
            } break;
            case LD_N8R_A: {
            } break;
            case LD_CR_A: {
            } break;
            case LD_N16R_A: {
            } break;
            case LD_BCR_A: {
            } break;
            case LD_DER_A: {
            } break;
            case LD_HLR_N8: {
            } break;
            case LD_N16R_SP: {
            } break;
            case LD_R8_R8: {
            } break;
            case LD_SP_HL: {
            } break;
            case PUSH_R16: {
            } break;
    
            default:
                break;
            }
        
        // TODO nextNonIdleCycle, PC
    }
    
    
    
    
    
}
