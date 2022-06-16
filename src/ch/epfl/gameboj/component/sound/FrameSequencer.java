package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.GameBoy;

public class FrameSequencer {
	private static final int PERIOD = (int) GameBoy.CYCLES_PER_SECOND / 512;
	private static final int STEPS = 8;
	private int step;
	private int clock;

	public FrameSequencer() {
		this.step = 0;
		this.clock = 0;
	}

	public void cycle(long cycle) {
		clock = (clock + 1) % PERIOD;
		if (clock == 0) {
			step = (step + 1) % STEPS;
		}
	}

	public void reset() {
		// When powered on, the frame sequencer is reset so that the next step will be 0
		step = STEPS - 1;
	}

	public boolean lengthCounterTick() {
		return step % 2 == 0 && clock == 0;
	}

	public boolean sweepTick() {
		return (step + 2) % 4 == 0 && clock == 0;
	}

	public boolean envelopeTick() {
		return step == 7 && clock == 0;
	}
}
