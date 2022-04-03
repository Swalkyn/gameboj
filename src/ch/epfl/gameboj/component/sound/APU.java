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
	private static final int CHANNEL_REGS = CReg.values().length;
	private final RegisterFile<Reg> rf;
	private final Ram waveRam;
	private final Timer timer;
	private final GBSpeaker speaker;

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
		public static boolean isSpecial(Reg r) { return r.ordinal() >= 20; }
		public static Reg getRegister(Channel c, CReg r) {
			return values[c.ordinal() * CHANNEL_REGS + r.ordinal()];
		}
	}

	private enum CReg {
		Sweep, Length, Volume, Frequency, Control;

		public static final CReg[] values = CReg.values();
		public static CReg getChannelRegister(Reg r) {
			return values[r.ordinal() % CHANNEL_REGS];

		}
	}

	private enum Channel {
		PulseA, PulseB, Wave, Noise;

		public static final Channel[] values = Channel.values();
		public static Channel addressToChannel(int address) {
			return values[(address - AddressMap.REGS_SOUND_START) / CHANNEL_REGS];
		}
	}

	private final PulseB pulseA;

	public APU() {
		this.rf = new RegisterFile<>(Reg.values());
		this.waveRam = new Ram(AddressMap.WAVE_RAM_SIZE);
		this.timer = new Timer();

		this.speaker = new GBSpeaker();

		this.pulseA = new PulseB(timer, rf);
	}

	public void start() {
		speaker.start();
	}

	public void stop() {
		speaker.stop();
	}

	private int readReg(int address) {
		Reg r = Reg.addressToReg(address);
		return rf.get(r) | r.getMask();
	}

	private void writeToReg(int address, int data) {
		Reg r = Reg.addressToReg(address);
		if (!Reg.isSpecial(r)) {
			Channel c = Channel.addressToChannel(address);
			CReg cr = CReg.getChannelRegister(r);
			rf.set(r, data);

			if (c == Channel.PulseB && cr == CReg.Control && Bits.test(data, 7)) {
				pulseA.enable();
			}
		}
	}

	@Override
	public void cycle(long cycle) {
		// Mix channels output
		timer.cycle(cycle);
		int nextSample = pulseA.getAsInt();
		Preconditions.checkBits8(nextSample);
		speaker.play(nextSample, nextSample);
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);

		if (AddressMap.REGS_SOUND_START <= address && address < AddressMap.REGS_SOUND_END) {
			return readReg(address);
		} else if (AddressMap.WAVE_RAM_START <= address && address < AddressMap.WAVE_RAM_END) {
			return waveRam.read(address - AddressMap.WAVE_RAM_START);
		}

		return Component.NO_DATA;
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
