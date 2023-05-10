/*
 * AbstractLabelFormatter.java, base class for ILabelFormatter 
 * implementations.  
 * Copyright (c) 2004 - 2011 Achim Westermann, Achim.Westermann@gmx.de 
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * If you modify or optimize the code in a useful way please let me know.
 * Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart.labelformatters;

import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.util.Range;
import info.monitorenter.util.units.AUnit;
import info.monitorenter.util.units.UnitUnchanged;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * A label formatter that is aware of the
 * {@link info.monitorenter.gui.chart.axis.AAxis} it formats label for.
 * <p>
 * This allows to compute the amount of fraction digits needed from the range to
 * display.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.10 $
 */
public abstract class ALabelFormatter implements IAxisLabelFormatter {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 5211073371003781159L;

  /**
   * The default unit with the factor 1 that is returned as the default for
   * {@link #getUnit()}.
   */
  public static final AUnit UNIT_UNCHANGED = new UnitUnchanged();

  /** The corresponding axis to format for. */
  private transient IAxis<?> m_axis;

  /** Support for acting as a property change event producer for listeners. */
  protected PropertyChangeSupport m_propertyChangeSupport;

  /**
   * Default constructor.
   * <p>
   */
  protected ALabelFormatter() {
    this.m_propertyChangeSupport = new SwingPropertyChangeSupport(this);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(final String propertyName,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final ALabelFormatter other = (ALabelFormatter) obj;
    if (this.m_axis == null) {
      if (other.m_axis != null) {
        return false;
      }
    } else if (!this.m_axis.equals(other.m_axis)) {
      return false;
    }
    if (this.m_propertyChangeSupport == null) {
      if (other.m_propertyChangeSupport != null) {
        return false;
      }
    } else if (!this.m_propertyChangeSupport.equals(other.m_propertyChangeSupport)) {
      return false;
    }
    return true;
  }

  /**
   * Intended for {@link info.monitorenter.gui.chart.axis.AAxis} only.
   * <p>
   * 
   * @return Returns the axis.
   */
  public IAxis<?> getAxis() {
    return this.m_axis;
  }

  /**
   * Returns the maximum amount of characters that will be returned from
   * {@link #format(double)}.
   * <p>
   * 
   * @return the maximum amount of characters that will be returned from
   *         {@link #format(double)}.
   */
  public int getMaxAmountChars() {
    // find the fractions by using range information:
    int fractionDigits = 0;
    final Range range = this.m_axis.getRange();
    double dRange = range.getExtent();
    if (dRange < 1) {
      if (dRange == 0) {
        fractionDigits = 1;
      } else {
        // find the power
        while (dRange < 1) {
          dRange *= 10;
          fractionDigits++;
        }
      }
    } else {
      if (dRange == 0) {
        dRange = 1;
      }
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
    double max = range.getMax();
    double min = Math.abs(range.getMin());
    if ((max == 0) && (min == 0)) {
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

    // <sign> integerDigits <dot> fractionDigits:
    return 1 + integerDigits + 1 + fractionDigits;
  }

  /**
   * Returns {@link #UNIT_UNCHANGED}.
   * <p>
   * 
   * @return {@link #UNIT_UNCHANGED}
   * 
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getUnit()
   */
  public AUnit getUnit() {

    return ALabelFormatter.UNIT_UNCHANGED;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.m_propertyChangeSupport == null) ? 0 : this.m_propertyChangeSupport.hashCode());
    return result;
  }

  /**
   * Void adapter method implementation - optional to override.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#initPaintIteration()
   */
  public void initPaintIteration() {
    // nop adapter
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Intended for {@link info.monitorenter.gui.chart.axis.AAxis} only.
   * <p>
   * 
   * Do never invoke this! This is only public for package sorting reasons.
   * <p>
   * 
   * @param axis
   *          The m_axis to set.
   */
  public void setAxis(final IAxis<?> axis) {
    this.m_axis = axis;
  }

}
