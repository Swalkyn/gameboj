package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class GBSaver {
    
    private Cartridge cartridge;
    private Optional<File> save;
    
    public GBSaver() {
        save = Optional.empty();
    }
    
    public void save() {
        try {
            if(save.isPresent()) {
                cartridge.save(save.get());                
            }
        } catch (IOException e) {
            System.err.println("Error happened during save");
        }
    }
    
    public void load(Cartridge cartridge, Optional<File> save) {
        this.cartridge = Objects.requireNonNull(cartridge);
        this.save = save;
            
        if (save.isPresent()) {
            try {
                cartridge.load(save.get());
            } catch (FileNotFoundException e) {
                System.out.println("No save found");
            } catch (IOException e) {
                System.err.println("Error during loading of save");
                System.exit(1);
            }
        }
    }
}
