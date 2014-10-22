/*
 *  AxisActionSetTitle.java of project jchart2d, an action  implementation 
 *  to set the title String of an IAxis.
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 20:30:06.
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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.controls.OneStringChooserPanel;
import info.monitorenter.gui.chart.dialogs.ModalDialog;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * <code>Action</code> that sets the title String of the <code>{@link IAxis}</code>
 * specified by the constructor. 
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.5 $
 */
public class AxisActionSetTitle extends AAxisAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = -8187453368186978451L;

  /**
   * Create an <code>Action</code> that accesses the chart's axis by argument
   * <code>axis</code> and identifies itself with the given action String.
   * <p>
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * @param axis
   *          needed to identify the axis of the chart: one of {@link Chart2D#X},
   *          {@link Chart2D#Y}.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public AxisActionSetTitle(final Chart2D chart, final String description, final int axis) {
    super(chart, description, axis);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {

    IAxis<?> axis = this.getAxis();
    OneStringChooserPanel titlePanel = new OneStringChooserPanel("Title", axis.getAxisTitle().getTitle());
    ModalDialog dialog = new ModalDialog(axis.getAccessor().getChart(), "Choose a Title for the "
        + axis.toString() + " axis", titlePanel);
    dialog.showDialog();
    axis.getAxisTitle().setTitle(titlePanel.getValue());
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    // will check for an axis replacement and transfer listening to the new axis if so: 
    super.propertyChange(evt);
    // nop as this action will not be used
    // by checkbox or radio button menu items that have a state.

  }
}
