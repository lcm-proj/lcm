/*
 *  TestTrace2DSimple.java of project jchart2d - a Junit test case 
 *  for class Trace2DSimple. 
 *  Copyright (C) Achim Westermann, created on 08.06.2005, 22:58:51
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
package info.monitorenter.gui.chart.traces;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;

import java.beans.PropertyChangeListener;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A Junit test case for class <code>{@link Trace2DSimple}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 */
public class TestTrace2DSimple
    extends TestCase {
  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestTrace2DSimple.class.getName());

    suite.addTest(new TestTrace2DSimple("testMemoryLeakTrace2DListeners"));
    suite.addTest(new TestTrace2DSimple("testMemoryLeakTrace2DListenersSeverity"));

    return suite;
  }

  /**
   * Creates a test case with the given name.
   * <p>
   * 
   * @param testName
   *            the name of the test.
   */
  public TestTrace2DSimple(final String testName) {
    super(testName);
  }

  /**
   * Adds and removes a trace to a chart and asserts that only one and afterwards zero listeners are
   * contained in the chart.
   * <p>
   */
  public void testMemoryLeakTrace2DListeners() {
    Chart2D chart = new Chart2D();
    ITrace2D trace = new Trace2DSimple();
    PropertyChangeListener[] listeners;
    for (int i = 0; i < 100; i++) {
      chart.addTrace(trace);
      listeners = trace.getPropertyChangeListeners(ITrace2D.PROPERTY_MAX_X);
      Assert.assertEquals("Only one listener should be registered!", 1, listeners.length);
      chart.removeTrace(trace);
      listeners = trace.getPropertyChangeListeners(ITrace2D.PROPERTY_MAX_X);
      Assert.assertEquals("All listeners have to be deregistered!", 0, listeners.length);
    }

  }

  /**
   * <p>
   * Adds and removes a trace to a chart 100 times and asserts that only zero listeners are
   * contained in the chart afterwards.
   * </p>
   */
  public void testMemoryLeakTrace2DListenersSeverity() {
    Chart2D chart = new Chart2D();
    ITrace2D trace = new Trace2DSimple();
    int listeners = 0;
    for (int i = 0; i < 100; i++) {
      chart.addTrace(trace);
      chart.removeTrace(trace);
    }
    listeners = trace.getPropertyChangeListeners(ITrace2D.PROPERTY_MAX_X).length;
    Assert.assertEquals("All listeners have to be deregistered!", 0, listeners);

  }

}
