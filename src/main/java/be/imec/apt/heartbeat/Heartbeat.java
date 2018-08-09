/*
 * libHeartbeat (https://github.com/imec-apt/heartbeat)
 *  Copyright 2018 Matthias Stevens, IMEC vzw, Belgium.
 *  Released to the public domain (CC0-v1.0 license: https://creativecommons.org/publicdomain/zero/1.0/).
 */

package be.imec.apt.heartbeat;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import be.imec.apt.heartbeat.utils.thirdparty.IirFilter;
import be.imec.apt.heartbeat.utils.thirdparty.IirFilterCoefficients;
import be.imec.apt.heartbeat.utils.thirdparty.WaveFileWriter;
import be.imec.apt.heartbeat.utils.AudioTools;
import uk.me.berndporr.iirj.Butterworth;

/**
 * Heartbeat audio signal synthesis tool, written in "pure" Java.
 *
 * Based on Ben Holmes' MATLAB implementation (https://github.com/bencholmes/heartbeat),
 * ported to Java by Matthias Stevens (matthias.stevens@imec.be) for imec APT.
 * Just like Ben's original this code is released to the public domain (CC0-v1.0 license),
 * except for the classes in the be.imec.apt.heartbeat.utils.thirdparty package (see below).
 *
 * Uses:
 *  - Bernd Porr's iirj library (https://github.com/berndporr/iirj),
 *      as an external dependency;
 *  - a small portion of Christian d'Heureuse's Java DSP collection (http://www.source-code.biz/dsp/java),
 *      2 modified source files published here (see be.imec.apt.heartbeat.utils.thirdparty package), EPLv 1.0 &amp; LGPL v2.1 apply;
 *  - a small portion of Phil Burk's Java audio synthesizer library (https://github.com/philburk/jsyn),
 *      1 modified source file published here (see be.imec.apt.heartbeat.utils.thirdparty package), Apache License v2.0 applies.
 *
 * Special thanks to:
 *  - Ben Holmes, for creating of the MATLAB original and helping me port it;
 *  - and Joren Six (IPEM, Ghent University, https://github.com/JorenSix) for advising me.
 *
 * @author Matthias Stevens (C) 2018 IMEC vzw
 */
public final class Heartbeat {

	static private final double TWO_PI = 2.0 * Math.PI;

	static private final Random r = new Random();

