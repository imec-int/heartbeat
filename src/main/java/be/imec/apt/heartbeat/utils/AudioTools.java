/*
 * libHeartbeat (https://github.com/imec-apt/heartbeat)
 *  Copyright 2018 Matthias Stevens, IMEC vzw, Belgium.
 *  Released to the public domain (CC0-v1.0 license: https://creativecommons.org/publicdomain/zero/1.0/).
 */

package be.imec.apt.heartbeat.utils;

import be.imec.apt.heartbeat.utils.thirdparty.IirFilter;
import uk.me.berndporr.iirj.Cascade;

/**
 * Small collection of helper methods to deal with audio signals.
 *
 * @author Matthias Stevens (C) 2018 IMEC vzw
 */
public final class AudioTools {
	private AudioTools() {}

	/**
	 * Applies in-place normalisation of the given samples with respect to max(abs(samples)).
	 *
	 * @param samples array of samples to normalise
	 * @return the same given array (now containing normalised samples)
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static double[] normalise(final double[] samples) {
		double maxAbs = 0.0;
		// Find max(abs(s))
		for(double sample : samples)
			if(Math.abs(sample) > maxAbs)
				maxAbs = Math.abs(sample);
		// Normalise:
		return gain(1.0 / maxAbs, samples);
	}

	/**
	 * In-place application of linear gain.
	 *
	 * @param gainFactor the gain (multiplication) factor
	 * @param samples array of samples to apply it to
	 * @return the same given array (now containing multiplied values)
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static double[] gain(final double gainFactor, final double[] samples) {
		return applyFilter(x -> x * gainFactor, samples);
	}

	/**
	 * In-place {@link IirFilter} filter application.
	 *
	 * @param iirFilter an {@link IirFilter} filter
	 * @param samples array of samples to apply it to
	 * @return the same given array (now containing filtered samples)
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static double[] applyFilter(final IirFilter iirFilter, final double[] samples) {
		return applyFilter(iirFilter::step, samples);
	}

	/**
	 * In-place Cascade filter application.
	 *
	 * @param cascadeFilter a {@link Cascade} filter
	 * @param samples array of samples to apply it to
	 * @return the same given array (now containing filtered samples)
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static double[] applyFilter(final Cascade cascadeFilter, final double[] samples) {
		return applyFilter(cascadeFilter::filter, samples);
	}

	/**
	 * In-place {@link Filter} application.
	 *
	 * @param filter a {@link Filter} to apply
	 * @param samples array of samples to apply it to
	 * @return the same given array (now containing filtered samples)
	 */
	@SuppressWarnings("UnusedReturnValue")
	public static double[] applyFilter(final Filter filter, final double[] samples) {
		for(int i = 0; i < samples.length; i++)
			samples[i] = filter.apply(samples[i]);
		return samples;
	}

	/**
	 * Interface representing filters to apply to a signal on a sample-by-sample basis.
	 */
	public interface Filter {
		double apply(final double sample);
	}
}
