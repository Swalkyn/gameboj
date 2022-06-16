package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;

public class PulseA extends Channel {
	public final SweepPulse pulse;
	public final LengthCounter lengthCounter;
	public final Envelope envelope;

	private final RegisterFile<APU.Reg> rf;

	public PulseA(FrameSequencer frameSequencer, RegisterFile<APU.Reg> rf) {
		this.rf = rf;
		this.pulse = new SweepPulse(frameSequencer);
		this.lengthCounter = new LengthCounter(this, frameSequencer, 64);
		this.envelope = new Envelope(frameSequencer);
	}

	@Override
	public void trigger() {
		if (dacEnabled()) {
			enable();
		}
		// Channel is enabled (see length counter).
		// If length counter is zero, it is set to 64 (256 for wave channel).
		if (lengthCounter.isZero()) {
			lengthCounter.loadCounter(0);
		}
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
		return Bits.extract(rf.get(APU.Reg.NR12), 3, 5) != 0;
	}

	@Override
	public int getSample() {
		return pulse.andThen(lengthCounter).andThen(envelope).applyAsInt(0);
	}
}
