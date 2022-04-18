package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;

public class PulseA extends Channel {
	private final SweepPulse pulse;
	private final LengthCounter lengthCounter;
	private final Envelope envelope;

	private final RegisterFile<APU.Reg> rf;

	public PulseA(Timer timer, RegisterFile<APU.Reg> rf) {
		this.rf = rf;
		this.pulse = new SweepPulse(timer);
		this.lengthCounter = new LengthCounter(timer, 64);
		this.envelope = new Envelope(timer);
	}

	@Override
	public void trigger() {
		enable();
		// Channel is enabled (see length counter).
		// If length counter is zero, it is set to 64 (256 for wave channel).
		lengthCounter.configure(rf.testBit(APU.Reg.NR14, SBit.B6), Bits.clip(6, rf.get(APU.Reg.NR11)));
		// Frequency timer is reloaded with period.
		int freq = Bits.make16(Bits.clip(3, rf.get(APU.Reg.NR14)), rf.get(APU.Reg.NR13));
		int duty = Bits.extract(rf.get(APU.Reg.NR11), 6, 2);
		int period = Bits.extract(rf.get(APU.Reg.NR10), 4, 3);
		boolean negate = Bits.test(rf.get(APU.Reg.NR10), SBit.B3);
		int shift = Bits.clip(3, rf.get(APU.Reg.NR10));
		pulse.configure(period, negate, shift, duty, freq);
		// Volume envelope timer is reloaded with period.
		envelope.configure(rf.testBit(APU.Reg.NR12, SBit.B3), Bits.clip(3, rf.get(APU.Reg.NR12)), Bits.extract(rf.get(APU.Reg.NR12), 4, 4));
		// Channel volume is reloaded from NRx2.
	}

	@Override
	public boolean dacEnabled() {
		return Bits.extract(rf.get(APU.Reg.NR22), 3, 5) != 0;
	}

	@Override
	public int getSample() {
		return pulse.andThen(lengthCounter).andThen(envelope).applyAsInt(0);
	}
}
