/*
 * RingBufferArrayFast, an array- based fast implementation of a RingBuffer.
 * Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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

import info.monitorenter.util.StringUtil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Fast ring buffer implementation.
 * <p>
 * 
 * This implementation differs from the <code>RingBufferArray</code> in one
 * point: <br>
 * If <code>setBufferSize(int asize)</code> decreases the size of the buffer and
 * it will get smaller than the actual amount of elements stored, they will get
 * lost. This avoids the need for an internal List to store elements
 * overhanging. Some tests may be left out that may speed up this
 * <code>IRingBuffer</code>. Adding 5000000 elements was about 25 % faster
 * compared to the <code>RingBufferArray</code> on an Athlon 1200, 256 MB RAM.
 * <p>
 * 
 * For allowing high performance single-threaded use this implementation and the
 * implementations of the retrievable <code>Iterator</code>- instances are not
 * synchronized at all.
 * <p>
 * 
 * @param <T>
 *          the type of instances to store.
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 */
public class RingBufferArrayFast<T> implements Cloneable, IRingBuffer<T> {

  /**
   * Base for ring buffer iterators that has access to the ring buffer by being
   * an non-static inner class.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.13 $
   */
  protected abstract class ARingBufferIterator implements Iterator<T> {
    /**
     * The amount of returned instances, needed for knowing if iterator is
     * empty.
     */
    protected int m_count;

    /** The index of the next instance to return. */
    protected int m_pos;

    /**
     * Defcon.
     * <p>
     * 
     */
    ARingBufferIterator() {
      // nop
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
      final ARingBufferIterator other = (ARingBufferIterator) obj;
      if (!this.getOuterType().equals(other.getOuterType())) {
        return false;
      }
      if (this.m_count != other.m_count) {
        return false;
      }
      if (this.m_pos != other.m_pos) {
        return false;
      }
      return true;
    }

    /**
     * Returns the outer instance.<p>
     * 
     * @return the outer instance.
     */
    private RingBufferArrayFast<T> getOuterType() {
      return RingBufferArrayFast.this;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + this.getOuterType().hashCode();
      result = prime * result + this.m_count;
      result = prime * result + this.m_pos;
      return result;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
      return (this.m_count < RingBufferArrayFast.this.size());
    }

    /** Increment the internal read position pointer. */
    protected abstract void incPos();

    /**
     * @see java.util.Iterator#next()
     */
    @SuppressWarnings("unchecked")
    public T next() {
      if (!this.hasNext()) {
        throw new NoSuchElementException();
      }
      final Object result = RingBufferArrayFast.this.m_buffer[this.m_pos];
      this.m_count++;
      this.incPos();
      if (result == null) {
        throw new NoSuchElementException("RingBufferArrayFast.iteratorF2L returns null: pos:"
            + this.m_pos + " count: " + this.m_count);
      }
      return (T) result;
    }

    /**
     * Not supported.
     * <p>
     * 
     * @throws UnsupportedOperationException
     *           always as this is not supported.
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * Flip the switch and you will see how the compiler changes the size of the
   * class file.
   */
  public static final boolean DEBUG = false;

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3834590997991404595L;

  /** The internal array used as buffer. */
  protected Object[] m_buffer;

  /**
   * Flag that marks whether this buffer is empty or not.
   * <p>
   * 
   * <pre>
   * 
   *             headpointer
   *              |
   *        +---+---+---+---+
   *        | 0 | 1 | 2 | 3 |
   *        +---+---+---+---+
   *              |
   *             tailpointer
   *    
   *        From where to where are the elements?
   *        Where is empty space?
   *        empty == true:  0 elements are contained: buffer empty
   *        empty == false:  4 elements are contained: buffer full
   *        remember:
   *            -the headpointer points to the space where the next element will be inserted.
   *            -the tailpointer points to the space to read the next element from.
   * 
   * 
   * </pre>
   * 
   * <p>
   */
  protected boolean m_empty = true;

  /**
   * The internal index to buffer where the next element is going to be placed
   * (not placed yet!).
   */
  protected int m_headpointer = 0;

  /**
   * The internal size of the buffer.
   * <p>
   * 
   * For performance reasons the size of the buffer -1!
   * <p>
   */
  protected int m_size;

  /**
   * The internal index to buffer where the next element is going to be read.
   */
  protected int m_tailpointer = 0;

  /**
   * Constructs a RingBuffer with the given size.
   * 
   * @param aSize
   *          the size of the buffer.
   */
  public RingBufferArrayFast(final int aSize) {
    this.m_buffer = new Object[aSize];
    this.m_size = aSize - 1;
  }

