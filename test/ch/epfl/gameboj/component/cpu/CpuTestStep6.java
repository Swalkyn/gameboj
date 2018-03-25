package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.test.ProgramBuilder;

class CpuTestStep6 {

    @Test
    void jrNegativeWorks() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd8(Opcode.LD_A_N8, 0);           // 0 1
        pb.execOpAnd16(Opcode.JP_N16, 16);          // 2 3 4
        pb.execOp(Opcode.INC_A);                    // 5
        pb.execOp(Opcode.INC_A);                    // 6
        pb.execOp(Opcode.INC_A);                    // 7
        pb.execOpAnd8(Opcode.CP_A_N8, 2);           // 8 9
        pb.execOpAnd16(Opcode.JP_NZ_N16, 19);       // 10 11 12
        pb.execOpAnd16(Opcode.JP_N16, 18);          // 13 14 15
        pb.execOpAnd8(Opcode.JR_E8, 0b1111_0100);   // 16 17
        pb.execOp(Opcode.HALT);                     // 18
        pb.execOpAnd8(Opcode.LD_A_N8, 0x99);        // 19 20
        
        pb.run(500);
        
        assertEquals(2, pb.getResult()[2]);
    }
    
    @Test
    void jrPositiveWorks() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd8(Opcode.LD_A_N8, 0);           // 0 1
        pb.execOpAnd8(Opcode.JR_E8, 1);             // 2 3 
        pb.execOp(Opcode.INC_A);                    // 4
        pb.execOp(Opcode.INC_A);                    // 5
        pb.execOp(Opcode.INC_A);                    // 6
        pb.execOpAnd8(Opcode.CP_A_N8, 2);           // 7 8
        pb.execOpAnd16(Opcode.JP_NZ_N16, 13);       // 9 10 11
        pb.execOp(Opcode.HALT);                     // 12
        pb.execOpAnd8(Opcode.LD_A_N8, 0x99);        // 13 14
        
        
        pb.run(500);
        
        assertEquals(2, pb.getResult()[2]);
    }

}
