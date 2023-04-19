package ch.epfl.gameboj.component.sound;

public class Envelope implements SoundUnit {
	private static final int MIN_VOLUME = 0x0;
	private static final int MAX_VOLUME = 0xF;
	private final FrameSequencer frameSequencer;
	private int volume;
	private int period;
	private int cycles;
	private boolean addMode;

	public Envelope(FrameSequencer frameSequencer) {
		this.frameSequencer = frameSequencer;
		configure(false, 1, 0);
	}

	public void configure(boolean addMode, int period, int startVolume) {
		this.volume = startVolume;
		this.period = period;
		this.addMode = addMode;
		this.cycles = period;
	}

	@Override
	public int applyAsInt(int i) {
		if (period != 0) {
			if (frameSequencer.envelopeTick()) {
				cycles -= 1;
			}
			if (cycles == 0) {
				cycles = period;
				if (addMode && volume < MAX_VOLUME) {
					volume += 1;
				} else if (!addMode && volume > MIN_VOLUME) {
					volume -= 1;
				}
			}
		}
		return i / MAX_VOLUME * volume;
	}
}
