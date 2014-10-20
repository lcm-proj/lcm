/*
 * RangePolicyHighestValuesForcedMin.java,
 * range policy that shows the highest values and forces to show empty
 * space towards the first highest values.
 *
 * Copyright (c) 2006 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
 * Range policy implementation that will show a viewport with only the highest
 * values that are in the range of maximum - x which also forces the lower bound
 * to be shown if no data towards this bound exists.
 * <p>
 * 
 * @author zoola
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 * 
 */
public final class RangePolicyHighestValuesForcedMin extends ARangePolicy {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -6200980210918463142L;

  /** The value range for the highest values to show. */
  private final double m_highestValueRangeToShow;

  /**
   * Constructor with a range and the value range for the highest values to show
   * only.
   * <p>
   * 
   * @param range
   *          unused, maximum bound is always returned.
   * 
   * @param highestValueRangeToShow
   *          the value range for the highest values to show.
   */
  public RangePolicyHighestValuesForcedMin(final Range range, final double highestValueRangeToShow) {
    super(range);
    this.m_highestValueRangeToShow = highestValueRangeToShow;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final RangePolicyHighestValuesForcedMin other = (RangePolicyHighestValuesForcedMin) obj;
    if (Double.doubleToLongBits(this.m_highestValueRangeToShow) != Double
        .doubleToLongBits(other.m_highestValueRangeToShow)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the maximum of the chart always.
   * <p>
   * 
   * @return Returns the maximum of the chart always.
   * 
   * @param chartMin
   *          ignored.
   * 
   * @param chartMax
   *          returned always.
   * 
   * @see info.monitorenter.gui.chart.IRangePolicy#getMax(double, double)
   */
  public double getMax(final double chartMin, final double chartMax) {
    return chartMax;
  }

  /**
   * Returns the maximum of the chart - interal highestValueRangeToShow.
   * <p>
   * 
   * 
   * @param chartMin
   *          unused: the lower bound is always controlled by max - internal
   *          highesValueRangeToShow.
   * 
   * @param chartMax
   *          upper bound to compute down to the start of the latest highest
   *          values.
   * 
   * @return the maximum of the chart - interal highestValueRangeToShow.
   * 
   * @see info.monitorenter.gui.chart.IRangePolicy#getMin(double, double)
   */
  public double getMin(final double chartMin, final double chartMax) {
    return chartMax - this.m_highestValueRangeToShow;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(this.m_highestValueRangeToShow);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
