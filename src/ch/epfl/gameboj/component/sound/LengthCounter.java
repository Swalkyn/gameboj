package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;


public class LengthCounter implements SoundUnit {
	private final Timer timer;
	private final int countMax;
	private boolean enabled;
	private int cycles;

	public LengthCounter(Timer timer, int countMax) {
		this.timer = timer;
		this.countMax = countMax;
		configure(false, 0);
	}

	public void configure(boolean enabled, int offset) {
		this.enabled = enabled;
		setCounter(offset);
	}

	private void setCounter(int offset) {
		Preconditions.checkArgument(offset < countMax);
		this.cycles = countMax - offset;
	}

	@Override
	public int applyAsInt(int i) {
		if (!enabled) {
			return i;
		} else if (cycles > 0) {
			if (timer.lengthCounterTick()) {
				cycles -= 1;
			}
			return i;
		} else {
			return 0;
		}
	}
}
