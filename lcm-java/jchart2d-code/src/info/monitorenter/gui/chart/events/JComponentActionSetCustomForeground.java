/*
 *  JComponentActionSetCustomForeground, action for setting a custom foreground color of a JComponent.
 *  Copyright (C) 2004 - 2011 by Achim Westermann, created on 10.12.2004, 13:48:55
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
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

/**
 * <p>
 * <code>Action</code> that sets a custom foreground color of the
 * corresponding <code>JComponent</code> by showing a modal color chooser.
 * </p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.8 $
 */
public final class JComponentActionSetCustomForeground extends AJComponentAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3904680491952451890L;

  /**
   * Reference to the last custom color chosen to check whether the corresponding
   * menu is selected.
   */
  private Color m_lastChosenColor = null;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies
   * itself with the given action String.
   * <p>
   * 
   * @param component
   *            the target the action will work on.
   * 
   * @param description
   *            the descriptive <code>String</code> that will be displayed by
   *            {@link javax.swing.AbstractButton} subclasses that get this
   *            <code>Action</code> assigned (
   *            {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public JComponentActionSetCustomForeground(final JComponent component, final String description) {
    super(component, description);
    component.addPropertyChangeListener(Chart2D.PROPERTY_FOREGROUND_COLOR, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    Color chosen = JColorChooser.showDialog(this.m_component, "choose foreground color for "
        + this.m_component.getName(), this.m_component.getForeground());
    this.m_component.setForeground(chosen);
    this.m_lastChosenColor = chosen;
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_FOREGROUND_COLOR)) {
      Color newColor = (Color) evt.getNewValue();
      if ((this.m_lastChosenColor != null) && (newColor.equals(this.m_lastChosenColor))) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, Boolean
            .valueOf(false), Boolean.valueOf(true));

      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, Boolean
            .valueOf(true), Boolean.valueOf(false));
      }
    }
  }
}
