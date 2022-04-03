package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;

public class PulseB extends Channel {
	private static final Pulse pulse = new Pulse();
	private static final LengthCounter lengthCounter = new LengthCounter(64);
	private static final Envelope envelope = new Envelope();

	private final RegisterFile<APU.Reg> rf;

	public PulseB(RegisterFile<APU.Reg> rf) {
		this.rf = rf;
	}

	@Override
	public void enable() {
		// Channel is enabled (see length counter).
		// If length counter is zero, it is set to 64 (256 for wave channel).
		lengthCounter.configure(rf.testBit(APU.Reg.NR24, SBit.B6), Bits.clip(6, rf.get(APU.Reg.NR21)));
		// Frequency timer is reloaded with period.
		int freq = Bits.make16(Bits.clip(3, rf.get(APU.Reg.NR24)), rf.get(APU.Reg.NR23));
		pulse.configure(Bits.extract(rf.get(APU.Reg.NR21), 6, 2), freq);
		// Volume envelope timer is reloaded with period.
		envelope.configure(rf.testBit(APU.Reg.NR22, SBit.B3), Bits.clip(3, rf.get(APU.Reg.NR22)), Bits.extract(rf.get(APU.Reg.NR22), 4, 4));
		// Channel volume is reloaded from NRx2.
	}

	@Override
	public int getAsInt() {
		return pulse.andThen(lengthCounter).andThen(envelope).applyAsInt(0);
	}
}
