/*
 *  AllTest.java of project JChart2d, 
 *  mother of all test suites that incorporates all tests for JChart2d..
 *  Copyright 2007 (C) Achim Westermann, created on 25.02.2007 18:59:08.
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

package info.monitorenter.gui.chart.test;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JChart2D main test suite, executes the individual test suites of all core
 * packages.
 * <p>
 * 
 * @author Achim Westermann
 * @version $Revision: 1.7 $
 * 
 * @since 2.1.0
 */
public final class AllTests {
  /**
   * Creates the JChart2D JUnit test suite.
   * <p>
   * 
   * @return the JChart2D JUnit test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite("JChart2d complete tests");

    suite.addTest(info.monitorenter.gui.chart.AllTests.suite());
    suite.addTest(info.monitorenter.gui.chart.axis.AllTests.suite());
    suite.addTest(info.monitorenter.gui.chart.demos.AllTests.suite());
    suite.addTest(info.monitorenter.gui.chart.errorbars.AllTests.suite());
    suite.addTest(info.monitorenter.gui.chart.labelformatters.AllTests.suite());
    suite.addTest(info.monitorenter.gui.chart.layouts.AllTests.suite());
    suite.addTest(info.monitorenter.gui.chart.traces.AllTests.suite());
    suite.addTest(info.monitorenter.util.collections.AllTests.suite());

    TestSetup wrapper = new TestSetup(suite) {

      /**
       * @see junit.extensions.TestSetup#setUp()
       */
      @Override
      protected void setUp() {

        // oneTimeSetUp();
      }

      /**
       * 
       * @see junit.extensions.TestSetup#tearDown()
       */
      @Override
      protected void tearDown() {

        // oneTimeTearDown();
      }
    };

    return wrapper;
  }

  /**
   * Hide constructor to prevent generation of class instances.
   * <p>
   */
  private AllTests() {

    // empty
  }
}
