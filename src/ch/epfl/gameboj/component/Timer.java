package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
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
    private int regDiv;
    private int regTima;
    private int regTma;
    private int regTac;
    
    public Timer(Cpu cpu) {
        cpu = Objects.requireNonNull(cpu);
        regDiv = 0;
        regTima = 0;
    }
    
    /**
     * Cycles the timer, incrementing DIV and TIMA timers
     * @param cycle: the number of cycles since start of clock
     */
    @Override
    public void cycle(long cycle) {
        regDiv = Bits.clip(16, regDiv + 4);
        incIfChange(state());
    }

    /**
     * Reads values at specified address, returns NO_DATA if address not mapped
     * @param address: 16-bits
     * @throws IllegalArgumentException if address not 16 bits
     * @return the data at address
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        switch (address) {
            case AddressMap.REG_DIV:
                return Bits.extract(regDiv, 8, 8);
            case AddressMap.REG_TIMA:
                return regTima;
            case AddressMap.REG_TMA:
                return regTma;
            case AddressMap.REG_TAC:
                return regTac;
                
            default:
                return Component.NO_DATA;
        }
    }

    /**
     * Stores given data at specified address, does nothing if address not mapped
     * Also increments TIMA if DIV or TAC change
     * @param address : 16-bit address
     * @param data : 8-bit value to be stored
     * @throws IllegalArgumentException if address is not 16 bits or data is not 8 bits
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        switch (address) {
            case AddressMap.REG_DIV: {
                // TODO : quel compteur ?
                boolean s0 = state();
                regDiv = Bits.make16(data, Bits.clip(8, regDiv));
                incIfChange(s0);
            } break;
            case AddressMap.REG_TIMA: {
                regTima = data;
            } break; 
            case AddressMap.REG_TMA: {
                regTma = data;
            } break;
            case AddressMap.REG_TAC: {
                boolean s0 = state();
                regTac = data;
                incIfChange(s0);
            } break;
        } 
    }
    
    private boolean state() {
        return Bits.test(regTac, 2) && Bits.test(regDiv, extractTimerIndex());
    }
    
    private void incIfChange(boolean previousState) {
        if (previousState && !state()) {
            
            if (regTima == 0xFF) {
                cpu.requestInterrupt(Cpu.Interrupt.TIMER);
                regTima = regTma;
            } else {
                regTima++;
            }
        }
    }

    private int extractTimerIndex() {
        int bit = Bits.clip(2, regTac);
        
        switch (bit) {
            case 0b00: return 9;
            case 0b01: return 3;
            case 0b10: return 5;
            case 0b11: return 7;
                
            default: throw new IllegalArgumentException();
        }
    }
    
}
