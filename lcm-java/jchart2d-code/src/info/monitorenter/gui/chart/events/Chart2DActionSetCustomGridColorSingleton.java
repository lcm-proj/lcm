/*
 *  Chart2DActionSetCustomGridColorSingleton, 
 *  singleton action that sets a custom grid color to the chart.
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

/**
 * Singleton <code>Action</code> that sets a custom grid color to the
 * corresponding chart ({@link Chart2D#setGridColor(Color)}) by showing a
 * modal color chooser.
 * <p>
 * Only one instance per target component may exist.
 * <p>
 * 
 * @see info.monitorenter.gui.chart.events.Chart2DActionSetCustomGridColor
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.8 $
 */
public final class Chart2DActionSetCustomGridColorSingleton extends AChart2DAction {
  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3691034370412916788L;

  /**
   * Map for instances.
   */
  private static Map<String, Chart2DActionSetCustomGridColorSingleton> instances;
  static {
    Chart2DActionSetCustomGridColorSingleton.instances = new HashMap<String, Chart2DActionSetCustomGridColorSingleton>();
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
   * @param chart
   *            the target the action will work on
   * @param colorName
   *            the descriptive <code>String</code> that will be displayed by
   *            {@link javax.swing.AbstractButton} subclasses that get this
   *            <code>Action</code> assigned (
   *            {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   * @return the single instance for the given component.
   */
  public static Chart2DActionSetCustomGridColorSingleton getInstance(final Chart2D chart,
      final String colorName) {
    Chart2DActionSetCustomGridColorSingleton result = Chart2DActionSetCustomGridColorSingleton.instances
        .get(Chart2DActionSetCustomGridColorSingleton.key(chart));
    if (result == null) {
      result = new Chart2DActionSetCustomGridColorSingleton(chart, colorName);
      Chart2DActionSetCustomGridColorSingleton.instances.put(
          Chart2DActionSetCustomGridColorSingleton.key(chart), result);
    }
    return result;
  }

  /**
   * Creates a key for the component for internal storage.
   * <p>
   * 
   * @param chart
   *            the chart to generate the storage key for.
   * 
   * @return a storage key unique for the given chart instance.
   */
  private static String key(final Chart2D chart) {
    return chart.getClass().getName() + chart.hashCode();
  }

  /**
   * Reference to the last custom color chosen to check wether the corresponding
   * menu is selected.
   */
  private Color m_lastChosenColor;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies
   * itself with the given action String.
   * <p>
   * 
   * @param chart
   *            the target the action will work on
   * @param colorName
   *            the descriptive <code>String</code> that will be displayed by
   *            {@link javax.swing.AbstractButton} subclasses that get this
   *            <code>Action</code> assigned (
   *            {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  private Chart2DActionSetCustomGridColorSingleton(final Chart2D chart, final String colorName) {
    super(chart, colorName);
    chart.addPropertyChangeListener(Chart2D.PROPERTY_GRID_COLOR, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    Color chosen = JColorChooser.showDialog(this.m_chart, "choose color for "
        + this.m_chart.getName(), this.m_chart.getGridColor());
    this.m_lastChosenColor = chosen;
    this.m_chart.setGridColor(chosen);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_GRID_COLOR)) {
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
