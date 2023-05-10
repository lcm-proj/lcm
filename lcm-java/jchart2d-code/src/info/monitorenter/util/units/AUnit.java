/*
 *  AUnit.java, base class for units in jchart2d.
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

package info.monitorenter.util.units;

import java.io.Serializable;

/**
 * A unit.
 * <p>
 *
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 *
 * @version $Revision: 1.10 $
 *
 * @see info.monitorenter.util.units.UnitFactory
 *
 * @see info.monitorenter.util.units.IUnitSystem
 *
 * @see info.monitorenter.util.units.UnitSystemSI
 */
public abstract class AUnit extends Object implements Serializable {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = -8890511971185813347L;

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.m_decimals;
    long temp;
    temp = Double.doubleToLongBits(this.m_factor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((this.m_unitName == null) ? 0 : this.m_unitName.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AUnit other = (AUnit) obj;
    if (this.m_decimals != other.m_decimals) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_factor) != Double.doubleToLongBits(other.m_factor)) {
      return false;
    }
    if (this.m_unitName == null) {
      if (other.m_unitName != null) {
        return false;
      }
    } else if (!this.m_unitName.equals(other.m_unitName)) {
      return false;
    }
    return true;
  }

  /** Decimals for rounding. */
  protected int m_decimals = 2;

  /**
   * The factor a result of {@link #getValue(double)} had to be multiplied with
   * if the real (unitless) value has to be calculated.
   */
  protected double m_factor;

  /**
   * The next smaller unit to this one within this unit's {@link IUnitSystem}.
   * <p>
   */
  protected AUnit m_nexLowerUnit;

  /**
   * The next greater unit to this one within this unit's {@link IUnitSystem}.
   * <p>
   */
  protected AUnit m_nextHigherUnit;

  /**
   * The short unit name of this unit a result of {@link #getValue(double)} has
   * to be related with to know the this result is displayed in a unit.
   * <p>
   */
  protected String m_unitName;

  /**
   * Protected constructor to ensure package access only.
   * <p>
   *
   * Use {@link UnitFactory#getInstance()} and
   * {@link UnitFactory#getUnit(double, IUnitSystem)} to obtain a proper unit.
   * <p>
   *
   */
  protected AUnit() {
    // nop
  }

  /**
   * Returns the number of decimals that should be be taken into account if the
   * method {@link #getValue(double)} is invoked (rounding).
   * <p>
   *
   * @return the number of decimals that should be be taken into account if the
   *         method {@link #getValue(double)} is invoked (rounding).
   */
  public int getDecimals() {
    return this.m_decimals;
  }

  /**
   * Returns the factor a result of {@link #getValue(double)} had to be
   * multiplied with if the real (unitless) value has to be calculated.
   * <p>
   *
   * For performance reason (fast access) factor is public. This is against
   * "safety by desing" so do never set this value.
   * <p>
   *
   * @return the factor a result of {@link #getValue(double)} had to be
   *         multiplied with if the real (unitless) value has to be calculated.
   */
  public double getFactor() {
    return this.m_factor;
  }

  /**
   * Returns the the value divided by this unit's factor, rounded to this unit's
   * configured decimals and suffixed by the unit name.
   * <p>
   *
   * @param value
   *          the value for the label.
   *
   * @return the the value divided by this unit's factor, rounded by this unit's
   *         configured decimals and suffixed by the unit name.
   *
   * @see #getUnitName()
   * @see #getDecimals()
   *
   */
  public String getLabel(final double value) {
    return new StringBuffer().append(this.round(value / this.m_factor)).append(" ").append(
        this.m_unitName).toString();
  }

  /**
   * Returns the next smaller unit to this one within this unit's
   * {@link IUnitSystem}.
   * <p>
   *
   * If this is already the smallest unit, this will be returned so add
   * <code>unit == unit.getLowerUnit()</code> as the termination criteria in
   * loops to search for the smallest unit (to avoid endless loops).
   * <p>
   *
   * @return the next lower unit to this one within this unit's
   *         {@link IUnitSystem}.
   */
  public AUnit getNexLowerUnit() {
    return this.m_nexLowerUnit;
  }

  /**
   * Returns the next greater unit to this one within this unit's
   * {@link IUnitSystem}.
   * <p>
   *
   * If this is already the greatest unit, this will be returned so add
   * <code>unit == unit.getNextHigherUnit()</code> as the termination criteria
   * in loops to search for the greatest unit (to avoid endless loops).
   * <p>
   *
   * @return the next greater unit to this one within this unit's
   *         {@link IUnitSystem}.
   */
  public AUnit getNextHigherUnit() {
    return this.m_nextHigherUnit;
  }

  /**
   * Retunrns the short unit name of this unit a result of
   * {@link #getValue(double)} has to be related with to know the this result is
   * displayed in a unit.
   * <p>
   *
   * @return the short unit name of this unit a result of
   *         {@link #getValue(double)} has to be related with to know the this
   *         result is displayed in a unit.
   *         <p>
   */
  public String getUnitName() {
    return this.m_unitName;
  }

  /**
   * Transforms the given absolute value into the represented unit value by
   * dividing by the specific factor.
   * <p>
   *
   * The result is rounded using the actual decimal setting.
   * <p>
   *
   * @param value
   *          the value to represent in this unit.
   *
   * @return The value to display in this unit rounded using the internal
   *         decimals.
   */
  public double getValue(final double value) {
    return this.round(value / this.m_factor);
  }

  /**
   * Internal rounding routine for {@link #getValue(double)}.
   * <p>
   *
   * @param value
   *          the value to round.
   * @return the given value rounded to the amount of decimals configured.
   */
  private final double round(final double value) {
    double tmp = Math.pow(10, this.m_decimals);
    return (Math.floor(value * tmp + 0.5d)) / tmp;
  }

  /**
   * Define how many decimals should be taken into account if the method
   * {@link #getValue(double)} is invoked (rounding).
   * <p>
   *
   * @param aftercomma
   *          the number of decimals that should be taken into account if the
   *          method {@link #getValue(double)} is invoked (rounding)
   */
  public void setDecimals(final int aftercomma) {
    if (aftercomma >= 0) {
      this.m_decimals = aftercomma;
    }
  }

  /**
   * Returns the same as {@link #getUnitName()}, prefer calling this directly
   * if desired.
   * <p>
   *
   * @return the same as {@link #getUnitName()}, prefer calling this directly
   *         if desired.
   *
   */
  @Override
  public String toString() {
    return this.getUnitName();
  }
}
