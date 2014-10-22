/*
 * LabelFormatterUnit.java, a label formatter that adds 
 * a constructor given unit with the unit SI prefix to a decorated 
 * label formatter. 
 * Copyright (c) 2005 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
 */
package info.monitorenter.gui.chart.labelformatters;

import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.util.Range;
import info.monitorenter.util.units.AUnit;

import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

/**
 * A label formatter that adds a constructor given <code>{@link AUnit}</code>
 * of the unit SI prefix to a decorated
 * <code>{@link info.monitorenter.gui.chart.IAxisLabelFormatter}</code>.
 * <p>
 * 
 * The formatted Strings will be divided by a factor according to the chosen
 * unit.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 * 
 */
public class LabelFormatterUnit
    extends ALabelFormatter {
  
  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -6180347628045892405L;

  /**
   * The decorated instance.
   */
  private ALabelFormatter m_delegate;

  /**
   * The internal unit.
   * <p>
   * 
   * In this implementation it is only used for finding labels that match the
   * ticks.
   * <p>
   */
  private AUnit m_unit = UNIT_UNCHANGED;

  /**
   * Default constructor that uses a <code>{@link LabelFormatterSimple}</code>
   * to add the unit feature to.
   * <p>
   * 
   * @param unit
   *          the unit to use.
   * 
   */
  public LabelFormatterUnit(final AUnit unit) {
    this(unit, new LabelFormatterSimple());
  }

  /**
   * Creates an instance that will add "unit-functionality" to the given
   * formatter.
   * <p>
   * 
   * @param unit
   *          the unit to use.
   * 
   * @param delegate
   *          the formatter that will be decorated with units.
   */
  public LabelFormatterUnit(final AUnit unit, final ALabelFormatter delegate) {
    super();
    this.m_delegate = delegate;
    this.m_unit = unit;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener(final String propertyName,
      final PropertyChangeListener listener) {
    this.m_delegate.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    return this.m_delegate.equals(obj);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#format(double)
   */
  public String format(final double value) {
    double tmp = value / this.m_unit.getFactor();
    return this.m_delegate.format(tmp);
  }

  /**
   * @see ALabelFormatter#getAxis()
   */
  @Override
  public IAxis<?> getAxis() {
    return this.m_delegate.getAxis();
  }

  /**
   * Returns the decoroated label formatter.
   * <p>
   * 
   * @return the the decoroated label formatter.
   */
  final ALabelFormatter getDelegate() {
    return this.m_delegate;
  }

  /**
   * @see ALabelFormatter#getMaxAmountChars()
   */
  @Override
  public int getMaxAmountChars() {
    // find the fractions by using range information:
    int fractionDigits = 0;
    Range range = this.getAxis().getRange();
    double dRange = range.getExtent() / this.m_unit.getFactor();
    if (dRange < 1) {
      if (dRange == 0) {
        fractionDigits = 1;
      } else {
        if (dRange == 0) {
          fractionDigits = 1;
        } else {
          // find the power
          while (dRange < 1) {
            dRange *= 10;
            fractionDigits++;
          }
        }
      }
    } else {
      if (dRange < 10) {
        fractionDigits = 2;
      } else if (dRange < 100) {
        fractionDigits = 1;
      } else {
        fractionDigits = 0;
      }
    }

    // find integer digits by using longest value:
    int integerDigits = 0;
    double max = range.getMax() / (this.m_unit.getFactor());
    double min = Math.abs(range.getMin()) / (this.m_unit.getFactor());
    if (max == 0 && min == 0) {
      integerDigits = 1;
    } else if (max < min) {
      while (min > 1) {
        min /= 10;
        integerDigits++;
      }
    } else {
      while (max > 1) {
        max /= 10;
        integerDigits++;
      }
    }
    // check if the internal numberformat would cut values and cause rendering
    // errors:
    if (this.m_delegate instanceof LabelFormatterNumber) {

      NumberFormat nf = ((LabelFormatterNumber) this.m_delegate).getNumberFormat();
      if (integerDigits > nf.getMaximumIntegerDigits()) {
        nf.setMaximumIntegerDigits(integerDigits);
      }
      if (fractionDigits > nf.getMaximumFractionDigits()) {
        nf.setMaximumFractionDigits(fractionDigits);
      }
    }
    // <sign> integerDigits <dot> fractionDigits:
    return 1 + integerDigits + 1 + fractionDigits;

  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getMinimumValueShiftForChange()
   */
  public double getMinimumValueShiftForChange() {
    return this.m_delegate.getMinimumValueShiftForChange() * this.m_unit.getFactor();
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getNextEvenValue(double,
   *      boolean)
   */
  public double getNextEvenValue(final double value, final boolean ceiling) {
    return this.m_delegate.getNextEvenValue(value, ceiling);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getUnit()
   */
  @Override
  public AUnit getUnit() {
    return this.m_unit;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return this.m_delegate.hashCode();
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#initPaintIteration()
   */
  @Override
  public void initPaintIteration() {
    this.m_delegate.initPaintIteration();
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#parse(String)
   */
  public Number parse(final String formatted) throws NumberFormatException {
    double parsed = this.m_delegate.parse(formatted).doubleValue();
    parsed *= this.m_unit.getFactor();
    return new Double(parsed);
  }

  /**
   * @see info.monitorenter.gui.chart.labelformatters.ALabelFormatter#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  @Override
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_delegate.removePropertyChangeListener(property, listener);
  }

  /**
   * @see ALabelFormatter#setAxis(IAxis)
   */
  @Override
  public void setAxis(final IAxis<?> axis) {

    this.m_delegate.setAxis(axis);
  }

  /**
   * Sets the label formatter to decorate by the feature of automatic unit
   * choice.
   * <p>
   * 
   * @param delegate
   *          the label formatter to decorate by the feature of automatic unit
   *          choice.
   */
  final void setDelegate(final ALabelFormatter delegate) {
    this.m_delegate = delegate;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.m_delegate.toString();
  }
}
