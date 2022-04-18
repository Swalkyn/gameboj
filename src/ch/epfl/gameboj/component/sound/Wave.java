package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;
import ch.epfl.gameboj.component.memory.Ram;

public class Wave extends Channel {
	private static class WaveGen implements SoundUnit {
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
//			int start = index % 2 == 0 ? 4 : 0;
//			return Bits.extract(ram.read(index / 2), start, 4);
			return 0;
		}

		@Override
		public int applyAsInt(int i) {
			cycle += 2;
			sample_index = ((cycle / timer_period) + 1) % SAMPLES;
			return getSample(sample_index);
		}
	}

	private static class WaveVolume implements SoundUnit {
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

	private final WaveGen wave;
	private final LengthCounter lengthCounter;
	private final WaveVolume waveVolume;
	private final RegisterFile<APU.Reg> rf;


	public Wave(RegisterFile<APU.Reg> rf, Ram ram, Timer timer) {
		this.wave = new WaveGen(ram);
		this.lengthCounter = new LengthCounter(timer, 256);
		this.waveVolume = new WaveVolume();
		this.rf = rf;
	}

	@Override
	public boolean dacEnabled() {
		return rf.testBit(APU.Reg.NR30, SBit.B7);
	}

	@Override
	public void trigger() {
		enable();
		int freq = Bits.make16(Bits.clip(3, rf.get(APU.Reg.NR34)), rf.get(APU.Reg.NR33));
		wave.configure(freq);
		lengthCounter.configure(rf.testBit(APU.Reg.NR34, SBit.B6), rf.get(APU.Reg.NR21));
		waveVolume.configure(Bits.extract(rf.get(APU.Reg.NR32), 5, 2));
	}

	@Override
	public int getSample() {
		return wave.andThen(lengthCounter).andThen(waveVolume).applyAsInt(0);
	}
}
