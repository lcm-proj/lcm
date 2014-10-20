/*
 * Entry.java, base data container for mapping two elements.
 * Copyright (C) 2003 - 2011 Achim Westermann.
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
package info.monitorenter.util.collections;

/**
 * <p>
 * I have written implementations of <tt>java.util.Map.Entry</tt> in form of
 * <ul>
 * <li>Static inner classes.</li>
 * <li>Non-static inner classes.</li>
 * <li>Non-public classes.</li>
 * <li>Anonymous classes.</li>
 * </ul>
 * </p>
 * <p>
 * Almost all implementations were plainforward and not hiding any complexity.
 * One could not downcast them to get more methods, and they were replaceable. <br>
 * That's it! Finally i decided to hardcode it here... .
 * </p>
 * <p>
 * But don't you start writing methods like:
 * 
 * <pre>
 * public Entry getEntry(String name);
 * 
 * public void setEntry(Entry entry);
 * </pre>
 * 
 * Try sticking to the interface <tt>java.util.Map.Entry</tt>.
 * </p>
 * 
 * @see java.util.Map.Entry
 * 
 * @param <V>
 *          the key type.
 * 
 * @param <K>
 *          the value type.
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann</a>
 */
public final class Entry<V, K> implements java.util.Map.Entry<V, K> {

  /** The key instance. */
  private final V m_key;

  /** The value instance. */
  private K m_value;

  /**
   * Creates an instance with the given key and value.
   * <p>
   * 
   * @param key
   *          the key instance to use.
   * 
   * @param value
   *          the value instance to use.
   */
  public Entry(final V key, final K value) {
    this.m_key = key;
    this.m_value = value;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
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
    final Entry<V,K> other = (Entry<V,K>) obj;
    if (this.m_key == null) {
      if (other.m_key != null) {
        return false;
      }
    } else if (!this.m_key.equals(other.m_key)) {
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
   * Maybe null!
   * 
   * @see java.util.Map.Entry#getKey()
   */
  public V getKey() {
    return this.m_key;
  }

  /**
   * Maybe null!
   * 
   * @see java.util.Map.Entry#getValue()
   */
  public K getValue() {
    return this.m_value;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_key == null) ? 0 : this.m_key.hashCode());
    result = prime * result + ((this.m_value == null) ? 0 : this.m_value.hashCode());
    return result;
  }

  /**
   * Sets a new value instance overwriting the old value which is returned.
   * <p>
   * 
   * You may use null. But you will get it back next call!
   * <p>
   * 
   * @see java.util.Map.Entry#setValue(java.lang.Object)
   * 
   * @return the previous value instance.
   */
  public K setValue(final K value) {
    final K ret = this.m_value;
    this.m_value = value;
    return ret;
  }

}
