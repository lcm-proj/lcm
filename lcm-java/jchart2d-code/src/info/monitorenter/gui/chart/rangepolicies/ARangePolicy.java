/*
 * AbstractRangePolicy.java of project jchart2d,  A default superclass for chart viewport 
 * implementations that adds support for setting and getting ranges.
 * 
 * Copyright (c) 2007 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
package info.monitorenter.gui.chart.rangepolicies;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.util.Range;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * A default superclass for IRangePolicy implementations that adds support for
 * setting and getting ranges.
 * <p>
 * Should be used by any implementation that really works on the data of ranges
 * (not unbounded ranges). Subclasses should access the internal member range or
 * use {@link #getRange()}.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.8 $
 */
public abstract class ARangePolicy implements IRangePolicy {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 1895087230983658166L;

  /**
   * The instance that add support for firing <code>PropertyChangeEvents</code>
   * and maintaining <code>PropertyChangeListeners</code>.
   * {@link PropertyChangeListener} instances.
   */
  protected PropertyChangeSupport m_propertyChangeSupport = new SwingPropertyChangeSupport(this);

  /**
   * The internal range that may be taken into account for returning bounds from
   * {@link IRangePolicy#getMax(double, double)} and
   * {@link IRangePolicy#getMax(double, double)}.
   * <p>
   */
  private Range m_range;

  /**
   * Creates a range policy with an unconfigured range (
   * {@link Range#RANGE_UNBOUNDED}).
   * <p>
   * 
   */
  public ARangePolicy() {
    this.m_range = Range.RANGE_UNBOUNDED;
  }

  /**
   * Creates a range policy backed by the given range.
   * <p>
   * 
   * @param range
   *          the range that may be used to decide about the policy of
   *          displaying the range.
   */
  public ARangePolicy(final Range range) {
    this.m_range = range;
  }

  /**
   * Adds a property change listener.
   * <p>
   * 
   * @param propertyName
   *          The name of the property to listen on.
   * @param listener
   *          The PropertyChangeListener to be added.
   * @see info.monitorenter.gui.chart.ITrace2D#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public final void addPropertyChangeListener(final String propertyName,
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
    final ARangePolicy other = (ARangePolicy) obj;
    if (this.m_propertyChangeSupport == null) {
      if (other.m_propertyChangeSupport != null) {
        return false;
      }
    } else if (!this.m_propertyChangeSupport.equals(other.m_propertyChangeSupport)) {
      return false;
    }
    if (this.m_range == null) {
      if (other.m_range != null) {
        return false;
      }
    } else if (!this.m_range.equals(other.m_range)) {
      return false;
    }
    return true;
  }

  /**
   * Fires a property change event to the registered listeners.
   * <p>
   * 
   * @param property
   *          one of the <code>PROPERTY_XXX</code> constants defined in <code>
   *          {@link info.monitorenter.gui.chart.ITrace2D}</code>.
   * 
   * @param oldvalue
   *          the old value of the property.
   * 
   * @param newvalue
   *          the new value of the property.
   */
  protected final void firePropertyChange(final String property, final Object oldvalue,
      final Object newvalue) {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("AbstractRangePolicy.firePropertyChange (" + property + "), 0 locks");
    }
    this.m_propertyChangeSupport.firePropertyChange(property, oldvalue, newvalue);
  }

  /**
   * @see info.monitorenter.gui.chart.IRangePolicy#getPropertyChangeListeners(java.lang.String)
   */
  public PropertyChangeListener[] getPropertyChangeListeners(final String property) {
    return this.m_propertyChangeSupport.getPropertyChangeListeners(property);
  }

  /**
   * Returns the internal range that is used to decide about the policy of
   * displaying the chart.
   * <p>
   * 
   * @return the internal range that may be taken into account for returning
   *         bounds from {@link IRangePolicy#getMax(double, double)} and
   *         {@link IRangePolicy#getMax(double, double)}.
   */
  public final Range getRange() {
    return this.m_range;
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
    result = prime * result + ((this.m_range == null) ? 0 : this.m_range.hashCode());
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IRangePolicy#removePropertyChangeListener(java.beans.PropertyChangeListener,
   *      java.lang.String)
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener,
      final String property) {
    this.m_propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * @see info.monitorenter.gui.chart.IRangePolicy#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(property, listener);
  }

  /**
   * Sets the internal range that is used to decide about the policy of
   * displaying the chart.
   * <p>
   * 
   * @param range
   *          the internal range that may be taken into account for returning
   *          bounds from {@link IRangePolicy#getMax(double, double)} and
   *          {@link IRangePolicy#getMax(double, double)}.
   */
  public void setRange(final Range range) {
    final double oldMin = this.m_range.getMin();
    final double oldMax = this.m_range.getMax();
    final Range oldRange = this.m_range;
    final boolean minchanged = range.getMin() != oldMin;
    final boolean maxchanged = range.getMax() != oldMax;
    this.m_range = range;
    if (minchanged && maxchanged) {
      this.firePropertyChange(IRangePolicy.PROPERTY_RANGE, oldRange, this.m_range);
    } else if (minchanged) {
      this.firePropertyChange(IRangePolicy.PROPERTY_RANGE_MIN, new Double(oldMin), new Double(range
          .getMin()));
    } else if (maxchanged) {
      this.firePropertyChange(IRangePolicy.PROPERTY_RANGE_MAX, new Double(oldMax), new Double(range
          .getMax()));
    }

  }

}
