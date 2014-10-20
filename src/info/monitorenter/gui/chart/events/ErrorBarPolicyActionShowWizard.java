/*
 *  ErrorBarPolicyActionShowWizard.java of project jchart2d, an 
 *  action that shows a modal error bar wizard dialog. 
 *  Copyright 2007 - 2011 (C) Achim Westermann, created on 08.06.2007 12:08:53.
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

import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.controls.errorbarwizard.ErrorBarPolicyPanel;
import info.monitorenter.gui.chart.dialogs.ModalDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * An action that shows a modal error bar wizard dialog.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.7 $
 */
public class ErrorBarPolicyActionShowWizard extends AErrorBarPolicyAction {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 3413195708957445554L;

  /**
   * Creates a trigger for showing the error bar policy wizard dialog for the
   * given <code>{@link IErrorBarPolicy}</code> with the given name.
   * <p>
   * 
   * @param errorBarPolicy
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public ErrorBarPolicyActionShowWizard(final IErrorBarPolicy< ? > errorBarPolicy,
      final String description) {
    super(errorBarPolicy, description);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    ErrorBarPolicyPanel panel = new ErrorBarPolicyPanel(this.m_errorBarPolicy);
    ModalDialog dialog = new ModalDialog((Component) e.getSource(), "Configure Error Bar Policy",
        panel);
    dialog.setVisible(true);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop as this action is not used in Checkbox menu items
    // or anything that supports SelectionPropertyAdaptSupport.
  }

}
