/*
 *  TestStaticCollectorChart.java  of project jchart2d - 
 *  Junit test case that works on property file based 
 *  data files. 
 *  Copyright (C) Achim Westermann, created on 23.04.2005, 08:21:12
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
import info.monitorenter.gui.chart.io.AStaticDataCollector;
import info.monitorenter.gui.chart.io.PropertyFileStaticDataCollector;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyForcedPoint;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Visual Junit test that uses a
 * <code>{@link PropertyFileStaticDataCollector}</code> with data files in the
 * same package that are named <tt>test&lt;x&gt;.properties</tt> where &gt;x&lt;
 * is a number starting from 0 and increasing by one.
 * <p>
 * 
 * @author Achim Westermann
 */
public class TestStaticCollectorChart extends TestCase {

  /**
   * Main debug entry.
   * <p>
   * 
   * @param args
   *          ignored.
   * @throws IOException
   *           if sth. goes wrong reading data.
   */
  public static void main(final String[] args) throws IOException {
    TestStaticCollectorChart test = new TestStaticCollectorChart(TestStaticCollectorChart.class
        .getName());
    test.testStaticCollectorChart2();
  }

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestStaticCollectorChart.class.getName());

    suite.addTest(new TestStaticCollectorChart("testStaticCollectorChart1"));
    suite.addTest(new TestStaticCollectorChart("testStaticCollectorChart2"));
    suite.addTest(new TestStaticCollectorChart("testStaticCollectorChart3"));
    suite.addTest(new TestStaticCollectorChart("testStaticCollectorChart7"));

    return suite;
  }

  /**
   * Creates a test case with the given name.
   * <p>
   * 
   * @param testName
   *          the name of the test case.
   */
  public TestStaticCollectorChart(final String testName) {
    super(testName);
  }

  /**
   * Internal helper that shows the chart in a frame.
   * <p>
   * 
   * @param chart
   *          the chart to display.
   */
  private void show(final StaticCollectorChart chart) {
    final JFrame frame = new JFrame("StaticCollectorChart");
    frame.getContentPane().add(chart);
    frame.addWindowListener(new WindowAdapter() {

      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {
        frame.setVisible(false);
        frame.dispose();
      }
    });
    frame.setSize(600, 600);
    frame.setVisible(true);
    while (frame.isVisible()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
    }
  }

  /**
   * Tests {@link StaticCollectorChart} with data from test1.properties.
   * <p>
   * 
   * @throws IOException
   *           if sth. goes wrong.
   */
  public final void testStaticCollectorChart1() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("test1.properties");
    ITrace2D trace = new Trace2DSimple();
    trace.setColor(Color.BLUE);
    AStaticDataCollector collector = new PropertyFileStaticDataCollector(trace, stream);
    StaticCollectorChart chart = new StaticCollectorChart(collector);
    this.show(chart);
  }

  /**
   * Tests {@link StaticCollectorChart} with data from test2.properties.
   * <p>
   * 
   * @throws IOException
   *           if sth. goes wrong.
   */
  public final void testStaticCollectorChart2() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("test2.properties");
    ITrace2D trace = new Trace2DSimple();
    trace.setColor(Color.BLUE);
    AStaticDataCollector collector = new PropertyFileStaticDataCollector(trace, stream);
    StaticCollectorChart chart = new StaticCollectorChart(collector);
    // Adapt the decimal formatting for this data set.
    // chart.getChart().getAxisY().setFormatter(
    // new LabelFormatterNumber(new DecimalFormat("#.#####")));
    this.show(chart);
  }

  /**
   * Tests {@link StaticCollectorChart} with data from test2.properties.
   * <p>
   * 
   * @throws IOException
   *           if sth. goes wrong.
   */
  public final void testStaticCollectorChart3() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("test3.properties");
    ITrace2D trace = new Trace2DSimple();
    trace.setColor(Color.RED);
    AStaticDataCollector collector = new PropertyFileStaticDataCollector(trace, stream);
    StaticCollectorChart chart = new StaticCollectorChart(collector);
    // Adapt the decimal formatting for this data set.
    this.show(chart);
  }

  /**
   * Tests {@link StaticCollectorChart} with data from test2.properties.
   * <p>
   * 
   * @throws IOException
   *           if sth. goes wrong.
   */
  public final void testStaticCollectorChart7() throws IOException {
    InputStream stream = this.getClass().getResourceAsStream("test7.properties");
    ITrace2D trace = new Trace2DSimple();
    trace.setColor(Color.RED);
    AStaticDataCollector collector = new PropertyFileStaticDataCollector(trace, stream);
    StaticCollectorChart collectorchart = new StaticCollectorChart(collector);
    Chart2D chart = collectorchart.getChart();
    chart.getAxisX().setRangePolicy(new RangePolicyForcedPoint(0));
    chart.getAxisY().setRangePolicy(new RangePolicyForcedPoint(0));
    this.show(collectorchart);
  }
}
