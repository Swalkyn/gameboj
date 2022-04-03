package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.GameBoy;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class GBSpeaker {
	private static final int BUFFER_SIZE = 8 * 2048;
	private static int actualBufferSize;
	private final static float SAMPLING_RATE = 44100;
	private final static AudioFormat FORMAT = new AudioFormat(SAMPLING_RATE, 8, 2, false, false);

	private SourceDataLine line;
	private byte[] buffer;
	private int index;
	private int cycle;
	private int divider;

	public void start() {
		System.out.println("Sound starting");
		if (line != null) {
			throw new IllegalStateException("Sound is already started");
		}
		try {
			line = AudioSystem.getSourceDataLine(FORMAT);
			line.open(FORMAT, BUFFER_SIZE);
		} catch (LineUnavailableException e) {
			throw new RuntimeException(e);
		}
		line.start();
		actualBufferSize = line.getBufferSize();
		buffer = new byte[line.getBufferSize()];
		divider = (int) (GameBoy.CYCLES_PER_SECOND / FORMAT.getSampleRate()) + 1;
	}

	public void stop() {
		System.out.println("Sound stopped");
		line.drain();
		line.stop();
		line = null;
	}

	public void play(int left, int right) {
		Preconditions.checkBits8(left);
		Preconditions.checkBits8(right);

		cycle %= divider;
		if (cycle == 0) {
			buffer[index++] = (byte) (left);
			buffer[index++] = (byte) (right);
			if (index > actualBufferSize / 2) {
				int available = line.available();
				System.err.printf("Writing %d, %d available\n", index, available);
				int toWrite = Math.min(available, index);
				line.write(buffer, 0, toWrite);
				index = 0;
			}
		}
		cycle += 1;
	}
}
