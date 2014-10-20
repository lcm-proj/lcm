/*
 *  ZoomTest.java of project jchart2d, demonstration of a zoom-enabled 
 *  chart with error bars and a logarithmic y axis. 
 *  Copyright 2007 - 2011 (C) Achim Westermann, created on 23:59:21.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart.demos;

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.axis.AxisLogE;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyTransformation;
import info.monitorenter.gui.chart.errorbars.ErrorBarPainter;
import info.monitorenter.gui.chart.errorbars.ErrorBarPolicyAbsoluteSummation;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Demonstration of a zoom - enabled chart (
 * {@link info.monitorenter.gui.chart.ZoomableChart}) with error bars and an
 * <code>{@link AxisLogE}</code> for the y axis.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.14 $
 */
public class ZoomChartWithErrorBarsAndLogAxis extends JFrame {

  /**
   * Action adapter for zoomAllButton.
   * <p>
   */
  class ZoomAllAdapter implements ActionListener {
    /** The zoomable chart to act upon. */
    private ZoomableChart m_zoomableChart;

    /**
     * Creates an instance that will reset zooming on the given zoomable chart
     * upon the triggered action.
     * <p>
     * 
     * @param chart
     *          the target to reset zoomin on.
     */
    public ZoomAllAdapter(final ZoomableChart chart) {
      this.m_zoomableChart = chart;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent event) {
      this.m_zoomableChart.zoomAll();
    }
  }

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = 1497610918434714345L;

  /**
   * Main startup method.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {

    ZoomChartWithErrorBarsAndLogAxis zoomTest = new ZoomChartWithErrorBarsAndLogAxis();
    // Show the frame
    zoomTest.setSize(640, 480);
    zoomTest.setVisible(true);

  }

  /**
   * Defcon.
   * <p>
   */
  public ZoomChartWithErrorBarsAndLogAxis() {

    super("ZoomableChart with log y axis and errorbars");

    Container c = this.getContentPane();
    c.setLayout(new BorderLayout());

    // Create a chart
    ZoomableChart chart = new ZoomableChart();

    // Create ITrace
    ITrace2D trace = new Trace2DSimple("Trace");
    trace.setColor(Color.BLUE);
    // Add the trace to the chart before adding any point / point highlighters / error bar policies! 
    chart.addTrace(trace);
    IErrorBarPolicy< ? > errorBarPolicy = new ErrorBarPolicyAbsoluteSummation(4, 4);
    errorBarPolicy.setShowPositiveYErrors(true);
    IErrorBarPainter painter = new ErrorBarPainter();
    errorBarPolicy.addErrorBarPainter(painter);
    trace.addErrorBarPolicy(errorBarPolicy);

    chart.setAxisYLeft(new AxisLogE<AxisScalePolicyTransformation>(), 0);

    // Add all points, as it is static
    for (int i = 1; i < 100; i++) {
      trace.addPoint(i, i);
    }

    // Add chart to the pane
    c.add(new ChartPanel(chart));

    // Create the zoomAll button
    JButton zoomAllButton = new JButton("Zoom All");
    zoomAllButton.addActionListener(new ZoomAllAdapter(chart));

    // Add zoomAll button to the pane
    c.add(zoomAllButton, BorderLayout.NORTH);

    // Enable the termination button:
    this.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });

  }
}
