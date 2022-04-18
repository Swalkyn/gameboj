package ch.epfl.gameboj.component.sound;

public class Noise extends Channel {

	@Override
	public int getSample() {
		return 0;
	}

	@Override
	public void trigger() {

	}

	@Override
	public boolean dacEnabled() {
		return false;
	}

	@Override
	public void disable() {

	}
}
