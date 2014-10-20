/*
 * RangePolicyFixedViewport.java,  a range policy that forces the chart 
 * only to display the bounds of the internal range regardless of 
 * the actual bounds of the traces within the chart.
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
package info.monitorenter.gui.chart.rangepolicies;

import info.monitorenter.util.Range;

/**
 * A range policy that forces the chart only to display the bounds of the
 * internal range regardless of the actual bounds of the traces within the
 * chart.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.6 $
 * 
 */
public final class RangePolicyFixedViewport extends ARangePolicy {
  
  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 4403327213232041577L;

  /**
   * Creates a range policy with an unconfigured range ({@link Range#RANGE_UNBOUNDED}).
   * <p>
   */
  public RangePolicyFixedViewport() {
    super();
  }

  /**
   * Creates a range policy backed by the given range.
   * <p>
   * 
   * @param range
   *          the range that may be used to decide about the policy of
   *          displaying the range.
   */
  public RangePolicyFixedViewport(final Range range) {
    super(range);
  }

  /**
   * Returns the fixed maximum of the internal range.
   * <p>
   * 
   * @param chartMax
   *          ignored.
   * 
   * @param chartMin
   *          ignored.
   * 
   * @return the fixed maximum of the internal range.
   * 
   * @see info.monitorenter.gui.chart.IRangePolicy#getMax(double, double)
   */
  public double getMax(final double chartMin, final double chartMax) {
    return this.getRange().getMax();
  }

  /**
   * Returns the fixed minimum of the internal range.
   * <p>
   * 
   * @param chartMin
   *          ignored.
   * 
   * @param chartMax
   *          ignored.
   * 
   * @return the fixed minimum of the internal range.
   * 
   * @see info.monitorenter.gui.chart.IRangePolicy#getMin(double, double)
   */
  public double getMin(final double chartMin, final double chartMax) {
    return this.getRange().getMin();
  }

}
