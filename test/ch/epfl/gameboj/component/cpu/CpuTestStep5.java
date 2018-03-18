package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.test.ProgramBuilder;

class CpuTestStep5 {

    @Test
    void fibonacci() {
        int[] program = new int[] {
            0x31, 0xFF, 0xFF, 0x3E,
            0x0B, 0xCD, 0x0A, 0x00,
            0x76, 0x00, 0xFE, 0x02,
            0xD8, 0xC5, 0x3D, 0x47,
            0xCD, 0x0A, 0x00, 0x4F,
            0x78, 0x3D, 0xCD, 0x0A,
            0x00, 0x81, 0xC1, 0xC9,
        };
        
        ProgramBuilder pb = new ProgramBuilder(program, 10000);
        pb.run();
        
        //assertEquals(8, pb.getResult()[0]);
        assertEquals(89, pb.getResult()[2]);
    }
    
    @Test
    void testJP_HL() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_HL_N16, 0x0006);
        pb.execOp(Opcode.JP_HL);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_A_N8, 0xEE);
        pb.run();
        
        assertEquals(0xEE, pb.getResult()[2]);
        assertEquals(0x00, pb.getResult()[4]);
    }
    
    @Test
    void testJP_N16() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.JP_N16, 0x0005);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_A_N8, 0xEE);
        pb.run();
        
        assertEquals(0xEE, pb.getResult()[2]);
        assertEquals(0x00, pb.getResult()[4]);
    }
    
    @Test
    void testJP_NZ_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOpAnd16(Opcode.JP_NZ_N16, 0x0009);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJP_Z_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOpAnd16(Opcode.JP_Z_N16, 0x0009);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJP_NC_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOpAnd16(Opcode.JP_NZ_N16, 0x0009);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJP_C_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0E);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOpAnd16(Opcode.JP_NZ_N16, 0x0009);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJR_E8() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd8(Opcode.JR_E8, 0x02);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJR_E8WithNegativeValue() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd8(Opcode.JR_E8, 0x04);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.execOpAnd8(Opcode.JR_E8, 0xFC);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJR_NZ_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOpAnd8(Opcode.JR_NZ_E8, 0x02);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJR_Z_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.JR_Z_E8, 0x02);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJR_NC_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOpAnd8(Opcode.JR_NC_E8, 0x02);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testJR_C_N16() {
        ProgramBuilder pb = new ProgramBuilder();        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0E);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.JR_C_E8, 0x02);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testCALL_N16() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd16(Opcode.CALL_N16, 0x08);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x06, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testCALL_NZ_N16() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOpAnd16(Opcode.CALL_NZ_N16, 0x0C);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0A, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testCALL_Z_N16() {
        ProgramBuilder pb = new ProgramBuilder(); 
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOpAnd16(Opcode.CALL_Z_N16, 0x0C);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0A, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testCALL_NC_N16() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOpAnd16(Opcode.CALL_NC_N16, 0x0C);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0A, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testCALL_C_N16() {
        ProgramBuilder pb = new ProgramBuilder(); 
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0E);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOpAnd16(Opcode.CALL_C_N16, 0x0C);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0A, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testRES_1() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOp(Opcode.RST_1);
        pb.execOp(Opcode.NOP);
        pb.execOp(Opcode.NOP);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testRET() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd16(Opcode.CALL_N16, 0x0008);
        pb.execOpAnd8(Opcode.LD_A_N8, 0xCC);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.execOp(Opcode.RET);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.run();
        
        assertEquals(0xCC, pb.getResult()[2]);
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
    }
    
    @Test
    void testRET_NZ() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd16(Opcode.CALL_N16, 0x0008);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOp(Opcode.RET_NZ);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0006, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testRET_Z() {
        ProgramBuilder pb = new ProgramBuilder(); 
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd16(Opcode.CALL_N16, 0x0008);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0f);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOp(Opcode.RET_Z);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0006, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testRET_NC() {
        ProgramBuilder pb = new ProgramBuilder();
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd16(Opcode.CALL_N16, 0x0008);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0F);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0E);
        pb.execOp(Opcode.RET_NC);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0006, pb.get16BitsFromBus(0xFFFD));
    }
    
    @Test
    void testRET_C() {
        ProgramBuilder pb = new ProgramBuilder(); 
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOpAnd16(Opcode.CALL_N16, 0x0008);
        pb.execOpAnd8(Opcode.LD_C_N8, 0xEE);
        
        pb.execOpAnd8(Opcode.LD_A_N8, 0x0E);
        pb.execOpAnd8(Opcode.CP_A_N8, 0x0F);
        pb.execOp(Opcode.RET_C);
        pb.execOpAnd8(Opcode.LD_B_N8, 0xDD);
        pb.run();
        
        assertEquals(0x00, pb.getResult()[4]);
        assertEquals(0xEE, pb.getResult()[5]);
        assertEquals(0x0006, pb.get16BitsFromBus(0xFFFD));
    }
}
