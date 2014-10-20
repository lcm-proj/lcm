/*
 *  IPointFinder.java of project jchart2d, interface for finding a tracepoint of 
 *  a chart corresponding to a mouse event. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on Jun 12, 2011
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
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
 *
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/IPointFinder.java,v $
 * Date   : $Date: 2011/01/14 08:36:10 $
 * Version: $Revision: 1.2 $
 */

package info.monitorenter.gui.chart;

import java.awt.event.MouseEvent;

/**
 * Interface for finding a <code>{@link ITracePoint2D}</code> of a
 * <code>{@link Chart2D}</code> corresponding to a mouse event.
 * <p>
 * This is used to allow a pluggable strategy for this task which is needed by
 * point highlighting and/or tool tips.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public interface IPointFinder {
  /**
   * Returns the nearest <code>{@link ITracePoint2D}</code> to the given mouse
   * event's screen coordinates.
   * <p>
   * 
   * @param mouseEventX
   *          the x pixel value relative to the chart (e.g.: <code>
   *          {@link MouseEvent#getY()}</code>).
   * 
   * @param mouseEventY
   *          the y pixel value relative to the chart (e.g.: <code>
   *          {@link MouseEvent#getY()}</code>).
   * 
   * @param chart
   *          to search points within.
   * 
   * @return the nearest <code>{@link ITracePoint2D}</code> to the given mouse
   *         event's screen coordinates.
   * 
   */
  public ITracePoint2D getNearestPoint(final int mouseEventX, final int mouseEventY, Chart2D chart);

  /**
   * Returns the nearest <code>{@link MouseEvent}</code> to the given mouse
   * event's screen coordinates or <code>null</code> if no point was found /
   * service is not implemented.
   * <p>
   * Simple implementations could use Manhattan distance or Euclid distance.
   * <p>
   * 
   * @param me
   *          the mouse event over the chart.
   * 
   * @param chart
   *          to search points within.
   * 
   * @return e nearest <code>{@link MouseEvent}</code> to the given mouse
   *         event's screen coordinates or <code>null</code> if no point was
   *         found / service is not implemented.
   */
  public ITracePoint2D getNearestPoint(final MouseEvent me, final Chart2D chart);

}
