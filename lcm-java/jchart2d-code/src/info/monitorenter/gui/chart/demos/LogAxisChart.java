/*
 *
 *  LogAxisChart.java, rendering demo of jchart2d.
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
import info.monitorenter.gui.chart.axis.AxisLogE;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyTransformation;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * A demo chart that uses a logarithmic axis for Y ({@link info.monitorenter.gui.chart.axis.AxisLog10})
 * and a trace painter for discs ({@link info.monitorenter.gui.chart.traces.painters.TracePainterDisc}).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.10 $
 * 
 */
public final class LogAxisChart {

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
    // set a special axis:
    chart.setAxisYLeft(new AxisLogE<AxisScalePolicyTransformation>(), 0);

    // Create an ITrace:
    ITrace2D trace = new Trace2DSimple("exponential");
    // Add the trace to the chart:
    chart.addTrace(trace);
    // configure trace:
    trace.setTracePainter(new TracePainterDisc());
    trace.setColor(Color.DARK_GRAY);
    // Add the function 1/x + random
    for (int i = 1; i < 50; i++) {
      trace.addPoint(i, Math.exp(i));
    }
    
    ITrace2D trace2 = new Trace2DLtd("linear");
    trace2.setTracePainter(new TracePainterDisc());
    trace2.setColor(Color.BLUE);
    chart.addTrace(trace2);
    for (int i = 1; i < 50; i++) {
      trace2.addPoint(i, i);
    }
    

    // Make it visible:
    // Create a frame.
    JFrame frame = new JFrame(LogAxisChart.class.getName());
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
  private LogAxisChart() {
    // nop
  }
}
