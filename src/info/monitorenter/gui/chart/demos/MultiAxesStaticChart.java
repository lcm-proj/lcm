/*
 *  MultipleAxesStaticChart.java of project jchart2d, a demonstration 
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
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.ITracePointProvider;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Title: MultipleAxesStaticChart
 * <p>
 * Description: A minimal example for rendering a static chart with multiple
 * axes.
 * <p>
 * 
 * @author Achim Westermann
 * @version $Revision: 1.15 $
 */
public final class MultiAxesStaticChart extends JPanel {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 3476998470009995195L;

  /**
   * Main entry.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    for (int i = 0; i < 1; i++) {
      JFrame frame = new JFrame(MultiAxesStaticChart.class.getName());
      frame.getContentPane().add(new MultiAxesStaticChart());
      frame.addWindowListener(new WindowAdapter() {
        /**
         * 
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
   */
  private MultiAxesStaticChart() {
    this.setLayout(new BorderLayout());
    Chart2D chart = new Chart2D();
    chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
    // This is not important: We do this just because we want to add points
    // before
    // configuring the axes and add the traces to the chart:
    ITracePointProvider pointCreator = chart.getTracePointProvider();

    // Create apples trace:
    ITrace2D apples = new Trace2DSimple();
    apples.setColor(Color.RED);
    apples.setName("Apples");


    // Create pears trace:
    ITrace2D pears = new Trace2DSimple();
    pears.setColor(Color.BLUE);
    pears.setName("Pears");

    // Create carrots trace:
    ITrace2D carrots = new Trace2DSimple();
    carrots.setColor(Color.MAGENTA);
    carrots.setName("Carrots");

    // Use three y axes:
    AAxis<?> yAxisApples = new AxisLinear<IAxisScalePolicy>();
    yAxisApples.getAxisTitle().setTitle("Y-Apples");
    yAxisApples.getAxisTitle().setTitleColor(Color.RED);

    AAxis<?> yAxisPears = new AxisLinear<IAxisScalePolicy>();
    yAxisPears.getAxisTitle().setTitle("Y-Pears");
    yAxisPears.getAxisTitle().setTitleColor(Color.BLUE);

    AAxis<?> yAxisCarrots = new AxisLinear<IAxisScalePolicy>();
    yAxisCarrots.getAxisTitle().setTitle("Y-Carrots");
    yAxisCarrots.getAxisTitle().setTitleColor(Color.MAGENTA);

    // Add these axes to the chart:
    chart.setAxisYLeft(yAxisApples, 0);
    chart.setAxisYRight(yAxisPears, 0);
    chart.addAxisYLeft(yAxisCarrots);

    // use three x axes:
    AAxis<?> xAxisApples = new AxisLinear<IAxisScalePolicy>();
    xAxisApples.getAxisTitle().setTitle("X-Apples");
    xAxisApples.getAxisTitle().setTitleColor(Color.RED);

    AAxis<?> xAxisPears = new AxisLinear<IAxisScalePolicy>();
    xAxisPears.getAxisTitle().setTitle("X-Pears");
    xAxisPears.getAxisTitle().setTitleColor(Color.BLUE);

    AAxis<?> xAxisCarrots = new AxisLinear<IAxisScalePolicy>();
    xAxisCarrots.getAxisTitle().setTitle("X-Carrots");
    xAxisCarrots.getAxisTitle().setTitleColor(Color.MAGENTA);

    // Add these axes to the chart:
    chart.setAxisXBottom(xAxisApples, 0);
    chart.setAxisXTop(xAxisPears, 0);
    chart.addAxisXBottom(xAxisCarrots);

    // TODO: Also test adding the traces directly to the axes!
    // add the traces to the chart:
    chart.addTrace(apples, xAxisApples, yAxisApples);
    chart.addTrace(pears, xAxisPears, yAxisPears);
    chart.addTrace(carrots, xAxisCarrots, yAxisCarrots);
    
    
    // Only the trace is assigned to the chart points may be added! 
    // Add all points, as it is static:
    double time = System.currentTimeMillis();
    int i;
    for (i = 0; i < 120; i++) {
      apples.addPoint(pointCreator.createTracePoint(time + i * 10000, (10.0 + Math.random()) * i));
    }
    // add pears:
    ITracePoint2D copyPoint;
    Iterator<ITracePoint2D> it = apples.iterator();
    i = 0;
    while (it.hasNext()) {
      copyPoint = it.next();
      pears.addPoint(pointCreator.createTracePoint(i * 0.001, copyPoint.getY()
          + (Math.random() * 1000)));
      i++;
    }
    // add carrots:
    it = apples.iterator();
    i = 0;
    while (it.hasNext()) {
      copyPoint = it.next();
      carrots.addPoint(pointCreator.createTracePoint(i * 100, 100 - copyPoint.getY()));
      i++;
    }




    // Make it visible:
    this.add(new ChartPanel(chart), BorderLayout.CENTER);

  }

}
