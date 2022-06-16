package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.Preconditions;


public class LengthCounter implements SoundUnit {
	private final Channel channel;
	private final FrameSequencer frameSequencer;
	private final int countMax;
	private boolean enabled;
	private int count;

	public LengthCounter(Channel channel, FrameSequencer frameSequencer, int countMax) {
		this.channel = channel;
		this.frameSequencer = frameSequencer;
		this.countMax = countMax;
		configure(false, 0);
	}

	public void configure(boolean enabled, int offset) {
		this.enabled = enabled;
		loadCounter(offset);
	}

	public void loadCounter(int offset) {
		Preconditions.checkArgument(offset < countMax);
		this.count = countMax - offset;
	}

	@Override
	public int applyAsInt(int i) {
		if (!enabled) {
			return i;
		} else if (count > 0) {
			if (frameSequencer.lengthCounterTick()) {
				count -= 1;
				if (count == 0) {
					channel.disable();
				}
			}
			return i;
		} else {
			return 0;
		}
	}
}
