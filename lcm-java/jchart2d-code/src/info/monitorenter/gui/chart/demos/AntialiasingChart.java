/*
 * RunningChart, a test for the Chart2D.
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
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.painters.TracePainterPolyline;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.reflection.ObjRecorder2Trace2DAdapter;
import info.monitorenter.util.Range;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * A test for the <code>Chart2D</code> that constantly adds new tracepoints to a
 * <code> Trace2DLtd</code>. Mainly the runtime- scaling is interesting.
 * <p>
 * Furthermore this is an example on how to connect other components to the
 * <code>Chart2D</code> using an adaptor- class. If interested have a look on
 * {@link info.monitorenter.reflection.ObjRecorder2Trace2DAdapter}.
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'> Achim Westermann </a>
 * @version $Revision: 1.5 $
 */
public class AntialiasingChart extends JFrame {
  /**
   * Helper class that holds an internal number that is randomly modified by a
   * Thread.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * @version $Revision: 1.5 $
   */
  static class RandomBumper extends Thread {
    /** Streches or compresses the grade of jumping of the internal number. */
    protected double m_factor;

    /** The bumping number. */
    protected double m_number = 0;

    /** The propability of an increase versus a decrease of the bumped number. */
    protected double m_plusminus = 0.5;

    /** Needed for randomization of bumping the number. */
    protected java.util.Random m_randomizer = new java.util.Random();

    /**
     * Creates an instance.
     * <p>
     * 
     * @param plusminus
     *          probability to increase or decrease the number each step.
     * @param factor
     *          affects the amplitude of the number (severity of jumps).
     */
    public RandomBumper(final double plusminus, final int factor) {

      if (plusminus < 0 || plusminus > 1) {
        System.out.println(this.getClass().getName()
            + " ignores constructor-passed value. Must be between 0.0 and 1.0!");
      } else {
        this.m_plusminus = plusminus;
      }
      this.m_factor = factor;
      this.setDaemon(true);
      this.start();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

      while (true) {
        double rand = this.m_randomizer.nextDouble();
        if (rand < this.m_plusminus) {
          this.m_number += this.m_randomizer.nextDouble() * this.m_factor;
        } else {
          this.m_number -= this.m_randomizer.nextDouble() * this.m_factor;
        }

        try {
          Thread.sleep(20);
        } catch (InterruptedException e) {
          // nop
        }

      }
    }
  }

  /**
   * Generated for <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3545231432038627123L;

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {

    Chart2D chart = new Chart2D();
    chart.setUseAntialiasing(true);
    chart.setMinPaintLatency(20);
    ITrace2D data = new Trace2DLtd(300);
    data.setStroke(new BasicStroke(3));
    data.setColor(new Color(255, 0, 0, 255));
    data.setName("random");
    data.setPhysicalUnits("hertz", "ms");

    ITracePainter<?> dotPainter = new TracePainterPolyline();
    data.setTracePainter(dotPainter);
    chart.addTrace(data);

    AntialiasingChart wnd = new AntialiasingChart(chart, "AntialiasingChart");
    chart.getAxisX().setPaintGrid(true);
    chart.getAxisX().setStartMajorTick(false);
    chart.getAxisY().setPaintGrid(true);

    chart.getAxisX().setPaintScale(true);
    chart.getAxisX().setPaintScale(true);

    // force ranges:
    chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(-1e4, +1e4)));
    // chart.setFont(new Font(null,0,12));
    wnd.setLocation(200, 300);
    wnd.setSize(700, 210);
    wnd.setResizable(true);
    wnd.setVisible(true);
    new ObjRecorder2Trace2DAdapter(data, new RandomBumper(0.5, 1000), "m_number", 1000);
  }

  /** The chart to use. */
  protected Chart2D m_chart = null;

  /**
   * Creates an instance that will dynamically paint on the chart to a trace
   * with the given label.
   * <p>
   * 
   * @param chart
   *          the chart to use.
   * @param label
   *          the name of the trace too display.
   */
  public AntialiasingChart(final Chart2D chart, final String label) {

    super(label);
    this.m_chart = chart;
    this.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {

        AntialiasingChart.this.setVisible(false);
        AntialiasingChart.this.dispose();
        System.exit(0);

      }
    });
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.add(new ChartPanel(this.m_chart), BorderLayout.CENTER);
  }
}
