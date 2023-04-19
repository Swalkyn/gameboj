package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 *  Boot Rom Controller 
 * 
 *  @author Sylvain Kuchen (282380)
 *  @author Luca Bataillard (282152)
 */
public final class BootRomController implements Component {
    
    private final Cartridge cartridge;
    private final Rom bootRom;
    private boolean bootRomEnabled;
    
    /**
     * Creates a new boot rom controller with a cartridge attached to it
     * @param cartridge : the cartridge to be attached
     */
    public BootRomController(Cartridge cartridge) {
        this.cartridge = Objects.requireNonNull(cartridge);;
        bootRom = new Rom(BootRom.DATA);
        bootRomEnabled = true;
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (bootRomEnabled && (address >= AddressMap.BOOT_ROM_START && address < AddressMap.BOOT_ROM_END)) {
            return bootRom.read(address - AddressMap.BOOT_ROM_START);
        }
        
        return cartridge.read(address);
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        
        if (address == AddressMap.REG_BOOT_ROM_DISABLE) {
            bootRomEnabled = false;
        }
        
        cartridge.write(address, data);
    }
}
