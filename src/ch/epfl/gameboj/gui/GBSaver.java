package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class GBSaver {
    
    private Cartridge cartridge;
    private File save;
    
    public GBSaver() {}
    
    public void save() {
        try {
            if(save != null && cartridge.canBeSaved()) {
                cartridge.save(save);                
            }
        } catch (IOException e) {
            System.err.println("Error happened during save");
        }
    }
    
    public void load(Cartridge cartridge, File save) {
        this.cartridge = Objects.requireNonNull(cartridge);
        this.save = save;
        
        if (save != null && cartridge.canBeSaved()) {
            try {
                cartridge.load(save);
            } catch (FileNotFoundException e) {
                System.out.println("No save found");
            } catch (IOException e) {
                System.err.println("Error during loading of save");
                System.exit(1);
            }
        }
    }
}
