package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages the save files for saveable cartridges
 * 
 * @author Sylvain Kuchen (282380)
 * @author Luca Bataillard (282152)
 */
public final class GBSaver {
    
    private Cartridge cartridge;
    private Optional<File> save;
    
    /**
     * Creates a new GBSaver, initially with an empty save file
     */
    public GBSaver() {
        save = Optional.empty();
    }
    
    /**
     * Saves the current state of the cartridge ram to file
     */
    public void save() {
        try {
            if(save.isPresent()) {
                cartridge.save(save.get());                
            }
        } catch (IOException e) {
            System.err.println("Error happened during save");
            e.printStackTrace();
        }
    }
    
    /**
     * Loads save onto cartridge. If empty optional or file not found, does nothing.
     * Crashes if IOException occurs
     * @param cartridge : the gameboy cartridge
     * @param save : optional save, if empty, does nothing
     */
    public void load(Cartridge cartridge, Optional<File> save) {
        this.cartridge = Objects.requireNonNull(cartridge);
        this.save = save;
            
        if (save.isPresent()) {
            try {
                cartridge.load(save.get());
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                System.err.println("Error during loading of save");
                System.exit(1);
            }
        }
    }
}
