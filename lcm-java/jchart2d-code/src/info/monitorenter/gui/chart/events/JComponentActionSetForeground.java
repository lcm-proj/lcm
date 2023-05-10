/*
 *  JComponentActionSetForeground, action for setting a foreground color to a JComponent.
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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;

/**
 * Performs the action of setting the foreground color (
 * {@link javax.swing.JComponent#setForeground(java.awt.Color)}} of a
 * <code>JComponent</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.6 $
 */
public final class JComponentActionSetForeground extends AJComponentAction {

  /**
   * Generated for <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3258131345116181297L;

  /** The foreground color to set. */
  private Color m_color;

  /**
   * Create an <code>Action</code> that accesses the <code>JComponent</code>
   * and identifies itself with the given action String.
   * <p>
   * 
   * @param component
   *          the target the action will work on.
   * 
   * @param colorName
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   * @param color
   *          the <code>Color</code> to set as "foreground" of the internal
   *          <code>JComponent</code>.
   */
  public JComponentActionSetForeground(final JComponent component, final String colorName,
      final Color color) {
    super(component, colorName);
    this.m_color = color;
    component.addPropertyChangeListener(Chart2D.PROPERTY_FOREGROUND_COLOR, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    this.m_component.setForeground(this.m_color);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_FOREGROUND_COLOR)) {
      Color newColor = (Color) evt.getNewValue();
      if (newColor.equals(this.m_color)) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            Boolean.valueOf(false), Boolean.valueOf(true));

      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            Boolean.valueOf(true), Boolean.valueOf(false));
      }
    }
  }
}
