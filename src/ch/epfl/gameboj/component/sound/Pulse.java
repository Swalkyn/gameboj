package ch.epfl.gameboj.component.sound;

public class Pulse implements SoundUnit {
	private static final int TIMER_CYCLES_PER_WAVEFORM = 8;
	private static final int FREQ_START = 2048;

	private int duty;
	private int timer_period;
	private long cycle;

	public Pulse() {
		this.configure(0, 0);
	}

	public void configure(int duty, int freq) {
		this.duty = duty;
		this.timer_period = FREQ_START - freq;
		this.cycle = 0;
	}

	@Override
	public int applyAsInt(int i) {
		int timerCycle = (int) ((cycle / timer_period) % TIMER_CYCLES_PER_WAVEFORM);
		cycle += 1;

		boolean output;
		switch (duty) {
			case 0:
				output = timerCycle == 0;
				break;
			case 1:
				output = timerCycle == 0 || timerCycle == 7;
				break;
			case 2:
				output = timerCycle == 0 || timerCycle > 4;
				break;
			case 3:
				output = !(timerCycle == 0 || timerCycle == 7);
				break;
			default:
				throw new IllegalArgumentException("Incorrect duty index");
		}
		return output ? 0xFF : 0x00;
	}

}
