/*
 * RingBufferArray, an array- based implementation of a RingBuffer, which never
 * drops stored elements in case of decreasing the buffer size.
 * Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
package info.monitorenter.util.collections;

import info.monitorenter.util.StringUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A RingBuffer can be used to store a limited number of entries of any type
 * within a buffer.
 * <p>
 * 
 * As soon as the maximum number of entries is reached, the next entry is added
 * to the end and the first entry is removed from it. In this case, all elements
 * are stored in a Object[]. But don't worry there will not be a single call to
 * <code>System.arraycopy</code> caused by invocation of the
 * <code>add(Object element)</code>- method. Internal indexes into the array for
 * the head and tail allow to reuse the same memory again and again. <br>
 * No element is lost: If <code>setBufferSize(int asize)</code> decreases the
 * size of the buffer and it will get smaller than the actual amount of elements
 * stored, they will get cached until removed.
 * <p>
 * For allowing high performance single-threaded use this implementation and the
 * implementations of the retrievable <code>Iterator</code>- instances are not
 * synchronized at all.
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @param <T>
 *          the type to store.
 * 
 * @version $Revision: 1.10 $
 */
public class RingBufferArray<T> extends RingBufferArrayFast<T> {

  /**
   * Base class for ring buffer iterators with pending removals (those who do
   * not drop instances if size is exceeded).
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * 
   * 
   * @version $Revision: 1.10 $
   */
  abstract class ARingBufferIterator extends RingBufferArrayFast<T>.ARingBufferIterator {

    /**
     * The position of the next instance of the pending removed elements to
     * return.
     */
    protected int m_pendpos;

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
      final ARingBufferIterator other = (ARingBufferIterator) obj;
      if (!this.getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (this.m_pendpos != other.m_pendpos) {
        return false;
      }
      return true;
    }

    /**
     * Returns the outer instance.<p>
     * 
     * @return the outer instance.
     */
    private RingBufferArray<T> getOuterType() {
      return RingBufferArray.this;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.getOuterType().hashCode();
      result = prime * result + this.m_pendpos;
      return result;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return super.hasNext() || (this.m_pendpos >= 0);
    }

    /**
     * @see info.monitorenter.util.collections.RingBufferArrayFast.ARingBufferIterator#incPos()
     */
    @Override
    protected void incPos() {
      // TODO Auto-generated method stub

    }

    /**
     * @see java.util.Iterator#next()
     */
    @SuppressWarnings("unchecked")
    @Override
    public T next() {
      Object ret = null;
      // Pending elements are the oldest
      if (this.m_pendpos >= 0) {
        ret = RingBufferArray.this.m_pendingremove.get(this.m_pendpos);
        this.m_pendpos--;
        if (ret == null) {
          System.out
              .println("RingBufferArray.iteratorF2L returns null: head:"
                  + RingBufferArray.this.m_headpointer + " tail: "
                  + RingBufferArray.this.m_tailpointer);
        }
      } else {
        if (!this.hasNext()) {
          throw new NoSuchElementException();
        }
        ret = RingBufferArray.this.m_buffer[this.m_pos];
        this.m_count++;
        this.incPos();
      }
      return (T) ret;
    }

  }

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3977861774055585593L;

  /**
   * Elements that stores elements that have to be removed due to an invocation
   * to {@link #setBufferSize(int)} with a smaller argument than the amount of
   * elements stored.
   */
  protected List<T> m_pendingremove = new LinkedList<T>();

