/*
 *  ErrorBarPolicyActionAddPainter.java of project jchart2d, action 
 *  that adds a new ErrorBarPainterConfigureable to the given IErrorBarPolicy. 
 *  Copyright (C) 2006 - 2011 Achim Westermann, created on 09.12.2006 00:14:25.
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
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.errorbars.ErrorBarPainter;
import info.monitorenter.util.UIUtil;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * Action that adds a new
 * {@link info.monitorenter.gui.chart.errorbars.ErrorBarPainter} to the given
 * {@link info.monitorenter.gui.chart.IErrorBarPolicy}.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.9 $
 */
public class ErrorBarPolicyActionAddPainter extends AErrorBarPolicyAction {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -697786192464802918L;

  /**
   * Create an instance that accesses the error bar policy to add a new painter
   * to it with the given action String.
   * 
   * @param errorBarPolicy
   *          the target the action will work on.
   * 
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public ErrorBarPolicyActionAddPainter(final IErrorBarPolicy< ? > errorBarPolicy,
      final String description) {
    super(errorBarPolicy, description);

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    IErrorBarPainter painter = new ErrorBarPainter();
    this.m_errorBarPolicy.addErrorBarPainter(painter);
    // update UI:
    Component component = (Component) e.getSource();
    Window dialog = UIUtil.findDialogWindow(component);
    dialog.validate();
    dialog.pack();
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // TODO Auto-generated method stub
  }
}
