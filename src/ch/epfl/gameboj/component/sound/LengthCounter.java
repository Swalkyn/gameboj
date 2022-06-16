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
		this.loadCounter(0);
		this.setEnabled(false);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void loadCounter(int offset) {
		Preconditions.checkArgument(offset < countMax);
		this.count = countMax - offset;
	}

	public boolean isZero() {
		return count == 0;
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
