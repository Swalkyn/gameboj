package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;

import java.util.function.IntSupplier;

public abstract class Channel implements IntSupplier {
	private boolean enabled = false;

	public abstract int getSample();
	public abstract void trigger();
	public abstract boolean dacEnabled();
	public void disable() {
		enabled = false;
	}
	public void enable() { enabled = true; }
	public boolean isEnabled() {
		return enabled && dacEnabled();
	}

	@Override
	public int getAsInt() {
		int sample = getSample();
		return enabled ? sample : 0x00;
	}
}
