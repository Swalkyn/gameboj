package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

/**
* The Gameboy's timer component
*
* @author Sylvain Kuchen (282380)
* @author Luca Bataillard (282152)
*/
public final class Timer implements Component, Clocked {
    
    private Cpu cpu;
    private RegisterFile<RegTimer> rf;
    
    private final int TIMA_MAX_VALUE = 0xFF;
    
    private enum RegTimer implements Register {
        DIV_MSB, DIV_LSB, TIMA, TMA, TAC
    }
    
    public Timer(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
        this.rf = new RegisterFile<>(RegTimer.values());
    }
    
    @Override
    public void cycle(long cycle) {
        boolean s0 = state();
        writeDiv(Bits.clip(16, readDiv() + 4));
        incIfChange(s0);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        switch (address) {
            case AddressMap.REG_DIV:  return rf.get(RegTimer.DIV_MSB);
            case AddressMap.REG_TIMA: return rf.get(RegTimer.TIMA);
            case AddressMap.REG_TMA:  return rf.get(RegTimer.TMA);
            case AddressMap.REG_TAC:  return rf.get(RegTimer.TAC);
        
            default: return Component.NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        boolean s0 = state();
        
        switch (address) {
            case AddressMap.REG_DIV:
                writeDiv(0);
                incIfChange(s0);
                break;
            case AddressMap.REG_TIMA: 
                rf.set(RegTimer.TIMA, data);
                break;
            case AddressMap.REG_TMA:  
                rf.set(RegTimer.TMA, data);                
                break;                
            case AddressMap.REG_TAC:
                rf.set(RegTimer.TAC, data);
                incIfChange(s0);
                break;
        }
    }
    
    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            if (rf.get(RegTimer.TIMA) == TIMA_MAX_VALUE) {
                cpu.requestInterrupt(Cpu.Interrupt.TIMER);
                rf.set(RegTimer.TIMA, rf.get(RegTimer.TMA));
            } else {
                rf.set(RegTimer.TIMA, Bits.clip(8, rf.get(RegTimer.TIMA) + 1));
            }
        }
    }
    
    private boolean state() {
        return Bits.test(rf.get(RegTimer.TAC), 2) && Bits.test(readDiv(), extractTimerIndex());
    }
    
    private int extractTimerIndex() {
        int bit = Bits.clip(2, rf.get(RegTimer.TAC));
        
        switch(bit) {
            case 0b00: return 9;
            case 0b01: return 3;
            case 0b10: return 5;
            case 0b11: return 7;
            
            default: throw new IllegalArgumentException();
        }
    }
    
    private void writeDiv(int data) {
        Preconditions.checkBits16(data);
        
        rf.set(RegTimer.DIV_MSB, Bits.extract(data, 8, 8));
        rf.set(RegTimer.DIV_LSB, Bits.clip(8, data));
    }
    
    private int readDiv() {
        return Bits.make16(rf.get(RegTimer.DIV_MSB), rf.get(RegTimer.DIV_LSB));
    }

    
    
}