  /**
   * Adds an element to the ring buffer, potentially removing the first element
   * to make more room.
   * <P>
   * 
   * @param anObject
   *          the instance to add.
   * 
   * @return the oldest Object, if RingBuffer was filled with 'maxsize' elements
   *         before, or null.
   */
  @SuppressWarnings("unchecked")
  public T add(final T anObject) {
    Object ret = null;
    if (this.isFull()) {
      ret = this.m_buffer[this.m_tailpointer];
      this.incTail();
    }
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("add: tailpointer: " + this.m_tailpointer + " headpointer: "
          + this.m_headpointer + " size: " + this.size());
    }
    this.m_buffer[this.m_headpointer] = anObject;
    this.incHead();
    return (T) ret;
  }

  /**
   * Fast method to clear the buffer - only needs to set three primitive
   * members.
   * <p>
   * 
   * @see info.monitorenter.util.collections.IRingBuffer#clear()
   */
  public void clear() {
    this.m_headpointer = 0;
    this.m_tailpointer = 0;
    this.m_empty = true;
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
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
    final RingBufferArrayFast<T> other = (RingBufferArrayFast<T>) obj;
    if (!Arrays.equals(this.m_buffer, other.m_buffer)) {
      return false;
    }
    if (this.m_empty != other.m_empty) {
      return false;
    }
    if (this.m_headpointer != other.m_headpointer) {
      return false;
    }
    if (this.m_size != other.m_size) {
      return false;
    }
    if (this.m_tailpointer != other.m_tailpointer) {
      return false;
    }
    return true;
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#getBufferSize()
   */
  public int getBufferSize() {
    return this.m_size + 1;
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#getOldest()
   */
  @SuppressWarnings("unchecked")
  public T getOldest() throws RingBufferException {
    if (this.isEmpty()) {
      throw new IRingBuffer.RingBufferException("Buffer is empty.");
    }
    return (T) this.m_buffer[this.m_tailpointer];
  }

  /**
   * 
   * @see info.monitorenter.util.collections.IRingBuffer#getYoungest()
   */
  @SuppressWarnings("unchecked")
  public T getYoungest() throws RingBufferException {
    if (this.isEmpty()) {
      throw new IRingBuffer.RingBufferException("Buffer is empty.");
    }
    int tmp = this.m_headpointer;
    if (tmp == 0) {
      tmp = this.m_size;
    } else {
      tmp--;
    }
    return (T) this.m_buffer[tmp];
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(this.m_buffer);
    result = prime * result + (this.m_empty ? 1231 : 1237);
    result = prime * result + this.m_headpointer;
    result = prime * result + this.m_size;
    result = prime * result + this.m_tailpointer;
    return result;
  }

  /**
   * Internally increases the array index pointer to the head of the buffer.
   * <p>
   * 
   */
  private void incHead() {
    if (this.m_headpointer == this.m_size) {
      this.m_headpointer = 0;
    } else {
      this.m_headpointer++;
    }
    this.m_empty = false;
  }

  /**
   * Internally increases the array index pointer to the taill of the buffer.
   * <p>
   * 
   */
  private void incTail() {
    if (this.m_tailpointer == this.m_size) {
      this.m_tailpointer = 0;
    } else {
      this.m_tailpointer++;
    }
    if (this.m_tailpointer == this.m_headpointer) {
      this.m_empty = true;
    }
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#isEmpty()
   */
  public boolean isEmpty() {
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("isEmpty: " + this.m_empty + " head: " + this.m_headpointer + " tail: "
          + this.m_tailpointer);
    }
    return this.m_empty;
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#isFull()
   */
  public boolean isFull() {
    final boolean ret = (this.m_headpointer == this.m_tailpointer) && !this.m_empty;
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("isFull: " + ret + " head: " + this.m_headpointer + " tail: "
          + this.m_tailpointer);
    }
    return ret;
  }

  /**
   * Delegates to <code>{@link RingBufferArrayFast#iteratorL2F()}</code>.
   * <p>
   * 
   * @see java.lang.Iterable#iterator()
   */
  public java.util.Iterator<T> iterator() {
    return this.iteratorL2F();
  }

  /**
   * Returns an <code>Iterator</code> that will return the elements in exactly
   * the inverse order the subsequent call to <code>remove()</code> would do.
   * <p>
   * 
   * The youngest elements are returned first. <b>The <code>Iterator</code>
   * returned is not thread- safe! </b>
   * <p>
   * 
   * @return an <code>Iterator</code> that will return the elements in exactly
   *         the inverse order the subsequent call to <code>remove()</code>
   *         would do.
   */

  public java.util.Iterator<T> iteratorF2L() {
    return new ARingBufferIterator() {
      {
        this.m_pos = (RingBufferArrayFast.this.m_headpointer == 0) ? RingBufferArrayFast.this
            .size() - 1 : RingBufferArrayFast.this.m_headpointer - 1;
      }

      /**
       * @see info.monitorenter.util.collections.RingBufferArrayFast.ARingBufferIterator#incPos()
       */
      @Override
      protected void incPos() {
        if (this.m_pos == 0) {
          this.m_pos = RingBufferArrayFast.this.m_size;
        } else {
          this.m_pos--;
        }
      }
    };
  }

  /**
   * Returns an <code>Iterator</code> that will return the elements in exactly
   * the order the subsequent call to <code>remove()</code> would do.
   * <p>
   * The oldest elements are returned first. <b>The <code>Iterator</code>
   * returned is not thread- safe! </b>
   * <p>
   * 
   * @return an <code>Iterator</code> that will return the elements in exactly
   *         the order the subsequent call to <code>remove()</code> would do.
   */
  public java.util.Iterator<T> iteratorL2F() {
    return new ARingBufferIterator() {
      {
        this.m_pos = RingBufferArrayFast.this.m_tailpointer;
      }

      /**
       * @see info.monitorenter.util.collections.RingBufferArrayFast.ARingBufferIterator#incPos()
       */
      @Override
      protected void incPos() {
        if (this.m_pos == RingBufferArrayFast.this.m_size) {
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
  @SuppressWarnings("unchecked")
  public T remove() {
    if (this.isEmpty()) {
      throw new IRingBuffer.RingBufferException("Buffer is empty.");
    }
    Object ret = null;
    ret = this.m_buffer[this.m_tailpointer];
    this.incTail();
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("Removing element: " + ret + " head: " + this.m_headpointer + " tail: "
          + this.m_tailpointer + " size: " + this.size());
    }
    return (T) ret;
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#removeAll()
   */
  @SuppressWarnings("unchecked")
  public T[] removeAll() {
    final Object[] ret = new Object[this.size()];
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("removeAll()");
    }
    for (int i = 0; i < ret.length; i++) {
      ret[i] = this.remove();
    }
    return (T[]) ret;
  }

  /**
   * Sets a new buffer- size. <br>
   * <p>
   * A new size is assigned but the elements "overhanging" are returned by the
   * <code>Object remove()</code>- method first. This may take time until the
   * buffer has its actual size again. Don't pretend on calling this method for
   * saving of memory very often as the whole buffer has to be copied into a new
   * array every time- and if newSize < getSize() additional the overhanging
   * elements references have to be moved to the internal
   * <code>List pendingremove</code>.
   * 
   * @param newSize
   *          the new size of the buffer.
   */
  public void setBufferSize(final int newSize) {
    final Object[] newbuffer = new Object[newSize];
    final boolean emptyStore = this.m_empty;
    int i = 0;
    int j = 0;
    if (RingBufferArrayFast.DEBUG) {
      System.out.println("setBufferSize(" + newSize + "): isEmpty(): " + this.isEmpty() + " tail: "
          + this.m_tailpointer + " head: " + this.m_headpointer);
    }
    // skip the oldest ones that are discarded
    final int oldSize = this.size();
    final int stop = oldSize - newSize;
    for (; (i < stop) && !this.isEmpty(); i++) {
      this.remove();
    }
    // add the ones that are the youngest (if some remaining)
    for (j = 0; (j < newSize) && !this.isEmpty(); j++) {
      newbuffer[j] = this.remove();
    }
    this.m_tailpointer = 0;
    this.m_headpointer = j;
    if (this.m_headpointer == newSize) {
      this.m_headpointer = 0;
    }
    this.m_buffer = newbuffer;
    this.m_size = newSize - 1;
    this.m_empty = emptyStore || (newSize == 0);
  }

  /**
   * @see info.monitorenter.util.collections.IRingBuffer#size()
   */
  public int size() {
    int result = 0;
    if (this.m_empty) {
      result = 0;
    } else if (this.m_headpointer == this.m_tailpointer) {
      result = this.m_size + 1;
    } else if (this.m_headpointer > this.m_tailpointer) {
      result = this.m_headpointer - this.m_tailpointer;
    } else {
      result = this.m_headpointer + this.m_size + 1 - this.m_tailpointer;
    }
    return result;
  }

  /**
   * Returns a string representation of the RingBuffer and it's contents.
   * <p>
   * Don't call this in your application too often: hard arraycopy - operation
   * an malloc are triggered.
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
      int i = 0;
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
