package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;

import java.util.function.IntSupplier;

public abstract class Channel implements IntSupplier {
	public abstract void enable();
}
