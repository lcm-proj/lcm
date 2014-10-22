/*
 *  ErrorBarPainterActionSetSegmentPainter.java, action that 
 *  sets an IPointPainter to a segment of the error bar painter.
 *  Copyright (C) 2004 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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
 *
 */
package info.monitorenter.gui.chart.events;

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IPointPainter;
import info.monitorenter.gui.chart.IPointPainterConfigurableUI;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * <code>Action</code> that sets an <code>{@link IPointPainter}</code> to the
 * corresponding segment of an error bar painter.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 */
public final class ErrorBarPainterActionSetSegmentPainter extends AbstractAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = -7759456438679504271L;

  /** The point painter to set to the segment of the error bar painter. */
  private IPointPainterConfigurableUI< ? > m_pointPainter;

  /**
   * The segment of the painter to set the color of.
   */
  private IErrorBarPainter.ISegment m_segment;

  /**
   * Create an <code>Action</code> that accesses the error bar painter and
   * identifies itself with the given action String.
   * 
   * @param errorBarPainterFacade
   *          the target the action will work on.
   * 
   * @param pointPainter
   *          the point painter to set to the segment of the error bar painter.
   * 
   * @param name
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public ErrorBarPainterActionSetSegmentPainter(
      final IErrorBarPainter.ISegment errorBarPainterFacade, final IPointPainterConfigurableUI< ? > pointPainter,
      final String name) {
    super(name);
    this.m_segment = errorBarPainterFacade;
    this.m_pointPainter = pointPainter;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    this.m_segment.setPointPainter(this.m_pointPainter);
  }

  /**
   * Overridden to work inside <code>{@link javax.swing.JComboBox}</code>.
   * <p>
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (String) this.getValue(Action.NAME);
  }

}
