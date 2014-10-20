/*
 *  TestAddRemoveTrace.java of project jchart2d, 
 *  a Junit test case for adding / removing 
 *  Copyright (c) 2007 Achim Westermann, created on 22:22:26.
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
package info.monitorenter.gui.chart.demos;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;

import java.awt.Dimension;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Contributed by zoola for bug report <a href=
 * "http://sourceforge.net/tracker/?func=detail&atid=459734&aid=1426461&group_id=50440"
 * target="_blank">#1426461 </a>.
 * <p>
 * 
 * @author zoola
 * 
 * @version $Revision: 1.3 $
 */
public final class TestAddRemoveTrace
    extends TestCase {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3689069560279873588L;

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestAddRemoveTrace.class.getName());

    suite.addTest(new TestAddRemoveTrace("testAddRemoveTrace"));

    return suite;
  }

  /**
   * Creates a test case with the given name.
   * <p>
   * 
   * @param testName
   *          the name of the test case.
   * 
   */
  public TestAddRemoveTrace(final String testName) {
    super(testName);
  }

  /**
   * Creates an empty chart adds a trace, removes it again and displays it.
   * <p>
   * 
   * @throws InterruptedException
   *           if sleeping is interrupted.
   */
  public void testAddRemoveTrace() throws InterruptedException {
    JFrame frame = new JFrame(this.getClass().getName());
    Dimension size = new Dimension(400,400);
    Chart2D chart = new Chart2D();
    chart.setPreferredSize(size);
    ITrace2D trace = new Trace2DLtd();
    chart.addTrace(trace);
    chart.removeTrace(trace);
    frame.getContentPane().add(chart);
    frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    while (frame.isVisible()) {
      Thread.sleep(1000);
    }
  }
}
