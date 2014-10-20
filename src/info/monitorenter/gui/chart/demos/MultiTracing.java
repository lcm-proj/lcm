/*
 * MultiTracing, a demo testing the thread- safetiness of the Chart2D.
 * Copyright (c) 2007 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
 */
package info.monitorenter.gui.chart.demos;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.util.Range;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * <p>
 * An example that tests the ability of multithreaded use of a single
 * <code>Chart2D</code>. Six different Threads are painting subsequently a
 * single point to the chart and go to a sleep. After having painted the whole
 * trace, each Thread sleeps for a random time, removes it's trace, sleeps for
 * another random time and starts again. <br>
 * To be true: the data for the <code>TracePoint</code> instances is computed a
 * single time at startup.
 * </p>
 * <p>
 * This test may blow your CPU. I am currently working on an AMD Athlon 1200,
 * 512 MB RAM so I did not get these problems.
 * </p>
 * 
 * @version $Revision: 1.13 $
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 */

public final class MultiTracing extends JFrame {
  /**
   * Thread that adds a trace to a chart, paints the points with configurable
   * sleep breaks and then removes it. It then goes to sleep and starts this
   * cycle anew.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.13 $
   */
  static final class AddPaintRemoveThread extends Thread {

    /** The y values to paint. */
    private double[] m_data;

    /** the chart to use. */
    private Chart2D m_innnerChart;

    /** The break the Thread takes between painting two points. */
    private long m_sleep;

    /** The trace to paint to. */
    private ITrace2D m_trace;

    /**
     * Creates an instance that paints data to the trace that is added to the
     * chart.
     * <p>
     * 
     * @param chart
     *          the chart to use.
     * 
     * @param trace
     *          the trace to add points to.
     * 
     * @param data
     *          the y values of the points to add.
     * 
     * @param sleep
     *          the length of the sleep break between painting points in ms.
     */
    public AddPaintRemoveThread(final Chart2D chart, final ITrace2D trace, final double[] data,
        final long sleep) {
      this.m_innnerChart = chart;
      this.m_trace = trace;
      this.m_trace.setName(this.getName());
      this.m_data = data;
      this.m_sleep = sleep;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      try {

        while (true) {

          if (Chart2D.DEBUG_THREADING) {
            System.out.println(this.getName() + "(" + Thread.currentThread().getName()
                + ") adding trace.");
          }
          this.m_innnerChart.addTrace(this.m_trace);
          for (int i = 0; i < this.m_data.length; i++) {
            if (Chart2D.DEBUG_THREADING) {
              System.out.println("Thread "+ this.getName() + " adding point to " + this.m_trace.getName());
            }
            this.m_trace.addPoint(i, this.m_data[i]);
            try {
              Thread.sleep(this.m_sleep);
            } catch (InterruptedException e) {
              e.printStackTrace(System.err);
            }

          }
          try {
            Thread.sleep((long) (Math.random() * this.m_sleep));
          } catch (InterruptedException e) {
            e.printStackTrace(System.err);
          }
          if (Chart2D.DEBUG_THREADING) {
            System.out.println(this.getName() + "(" + Thread.currentThread().getName()
                + ") removing trace.");
          }
          this.m_innnerChart.removeTrace(this.m_trace);
          this.m_trace.removeAllPoints();

          try {
            Thread.sleep((long) (Math.random() * this.m_sleep));
          } catch (InterruptedException e) {
            e.printStackTrace(System.err);
          }
        }
      } catch (Throwable f) {
        f.printStackTrace(System.err);
      }
    }
  }

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3256722879394820657L;

