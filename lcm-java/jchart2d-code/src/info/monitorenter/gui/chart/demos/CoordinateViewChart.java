/*
 * CoordinateViewChart.java of project jchart2d, a demo that uses  
 * a ChartCoordinateView to display the position of the mouse over the chart. 
 *
 * Copyright (c) 2007 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
 */
package info.monitorenter.gui.chart.demos;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.io.AStaticDataCollector;
import info.monitorenter.gui.chart.io.PropertyFileStaticDataCollector;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartCoordinateView;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

/**
 * A demo that uses a
 * {@link info.monitorenter.gui.chart.views.ChartCoordinateView} to display the
 * position of the mouse over the chart.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.8 $
 */
public class CoordinateViewChart extends JFrame {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = 1195707820931595997L;

  /**
   * Demo application startup method.
   * <p>
   * 
   * @param args
   *          ignored.
   * @throws IOException
   *           if loading data for the demo chart fails.
   */
  public static void main(final String[] args) throws IOException {
    Chart2D chart = new Chart2D();
    chart.enablePointHighlighting(true);
    chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
    ITrace2D trace = new Trace2DLtd(400);

    AStaticDataCollector collector = new PropertyFileStaticDataCollector(trace,
        CoordinateViewChart.class.getResourceAsStream("data.properties"));
    chart.addTrace(trace);
    trace.setPointHighlighter(new PointPainterDisc(10));
    collector.collectData();
    new CoordinateViewChart(chart);

  }

  /** The chart to display and query for coordinates. */
  private Chart2D m_chart;

  /**
   * Creates an instance that displays the given chart and a
   * {@link ChartCoordinateView} to show the coordinates of the mouse cursor
   * over the chart.
   * <p>
   * 
   * @param chart
   *          the chart to display and sshow the coordinates of the mouse cursor
   *          over it.
   */
  public CoordinateViewChart(final Chart2D chart) {
    super("View coordinates");
    this.m_chart = chart;

    ChartCoordinateView viewChartValue = new ChartCoordinateView(this.m_chart);
    // listen for basic propery changes of the chart:
    new LayoutFactory.BasicPropertyAdaptSupport(viewChartValue, this.m_chart,LayoutFactory.BasicPropertyAdaptSupport.RemoveAsListenerFromComponentNever.getInstance());

    Container contentPane = this.getContentPane();
    contentPane.setLayout(new GridBagLayout());
    new LayoutFactory.BasicPropertyAdaptSupport(contentPane, this.m_chart,LayoutFactory.BasicPropertyAdaptSupport.RemoveAsListenerFromComponentNever.getInstance());

    // chart: use space
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.weightx = 1;
    gbc.weighty = 1.0;
    gbc.insets.bottom = 8;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.BOTH;
    contentPane.add(new ChartPanel(this.m_chart), gbc);

    // coordinate view: stay small
    gbc.gridy = 1;
    gbc.weighty = 0.0;
    gbc.insets.bottom = 0;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.fill = GridBagConstraints.NONE;
    contentPane.add(viewChartValue, gbc);

    this.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });

    this.setSize(new Dimension(400, 300));
    this.setVisible(true);

  }

}
