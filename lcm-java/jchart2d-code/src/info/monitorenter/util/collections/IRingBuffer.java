/*
 * IRingBuffer, an interface for implementations of a RingBuffer.
 * Copyright (c) 2004 - 2011 Achim Westermann, Achim.Westermann@gmx.de.
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


/**
 * Interface for implementations of RingBuffers.
 * <p>
 * 
 * @param <T>
 *            the type of instances to store in implementations of this ring
 *            buffer.
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.6 $
 */
public interface IRingBuffer<T> extends java.io.Serializable, Iterable<T> {

  /**
   * Special exception related to ring buffer operations.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.6 $
   */
  public final class RingBufferException
      extends RuntimeException {
    /**
     * Comment for <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 3762255244691714610L;

    /**
     * Creates an instance with the given message.
     * <p>
     * 
     * @param msg
     *            the message of the exception.
     */
    protected RingBufferException(final String msg) {
      super(msg);
    }
  }

  /**
   * Adds element to the RingBuffer.
   * <p>
   * 
   * If the buffer is full, an Exception will be thrown.
   * <p>
   * 
   * Note that <code>RingBufferException</code> does not need to be caught
   * because it is an inheritant of <code>java.lang.RuntimeException</code>.
   * Especially for the <code>Object add(Object element)</code>- method there
   * may be an implementation that never throws <code>BUFFER_FULL</code> but
   * returns the oldest element in case the buffer is full.
   * <p>
   * 
   * @param element
   *            the element to add.
   * 
   * @return the instance that had to be removed in order to add the new one or
   *         null, if the capacity was not reached yet.
   * 
   * @throws IRingBuffer.RingBufferException
   *             if the buffer cannot accept any more elements.
   * 
   * 
   */
  public T add(final T element) throws IRingBuffer.RingBufferException;

  /**
   * Clears the buffer without returning anything.
   * <p>
   * If the content is of no interest prefer using this method instead of
   * {@link #removeAll()} as it may be implemented in a much faster way (
   * <code>O(constant)</code> instead of <code>O(n)</code>).
   * <p>
   * 
   */
  public void clear();

  /**
   * Returns the absolute amount of space in the buffer.
   * <p>
   * 
   * @return the absolute amount of space in the buffer.
   */
  public int getBufferSize();

  /**
   * Returns the oldest element from the buffer. This method does not remove the
   * element.
   * <p>
   * 
   * @return the oldest element from the buffer.
   * 
   * @throws IRingBuffer.RingBufferException
   *             if the buffer is empty.
   */
  public T getOldest() throws IRingBuffer.RingBufferException;

  /**
   * Returns the last element added. This method does not remove the element.
   * <p>
   * 
   * @return the last element added.
   * 
   * @throws IRingBuffer.RingBufferException
   *             if the buffer is empty.
   */
  public T getYoungest() throws IRingBuffer.RingBufferException;

  /**
   * Tests whether no elements are stored in the buffer.
   * <p>
   * 
   * @return true if no element is stored in the buffer.
   */
  public boolean isEmpty();

  /**
   * Returns true if no more space in the buffer is available. This method
   * should be used to test before calling {@link #add(Object)}.
   * <p>
   * 
   * 
   * @return true if no more space in the buffer is available.
   */
  public boolean isFull();

  /**
   * Returns an iterator starting from the first (youngest) to the last (oldest)
   * element.
   * <p>
   * 
   * @return an iterator starting from the first (youngest) to the last (oldest)
   *         element.
   */
  public java.util.Iterator<T> iteratorF2L();

  /**
   * Returns an iterator starting from the last (oldest) to the first (youngest)
   * element.
   * 
   * @return an iterator starting from the last (oldest) to the first (youngest)
   *         element.
   */
  public java.util.Iterator<T> iteratorL2F();

  /**
   * Removes the oldest element from the buffer.
   * <p>
   * 
   * @return the removed oldest element from the buffer.
   * 
   * @throws IRingBuffer.RingBufferException
   *             if the buffer is empty.
   */
  public T remove() throws IRingBuffer.RingBufferException;

  /**
   * Clears the buffer. It will return all of it's stored elements.
   * <p>
   * 
   * @return all removed elements.
   */
  public T[] removeAll();

  /**
   * Sets a new buffer- size.
   * <p>
   * 
   * Implementations may vary on handling the problem that the new size is
   * smaller than the actual amount of elements in the buffer: <br>
   * The oldest elements may be thrown away.
   * <p>
   * A new size is assigned but the elements "overhanging" are returned by the
   * <code>Object remove()</code>- method first. This may take time until the
   * buffer has its actual size again.
   * <p>
   * 
   * @param newSize
   *            the new buffer size to set.
   */
  public void setBufferSize(final int newSize);

  /**
   * Returns the actual amount of elements stored in the buffer.
   * <p>
   * 
   * @return the actual amount of elements stored in the buffer.
   */
  public int size();

}
