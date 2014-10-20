/*
 * RangePolicyUnbounded.java  of project jchart2d, a viewport 
 * that always shows every point in every trace of a chart but uses no 
 * more space than necessary. 
 * Copyright (C) 2005 - 2011 Achim Westermann, created on 20.04.2005, 10:30:29
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
package info.monitorenter.gui.chart.rangepolicies;

import info.monitorenter.util.Range;

/**
 * A dummy IRangePolicy implementation that reflects the bounds of the connected
 * Chart2D instance.<p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 */
public final class RangePolicyUnbounded
    extends ARangePolicy {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 1008709094237454345L;

  /**
   * Creates a range policy with an unconfigured range ({@link Range#RANGE_UNBOUNDED}).
   * <p>
   */
  public RangePolicyUnbounded() {
    super();
  }

  /**
   * Creates a range policy backed by the given range which is not used for
   * {@link #getMax(double, double)} or {@link #getMin(double, double)} but
   * stored for later changes of the range policy from outside.
   * <p>
   * 
   * @param range
   *          the range that may be used to decide about the policy of
   *          displaying the range.
   * 
   * @see info.monitorenter.gui.chart.axis.AAxis#setRangePolicy(info.monitorenter.gui.chart.IRangePolicy)
   */
  public RangePolicyUnbounded(final Range range) {
    super(range);
  }

  /**
   * @see info.monitorenter.gui.chart.IRangePolicy#getMax(double,double)
   */
  public double getMax(final double chartMin, final double chartMax) {
    return chartMax;
  }

  /**
   * @see info.monitorenter.gui.chart.IRangePolicy#getMin(double, double)
   */
  public double getMin(final double chartMin, final double chartMax) {
    return chartMin;
  }

}
