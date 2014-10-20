/*
 *  ErrorBarPainterLine.java of project jchart2d, <purpose>
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 03.09.2006 21:55:43.
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
package info.monitorenter.gui.chart.errorbars;

import info.monitorenter.gui.chart.pointpainters.PointPainterLine;

/**
 * An error bar painter that renders no end point and start point and renders a
 * line as segment (
 * {@link info.monitorenter.gui.chart.pointpainters.PointPainterLine}).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.9 $
 */
public class ErrorBarPainterLine extends ErrorBarPainter {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -7877672177550520292L;

  /**
   * Defcon.
   * <p>
   */
  public ErrorBarPainterLine() {
    super();
    this.setConnectionPainter(new PointPainterLine());
    this.setStartPointPainter(null);
    this.setEndPointPainter(null);
  }
}
