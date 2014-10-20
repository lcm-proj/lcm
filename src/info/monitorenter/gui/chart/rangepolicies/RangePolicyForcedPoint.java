/*
 * RangePolicyFixedViewport.java,  <enter purpose here>.
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
 * A range policy that forces the chart always to display the constructor given
 * point regardless of the actual bounds of the traces within the chart.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.8 $
 * 
 */
public final class RangePolicyForcedPoint extends RangePolicyMinimumViewport {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 5676959746627361093L;

  /**
   * Creates a range policy that ensures zero to be visible.
   * <p>
   */
  public RangePolicyForcedPoint() {
    super(new Range(0, 0));
  }

  /**
   * Creates a range policy backed by the given point in the dimension this
   * policy is used (x or y).
   * <p>
   * 
   * @param point
   *          the point that always has to be shown.
   */
  public RangePolicyForcedPoint(final double point) {
    super(new Range(point, point));
  }

  /**
   * Sets the point to ensure to be visible.
   * <p>
   * 
   * @param point
   *          the point to ensure to be visible.
   */
  public void setPoint(final double point) {
    super.setRange(new Range(point, point));
  }

  /**
   * This method is an invariant of the super class contract: only the minimum
   * value of the given range is used to enforce visibility.
   * <p>
   * 
   * Use {@link #setPoint(double)} instead.
   * 
   * @param range
   *          the internal range that may be taken into account for returning
   *          bounds from
   *          {@link info.monitorenter.gui.chart.IRangePolicy#getMax(double, double)}
   *          and
   *          {@link info.monitorenter.gui.chart.IRangePolicy#getMax(double, double)}.
   */
  @Override
  public void setRange(final Range range) {
    double min = range.getMin();
    if (min != range.getMax()) {
      super.setRange(new Range(min, min));
    } else {
      super.setRange(range);
    }
  }
}
