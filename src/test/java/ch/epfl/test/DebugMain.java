package ch.epfl.test;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class DebugMain {
    
    private static final String PATH = "test/testBlargg/";
    private static final String[] TESTS = {
            "01-special.gb",
            "02-interrupts.gb",
            "03-op sp,hl.gb",
            "04-op r,imm.gb",
            "05-op rp.gb",
            "06-ld r,r.gb",
            "07-jr,jp,call,ret,rst.gb",
            "08-misc instrs.gb",
            "09-op r,r.gb",
            "10-bit ops.gb",
            "11-op a,(hl).gb",
            "instr_timing.gb"
    };
    private static final int NUMBER_OF_CYCLES = 30000000;
    
    public static void main(String[] args) throws IOException {
        for (String s : TESTS) {
            System.out.println("==============================");
            runTest(PATH + s, NUMBER_OF_CYCLES);
        }
    }
    
    private static void runTest(String testName, long cycles) throws IOException {
        File romFile = new File(testName);

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        Component printer = new DebugPrintComponent();
        printer.attachTo(gb.bus());
        
        while (gb.cycles() < cycles) {
            long nextCycles = Math.min(gb.cycles() + 17556, cycles);
            gb.runUntil(nextCycles);
            gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
        }
    }
}