  /**
   * Constructs a RingBuffer with the given size.
   * <p>
   * 
   * @param aSize
   *          the size of the buffer.
   */
  public RingBufferArray(final int aSize) {
    super(aSize);
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
    final RingBufferArray<T> other = (RingBufferArray<T>) obj;
    if (this.m_pendingremove == null) {
      if (other.m_pendingremove != null) {
        return false;
      }
    } else if (!this.m_pendingremove.equals(other.m_pendingremove)) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.m_pendingremove == null) ? 0 : this.m_pendingremove.hashCode());
    return result;
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return super.isEmpty() && (this.m_pendingremove.size() == 0);
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#iteratorF2L()
   */
  @Override
  public java.util.Iterator<T> iteratorF2L() {
    return new ARingBufferIterator() {
      {
        this.m_pos = (RingBufferArray.this.m_headpointer == 0) ? RingBufferArray.this.m_size
            : RingBufferArray.this.m_headpointer - 1;
        this.m_pendpos = RingBufferArray.this.m_pendingremove.size() - 1;
      }

      /**
       * @see info.monitorenter.util.collections.RingBufferArray.ARingBufferIterator#incPos()
       */
      @Override
      protected void incPos() {
        if (this.m_pos == 0) {
          this.m_pos = RingBufferArray.this.m_size;
        } else {
          this.m_pos--;
        }

      }
    };
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#iteratorL2F()
   */
  @Override
  public java.util.Iterator<T> iteratorL2F() {
    return new ARingBufferIterator() {
      {
        this.m_pos = RingBufferArray.this.m_tailpointer;
        this.m_pendpos = 0;
      }

      /**
       * @see info.monitorenter.util.collections.RingBufferArray.ARingBufferIterator#incPos()
       */
      @Override
      protected void incPos() {
        if (this.m_pos == RingBufferArray.this.m_size) {
          this.m_pos = 0;
        } else {
          this.m_pos++;
        }
      }
    };
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#remove()
   */
  @Override
  public T remove() {
    T result = null;
    if (this.m_pendingremove.size() > 0) {
      if (RingBufferArrayFast.DEBUG) {
        System.out.println("Removing pending element!!!");
      }
      result = this.m_pendingremove.remove(0);
    } else {
      result = super.remove();
    }
    return result;
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#removeAll()
   */
  @SuppressWarnings("unchecked")
  @Override
  public T[] removeAll() {
    final Object[] ret = new Object[this.size() + this.m_pendingremove.size()];
    final int stop = this.m_pendingremove.size();
    int i;
    for (i = 0; i < stop; i++) {
      ret[i] = this.m_pendingremove.remove(0);
    }
    for (; i < ret.length; i++) {
      ret[i] = this.remove();
    }
    return (T[]) ret;
  }

  /**
   * Sets a new buffer- size.
   * <p>
   * A new size is assigned but the elements "overhanging" are returned by the
   * <code>Object remove()</code>- method first. This may take time until the
   * buffer has its actual size again. Don't pretend on calling this method for
   * saving of memory very often as the whole buffer has to be copied into a new
   * array every time- and if newSize < getSize() additional the overhanging
   * elements references have to be moved to the internal
   * <code>List pendingremove</code>.
   * <p>
   * 
   * @param newSize
   *          the new size of the buffer.
   * 
   */
  @Override
  public void setBufferSize(final int newSize) {
    List<T> newpending = null;
    if (this.size() > newSize) {
      newpending = new LinkedList<T>();
      final int stop = this.size();
      for (int i = newSize; i < stop; i++) {
        final T add = this.remove();
        newpending.add(add);
      }
    }
    final Object[] newbuffer = new Object[newSize];
    int i = 0;
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("setBufferSize(" + newSize + "): isEmpty(): " + this.isEmpty() + " tail: "
          + this.m_tailpointer + " head: " + this.m_headpointer);
    }
    while (!this.isEmpty()) {
      newbuffer[i] = this.remove();
      i++;
    }
    this.m_tailpointer = 0;
    if (newSize == i) {
      this.m_headpointer = 0;
    } else {
      this.m_headpointer = i;
    }
    this.m_buffer = newbuffer;
    this.m_size = newSize - 1;
    if (newpending != null) {
      this.m_pendingremove = newpending;
    }
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#size()
   */
  @Override
  public int size() {
    return super.size() + this.m_pendingremove.size();
  }

  /**
   * Returns a string representation of the RingBuffer and it's contents.
   * <p>
   * 
   * Don't call this in your application too often: hard array copy - operation
   * and memory allocations are triggered.
   * <p>
   * 
   * @return a string representation of the RingBuffer and it's contents.
   */
  @Override
  public String toString() {
    String result;
    if (this.isEmpty()) {
      if (RingBufferArrayFast.DEBUG) {
        System.out.println("toString(): isEmpty: true");
      }
      result = "[]";
    } else {

      final Object[] actualcontent = new Object[this.size()];
      int tmp = this.m_tailpointer;
      final int stop = this.m_pendingremove.size();
      final Iterator<T> it = this.m_pendingremove.iterator();
      int i = 0;
      for (; i < stop; i++) {
        actualcontent[i] = it.next();
      }
      for (; i < actualcontent.length; i++) {
        actualcontent[i] = this.m_buffer[tmp];
        if (tmp == this.m_size) {
          tmp = 0;
        } else {
          tmp++;
        }
        if ((tmp == this.m_headpointer) && this.m_empty) {
          break;
        }
      }
      result = StringUtil.arrayToString(actualcontent);
    }
    return result;
  }

}
