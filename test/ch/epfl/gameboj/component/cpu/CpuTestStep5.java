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
        
        ProgramBuilder pb = new ProgramBuilder(program, 500);
        pb.run();
        
        assertEquals(8, pb.getResult()[0]);
        assertEquals(89, pb.getResult()[2]);
    }

}
