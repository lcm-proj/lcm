/*
 *  AErrorBarPainterActionSetSegmentColor, action that 
 *  sets a custom color to a segment of the error bar painter.
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

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JComponent;

/**
 * <code>Action</code> that sets a custom color to the corresponding segment
 * of an error bar painter by showing a modal color chooser.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.5 $
 */
public final class ErrorBarPainterActionSetSegmentColor
    extends AbstractAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 7905566121691585692L;

  /**
   * The segment of the painter to set the color of.
   */
  private IErrorBarPainter.ISegment m_segment;

  /**
   * Needed as the parent UI component of the modal dialog which will be blocked
   * by the modal color chooser dialog that pops up.
   * 
   */
  private JComponent m_dialogParent;

  /**
   * Create an <code>Action</code> that accesses the error bar painter and
   * identifies itself with the given action String.
   * 
   * @param errorBarPainterFacade
   *          the target the action will work on.
   * 
   * @param dialogParent
   *          needed as the parent UI component of the modal dialog which will
   *          be blocked by the modal color chooser dialog that pops up.
   * 
   * @param name
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public ErrorBarPainterActionSetSegmentColor(
      final IErrorBarPainter.ISegment errorBarPainterFacade, final JComponent dialogParent,
      final String name) {
    super(name);
    this.m_segment = errorBarPainterFacade;
    this.m_dialogParent = dialogParent;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    Color chosen = JColorChooser.showDialog(this.m_dialogParent, "choose color for "
        + this.m_segment.getName(), this.m_segment.getColor());
    this.m_segment.setColor(chosen);
    // update UI with new color if possible:
    // this is needed if the trigger (e.g. a button) shows an icon
    // (this is within this instance->putValue(SMALL_ICON,icon)) that
    // reflects this color.
    Icon icon = (Icon) this.getValue(SMALL_ICON);
    if (icon != null) {
      Object source = e.getSource();
      if (source instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) source;
        button.setIcon(null);
        button.setIcon(icon);
      }
    }
  }

}
