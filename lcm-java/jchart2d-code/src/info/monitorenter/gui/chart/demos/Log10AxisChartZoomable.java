/*
 *  Log10AxisChart.java, A demo chart that uses a logarithmic axis for Y.
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
import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.axis.AxisLog10;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyTransformation;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterSimple;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.JFrame;

/**
 * A demo chart that uses a logarithmic axis for Y (
 * {@link info.monitorenter.gui.chart.axis.AxisLog10}) and a trace painter for
 * discs ({@link info.monitorenter.gui.chart.traces.painters.TracePainterDisc}).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.11 $
 * 
 */
public final class Log10AxisChartZoomable {

  /**
   * Creates a zoomable chart with y axis log 10, simple label formatter, gridlines, acitvated highlighting and activated tooltips. 
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    // Create a chart:
    Chart2D chart = new ZoomableChart();
    chart.setUseAntialiasing(true);
    chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
    chart.enablePointHighlighting(true);
    // set a special axis:
    AxisLog10<AxisScalePolicyTransformation> axisy = new AxisLog10<AxisScalePolicyTransformation>();
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(100);
    chart.setAxisYLeft(axisy, 0);
    axisy.setFormatter(new LabelFormatterSimple());
    axisy.setPaintGrid(true);
    
    chart.getAxisX().setPaintGrid(true);

    // Create an ITrace:
    ITrace2D trace = new Trace2DSimple();
    // Add the trace to the chart:
    chart.addTrace(trace);
    trace.addPointHighlighter(new PointPainterDisc(10));
    trace.setTracePainter(new TracePainterDisc(4));
    trace.setColor(Color.BLUE);
    trace.setStroke(new BasicStroke(1));
    // Add the function 1/x + random
    for (double i = 1; i <= 10; i += 0.1) {
      trace.addPoint(i, Math.pow(10, i));
    }

    // Make it visible:
    // Create a frame.
    JFrame frame = new JFrame(Log10AxisChartZoomable.class.getName());
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
  private Log10AxisChartZoomable() {
    // nop
  }
}
