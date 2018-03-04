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

}
