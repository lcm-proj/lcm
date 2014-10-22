/*
 *  TraceTester, an incredible old but remarkable weird visual test for 
 *  the automatic scaling routines of jchart2d.
 * 
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
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.TracePoint2D;
import info.monitorenter.gui.chart.traces.Trace2DBijective;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DLtdReplacing;
import info.monitorenter.gui.chart.traces.Trace2DLtdSorted;
import info.monitorenter.gui.chart.traces.Trace2DReplacing;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.Trace2DSorted;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * A test class that steps through all <code>ITrace2D</code> implementations and
 * adds random or "half- random" <code>TracePoint2D</code> -instances.
 * <p>
 * 
 * You may see, that not all <code>ITrace2D</code> implementations work as
 * proposed (Trace2DLimitedReplacing). This will be fixed.
 * <p>
 * 
 * @author Achim Westermann <a
 *         href='mailto:Achim.Westermann@gmx.de'>Achim.Westermann@gmx.de </a>
 * 
 * @version $Revision: 1.8 $
 */
public final class TraceTester {

  /**
   * Generates "half random" points.
   * <p>
   * Unlike random points half random points are not totally random samples
   * within the given bounds but repeat the same x value 10 times.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.8 $
   */
  static class HalfRandomPoints extends TraceTester.RandomPoints {

    /** The old x value. */
    private double m_oldx = 0;

    /**
     * Defines how often the same x value is returned from subsequent calls to
     * {@link #nextPoint()}.
     */
    private double m_samexcount = 0;

    /**
     * Creates a half random point generator that will create random points
     * within ([minx..minx+maxx], [miny..miny+maxy]).
     * <p>
     * 
     * @param minx
     *          the lower x bound for points to generate.
     * 
     * @param maxx
     *          the x range for points to generate.
     * 
     * @param miny
     *          the lower y bound for points to generate.
     * 
     * @param maxy
     *          the y range for points to generate.
     * 
     */
    HalfRandomPoints(final int minx, final int maxx, final int miny, final int maxy) {
      super(minx, maxx, miny, maxy);
      this.m_oldx = (maxx - minx) / 2.0;
    }

    /**
     * @see info.monitorenter.gui.chart.demos.TraceTester.RandomPoints#nextPoint()
     */
    @Override
    public ITracePoint2D nextPoint() {
      if (this.m_samexcount == 10) {
        this.m_samexcount = 0;
        this.m_oldx = this.m_rand.nextDouble() * this.m_xrange + this.m_xmin;
      }
      this.m_samexcount++;
      return new TracePoint2D(this.m_oldx, this.m_rand.nextDouble() * this.m_yrange + this.m_ymin);
    }
  }

  /**
   * Helper that creates random points.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.8 $
   */
  private static class RandomPoints {
    /** Used for randomization. */
    protected java.util.Random m_rand = new java.util.Random();

    /** Minimum bound for x. */
    protected double m_xmin;

    /** Range for x. */
    protected double m_xrange;

    /** Minimum bound for y. */
    protected double m_ymin;

    /** Range for y. */
    protected double m_yrange;

    /**
     * Creates a random point generator that will create random points within
     * ([minx..minx+maxx], [miny..miny+maxy]).
     * <p>
     * 
     * @param minx
     *          the lower x bound for points to generate.
     * 
     * @param maxx
     *          the x range for points to generate.
     * 
     * @param miny
     *          the lower y bound for points to generate.
     * 
     * @param maxy
     *          the y range for points to generate.
     * 
     */
    RandomPoints(final int minx, final int maxx, final int miny, final int maxy) {
      if (minx >= maxx) {
        throw new IllegalArgumentException("minx>=maxx!");
      }
      if (miny >= maxy) {
        throw new IllegalArgumentException("miny>=maxy!");
      }
      this.m_xmin = minx;
      this.m_xrange = maxx - minx;
      this.m_ymin = miny;
      this.m_yrange = maxy - miny;
    }

    /**
     * Returns the next random point.
     * <p>
     * 
     * @return the next random point.
     */
    public ITracePoint2D nextPoint() {
      return new TracePoint2D(this.m_rand.nextDouble() * this.m_xrange + this.m_xmin, this.m_rand
          .nextDouble()
          * this.m_yrange + this.m_ymin);
    }

  }

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  @SuppressWarnings("unchecked")
  public static void main(final String[] args) {
    try {
      Class< ? extends ITrace2D>[] traces = new Class[] {Trace2DSimple.class,
          Trace2DBijective.class, Trace2DReplacing.class, Trace2DSorted.class, Trace2DLtd.class,
          Trace2DLtdReplacing.class, Trace2DLtdSorted.class };
      RandomPoints rand = new RandomPoints(0, 3, 0, 3);

      Chart2D test = new Chart2D();
      JFrame frame = new JFrame("TraceTester");
      frame.addWindowListener(new WindowAdapter() {
        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        @Override
        public void windowClosing(final WindowEvent e) {
          System.exit(0);
        }
      });

      frame.getContentPane().add(test);
      frame.setSize(600, 500);
      frame.setLocation(200, 200);
      frame.setVisible(true);
      ITrace2D current = null;
      for (int i = 0; i < traces.length; i++) {
        current = traces[i].newInstance();
        test.addTrace(current);
        frame.setTitle("TraceTester: full-random, current: " + traces[i].getName());

        for (int j = 0; j < 200; j++) {
          current.addPoint(rand.nextPoint());
          Thread.sleep(50);
        }
        Thread.sleep(2000);
        test.removeTrace(current);
      }
      rand = new HalfRandomPoints(0, 3, 0, 3);
      for (int i = 0; i < traces.length; i++) {
        current = traces[i].newInstance();
        test.addTrace(current);
        frame.setTitle("TraceTester: repeating x 10 times, current: " + traces[i].getName());

        for (int j = 0; j < 200; j++) {
          current.addPoint(rand.nextPoint());
          Thread.sleep(50);
        }
        Thread.sleep(2000);
        test.removeTrace(current);
      }

      System.exit(0);

    } catch (Throwable f) {
      f.printStackTrace();
      System.exit(0);
    }
  }

  /**
   * Defcon.
   * <p>
   */
  private TraceTester() {
    // nop
  }

}
