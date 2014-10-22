/*
 *  Trace2DReplacing, a list- based implementation of a ITrace2D.
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

package info.monitorenter.gui.chart.traces;

import info.monitorenter.gui.chart.ITracePoint2D;


/**
 * Has the behavior of <code>Trace2DBijective</code> and additional
 * features.<p>
 * 
 * <ul>
 * <li>All tracepoints that are added are stored unchanged in a LinkedList.
 * </li>
 * <li>All traceoints added whose x- values are not already contained are added
 * to the end.</li>
 * <li>If a tracepoint is inserted whose x - value already exists in the List,
 * the old tracepoint with that value will be replaced by the new tracepoint.
 * </li>
 * </ul>
 * <p>
 * 
 * @see Trace2DBijective
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.12 $
 */
public class Trace2DReplacing extends Trace2DSimple {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 858570477289251003L;

  /**
   * Defcon.
   */
  public Trace2DReplacing() {
    // nop
  }

  /**
   * In case p has an x- value already contained, the old tracepoint with that
   * value will be replaced by the new one. Else the new tracepoint will be
   * added to the end, not caring wether tracepoints with a higher x- value are
   * contained.
   * <p>
   * 
   * @param p
   *          the point to add.
   * 
   * @return true if the point wathe maximum amount of points that will be
   *         showns successfully added.
   */
  @Override
  public boolean addPointInternal(final ITracePoint2D p) {
    boolean result = true;
    int index = -1;
    ITracePoint2D old;
    index = this.m_points.indexOf(p);
    if (index != -1) {
      // already contained.
      old = this.m_points.get(index);
      // fires property changes with bound checks
      old.setLocation(old.getX(), p.getY());
      // we don't need further bound checks and property change events from
      // calling
      // addPoint method.
      result = false;
    } else {
      this.m_points.add(p);
      result = true;
    }
    return result;
  }
}
