package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;
import ch.epfl.gameboj.component.memory.Ram;

public class Wave extends Channel {
	public static class WaveGen implements SoundUnit {
		private static final int SAMPLES = 32;
		private static final int FREQ_START = 2048;
		private final Ram ram;
		private int cycle;
		private int timer_period;
		private int sample_index;

		public WaveGen(Ram ram) {
			this.ram = ram;
			configure(0);
		}

		public void configure(int freq) {
			this.timer_period = FREQ_START - freq;
			this.cycle = 0;
			this.sample_index = 0;
		}

		private int getSample(int index) {
			int start = index % 2 == 0 ? 4 : 0;
			return Bits.extract(ram.read(index / 2), start, 4);
		}

		@Override
		public int applyAsInt(int i) {
			cycle += 2;
			sample_index = (cycle / timer_period) % SAMPLES;
			return getSample(sample_index);
		}
	}

	public static class WaveVolume implements SoundUnit {
		private int volume;

		public WaveVolume() {
			configure(0);
		}

		public void configure(int volume) {
			this.volume = volume;
		}

		@Override
		public int applyAsInt(int i) {
			switch (volume) {
				case 0: return 0;
				case 1: return i << 4;
				case 2: return i;
				case 3: return i >> 2;
				default:
					throw new IllegalArgumentException("Volume must be between 0 and 3");
			}
		}
	}

	public final WaveGen wave;
	public final LengthCounter lengthCounter;
	public final WaveVolume waveVolume;
	private final RegisterFile<APU.Reg> rf;


	public Wave(RegisterFile<APU.Reg> rf, Ram ram, FrameSequencer frameSequencer) {
		this.wave = new WaveGen(ram);
		this.lengthCounter = new LengthCounter(this, frameSequencer, 256);
		this.waveVolume = new WaveVolume();
		this.rf = rf;
	}

	@Override
	public boolean dacEnabled() {
		return rf.testBit(APU.Reg.NR30, SBit.B7);
	}

	@Override
	public void trigger() {
		// Channel is enabled, if the DAC is enabled
		if (dacEnabled()) {
			enable();
		}
		// If length counter is zero, it is set to 64 (256 for wave channel).
		if (lengthCounter.isZero()) {
			lengthCounter.loadCounter(0);
		}
		// Frequency timer is reloaded
		int freq = Bits.make16(Bits.clip(3, rf.get(APU.Reg.NR34)), rf.get(APU.Reg.NR33));
		wave.configure(freq);
		// Channel volume is reloaded
		waveVolume.configure(Bits.extract(rf.get(APU.Reg.NR32), 5, 2));
	}

	@Override
	public int getSample() {
		return wave.andThen(lengthCounter).andThen(waveVolume).applyAsInt(0);
	}
}
