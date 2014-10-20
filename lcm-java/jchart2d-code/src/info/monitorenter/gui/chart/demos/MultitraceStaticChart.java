/*
 *  MultitraceStaticChart.java of project jchart2d, a demonstration 
 *  of the minimal code to set up a chart with static data and several 
 *  traces. 
 *  Copyright (C) 2007 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A demonstration of the minimal code to set up a chart with static data and
 * several traces.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.9 $
 */
public final class MultitraceStaticChart
    extends JPanel {

  /**
   * Generated for <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 5364605038515831018L;

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    for (int i = 0; i < 1; i++) {
      JFrame frame = new JFrame("SampleChart");
      frame.getContentPane().add(new MultitraceStaticChart());
      frame.addWindowListener(new WindowAdapter() {
        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        @Override
        public void windowClosing(final WindowEvent e) {
          System.exit(0);
        }
      });
      frame.setSize(600, 600);
      frame.setLocation(i % 3 * 200, i / 3 * 100);
      frame.setVisible(true);
    }
  }

  /**
   * Defcon.
   * <p>
   */
  private MultitraceStaticChart() {
    this.setLayout(new BorderLayout());
    Chart2D chart = new Chart2D();


    // Create first ITrace:
    // Note that dynamic charts need limited amount of values!!!
    // ITrace2D trace = new Trace2DLtd(200);
    ITrace2D trace = new Trace2DSimple();
    trace.setColor(Color.RED);
    // Add the trace to the chart before adding any points / point highlighters:
    chart.addTrace(trace);

    // Feature: turn on tool tips that mark the nearest tracepoint: 
    chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
    // Feature: turn on highlightings that mark the nearest tracepoint: 
    chart.enablePointHighlighting(true);
    // also specify which highlighter to use for the trace!
    trace.setPointHighlighter(new PointPainterDisc(8));

    // Add all points, as it is static:
    double time = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
      trace.addPoint(time + i, i * Math.random());
    }

    // Create 2nd ITrace:
    trace = new Trace2DSimple();
    trace.setColor(Color.BLUE);
    // Add the trace to the chart before adding any points / point highlighters:
    chart.addTrace(trace);
    trace.setPointHighlighter(new PointPainterDisc(8));
    // Add all points, as it is static:
    for (int i = 0; i < 100; i++) {
      trace.addPoint(time + i, 200 - i * Math.random());
    }

    // Make it visible:
    this.add(new ChartPanel(chart), BorderLayout.CENTER);

  }

}
