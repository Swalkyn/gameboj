package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.GameBoy;

public class Envelope implements SoundUnit {
	private static final int TIMER_PERIOD = (int) (GameBoy.CYCLES_PER_SECOND / 16);
	private static final int MIN_VOLUME = 0x0;
	private static final int MAX_VOLUME = 0xF;
	private int volume;
	private int period;
	private int cycles;
	private boolean addMode;

	public Envelope() {
		configure(false, 1, 0);
	}

	public void configure(boolean addMode, int period, int startVolume) {
		this.volume = startVolume;
		this.period = period;
		this.addMode = addMode;
		this.cycles = TIMER_PERIOD * period;
	}

	@Override
	public int applyAsInt(int i) {
		if (cycles > 0) {
			cycles -= 1;
		} else {
			cycles = TIMER_PERIOD * period;
			if (addMode && volume < MAX_VOLUME) {
				volume += 1;
			} else if (!addMode && volume > MIN_VOLUME) {
				volume -= 1;
			}
		}
		return i / MAX_VOLUME * volume;
	}
}
