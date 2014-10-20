/*
 *  TimeStampedValue, wrapper class for values marked with a timestamp.
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
package info.monitorenter.util;

import java.util.Map;

/**
 * Simple wrapper around a time in ms and a value Object.
 * <p>
 * 
 * The key is the time in ms and may be used in a Map.
 * <code>{@link #compareTo(TimeStampedValue)}</code> compares the key.
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann</a>
 * 
 * @version $Revision: 1.11 $
 */
public final class TimeStampedValue implements Map.Entry<Long, Object>,
    Comparable<TimeStampedValue> {

  /**
   * The time stamp (difference, measured in milliseconds, between the current
   * time and midnight, January 1, 1970 UTC).
   */
  private final long m_key;

  /** The time stamp value. */
  private Object m_value;

  /**
   * Creates an instance with the given timestamp key and the value to time
   * stamp.
   * <p>
   * 
   * @param key
   *          the time stamp (difference, measured in milliseconds, between the
   *          current time and midnight, January 1, 1970 UTC).
   * 
   * @param value
   *          the value to time stamp.
   */
  public TimeStampedValue(final long key, final Object value) {
    this.m_key = key;
    this.m_value = value;
  }

  /**
   * Creates an instance for the given value that is time stamped with the
   * current time.
   * <p>
   * 
   * @param value
   *          the value to time stamp.
   * 
   * @see System#currentTimeMillis()
   */
  public TimeStampedValue(final Object value) {
    this(System.currentTimeMillis(), value);
  }

  /**
   * Compares the given {@link TimeStampedValue} to this by the internal
   * {@link #getTime()}.
   * <p>
   * 
   * @param obj
   *          the object to compare this to.
   * 
   * @return see interface.
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(final TimeStampedValue obj) {
    int result;
    if (this.m_key < obj.m_key) {
      result = -1;
    } else if (this.m_key == obj.m_key) {
      result = 0;
    } else {
      result = 1;
    }
    return result;
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
    final TimeStampedValue other = (TimeStampedValue) obj;
    if (this.m_key != other.m_key) {
      return false;
    }
    if (this.m_value == null) {
      if (other.m_value != null) {
        return false;
      }
    } else if (!this.m_value.equals(other.m_value)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the {@link Long} that marks the time stamp (difference, measured in
   * milliseconds, between the current time and midnight, January 1, 1970 UTC).
   * 
   * @return the {@link Long} that marks the time stamp (difference, measured in
   *         milliseconds, between the current time and midnight, January 1,
   *         1970 UTC).
   * 
   * @see java.util.Map.Entry#getKey()
   */
  public Long getKey() {
    return new Long(this.m_key);
  }

  /**
   * Returns the time stamp (difference, measured in milliseconds, between the
   * current time and midnight, January 1, 1970 UTC).
   * <p>
   * 
   * @return the time stamp (difference, measured in milliseconds, between the
   *         current time and midnight, January 1, 1970 UTC).
   * 
   */
  public long getTime() {
    return this.m_key;
  }

  /**
   * Returns the time stamp.
   * <p>
   * 
   * @return the time stamp.
   * 
   * @see java.util.Map.Entry#getValue()
   */
  public Object getValue() {
    return this.m_value;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (this.m_key ^ (this.m_key >>> 32));
    result = prime * result + ((this.m_value == null) ? 0 : this.m_value.hashCode());
    return result;
  }

  /**
   * Returns whether the internal time stamp marks a time in the past or not.
   * <p>
   * 
   * For normal a time stamp represents a value regarded at a time. But it is
   * also thinkable to mark a value for expiration in the future. This method
   * returns true if the internal time- representing key is smaller than the
   * actual time.
   * <p>
   * 
   * @return true if the internal time stamp marks a moment in the past, false
   *         else.
   */
  public boolean isPast() {
    return this.m_key < System.currentTimeMillis();
  }

  /**
   * Assigns a different value to the timestamp.
   * <p>
   * 
   * @param value
   *          the new value to be marked with this timestamp.
   * 
   * @return the previous value that was contained.
   * 
   * @see java.util.Map.Entry#setValue(java.lang.Object)
   */
  public Object setValue(final Object value) {
    final Object ret = this.m_value;
    this.m_value = value;
    return ret;
  }
}
