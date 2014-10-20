/*
 *  IPaintPointer.java of project jchart2d, generic interface for 
 *  instances that have to render a point in pixel coordinates.
 *  Copyright (c) 2004 - 2011 Achim Westermann.
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
package info.monitorenter.gui.chart;

import java.awt.Graphics;
import java.io.Serializable;

/**
 * Generic interface for instances that have to render a point in pixel
 * coordinates.
 * <p>
 * 
 * This low level interface is used wherever points have to be painted:
 * <ul>
 * <li>painting traces ({@link info.monitorenter.gui.chart.ITracePainter})</li>
 * <li>painting endpoints, startpoints and the segments of errorbars (
 * {@link info.monitorenter.gui.chart.errorbars.ErrorBarPainter}).</li>
 * <li>painting additional point highlighters (
 * {@link ITracePoint2D#addAdditionalPointPainter(IPointPainter)}).</li>
 * </ul>
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * @param <T>
 *          demonstration of unknown comparable and inheritance idiom or bad
 *          generics design for this case.
 * 
 * @version $Revision: 1.19 $
 */
public interface IPointPainter<T extends IPointPainter<T>> extends Serializable, Comparable<T> {
  /**
   * Invoked to inform implementations that a paint iteration ends for the
   * corresponding {@link info.monitorenter.gui.chart.ITrace2D}.
   * <p>
   * 
   * @param g2d
   *          provided in case pending paint operations have to be performed.
   */
  public void endPaintIteration(Graphics g2d);

  /**
   * Paint the point given by absolute coordinates on the given graphic context.
   * <p>
   * 
   * The next coordinates are also provided to allow to check how much distance
   * is available for the graphic representation of the current point.
   * <p>
   * 
   * 
   * @param absoluteX
   *          the ready to use x value for the point to paint.
   * 
   * @param absoluteY
   *          the ready to use y value for the point to paint.
   * 
   * @param nextX
   *          the ready to use next x value for the point to paint.
   * 
   * @param nextY
   *          the ready to use next y value for the point to paint.
   * 
   * @param g
   *          the graphic context to paint on.
   * 
   * @param original
   *          just for information, for painting this should be irrelevant and
   *          it should not be changed too!
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics g, final ITracePoint2D original);

  /**
   * Invoked to inform implementations that a paint iteration starts for the
   * corresponding {@link info.monitorenter.gui.chart.ITrace2D}.
   * <p>
   * 
   * @param g2d
   *          provided in case pending paint operations have to be performed.
   */
  public void startPaintIteration(Graphics g2d);
}
