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
    
    private MBC0 memoryBank;
    
    private Cartridge(byte[] data) {
        memoryBank = new MBC0(new Rom(data));
    }
    
    public static Cartridge ofFile(File romFile) throws IOException {
        try(InputStream file = new FileInputStream(romFile)) {
            
            byte[] data = new byte[(int) romFile.length()];
            
            int streamSize = file.read(data);
            
            if (streamSize != data.length) {
                throw new IOException();
            }
            
            if (data[0x147] != 0) {
                throw new IllegalArgumentException();
            }
                        
            return new Cartridge(data);
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
