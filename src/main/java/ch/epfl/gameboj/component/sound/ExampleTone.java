package ch.epfl.gameboj.component.sound;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;
import java.util.function.IntSupplier;

public class ExampleTone {

	private static final int BUFFER_SIZE = 1024;
	private static final byte[] buffer = new byte[BUFFER_SIZE];
	private static final int RATE = 44100;
	private static int index = 0;
	private static SourceDataLine line;

	public static void main(String[] args) {

		try {
			ExampleTone.createTone(262, 100, 0.5f);
		} catch (LineUnavailableException lue) {
			System.out.println(lue);
		}
	}

	public static void createSquareWave(int durationCycles) {
		Pulse pulse = new Pulse();
		pulse.configure(0, 0x700);

		int samples = (int) (durationCycles / (float) GameBoy.CYCLES_PER_SECOND * RATE);
		int divider = (int) (GameBoy.CYCLES_PER_SECOND / RATE) + 1;
		int index = 0;

		byte[] buf = new byte[2 * samples];

		for (int cycle = 0; cycle < durationCycles; cycle++) {
			int sample = pulse.applyAsInt(0);
			if (cycle % divider == 0) {
				System.out.println(cycle);
				play(sample, sample);
			}
		}
	}

	public static byte[] createSmoothSquareWave(int frequency, int volume, float dutyCycle, int duration) {
		int samples = duration * RATE / 1000;
		byte[] buf = new byte[samples];
		int numHarmonics = RATE / (frequency * 2);
		double[] coefficients = new double[numHarmonics + 1];
		coefficients[0] = dutyCycle - 0.5;  // Start with DC coefficient
		for (int i = 1; i < coefficients.length; i++)
			coefficients[i] = Math.sin(i * dutyCycle * Math.PI) * 2 / (i * Math.PI);

		// Generate audio samples
		double scaler = frequency * Math.PI * 2 / RATE;
		for (int i = 0; i < samples; i++) {
			double temp = scaler * i;
			double val = coefficients[0];
			for (int j = 1; j < coefficients.length; j++)
				val += Math.cos(j * temp) * coefficients[j];
			buf[i] = (byte) (val * volume);
		}

		return buf;
	}

	public static void play(int left, int right) {
		System.out.printf("Playing %d %d\n", left, right);
		Preconditions.checkBits8(left);
		Preconditions.checkBits8(right);
		buffer[index++] = (byte) (left);
		buffer[index++] = (byte) (right);
		if (index > BUFFER_SIZE / 2) {
			line.write(buffer, 0, index);
			index = 0;
		}
	}
	/**
	 * parameters are frequency in Hertz and volume
	 **/
	public static void createTone(int frequency, int volume, float dutyCycle)
			throws LineUnavailableException {
		/** Exception is thrown when line cannot be opened */

		byte[] buf;
		AudioFormat audioF;

		buf = new byte[1];
		audioF = new AudioFormat(RATE, 8, 2, false, false);
		//sampleRate, sampleSizeInBits,channels,signed,bigEndian

		line = AudioSystem.getSourceDataLine(audioF);
		line.open(audioF);
		line.start();
		System.out.println(line.available());

		createSquareWave(524288 * 10);
//		byte[] sample1 = createSquareWave(262, 50, 0.5f, 500);
//		byte[] sample3 = createSquareWave(2 * 262, 50, 0.5f, 500);

//		sourceDL.write(sample3, 0, sample3.length);
//		sourceDL.write(sample1, 0, sample1.length);
//		sourceDL.write(sample3, 0, sample3.length);
//		sourceDL.write(sample1, 0, sample1.length);
//		sourceDL.write(sample3, 0, sample3.length);

		line.drain();
		line.stop();
		line.close();
	}
}