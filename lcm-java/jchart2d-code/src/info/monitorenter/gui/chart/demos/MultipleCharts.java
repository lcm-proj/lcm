/*
 *  MultipleCharts.java of project jchart2d, a demonstration 
 *  of the minimal code to display multiple charts in a window. 
 *  Copyright (C) 2007 - 2011 Achim Westermann, created on 01.08.2006, 19:31:55
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
import info.monitorenter.gui.chart.io.ADataCollector;
import info.monitorenter.gui.chart.io.RandomDataCollectorOffset;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Demonstrates minimal effort to create multiple charts in one window.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public final class MultipleCharts {

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored
   */
  public static void main(final String[] args) {

    // Make it visible:
    // Create a frame.
    JFrame frame = new JFrame("MinimalDynamicChart");
    // add the chart to the frame:
    frame.setSize(500, 400);
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
    Container contentPane = frame.getContentPane();
    contentPane.setLayout(new GridLayout(2, 2));

    // add 4 charts with different update speed:
    Chart2D chart;
    ITrace2D trace;
    ADataCollector collector;
    Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA };
    Chart2D[] charts = new Chart2D[4];
    for (int i = 1; i < 5; i++) {
      // Create a chart:
      chart = new Chart2D();
      charts[i - 1] = chart;
      // Create an ITrace:
      // Note that dynamic charts need limited amount of values!!!
      trace = new Trace2DLtd(400);
      trace.setColor(colors[i - 1]);
      // Every 50 * i milliseconds a new value is collected.
      collector = new RandomDataCollectorOffset(trace, 70 * i);
      
      // Add the trace to the chart:
      chart.addTrace(trace);
      // collector only may be started after the trace is connected to a chart (deadlock prevention). 
      collector.start();
      contentPane.add(new ChartPanel(chart));
    }
    frame.setVisible(true);
  }

  /** Defcon. */
  private MultipleCharts() {
    // nop
  }
}
