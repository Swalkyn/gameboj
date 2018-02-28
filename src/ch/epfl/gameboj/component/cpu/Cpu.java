package ch.epfl.gameboj.component.cpu;


import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;

public final class Cpu implements Component, Clocked {
    
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    
    private enum Reg16 {}
    
    
    @Override
    public void cycle(long cycle) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int read(int address) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void write(int address, int data) {
        // TODO Auto-generated method stub
        
    }
    
    
}
