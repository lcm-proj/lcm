/*
 * ViewChartValue.java of project jchart2d, a view that displays the data value
 *  Copyright (C) 2008 - 2011 Achim Westermann.
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
package info.monitorenter.gui.chart.views;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A view that displays the data value of the point the mouse pointer currently
 * is over the Chart2D component within two {@link javax.swing.JTextField}
 * instances.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.10 $
 */
public class ChartCoordinateView extends JPanel {

  /**
   * Generated <code>serial version UID</code>.
   */
  private static final long serialVersionUID = 2547926983897553336L;

  /** The chart to display the values from. */
  protected Chart2D m_chart2D;

  /** The x value view. */
  protected JTextField m_xView;

  /** The y value view. */
  protected JTextField m_yView;

  /**
   * Handle to the mouse listener that is registered to the chart component.
   * Needed to remove it from the chart when this component dies.
   * <p>
   */
  private transient final MouseMotionListener m_mouseListener;

  /**
   * Creates an component that will contain two text fields that display the
   * chart value (x,y) when moving the mouse over the chart component.
   * <p>
   * 
   * @param chart
   *          the chart to listen to for mouse events.
   */
  public ChartCoordinateView(final Chart2D chart) {
    this.m_chart2D = chart;
    this.m_xView = new JTextField(10);
    this.m_xView.setEditable(false);
    this.m_yView = new JTextField(10);
    this.m_yView.setEditable(false);

    this.m_mouseListener = new MouseMotionAdapter() {
      /**
       * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
       */
      @Override
      public void mouseMoved(final MouseEvent me) {
        final ITracePoint2D value = ChartCoordinateView.this.m_chart2D.translateMousePosition(me);
        if (value != null) {
          ChartCoordinateView.this.m_xView.setText(ChartCoordinateView.this.m_chart2D.getAxisX()
              .getFormatter().format(value.getX()));
          ChartCoordinateView.this.m_yView.setText(ChartCoordinateView.this.m_chart2D.getAxisY()
              .getFormatter().format(value.getY()));
        }
      }
    };
    this.m_chart2D.addMouseMotionListener(this.m_mouseListener);

    // layout: GridBagLayout as I was unable to respect the preferred size
    // of visible components and use box glue for expansion (I did this once and
    // they say everything is possible with nexted BoxLayout but I forgot how
    // and there is no use in things that don't stick in mind.
    this.setLayout(new GridBagLayout());

    // First "row"
    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets.left = 4;
    gbc.insets.right = 4;
    gbc.insets.top = 4;
    gbc.insets.bottom = 4;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0;
    gbc.gridheight = 1;
    gbc.weighty = 0;
    this.add(new JLabel("X value:"), gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.2;
    this.add(Box.createHorizontalStrut(4));

    gbc.gridx = 2;
    gbc.weightx = 0;
    this.add(this.m_xView, gbc);

    // second "row"
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 1;
    this.add(new JLabel("Y value:"), gbc);

    gbc.weightx = 0.2;
    gbc.gridx = 1;
    this.add(Box.createHorizontalStrut(4));

    gbc.gridx = 2;
    gbc.weightx = 0;
    this.add(this.m_yView, gbc);

  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final ChartCoordinateView other = (ChartCoordinateView) obj;
    if (this.m_chart2D == null) {
      if (other.m_chart2D != null) {
        return false;
      }
    } else if (!this.m_chart2D.equals(other.m_chart2D)) {
      return false;
    }
    if (this.m_xView == null) {
      if (other.m_xView != null) {
        return false;
      }
    } else if (!this.m_xView.equals(other.m_xView)) {
      return false;
    }
    if (this.m_yView == null) {
      if (other.m_yView != null) {
        return false;
      }
    } else if (!this.m_yView.equals(other.m_yView)) {
      return false;
    }
    return true;
  }

  /**
   * Removes the mouse motion listener from the chart.
   * <p>
   * 
   * @throws Throwable
   *           if something goes wrong in super classes finalize.
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.m_chart2D.removeMouseMotionListener(this.m_mouseListener);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_chart2D == null) ? 0 : this.m_chart2D.hashCode());
    result = prime * result + ((this.m_xView == null) ? 0 : this.m_xView.hashCode());
    result = prime * result + ((this.m_yView == null) ? 0 : this.m_yView.hashCode());
    return result;
  }
}
