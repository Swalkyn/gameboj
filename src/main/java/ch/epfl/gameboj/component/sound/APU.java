package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.gui.GBSpeaker;

public class APU implements Component, Clocked {
	private final RegisterFile<Reg> rf;
	private final Ram waveRam;
	private final FrameSequencer frameSequencer;
	private final GBSpeaker speaker;

	private boolean powered;

	public enum Reg implements Register {
		NR10(0x80), NR11(0x3F), NR12(0x00), NR13(0xFF), NR14(0xBF),
		xx20(0xFF), NR21(0x3F), NR22(0x00), NR23(0xFF), NR24(0xBF),
		NR30(0x7F), NR31(0xFF), NR32(0x9F), NR33(0xFF), NR34(0xBF),
		xx40(0xFF), NR41(0xFF), NR42(0x00), NR43(0x00), NR44(0xBF),
		NR50(0x00), NR51(0x00), NR52(0x70);

		private final int mask;

		Reg(int mask) {
			this.mask = mask;
		}

		public int getMask() {
			return mask;
		}

		public static final Reg[] values = Reg.values();
		public static Reg addressToReg(int address) {
			return values[address - AddressMap.REGS_SOUND_START];
		}
	}

	private final PulseA pulseA;
	private final PulseB pulseB;
	private final Wave wave;
	private final Noise noise;

	public APU() {
		this.rf = new RegisterFile<>(Reg.values());
		this.waveRam = new Ram(AddressMap.WAVE_RAM_SIZE);
		this.frameSequencer = new FrameSequencer();

		this.speaker = new GBSpeaker();

		this.pulseA = new PulseA(frameSequencer, rf);
		this.pulseB = new PulseB(frameSequencer, rf);
		this.wave = new Wave(rf, waveRam, frameSequencer);
		this.noise = new Noise(rf, frameSequencer);

		this.powered = true;
	}

	public void start() {
		speaker.start();
	}

	public void stop() {
		speaker.stop();
	}

	private int readReg(int address) {
		Reg r = Reg.addressToReg(address);
		int value = rf.get(r) | r.getMask();
		if (r == Reg.NR52) {
			value = Bits.set(value, 0, pulseA.isEnabled());
			value = Bits.set(value, 1, pulseB.isEnabled());
			value = Bits.set(value, 2, wave.isEnabled());
			value = Bits.set(value, 3, noise.isEnabled());
		}
		return value;
	}

	private void writeToReg(int address, int data) {
		Reg r = Reg.addressToReg(address);
		if (powered || r == Reg.NR52) {
			rf.set(r, data);
		}
		switch (r) {
			case NR10:
				break;
			case NR11:
				pulseA.lengthCounter.loadCounter(Bits.clip(6, data));
				break;
			case NR12:
				if (Bits.extract(data, 3, 5) == 0) {
					pulseA.disable();
				}
				break;
			case NR13:
				break;
			case NR14:
				pulseA.lengthCounter.setEnabled(Bits.test(data, 6));
				if (Bits.test(data, 7)) {
					pulseA.trigger();
				}
				break;
			case xx20:
				break;
			case NR21:
				pulseB.lengthCounter.loadCounter(Bits.clip(6, data));
				break;
			case NR22:
				if (Bits.extract(data, 3, 5) == 0) {
					pulseB.disable();
				}
				break;
			case NR23:
				break;
			case NR24:
				pulseB.lengthCounter.setEnabled(Bits.test(data, 6));
				if (Bits.test(data, 7)) {
					pulseB.trigger();
				}
				break;
			case NR30:
				if (!Bits.test(data, 7)) {
					wave.disable();
				}
				break;
			case NR31:
				wave.lengthCounter.loadCounter(data);
				break;
			case NR32:
				wave.waveVolume.configure(Bits.extract(data, 5, 2));
				break;
			case NR33:
				break;
			case NR34:
				wave.lengthCounter.setEnabled(Bits.test(data, 6));
				if (Bits.test(data, 7)) {
					wave.trigger();
				}
				break;
			case xx40:
				break;
			case NR41:
				noise.lengthCounter.loadCounter(Bits.clip(6, data));
				break;
			case NR42:
				if (Bits.extract(data, 3, 5) == 0) {
					noise.disable();
				}
				noise.envelope.configure(Bits.test(data, 3), Bits.clip(3, data), Bits.extract(data, 4, 4));
				break;
			case NR43:
				break;
			case NR44:
				noise.lengthCounter.setEnabled(Bits.test(data, 6));
				if (Bits.test(data, 7)) {
					noise.trigger();
				}
				break;
			case NR50:
				break;
			case NR51:
				break;
			case NR52:
				if (Bits.test(data, 7)) {
					powered = true;
				} else {
					// Power on
					powered = false;
					// Set all regsiters to 0
					for (Reg reg: Reg.values()) {
						rf.set(reg, 0);
					}
					frameSequencer.reset();
				}
				break;
		}
	}

	@Override
	public void cycle(long cycle) {
		// Mix channels output
		frameSequencer.cycle(cycle);
		int pulseASample = pulseA.getAsInt();
		int pulseBSample = pulseB.getAsInt();
		int waveSample = wave.getAsInt();
		int noiseSample = noise.getAsInt();
		int nextSample = (pulseASample + pulseBSample + waveSample + noiseSample) / 4;
		speaker.play(nextSample, nextSample);
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		int value;

		if (AddressMap.REGS_SOUND_START <= address && address < AddressMap.REGS_SOUND_END) {
			// APU Registers
			value = readReg(address);
		} else if (AddressMap.REGS_SOUND_END <= address && address < AddressMap.WAVE_RAM_START) {
			// Unusable registers
			value = 0xFF;
		} else if (AddressMap.WAVE_RAM_START <= address && address < AddressMap.WAVE_RAM_END) {
			// Wave ram
			value = waveRam.read(address - AddressMap.WAVE_RAM_START);
		} else {
			value = Component.NO_DATA;
		}

		return value;
	}

	@Override
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);

		if (AddressMap.REGS_SOUND_START <= address && address < AddressMap.REGS_SOUND_END) {
			writeToReg(address, data);
		} else if (AddressMap.WAVE_RAM_START <= address && address < AddressMap.WAVE_RAM_END) {
			waveRam.write(address - AddressMap.WAVE_RAM_START, data);
		}
	}

}
