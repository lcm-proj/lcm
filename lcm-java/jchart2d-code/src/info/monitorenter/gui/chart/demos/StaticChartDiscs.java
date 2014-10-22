/*
 *
 *  StaticChartDiscs.java, rendering demo of jchart2d.
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
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * A demo chart that uses a
 * {@link info.monitorenter.gui.chart.traces.painters.TracePainterDisc}.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.6 $
 * 
 */
public final class StaticChartDiscs {

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    // Create a chart:
    Chart2D chart = new Chart2D();

    // Create an ITrace:
    ITrace2D trace = new Trace2DSimple();
    // Add the trace to the chart:
    chart.addTrace(trace);
    trace.setTracePainter(new TracePainterDisc());
    trace.setColor(Color.DARK_GRAY);
    // Add all points, as it is static:
    double count = 0;
    double value;
    double place = 0;
    for (int i = 120; i >= 0; i--) {
      count += 1.0;
      place += 1.0;
      value = Math.random() * count * 10;
      trace.addPoint(place, value);
    }

    // Make it visible:
    // Create a frame.
    JFrame frame = new JFrame("StaticChartDiscs");
    // add the chart to the frame:
    frame.getContentPane().add(new ChartPanel(chart));
    frame.setSize(400, 300);
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
  private StaticChartDiscs() {
    // nop
  }
}
