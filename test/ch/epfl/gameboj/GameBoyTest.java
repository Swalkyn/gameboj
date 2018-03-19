package ch.epfl.gameboj;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;


public final class GameBoyTest {
    @Test
    void workRamWorksForValuesInRange() {
        GameBoy gb = new GameBoy(null);
        
        Random rng = newRandom();
        for(int i = 0; i < RANDOM_ITERATIONS; i++) {
            int address = AddressMap.WORK_RAM_START + rng.nextInt(AddressMap.WORK_RAM_SIZE);
            
            assertEquals(0, gb.bus().read(address));
        }        
    }
    
    @Test
    void echoRamWorksForValuesInRange() {
        GameBoy gb = new GameBoy(null);
        
        Random rng = newRandom();
        for(int i = 0; i < RANDOM_ITERATIONS; i++) {
            int address = AddressMap.ECHO_RAM_START + rng.nextInt(AddressMap.ECHO_RAM_SIZE);
            
            assertEquals(0, gb.bus().read(address));
        }        
    }
    
    @Test
    void workRamWorksForStartAddress() {
        GameBoy gb = new GameBoy(null);
        
        assertEquals(0, gb.bus().read(AddressMap.WORK_RAM_START));
    }
    
    @Test
    void echoRamWorksForStartAddress() {
        GameBoy gb = new GameBoy(null);
        
        assertEquals(0, gb.bus().read(AddressMap.ECHO_RAM_START));
    }
    
    @Test
    void workRamWorksForEndAddress() {
        GameBoy gb = new GameBoy(null);
        
        assertEquals(0, gb.bus().read(AddressMap.WORK_RAM_END - 1));
    }
    
    @Test
    void echoRamWorksForEndAddress() {
        GameBoy gb = new GameBoy(null);
        
        assertEquals(0, gb.bus().read(AddressMap.ECHO_RAM_END - 1));
    }
    
    @Test
    void ramReturnsNODATAonOutofRangeValues() {
        GameBoy gb = new GameBoy(null);
        
        assertEquals(0xFF, gb.bus().read(AddressMap.WORK_RAM_START - 1));
        assertEquals(0xFF, gb.bus().read(AddressMap.ECHO_RAM_END));
    }
    
    @Test
    void runUntilFailsForInvalidValues() {
        GameBoy gb = new GameBoy(null);
        assertThrows(IllegalArgumentException.class, () -> gb.runUntil(-1));
    }
}
