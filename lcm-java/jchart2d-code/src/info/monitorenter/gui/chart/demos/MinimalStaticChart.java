/*
 *  MinimalStaticChart.java of project jchart2d, a demonstration 
 *  of the minimal code to set up a chart with static data. 
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
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Title: MinimalStaticChart
 * <p>
 * 
 * Description: A minimal example for rendering a static chart.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.15 $
 */
public final class MinimalStaticChart extends JPanel {
  /**
   * Generated for <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3257009847668192306L;

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
      frame.getContentPane().add(new MinimalStaticChart());
      frame.addWindowListener(new WindowAdapter() {
        /**
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        @Override
        public void windowClosing(final WindowEvent e) {
          System.exit(0);
        }
      });
      frame.setSize(300, 300);
      frame.setLocation(i % 3 * 200, i / 3 * 100);
      frame.setVisible(true);
    }
  }

  /**
   * Defcon.
   */
  private MinimalStaticChart() {
    this.setLayout(new BorderLayout());
    Chart2D chart = new Chart2D();

    // Create an ITrace:
    // Note that dynamic charts need limited amount of values!!!
    // ITrace2D trace = new Trace2DLtd(200);
    ITrace2D trace = new Trace2DSimple();
    trace.setColor(Color.RED);

    // Add the trace to the chart:
    chart.addTrace(trace);

    // Add all points, as it is static:
    double time = System.currentTimeMillis();
    for (int i = 0; i < 120; i++) {
      trace.addPoint(time + 1000 * 60 * i, i);
    }

    chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);

    chart.getAxisY().setPaintScale(false);
    chart.getAxisX().setPaintScale(false);
    
    // Make it visible:
    this.add(new ChartPanel(chart), BorderLayout.CENTER);

  }

}
