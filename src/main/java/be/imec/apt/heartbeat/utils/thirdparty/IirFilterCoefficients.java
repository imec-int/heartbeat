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
* The coefficients for an IIR filter.
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
public class IirFilterCoefficients {
	/**
	 * A coefficients, applied to output values (negative).
	 */
	public final double[] a;

	/**
	 * B coefficients, applied to input values.
	 */
	public final double[] b;

	/**
	 *
	 * @param a the A coefficients. a[0] must be 1.
	 * @param b the B coefficients.
	 */
	public IirFilterCoefficients(final double[] a, final double[] b) {
		this.a = a;
		this.b = b;
	}
}