  /** Sleep break time between adding two points. */
  private static final int SLEEP = 100;

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    /*
     * [[xa 1,..,xa n],..,[xf 1,...,xf n]]
     */
    final double[][] data = new double[6][200];
    final java.util.Random rand = new java.util.Random();
    // first traces data
    // recursive entry:
    data[0][0] = rand.nextDouble() * 5;
    for (int i = 1; i < data[0].length; i++) {
      data[0][i] = (rand.nextDouble() < 0.5) ? data[0][i - 1] + rand.nextDouble() * 5
          : data[0][i - 1] - rand.nextDouble() * 5;
    }
    // second trace
    double tmp;
    for (int i = 0; i < data[0].length; i++) {
      tmp = Math.pow(Math.E, ((double) i) / 40) + (Math.random() < 0.5 ? data[0][i] : -data[0][i]);
      data[1][i] = tmp;
    }
    // third trace
    for (int i = 0; i < data[0].length; i++) {
      data[2][i] = Math.pow(Math.cos(((double) i) / 10) * 5, 2);
    }
    // fourth trace: numerical integration of fist trace's previous entries.
    // recursive entry:
    data[3][0] = data[0][0];
    tmp = 0;
    for (int i = 1; i < data[0].length; i++) {
      for (int j = Math.max(0, i - 10); j <= i; j++) {
        tmp += data[0][j];
      }
      data[3][i] = tmp / (((double) i) + 1);
      tmp = 0;
    }
    // fifth trace addition of second trace and third trace
    for (int i = 0; i < data[0].length; i++) {
      data[4][i] = data[1][i] + data[2][i] * (0.1 * -data[0][i]);
    }
    // sixth trace: addition of first and second trace
    for (int i = 0; i < data[0].length; i++) {
      data[5][i] = data[0][i] + data[2][i];
    }

    final MultiTracing wnd = new MultiTracing();
    // wnd.setForceXRange(new Range(0, data[0].length + 10));
    // wnd.setForceYRange(MultiTracing.getRange(data));
    wnd.setLocation(100, 300);
    wnd.setSize(800, 300);
    wnd.setResizable(true);
    wnd.setVisible(true);

    ITrace2D trace;
    // first Thread:
    trace = new Trace2DSimple();
    trace.setColor(Color.red);
    new AddPaintRemoveThread(wnd.m_chart, trace, data[0], MultiTracing.SLEEP).start();
    // second Thread:
    trace = new Trace2DSimple();
    trace.setColor(Color.green);
    new AddPaintRemoveThread(wnd.m_chart, trace, data[1], (long) (MultiTracing.SLEEP * 1.5))
        .start();
    // third Thread:
    trace = new Trace2DSimple();
    trace.setColor(Color.blue);
    new AddPaintRemoveThread(wnd.m_chart, trace, data[2], (MultiTracing.SLEEP * 2)).start();

    // fourth Thread:
    trace = new Trace2DSimple();
    trace.setColor(Color.cyan);
    new AddPaintRemoveThread(wnd.m_chart, trace, data[3], (long) (MultiTracing.SLEEP * 2.5))
        .start();
    // fifth Thread:
    trace = new Trace2DSimple();
    trace.setColor(Color.black);
    new AddPaintRemoveThread(wnd.m_chart, trace, data[4], (MultiTracing.SLEEP * 3)).start();
    // sixth Thread:
    trace = new Trace2DSimple();
    trace.setColor(Color.white);
    new AddPaintRemoveThread(wnd.m_chart, trace, data[5], (long) (MultiTracing.SLEEP * 3.5))
        .start();

  }

  /** The chart to fill. */
  protected Chart2D m_chart = null;

  /** Defcon. */
  public MultiTracing() {
    super("MultiTracing");
    this.m_chart = new Chart2D();
    this.m_chart.getAxisX().setPaintGrid(true);
    this.m_chart.getAxisY().setPaintGrid(true);
    this.m_chart.setBackground(Color.lightGray);
    this.m_chart.setGridColor(new Color(0xDD, 0xDD, 0xDD));
    // add WindowListener
    this.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(new ChartPanel(this.m_chart), BorderLayout.CENTER);
  }

  /**
   * Enforces to display a certain visible x range that will be expanded if
   * traces in the chart have higher or lower values.
   * <p>
   * 
   * @param forceXRange
   *          the range that at least has to be kept visible.
   */
  public void setForceXRange(final Range forceXRange) {
    this.m_chart.getAxisX().setRangePolicy(new RangePolicyMinimumViewport(forceXRange));
  }

  /**
   * Enforces to display a certain visible x range that will be expanded if
   * traces in the chart have higher or lower values.
   * <p>
   * 
   * @param forceYRange
   *          the range that at least has to be kept visible.
   */
  public void setForceYRange(final Range forceYRange) {
    this.m_chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(forceYRange));
  }
}
