package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.cartridge.Cartridge;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BlarggSoundTest {
	private static final String PATH = "src/test/resources/testBlargg/dmg_sound/";
	private static final long CYCLES_BY_STEP = 65536;
	private static final long CYCLES_FIRST_STEP = 3 * 1048576;
	private static final long CYCLES_TIMEOUT = 15 * 1048576;
	private static final int MSG_ADDR = 0xa004;
	private static final int MSG_MAX_LEN = 100;

	@Test
	void test01Registers() throws IOException {
		runUntilTestEnds("01-registers");
	}

	@Test
	void test02LenCtr() throws IOException {
		runUntilTestEnds("02-len ctr", "0 1 2 3 ");
	}

	@Test
	void test03Trigger() throws IOException {
		runUntilTestEnds("03-trigger");
	}

	@Test
	void test04Sweep() throws IOException {
		runUntilTestEnds("04-sweep");
	}

	@Test
	void test05SweepDetails() throws IOException {
		runUntilTestEnds("05-sweep details");
	}

	@Test
	void test06OverflowOnTrigger() throws IOException {
		runUntilTestEnds("06-overflow on trigger");
	}

	@Test
	void test07LenSweepPeriodSync() throws IOException {
		runUntilTestEnds("07-len sweep period sync");
	}

	private void runUntilTestEnds(String test)  throws IOException {
		runUntilTestEnds(test, "");
	}

	private void runUntilTestEnds(String test, String extraMsg) throws IOException {
		File romFile = new File(PATH + test + ".gb");

		GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
		gb.runUntil(CYCLES_FIRST_STEP);

		do {
			gb.runUntil(gb.cycles() + CYCLES_BY_STEP);
			if (gb.cycles() > CYCLES_TIMEOUT) {
				fail("Timeout");
			}
		} while (testIsRunning(gb));

		assertTrue(checkSignature(gb), "Wrong signature!");
		assertEquals(0, getStatus(gb), "Failed: " + getMessage(gb));
		assertEquals(test + "\n\n" + extraMsg + "\nPassed\n", getMessage(gb));
	}

	private boolean testIsRunning(GameBoy gb) {
		return getStatus(gb) == 0x80;
	}

	private String getMessage(GameBoy gb) {
		StringBuilder sb = new StringBuilder();
		int addr = MSG_ADDR;
		int read = gb.bus().read(addr);
		while (read != 0 && addr - MSG_ADDR < MSG_MAX_LEN) {
			sb.append((char) read);
			addr += 1;
			read = gb.bus().read(addr);
		}
		return sb.toString();
	}

	private int getStatus(GameBoy gb) {
		return gb.bus().read(0xa000);
	}

	private boolean checkSignature(GameBoy gb) {
		int s0 = gb.bus().read(0xa001);
		int s1 = gb.bus().read(0xa002);
		int s2 = gb.bus().read(0xa003);
		return s0 == 0xde && s1 == 0xb0 && s2 == 0x61;
	}

}
