/*
 *  LinePainter.java,  point painter that paints lines. 
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
package info.monitorenter.gui.chart.pointpainters;

import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.Graphics;

/**
 * A point painter that renders a trace by lines.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.18 $
 * 
 */
public class PointPainterLine extends APointPainter<PointPainterLine> {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 4325801979289678143L;

  /**
   * Defcon.
   * <p>
   */
  public PointPainterLine() {
    // nop
  }

  /**
   * Paints a line from current to next point.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
   *      int, java.awt.Graphics, info.monitorenter.gui.chart.ITracePoint2D)
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics g, final ITracePoint2D point) {
    g.drawLine(absoluteX, absoluteY, nextX, nextY);
  }

}
