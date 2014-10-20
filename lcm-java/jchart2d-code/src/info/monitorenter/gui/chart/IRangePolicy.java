/*
 *  IRangePolicy.java of project jchart2d. Interface for viewport policies. 
 *  Copyright (C) 2004 - 2011 Achim Westermann.
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
package info.monitorenter.gui.chart;

import info.monitorenter.util.Range;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * An interface that allows an axis to be plugged with a range policy.
 * <p>
 * Implementations may limit the range of the underlying Chart2D's data
 * (clipping / zooming), increase it (void space offset), guarantee a minimum
 * viewport... .
 * <p>
 * 
 * <h3>Property Change events</h3>
 * <p>
 * <table border="0">
 * <tr>
 * <th><code>property</code></th>
 * <th><code>oldValue</code></th>
 * <th><code>newValue</code></th>
 * <th>occurrence</th>
 * </tr>
 * <tr>
 * <td><code>{@link #PROPERTY_RANGE}</code></td>
 * <td><code>{@link info.monitorenter.util.Range}</code> that changed</td>
 * <td><code>{@link info.monitorenter.util.Range}</code>, the new value</td>
 * <td>Fired if any bound of the range changed (min or max).</td>
 * </tr>
 * <tr>
 * <tr>
 * <td><code>{@link #PROPERTY_RANGE_MAX}</code></td>
 * <td><code>{@link java.lang.Double}</code>, the old max value of the
 * range. </td>
 * <td><code>{@link info.monitorenter.util.Range}</code>, the new max value
 * of the range. </td>
 * <td></td>
 * </tr>
 * <tr>
 * <td><code>{@link #PROPERTY_RANGE_MIN}</code></td>
 * <td><code>{@link java.lang.Double}</code>, the old min value of the
 * range. </td>
 * <td><code>{@link info.monitorenter.util.Range}</code>, the new min value
 * of the range. </td>
 * <td></td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.9 $
 * 
 * @see info.monitorenter.gui.chart.axis.AAxis
 */
public interface IRangePolicy extends Serializable {
  /**
   * The property key defining a change of the <code>min</code> or the
   * <code>max</code> property.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   */
  public static final String PROPERTY_RANGE = "IRangePolicy.PROPERTY_RANGE";

  /**
   * The property key defining the <code>max</code> property.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   */
  public static final String PROPERTY_RANGE_MAX = "IRangePolicy.PROPERTY_RANGE";

  /**
   * The property key defining the <code>min</code> property.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   */
  public static final String PROPERTY_RANGE_MIN = "IRangePolicy.PROPERTY_RANGE";

  /**
   * Registers a property change listener that will be informed about changes of
   * the property identified by the given <code>propertyName</code>.
   * <p>
   * 
   * @param propertyName
   *          the name of the property the listener is interested in
   * 
   * @param listener
   *          a listener that will only be informed if the property identified
   *          by the argument <code>propertyName</code> changes
   * 
   * 
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Define the upper bound of the Chart2D's value range. Depends on the
   * {@link info.monitorenter.gui.chart.axis.AAxis} this instance is bound to.
   * 
   * @param chartMin
   *          the minimum value of the connected Chart2D that may / should be
   *          taken into account.
   * 
   * @param chartMax
   *          the maximum value of the connected Chart2D that may / should be
   *          taken into account.
   * 
   * @return the maximum value (upper bound) for the Chart2D to display.
   * 
   */
  public double getMax(double chartMin, double chartMax);

  /**
   * Define the lower bound of the Chart2D's value range. Depends on the
   * {@link info.monitorenter.gui.chart.axis.AAxis} this instance is bound to.
   * 
   * @param chartMin
   *          the minimum value of the connected Chart2D that may / should be
   *          taken into account.
   * 
   * @param chartMax
   *          the maximum value of the connected Chart2D that may / should be
   *          taken into account.
   * 
   * @return the minimum value (lower bound) for the Chart2D to display.
   * 
   */
  public double getMin(double chartMin, double chartMax);

  /**
   * <p>
   * Returns all property change listeners for the given property.
   * </p>
   * 
   * @param property
   *          one of the constants with teh <code>PROPERTY_</code> prefix
   *          defined in this class or subclasses.
   * 
   * @return the property change listeners for the given property.
   */
  public PropertyChangeListener[] getPropertyChangeListeners(String property);

  /**
   * Get the range of this range policy.
   * <p>
   * 
   * @return he range of this range policy
   */
  public Range getRange();

  /**
   * Deregisters a property change listener that has been registerd for
   * listening on the given property.
   * <p>
   * 
   * @param listener
   *          a listener that will only be informed if the property identified
   *          by the argument <code>propertyName</code> changes
   * 
   * @param property
   *          the property the listener was registered to.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener, final String property);

  /**
   * <p>
   * Removes a property change listener for listening on the given property.
   * </p>
   * 
   * @param property
   *          one of the constants with teh <code>PROPERTY_</code> prefix
   *          defined in this class or subclasses.
   * 
   * @param listener
   *          the listener for this property change.
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener);

  /**
   * Set the range of this RangePolicy.
   * <p>
   * 
   * @param range
   *          the Range for the range policy.
   */
  public void setRange(Range range);
}
