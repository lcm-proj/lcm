/*
 *  TestChart2DHeadless.java of project jchart2d, junit tests for Chart2D
 *  in headless mode.
 *  Copyright (c) 2007 Achim Westermann, created on 14:32:20.
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
package info.monitorenter.gui.chart;

import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.events.Chart2DActionSaveImageSingleton;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterDate;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Junit testcase for <code>{@link info.monitorenter.gui.chart.Chart2D}</code>
 * in headless (non-ui) mode.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * @version $Revision: 1.10 $
 */
public class TestChart2DHeadless extends TestCase {

  /**
   * Junit test ui runner.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
//    TestRunner.run(TestChart2DHeadless.class);
    TestChart2DHeadless test = new TestChart2DHeadless("blabla");
    test.testAddRemoveTraceAfterChangingZIndex();
  }

  /**
   * Constructor with test name.
   * <p>
   * 
   * @param testName
   *          the name of the test.
   */
  public TestChart2DHeadless(final String testName) {
    super(testName);
  }

  /**
   * Creates several charts, adds a trace to each of them, destroys the chart
   * and checks, if a memory leak occurs.
   * <p>
   * 
   * @org.junit.Test
   */
  public void testMemoryLeak() {
    Chart2D chart;
    ITrace2D trace;
    WeakHashMap<Chart2D, ? > chartMap = new WeakHashMap<Chart2D, Object>();
    for (int i = 0; i < 50; i++) {
      chart = new Chart2D();
      System.out.print("Creating really big trace (100000)...");
      trace = new Trace2DLtd(100000);
      System.out.println("    done!");
      if (i % 5 == 0) {
        System.out.println(i * 100 / 50 + " % done.");
      }
      chart.addTrace(trace);
      chartMap.put(chart, null);
      chart.destroy();
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    chart = null;
    trace = null;
    System.runFinalization();
    System.gc();
    try {
      System.out.println("Please wait 10 seconds...");
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.runFinalization();
    System.gc();
    try {
      System.out.println("Please wait another 10 seconds...");
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Assert.assertEquals(0, chartMap.size());
  }

  /**
   * Tests the method {@link Chart2D#snapShot()} method in non-UI mode by
   * creating an image of a chart that has not been painted (in UI) before.
   * <p>
   * 
   * @throws IOException
   *           if sth goes wrong in I/O (saving chart, deleting test file,...).
   * 
   * @org.junit.Test
   * 
   */
  public void testSnapshot() throws IOException {
    Chart2D chart;
    ITrace2D trace;
    chart = new Chart2D();
    trace = new Trace2DSimple();
    chart.addTrace(trace);
    for (int i = 0; i < 100; i++) {
      trace.addPoint(i, Math.random() + 1 * i);
    }
    Chart2DActionSaveImageSingleton saver = Chart2DActionSaveImageSingleton.getInstance(chart,
        "BLUE");
    saver.actionPerformed(null);

    final BufferedImage img = chart.snapShot();

    JFrame frame = new JFrame("testShanpshot()");
    JPanel imgPanel = new JPanel() {
      /** serialVersionUID */
      private static final long serialVersionUID = -1171046385909150778L;

      /**
       * @see javax.swing.JComponent#paint(java.awt.Graphics)
       */
      @Override
      public void paint(final Graphics g) {
        super.paint(g);
        g.drawImage(img, 0, 0, null);
      }
    };
    frame.getContentPane().add(imgPanel);
    frame.setSize(img.getWidth(), img.getHeight());
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    while (frame.isVisible()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Tests the policy of adding axis to charts.
   * <p>
   * 
   * Checks the old formatter of the x axis and adds a new x axis with a
   * different formatter: after the call the new axis should have the formatter
   * of the previous axis due to the replace semantics of the
   * {@link Chart2D#setAxisXBottom(AAxis, int)}.
   * <p>
   * 
   */
  @org.junit.Test
  public void testSetAxis() {
    Chart2D chart = new Chart2D();
    IAxisLabelFormatter oldFormatter = chart.getAxisX().getFormatter();
    AAxis<?> axis = new AxisLinear<IAxisScalePolicy>();
    IAxisLabelFormatter formatter = new LabelFormatterDate((SimpleDateFormat) DateFormat
        .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
    axis.setFormatter(formatter);
    chart.setAxisXBottom(axis, 0);
    Assert.assertSame(oldFormatter, chart.getAxisX().getFormatter());
  }

  /**
   * Test for bug 3352480: <br/>
   * Removing trace after z-index has changed does not work.
   * <p>
   * @org.junit.Test
   */
  public void testAddRemoveTraceAfterChangingZIndex() {
    Chart2D chart = new Chart2D();
    ITrace2D trace = new Trace2DSimple();
    chart.addTrace(trace);
    // add some more dummy traces to test the finding of trace within a set:
    for (int i = 0; i < 100; i++) {
      chart.addTrace(new Trace2DSimple());
    }
    trace.setZIndex(new Integer(33));
    boolean removed = chart.removeTrace(trace);
    assertTrue("The trace was not removed after changing z-index!", removed);

  }
  
}
