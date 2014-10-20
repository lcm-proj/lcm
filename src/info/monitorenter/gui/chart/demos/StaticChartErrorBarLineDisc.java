/*
 *  StaticChartErrorBarLineDisc.java of project jchart2d, a demonstration 
 *  of the minimal code to set up a chart with static data and 
 *  an relative error bar policy that paints error bars by lines only. 
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
import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.errorbars.ErrorBarPainter;
import info.monitorenter.gui.chart.errorbars.ErrorBarPolicyAbsoluteSummation;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.pointpainters.PointPainterLine;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Title: StaticChartErrorBarLineDisc
 * <p>
 * 
 * Description: A demonstration of the minimal code to set up a chart with
 * static data and an relative error bar policy that paints relative error bars
 * by line segments with a disc at the end in y dimension (positive and
 * negative).
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.13 $
 */
public final class StaticChartErrorBarLineDisc extends JPanel {
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
      JFrame frame = new JFrame(StaticChartErrorBarLineDisc.class.getName());
      frame.getContentPane().add(new StaticChartErrorBarLineDisc());
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
   */
  private StaticChartErrorBarLineDisc() {
    this.setLayout(new BorderLayout());
    Chart2D chart = new Chart2D();

    // Create an ITrace:
    // Note that dynamic charts need limited amount of values!!!
    ITrace2D trace = new Trace2DSimple();
    // Add the trace to the chart:
    chart.addTrace(trace);
    trace.setColor(Color.RED);
    // create an error bar policy and configure it
    IErrorBarPolicy< ? > errorBarPolicy = new ErrorBarPolicyAbsoluteSummation(10, 10);
    errorBarPolicy.setShowNegativeYErrors(true);
    errorBarPolicy.setShowPositiveYErrors(true);
    // errorBarPolicy.setShowNegativeXErrors(true);
    // errorBarPolicy.setShowPositiveXErrors(true);
    trace.setErrorBarPolicy(errorBarPolicy);
    // configure how error bars are rendered with an error bar painter:
    IErrorBarPainter errorBarPainter = new ErrorBarPainter();
    errorBarPainter.setEndPointPainter(new PointPainterDisc());
    errorBarPainter.setEndPointColor(Color.GRAY);
    errorBarPainter.setConnectionPainter(new PointPainterLine());
    errorBarPainter.setConnectionColor(Color.LIGHT_GRAY);
    // add the painter to the policy
    errorBarPolicy.setErrorBarPainter(errorBarPainter);

    // Fini: now we have configured how error bars look,
    // which parts they render (end point, start point, segment), in which
    // direction they should extend and what kind of error is calculated.

    // Add all points, as it is static:
    for (double i = 2; i < 40; i++) {
      trace.addPoint(i * 100, Math.random() * i + i * 10);
    }

    // Make it visible:
    this.add(new ChartPanel(chart), BorderLayout.CENTER);

  }

}
