package ch.epfl.gameboj.component.sound;

public class SweepPulse implements SoundUnit {
	private final Pulse pulse;
	private final FrameSequencer frameSequencer;
	private int period;
	private boolean negate;
	private int shift;
	private int freq;
	private int duty;
	private boolean enabled;
	private int cycle;

	public SweepPulse(FrameSequencer frameSequencer) {
		this.pulse = new Pulse();
		this.frameSequencer = frameSequencer;
		configure(0, false, 0, 0, 0);
	}

	public void configure(int period, boolean negate, int shift, int duty, int freq) {
		pulse.configure(duty, freq);
		this.freq = freq;
		this.duty = duty;
		this.period = period;
		this.negate = negate;
		this.enabled = period != 0 || shift != 0;
		this.shift = shift;
		this.cycle = period;

	}

	private int calculateNewFreq() {
		int sign = negate ? -1 : 1;
		return freq + sign * (freq >> shift);
	}

	private boolean overflowCheck(int freq) {
		return freq < 2048;
	}

	@Override
	public int applyAsInt(int i) {
		if (enabled && period != 0) {
			if (frameSequencer.sweepTick()) {
				cycle -= 1;
			}
			if (cycle == 0 && shift != 0) {
				int newFreq = calculateNewFreq();
				if (overflowCheck(newFreq)) {
					freq = newFreq;
					pulse.configure(duty, freq);
				}
			}
		}
		return overflowCheck(calculateNewFreq()) ? pulse.applyAsInt(i) : pulse.applyAsInt(i);
	}
}
