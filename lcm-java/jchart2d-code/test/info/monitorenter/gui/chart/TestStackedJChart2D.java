/*
 *  TestStackedJChart2D.java of project jchart2d, Junit test case for 
 *  jchart2d when two or more charts are stacked on each other.
 *  Copyright (C) Achim Westermann, created on 16.07.2005, 10:52:43
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
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.test.ATestStackedJChart2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Junit test for <code>{@link info.monitorenter.gui.chart.Chart2D}</code>
 * implementations.
 * <p>
 * 
 * @author Bill Schoolfield
 */
public class TestStackedJChart2D extends ATestStackedJChart2D {
  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestStackedJChart2D.class.getName());

    suite.addTest(new TestStackedJChart2D("testAlignment"));
    
    return suite;
  }

  /**
   * Constructor with the test name.
   * <p>
   * 
   * @param testname
   *          the test name.
   */
  public TestStackedJChart2D(final String testname) {
    super(testname);
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#createAxisX()
   */
  @Override
  protected AAxis<?> createAxisX() {
    return new AxisLinear<IAxisScalePolicy>();
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#createAxisY()
   */
  @Override
  protected AAxis<?> createAxisY() {
    return this.createAxisX();
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#createTraces()
   */
  @Override
  protected ITrace2D[] createTraces() {
    return new ITrace2D[] {new Trace2DSimple()};
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#fillTrace1(info.monitorenter.gui.chart.ITrace2D)
   */
  @Override
  protected void fillTrace1(final ITrace2D trace2D) {
    for (int i = 100000; i < 100100; i++) {
      trace2D.addPoint(i, i);
    }
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#fillTrace2(info.monitorenter.gui.chart.ITrace2D)
   */
  @Override
  protected void fillTrace2(final ITrace2D trace2D) {
    for (int i = 0; i < 101; i++) {
      trace2D.addPoint(i, i);
    }
  }
  
  /**
   * Tests x start alignment of two charts. 
   * <p>
   */
  public void testAlignment() {
    //Assert.assertEquals(this.m_axisX1.getPixelXRight(), this.m_axisX2.getPixelXRight(), 0);
  }
  
}
