/**
 * Copyright 2013 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
 * 	http://www.source-code.biz, http://www.inventec.ch/chdh
 * Portions: Copyright 2018 Matthias Stevens, IMEC vzw, Belgium
 *  http://www.imec-int.com, matthias.stevens@imec.be
 *
 * This module is multi-licensed and may be used under the terms
 *  of any of the following licenses:
 *
 *   EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
 *   LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
 *
 *  Please contact the author if you need another license.
 *  This module is provided "as is", without warranties of any kind.
 */

package be.imec.apt.heartbeat.utils.thirdparty;

/**
 * An IIR (infinite impulse response) filter.
 * <p>
 * Filter schema: <a href="http://commons.wikimedia.org/wiki/File:IIR_Filter_Direct_Form_1.svg">Wikipedia</a>
 * <p>
 * Formula:
 * <pre>
 *    y[i] = x[i] * b[0]  +  x[i-1] * b[1]  +  x[i-2] * b[2]  +  ...
 *                        -  y[i-1] * a[1]  -  y[i-2] * a[2]  -  ...
 * </pre>
 * (x = input, y = output, a and b = filter coefficients, a[0] must be 1)
 * <p>
 * Original implementation by Christian d'Heureuse, made available under terms of EPLv1.0 &amp; LGPLv2.1 at <a href="http://www.source-code.biz/dsp/java">http://www.source-code.biz/dsp/java</a>.<br>
 * Modified by Matthias Stevens, redistributed under the same terms at <a href="https://github.com/imec-apt/libheartbeat">https://github.com/imec-apt/libheartbeat</a>.
 */
public class IirFilter {
	/**
	 * A coefficients, applied to output values (negative)
	 */
	private final double[] a;

	/**
	 * B coefficients, applied to input values
	 */
	private final double[] b;

	/**
	 * size of input delay line
	 */
	private final int n1;
	/**
	 * size of output delay line
	 */
	private final int n2;

	/**
	 * input signal delay line (ring buffer)
	 */
	private double[] buf1;
	/**
	 * output signal delay line (ring buffer)
	 */
	private double[] buf2;

	/**
	 * current ring buffer position in buf1
	 */
	private int pos1;
	/**
	 * current ring buffer position in buf2
	 */
	private int pos2;

	/**
	 * Creates an IIR filter.
	 *
	 * @param coeffs the A and B coefficients. a[0] must be 1.
	 **/
	public IirFilter(final IirFilterCoefficients coeffs) {
		this(coeffs.a, coeffs.b);
	}

	/**
	 * Creates an IIR filter.
	 *
	 * @param a the A coefficients. a[0] must be 1.
	 * @param b the B coefficients.
	 **/
	public IirFilter(final double[] a, final double[] b) {
		this.a = a;
		this.b = b;
		if (a.length < 1 || b.length < 1 || a[0] != 1.0) {
			throw new IllegalArgumentException("Invalid coefficients.");
		}
		n1 = b.length - 1;
		n2 = a.length - 1;
		reset(); // !!!
	}

	public void reset() {
		buf1 = new double[n1];
		buf2 = new double[n2];
		pos1 = 0;
		pos2 = 0;
	}

	public void apply(final double[] signal) {
		for(int s = 0; s < signal.length; s++)
			signal[s] = step(signal[s]);
	}

	public double step(final double inputValue) {
		double acc = b[0] * inputValue;
		for (int j = 1; j <= n1; j++) {
			int p = (pos1 + n1 - j) % n1;
			acc += b[j] * buf1[p];
		}
		for (int j = 1; j <= n2; j++) {
			int p = (pos2 + n2 - j) % n2;
			acc -= a[j] * buf2[p];
		}
		if (n1 > 0) {
			buf1[pos1] = inputValue;
			pos1 = (pos1 + 1) % n1;
		}
		if (n2 > 0) {
			buf2[pos2] = acc;
			pos2 = (pos2 + 1) % n2;
		}
		return acc;
	}

}