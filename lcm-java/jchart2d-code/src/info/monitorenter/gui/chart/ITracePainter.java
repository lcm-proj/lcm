/*
 * ITracePainter.java, part of a trace responsible for painting it.
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
package info.monitorenter.gui.chart;

import java.awt.Graphics;

/**
 * An interface that works at trace level and defines how it's points are rendered.
 * <p>
 * A contract for implementation is that {@link java.lang.Object#equals(java.lang.Object)} has to be
 * implemented to return true if two instances are of the same class and
 * {@link java.lang.Comparable#compareTo(java.lang.Object)} is implemented according to that. This
 * is OK as trace painters are mostly to characterize by their different implementation of rendering
 * a trace.
 * <p>
 * <h3>Caution</h3>
 * There is no guarantee that further manipulation on the given {@link java.awt.Graphics2D} instance
 * than painting just the label or tick will not produce layout problems. E.g. changing the color or
 * font is not recommended as these should be assigned to the
 * {@link info.monitorenter.gui.chart.ITrace2D}/ {@link info.monitorenter.gui.chart.Chart2D}.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.15 $
 * @param <T> demonstration of unknown comparable and inheritance idiom or bad generics design for this case. 
 * 
 */
public interface ITracePainter<T extends ITracePainter<T>> extends IPointPainter<T> {

  /**
   * Invoked to inform the painter that a discontinue in the trace to # paint has occured.
   * <p>
   * This only has to be implemented by painters that collect several points of
   * {@link #paintPoint(int, int, int, int, Graphics, ITracePoint2D)} to draw them as polygons (e.g.:
   * {@link java.awt.Graphics#drawPolyline(int[], int[], int)}).
   * <p>
   * 
   * @param g2d
   *            provided in case pending paint operations have to be performed.
   */
  public void discontinue(Graphics g2d);

}
