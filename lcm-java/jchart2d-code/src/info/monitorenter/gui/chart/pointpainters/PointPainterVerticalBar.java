/*
 *  PointPainterVerticalBar.java, a point painter that renders a bar 
 *  for each point.
 *  Copyright (c) 2004 - 2011 Achim Westermann, Achim.Westermann@gmx.de
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
package info.monitorenter.gui.chart.pointpainters;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.Graphics;

/**
 * Renders points by painting a bar with choosable width for each point to show.
 * <p>
 * Bars are placed around the x value to render: the middle of the bar in x
 * dimension is the exact x value.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.18 $
 * 
 */
public class PointPainterVerticalBar extends APointPainter<PointPainterVerticalBar> {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 659897369391828199L;

  /**
   * Stores the corresponding chart to know the coordinate roots for closing the
   * rectangle to fill.
   * <p>
   */
  private final Chart2D m_chart;

  /** Half the width of a bar. */
  private int m_halfWidth;

  /**
   * Creates an instance with a default bar width size of 4.
   * <p>
   * 
   * @param chart
   *          the corresponding chart for bound information.
   */
  public PointPainterVerticalBar(final Chart2D chart) {
    this(4, chart);
  }

  /**
   * Creates an instance with the bar width.
   * 
   * @param barWidth
   *          the bar width in pixel to use.
   * 
   * @param chart
   *          the corresponding chart for bound information.
   */
  public PointPainterVerticalBar(final int barWidth, final Chart2D chart) {
    this.setBarWidth(barWidth);
    this.m_chart = chart;
  }

  /**
   * @see info.monitorenter.gui.chart.pointpainters.APointPainter#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final PointPainterVerticalBar other = (PointPainterVerticalBar) obj;
    if (this.m_chart == null) {
      if (other.m_chart != null) {
        return false;
      }
    } else if (!this.m_chart.equals(other.m_chart)) {
      return false;
    }
    if (this.m_halfWidth != other.m_halfWidth) {
      return false;
    }
    return true;
  }

  /**
   * Returns the diameter of the discs to paint in pixel.
   * <p>
   * 
   * @return the diameter of the discs to paint in pixel.
   */
  public int getBarWidth() {
    return this.m_halfWidth;
  }

  /**
   * @see info.monitorenter.gui.chart.pointpainters.APointPainter#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.m_chart == null) ? 0 : this.m_chart.hashCode());
    result = prime * result + this.m_halfWidth;
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
   *      int, java.awt.Graphics, info.monitorenter.gui.chart.ITracePoint2D)
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics g, final ITracePoint2D original) {
    g.fillRect(absoluteX - this.m_halfWidth, absoluteY, 2 * this.m_halfWidth, this.m_chart
        .getYChartStart()
        - absoluteY);
  }

  /**
   * Sets the width of the bars to paint in pixel.
   * <p>
   * 
   * @param barWidth
   *          the width of the bars to paint in pixel.
   */
  public void setBarWidth(final int barWidth) {
    this.m_halfWidth = barWidth / 2;
  }

}
