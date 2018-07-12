/*
 * libHeartbeat (https://github.com/imec-apt/libheartbeat)
 *  Copyright 2018 Matthias Stevens, IMEC vzw, Belgium.
 *  Released to the public domain (CC0-v1.0 license: https://creativecommons.org/publicdomain/zero/1.0/).
 */

package be.imec.apt.heartbeat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests of the Heartbeat class.
 */
public class HeartbeatTest {
	@Test
	public void testSynthHalfHeartbeatLength() {
		final int halfBeatNs = 6615; // at 60 BPM & 44100Hz
		assertEquals(halfBeatNs, Heartbeat.synthHalfHeartbeat(halfBeatNs).length);
	}

	@Test
	public void testSynthHeartbeatsLength() {
		final float tempoBpm = 100;
		final int numBeats = 10;
		final int sampleRate = 44100;
		assertEquals(Heartbeat.calculateTotalSamples(tempoBpm, numBeats, sampleRate), Heartbeat.synthHeartbeats64bit(tempoBpm, numBeats, sampleRate).length);
	}

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testWriteHeartbeatFile() throws IOException {
		assertTrue(Heartbeat.writeHeartbeatFile(tempFolder.newFolder(), 60, 10, 44100, true).exists());
	}

	@Test
	public void generateAllHeartbeatFiles() throws IOException {
		final int numBeats = 1;
		for(int sampleRate : Arrays.asList(44100, 48000)) {
			for (boolean filtered : new boolean[]{false, true}) {
				final File folder = new File(new File(new File(System.getProperty("user.dir"), "output"), sampleRate + "Hz"), (!filtered ? "un" : "") + "filtered");
				folder.mkdir();
				System.out.println("Path for " + (!filtered ? "un" : "") + "filtered wave files: " + folder.getAbsolutePath());
				for (float tempoBpm = 40; tempoBpm <= 190; tempoBpm++)
					Heartbeat.writeHeartbeatFile(folder, tempoBpm, numBeats, sampleRate, filtered);
				assertEquals(folder.listFiles().length, 190 - 40 + 1);
			}
		}
	}
}