package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a memory bank of type 0
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class MBC0 implements Component {
    
    public static final int ROM_SIZE = 32768;
    private final Rom rom;
    
    /**
     * Creates a new MBC0
     * @param rom : non-null, of size 32768
     * @throws NullPointerException if rom is null;
     * @throws IllegalArgumentException if rom size is not 32768
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == ROM_SIZE);
        
        this.rom = rom;
    }
    
    /**
     * Reads the memory bank
     * Mapped addresses : 0 (incl) to 32768 (excl)
     * @param address: 16 bits
     * @throws IllegalArgumentException if address not 16 bits
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (address < ROM_SIZE) {
            return rom.read(address);
        } else {
            return Component.NO_DATA;
        }
        
    }

    /**
     * Does nothing for a memory bank since read-only
     */
    @Override
    public void write(int address, int data) {
        // Does nothing, ROM can't be written to
    }

}
