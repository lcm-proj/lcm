/*
 *  MathUtil, utility class for math operations.
 *  Copyright (C) 2004 - 2011 Achim Westermann.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.util.math;

/**
 * Static helpers for working with numbers.
 * <p>
 * Maybe not always the fastest solution to call in here, but working. Also
 * usable for seeing examples and cutting code for manual inlining.
 * <p>
 * 
 * @author Achim.Westermann@gmx.de
 * 
 * @version $Revision: 1.3 $
 */
public final class MathUtil {

	/** Singleton instance. */
	private static MathUtil instance = null;

	/**
	 * Asserts that the given double is not invalid for calculation.
	 * <p>
	 * It must not be one of:
	 * <ul>
	 * <li> {@link Double#NaN}</li>
	 * <li> {@link Double#NEGATIVE_INFINITY}</li>
	 * <li> {@link Double#POSITIVE_INFINITY}</li>
	 * </ul>
	 * <p>
	 * 
	 * @param d
	 *            the double to test.
	 * 
	 * @throws IllegalArgumentException
	 *             if the assertion fails.
	 */
	public static void assertDouble(final double d)
			throws IllegalArgumentException {
		if (!MathUtil.isDouble(d)) {
			throw new IllegalArgumentException(d + " is not valid.");
		}
	}

	/**
	 * Asserts if the given two doubles are equal within the given precision
	 * range by the operation:
	 * 
	 * <pre>
	 * Math.abs(d1 - d2) &lt; precision
	 * </pre>
	 * 
	 * .
	 * <p>
	 * 
	 * Because floating point calculations may involve rounding, calculated
	 * float and double values may not be accurate. This routine should be used
	 * instead. If called with a very small precision range this routine will
	 * not be stable against the rounding of calculated floats but at least
	 * prevent a bug report of the findbugs tool. See the Java Language
	 * Specification, section 4.2.4.
	 * <p>
	 * 
	 * @param d1
	 *            a double to check equality to the other given double.
	 * 
	 * @param d2
	 *            a double to check equality to the other given double.
	 * 
	 * @param precisionRange
	 *            the range to allow differences of the two doubles without
	 *            judging a difference - this is typically a small value below
	 *            0.5.
	 * 
	 * @return true if both given doubles are equal within the given precision
	 *         range.
	 */
	public static boolean assertEqual(final double d1, final double d2,
			final double precisionRange) {
		return Math.abs(d1 - d2) < precisionRange;
	}

	/**
	 * Returns the singleton instance of this class.
	 * <p>
	 * 
	 * This method is useless for now as all methods are static. It may be used
	 * in future if VM-global configuration will be put to the state of the
	 * instance.
	 * <p>
	 * #
	 * 
	 * @return the singleton instance of this class.
	 */
	public static MathUtil getInstance() {
		if (MathUtil.instance == null) {
			MathUtil.instance = new MathUtil();
		}
		return MathUtil.instance;
	}

	/**
	 * Raises the given integer by one (bad performance).
	 * <p>
	 * 
	 * <b>Warning</b>: Only use this for testing code or prototypes as a new
	 * instance might be created for each call. Use primitive data types when
	 * fast calculations are required.
	 * <p>
	 * 
	 * <b>Warning</b>: Never do the following: <code>
	 *  Integer count = new Integer(6);
	 * 	MathUtil.increment(count);
	 *  // don't expect count now carries 6.
 * </code> Integers are immutable.
	 * Write: <code>
	 *  Integer count = new Integer(6);
	 * 	count = MathUtil.increment(count);
 * </code>
	 * 
	 * <p>
	 * 
	 * @param value
	 *            the value to increase, if null is used a new instance will be
	 *            initialized with value 0 and incremented.
	 * 
	 * @return a (potentially new by the means of <code>
	 *         {@link Integer#valueOf(int)}</code>) value increased by one.
	 */
	public static Integer increment(final Integer value) {
		Integer result;
		int ival = 0;
		if (value != null) {
			ival = value.intValue();
		}
		result = Integer.valueOf(ival + 1);
		return result;
	}

	/**
	 * Tests that the given double is not invalid for calculation.
	 * <p>
	 * It must not be one of:
	 * <ul>
	 * <li> {@link Double#NaN}</li>
	 * <li> {@link Double#NEGATIVE_INFINITY}</li>
	 * <li> {@link Double#POSITIVE_INFINITY}</li>
	 * </ul>
	 * <p>
	 * 
	 * @param d
	 *            the double to test.
	 * 
	 * @return true if the given double is valid for calculation (not infinite
	 *         or NaN).
	 */
	public static boolean isDouble(final double d) {
		return !(Double.isInfinite(d) || Double.isNaN(d));
	}

	/**
	 * Avoids creation from outside for singleton support.
	 * <p>
	 * 
	 */
	private MathUtil() {
		// nop
	}
}
