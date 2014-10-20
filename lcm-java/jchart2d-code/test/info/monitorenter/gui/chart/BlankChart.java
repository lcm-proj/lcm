/*
 *  BlankChart.java of project jchart2d
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
package info.monitorenter.gui.chart;

import java.awt.Dimension;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Contributed by zoola for bug report <a href=
 * "http://sourceforge.net/tracker/?func=detail&atid=459734&aid=1426449&group_id=50440"
 * target="_blank">#1426449 </a>.
 * <p>
 * 
 * @author zoola
 * 
 * @version $Revision: 1.3 $
 */
public final class BlankChart
    extends TestCase {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3258408426458525753L;

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(BlankChart.class.getName());

    suite.addTest(new BlankChart("testBlankChart"));

    return suite;
  }

  /**
   * Creates a test case with the given name.
   * <p>
   * 
   * @param testName
   *          the name of the test case.
   */
  public BlankChart(final String testName) {
    super(testName);

  }

  /**
   * Test the display for a blank chart.
   * <p>
   * 
   * @throws InterruptedException
   *           if sleep is interrupted.
   */
  public void testBlankChart() throws InterruptedException {
    JFrame frame = new JFrame(this.getClass().getName());
    frame.getContentPane().add(new Chart2D());
    frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    frame.setSize(new Dimension(400, 300));
    while (frame.isVisible()) {
      Thread.sleep(1000);
    }
  }

}
