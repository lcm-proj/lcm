/*
 *  TreeSetGreedy.java, special container for managing z-Indexes of traces in jchart2d.
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
package info.monitorenter.util.collections;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A <code>Set</code> that will always successfully add new instances and
 * guarantee that all the "<em>Comparable properties</em>" of the contained
 * <code>{@link info.monitorenter.util.collections.IComparableProperty}</code>
 * instances will build a set (no duplicates).
 * <p>
 * Although the interface of {@link java.util.Set} is preserved and allows
 * adding any <code>Object</code> <b>only
 * {@link info.monitorenter.util.collections.IComparableProperty} instances may
 * be added to <code>TreeSetGreedy</code> </b> because it uses a proprietary
 * {@link java.util.Comparator}.
 * <p>
 * The added <code>IComparableProperty</code> instances with the lowest
 * {@link java.lang.Number} property (see
 * {@link info.monitorenter.util.collections.IComparableProperty#getComparableProperty()}
 * ) will be returned first from the retrievable <code>Iterator</code>
 * <p>
 * <h2>Warning</h2>
 * If the <code>IComparableProperty</code> (thus meaning the member or accessed
 * data) is changed from outside, the internal order of this set will get
 * corrupted and iterations or add/remove calls may fail. Therefore it is
 * necessary to remove the instance before outside modification and later on add
 * it again.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @param <T>
 *          the type of instances to store.
 */
public class TreeSetGreedy<T extends IComparableProperty> extends TreeSet<T> implements Set<T> {

  /**
   * A <code>Comparator</code> that will compare {@link IComparableProperty}
   * instances by their {@link IComparableProperty#getComparableProperty()}
   * number.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @param <T>
   *          the type of instances to compare.
   */
  private static final class NumberPropertyComparator<T extends IComparableProperty> implements
      Comparator<T>, Serializable {

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 2279828650090806643L;

    /**
     * Defcon.
     * <p>
     */
    public NumberPropertyComparator() {
      // nop
    }

    /**
     * Compares two Objects by casting them to {@link IComparableProperty} and
     * using their {@link IComparableProperty#getComparableProperty()} property.
     * <p>
     * This <code>Comparator</code> may only be used in {@link java.util.Set}
     * instances that only contain <code>IComparableProperty</code> instances.
     * <p>
     * For two instances that are judged equal (equal z-index) the first
     * argument is decided to stay, the second instance gets assigned a higher
     * {@link IComparableProperty#setComparableProperty(Number)} by one, then is
     * removed from the <code>Set</code> it is working for and added anew thus
     * getting the next index order.
     * <p>
     * This procedure is expensive (performs <em>O(n!)</em>) but easy to
     * implement and guarantees that every <code>IComparableProperty</code>
     * instance is added (never returns 0), all instances have a different
     * values and the newest added instances definetely get their chosen value.
     * <p>
     * 
     * @param o1
     *          the first instance for comparison.
     * 
     * @param o2
     *          the second instance for comparison.
     * 
     * @throws ClassCastException
     *           if one of the given arguments does not implement
     *           {@link IComparableProperty}
     * 
     * @return -1 if o1 is first, 0, if both are equal and +1 if o1 comes last.
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final T o1, final T o2) throws ClassCastException {
      int result;
      // Don't take real identical elements (set policy)
      if (o1 == o2) {
        result = 0;
      } else {
        double i1 = o1.getComparableProperty().doubleValue();
        double i2 = o2.getComparableProperty().doubleValue();

        // fake and take equal elements.
        if (i1 < i2) {
          result = -1;
        } else if (i1 == i2) {
          result = 0;
        } else {
          result = 1;
        }
      }
      return result;
    }

  }

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3258130237048173623L;

  /**
   * Creates a new number of the correct type increased by 1.
   * <p>
   * 
   * @param n
   *          the number to increase.
   * 
   * @return a new number of the correct type increased by 1.
   */
  private static Number createDecreasedNumber(final Number n) {
    Number result;
    Class< ? > c = n.getClass();
    if (c == Integer.class) {
      result = new Integer(n.intValue() - 1);
    } else if (c == Double.class) {
      result = new Double(n.doubleValue() - 1);
    } else if (c == Float.class) {
      result = new Float(n.floatValue() - 1);
    } else if (c == Short.class) {
      result = new Short((short) (n.shortValue() - 1));
    } else if (c == Byte.class) {
      result = new Byte((byte) (n.byteValue() - 1));
    } else if (c == Long.class) {
      result = new Long(n.longValue() - 1);
    } else if (c == BigDecimal.class) {
      BigDecimal bd = new BigDecimal(n.toString());
      bd = bd.subtract(new BigDecimal(1));
      result = bd;
    } else {
      BigInteger bi = new BigInteger(n.toString());
      bi = bi.subtract(new BigInteger("1"));
      result = bi;
    }
    return result;
  }

  /**
   * Creates an instance with an internal <code>Comparator</code> to fulfill the
   * contract of this class.
   */
  public TreeSetGreedy() {
    super(new NumberPropertyComparator<IComparableProperty>());
  }

  /**
   * Attempts to add the the given <code>T</code>.
   * <p>
   * 
   * @param o
   *          the T to add.
   * @return see superclass.
   * @see java.util.TreeSet#add(java.lang.Object)
   */
  @Override
  public synchronized boolean add(final T o) {
    boolean ret = this.addInternal(o);
    // this.packComparableProperties();
    return ret;
  }

  /**
   * Internally adds the given instance.
   * <p>
   * 
   * @param o
   *          the instance to add.
   * 
   * @return true if the instance was added.
   */
  private boolean addInternal(final T o) {
    if (!this.isEmpty()) {
      // check if we have to manipulate the order to be able to store the
      // element.
      // Policy is: never change comparable property if it is avoidable!
      boolean alreadyThrere = this.contains(o);
      if (alreadyThrere) {
        T first = this.first();
        o.setComparableProperty(TreeSetGreedy.createDecreasedNumber(first.getComparableProperty()));
      }
    }
    boolean ret = super.add(o);
    return ret;
  }

  /**
   * @see java.util.TreeSet#remove(java.lang.Object)
   */
  @Override
  public boolean remove(final Object o) {
    boolean result = super.remove(o);
    return result;
  }

  /**
   * Modifies the values of the comparable properties of the contained elements
   * to have a continuous order of increasing integer values.
   * <p>
   * As the real value of the comparable property is unimportant for this set
   * but only the relation of the values (order) is of interest this method may
   * change all values to use only the minimum integer range for expressing the
   * order.
   * <p>
   * An example of the procedure:
   * 
   * <pre>
   *   [0, 10, 11, 22] -&gt; [0,1,2,3]
   * </pre>
   * 
   * <p>
   * This method allows to avoid exceeding bounds (e.g. between {
   * {@link info.monitorenter.gui.chart.ITrace2D#Z_INDEX_MIN} and
   * {@link info.monitorenter.gui.chart.ITrace2D#ZINDEX_MAX}) and allows that
   * changes of the comparable properties always have an effect. If in the
   * example above the 2nd instance would increase it's property by one from 10
   * to 11 nothing would happen to the order.
   * <p>
   * 
   */
  // private void packComparableProperties() {
  // int i = ITrace2D.ZINDEX_MAX;
  // for (IComparableProperty prop : this) {
  // prop.setComparableProperty(new Integer(i));
  // i++;
  // }
  // }
}
