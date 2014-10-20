/*
 *  TracePainterFill.java of project jchart2d. A trace painter implementation 
 *  that fills the area between trace an y=0,x=0.
 *  Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
package info.monitorenter.gui.chart.traces.painters;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

/**
 * A trace painter that fills the area between trace to render and the x axis
 * baseline with it's color.
 * <p>
 * 
 * Additionally it increases performance by summing up all points to render for
 * a paint iteration (submitted by
 * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)} between
 * {@link #startPaintIteration(Graphics)} and
 * {@link #endPaintIteration(Graphics)}) and only invoking only one polygon
 * paint for a paint call of the corresponding
 * {@link info.monitorenter.gui.chart.Chart2D}.
 * <p>
 * 
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.21 $
 * 
 */
public class TracePainterFill extends ATracePainter {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -7194158082574997539L;

  /**
   * Stores the corresponding chart to know the coordinate roots for closing the
   * polygon to fill.
   */
  private final Chart2D m_chart;

  /** The list of x coordinates collected in one paint iteration. */
  private List<Integer> m_xPoints;

  /** The list of y coordinates collected in one paint iteration. */
  private List<Integer> m_yPoints;

  /**
   * Constructor with the corresponding chart.
   * <p>
   * 
   * @param chart
   *          needed to get the start pixel coordinates of traces.
   */
  public TracePainterFill(final Chart2D chart) {
    this.m_chart = chart;
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#discontinue(java.awt.Graphics)
   */
  @Override
  public void discontinue(final Graphics g2d) {
    this.endPaintIteration(g2d);
    this.startPaintIteration(g2d);
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#endPaintIteration(java.awt.Graphics)
   */
  @Override
  public void endPaintIteration(final Graphics g2d) {
    if (g2d != null) {

      final int[] x = new int[this.m_xPoints.size() + 4];
      x[0] = this.m_chart.getXChartStart();
      int count = 1;
      for (final Integer xpoint : this.m_xPoints) {
        x[count] = xpoint.intValue();
        count++;
      }
      x[count] = this.getPreviousX();
      // step down (or up) to the y=0 for the last value (in y)
      x[count + 1] = x[count];
      // step back to startx,starty (root)
      x[count + 2] = this.m_chart.getXChartStart();

      final int[] y = new int[this.m_yPoints.size() + 4];
      y[0] = this.m_chart.getYChartStart();
      count = 1;
      for (final Integer ypoint : this.m_yPoints) {
        y[count] = ypoint.intValue();
        count++;
      }
      y[count] = this.getPreviousY();
      // step down (or up) to the y=0 for the last value (in y)
      y[count + 1] = this.m_chart.getYChartStart();
      // step back to startx,starty (root)
      y[count + 2] = this.m_chart.getYChartStart();

      g2d.fillPolygon(x, y, x.length);
    }
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
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
    final TracePainterFill other = (TracePainterFill) obj;
    if (this.m_chart == null) {
      if (other.m_chart != null) {
        return false;
      }
    } else if (!this.m_chart.equals(other.m_chart)) {
      return false;
    }
    if (this.m_xPoints == null) {
      if (other.m_xPoints != null) {
        return false;
      }
    } else if (!this.m_xPoints.equals(other.m_xPoints)) {
      return false;
    }
    if (this.m_yPoints == null) {
      if (other.m_yPoints != null) {
        return false;
      }
    } else if (!this.m_yPoints.equals(other.m_yPoints)) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.m_chart == null) ? 0 : this.m_chart.hashCode());
    result = prime * result + ((this.m_xPoints == null) ? 0 : this.m_xPoints.hashCode());
    result = prime * result + ((this.m_yPoints == null) ? 0 : this.m_yPoints.hashCode());
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.traces.painters.ATracePainter#paintPoint(int,
   *      int, int, int, java.awt.Graphics,
   *      info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics g, final ITracePoint2D original) {

    this.m_xPoints.add(Integer.valueOf(absoluteX));
    this.m_yPoints.add(Integer.valueOf(absoluteY));
    super.paintPoint(absoluteX, absoluteY, nextX, nextY, g, original);
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#startPaintIteration(java.awt.Graphics)
   */
  @Override
  public void startPaintIteration(final Graphics g2d) {
    this.m_xPoints = new LinkedList<Integer>();
    this.m_yPoints = new LinkedList<Integer>();
  }

}
