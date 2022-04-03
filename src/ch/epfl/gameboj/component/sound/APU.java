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
	private final GBSpeaker speaker;

	public enum Reg implements Register {
		NR10, NR11, NR12, NR13, NR14,
		xx20, NR21, NR22, NR23, NR24,
		NR30, NR31, NR32, NR33, NR34,
		xx40, NR41, NR42, NR43, NR44,
		NR50, NR51, NR52;

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

		this.speaker = new GBSpeaker();

		this.pulseA = new PulseB(rf);
	}

	public void start() {
		speaker.start();
	}

	public void stop() {
		speaker.stop();
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
		int nextSample = pulseA.getAsInt();
		Preconditions.checkBits8(nextSample);
		speaker.play(nextSample, nextSample);
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
//		System.out.printf("Read to %x%n", address);
		// TODO: implement masking

		if (AddressMap.REGS_SOUND_START <= address && address < AddressMap.REGS_SOUND_END) {
			return rf.get(Reg.addressToReg(address));
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