	/**
	 * A & B filter coefficients for chest/abdomen resonance,
	 * as calculated by following Matlab code (from https://github.com/bencholmes/heartbeat):
	 * <pre>
	 *     % Peaking filter
	 *     [bPeak, aPeak] = iirpeak(110/(fs/2),120/(0.5*fs));
	 * </pre>
	 * Or the following Python/SciPy code (by myself):
	 * <pre>
	 *     from scipy import signal
	 *
	 *     def computeIIRPeakCoeffs(fs, f0, f1):
	 *         w0 = f0 / (fs/2)
	 *         bw = f1 / (fs/2)
	 *         Q = w0/bw
	 *         b, a = signal.iirpeak(w0, Q);
	 *
	 *     computeIIRPeakCoeffs(44100, 110, 120);
	 * </pre>
	 */
	static private final Map<Integer, IirFilterCoefficients> CHEST_RESONANCE_IIRPEAK_COEFFS = new HashMap<>();
	static {
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(4000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.80006264,  0.82727195 },
											new double[] {  0.08636403,  0.0,        -0.08636403 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(8000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.90280667,  0.90992999 },
											new double[] {  0.04503501,  0.0,        -0.04503501 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(11025, new IirFilterCoefficients(
											new double[] {  1.0,        -1.9300491,   0.93384782 },
											new double[] {  0.03307609,  0.0,        -0.03307609 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(22050, new IirFilterCoefficients(
											new double[] {  1.0,        -1.96541147,  0.96637737 },
											new double[] {  0.01681132,  0.0,        -0.01681132 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(32000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.9762503,   0.97671134 },
											new double[] {  0.01164433,  0.0,        -0.01164433 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(44100, new IirFilterCoefficients(
											new double[] {  1.0,        -1.98280387,  0.9830474 },
											new double[] {  0.0084763,   0.0,        -0.0084763 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(48000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.98420842,  0.98441413 },
											new double[] {  0.00779294,  0.0,        -0.00779294 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(64000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.98817194,  0.98828788 },
											new double[] {  0.00585606,  0.0,        -0.00585606 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(88200, new IirFilterCoefficients(
											new double[] {  1.0,        -1.99142664,  0.99148778 },
											new double[] {  0.00425611,  0.0,        -0.00425611 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(96000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.99212507,  0.9921767  },
											new double[] {  0.00391165,  0.0,        -0.00391165 }));
		CHEST_RESONANCE_IIRPEAK_COEFFS.put(192000, new IirFilterCoefficients(
											new double[] {  1.0,        -1.99606777,  0.9960807  },
											new double[] {  0.00195965,  0.0,        -0.00195965 }));
	}

	private Heartbeat() {}

	static public File writeHeartbeatFile(final File folder, final float tempoBpm, final int numBeats, final int sampleRate, final boolean filtered) throws IOException {
		// Create file (will be overwritten if existing!):
		final File wav = new File(folder, "Heartbeats_" + ((int) tempoBpm) + "bpm.wav");
		wav.createNewFile();

		// Prepare WaveFileWriter:
		WaveFileWriter waveFileWriter = new WaveFileWriter(wav);
		waveFileWriter.setBitsPerSample(16);	// 16bits per sample
		waveFileWriter.setFrameRate(sampleRate);
		waveFileWriter.setSamplesPerFrame(1);	// Mono

		// Synthesize heartbeats samples and write to wav file:
		waveFileWriter.write(synthHeartbeats16bit(tempoBpm, numBeats, sampleRate, filtered));

		return wav;
	}

	static public short[] synthHeartbeats16bit(final float tempoBpm, final int numBeats, final int sampleRate) {
		return synthHeartbeats16bit(tempoBpm, numBeats, sampleRate, true);
	}

	static public short[] synthHeartbeats16bit(final float tempoBpm, final int numBeats, final int sampleRate, final boolean filtered) {
		final double[] samples = synthHeartbeats64bit(tempoBpm, numBeats, sampleRate, filtered);

		// Convert to 16bit PCM:
		final short[] shorts = new short[samples.length];
		for(int s = 0; s < samples.length; s++)
			shorts[s] = WaveFileWriter.convertToPCM16(samples[s]);

		return shorts;
	}

	static public double[] synthHeartbeats64bit(final float tempoBpm, final int numBeats, final int sampleRate) {
		return synthHeartbeats64bit(tempoBpm, numBeats, sampleRate, true);
	}

	static public double[] synthHeartbeats64bit(final float tempoBpm, final int numBeats, final int sampleRate, final boolean filtered) {
		final int totalNs = calculateTotalSamples(tempoBpm, numBeats, sampleRate);

		// Synthesize heartbeats:
		final DoubleBuffer beatsBff = DoubleBuffer.allocate(totalNs); // init to all zeros
		for(int b = 0; b < numBeats; b++)
			beatsBff.put(synthHeartbeat(tempoBpm, sampleRate, filtered));

		return beatsBff.array();
	}

	static int calculateTotalSamples(final float tempoBpm, final int numBeats, final int sampleRate) {
		// Durations and number of samples
		//	Calculate the duration of one beat and all the beats.
		final double beatDur	= (60.0 / tempoBpm);
		final double totalDur	= beatDur * numBeats;
		// Number of samples:
		return (int) Math.floor(totalDur * sampleRate);
	}

	static private double[] synthHeartbeat(final float tempoBpm, final int sampleRate, final boolean filtered) {
		// Durations and number of samples
		//	Calculate the duration of one beat and all the beats.
		final double beatDur    	= (60.0 / tempoBpm);
		final double halfBeatDur	= Math.min(0.15, 0.15 * beatDur);
		//	Number of samples for respective durations
		final int beatNs			= (int) Math.floor(beatDur * sampleRate);
		final int halfBeatNs		= (int) Math.floor(halfBeatDur * sampleRate);
		final int shortPauseNs		= (int) Math.ceil(0.25 * (beatNs - 2 * halfBeatNs));
		final int longPauseNs		= (int) Math.floor(0.75 * (beatNs - 2 * halfBeatNs));

		// Synthesize unfiltered heartbeat:
		final DoubleBuffer beatBff = DoubleBuffer.allocate(beatNs); // init to all zeros
		// First "half-beat" at 80% gain:
		beatBff.put(AudioTools.gain(0.8, synthHalfHeartbeat(halfBeatNs)));
		advanceBuffer(beatBff, shortPauseNs);
		// Second "half-beat" at 100% gain:
		beatBff.put(/*AudioTools.gain(1.0, */synthHalfHeartbeat(halfBeatNs)/*)*/);
		advanceBuffer(beatBff, longPauseNs);

		double[] heartbeatSamples = beatBff.array();

		// Filter if needed:
		if(filtered) {
			//Filtering to simulate resonant "abdomen"
			//	Butterworth 3rd order bandpass:
			final Butterworth bandpass = new Butterworth();
			bandpass.bandPass(3, sampleRate, (20 + 140 + tempoBpm) / 2, (140 + tempoBpm - 20));
			AudioTools.applyFilter(bandpass, heartbeatSamples);
			//	Peaking filter
			IirFilterCoefficients coeffs = CHEST_RESONANCE_IIRPEAK_COEFFS.get(sampleRate);
			if(coeffs == null)
				throw new IllegalArgumentException("Unsupported sample rate for filtering: " + sampleRate);
			AudioTools.applyFilter(new IirFilter(coeffs), heartbeatSamples);
		}

		// Return normalised samples:
		return AudioTools.normalise(heartbeatSamples);
	}

	/**
	 * Simulates the sound of "half heartbeart"
	 * One "dum" of the "dum-dum" sound of a single heartbeat (not a medical definition).
	 *
	 * @param halfBeatLength length in samples
	 * @return audio samples
	 */
	static /*package*/ double[] synthHalfHeartbeat(final int halfBeatLength) {
		final DoubleBuffer halfBeatBff = DoubleBuffer.allocate(halfBeatLength); // init to all zeros

		// EKG sections:
		//	P
		halfBeatBff.put(ekgHannSegment(halfBeatLength, 9.0, 0.1));
		//	PR (zeros)
		advanceBuffer(halfBeatBff, + ekgSegmentLength(halfBeatLength, 8.0));
		//	Q
		halfBeatBff.put(ekgHannSegment(halfBeatLength, 24.0, -0.1));
		//	R
		halfBeatBff.put(ekgHannSegment(halfBeatLength, 6.0, 1.0));
		//	S
		halfBeatBff.put(ekgHannSegment(halfBeatLength, 24.0, -0.3));
		//	ST (zeros)
		advanceBuffer(halfBeatBff, ekgSegmentLength(halfBeatLength, 9.0));
		//	T
		halfBeatBff.put(ekgHannSegment(halfBeatLength, 9.0, 0.2));
		//	U
		halfBeatBff.put(ekgHannSegment(halfBeatLength, 11.0, 0.1));

		// Silence for the remaining number of samples:
		halfBeatBff.position(halfBeatLength - 1);

		// "Stitch together":
		return halfBeatBff.array();
	}

	static private int ekgSegmentLength(final int pulse_Ns, final double pulseDivider) {
		return (int) Math.floor((0.75 + r.nextDouble() / 2.0) * pulse_Ns / pulseDivider);
	}

	static private double[] ekgHannSegment(final int pulse_Ns, final double pulseDivider, final double ampMultiplier) {
		final double amp = (0.75 + r.nextDouble() / 2.0) * ampMultiplier;
		return AudioTools.gain(amp, hann(ekgSegmentLength(pulse_Ns, pulseDivider)));
	}

	static private void advanceBuffer(final Buffer buffer, final int distance) {
		buffer.position(buffer.position() + distance);
	}

	/**
	 * https://www.mathworks.com/help/signal/ref/hann.html#d119e79596
	 * https://en.wikipedia.org/wiki/Hann_function
	 *
	 * @param length length of the hann window
	 * @return array containing the values in the window
	 */
	static private double[] hann(final int length) {
		final double[] w = new double[length];
		for(int n = 0; n < length; n++)
			w[n] = 0.5 * (1.0 - Math.cos(TWO_PI * n / (length - 1)));
		return w;
	}
}
