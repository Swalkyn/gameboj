package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;

public class PulseB extends Channel {
	public final Pulse pulse;
	public final LengthCounter lengthCounter;
	public final Envelope envelope;

	private final RegisterFile<APU.Reg> rf;

	public PulseB(FrameSequencer frameSequencer, RegisterFile<APU.Reg> rf) {
		this.rf = rf;
		pulse = new Pulse();
		lengthCounter = new LengthCounter(this, frameSequencer, 64);
		envelope = new Envelope(frameSequencer);
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
		int freq = Bits.make16(Bits.clip(3, rf.get(APU.Reg.NR24)), rf.get(APU.Reg.NR23));
		int duty = Bits.extract(rf.get(APU.Reg.NR21), 6, 2);
		pulse.configure(duty, freq);
		// Volume envelope timer is reloaded with period.
		envelope.configure(rf.testBit(APU.Reg.NR22, SBit.B3), Bits.clip(3, rf.get(APU.Reg.NR22)), Bits.extract(rf.get(APU.Reg.NR22), 4, 4));
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
