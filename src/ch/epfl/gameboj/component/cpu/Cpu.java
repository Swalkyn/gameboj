package ch.epfl.gameboj.component.cpu;


import java.util.ArrayList;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
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
    
    
    /* Read and write */
    
    private int read8(int address) {
        Preconditions.checkBits8(address);
        
        return bus.read(address);
    }
    
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }
    
    private int read8AfterOpcode() {
        return read8(reg16(Reg16.PC) + 1);
        // TODO set PC to 0 ?
    }
    
    private int read16(int address) {
        Preconditions.checkBits16(address);
        
        return bus.read(address);
    }
    
    private int read16AfterOpcode() {
        return read16(reg16(Reg16.PC) + 1);
    }
    
    private void write8(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(v);
        
        bus.write(address, v);
    }
    
    private void write16(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits16(v);
        
        bus.write(address, v);
    }
    
    private void write8AtHl(int v) {        
        write8(reg16(Reg16.HL), v);
    }
    
    private void push16(int v) {
        int newSpAddress = reg16(Reg16.SP) - 2;
        
        setReg16(Reg16.SP, newSpAddress);
        write16(newSpAddress, v);
    }
    
    private int pop16() {
        int address = reg16(Reg16.SP);
        setReg16(Reg16.SP, address + 2);
        
        return bus.read(address);
    }
    
    
    /* Getters and setters for registers */
    
    private int reg16(Reg16 r) {
        return rf16.get(r);
    }
    
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        
        if (r == Reg16.AF) {
            newV = Bits.extract(newV, 8, 8) << 8;
        }
        
        rf16.set(r, newV);
    }
    
    private void setReg16SP(Reg16 r, int newV) {
        if (r == Reg16.AF) {
            setReg16(Reg16.SP, newV);
        } else {
            setReg16(r, newV);
        }
    }
    

    /* Bit extraction */
    
    private Reg extractReg(Opcode opcode, int startBit) {
        int registerCode = Bits.extract(opcode.encoding, startBit, 3);
        
        switch(registerCode) {
            case 0b000: return Reg.B;
            case 0b001: return Reg.C;
            case 0b010: return Reg.D;
            case 0b011: return Reg.E;
            case 0b100: return Reg.H;
            case 0b101: return Reg.L;
            case 0b111: return Reg.A;
            
            default : throw new IllegalArgumentException("Invalid register code");
        }
    }
    
    private Reg16 extractReg16(Opcode opcode) {
        int registerCode = Bits.extract(opcode.encoding, 4, 2);
        
        // TODO : ask for AF/SP conflict
        switch(registerCode) {
            case 0b00: return Reg16.BC;
            case 0b01: return Reg16.DE;
            case 0b10: return Reg16.HL;
            case 0b11: return Reg16.AF;

            default : throw new IllegalArgumentException("Invalid register code");
        }
    }
    
    private int extractHlIncrement(Opcode opcode) {
        // TODO : return order ?
        return Bits.test(opcode.encoding, 4) ? 1 : -1;
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
