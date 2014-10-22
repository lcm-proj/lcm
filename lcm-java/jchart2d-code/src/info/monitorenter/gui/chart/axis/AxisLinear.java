/*
 *  AxisLinear.java of project jchart2d, Axis implementation with linear display.
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 20:33:13.
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
package info.monitorenter.gui.chart.axis;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyAutomaticBestFit;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterSimple;
import info.monitorenter.util.Range;
import info.monitorenter.util.math.MathUtil;

/**
 * An <code>{@link AAxis}</code> with linear display of values.
 * <p>
 * 
 * @param <T>
 *          Subtypes may be more picky which scale policies the accept to
 *          disallow incorrect scales: This supports it (see
 *          {@link IAxis#setAxisScalePolicy(IAxisScalePolicy)}).  
 *           
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.22 $
 */
public class AxisLinear<T extends IAxisScalePolicy> extends AAxis<T> {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 4725336592625669661L;

  /**
   * Creates an instance that uses a {@link LabelFormatterSimple} for formatting
   * numbers and a {@link AxisScalePolicyAutomaticBestFit} for controlling the
   * scale.
   * <p>
   * 
   */
  @SuppressWarnings("unchecked")
  public AxisLinear() {
    /*
     * Necessary cast a java bug?
     */
    this(new LabelFormatterSimple(), (T)new AxisScalePolicyAutomaticBestFit());
  }

  /**
   * Constructor that uses the given label formatter for formatting labels.
   * <p>
   * 
   * @param formatter
   *          needed for formatting labels of this axis.
   * 
   */
  @SuppressWarnings("unchecked")
  public AxisLinear(final IAxisLabelFormatter formatter) {
    /*
     * Necessary cast a java bug?
     */
    this(formatter, (T)new AxisScalePolicyAutomaticBestFit());
  }
  
  /**
   * Constructor that uses the given label formatter for formatting labels along with the given 
   * scale policy.
   * <p>
   * 
   * @param formatter
   *          needed for formatting labels of this axis.
   * 
   * @param scalePolicy
   *          controls the ticks/labels and their distance.
   */
  public AxisLinear(final IAxisLabelFormatter formatter, final T scalePolicy) {
    super(formatter, scalePolicy);
  }

  /**
   * @see info.monitorenter.gui.chart.axis.AAxis#createAccessor(info.monitorenter.gui.chart.Chart2D,
   *      int, int)
   */
  @Override
  protected AChart2DDataAccessor createAccessor(final Chart2D chart, final int dimension,
      final int position) {
    AChart2DDataAccessor result;
    if (dimension == Chart2D.X) {
      // Don't allow a combination of dimension and position that is not usable:
      if ((position & (Chart2D.CHART_POSITION_BOTTOM | Chart2D.CHART_POSITION_TOP)) == 0) {
        throw new IllegalArgumentException("X axis only valid with top or bottom position.");
      }
      this.setAxisPosition(position);
      result = new XDataAccessor(chart);
    } else if (dimension == Chart2D.Y) {
      // Don't allow a combination of dimension and position that is not usable:
      if ((position & (Chart2D.CHART_POSITION_LEFT | Chart2D.CHART_POSITION_RIGHT)) == 0) {
        throw new IllegalArgumentException("Y axis only valid with left or right position.");
      }
      this.setAxisPosition(position);
      result = new YDataAccessor(chart);
    } else {
      throw new IllegalArgumentException("Dimension has to be Chart2D.X or Chart2D.Y!");
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getScaledValue(double)
   */
  public double getScaledValue(final double absolute) {
    Range range = this.getRange();
    double scalerX = range.getExtent();
    double result = (absolute - range.getMin()) / scalerX;
    if (!MathUtil.isDouble(result)) {
      result = 0;
    }
    return result;
  }
}
