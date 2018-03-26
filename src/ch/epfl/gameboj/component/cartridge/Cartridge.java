package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * Represents a cartridge that can be inserted in the gameboy
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class Cartridge implements Component {
    
    private Component memoryBank;
    public static final int MB_TYPE_ADDRESS = 0x147;
    public static final int MB_TYPE = 0;
    
    private Cartridge(Component memoryBank) {
        this.memoryBank = memoryBank;
    }
    
    public static Cartridge ofFile(File romFile) throws IOException {
        try(InputStream stream = new FileInputStream(romFile)) {
            
            byte[] data = new byte[(int) romFile.length()];
            
            int streamSize = stream.read(data);
            
            if (streamSize != data.length) {
                throw new IOException();
            }
            
            if (data[MB_TYPE_ADDRESS] != MB_TYPE) {
                throw new IllegalArgumentException();
            }
            
            Rom rom = new Rom(data);
            return new Cartridge(new MBC0(rom));
        }
    }        

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        return memoryBank.read(address); 
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        memoryBank.write(address, data);
    }
}
