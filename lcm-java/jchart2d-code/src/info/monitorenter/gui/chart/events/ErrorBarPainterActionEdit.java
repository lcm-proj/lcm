/*
 *  ErrorBarPolicyActionEdit.java of project jchart2d, action 
 *  that pops up a modal dialog to edit the given IErrorBarPolicy. 
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 09.12.2006 00:14:25.
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
package info.monitorenter.gui.chart.events;

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.controls.errorbarwizard.ErrorBarPainterEditPanel;
import info.monitorenter.gui.chart.dialogs.ModalDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;

/**
 * Action that pops up a modal dialog to edit the given
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.10 $
 */
public class ErrorBarPainterActionEdit
    extends AErrorBarPainterAction {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 3337393664927952024L;

  /** The parent component for the color chooser dialog to show. */
  private Component m_dialogParent;

  /**
   * Create an instance that accesses the error bar painter with a modal dialog
   * to edit it and identifies itself with the given action String.
   * 
   * @param errorBarPainter
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   * @param dialogParent
   *          the parent component for the modal dialog.
   */
  public ErrorBarPainterActionEdit(final IErrorBarPainter errorBarPainter,
      final String description, final Component dialogParent) {
    super(errorBarPainter, description);
    this.m_dialogParent = dialogParent;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
     // create a modal dialog:
    ModalDialog dialog = new ModalDialog(this.m_dialogParent, "title",
        new ErrorBarPainterEditPanel(this.m_errorBarPainter));
    dialog.setVisible(true);
  }
}
