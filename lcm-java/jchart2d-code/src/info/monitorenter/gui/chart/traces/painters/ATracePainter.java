/*
 * ATracePainter.java of project jchart2d. Base class for ITracePainter implementations.
 * Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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

import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.TracePoint2D;

import java.awt.Graphics;

/**
 * A trace painter that adds the service of knowing the previous point that had
 * to be painted.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.27 $
 * 
 */
public abstract class ATracePainter implements
    info.monitorenter.gui.chart.ITracePainter<ATracePainter> {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = -1091004082187803076L;

  /** Flag to remember if a paint iteration has ended. */
  private boolean m_isEnded = false;

  /**
   * The last trace point that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  private ITracePoint2D m_previousPoint = new TracePoint2D(0, 0);

  /**
   * The last x coordinate that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  private int m_previousX;

  /**
   * The last y coordinate that was sent to
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * It will be needed at {@link #endPaintIteration(Graphics)} as the former
   * method only uses the first set of coordinates to store in the internal list
   * to avoid duplicates.
   * <p>
   */
  private int m_previousY;

  public int compareTo(final ATracePainter o) {
    return this.toString().compareTo(o.toString());
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#discontinue(java.awt.Graphics)
   */
  public void discontinue(final Graphics g2d) {
    this.endPaintIteration(g2d);
    this.startPaintIteration(g2d);
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#endPaintIteration(java.awt.Graphics)
   */
  public void endPaintIteration(final Graphics g2d) {
    // nop
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
    final ATracePainter other = (ATracePainter) obj;
    if (this.m_isEnded != other.m_isEnded) {
      return false;
    }
    if (this.m_previousPoint == null) {
      if (other.m_previousPoint != null) {
        return false;
      }
    } else if (!this.m_previousPoint.equals(other.m_previousPoint)) {
      return false;
    }
    if (this.m_previousX != other.m_previousX) {
      return false;
    }
    if (this.m_previousY != other.m_previousY) {
      return false;
    }
    return true;
  }

  /**
   * Returns the previous trace point that had to be painted by
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * 
   * This value will be <code>null</code> if no previous point had to be
   * painted.
   * <p>
   * 
   * @return the previous trace point that had to be painted by
   *         {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   */
  protected ITracePoint2D getPreviousPoint() {
    return this.m_previousPoint;
  }

  /**
   * Returns the previous X value that had to be painted by
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * 
   * This value will be {@link Integer#MIN_VALUE} if no previous point had to be
   * painted.
   * <p>
   * 
   * @return the previous X value that had to be painted by
   *         {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   */
  public int getPreviousX() {
    final int result = this.m_previousX;
    if (this.m_isEnded) {
      this.m_previousX = Integer.MIN_VALUE;
      if (this.m_previousY == Integer.MIN_VALUE) {
        this.m_isEnded = false;
      }

    }
    return result;
  }

  /**
   * Returns the previous Y value that had to be painted by
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * 
   * This value will be {@link Integer#MIN_VALUE} if no previous point had to be
   * painted.
   * <p>
   * 
   * @return the previous Y value that had to be painted by
   *         {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   */
  public int getPreviousY() {
    final int result = this.m_previousY;
    if (this.m_isEnded) {
      this.m_previousY = Integer.MIN_VALUE;
      if (this.m_previousX == Integer.MIN_VALUE) {
        this.m_isEnded = false;
      }
    }
    return result;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.m_isEnded ? 1231 : 1237);
    result = prime * result
        + ((this.m_previousPoint == null) ? 0 : this.m_previousPoint.hashCode());
    result = prime * result + this.m_previousX;
    result = prime * result + this.m_previousY;
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
   *      int, java.awt.Graphics, info.monitorenter.gui.chart.ITracePoint2D)
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics g, final ITracePoint2D original) {
    this.m_previousX = nextX;
    this.m_previousY = nextY;
    this.m_previousPoint = original;
  }

  /**
   * @see info.monitorenter.gui.chart.ITracePainter#startPaintIteration(java.awt.Graphics)
   */
  public void startPaintIteration(final Graphics g2d) {
    // nop
  }

}
