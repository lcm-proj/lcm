/*
 *  TestTracePoint2D.java of project jchart2d - a Junit 
 *  Test for class TracePoint2D.
 *  Copyright (C) 2007, Achim Westermann.
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

import info.monitorenter.gui.chart.traces.Trace2DLtd;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * JUnit test for class <code>{@link TracePoint2D}</code>.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 7.0.0
 * 
 */
public class TestTracePoint2D extends TestChart2D {
  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestTracePoint2D.class.getName());

    suite.addTest(new TestTracePoint2D("testSetLocationDoubleDouble"));

    return suite;
  }

  /**
   * Constructor with the test name.
   * <p>
   * 
   * @param testname
   *          the test name.
   */
  public TestTracePoint2D(final String testname) {
    super(testname);
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#createTraces()
   */
  @Override
  protected ITrace2D[] createTraces() {
    return new ITrace2D[] {new Trace2DLtd(200) };
  }

  /**
   * Test for <code>{@link TracePoint2D#setLocation(double, double)}.</code>
   * <p>
   * 
   * @throws InterruptedException
   *           if you don't let me sleep.
   * 
   */
  public void testSetLocationDoubleDouble() throws InterruptedException {
    ITracePoint2D point = this.getTraces()[0].iterator().next();
    Thread.sleep(2000);
    point.setLocation(point.getX(), point.getY() + 20);
    Thread.sleep(2000);

  }

}
