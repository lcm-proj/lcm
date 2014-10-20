/*
 *
 *  AllTests.java, invokes all tests for package 
 *  info.monitorenter.gui.chart.
 *  Copyright (C) Achim Westermann, created on 23.04.2005, 15:24:13
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
package info.monitorenter.gui.chart;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Main test suite for the package <code>{@link info.monitorenter.gui.chart}</code>.<p>
 * 
 * @author Achim westermann
 * @version $Revision: 1.6 $
 * 
 * @since 2.1.0
 */

@RunWith(Suite.class)
@SuiteClasses( { TestMultiThreadingAndTracing.class, TestChart2DHeadless.class,TestMultithreading.class,TestChartOperationsVisual.class})

public final class AllTests {

  /**
   * Returns the JUnit test suite for this package.<p>
   * 
   * @return the JUnit test suite for this package
   */
  public static Test suite() {

    return new JUnit4TestAdapter(AllTests.class);

  }

  /**
   * Hide constructor to prevent generation of class instances.<p>
   */
  private AllTests() {

    // empty
  }
}
