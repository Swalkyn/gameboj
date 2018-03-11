package ch.epfl.gameboj.component.cpu;


import java.util.ArrayList;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;

public final class Cpu implements Component, Clocked {
    
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT); 
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);
    private static final int OPCODE_PREFIX = 0xCB;
    private long nextNonIdleCycle = 0;
    private Bus bus;
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    
    private enum Reg16 {
        AF(Reg.A, Reg.F), 
        BC(Reg.B, Reg.C), 
        DE(Reg.D, Reg.E), 
        HL(Reg.H, Reg.L);
        
        public final Reg r1;
        public final Reg r2;
        
        Reg16(Reg r1, Reg r2) {
            this.r1 = r1;
            this.r2 = r2;
        }
    }
    
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }
    
    private int PC = 0;
    private int SP;
    
    private RegisterFile<Reg> rf = new RegisterFile<>(Reg.values());
    
    
    /* Methods for Component interface */
    
    /**
     * Stores bus as attribute then attaches Cpu to bus 
     * @param bus
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    
    /**
     * Implemented for component interface, returns NO_DATA
     * @param address
     * @return NO_DATA
     */
    @Override
    public int read(int address) {
        return Component.NO_DATA;
    }

    /**
     * Implemented for Component interface, does nothing
     * @param address
     * @param data
     */
    @Override
    public void write(int address, int data) {
        // Does nothing
    }
    
    /**
     * Executes next instruction based on program counter
     * @param cycle : number of elapsed cycles since start
     */
    @Override
    public void cycle(long cycle) {
        
        // If processor has something to do
        if (cycle == nextNonIdleCycle) {
            dispatch(read8(PC));
        }
    }
    
    
    /* Test helper */
    
    /**
     * @return an array of all the values stored in the cpu registers
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] registersValues = new int[10];
        
        registersValues[0] = PC;
        registersValues[1] = SP;
        
        int index = 2;
        for (Reg reg : Reg.values()) {
            registersValues[index] = rf.get(reg);
            index++;
        }
        
        return registersValues;
    }
    
    
    /* Read and write */
    
    /**
     * Reads an 8-bit value on the bus at specified address
     * @param address : the address to be read, 16 bits
     * @throws IllegalArgumentException if the address is not a 16-bit integer
     * @return the read value
     */
    private int read8(int address) {
        Preconditions.checkBits16(address);
        
        return bus.read(address);
    }
    
    /**
     * Reads an 8-bit value on the bus at the address stored in the HL register
     * @return the read value
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }
    
    /**
     * Reads an 8-bit value at the address following the opcode's address 
     * @return the read value
     */
    private int read8AfterOpcode() {
        return read8(PC + 1);
    }
    
    /**
     * Reads a 16-bit value on the bus at specified address
     * @param address : the address to be read, 16 bits
     * @throws IllegalArgumentException if the address is not a 16-bit integer
     * @return the read value
     */
    private int read16(int address) {
        Preconditions.checkBits16(address);
        
        int lsb = bus.read(address);
        int msb = bus.read(address + 1);
        
        return Bits.make16(msb, lsb);
    }
    
    /**
     * Reads a 16-bit value at the address following the opcode's address
     * @return the read value
     */
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }
    
    /**
     * Writes the given 8-bit value to the bus at the specified address
     * @param address : the address were the value will be written
     * @throws IllegalArgumentException if the address is not a 16-bit integer or the value is not a 8-bit integer
     * @param v : the value to be written
     */
    private void write8(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(v);
        
        bus.write(address, v);
    }
    
    /**
     * Writes the given 16-bit value on the bus at the specified address
     * @param address : the address were the value will be written
     * @throws IllegalArgumentException if the address is not a 16-bit integer or the value is not a 8-bit integer
     * @param v : the value to be written
     */
    private void write16(int address, int v) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits16(v);
        
        int lsb = Bits.clip(8, v);
        int msb = Bits.extract(v, 8, 8);
        
        bus.write(address, lsb);
        bus.write(address + 1, msb);
    }
    
    /**
     * Writes the given 8-bit value on the bus at the address stored in the HL register
     * @param v
     */
    private void write8AtHl(int v) {        
        write8(reg16(Reg16.HL), v);
    }
    
    /**
     * Writes the given 16-bit value at the address stored in SP - 2
     * @param v : the value to be written
     */
    private void push16(int v) {
        SP -= 2;
        write16(SP, v);
    }
    
    /**
     * Reads the value at the address stored in the SP register, and increments the address by 2
     * @return the read value
     */
    private int pop16() {
        int value = read16(SP);
        SP += 2;
        return value;
    }
    
    
    /* Getters and setters for 16-bit registers */
    
    /**
     * Reads a value stored in a 16-bit register
     * @param r : the register where the value is stored
     * @return the value
     */
    private int reg16(Reg16 r) {
        int lsb = rf.get(r.r1);
        int msb = rf.get(r.r2);
        
        return Bits.make16(msb, lsb);
    }
    
    /**
     * Stores a given 16-bit value in the specified 16-bit register
     * @param r : the register where the value will be stored
     * @param newV : the value to store
     * @throws IllegalArgumentException if the value is not a 16-bit integer
     */
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        
        int lsb = Bits.clip(8, newV);
        int msb = Bits.extract(newV, 8, 8);
        
        if (r == Reg16.AF) {
            lsb = 0;
        }
        
        rf.set(r.r1, lsb);
        rf.set(r.r2, msb);
    }
    
    /**
     * Stores a given 16-bit value in the specified 16-bit register, but if the the specified register is AF, stores the value in SP instead
     * @param r : the register where the value will be stored
     * @param newV : the value to store
     */
    private void setReg16SP(Reg16 r, int newV) {
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }
    }
    

    /* Bit extraction */
    
    /**
     * Extracts the 8-bit register's code from an opcode at a specified index
     * @param opcode : the opcode from which the register's code will be extracted
     * @param startBit : the index where the 3 bit long code starts
     * @throws IllegalArgumentException if the register code is not valid
     * @return the register's code
     */
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
    
    /**
     * Extracts the 16-bit register's code from an opcode
     * @param opcode : the opcode from which the register's code will be extracted
     * @throws IllegalArgumentException if the register code is not valid
     * @return the register's code
     */
    private Reg16 extractReg16(Opcode opcode) {
        int registerCode = Bits.extract(opcode.encoding, 4, 2);
        
        switch(registerCode) {
            case 0b00: return Reg16.BC;
            case 0b01: return Reg16.DE;
            case 0b10: return Reg16.HL;
            case 0b11: return Reg16.AF;

            default : throw new IllegalArgumentException("Invalid register code");
        }
    }
    
    /**
     * Determines whether the value in HL should be incremented or decremented from opcode
     * @param opcode : the opcode from which the increment will be extracted
     * @return the increment (1 or -1)
     */
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }
    
    private RotDir extractRotDir(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }
    
    private int extractBitIndex(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3);
    }
    
    private boolean extractNewBitValue(Opcode opcode) {
        return Bits.test(opcode.encoding, 6);
    }
    
    private boolean extractInitalCarry(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) && getFlagFromF(Flag.C);
    }
   
    /* Opcode table methods */
    
    /**
     * Creates a table of opcodes, filtering out non-direct ones
     * @param kind : the kind of opcodes the table will contain
     * @return the table of opcodes
     */
    private static Opcode[] buildOpcodeTable(Kind kind) {
        ArrayList<Opcode> table = new ArrayList<>();
        
        for (Opcode o : Opcode.values()) {
            if (o.kind == Kind.DIRECT) {
                table.add(o);
            }
        }
        
        return table.toArray(new Opcode[table.size()]);
    }
    
    /**
     * Searches an opcode by its encoding in an opcode table
     * @param opcodeEncoding : the encoding of the opcode
     * @throws NullPointerException if the register encoding is not 8 bits long
     * @return the opcode
     */
    private Opcode searchOpcodeTable(int opcodeEncoding, Opcode[] opcodeTable) {
        Preconditions.checkBits8(opcodeEncoding);
        
        for (Opcode o : opcodeTable) {
            if (o.encoding == opcodeEncoding) {
                return o;
            }
        }
        
        throw new NullPointerException("Opcode encoding does not exist");
    }
    
    /* Flag Manipulation */
    
    private void setRegFromAlu(Reg r, int vf) {
        rf.set(r, Alu.unpackValue(vf));
    }
    
    private void setFlags(int valueFlags) {
        rf.set(Reg.F, Alu.unpackFlags(valueFlags));
    }
    
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }
    
    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }
    
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {         
         int mask = Alu.maskZNHC(getFlagValue(vf, Flag.Z, z), getFlagValue(vf, Flag.N, n), 
                 getFlagValue(vf, Flag.H, h), getFlagValue(vf, Flag.C, c));
         
         setFlags(mask);
    }
    
    private boolean getFlagValue(int vf, Flag f, FlagSrc s) {
        if (s == FlagSrc.V0) return false;
        else if (s == FlagSrc.V1) return true;
        else if (s == FlagSrc.ALU) return Bits.test(vf, f);
        else return getFlagFromF(f);
    }
    
    private boolean getFlagFromF(Flag f) {
        return rf.testBit(Reg.F, f);
    }
    
    /* Dispatch method */
    
    /**
     * Executes a task corresponding to an opcode, and increments the program counter and the next non-idle cycle
     * @param opcodeEncoding : the opcode encoding
     */
    private void dispatch(int opcodeEncoding) {
        Preconditions.checkBits8(opcodeEncoding);
        
        Opcode opcode = null;
        if (opcodeEncoding == OPCODE_PREFIX) {
            opcode = searchOpcodeTable(read8AfterOpcode(), PREFIXED_OPCODE_TABLE);
        } else {
            opcode = searchOpcodeTable(opcodeEncoding, DIRECT_OPCODE_TABLE); 
        }
        
        switch (opcode.family) {
            case NOP: {
                // Does nothing
            } break;
            
            // Load instructions
            
            case LD_R8_HLR: {
                Reg reg = extractReg(opcode, 3);
                rf.set(reg, read8AtHl());
            } break;
            case LD_A_HLRU: {
                rf.set(Reg.A, read8AtHl());
                setReg16(Reg16.HL, reg16(Reg16.HL) + extractHlIncrement(opcode));
            } break;
            case LD_A_N8R: {
                rf.set(Reg.A, read8(0xFF00 + read8AfterOpcode()));
            } break;
            case LD_A_CR: {
                rf.set(Reg.A, read8(0xFF00 + rf.get(Reg.C)));
            } break;
            case LD_A_N16R: {
                rf.set(Reg.A, read8(read16AfterOpcode()));
            } break;
            case LD_A_BCR: {
                rf.set(Reg.A, read8(reg16(Reg16.BC)));
            } break;
            case LD_A_DER: {
                rf.set(Reg.A, read8(reg16(Reg16.DE)));
            } break;
            case LD_R8_N8: {
                Reg reg = extractReg(opcode, 3);
                rf.set(reg, read8AfterOpcode());
            } break;
            case LD_R16SP_N16: {
                Reg16 reg16 = extractReg16(opcode);
                setReg16SP(reg16, read16AfterOpcode());
            } break;
            case POP_R16: {
                Reg16 reg16 = extractReg16(opcode);
                setReg16(reg16, pop16());
            } break;
            
            // Write instructions
            
            case LD_HLR_R8: {
                Reg reg = extractReg(opcode, 0);
                write8AtHl(rf.get(reg));
            } break;
            case LD_HLRU_A: {
                write8AtHl(rf.get(Reg.A));
                setReg16(Reg16.HL, reg16(Reg16.HL) + extractHlIncrement(opcode));
            } break;
            case LD_N8R_A: {
                write8(0xFF00 + read8AfterOpcode(), rf.get(Reg.A));
            } break;
            case LD_CR_A: {
                write8(0xFF00 + rf.get(Reg.C), rf.get(Reg.A));
            } break;
            case LD_N16R_A: {
                write8(read16AfterOpcode(), rf.get(Reg.A));
            } break;
            case LD_BCR_A: {
                write8(reg16(Reg16.BC), rf.get(Reg.A));
            } break;
            case LD_DER_A: {
                write8(reg16(Reg16.DE), rf.get(Reg.A));
            } break;
            case LD_HLR_N8: {
                write8AtHl(read8AfterOpcode());
            } break;
            case LD_N16R_SP: {
                write16(read16AfterOpcode(), SP);
            } break;
            case PUSH_R16: {
                Reg16 reg16 = extractReg16(opcode);
                push16(reg16(reg16));
            } break;
            case LD_R8_R8: {
                Reg reg1 = extractReg(opcode, 0);
                Reg reg2 = extractReg(opcode, 3);
                
                rf.set(reg2, rf.get(reg1));
            } break;
            case LD_SP_HL: {
                SP = reg16(Reg16.HL);
            } break;
            
            // Add instructions
            
            case ADD_A_R8: {
                int vf = Alu.add(rf.get(Reg.A), rf.get(extractReg(opcode, 0)), extractInitalCarry(opcode));
                setRegFlags(Reg.A, vf);
            } break;
            case ADD_A_N8: {
                int vf = Alu.add(rf.get(Reg.A), read8AfterOpcode(), extractInitalCarry(opcode));
                setRegFlags(Reg.A, vf);
            } break;
            case ADD_A_HLR: {
                int vf = Alu.add(rf.get(Reg.A), read8AtHl(), extractInitalCarry(opcode));
                setRegFlags(Reg.A, vf);
            } break;
            case INC_R8: {
                Reg r = extractReg(opcode, 3);
                int vf = Alu.add(rf.get(r), 1);
                setRegFromAlu(r, vf);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            } break;
            case INC_HLR: {
                int vf = Alu.add(read8AtHl(), 1);
                write8AtHl(Alu.unpackValue(vf));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            } break;
            case INC_R16SP: {
                Reg16 r = extractReg16(opcode);
                int vf = Alu.add16H(reg16(r), 1);
                setReg16SP(r, Alu.unpackValue(vf));
                combineAluFlags(vf, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU);
            } break;
            case ADD_HL_R16SP: {
                Reg16 r = extractReg16(opcode);
                int vf = Alu.add16H(reg16(Reg16.HL), reg16(r));
                setReg16(Reg16.HL, Alu.unpackValue(vf));
                combineAluFlags(vf, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            } break;
            case LD_HLSP_S8: {
                int vf = Alu.add(SP, Bits.clip(8, Bits.signExtend8(read8AfterOpcode())));
                if (Bits.test(opcode.encoding, 4)) {
                    setReg16(Reg16.HL, Alu.unpackValue(vf));
                } else {
                    SP = Alu.unpackValue(vf);
                }
                combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            } break;
            
         // Subtract
            case SUB_A_R8: {
                int vf = Alu.sub(rf.get(Reg.A), rf.get(extractReg(opcode, 0)), extractInitalCarry(opcode));
                setRegFlags(Reg.A, vf);
            } break;
            case SUB_A_N8: {
                int vf = Alu.sub(rf.get(Reg.A), read8AfterOpcode(), extractInitalCarry(opcode));
                setRegFlags(Reg.A, vf);
            } break;
            case SUB_A_HLR: {
                int vf = Alu.sub(rf.get(Reg.A), read8AtHl(), extractInitalCarry(opcode));
                setRegFlags(Reg.A, vf);;
            } break;
            case DEC_R8: {
                Reg r = extractReg(opcode, 3);
                int vf = Alu.sub(rf.get(r), 1);
                setRegFromAlu(r, vf);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
            } break;
            case DEC_HLR: {
                int vf = Alu.sub(read8AtHl(), 1);
                write8AtHl(Alu.unpackValue(vf));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            } break;
            case CP_A_R8: {
                int vf = Alu.sub(rf.get(Reg.A), rf.get(extractReg(opcode, 0)), extractInitalCarry(opcode));
                setFlags(vf);
            } break;
            case CP_A_N8: {
                int vf = Alu.sub(rf.get(Reg.A), read8AfterOpcode(), extractInitalCarry(opcode));
                setFlags(vf);
            } break;
            case CP_A_HLR: {
                int vf = Alu.sub(rf.get(Reg.A), read8AtHl(), extractInitalCarry(opcode));
                setFlags(vf);
            } break;
            case DEC_R16SP: {
                Reg16 r = extractReg16(opcode);
                if (r == Reg16.AF) {
                    SP = Bits.clip(16, SP - 1);
                } else {
                    setReg16SP(r, Bits.clip(16, reg16(r) - 1));                    
                }
            } break;
            
         // And, or, xor, complement
            case AND_A_N8: {
                int vf = Alu.and(rf.get(Reg.A), read8AfterOpcode());
                setRegFlags(Reg.A, vf);
            } break;
            case AND_A_R8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.and(rf.get(Reg.A), rf.get(r));
                setRegFlags(Reg.A, vf);
            } break;
            case AND_A_HLR: {
                int vf = Alu.and(rf.get(Reg.A), read8AtHl());
                setRegFlags(Reg.A, vf);
            } break;
            case OR_A_R8: {
                int vf = Alu.or(rf.get(Reg.A), read8AfterOpcode());
                setRegFlags(Reg.A, vf);
            } break;
            case OR_A_N8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.or(rf.get(Reg.A), rf.get(r));
                setRegFlags(Reg.A, vf);
            } break;
            case OR_A_HLR: {
                int vf = Alu.or(rf.get(Reg.A), read8AtHl());
                setRegFlags(Reg.A, vf);
            } break;
            case XOR_A_R8: {
                int vf = Alu.xor(rf.get(Reg.A), read8AfterOpcode());
                setRegFlags(Reg.A, vf);
            } break;
            case XOR_A_N8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.xor(rf.get(Reg.A), rf.get(r));
                setRegFlags(Reg.A, vf);
            } break;
            case XOR_A_HLR: {
                int vf = Alu.xor(rf.get(Reg.A), read8AtHl());
                setRegFlags(Reg.A, vf);
            } break;
            case CPL: {
                int v = Bits.complement8(rf.get(Reg.A));
                setRegFromAlu(Reg.A, v);
                combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
            } break;
            
         // Rotate, shift
            case ROTCA: {
                RotDir rd = extractRotDir(opcode);
                int vf = Alu.rotate(rd, rf.get(Reg.A));
                setRegFromAlu(Reg.A, vf);
                combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            } break;
            case ROTA: {
                RotDir rd = extractRotDir(opcode);
                int vf = Alu.rotate(rd, rf.get(Reg.A), getFlagFromF(Flag.C));
                setRegFromAlu(Reg.A, vf);
                combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            } break;
            case ROTC_R8: {
                RotDir rd = extractRotDir(opcode);
                Reg r = extractReg(opcode, 0);
                int vf = Alu.rotate(rd, rf.get(r));
                setRegFlags(r, vf);
            } break;
            case ROT_R8: {
                RotDir rd = extractRotDir(opcode);
                Reg r = extractReg(opcode, 0);
                int vf = Alu.rotate(rd, rf.get(r), getFlagFromF(Flag.C));
                setRegFlags(r, vf);
            } break;
            case ROTC_HLR: {
                RotDir rd = extractRotDir(opcode);
                int vf = Alu.rotate(rd, read8AtHl());
                write8AtHlAndSetFlags(vf);
            } break;
            case ROT_HLR: {
                RotDir rd = extractRotDir(opcode);
                int vf = Alu.rotate(rd, read8AtHl(), getFlagFromF(Flag.C));
                write8AtHlAndSetFlags(vf);
            } break;
            case SWAP_R8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.swap(rf.get(r));
                setRegFlags(r, vf);
            } break;
            case SWAP_HLR: {
                int vf = Alu.swap(read8AtHl());
                write8AtHlAndSetFlags(vf);
            } break;
            case SLA_R8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.shiftLeft(rf.get(r));
                setRegFlags(r, vf);
            } break;
            case SRA_R8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.shiftRightA(rf.get(r));
                setRegFlags(r, vf);
            } break;
            case SRL_R8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.shiftRightL(rf.get(r));
                setRegFlags(r, vf);
            } break;
            case SLA_HLR: {
                int vf = Alu.shiftLeft(read8AtHl());
                write8AtHlAndSetFlags(vf);
            } break;
            case SRA_HLR: {
                int vf = Alu.shiftRightA(read8AtHl());
                write8AtHlAndSetFlags(vf);
            } break;
            case SRL_HLR: {
                int vf = Alu.shiftRightL(read8AtHl());
                write8AtHlAndSetFlags(vf);
            } break;
            
         // Bit test and set
            case BIT_U3_R8: {
                Reg r = extractReg(opcode, 0);
                int vf = Alu.testBit(rf.get(r), extractBitIndex(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
            } break;
            case BIT_U3_HLR: {
                int vf = Alu.testBit(read8AtHl(), extractBitIndex(opcode));
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
            } break;
            case CHG_U3_R8: {
                Reg r = extractReg(opcode, 0);
                rf.set(r, Bits.set(rf.get(r), extractBitIndex(opcode), extractNewBitValue(opcode)));
            } break;
            case CHG_U3_HLR: {
                write8AtHl(Bits.set(read8AtHl(), extractBitIndex(opcode), extractNewBitValue(opcode)));
            } break;

            // Misc. ALU
            case DAA: {
                int vf = Alu.bcdAdjust(rf.get(Reg.A), getFlagFromF(Flag.N), getFlagFromF(Flag.H), getFlagFromF(Flag.C));
                setRegFromAlu(Reg.A, vf);
                combineAluFlags(vf, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
            } break;
            case SCCF: {
                rf.setBit(Reg.F, Flag.C, !extractInitalCarry(opcode));
            } break;

            default:
                break;
        }
        
        PC += opcode.totalBytes;
        nextNonIdleCycle += opcode.cycles;
    }
}
