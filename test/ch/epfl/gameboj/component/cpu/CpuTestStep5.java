package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
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
        
        ProgramBuilder pb = new ProgramBuilder(program, 200000000);
        pb.run();
        
        assertEquals(9, pb.getResult()[0]);
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
    
    /* Interrupt Tests */
    
    @Test
    void singleInterruptsAreCorrectlyHandled() {       
        for (Interrupt interrupt : Cpu.Interrupt.values()) {
            ProgramBuilder pb = new ProgramBuilder();
            
            pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
            pb.execOp(Opcode.EI);
            pb.execOpAnd8(Opcode.LD_A_N8, interrupt.mask());
            pb.execOpAnd16(Opcode.LD_N16R_A, AddressMap.REG_IE);
            pb.execOpAnd16(Opcode.LD_N16R_A, AddressMap.REG_IF);
            pb.execOpAnd8(Opcode.LD_C_N8, 0x21);
            
            int[] interruptHandler = {
                    Opcode.LD_B_N8.encoding,
                    0x13,
                    Opcode.RET.encoding
            };
            
            pb.ramAt(AddressMap.INTERRUPTS[interrupt.index()], interruptHandler);
            pb.run(40);
            
            assertEquals(0x13, pb.getResult()[4]);
            assertEquals(0x21, pb.getResult()[5]);
        }
    }
    
    @Test
    void interruptsDoNotTriggerWithIMEOff() {       
        for (Interrupt interrupt : Cpu.Interrupt.values()) {
            ProgramBuilder pb = new ProgramBuilder();
            
            pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
            pb.execOp(Opcode.EI);
            pb.execOpAnd8(Opcode.LD_A_N8, interrupt.mask());
            pb.execOpAnd8(Opcode.LD_B_N8, 0x44);
            pb.execOp(Opcode.DI);
            pb.execOpAnd16(Opcode.LD_N16R_A, AddressMap.REG_IE);
            pb.execOpAnd16(Opcode.LD_N16R_A, AddressMap.REG_IF);
            pb.execOpAnd8(Opcode.LD_C_N8, 0x21);
            
            int[] interruptHandler = {
                    Opcode.LD_B_N8.encoding,
                    0x13,
                    Opcode.RET.encoding
            };
            
            pb.ramAt(AddressMap.INTERRUPTS[interrupt.index()], interruptHandler);
            pb.run(40);
            
            assertEquals(0x44, pb.getResult()[4]);
            assertEquals(0x21, pb.getResult()[5]);
        }
    }
    
    @Test
    void multipleInterruptsAreCorrectlyHandled() {    
        
        int interruptMask = Cpu.Interrupt.SERIAL.mask() | Cpu.Interrupt.JOYPAD.mask() | Cpu.Interrupt.LCD_STAT.mask();
        
        ProgramBuilder pb = new ProgramBuilder();
        
        pb.execOpAnd16(Opcode.LD_SP_N16, 0xFFFF);
        pb.execOp(Opcode.EI);
        pb.execOpAnd8(Opcode.LD_A_N8, interruptMask);
        pb.execOpAnd8(Opcode.LD_B_N8, 0x44);
        pb.execOpAnd16(Opcode.LD_N16R_A, AddressMap.REG_IE);
        pb.execOpAnd16(Opcode.LD_N16R_A, AddressMap.REG_IF);
        pb.execOpAnd8(Opcode.LD_C_N8, 0x21);
        
        int[] lcdStatInterruptHandler = {
                Opcode.LD_B_N8.encoding,
                0x13,
                Opcode.LD_A_N8.encoding,
                0x13,
                Opcode.RETI.encoding
        };

        int[] serialInterruptHandler = {
                Opcode.LD_B_N8.encoding,
                0x21,
                Opcode.ADD_A_B.encoding,
                Opcode.RETI.encoding
        };
        
        int[] joypadInterruptHandler = {
                Opcode.LD_B_N8.encoding,
                0x44,
                Opcode.ADD_A_B.encoding,
                Opcode.RETI.encoding
        };
        
        pb.ramAt(AddressMap.INTERRUPTS[Cpu.Interrupt.LCD_STAT.index()], lcdStatInterruptHandler);
        pb.ramAt(AddressMap.INTERRUPTS[Cpu.Interrupt.SERIAL.index()], serialInterruptHandler);
        pb.ramAt(AddressMap.INTERRUPTS[Cpu.Interrupt.JOYPAD.index()], joypadInterruptHandler);
        
        pb.run(55);
        
        assertEquals(0x78, pb.getResult()[2]);
        assertEquals(0x44, pb.getResult()[4]);
        assertEquals(0x21, pb.getResult()[5]);
    }
    
    /* Halt tests */
}
