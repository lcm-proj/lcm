/*
 *  ILabelFormatter.java of project jchart2d, a formatter for labels 
 *  of axis.
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

import info.monitorenter.util.units.AUnit;

import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * An interface used by Axis to format labels and determine the maximum width
 * for the labels.
 * <p>
 * In order to get as much labels as possible on the Chart2D's axes, an Axis
 * should be configured in a way that reduces the return value of the method
 * {@link #getMaxAmountChars()}.
 * <p>
 * 
 * @see info.monitorenter.gui.chart.axis.AAxis
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.5 $
 */
public interface IAxisLabelFormatter extends Serializable {
  /**
   * Constant for <code>{@link java.beans.PropertyChangeEvent}</code> fired
   * when the configuration changes.
   */
  public static final String PROPERTY_FORMATCHANGE = "IAxisLabelFormatter.PROPERTY_FORMATCHANGE";

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
   * Provide a String for the value. Subclasses should override the label
   * formatting here. The raw value is passed here to allow a general treatment.
   * Transformations of this raw value should be done here (e.g. division by
   * multiples of 1000 for scientific unit system display, date formatting,...).
   * <p>
   * 
   * @param value
   *          the value to format.
   * 
   * @return the formatted value.
   */
  public String format(double value);

  /**
   * Returns the maximum amount of characters that will be returned from
   * {@link  #format(double)}.
   * <p>
   * 
   * @return the maximum amount of characters that will be returned from
   *         {@link  #format(double)}.
   * 
   */
  public int getMaxAmountChars();

  /**
   * Returns the minimum change in the value to format that will cause to return
   * a different formatted String.
   * <p>
   * To achieve two different formatted Strings to be returned from the format
   * method the corresponding values given to the format method have to differ
   * at least by this value.
   * <p>
   * Some implementations (e.g. a formatter for date) have to use their own
   * format method an increas a value to determine when the first change will
   * occur. This is expensive and it's recommended that this action is performed
   * once only and the result is stored. Additionally this routine has to start
   * with an "even" (see {@link #getNextEvenValue(double, boolean)}) value to
   * get a correct result (the distance from even number to even number).
   * <p>
   * 
   * @return the minimum change in the value to format that will cause to return
   *         a different formatted String.
   */
  double getMinimumValueShiftForChange();

  /**
   * Returns the next "even" value to the given one. "Even" means that the
   * format method will exactly return the String for the value and not cut or
   * round any information. A label String created with an "even" number will be
   * exactly at the position it describes.
   * <p>
   * 
   * @param value
   *          the value to get the next "even" value for.
   * @param ceiling
   *          if true, the next higher number will returned, else the next lower
   *          one.
   * @return the next "even" value to the given one.
   * 
   */
  double getNextEvenValue(double value, boolean ceiling);

  /**
   * Returns the unit that is currently used by this formatter.
   * <p>
   * 
   * @return the unit that is currently used by this formatter.
   */
  public AUnit getUnit();

  /**
   * Callback method invoked by the corresponding
   * {@link info.monitorenter.gui.chart.axis.AAxis} upon start of a paint
   * iteration of the {@link Chart2D}.
   * <p>
   * 
   */
  public void initPaintIteration();

  /**
   * The reverse operation to <code>{@link #format(double)}</code>.
   * <p>
   * The given argument has to be in the format that will be generated by that
   * method or exceptions may be thrown. <br>
   * <code>test.parse(test.format(d))== d </code><br>
   * has to be true if no rounding occurs by the formatter.
   * <p>
   * 
   * @param formatted
   *          a <code>String</code> in the format that will be produced by
   *          method <code>{@link #format(double)}</code>.
   * 
   * @return the parsed number.
   * 
   * @throws NumberFormatException
   *           if the format of the argument is invalid.
   * 
   */
  public Number parse(String formatted) throws NumberFormatException;

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
  public void removePropertyChangeListener(final String property, PropertyChangeListener listener);

  /**
   * Allows the {@link info.monitorenter.gui.chart.axis.AAxis} to register
   * itself with the given formatter so that it may get information about the
   * data (e.g. range) it has to format.
   * <p>
   * 
   * This method should only be invoked by
   * {@link info.monitorenter.gui.chart.axis.AAxis}, all other invocations will
   * cause trouble.
   * <p>
   * 
   * @param axis
   *          the axis to gain information about.
   */
  public void setAxis(IAxis<?> axis);

}
