/*
 *  IComparableProperty.java allows an instance to be compared by a member.
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

/**
 * An interface that allows implementors to let themselves be compared by a
 * <code>Comparable</code> {@link java.lang.Number} instance that may be
 * modified by the comparing classs.
 * <p>
 * This allows implementation of {@link java.util.Set} classes that guarantee to
 * take every new element by shifting the order of the contained operations with
 * an operation that modifies their comparable <code>Number</code>.
 * <p>
 * Note that the methods {@link #getComparableProperty()} and
 * {@link #setComparableProperty(Number)} have to be linked to the same member
 * or source of data (if more advanced) in a way that:
 * <p>
 *
 * <pre>
 Number number = &lt;initalisation&gt;;
 aComparableProperty.setComparableProperty(number);
 <b>1) number.equals(aComparableProperty.getComparableProperty());</b>
 <b>2) aComparableProperty.getComparableProperty().equals(number);</b>
 </pre>
 *
 * are both true.
 * </p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.6 $
 *
 */

public interface IComparableProperty extends Serializable {

  /**
   * Returns a {@link Number} this instance wants to be compared by.
   * <p>
   *
   * @return a {@link Number} this instance wants to be compared by.
   *
   */
  public Number getComparableProperty();

  /**
   * Set the comparable Number.
   * <p>
   *
   * Note that a <code>ComparableProperty</code> has to allow by contract that
   * it's <code>Number</code> property it lays open to be compared by has to
   * be modifiable from outside!
   * <p>
   *
   * @param n
   *          the comparable number.
   *
   */
  public void setComparableProperty(Number n);

}
