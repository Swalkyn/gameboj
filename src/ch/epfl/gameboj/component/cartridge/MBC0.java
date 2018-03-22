package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a bank of memory
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class MBC0 implements Component {
    public static final int ROM_SIZE = 32768;
    private final Rom rom;
    
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == ROM_SIZE);
        
        this.rom = rom;
    }
    
    
    @Override
    public int read(int index) {
        // TODO : implement method correctly
        return rom.read(index);
    }

    @Override
    public void write(int address, int data) {
        // Does nothing, ROM can't be written to
    }

}
