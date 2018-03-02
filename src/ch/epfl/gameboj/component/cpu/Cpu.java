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
    
    private enum Reg16 {
        AF(Reg.A, Reg.F), 
        BC(Reg.B, Reg.C), 
        DE(Reg.D, Reg.F), 
        HL(Reg.H, Reg.L);
        
        public final Reg r1;
        public final Reg r2;
        
        Reg16(Reg r1, Reg r2) {
            this.r1 = r1;
            this.r2 = r2;
        }
    }
    
    private int PC = 0;
    private int SP;
    
    private RegisterFile<Reg> rf = new RegisterFile<>(Reg.values());
    
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    
    
    /* Read and write */
    
    private int read8(int address) {
        Preconditions.checkBits16(address);
        
        return bus.read(address);
    }
    
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }
    
    private int read8AfterOpcode() {
        return read8(PC + 1);
    }
    
    private int read16(int address) {
        Preconditions.checkBits16(address);
        
        int lsb = bus.read(address);
        int msb = bus.read(address + 1);
        
        return Bits.make16(msb, lsb);
    }
    
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }
    
    private void write8(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(v);
        
        bus.write(address, v);
    }
    
    private void write16(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits16(v);
        
        int lsb = Bits.clip(8, v);
        int msb = Bits.extract(v, 8, 8);
        
        bus.write(address, lsb);
        bus.write(address + 1, msb);
    }
    
    private void write8AtHl(int v) {        
        write8(reg16(Reg16.HL), v);
    }
    
    private void push16(int v) {
        SP -= 2;
        write16(SP, v);
    }
    
    private int pop16() {
        int value = read16(SP);
        SP += 2;
        return value;
    }
    
    
    /* Getters and setters for 16-bit registers */
    
    private int reg16(Reg16 r) {
        int lsb = rf.get(r.r1);
        int msb = rf.get(r.r2);
        
        return Bits.make16(msb, lsb);
    }
    
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        
        int lsb = Bits.clip(newV, 8);
        int msb = Bits.extract(newV, 8, 8);
        
        if (r == Reg16.AF) {
            lsb = 0;
        }
        
        rf.set(r.r1, lsb);
        rf.set(r.r2, msb);
    }
    
    private void setReg16SP(Reg16 r, int newV) {
        if (r == Reg16.AF) {
            SP = newV;
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
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
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
        // TDOD INCR PC
        
        switch (opcode.family) {
            case NOP: {
                // Does nothing
            } break;
            case LD_R8_HLR: {
                Reg reg = extractReg(opcode, 3);
                rf.set(reg, read8AtHl());
            } break;
            case LD_A_HLRU: {
                rf.set(Reg.A, read8AtHl());
                setReg16(Reg16.HL, read8AtHl() + extractHlIncrement(opcode));
            } break;
            case LD_A_N8R: {
                rf.set(Reg.A, 0xFF + read8AfterOpcode());
            } break;
            case LD_A_CR: {
                rf.set(Reg.A, 0xFF + rf.get(Reg.C));
            } break;
            case LD_A_N16R: {
                rf.set(Reg.A, read16AfterOpcode());
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
