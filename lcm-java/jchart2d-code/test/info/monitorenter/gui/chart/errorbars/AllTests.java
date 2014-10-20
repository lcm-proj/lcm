/*
 *  AllTests.java of project jchart2d, <purpose>
 *  Copyright (c) 2007 Achim Westermann, created on 08.10.2006 11:58:09.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart.errorbars;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Main test suite for the package
 * <code>{@link info.monitorenter.gui.chart.errorbars}</code>.
 * <p>
 * 
 * @author Achim westermann
 * @version $Revision: 1.3 $
 * 
 * @since 2.1.0
 */
public final class AllTests {
	/**
	 * Returns the JUnit test suite for this package.
	 * <p>
	 * 
	 * @return the JUnit test suite for this package
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for info.monitorenter.gui.chart.errorbars");
		// $JUnit-BEGIN$
		suite.addTest(TestErrorBarsVisual.suite());
		// $JUnit-END$
		return suite;
	}

	/**
	 * Hide constructor to prevent generation of class instances.
	 * <p>
	 */
	private AllTests() {

		// empty
	}

}
