package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.epfl.gameboj.component.Joypad;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cpu.Cpu;

class JoypadTest {

	private static final int P1 = AddressMap.REG_P1;
	
	private Joypad joypad() {
		return new Joypad(new Cpu());
	}
	
	@Test
	void selectionBitsWork() {
		Joypad j = joypad();
		j.write(P1, 0b1100_1111);
		assertEquals("00110000", str(j));
		j.write(P1, 0b1111_1111);
		assertEquals("00000000", str(j));
		j.write(P1, 0b1101_1111);
		assertEquals("00100000", str(j));
		j.write(P1, 0b1110_1111);
		assertEquals("00010000", str(j));
	}
	
	@Test
	void keyAWorks() {
		Joypad j = joypad();
		
		j.keyPressed(Key.A);
		assertEquals("00000000", str(j));
		
		j.write(P1, 0b11101111);
		j.keyPressed(Key.A);
		assertEquals("00010000", str(j));

		j.write(P1, 0b11011111);
		j.keyPressed(Key.A);
		assertEquals("00100001", str(j));
		j.keyReleased(Key.A);
		assertEquals("00100000", str(j));
	}
	
	@Test
	void keyRIGHTWorks() {
		Joypad j = joypad();
		
		j.keyPressed(Key.RIGHT);
		assertEquals("00000000", str(j));
		
		j.write(P1, 0b11011111);
		j.keyPressed(Key.RIGHT);
		assertEquals("00100000", str(j));
		
		j.write(P1, 0b11101111);
		j.keyPressed(Key.RIGHT);
		assertEquals("00010001", str(j));
	}
	
	@Test
	void keyUPWorks() {
		Joypad j = joypad();
		
		j.keyPressed(Key.UP);
		assertEquals("00000000", str(j));
		
		j.write(P1, 0b11011111);
		j.keyPressed(Key.UP);
		assertEquals("00100000", str(j));
		
		j.write(P1, 0b11101111);
		j.keyPressed(Key.UP);
		assertEquals("00010100", str(j));
		j.keyReleased(Key.UP);
		assertEquals("00010000", str(j));
	}
	
	@Test
	void comprehensiveTest() {
		Joypad j = joypad();
		
		j.keyPressed(Key.DOWN);
		j.write(P1, 0b11101111);
		j.keyPressed(Key.A);
		j.keyPressed(Key.LEFT);
		j.keyPressed(Key.DOWN);
		assertEquals("00011010", str(j));
		j.write(P1, 0b1101_1111);
		assertEquals("00100001", str(j));
		j.keyReleased(Key.START);
		assertEquals("00100001", str(j));
		j.keyReleased(Key.LEFT);
		j.keyReleased(Key.A);
		j.keyPressed(Key.SELECT);
		assertEquals("00100100", str(j));
		j.write(P1, 0b1110_1111);
		assertEquals("00011000", str(j));
	}

	
	public String str(Joypad j) {
		return String.format("%8s", Integer.toBinaryString(Bits.complement8(j.read(P1)))).replace(' ', '0');
	}
	
	public void show(Joypad j) {
		System.out.println(str(j));
	}
}

