package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class CpuTest {
    
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
    
    private Cpu newCpu() {
        return newCpu(new int[0]);
    }
    

    @Test
    void attachToDoesNotThrowException() {
        Bus b = new Bus();
        Cpu c = newCpu();
        
        c.attachTo(b);
    }
    
    @Test
    void readReturnsNoData() {
        assertEquals(Component.NO_DATA, newCpu().read(0xFFFF));
        assertEquals(Component.NO_DATA, newCpu().read(0x0000));
        assertEquals(Component.NO_DATA, newCpu().read(0xF0F0));
    }
    
    @Test
    void writeDoesNotThrowException() {
        newCpu().write(0x00, 0xFF);
    }
    
    @Test
    void initalRegistersAreAllZero() {
        int[] expected = {0,0,0,0,0,0,0,0,0,0};
        Cpu cpu = newCpu();
        
        assertArrayEquals(expected, cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void testNOP() {
        int[] program = { Opcode.NOP.encoding };
        Cpu cpu = newCpu(program);
        int[] expected = {1,0,0,0,0,0,0,0,0,0};
        
        cpu.cycle(0);
        assertArrayEquals(expected, cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void testLD_R8_HLR() {        
        int[] program = {
                Opcode.LD_HL_N16.encoding,      // 0: LD HL nn
                0x04,                           // 1:
                0x00,                           // 2: address 0x0004
                Opcode.LD_A_HLR.encoding,       // 3: LD A HL
                0x13                            // 4: 
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_A_HLRU_Increment() {
        int[] program = {
                Opcode.LD_HL_N16.encoding,      // 0: LD HL nn
                0x04,                           // 1:
                0x00,                           // 2: address 0x0004
                Opcode.LD_A_HLRI.encoding,      // 3: LD A [HL+] HL = 0x0005
                0x13                            // 4: HL = 0x0005
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x05, cpu._testGetPcSpAFBCDEHL()[8]);
    }
    
    @Test
    void testLD_A_HLRU_Decrement() {
        int[] program = {
                Opcode.LD_HL_N16.encoding,      // 0: LD HL nn
                0x04,                           // 1:
                0x00,                           // 2: address 0x0004
                Opcode.LD_A_HLRD.encoding,      // 3: LD A [HL+] HL = 0x0005
                0x13                            // 4: HL = 0x0005
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x03, cpu._testGetPcSpAFBCDEHL()[8]);
    }
    
    @Test
    void testLD_A_N8R() {
    	    int[] program = new int[0xFFFF];
    	
    	    program[0] = Opcode.LD_A_N8R.encoding;
    	    program[1] = 0x04;
    	    program[0xFF00 + 0x04] = 0x13;
    	
    	    Cpu cpu = newCpu(program);
        runCpu(cpu, 5);
         
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_A_CR() {
        int[] program = new int[0xFFFF];
        
        program[0] = Opcode.LD_C_N8.encoding;
        program[1] = 0x04;
        program[2] = Opcode.LD_A_CR.encoding;
        program[0xFF00 + 0x04] = 0x13;
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, 5);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_A_N16R() {
        int[] program = {
                Opcode.LD_A_N16R.encoding,
                0x03,
                0x00,
                0x013
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_A_BCR() {
        int[] program = {
                Opcode.LD_BC_N16.encoding,
                0x04,
                0x00,
                Opcode.LD_A_BCR.encoding,
                0x13
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
        
    }
    
    @Test
    void testLD_A_DER() {
        int[] program = {
                Opcode.LD_BC_N16.encoding,
                0x04,
                0x00,
                Opcode.LD_A_BCR.encoding,
                0x13
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_R8_N8() {
        int[] program = {
                Opcode.LD_E_N8.encoding,
                0x13,
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[7]);
    }
    
    @Test
    void testLD_R16SP_N16() {
        int[] program = {
                Opcode.LD_DE_N16.encoding,
                0x13,
                0x14
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[6]);
        assertEquals(0x14, cpu._testGetPcSpAFBCDEHL()[7]);
    }

    @Test
    void testLD_R16SP_N16WithSPAsRegister() {
        int[] program = {
                Opcode.LD_SP_N16.encoding,
                0x13,
                0x14
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x1413, cpu._testGetPcSpAFBCDEHL()[1]);
    }
    
    @Test
    void testPOP_R16() {
        int[] program = new int[0xFF00];
        program[0] = Opcode.LD_SP_N16.encoding;
        program[1] = 0x04;
        program[2] = 0x05;
        program[3] = Opcode.POP_BC.encoding;
        program[0x0504] = 0x14;
        program[0x0505] = 0x13;
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, 5);
        
        assertEquals(0x14, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[5]);
        assertEquals(0x0504 + 2, cpu._testGetPcSpAFBCDEHL()[1]);
    }
    
    @Test
    void testLD_HLR_R8() {
        int[] program = {
        		Opcode.LD_C_N8.encoding,		// 0
        		0x13,							// 1
        		Opcode.LD_HL_N16.encoding,		
        		0x09,
        		0x00,
        		Opcode.LD_HLR_C.encoding,		// BUS[HL] = C
        		Opcode.LD_A_N16R.encoding,		// A = BUS[0x0009]
        		0x09,
        		0x00,
        		0x00							// Target
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testHLRU_ADecrement() {
    	int[] program = {
    			Opcode.LD_A_N8.encoding,	// A = 0x13
    			0x13,
    			Opcode.LD_HL_N16.encoding,	// HL = 0x0009
    			0x09,
    			0x00,
    			Opcode.LD_HLRD_A.encoding,	// BUS[HL] = A ; HL -= 1;
    			Opcode.LD_A_N16R.encoding,	// A = BUS[0x0009]
    			0x09,
    			0x00,
    			0x00						// Target
    	};
    	
    	Cpu cpu = newCpu(program);
    	runCpu(cpu, program.length);
    	
    	assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    	assertEquals(0x08, cpu._testGetPcSpAFBCDEHL()[8]);
    	assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[9]);
    }
    
    @Test
    void testHLRU_AIncrement() {
    	int[] program = {
    			Opcode.LD_A_N8.encoding,	// A = 0x13
    			0x13,
    			Opcode.LD_HL_N16.encoding,	// HL = 0x0009
    			0x09,
    			0x00,
    			Opcode.LD_HLRI_A.encoding,	// BUS[HL] = A ; HL += 1;
    			Opcode.LD_A_N16R.encoding,	// A = BUS[0x0009]
    			0x09,
    			0x00,
    			0x00						// Target
    	};
    	
    	Cpu cpu = newCpu(program);
    	runCpu(cpu, program.length);
    	
    	assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    	assertEquals(0x0A, cpu._testGetPcSpAFBCDEHL()[8]);
    	assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[9]);
    }
    
    @Test
    void testLD_N8R_A() {
    	int[] program = new int[0xFFFF];
    	program[0] = Opcode.LD_A_N8.encoding;
    	program[1] = 0x13;
    	program[2] = Opcode.LD_N8R_A.encoding;
    	program[3] = 0x04;
    	program[4] = Opcode.LD_A_N8.encoding;
    	program[5] = 0x01;
    	program[6] = Opcode.LD_A_N16R.encoding;
    	program[7] = 0x04; 
    	program[8] = 0xFF;
    	
    	Cpu cpu = newCpu(program);
    	runCpu(cpu, 10);
    	
    	assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
	}
    
    @Test
    void testLD_CR_A() {
        int[] program = new int[0xFFFF];
        program[0] = Opcode.LD_A_N8.encoding;
        program[1] = 0x13;
        program[2] = Opcode.LD_C_N8.encoding;
        program[3] = 0x04;
        program[4] = Opcode.LD_CR_A.encoding;
        program[5] = Opcode.LD_A_N8.encoding;
        program[6] = 0x01;
        program[7] = Opcode.LD_A_N16R.encoding;
        program[8] = 0x04; 
        program[9] = 0xFF;
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, 10);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_N16R_A() {
        int[] program = new int[0xFFFF];
        program[0] = Opcode.LD_A_N8.encoding;
        program[1] = 0x13;
        program[2] = Opcode.LD_N16R_A.encoding;
        program[3] = 0x04;
        program[4] = 0xFF;
        program[5] = Opcode.LD_A_N8.encoding;
        program[6] = 0x01;
        program[7] = Opcode.LD_A_N16R.encoding;
        program[8] = 0x04; 
        program[9] = 0xFF;
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, 10);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_BCR_A() {
        int[] program = new int[0xFFFF];
        program[0] = Opcode.LD_A_N8.encoding;
        program[1] = 0x13;
        program[2] = Opcode.LD_BC_N16.encoding;
        program[3] = 0x04;
        program[4] = 0xFF;
        program[5] = Opcode.LD_BCR_A.encoding;
        program[6] = Opcode.LD_A_N8.encoding;
        program[7] = 0x01;
        program[8] = Opcode.LD_A_N16R.encoding;
        program[9] = 0x04; 
        program[10] = 0xFF;
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, 11);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_DER_A() {
        int[] program = new int[0xFFFF];
        program[0] = Opcode.LD_A_N8.encoding;
        program[1] = 0x13;
        program[2] = Opcode.LD_DE_N16.encoding;
        program[3] = 0x04;
        program[4] = 0xFF;
        program[5] = Opcode.LD_DER_A.encoding;
        program[6] = Opcode.LD_A_N8.encoding;
        program[7] = 0x01;
        program[8] = Opcode.LD_A_N16R.encoding;
        program[9] = 0x04; 
        program[10] = 0xFF;
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, 11);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_HLR_N8() {
        int[] program = {
                Opcode.LD_HL_N16.encoding,  // HL = 0x0005
                0x08,
                0x00,
                Opcode.LD_HLR_N8.encoding,  // BUS[HL] = 0x13
                0x13,                       
                Opcode.LD_A_N16R.encoding,  // A = BUS[0x0005]
                0x08,
                0x00,
                0x01                        // Target
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_N16R_SP() {
        int[] program = {
                Opcode.LD_SP_N16.encoding,  // SP = 0x1413
                0x13,
                0x14,
                Opcode.LD_N16R_SP.encoding, // BUS[0x000D] = SP
                0x0D,
                0x00,
                Opcode.LD_A_N16R.encoding,  // A = BUS[0x000D]
                0x0D,
                0x00,
                Opcode.LD_B_A.encoding,     // B = A
                Opcode.LD_A_N16R.encoding,  // A = BUS[0x000E]
                0x0E,
                0x00,
                0x01,                       // Target 1
                0x02                        // Target 2
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x14, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[4]);
    }
    
    @Test
    void testPUSH_R16() {
        int[] program = {
                Opcode.LD_SP_N16.encoding,
                0x0C,
                0x00,
                Opcode.LD_DE_N16.encoding,
                0x13,
                0x14,
                Opcode.PUSH_DE.encoding,
                Opcode.LD_A_N16R.encoding,
                0x0A,
                0x00,
                0x00
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x0A, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[2]);
    }
    
    @Test
    void testLD_R8_R8() {
        int[] program = {
                Opcode.LD_B_N8.encoding,
                0x13,
                Opcode.LD_L_B.encoding
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x13, cpu._testGetPcSpAFBCDEHL()[9]);
    }
    
    
    @Test
    void testLD_SP_HL() {
        int[] program = {
                Opcode.LD_HL_N16.encoding,
                0x13,
                0x14,
                Opcode.LD_SP_HL.encoding
        };
        
        Cpu cpu = newCpu(program);
        runCpu(cpu, program.length);
        
        assertEquals(0x1413, cpu._testGetPcSpAFBCDEHL()[1]);
    }
}

















