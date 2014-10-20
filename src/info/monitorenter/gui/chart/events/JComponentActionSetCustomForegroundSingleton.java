/*
 *  JComponentActionSetCustomForegroundSingleton, 
 *  singleton action to set a custom foreground color of a JComponent.
 *  Copyright (C) 2007 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

/**
 * Singleton <code>Action</code> that sets a custom foreground color of the
 * corresponding <code>JComponent</code> by showing a modal color chooser.
 * <p>
 * Only one instance per target component may exist.
 * <p>
 * 
 * @see info.monitorenter.gui.chart.events.JComponentActionSetCustomForeground
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.8 $
 */
public final class JComponentActionSetCustomForegroundSingleton extends AJComponentAction {

  /**
   * Generated serial version ID.
   */
  private static final long serialVersionUID = 3904680491952451890L;

  /**
   * Reference to the last custom color chosen to check wether the corresponding
   * menu is selected.
   */
  private Color m_lastChosenColor;

  /**
   * Map for instances.
   */
  private static Map<String, JComponentActionSetCustomForegroundSingleton> instances;

  static {
    JComponentActionSetCustomForegroundSingleton.instances = new HashMap<String, JComponentActionSetCustomForegroundSingleton>();
  }
  /**
   * Returns the single instance for the given component, potentially creating
   * it.
   * <p>
   * 
   * If an instance for the given component had been created the description
   * String is ignored.
   * <p>
   * 
   * @param component
   *            the component to get the instance for (works as key).
   * 
   * @param description
   *            the description to use (ignored if instance for component has
   *            been created before).
   * 
   * @return the single instance for the given component.
   */
  public static JComponentActionSetCustomForegroundSingleton getInstance(
      final JComponent component, final String description) {
    JComponentActionSetCustomForegroundSingleton result = JComponentActionSetCustomForegroundSingleton.instances
        .get(JComponentActionSetCustomForegroundSingleton.key(component));
    if (result == null) {
      result = new JComponentActionSetCustomForegroundSingleton(component, description);
      JComponentActionSetCustomForegroundSingleton.instances.put(
          JComponentActionSetCustomForegroundSingleton.key(component), result);
    }
    return result;
  }

  /**
   * Creates a key for the component for internal storage.
   * 
   * @param component
   *            the component for the internal storage key to compute.
   * 
   * @return a key for the component for internal storage.
   */
  private static String key(final JComponent component) {
    return component.getClass().getName() + component.hashCode();
  }

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
  private JComponentActionSetCustomForegroundSingleton(final JComponent component,
      final String description) {
    super(component, description);
    component.addPropertyChangeListener(Chart2D.PROPERTY_FOREGROUND_COLOR, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    Color chosen = JColorChooser.showDialog(this.m_component, "choose background color for "
        + this.m_component.getName(), this.m_component.getBackground());
    if (chosen != null) {
      this.m_lastChosenColor = chosen;
      this.m_component.setForeground(chosen);
    }
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_FOREGROUND_COLOR)) {
      Color newColor = (Color) evt.getNewValue();
      if (newColor.equals(this.m_lastChosenColor)) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(false), new Boolean(true));

      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(true), new Boolean(false));
      }
    }
  }
}
