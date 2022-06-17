package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.bits.SBit;

public class Noise extends Channel {
	public final LFSR lfsr;
	public final LengthCounter lengthCounter;
	public final Envelope envelope;
	private final RegisterFile<APU.Reg> rf;

	public Noise(RegisterFile<APU.Reg> rf, FrameSequencer frameSequencer) {
		this.lfsr = new LFSR();
		this.lengthCounter = new LengthCounter(this, frameSequencer, 64);
		this.envelope = new Envelope(frameSequencer);
		this.rf = rf;
	}

	@Override
	public int getSample() {
		return lfsr.andThen(lengthCounter).andThen(envelope).applyAsInt(0);
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
		// Noise channel's LFSR bits are all set to 1.
		lfsr.reset();
		// Volume envelope timer is reloaded with period.
		boolean addMode = rf.testBit(APU.Reg.NR42, SBit.B3);
		int period = Bits.clip(3, rf.get(APU.Reg.NR42));
		int startVolume = Bits.extract(rf.get(APU.Reg.NR42), 4, 4);
		envelope.configure(addMode, period, startVolume);
	}

	@Override
	public boolean dacEnabled() {
		return Bits.extract(rf.get(APU.Reg.NR42), 3, 5) != 0;
	}
}
