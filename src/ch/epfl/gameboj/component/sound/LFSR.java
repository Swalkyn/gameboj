package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.bits.Bits;

public class LFSR implements SoundUnit {
	private final static int[] DIVISORS = {8, 16, 32, 48, 64, 80, 96, 112};

	private boolean widthMode;
	private int divCode;
	private int shift;
	private int register;
	// Count is incremented @ 1MHz
	private int count;

	public LFSR() {
		this.configure(0, false, 0);
		count = 0;
	}

	public void reset() {
		register = 0x7FFF;
	}

	public void configure(int shift, boolean widthMode, int divCode) {
		this.shift = shift;
		this.widthMode = widthMode;
		this.divCode = divCode;
	}

	@Override
	public int applyAsInt(int i) {
		count = (count + 1) % (DIVISORS[divCode] >> 1) << shift;
		if (count == 0) {
			boolean xor = Bits.test(register, 0) ^ Bits.test(register, 1);
			register >>= 1;
			register = Bits.set(register, 14, xor);
			if (widthMode) {
				register = Bits.set(register, 6, xor);
			}
		}

		return Bits.test(register, 0) ? 0 : 0xFF;
	}
}
