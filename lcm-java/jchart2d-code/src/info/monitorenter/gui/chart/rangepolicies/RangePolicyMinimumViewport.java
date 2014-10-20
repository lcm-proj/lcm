/*
 * RangePolicyMinimumViewport.java  of project jchart2d. Shows all points given 
 * but not any additional void space. 
 * Copyright (C) 2005 - 2011 Achim Westermann, created on 20.04.2005, 11:12:12
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
 * A <code>{@link info.monitorenter.gui.chart.IRangePolicy}</code> implementation that
 * guarantees a minimum displayed range (viewport) but will stretch if values of
 * the corresponding <code>{@link info.monitorenter.gui.chart.Chart2D}</code> exceeds these
 * constructor given bounds.
 * <p>
 * To sum up the policy of this implementation this
 * <code>{@link info.monitorenter.gui.chart.IRangePolicy}</code>
 * <ol>
 * <li>Guarantees to always display the constructor given range.
 * <li>Guarantees to always display every value within the
 * <code>{@link info.monitorenter.gui.chart.Chart2D}</code> (every
 * <code>{@link info.monitorenter.gui.chart.TracePoint2D}</code> of the chart's
 * <code>{@link info.monitorenter.gui.chart.ITrace2D}</code> instances).
 * </ol>
 * <p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.6 $
 */
public class RangePolicyMinimumViewport extends ARangePolicy {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 6561375203121878786L;

  /**
   * <p>
   * Constructs an instance that will always ensure that the given range will be
   * displayed.
   * </p>
   *
   * @param range
   *          the range that always should be visible.
   */
  public RangePolicyMinimumViewport(final Range range) {
    super(range);
  }

  /**
   * Returns the maximum of the chart or of the internal range if greater.
   * <p>
   *
   * @param chartMin
   *          ignored.
   *
   * @param chartMax
   *          returned if greater than the value of the internal range.
   *
   * @return Math.max(this.range.getMax(), chartMax).
   *
   * @see info.monitorenter.gui.chart.IRangePolicy#getMax(double, double)
   */
  public double getMax(final double chartMin, final double chartMax) {
    return Math.max(this.getRange().getMax(), chartMax);
  }

  /**
   * @see info.monitorenter.gui.chart.IRangePolicy#getMin(double, double)
   */
  public double getMin(final double chartMin, final double chartMax) {
    return Math.min(this.getRange().getMin(), chartMin);
  }
}
