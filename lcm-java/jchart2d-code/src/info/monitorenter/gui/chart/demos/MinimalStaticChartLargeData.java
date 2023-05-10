/*
 *  MinimalDynamicChartLargeData.java of project jchart2d, a demonstration 
 *  that on certain computer a chart can carry 70.000 data points. 
 *  Copyright (C) 2007 - 2011 Achim Westermann, created on 20.06.2007, 22:44:55
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

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JFrame;

/**
 * POC to show that "on some computers" jchart2d is able to show 70.000 data
 * points.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.7 $
 */
public final class MinimalStaticChartLargeData {

  /**
   * Testing main hook.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    // Create a chart:
    ZoomableChart chart = new ZoomableChart();
    // Create an ITrace:
    ITrace2D trace = new Trace2DSimple();
    // Add the trace to the chart:
    chart.addTrace(trace);
    // Add all points, as it is static:
    Random random = new Random();

    for (int i = 0; i < 70000; i++) {
      trace.addPoint(i, (60 + random.nextDouble()) * i);
    }
    // Make it visible:
    // Create a frame.
    JFrame frame = new JFrame("MinimalStaticChartLargeData");
    // add the chart to the frame:
    frame.getContentPane().add(new ChartPanel(chart));
    frame.setSize(800, 600);
    // Enable the termination button [cross on the upper right edge]:
    frame.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setVisible(true);
  }

  /**
   * Defcon.
   * <p>
   */
  private MinimalStaticChartLargeData() {
    super();
  }
}
