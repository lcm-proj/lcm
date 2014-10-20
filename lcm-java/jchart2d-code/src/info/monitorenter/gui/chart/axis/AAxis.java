/*
 *  AAxis.java (bold as love), base class for an axis  of the Chart2D.
 *  Copyright (C) 2007 -2011 Achim Westermann, created on 20:33:13.
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
import info.monitorenter.gui.chart.IAxisTickPainter;
import info.monitorenter.gui.chart.IAxisTitlePainter;
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.LabeledValue;
import info.monitorenter.gui.chart.axis.scalepolicy.AxisScalePolicyAutomaticBestFit;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterAutoUnits;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterSimple;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyUnbounded;
import info.monitorenter.util.ExceptionUtil;
import info.monitorenter.util.Range;
import info.monitorenter.util.StringUtil;
import info.monitorenter.util.math.MathUtil;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The base class for an axis of the <code>{@link Chart2D}</code>.
 * <p>
 * Normally - as the design and interaction of an <code>Axis</code> with the
 * <code>{@link Chart2D}D</code> is very fine-grained - it is not instantiated
 * by users of jchart2d: It is automatically instantiated by the constructor of
 * <code>Chart2D</code>. It then may be retrieved from the <code>Chart2D</code>
 * by the methods {@link Chart2D#getAxisX()} and {@link Chart2D#getAxisY()} for
 * further configuration.
 * <p>
 * 
 * @param <T>
 *          Subtypes may be more picky which scale policies the accept to
 *          disallow incorrect scales: This supports it (see
 *          {@link IAxis#setAxisScalePolicy(IAxisScalePolicy)}).  
 *          
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.61 $
 */
public abstract class AAxis<T extends IAxisScalePolicy> implements IAxis<T>, PropertyChangeListener {

  /**
   * Used for finding ticks with labels and controlling their value / distance.
   */
  private IAxisScalePolicy m_axisScalePolicy;

  /**
   * @see info.monitorenter.gui.chart.IAxis#getAxisScalePolicy()
   */
  public IAxisScalePolicy getAxisScalePolicy() {
    return this.m_axisScalePolicy;
  }

  /**
   * The default used is <code>{@link AxisScalePolicyAutomaticBestFit}</code>.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.IAxis#setAxisScalePolicy(info.monitorenter.gui.chart.IAxisScalePolicy)
   */
  public IAxisScalePolicy setAxisScalePolicy(final T axisScalePolicy) {
    // TODO Event management for update painting
    IAxisScalePolicy result = this.m_axisScalePolicy;
    this.m_axisScalePolicy = axisScalePolicy;
    this.m_propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this,
        IAxis.PROPERTY_AXIS_SCALE_POLICY_CHANGED, result, this.m_axisScalePolicy));

    return result;
  }

  /**
   * An internal connector class that will connect the axis to the a Chart2D.
   * <p>
   * It is aggregated to the {@link AAxis} in order to access either y values or
   * x values of the Chart2D thus making the IAxis an Y Axis or X axis. This
   * strategy reduces redundant code for label creation. It avoids complex
   * inheritance / interface implements for different IAxis implementation that
   * would be necessary for y-axis / x-axis implementations.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   */
  public abstract class AChart2DDataAccessor implements Serializable {

    /** Generated <code>serialVersionUID</code>. **/
    private static final long serialVersionUID = 5023422232812082122L;

    /** The chart that is accessed. */
    protected Chart2D m_chart;

    /**
     * Constructor with the chart that is accessed.
     * <p>
     * 
     * @param chart
     *          the chart that is accessed.
     */
    protected AChart2DDataAccessor(final Chart2D chart) {

      AAxis.this.setAccessor(this);
      this.m_chart = chart;
    }

    /**
     * Returns the chart that is accessed.
     * <p>
     * 
     * @return the chart that is accessed.
     */
    public final Chart2D getChart() {
      return this.m_chart;
    }

    /**
     * Returns the constant for the dimension that is accessed on the chart.
     * <p>
     * 
     * @return {@link Chart2D#X}, {@link Chart2D#Y} or -1 if this axis is not
     *         assigned to a chart.
     */
    public abstract int getDimension();

    /**
     * Returns the height in pixel the corresponding axis needs to paint itself.
     * <p>
     * This includes the axis line, it's ticks and labels and it's title.
     * <p>
     * 
     * @param g2d
     *          needed for font metric information.
     * @return the height in pixel the corresponding axis needs to paint itself.
     */
    public abstract int getHeight(Graphics g2d);

    /**
     * Returns the maximum pixels that will be needed to paint a label.
     * <p>
     * 
     * @param g2d
     *          provides information about the graphics context to paint on
     *          (e.g. font size).
     * @return the maximum pixels that will be needed to paint a label.
     */
    protected abstract double getMaximumPixelForLabel(final Graphics g2d);

    /**
     * Returns the max value of the given trace according to the dimension the
     * outer axis belongs to.
     * <p>
     * This is either an x or an y value depending on the dimension the outer
     * axis is working in.
     * <p>
     * 
     * @param trace
     *          the trace to read the maximum of.
     * @return the max value of the given trace according to the dimension the
     *         outer axis belongs to.
     */
    protected abstract double getMaxValue(ITrace2D trace);

    /**
     * Returns the minimum amount of increase in the value that will be needed
     * to display all labels without overwriting each others.
     * <p>
     * This procedure needs the amount of pixels needed by the largest possible
     * label and relies on the implementation of
     * {@link #getMaximumPixelForLabel(Graphics)}, whose result is multiplied
     * with the "value per pixel" quantifier.
     * <p>
     * 
     * @param g2d
     *          the current graphic context to use in case further information
     *          is required.
     * @return the minimum amount of increase in the value that will be needed
     *         to display all labels without overwriting each others.
     */
    public abstract double getMinimumValueDistanceForLabels(final Graphics g2d);

    /**
     * Returns the min value of the given trace according to the dimension the
     * outer axis belongs to.
     * <p>
     * This is either an x or an y value depending on the dimension the outer
     * axis is working in.
     * <p>
     * 
     * @param trace
     *          the trace to read the maximum of.
     * @return the min value of the given trace according to the dimension the
     *         outer axis belongs to.
     */
    protected abstract double getMinValue(ITrace2D trace);

    /**
     * Returns the amount of pixel available for displaying the values on the
     * chart in the dimension this accessor stands for.
     * <p>
     * This method must not be called within the first lines of a paint cycle
     * (necessary underlying values then are computed new).
     * <p>
     * 
     * @return the amount of pixel available for displaying the values on the
     *         chart in the dimension this accessor stands for.
     */
    protected abstract int getPixelRange();

    /**
     * Returns the value of the given point according to the dimension the outer
     * axis belongs to.
     * <p>
     * This is either <code>{@link ITracePoint2D#getX()}</code> or
     * <code>{@link ITracePoint2D#getY()}</code>.
     * <p>
     * 
     * @param point
     *          the point to read <code>{@link ITracePoint2D#getX()}</code> or
     *          <code>{@link ITracePoint2D#getY()}</code> from.
     * @return the value of the given point according to the dimension the outer
     *         axis belongs to.
     */
    protected abstract double getValue(ITracePoint2D point);

    /**
     * Returns the value distance on the current chart that exists for the given
     * amount of pixel distance in the given direction of this
     * <code>AAxis</code>.
     * <p>
     * Depending on the width of the actual Chart2D and the contained values,
     * the relation between displayed distances (pixel) and value distances (the
     * values of the added
     * <code>{@link info.monitorenter.gui.chart.ITrace2D}</code> instances
     * changes.
     * <p>
     * This method calculates depending on the actual painting area of the
     * Chart2D, the shift in value between two points that have a screen
     * distance of the given pixel. <br>
     * This method is not used by the chart itself but a helper for outside use.
     * <p>
     * 
     * @param pixel
     *          The desired distance between to scale points of the x- axis in
     *          pixel.
     * @return a scaled (from pixel to internal value-range) and normed (to the
     *         factor of the current unit of the axis) value usable to calculate
     *         the coordinates for the scale points of the axis.
     */
    protected abstract double getValueDistanceForPixel(int pixel);

    /**
     * Returns the width in pixel the corresponding axis needs to paint itself.
     * <p>
     * This includes the axis line, it's ticks and labels and it's title.
     * <p>
     * 
     * @param g2d
     *          needed for font metric information.
     * @return the width in pixel the corresponding axis needs to paint itself.
     */
    public abstract int getWidth(Graphics g2d);

    /**
     * Scales the given trace in the dimension represented by this axis.
     * <p>
     * This method is not deadlock - safe and should be called by the
     * <code>{@link Chart2D}</code> only!
     * <p>
     * 
     * @param trace
     *          the trace to scale.
     * @param range
     *          the range to use as scaler.
     */
    protected abstract void scaleTrace(ITrace2D trace, Range range);

    /**
     * Returns the translation of the mouse event coordinates of the given mouse
     * event to the value within the chart for the dimension (x,y) covered by
     * this axis.
     * <p>
     * Note that the mouse event has to be an event fired on this component!
     * <p>
     * 
     * @param mouseEvent
     *          a mouse event that has been fired on this component.
     * @return the translation of the mouse event coordinates of the given mouse
     *         event to the value within the chart for the dimension covered by
     *         this axis (x or y) or null if no calculations could be performed
     *         as the chart was not painted before.
     */
    public abstract double translateMousePosition(final MouseEvent mouseEvent);

    /**
     * Transforms the given pixel value (which has to be a awt value like
     * {@link java.awt.event.MouseEvent#getX()} into the chart value.
     * <p>
     * Internal use only, the interface does not guarantee that the pixel
     * corresponds to any valid awt pixel value within the chart component.
     * <p>
     * Warning: A value transformed to a pixel by
     * {@link #translateValueToPx(double)} and then retransformed by
     * {@link #translatePxToValue(int)} will most often have changed, as the
     * transformation from value to px a) has to hit an exact int b) most often
     * will map from a bigger domain (value range) to a smaller one (range of
     * chart on the screen).
     * <p>
     * 
     * @param pixel
     *          a pixel value of the chart component as used by awt.
     * @return the awt pixel value transformed to the chart value.
     */
    public abstract double translatePxToValue(final int pixel);

    /**
     * Transforms the given chart data value into the corresponding awt pixel
     * value for the chart.
     * <p>
     * The inverse transformation to {@link #translatePxToValue(int)}.
     * <p>
     * 
     * @param value
     *          a chart data value.
     * @return the awt pixel value corresponding to the chart data value.
     */
    public abstract int translateValueToPx(final double value);
  }

  /**
   * Base implementation that delegates the call to a template method after
   * synchronization on the chart.
   * <p>
   * 
   * @author Achim Westermann
   * @version $Revision: 1.61 $
   * @since 3.0.0
   */
  private abstract static class APropertyChangeReactorSynced implements
      AAxis.IPropertyChangeReactor {

    /**
     * Defcon.
     * <p>
     */
    protected APropertyChangeReactorSynced() {
      super();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.IPropertyChangeReactor#propertyChange(java.beans.PropertyChangeEvent,
     *      info.monitorenter.gui.chart.axis.AAxis)
     */
    public final void propertyChange(final PropertyChangeEvent changeEvent, final AAxis<?> receiver) {
      final Chart2D chart = receiver.getAccessor().getChart();
      synchronized (chart) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("AAxis.propertyChange (" + Thread.currentThread().getName()
              + "), 1 lock");
        }
        final boolean repaint = this.propertyChangeSynced(changeEvent, receiver);
        if (repaint) {
          chart.setRequestedRepaint(true);
        }
      }
    }

    /**
     * Handle the property change of the given receiver.
     * <p>
     * This method is invoked with synchronization on the chart of the receiver.
     * <p>
     * Please note: You must not react upon the property name because the sole
     * intention of this interface is to have a map with the property name as
     * key and access implementations of this interface very fast which allows
     * to avoid large else if - code for identification of the proper property
     * name for the code to execute.
     * <p>
     * Implementations are highly proprietary as they have to trust that they
     * are only invoked with the correct property change events!!!
     * <p>
     * 
     * @param changeEvent
     *          the change event the receiver received.
     * @param receiver
     *          the original receiver of the change event.
     * @return if true a repaint request will be marked for the chart.
     */
    protected abstract boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
        final AAxis<?> receiver);

  }

  /**
   * Internal encapsulation of the code to react upon a property change.
   * <p>
   * Used on the value side of a map to have a fast dispatch to the code to
   * react as axis listens to a lot of different properties.
   * <p>
   * 
   * @author Achim Westermann
   * @version $Revision: 1.61 $
   * @since 3.0.0
   */
  private static interface IPropertyChangeReactor {
    /**
     * Handle the property change of the given receiver.
     * <p>
     * Please note: You must not react upon the property name because the sole
     * intention of this interface is to have a map with the property name as
     * key and access implementations of this interface very fast which allows
     * to avoid large else if - code for identification of the proper property
     * name for the code to execute.
     * <p>
     * Implementations are highly proprietary as they have to trust that they
     * are only invoked with the correct property change events!!!
     * <p>
     * 
     * @param changeEvent
     *          the change event the receiver received.
     * @param receiver
     *          the original receiver of the change event.
     */
    public void propertyChange(PropertyChangeEvent changeEvent, final AAxis<?> receiver);
  }

  /**
   * Reused property change listener that will signal the chart to repaint.
   * TODO: Enter a comment that ends with a '.'
   * <p>
   * 
   * @author Achim Westermann
   * @version $Revision: 1.61 $
   * @since 3.0.0
   */
  private static final class PropertyChangeRepainter implements AAxis.IPropertyChangeReactor {

    /**
     * Defcon, internal use only.
     * <p>
     */
    protected PropertyChangeRepainter() {
      super();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.IPropertyChangeReactor#propertyChange(java.beans.PropertyChangeEvent,
     *      info.monitorenter.gui.chart.axis.AAxis)
     */
    public void propertyChange(final PropertyChangeEvent changeEvent, final AAxis<?> receiver) {
      final AAxis<?>.AChart2DDataAccessor accessor = receiver.getAccessor();
      // only if this axis is already connected to a chart:
      if (accessor != null) {
        final Chart2D parent = accessor.getChart();
        if (parent != null) {
          parent.setRequestedRepaint(true);
        }
      }
    }
  }

  /**
   * An accessor for the x axis of a chart.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de>Achim Westermann </a>
   * @see Chart2D#getAxisX()
   */
  public class XDataAccessor extends AAxis<T>.AChart2DDataAccessor {

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 1185826702304621485L;

    /**
     * Creates an instance that accesses the given chart's x axis.
     * <p>
     * 
     * @param chart
     *          the chart to access.
     */
    public XDataAccessor(final Chart2D chart) {

      super(chart);
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getDimension()
     */
    @Override
    public int getDimension() {
      return Chart2D.X;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getHeight(java.awt.Graphics)
     */
    @Override
    public int getHeight(final Graphics g2d) {
      final FontMetrics fontdim = g2d.getFontMetrics();
      // the height of the font:
      int height = fontdim.getHeight();
      // and the height of a major tick mark:
      height += this.getChart().getAxisTickPainter().getMajorTickLength();
      // and the height of the axis title:
      height += AAxis.this.getAxisTitle().getTitlePainter().getHeight(AAxis.this, g2d);
      return height;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMaximumPixelForLabel(Graphics)
     */
    @Override
    protected final double getMaximumPixelForLabel(final Graphics g2d) {

      final FontMetrics fontdim = g2d.getFontMetrics();
      final int fontwidth = fontdim.charWidth('0');
      /*
       * multiply with longest possible number. longest possible number is the
       * non-fraction part of the highest number plus the maximum amount of
       * fraction digits plus one for the fraction separator dot.
       */
      final int len = AAxis.this.getFormatter().getMaxAmountChars();
      return fontwidth * (len + 2);
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMaxValue(info.monitorenter.gui.chart.ITrace2D)
     */
    @Override
    protected double getMaxValue(final ITrace2D trace) {
      return trace.getMaxX();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMinimumValueDistanceForLabels(java.awt.Graphics)
     */
    @Override
    public final double getMinimumValueDistanceForLabels(final Graphics g2d) {

      double result;
      final Dimension d = this.m_chart.getSize();
      final int pxrange = (int) d.getWidth() - 60;
      if (pxrange <= 0) {
        result = 1;
      } else {
        double valuerange = AAxis.this.getMax() - AAxis.this.getMin();
        if (valuerange == 0) {
          valuerange = 10;
        }
        final double pxToValue = valuerange / pxrange;
        result = pxToValue * this.getMaximumPixelForLabel(g2d);
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMinValue(info.monitorenter.gui.chart.ITrace2D)
     */
    @Override
    protected double getMinValue(final ITrace2D trace) {
      return trace.getMinX();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getPixelRange()
     */
    @Override
    protected int getPixelRange() {
      return this.m_chart.getXChartEnd() - this.m_chart.getXChartStart();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getValue(info.monitorenter.gui.chart.ITracePoint2D)
     */
    @Override
    protected double getValue(final ITracePoint2D point) {
      return point.getX();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getValueDistanceForPixel(int)
     */
    @Override
    protected final double getValueDistanceForPixel(final int pixel) {
      double result;
      final Dimension d = this.m_chart.getSize();
      final int pxrangex = (int) d.getWidth() - 60;
      if (pxrangex <= 0) {
        result = -1d;
      } else {
        final double valuerangex = AAxis.this.getMax() - AAxis.this.getMin();
        final double pxToValue = valuerangex / pxrangex;
        result = pxToValue * pixel;
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getWidth(java.awt.Graphics)
     */
    @Override
    public int getWidth(final Graphics g2d) {
      final FontMetrics fontdim = g2d.getFontMetrics();
      // only the space required for the right side label:
      final int fontwidth = fontdim.charWidth('0');
      final int rightSideOverhang = (AAxis.this.getFormatter().getMaxAmountChars()) * fontwidth;

      return rightSideOverhang;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#scaleTrace(info.monitorenter.gui.chart.ITrace2D,
     *      info.monitorenter.util.Range)
     */
    @Override
    protected void scaleTrace(final ITrace2D trace, final Range range) {
      Iterator<ITracePoint2D> itPoints;
      final double scaler = range.getExtent();
      if (trace.isVisible()) {
        itPoints = trace.iterator();
        ITracePoint2D point;
        while (itPoints.hasNext()) {
          point = itPoints.next();
          final double absolute = point.getX();
          double result = (absolute - range.getMin()) / scaler;
          if (!MathUtil.isDouble(result)) {
            result = 0;
          }
          point.setScaledX(result);
        }
      }
    }

    /**
     * Returns "X".
     * <p>
     * 
     * @return "X"
     */
    @Override
    public String toString() {
      return "X";
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#translateMousePosition(java.awt.event.MouseEvent)
     */
    @Override
    public final double translateMousePosition(final MouseEvent mouseEvent) {

      return this.translatePxToValue(mouseEvent.getX());
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#translatePxToValue(int)
     */
    @Override
    public double translatePxToValue(final int pixel) {
      double result = 0;
      // relate to the offset:
      final double px = pixel - this.m_chart.getXChartStart();

      final int rangeX = this.m_chart.getXChartEnd() - this.m_chart.getXChartStart();
      if (rangeX != 0) {
        final double scaledX = px / rangeX;
        final Range valueRangeX = AAxis.this.getRange();
        result = scaledX * valueRangeX.getExtent() + valueRangeX.getMin();
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#translateValueToPx(double)
     */
    @Override
    public int translateValueToPx(final double value) {
      int result = 0;
      // first normalize to [00.0..1.0]
      double valueNormalized;
      // cannot use AAxis.this.getMax() / getMin() because log axis will
      // transform those values!
      final Range valueRange = AAxis.this.getRange();
      valueNormalized = (value - valueRange.getMin()) / valueRange.getExtent();
      // now expand into the pixel space:
      final int rangeX = this.getPixelRange();
      if (rangeX == 0) {
        // return null
      } else {
        final double tmpResult = (valueNormalized * rangeX + this.m_chart.getXChartStart());
        result = (int) Math.round(tmpResult);
      }
      return result;
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getDimensionString()
   */
  public String getDimensionString() {
    String result = null;
    if (this.m_accessor != null) {
      result = this.m_accessor.toString();
    }
    return result;
  }

  /**
   * Accesses the y axis of the {@link Chart2D}.
   * <p>
   * 
   * @see AAxis#setAccessor(info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor)
   * @see Chart2D#getAxisY()
   */
  protected class YDataAccessor extends AAxis<T>.AChart2DDataAccessor {

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -3665759247443586028L;

    /**
     * Creates an instance that accesses the y axis of the given chart.
     * <p>
     * 
     * @param chart
     *          the chart to access.
     */
    public YDataAccessor(final Chart2D chart) {

      super(chart);
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getDimension()
     */
    @Override
    public final int getDimension() {
      return Chart2D.Y;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getHeight(java.awt.Graphics)
     */
    @Override
    public final int getHeight(final Graphics g2d) {
      final FontMetrics fontdim = g2d.getFontMetrics();
      // only the space required for the right side label:
      int fontHeight = fontdim.getHeight();
      // -4 is for showing colons of x - labels that are below the baseline
      fontHeight += 4;
      return fontHeight;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMaximumPixelForLabel(Graphics)
     * @param g2d
     *          the current graphic context to use in case further information
     *          is required.
     */
    @Override
    protected final double getMaximumPixelForLabel(final Graphics g2d) {

      final FontMetrics fontdim = g2d.getFontMetrics();
      final int fontheight = fontdim.getHeight();
      return fontheight + 10;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMaxValue(info.monitorenter.gui.chart.ITrace2D)
     */
    @Override
    protected double getMaxValue(final ITrace2D trace) {
      return trace.getMaxY();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMinimumValueDistanceForLabels(Graphics)
     */
    @Override
    public final double getMinimumValueDistanceForLabels(final Graphics g2d) {

      double result;
      final Dimension d = this.m_chart.getSize();
      final int pxrange = (int) d.getHeight() - 40;
      if (pxrange <= 0) {
        result = 1;
      } else {
        double valuerange = AAxis.this.getMax() - AAxis.this.getMin();
        if (valuerange == 0) {
          valuerange = 10;
        }
        final double pxToValue = valuerange / pxrange;
        result = pxToValue * this.getMaximumPixelForLabel(g2d);
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getMinValue(info.monitorenter.gui.chart.ITrace2D)
     */
    @Override
    protected double getMinValue(final ITrace2D trace) {
      return trace.getMinY();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getPixelRange()
     */
    @Override
    protected final int getPixelRange() {
      return this.m_chart.getYChartStart() - this.m_chart.getYChartEnd();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getValue(info.monitorenter.gui.chart.ITracePoint2D)
     */
    @Override
    protected double getValue(final ITracePoint2D point) {
      return point.getY();
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getValueDistanceForPixel(int)
     */
    @Override
    protected final double getValueDistanceForPixel(final int pixel) {
      double result;
      final Dimension d = this.m_chart.getSize();
      final int pxrangey = (int) d.getHeight() - 40;
      if (pxrangey <= 0) {
        result = -1d;
      } else {
        final double valuerangey = AAxis.this.getMaxValue() - AAxis.this.getMinValue();
        final double pxToValue = valuerangey / pxrangey;
        result = pxToValue * pixel;
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#getWidth(java.awt.Graphics)
     */
    @Override
    public final int getWidth(final Graphics g2d) {
      final FontMetrics fontdim = g2d.getFontMetrics();
      // the width of the font:
      final int fontWidth = fontdim.charWidth('0');
      // times the maximum amount of chars:
      int height = fontWidth * AAxis.this.getFormatter().getMaxAmountChars();
      // and the height of a major tick mark:
      height += this.getChart().getAxisTickPainter().getMajorTickLength();
      // and the Width of the axis title:
      height += AAxis.this.getAxisTitle().getTitlePainter().getWidth(AAxis.this, g2d);
      return height;

    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#scaleTrace(info.monitorenter.gui.chart.ITrace2D,
     *      info.monitorenter.util.Range)
     */
    @Override
    protected void scaleTrace(final ITrace2D trace, final Range range) {
      if (trace.isVisible()) {
        final double scaler = range.getExtent();
        final Iterator<ITracePoint2D> itPoints = trace.iterator();
        ITracePoint2D point;
        while (itPoints.hasNext()) {
          point = itPoints.next();
          final double absolute = point.getY();
          double result = (absolute - range.getMin()) / scaler;
          if (!MathUtil.isDouble(result)) {
            result = 0;
          }
          point.setScaledY(result);
        }
      }

    }

    /**
     * Returns "Y".
     * <p>
     * 
     * @return "Y"
     */
    @Override
    public String toString() {
      return "Y";
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#translateMousePosition(java.awt.event.MouseEvent)
     */
    @Override
    public double translateMousePosition(final MouseEvent mouseEvent) {

      return this.translatePxToValue(mouseEvent.getY());
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#translatePxToValue(int)
     */
    @Override
    public double translatePxToValue(final int pixel) {
      double result = 0;
      // invert, as awt px are higher the lower the chart value is:
      final double px = this.m_chart.getYChartStart() - pixel;

      final int rangeY = this.m_chart.getYChartStart() - this.m_chart.getYChartEnd();
      if (rangeY != 0) {
        final double scaledY = px / rangeY;
        final Range valueRangeY = AAxis.this.getRange();
        result = scaledY * valueRangeY.getExtent() + valueRangeY.getMin();
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.chart.axis.AAxis.AChart2DDataAccessor#translateValueToPx(double)
     */
    @Override
    public int translateValueToPx(final double value) {
      int result = 0;
      // first normalize to [00.0..1.0]
      double valueNormalized;
      // cannot use AAxis.this.getMax() / getMin() because log axis will
      // transform those values!
      final Range valueRange = AAxis.this.getRange();
      valueNormalized = (value - valueRange.getMin()) / valueRange.getExtent();
      // now expand into the pixel space:
      final int rangeY = this.getPixelRange();
      if (rangeY == 0) {
        // return null
      } else {
        result = (int) Math.round(this.m_chart.getYChartStart() - valueNormalized * rangeY);
      }
      return result;
    }

  }

  /** Debugging flag for sysouts. */
  public static final boolean DEBUG = false;

  /**
   * Internal fast access to the right property change code encapsulation via
   * the property name.
   * <p>
   * This is done for better performance - an old endless
   * <code>..else if(propertyName.equals(..))..</code> has been replaced by
   * this.
   * <p>
   */
  private static SortedMap<String, AAxis.IPropertyChangeReactor> propertyReactors;

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = -3615740476406530580L;

  static {
    AAxis.propertyReactors = new TreeMap<String, AAxis.IPropertyChangeReactor>();
    // Don't waste the heap for stateless code:
    final IPropertyChangeReactor repaintReactor = new PropertyChangeRepainter();
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_STROKE, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_STROKE, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_PAINTERS, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_COLOR, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_NAME, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_ERRORBARPOLICY, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_ERRORBARPOLICY_CONFIGURATION, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_ZINDEX, repaintReactor);
    AAxis.propertyReactors.put(IAxis.PROPERTY_LABELFORMATTER, repaintReactor);
    AAxis.propertyReactors.put(IAxisLabelFormatter.PROPERTY_FORMATCHANGE, repaintReactor);
    AAxis.propertyReactors.put(AxisTitle.PROPERTY_TITLEFONT, repaintReactor);
    AAxis.propertyReactors.put(AxisTitle.PROPERTY_TITLE, repaintReactor);
    AAxis.propertyReactors.put(AxisTitle.PROPERTY_TITLEPAINTER, repaintReactor);
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_MAX_X, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("pc-Xmax");
        }
        final AAxis<?>.AChart2DDataAccessor accessor = receiver.getAccessor();
        // only care if axis works in x dimension:
        if (accessor.getDimension() == Chart2D.X) {
          final double value = ((Double) changeEvent.getNewValue()).doubleValue();
          if (value > receiver.m_max) {
            final ITrace2D trace = (ITrace2D) changeEvent.getSource();
            if (trace.isVisible()) {
              receiver.m_max = value;
              receiver.m_needsFullRescale = true;
              result = true;
            }
          } else if (value < receiver.m_max) {
            receiver.m_max = receiver.findMax();
            receiver.m_needsFullRescale = true;
            result = true;
          }
        }
        return result;
      }

    });
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_MIN_X, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("pc-Xmin");
        }
        if (receiver.getAccessor().getDimension() == Chart2D.X) {
          final double value = ((Double) changeEvent.getNewValue()).doubleValue();
          if (value < receiver.m_min) {
            final ITrace2D trace = (ITrace2D) changeEvent.getSource();
            if (trace.isVisible()) {
              receiver.m_min = value;
              receiver.m_needsFullRescale = true;
              result = true;
            }
          } else if (value > receiver.m_min) {
            receiver.m_min = receiver.findMin();
            receiver.m_needsFullRescale = true;
            result = true;
          }
        }
        return result;
      }

    });
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_MAX_Y, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("pc-Ymax");
        }
        // only care if axis works in y dimension:
        if (receiver.getAccessor().getDimension() == Chart2D.Y) {
          final double value = ((Double) changeEvent.getNewValue()).doubleValue();
          if (value > receiver.m_max) {
            final ITrace2D trace = (ITrace2D) changeEvent.getSource();
            if (trace.isVisible()) {
              receiver.m_max = value;
              receiver.m_needsFullRescale = true;
              result = true;
            }
          } else if (value < receiver.m_max) {
            receiver.m_max = receiver.findMax();
            receiver.m_needsFullRescale = true;
            result = true;
          }
        }
        return result;
      }
    });
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_MIN_Y, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("pc-Ymin");
        }
        if (receiver.getAccessor().getDimension() == Chart2D.Y) {

          final double value = ((Double) changeEvent.getNewValue()).doubleValue();
          if (value < receiver.m_min) {
            final ITrace2D trace = (ITrace2D) changeEvent.getSource();
            if (trace.isVisible()) {
              receiver.m_min = value;
              receiver.m_needsFullRescale = true;
              result = true;
            }
          } else if (value > receiver.m_min) {
            receiver.m_min = receiver.findMin();
            receiver.m_needsFullRescale = true;
            result = true;
          }
        }
        return result;
      }
    });
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_TRACEPOINT, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        // now points added or removed -> rescale!
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("pc-tp");
        }
        final ITracePoint2D oldPt = (ITracePoint2D) changeEvent.getOldValue();
        final ITracePoint2D newPt = (ITracePoint2D) changeEvent.getNewValue();
        // added or removed?
        // we only care about added points (rescaling is our task)
        if (oldPt == null) {
          receiver.scalePoint(newPt);
          result = true;
        }
        return result;
      }
    });
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_VISIBLE, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        // invisible traces don't count for max and min, so
        // expensive search has to be started here:
        // TODO: Do performance: Get the trace of the event and check only
        // it's bounds here!!!
        receiver.m_max = receiver.findMax();
        receiver.m_min = receiver.findMin();
        // if the trace that became visible does not exceed bounds
        // it will not cause a "dirty Scaling" -> updateScaling and
        // repainting (in Painter Thread).
        final ITrace2D trace = (ITrace2D) changeEvent.getSource();
        receiver.scaleTrace(trace);
        result = true;
        return result;
      }
    });
    AAxis.propertyReactors.put(ITrace2D.PROPERTY_POINT_CHANGED, new APropertyChangeReactorSynced() {

      /**
       * @see info.monitorenter.gui.chart.axis.AAxis.APropertyChangeReactorSynced#propertyChangeSynced(java.beans.PropertyChangeEvent,
       *      info.monitorenter.gui.chart.axis.AAxis)
       */
      @Override
      protected boolean propertyChangeSynced(final PropertyChangeEvent changeEvent,
          final AAxis<?> receiver) {
        boolean result = false;
        final ITracePoint2D changed = (ITracePoint2D) changeEvent.getNewValue();
        receiver.scalePoint(changed);
        result = true;
        return result;
      }
    });
  }

  /**
   * The accessor to the Chart2D.
   * <p>
   * It determines, which axis (x or y) this instance is representing.
   * <p>
   */
  protected AChart2DDataAccessor m_accessor;

  /** The position of this axis on the chart. */
  private int m_axisPosition = -1;

  /**
   * The title of this axis, by default an empty title with an <code>
   * {@link info.monitorenter.gui.chart.axistitlepainters.AxisTitlePainterDefault}
   * </code>.
   */
  private IAxis.AxisTitle m_axisTitle;

  /**
   * Formatting of the labels.
   */
  protected IAxisLabelFormatter m_formatter;

  /**
   * The major tick spacing for label generations.
   * <p>
   * 
   * @see #setMajorTickSpacing(double)
   */

  protected double m_majorTickSpacing = 5;

  /** The current maximum value for all points in all traces. */
  protected double m_max;

  /** The current minimum value for all points in all traces. */
  protected double m_min;

  /**
   * The minor tick spacing for label generations.
   * <p>
   * 
   * @see #setMinorTickSpacing(double)
   */
  protected double m_minorTickSpacing = 1;

  /**
   * Flag to detect if a re-scaling has to be done.
   * <p>
   * It is set to false in <code>{@link #scale()}</code> which is triggered from
   * the painting Thread. Whenever a bound change is detected in
   * <code>{@link #propertyChange(PropertyChangeEvent)}</code> this is set to
   * true.
   * <p>
   * Please remind: In previous versions there was only a test if the bounds had
   * changed since the last scaling. This was not always correct: If in between
   * two paint cycles the bounds were changed and new points added but at the
   * point in time when the 2nd paint cycle starts the bounds would be equal no
   * full rescaling would be performed even if the added points would have been
   * scaled in relation to the changed bounds at their adding time: Bounds
   * checks are not sufficient!
   * <p>
   */
  protected boolean m_needsFullRescale = false;

  /** Boolean switch for painting x grid lines. * */
  private boolean m_paintGrid = false;

  /** Boolean switch for painting the scale in this dimension. */
  private boolean m_paintScale = true;

  /** The left x pixel coordinate of this axis. */
  private int m_pixelXLeft;

  /** The right x pixel coordinate of this axis. */
  private int m_pixelXRight;

  /** The bottom y pixel coordinate of this axis. */
  private int m_pixelYBottom;

  /** The top y pixel coordinate of this axis. */
  private int m_pixelYTop;

  /** Support for acting as a property change event producer for listeners. */
  private final PropertyChangeSupport m_propertyChangeSupport;

  /**
   * A plugable range policy.
   */
  protected IRangePolicy m_rangePolicy;

  /**
   * The range used for scaling in the previous paint operation.
   * <p>
   * This is used for detection of dirty scaling.
   * <p>
   */
  private final Range m_rangePreviousScaling = new Range(0, 0);

  /** Reused range for <code>{@link #getRange()}</code>. */
  private final Range m_reusedRange = new Range(0, 0);

  /**
   * Controls whether scale values are started from major ticks.
   * <p>
   * Default is false.
   * <p>
   */
  private boolean m_startMajorTick = false;

  /**
   * The internal <code>Set</code> used to store the different
   * <code>ITrace2d</code> instances to paint with z-index ordering based on
   * <code>{@link ITrace2D#getZIndex()}</code>.
   * <p>
   * It is crucial to use a set implementation here that is not backed by a map. To be more precise: 
   * It is crucial to use an implementation that will use equals whenever operations like contains are invoked 
   * instead of searching by a computed key. In the latter case you could add traces here (at that point in time a key 
   * is computed from the trace state) then modify the traces (e.g. adding points) and later when trying to remove the 
   * trace the given traces's key would be computed but no key for it found. 
   */
  private final Set<ITrace2D> m_traces = new CopyOnWriteArraySet<ITrace2D>();

  /**
   * True if this axis is to be painted on a chart; false to hide.
   */
  private boolean m_visible = true;

  /**
   * Default constructor that uses a {@link LabelFormatterAutoUnits} for
   * formatting labels.
   * <p>
   */
  @SuppressWarnings("unchecked")
  public AAxis() {
    /*
     * I consider this necessary cast to T as a bug (compare with declaration if T in class header):
     */
    this(new LabelFormatterAutoUnits(new LabelFormatterSimple()),
        (T)new AxisScalePolicyAutomaticBestFit());
  }

  /**
   * Constructor that uses the given label formatter for formatting labels.
   * <p>
   * 
   * @param formatter
   *          needed for formatting labels of this axis.
   * 
   * @param scalePolicy
   *          controls the ticks/labels and their distance.
   */
  public AAxis(final IAxisLabelFormatter formatter, final T scalePolicy) {
    this.m_propertyChangeSupport = new PropertyChangeSupport(this);
    this.setAxisTitle(new AxisTitle(null));
    this.m_rangePolicy = new RangePolicyUnbounded(Range.RANGE_UNBOUNDED);
    this.setFormatter(formatter);
    this.setAxisScalePolicy(scalePolicy);
  }

//  public AAxis(LabelFormatterAutoUnits labelFormatterAutoUnits,
//      AxisScalePolicyAutomaticBestFit axisScalePolicyAutomaticBestFit) {
//    this(labelFormatterAutoUnits, axisScalePolicyAutomaticBestFit);
//  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(final String propertyName,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#addTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public boolean addTrace(final ITrace2D trace) {

    boolean result = false;
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName() + "Aaxis" + this.getDimensionString()
          + ".addTrace(), 0 locks");
    }
    synchronized (this.getAccessor().getChart()) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName() + ", AAxis" + this.getDimensionString()
            + ".addTrace(), 1 lock");
      }
      synchronized (trace) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println(Thread.currentThread().getName() + ", AAxis"
              + this.getDimensionString() + ".addTrace(), 2 locks");
        }
        if (this.m_traces.contains(trace)) {
          throw new IllegalArgumentException("Trace " + trace.getName()
              + " is already contaied in this axis " + this.getAxisTitle() + ". Review your code. ");
        }
        // do it here:
        result = this.m_traces.add(trace);
        if (result) {
          this.listen2Trace(trace);

          // for static traces (all points added) we won't get events.
          // so update here:
          final double max = this.getAccessor().getMaxValue(trace);
          if (max > this.m_max) {
            this.m_max = max;
          }
          final double min = this.getAccessor().getMinValue(trace);
          if (min < this.m_min) {
            this.m_min = min;
          }
          // special case: first trace added:
          if (this.getTraces().size() == 1) {
            this.m_min = min;
            this.m_max = max;
          }
          if (Chart2D.DEBUG_THREADING) {
            System.out.println(Thread.currentThread().getName() + ", AAxis"
                + this.getDimensionString() + ".addTrace(), before installing chart to trace "
                + trace.getName());
            ExceptionUtil.dumpThreadStack(System.out);

          }
          trace.setRenderer(this.m_accessor.getChart());
          if (Chart2D.DEBUG_THREADING) {
            System.out.println(Thread.currentThread().getName() + ", AAxis"
                + this.getDimensionString() + ".addTrace(), after installing chart to trace "
                + trace.getName());
          }
          // unconditionally scale the trace as we don't know which
          // bounds it was related to before.
          this.scaleTrace(trace);
        }

      }
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName() + ", AAxis" + this.getDimensionString()
            + ".addTrace(), left 1 lock: 1 remaining");
      }
    }
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName() + ", AAxis" + this.getDimensionString()
          + ".addTrace(), left 1 lock:  0 remaining");
    }
    // A deadlock occurs if a listener triggers paint.
    // This was the case with ChartPanel.
    this.m_propertyChangeSupport.firePropertyChange(IAxis.PROPERTY_ADD_REMOVE_TRACE, null, trace);

    return result;
  }

  /**
   * Template method to create the proper <code>
   * {@link AAxis.AChart2DDataAccessor}</code>
   * implementation.
   * <p>
   * 
   * @param chart
   *          the chart to access.
   * @param dimension
   *          <code>{@link Chart2D#X}</code> or <code>{@link Chart2D#Y}</code>.
   * @param position
   *          <code>{@link Chart2D#CHART_POSITION_BOTTOM}</code>, <code>
   *          {@link Chart2D#CHART_POSITION_LEFT}</code>, <code>
   *          {@link Chart2D#CHART_POSITION_RIGHT}</code> or <code>
   *          {@link Chart2D#CHART_POSITION_TOP}</code>.
   * @return the proper <code>{@link AAxis.AChart2DDataAccessor}</code>
   *         implementation.
   */
  protected abstract AAxis<T>.AChart2DDataAccessor createAccessor(Chart2D chart, int dimension, int position);

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
    final AAxis<?> other = (AAxis<?>) obj;
    if (this.m_accessor == null) {
      if (other.m_accessor != null) {
        return false;
      }
    } else if (!this.m_accessor.equals(other.m_accessor)) {
      return false;
    }
    if (this.m_axisPosition != other.m_axisPosition) {
      return false;
    }
    if (this.m_axisTitle == null) {
      if (other.m_axisTitle != null) {
        return false;
      }
    } else if (!this.m_axisTitle.equals(other.m_axisTitle)) {
      return false;
    }
    if (this.m_formatter == null) {
      if (other.m_formatter != null) {
        return false;
      }
    } else if (!this.m_formatter.equals(other.m_formatter)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_majorTickSpacing) != Double
        .doubleToLongBits(other.m_majorTickSpacing)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_max) != Double.doubleToLongBits(other.m_max)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_min) != Double.doubleToLongBits(other.m_min)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_minorTickSpacing) != Double
        .doubleToLongBits(other.m_minorTickSpacing)) {
      return false;
    }
    if (this.m_needsFullRescale != other.m_needsFullRescale) {
      return false;
    }
    if (this.m_paintGrid != other.m_paintGrid) {
      return false;
    }
    if (this.m_paintScale != other.m_paintScale) {
      return false;
    }
    if (this.m_pixelXLeft != other.m_pixelXLeft) {
      return false;
    }
    if (this.m_pixelXRight != other.m_pixelXRight) {
      return false;
    }
    if (this.m_pixelYBottom != other.m_pixelYBottom) {
      return false;
    }
    if (this.m_pixelYTop != other.m_pixelYTop) {
      return false;
    }
    if (this.m_propertyChangeSupport == null) {
      if (other.m_propertyChangeSupport != null) {
        return false;
      }
    } else if (!this.m_propertyChangeSupport.equals(other.m_propertyChangeSupport)) {
      return false;
    }
    if (this.m_rangePolicy == null) {
      if (other.m_rangePolicy != null) {
        return false;
      }
    } else if (!this.m_rangePolicy.equals(other.m_rangePolicy)) {
      return false;
    }
    if (this.m_rangePreviousScaling == null) {
      if (other.m_rangePreviousScaling != null) {
        return false;
      }
    } else if (!this.m_rangePreviousScaling.equals(other.m_rangePreviousScaling)) {
      return false;
    }
    if (this.m_reusedRange == null) {
      if (other.m_reusedRange != null) {
        return false;
      }
    } else if (!this.m_reusedRange.equals(other.m_reusedRange)) {
      return false;
    }
    if (this.m_startMajorTick != other.m_startMajorTick) {
      return false;
    }
    if (this.m_traces == null) {
      if (other.m_traces != null) {
        return false;
      }
    } else if (!this.m_traces.equals(other.m_traces)) {
      return false;
    }
    if (this.m_visible != other.m_visible) {
      return false;
    }
    return true;
  }

  /**
   * Searches for the maximum value of all contained ITraces in the dimension
   * this axis stands for.
   * <p>
   * This method is triggered when a trace fired a property change for property
   * <code>{@link ITrace2D#PROPERTY_MAX_X}</code> or
   * <code>{@link ITrace2D#PROPERTY_MAX_Y}</code> with a value lower than the
   * internal stored maximum.
   * <p>
   * Performance breakdown is avoided because all <code>ITrace2D</code>
   * implementations cache their max and min values.
   * <p>
   * 
   * @return the maximum value of all traces for the dimension this axis works
   *         in.
   */
  protected final double findMax() {
    double max = -Double.MAX_VALUE;
    double tmp;
    final Iterator<ITrace2D> it = this.getTraces().iterator();
    ITrace2D trace;
    while (it.hasNext()) {
      trace = it.next();
      if (trace.isVisible()) {
        tmp = this.getAccessor().getMaxValue(trace);
        if (tmp > max) {
          max = tmp;
        }
      }
    }
    if (max == -Double.MAX_VALUE) {
      max = 10;
    }
    return max;
  }

  /**
   * Searches for the minimum value of all contained ITraces in the dimension
   * this axis stands for.
   * <p>
   * This method is triggered when a trace fired a property change for property
   * <code>{@link ITrace2D#PROPERTY_MAX_X}</code> or
   * <code>{@link ITrace2D#PROPERTY_MAX_Y}</code> with a value lower than the
   * internal stored minimum.
   * <p>
   * Performance breakdown is avoided because all <code>ITrace2D</code>
   * implementations cache their max and min values.
   * <p>
   * 
   * @return the minimum value of all traces for the dimension this axis works
   *         in.
   */

  protected final double findMin() {
    double min = Double.MAX_VALUE;
    double tmp;
    final Iterator<ITrace2D> it = this.getTraces().iterator();
    ITrace2D trace;
    while (it.hasNext()) {
      trace = it.next();
      if (trace.isVisible()) {
        tmp = this.getAccessor().getMinValue(trace);
        if (tmp < min) {
          min = tmp;
        }
      }
    }
    if (min == Double.MAX_VALUE) {
      min = 0;
    }
    return min;
  }

  /**
   * Returns the accessor to the chart.
   * <p>
   * 
   * @return the accessor to the chart.
   */
  public AChart2DDataAccessor getAccessor() {
    return this.m_accessor;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getAxisPosition()
   */
  public int getAxisPosition() {
    return this.m_axisPosition;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getAxisTitle()
   */
  public AxisTitle getAxisTitle() {
    return this.m_axisTitle;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getDimension()
   */
  public int getDimension() {
    int result = -1;
    if (this.m_accessor != null) {
      result = this.m_accessor.getDimension();
    }
    return result;
  }

  /**
   * @return Returns the formatter.
   */
  public final IAxisLabelFormatter getFormatter() {

    return this.m_formatter;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getHeight(java.awt.Graphics)
   */
  public final int getHeight(final Graphics g2d) {

    return this.getAccessor().getHeight(g2d);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getMajorTickSpacing()
   */
  public double getMajorTickSpacing() {
    return this.m_majorTickSpacing;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getMax()
   */
  public double getMax() {
    return this.getRangePolicy().getMax(this.m_min, this.m_max);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getMaxValue()
   */
  public double getMaxValue() {
    return this.m_max;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getMin()
   */
  public double getMin() {
    return this.getRangePolicy().getMin(this.m_min, this.m_max);
  }

  /**
   * Get the minor tick spacing for label generation.
   * <p>
   * 
   * @return he minor tick spacing for label generation.
   * @see #setMinorTickSpacing(double)
   */
  public double getMinorTickSpacing() {
    return this.m_minorTickSpacing;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getMinValue()
   */
  public double getMinValue() {
    return this.m_min;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getPixelXLeft()
   */
  public final int getPixelXLeft() {
    return this.m_pixelXLeft;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getPixelXRight()
   */
  public final int getPixelXRight() {
    return this.m_pixelXRight;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getPixelYBottom()
   */
  public final int getPixelYBottom() {
    return this.m_pixelYBottom;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getPixelYTop()
   */
  public final int getPixelYTop() {
    return this.m_pixelYTop;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getPropertyChangeListeners(java.lang.String)
   */
  public PropertyChangeListener[] getPropertyChangeListeners(final String propertyName) {
    return this.m_propertyChangeSupport.getPropertyChangeListeners(propertyName);
  }

  /**
   * This method is used by the Chart2D to scale it's values during painting.
   * <p>
   * Caution: This method does not necessarily return the Range configured with
   * {@link #setRange(Range)}. The internal {@link IRangePolicy} is taken into
   * account.
   * <p>
   * 
   * @return the range corresponding to the upper and lower bound of the values
   *         that will be visible on this Axis of the Chart2D.
   * 
   * @see #setRangePolicy(IRangePolicy)
   */
  public final Range getRange() {
    final double min = this.getMin();
    double max = this.getMax();
    if (min == max) {
      max += 10;
    }
    this.m_reusedRange.setMax(max);
    this.m_reusedRange.setMin(min);
    return this.m_reusedRange;
  }

  /**
   * Returns the range policy of this axis.
   * <p>
   * 
   * @return the range policy of this axis.
   */
  public IRangePolicy getRangePolicy() {

    return this.m_rangePolicy;
  }

  /**
   * @deprecated use {@link #getAxisTitle()} and on the result
   *             {@link IAxis.AxisTitle#getTitle()}.
   */
  @Deprecated
  public final String getTitle() {
    return this.getAxisTitle().getTitle();
  }

  /**
   * @deprecated this method might be dropped because the painter should be of
   *             no concern.
   * @see info.monitorenter.gui.chart.IAxis#getTitlePainter()
   */
  @Deprecated
  public final IAxisTitlePainter getTitlePainter() {
    return this.getAxisTitle().getTitlePainter();
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getTraces()
   */
  public Set<ITrace2D> getTraces() {
    return this.m_traces;
  }

  /**
   * Returns the value distance on the current chart that exists for the given
   * amount of pixel distance in the given direction of this <code>Axis</code>.
   * <p>
   * Depending on the width of the actual Chart2D and the contained values, the
   * relation between displayed distances (pixel) and value distances (the
   * values of the addes
   * <code>{@link info.monitorenter.gui.chart.ITrace2D}</code> instances
   * changes.
   * <p>
   * This method calculates depending on the actual painting area of the
   * Chart2D, the shift in value between two points that have a screen distance
   * of the given pixel. <br>
   * This method is not used by the chart itself but a helper for outside use.
   * <p>
   * 
   * @param pixel
   *          The desired distance between to scalepoints of the x- axis in
   *          pixel.
   * @return a scaled (from pixel to internal value-range) and normed (to the
   *         factor of the current unit of the axis) value usable to calculate
   *         the coords for the scalepoints of the axis.
   */
  protected final double getValueDistanceForPixel(final int pixel) {
    return this.m_accessor.getValueDistanceForPixel(pixel);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#getWidth(java.awt.Graphics)
   */
  public final int getWidth(final Graphics g2d) {
    return this.getAccessor().getWidth(g2d);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_accessor == null) ? 0 : this.m_accessor.hashCode());
    result = prime * result + this.m_axisPosition;
    result = prime * result + ((this.m_axisTitle == null) ? 0 : this.m_axisTitle.hashCode());
    result = prime * result + ((this.m_formatter == null) ? 0 : this.m_formatter.hashCode());
    long temp;
    temp = Double.doubleToLongBits(this.m_majorTickSpacing);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_max);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_min);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_minorTickSpacing);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (this.m_needsFullRescale ? 1231 : 1237);
    result = prime * result + (this.m_paintGrid ? 1231 : 1237);
    result = prime * result + (this.m_paintScale ? 1231 : 1237);
    result = prime * result + this.m_pixelXLeft;
    result = prime * result + this.m_pixelXRight;
    result = prime * result + this.m_pixelYBottom;
    result = prime * result + this.m_pixelYTop;
    result = prime * result
        + ((this.m_propertyChangeSupport == null) ? 0 : this.m_propertyChangeSupport.hashCode());
    result = prime * result + ((this.m_rangePolicy == null) ? 0 : this.m_rangePolicy.hashCode());
    result = prime * result
        + ((this.m_rangePreviousScaling == null) ? 0 : this.m_rangePreviousScaling.hashCode());
    result = prime * result + ((this.m_reusedRange == null) ? 0 : this.m_reusedRange.hashCode());
    result = prime * result + (this.m_startMajorTick ? 1231 : 1237);
    result = prime * result + ((this.m_traces == null) ? 0 : this.m_traces.hashCode());
    result = prime * result + (this.m_visible ? 1231 : 1237);
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#hasTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public final boolean hasTrace(final ITrace2D trace) {
    // TODO: Null enters here for tooltips with arithmetic mean trace!!!
    boolean result = false;
    result = this.m_traces.contains(trace);
    return result;
  }

  /**
   * Performs expensive calculations for various values that are used by many
   * calls throughout a paint iterations.
   * <p>
   * These values are constant throughout a paint iteration by the contract that
   * no point is added removed or changed in this period. Because these values
   * are used from many methods it is impossible to calculate them at a
   * "transparent" method that may perform this caching over a paint period
   * without knowledge from outside. The first method called in a paint
   * iteration is called several further times in the iteration. So this is the
   * common hook to invoke before painting a chart.
   * <p>
   */
  public void initPaintIteration() {

    // This is needed e.g. for LabelFormatterAutoUnits to choose the unit
    // according to the actual range of this paint iteration.
    this.m_formatter.initPaintIteration();
    this.m_axisScalePolicy.initPaintIteration(this);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#isDirtyScaling()
   */
  public final boolean isDirtyScaling() {
    boolean result = this.m_needsFullRescale;
    if (!result) {
      Range range;
      range = this.getRange();
      result |= !range.equals(this.m_rangePreviousScaling);
    }
    return result;
  }

  /**
   * Returns whether the x grid is painted or not.
   * <p>
   * 
   * @return whether the x grid is painted or not.
   */
  public final boolean isPaintGrid() {
    return this.m_paintGrid;
  }

  /**
   * Returns whether the scale for this axis should be painted or not.
   * <p>
   * 
   * @return whether the scale for this axis should be painted or not.
   */
  public final boolean isPaintScale() {
    return this.m_paintScale;
  }

  /**
   * Check whether scale values are started from major ticks.
   * <p>
   * 
   * @return true if scale values start from major ticks.
   * @see AAxis#setMajorTickSpacing(double)
   */
  public boolean isStartMajorTick() {
    return this.m_startMajorTick;
  }

  /**
   * Check if this axis is visible, i.e. needs to be painted on the chart.
   * <p>
   * 
   * @return true if this axis has to be painted on the chart.
   */
  public boolean isVisible() {
    return this.m_visible;
  }

  /**
   * Adds this axis as a listener to all property change events of the given
   * trace that are needed here.
   * <p>
   * 
   * @param trace
   *          the trace to listen to.
   */
  private void listen2Trace(final ITrace2D trace) {
    // listen to bound changes and more
    if (this.getAccessor().getDimension() == Chart2D.X) {
      trace.addPropertyChangeListener(ITrace2D.PROPERTY_MAX_X, this);
      trace.addPropertyChangeListener(ITrace2D.PROPERTY_MIN_X, this);
    } else {
      trace.addPropertyChangeListener(ITrace2D.PROPERTY_MAX_Y, this);
      trace.addPropertyChangeListener(ITrace2D.PROPERTY_MIN_Y, this);
    }
    // These are repaint candidates:
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_COLOR, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_STROKE, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_VISIBLE, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_ZINDEX, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_PAINTERS, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_ERRORBARPOLICY, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_ERRORBARPOLICY_CONFIGURATION, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_ZINDEX, this);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_NAME, this);
    // listen to newly added points
    // this is needed for scaling at point level.
    // else every bound change would force to rescale all traces!
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_TRACEPOINT, this);
    // listen to changed points whose location was changed:
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_POINT_CHANGED, this);
  }

  /**
   * Internal routine for an axis to listen to the new given axis title.
   * <p>
   * Factored out to have a better overview of event handling (vs. putting this
   * code in setters).
   * <p>
   * 
   * @param axisTitle
   *          the axis title to listen to.
   */
  private void listenToAxisTitle(final AxisTitle axisTitle) {
    axisTitle.addPropertyChangeListener(AxisTitle.PROPERTY_TITLE, this);
    axisTitle.addPropertyChangeListener(AxisTitle.PROPERTY_TITLEFONT, this);
    axisTitle.addPropertyChangeListener(AxisTitle.PROPERTY_TITLEPAINTER, this);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#paint(java.awt.Graphics)
   */
  public void paint(final Graphics g2d) {
    if (!this.m_visible) {
      return;
    }
    switch (this.getDimension() | this.getAxisPosition()) {
      case (Chart2D.X | Chart2D.CHART_POSITION_BOTTOM): {
        this.paintAxisXBottom(g2d);
        break;
      }
      case (Chart2D.X | Chart2D.CHART_POSITION_TOP): {
        this.paintAxisXTop(g2d);
        break;
      }
      case (Chart2D.Y | Chart2D.CHART_POSITION_LEFT): {
        this.paintAxisYLeft(g2d);
        break;
      }
      case (Chart2D.Y | Chart2D.CHART_POSITION_RIGHT): {
        this.paintAxisYRight(g2d);
        break;
      }
      default: {

        throw new IllegalStateException("No valid Chart position found for this axis: " + this);
      }
    }
  }

  /**
   * Internally paints this axis in the case it is assigned as an x axis on
   * bottom of the corresponding chart.
   * <p>
   * 
   * @param g2d
   *          the graphics context to use.
   */
  private void paintAxisXBottom(final Graphics g2d) {
    final Chart2D chart = this.getAccessor().getChart();
    int tmp = 0;
    final FontMetrics fontdim = g2d.getFontMetrics();
    final int fontheight = fontdim.getHeight();

    final int xAxisStart = chart.getXChartStart();
    final int xAxisEnd = chart.getXChartEnd();
    final int yAxisEnd = chart.getYChartEnd();
    final int rangexPx = xAxisEnd - xAxisStart;

    final int yAxisLine = this.getPixelYTop();

    g2d.drawLine(xAxisStart, yAxisLine, xAxisEnd, yAxisLine);
    // drawing the x title :
    this.paintTitle(g2d);
    // drawing tick - scale, corresponding values, grid and conditional unit
    // label:
    if (this.isPaintScale() || this.isPaintGrid()) {
      final IAxisTickPainter tickPainter = chart.getAxisTickPainter();
      tmp = 0;
      final List<LabeledValue> labels = this.m_axisScalePolicy.getScaleValues(g2d, this);

      for (final LabeledValue label : labels) {
        tmp = xAxisStart + (int) (label.getValue() * rangexPx);
        // true -> is bottom axis:
        if (this.isPaintScale()) {
          tickPainter.paintXTick(tmp, yAxisLine, label.isMajorTick(), true, g2d);
          tickPainter.paintXLabel(tmp, yAxisLine + fontheight, label.getLabel(), g2d);
        }
        if (this.isPaintGrid()) {
          // do not paint over the axis
          if (tmp != xAxisStart) {
            g2d.setColor(chart.getGridColor());
            g2d.drawLine(tmp, yAxisLine - 1, tmp, yAxisEnd);
            g2d.setColor(chart.getForeground());

          }
        }
      }
    }
    // unit-labeling
    g2d.drawString(this.getFormatter().getUnit().getUnitName(), xAxisEnd, yAxisLine + 4
        + fontdim.getHeight() * 2);

  }

  /**
   * Internally paints this axis in the case it is assigned as an x axis on top
   * of the corresponding chart.
   * <p>
   * 
   * @param g2d
   *          the graphics context to use.
   */
  private void paintAxisXTop(final Graphics g2d) {
    final Chart2D chart = this.getAccessor().getChart();
    int tmp = 0;
    final FontMetrics fontdim = g2d.getFontMetrics();

    final int xAxisStart = chart.getXChartStart();
    final int xAxisEnd = chart.getXChartEnd();
    final int yAxisStart = chart.getYChartStart();
    final int rangexPx = xAxisEnd - xAxisStart;
    // 1.2) x axis top:
    final int yAxisLine = this.getPixelYBottom();
    g2d.drawLine(xAxisStart, yAxisLine, xAxisEnd, yAxisLine);
    // drawing the x title :
    this.paintTitle(g2d);
    // drawing tick - scale, corresponding values, grid and conditional unit
    // label:
    if (this.isPaintScale()||this.isPaintGrid()) {
      // first for x- angle.
      tmp = 0;
      final IAxisTickPainter tickPainter = chart.getAxisTickPainter();
      final int majorTickLength = tickPainter.getMajorTickLength();
      final List<LabeledValue> labels = this.m_axisScalePolicy.getScaleValues(g2d, this);
      for (final LabeledValue label : labels) {
        tmp = xAxisStart + (int) (label.getValue() * rangexPx);
        if (this.isPaintScale()) {
          // 2nd boolean false -> is not bottom axis (top):
          tickPainter.paintXTick(tmp, yAxisLine, label.isMajorTick(), false, g2d);
          tickPainter.paintXLabel(tmp, yAxisLine - majorTickLength, label.getLabel(), g2d);
        }
        if (this.isPaintGrid()) {
          // do not paint over the axis:
          if (tmp != xAxisStart) {
            g2d.setColor(chart.getGridColor());
            g2d.drawLine(tmp, yAxisLine + 1, tmp, yAxisStart);
            g2d.setColor(chart.getForeground());

          }
        }
      }
      // unit-labeling
      g2d.drawString(this.getFormatter().getUnit().getUnitName(), xAxisEnd, yAxisLine - 4
          - fontdim.getHeight());
    }

  }

  /**
   * Internally paints this axis in the case it is assigned as a y axis on the
   * left side of the corresponding chart.
   * <p>
   * 
   * @param g2d
   *          the graphics context to use.
   */
  private void paintAxisYLeft(final Graphics g2d) {
    final Chart2D chart = this.getAccessor().getChart();
    int tmp = 0;
    final FontMetrics fontdim = g2d.getFontMetrics();

    final int xAxisStart = chart.getXChartStart();
    final int xAxisEnd = chart.getXChartEnd();
    final int yAxisStart = chart.getYChartStart();
    final int yAxisEnd = chart.getYChartEnd();
    final int rangeyPx = yAxisStart - yAxisEnd;
    final int xAxisLine = this.getPixelXRight();
    g2d.drawLine(xAxisLine, yAxisStart, xAxisLine, yAxisEnd);
    // drawing the y title :
    this.paintTitle(g2d);
    // drawing tick - scale, corresponding values, grid and conditional unit
    // label:
    if (this.isPaintScale() || this.isPaintGrid()) {
      final IAxisTickPainter tickPainter = chart.getAxisTickPainter();
      final int majorTickLength = tickPainter.getMajorTickLength();
      final List<LabeledValue> labels = this.m_axisScalePolicy.getScaleValues(g2d, this);
      for (final LabeledValue label : labels) {
        tmp = yAxisStart - (int) (label.getValue() * rangeyPx);

        if (this.isPaintScale()) {
          // true -> is left y axis:
          tickPainter.paintYTick(xAxisLine, tmp, label.isMajorTick(), true, g2d);
          tickPainter.paintYLabel(xAxisLine - majorTickLength
              - fontdim.stringWidth(label.getLabel()), tmp, label.getLabel(), g2d);
        }
        if (this.isPaintGrid()) {
          if (tmp != yAxisStart) {
            g2d.setColor(chart.getGridColor());
            g2d.drawLine(xAxisStart + 1, tmp, xAxisEnd, tmp);
            g2d.setColor(chart.getForeground());
          }
        }
      }
      // unit-labeling
      final String unitName = this.getFormatter().getUnit().getUnitName();
      g2d.drawString(unitName, 4, yAxisEnd);
    }

  }

  /**
   * Internally paints this axis in the case it is assigned as a y axis on the
   * right side of the corresponding chart.
   * <p>
   * 
   * @param g2d
   *          the graphics context to use.
   */
  private void paintAxisYRight(final Graphics g2d) {
    final Chart2D chart = this.getAccessor().getChart();
    int tmp = 0;
    final FontMetrics fontdim = g2d.getFontMetrics();

    final int xAxisStart = chart.getXChartStart();
    final int xAxisEnd = chart.getXChartEnd();
    final int yAxisStart = chart.getYChartStart();
    final int yAxisEnd = chart.getYChartEnd();
    final int rangeyPx = yAxisStart - yAxisEnd;
    final int xAxisLine = this.getPixelXLeft();
    g2d.drawLine(xAxisLine, yAxisStart, xAxisLine, yAxisEnd);
    // drawing the y title :
    this.paintTitle(g2d);
    // drawing tick - scale, corresponding values, grid and conditional unit
    // label:
    if (this.isPaintScale() || this.isPaintGrid()) {
      // then for y- angle.
      final IAxisTickPainter tickPainter = chart.getAxisTickPainter();
      final List<LabeledValue> labels = this.m_axisScalePolicy.getScaleValues(g2d, this);
      final int tickWidth = tickPainter.getMajorTickLength() + 4;
      for (final LabeledValue label : labels) {
        tmp = yAxisStart - (int) (label.getValue() * rangeyPx);
        if (this.isPaintScale()) {
          // false -> is right y axis:
          tickPainter.paintYTick(xAxisLine, tmp, label.isMajorTick(), false, g2d);
          tickPainter.paintYLabel(xAxisLine + tickWidth, tmp, label.getLabel(), g2d);
        }
        if (this.isPaintGrid()) {
          // do not paint over the axis:
          if (tmp != yAxisStart) {
            g2d.setColor(chart.getGridColor());
            g2d.drawLine(xAxisStart + 1, tmp, xAxisEnd, tmp);
            g2d.setColor(chart.getForeground());
          }
        }
      }
      // unit-labeling
      final String unitName = this.getFormatter().getUnit().getUnitName();
      g2d.drawString(unitName, (int) chart.getSize().getWidth()
          - fontdim.charsWidth(unitName.toCharArray(), 0, unitName.length()) - 4, yAxisEnd);

    }

  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#paintTitle(java.awt.Graphics)
   */
  public int paintTitle(final Graphics g2d) {
    int result = 0;
    // TODO: Add support for different axis locations: top, bottom, left,right!
    // drawing the title :
    final IAxis.AxisTitle axisTitle = this.getAxisTitle();
    final String title = axisTitle.getTitle();
    if (!StringUtil.isEmpty(title)) {
      IAxisTitlePainter titlePainter;
      titlePainter = axisTitle.getTitlePainter();
      titlePainter.paintTitle(this, g2d);

      final int dimension = this.getDimension();
      switch (dimension) {
        case Chart2D.X:
          result = titlePainter.getHeight(this, g2d);
          break;
        case Chart2D.Y:
          result = titlePainter.getWidth(this, g2d);
          break;
        default:
          throw new IllegalArgumentException(
              "Given axis.getDimension() is neither Chart2D.X nor Chart2D.Y!");

      }
    }
    return result;
  }

  /**
   * Receives all <code>{@link PropertyChangeEvent}</code> from all instances
   * the chart registers itself as a <code>{@link PropertyChangeListener}</code>
   * .
   * <p>
   * 
   * @param evt
   *          the property change event that was fired.
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    if (Chart2D.DEBUG_THREADING) {
      System.out
          .println("AAxis.propertyChange (" + Thread.currentThread().getName() + "), 0 locks");
    }
    final String property = evt.getPropertyName();
    final IPropertyChangeReactor reactor = AAxis.propertyReactors.get(property);
    if (reactor != null) {
      reactor.propertyChange(evt, this);
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#removeAllTraces()
   */
  public final Set<ITrace2D> removeAllTraces() {
    final Set<ITrace2D> result = new TreeSet<ITrace2D>();
    result.addAll(this.m_traces);
    /*
     * cannot work on this.m_traces as remove will cause concurrent modification
     * exception!
     */
    for (final ITrace2D trace : result) {
      this.removeTrace(trace);
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#removeAxisTitle()
   */
  public AxisTitle removeAxisTitle() {
    final AxisTitle result = this.m_axisTitle;
    this.unListenToAxisTitle(this.m_axisTitle);
    this.m_axisTitle = null;
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(property, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#removeTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public boolean removeTrace(final ITrace2D trace) {
    final boolean result = this.m_traces.remove(trace);
    if (result) {
      this.unlisten2Trace(trace);
      this.m_max = this.findMax();
      this.m_min = this.findMin();
      this.m_propertyChangeSupport.firePropertyChange(IAxis.PROPERTY_ADD_REMOVE_TRACE, trace, null);
    }
    return result;
  }

  /**
   * Internally rescales the given <code>{@link ITracePoint2D}</code> in the
   * dimension this axis works in.
   * <p>
   * 
   * @param point
   *          the point to scale (between 0.0 and 1.0) according to the internal
   *          bounds.
   */
  protected final void scalePoint(final ITracePoint2D point) {
    final int axis = this.getAccessor().getDimension();
    if (axis == Chart2D.X) {
      point.setScaledX(this.getScaledValue(point.getX()));

    } else if (axis == Chart2D.Y) {
      point.setScaledY(this.getScaledValue(point.getY()));

    }
    if (Chart2D.DEBUG_SCALING) {
      // This is ok for fixed viewports that zoom!
      if ((point.getScaledX() > 1.0) || (point.getScaledX() < 0.0) || (point.getScaledY() > 1.0)
          || (point.getScaledY() < 0.0)) {
        System.out.println("Scaled Point " + point + " to [" + point.getScaledX() + ","
            + point.getScaledY() + "]");
      }
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#scale()
   */
  public void scale() {
    final Iterator<ITrace2D> it = this.m_traces.iterator();
    ITrace2D trace;
    while (it.hasNext()) {
      trace = it.next();
      this.scaleTrace(trace);
    }
    this.m_rangePreviousScaling.mimic(this.getRange());
    this.m_needsFullRescale = false;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#scaleTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public void scaleTrace(final ITrace2D trace) {
    final Range range = this.getRange();
    this.m_accessor.scaleTrace(trace, range);
  }

  /**
   * Sets the accessor to the axis of the chart.
   * <p>
   * 
   * @param accessor
   *          the accessor to the axis of the chart.
   */
  protected final void setAccessor(final AChart2DDataAccessor accessor) {

    this.m_accessor = accessor;
  }

  /**
   * Sets the axisPosition.
   * <p>
   * 
   * @param axisPosition
   *          {@link Chart2D#CHART_POSITION_LEFT},
   *          {@link Chart2D#CHART_POSITION_RIGHT},
   *          {@link Chart2D#CHART_POSITION_TOP},
   *          {@link Chart2D#CHART_POSITION_BOTTOM} or -1 if this axis is not
   *          assigned to a chart.
   */
  protected final synchronized void setAxisPosition(final int axisPosition) {
    this.m_axisPosition = axisPosition;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#setAxisTitle(info.monitorenter.gui.chart.IAxis.AxisTitle)
   */
  public void setAxisTitle(final AxisTitle axisTitle) {

    this.unListenToAxisTitle(this.m_axisTitle);
    this.m_axisTitle = axisTitle;
    this.listenToAxisTitle(this.m_axisTitle);

  }

  /**
   * Callback that allows the chart to register itself with the axis when the axis is added to the chart.
   * <p>
   * <b>This is intended for <code>Chart2D</code> only!</b>.
   * Please do not use this from anywhere in your code. It allows to 
   * <p>
   * 
   * @param chart
   *          the chart to register itself with this axis.
   * @param dimension
   *          <code>{@link Chart2D#X}</code> or <code>{@link Chart2D#Y}</code>.
   * @param position
   *          <code>{@link Chart2D#CHART_POSITION_BOTTOM}</code>, <code>
   *          {@link Chart2D#CHART_POSITION_LEFT}</code>, <code>
   *          {@link Chart2D#CHART_POSITION_RIGHT}</code> or <code>
   *          {@link Chart2D#CHART_POSITION_TOP}</code>.
   */
  public void setChart(final Chart2D chart, final int dimension, final int position) {
    this.m_accessor = this.createAccessor(chart, dimension, position);
    this.m_needsFullRescale = true;
  }

  /**
   * Sets the formatter to use for labels.
   * <p>
   * 
   * @param formatter
   *          The formatter to set.
   */
  public void setFormatter(final IAxisLabelFormatter formatter) {

    if (this.getAccessor() != null) {
      // remove listener for this:
      this.removePropertyChangeListener(IAxis.PROPERTY_LABELFORMATTER, this.getAccessor()
          .getChart());
      // add listener for this:
      this.addPropertyChangeListener(IAxis.PROPERTY_LABELFORMATTER, this.getAccessor().getChart());
      // listener to subsequent format changes:
      if (this.m_formatter != null) {
        // remove listener on old formatter:
        this.m_formatter.removePropertyChangeListener(IAxisLabelFormatter.PROPERTY_FORMATCHANGE,
            this);
      }
      formatter.addPropertyChangeListener(IAxisLabelFormatter.PROPERTY_FORMATCHANGE, this);
    }
    final IAxisLabelFormatter old = this.m_formatter;

    this.m_formatter = formatter;

    this.m_formatter.setAxis(this);
    this.m_propertyChangeSupport.firePropertyChange(IAxis.PROPERTY_LABELFORMATTER, old,
        this.m_formatter);
  }

  /**
   * This method sets the major tick spacing for label generation.
   * <p>
   * Only values between 0.0 and 100.0 are allowed.
   * <p>
   * The number that is passed-in represents the distance, measured in values,
   * between each major tick mark. If you have a trace with a range from 0 to 50
   * and the major tick spacing is set to 10, you will get major ticks next to
   * the following values: 0, 10, 20, 30, 40, 50.
   * <p>
   * <b>Note: </b> <br>
   * Ticks are free of any multiples of 1000. If the chart contains values
   * between 0 an 1000 and configured a tick of 2 the values 0, 200, 400, 600,
   * 800 and 1000 will highly probable to be displayed. This depends on the size
   * (in pixels) of the <code>Chart2D<</code>. Of course there is a difference:
   * ticks are used in divisions and multiplications: If the internal values are
   * very low and the ticks are very high, huge rounding errors might occur
   * (division by ticks results in very low values a double cannot hit exactly.
   * So prefer setting ticks between 0 an 10 or - if you know your values are
   * very small (e.g. in nano range [10 <sup>-9 </sup>]) use a small value (e.g.
   * 2*10 <sup>-9 </sup>).
   * <p>
   * 
   * @param majorTickSpacing
   *          the major tick spacing for label generation.
   */
  public void setMajorTickSpacing(final double majorTickSpacing) {
    this.m_majorTickSpacing = majorTickSpacing;
  }

  /**
   * This method sets the minor tick spacing for label generation.
   * <p>
   * The number that is passed-in represents the distance, measured in values,
   * between each major tick mark. If you have a trace with a range from 0 to 50
   * and the major tick spacing is set to 10, you will get major ticks next to
   * the following values: 0, 10, 20, 30, 40, 50.
   * <p>
   * <b>Note: </b> <br>
   * Ticks are free of any powers of 10. There is no difference between setting
   * a tick to 2, 200 or 20000 because ticks cannot break the rule that every
   * scale label has to be visible. If the chart contains values between 0 an
   * 1000 and configured a tick of 2 the values 0, 200, 400, 600, 800 and 1000
   * will highly probable to be displayed. This depends on the size (in pixels)
   * of the <code>Chart2D<</code>. Of course there is a difference: ticks are
   * used in divisions and multiplications: If the internal values are very low
   * and the ticks are very high, huge rounding errors might occur (division by
   * ticks results in very low values a double cannot hit exactly. So prefer
   * setting ticks between 0 an 10 or - if you know your values are very small
   * (e.g. in nano range [10 <sup>-9 </sup>]) use a small value (e.g. 2*10
   * <sup>-9 </sup>).
   * <p>
   * 
   * @param minorTickSpacing
   *          the minor tick spacing to set.
   */
  public void setMinorTickSpacing(final double minorTickSpacing) {
    this.m_minorTickSpacing = minorTickSpacing;
  }

  /**
   * Set whether the grid in this dimension should be painted or not.
   * <p>
   * 
   * @param grid
   *          true if the grid should be painted or false if not.
   */
  public final void setPaintGrid(final boolean grid) {
    final boolean oldValue = this.m_paintGrid;
    this.m_paintGrid = grid;
    if (oldValue != grid) {
      this.m_propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this,
          IAxis.PROPERTY_PAINTGRID, new Boolean(oldValue), Boolean.valueOf(this.m_paintGrid)));
    }
  }

  /**
   * Set if the scale for this axis should be shown.
   * <p>
   * 
   * @param show
   *          true if the scale on this axis should be shown, false else.
   */
  public final void setPaintScale(final boolean show) {
    boolean oldValue = this.m_paintScale;
    this.m_paintScale = show;
    if (oldValue != this.m_paintScale) {
      this.m_propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this,
          IAxis.PROPERTY_PAINTSCALE, new Boolean(oldValue), Boolean.valueOf(this.m_paintGrid)));
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#setPixelXLeft(int)
   */
  public final void setPixelXLeft(final int pixelXLeft) {
    this.m_pixelXLeft = pixelXLeft;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#setPixelXRight(int)
   */
  public final void setPixelXRight(final int pixelXRight) {
    this.m_pixelXRight = pixelXRight;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#setPixelYBottom(int)
   */
  public final void setPixelYBottom(final int pixelYBottom) {
    this.m_pixelYBottom = pixelYBottom;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#setPixelYTop(int)
   */
  public final void setPixelYTop(final int pixelYTop) {
    this.m_pixelYTop = pixelYTop;
  }

  /**
   * <p>
   * Sets a Range to use for filtering the view to the the connected Axis. Note
   * that it's effect will be affected by the internal {@link IRangePolicy}.
   * </p>
   * <p>
   * To get full control use: <br>
   * <code> setRangePolicy(new &lt;AnARangePolicy&gt;(range);</code>
   * </p>
   * 
   * @param range
   *          Range to use for filtering the view to the the connected Axis.
   * @see #getRangePolicy()
   * @see IRangePolicy#setRange(Range)
   */
  public final void setRange(final Range range) {

    this.getRangePolicy().setRange(range);
  }
  
  /**
   * Ensures that no deadlock / NPE due to a missing internal chart reference may
   * occur.
   * <p>
   * 
   * @throws IllegalStateException
   *           if this axis is not assigned to a chart.
   * 
   */
  protected final void ensureInitialized() {
    if (this.m_accessor == null) {
      throw new IllegalStateException("Add this axis to a chart first before this operation (undebuggable deadlocks might occur else)");
    } 
  }

  /**
   * <p>
   * Sets the RangePolicy.
   * </p>
   * <p>
   * If the given RangePolicy has an unconfigured internal Range (
   * {@link Range#RANGE_UNBOUNDED}) the old internal RangePolicy is taken into
   * account: <br>
   * If the old RangePolicy has a configured Range this is transferred to the
   * new RangePolicy.
   * </p>
   * A property change event for {@link IAxis#PROPERTY_RANGEPOLICY} is fired and
   * receives listeners if a change took place.
   * <p>
   * 
   * @param rangePolicy
   *          The rangePolicy to set.
   */
  public void setRangePolicy(final IRangePolicy rangePolicy) {

    this.ensureInitialized();
    final IRangePolicy old = this.getRangePolicy();

    double max = 0;
    double min = 0;
    if (old != null) {
      max = AAxis.this.getMax();
      min = AAxis.this.getMin();

      old.removePropertyChangeListener(this.m_accessor.m_chart, IRangePolicy.PROPERTY_RANGE);
      old.removePropertyChangeListener(this.m_accessor.m_chart, IRangePolicy.PROPERTY_RANGE_MAX);
      old.removePropertyChangeListener(this.m_accessor.m_chart, IRangePolicy.PROPERTY_RANGE_MIN);
    }

    this.m_rangePolicy = rangePolicy;
    this.m_rangePolicy.addPropertyChangeListener(IRangePolicy.PROPERTY_RANGE,
        this.m_accessor.m_chart);
    this.m_rangePolicy.addPropertyChangeListener(IRangePolicy.PROPERTY_RANGE_MAX,
        this.m_accessor.m_chart);
    this.m_rangePolicy.addPropertyChangeListener(IRangePolicy.PROPERTY_RANGE_MIN,
        this.m_accessor.m_chart);

    // check for scaling changes:
    if (((max != 0) && (min != 0))
        && ((max != AAxis.this.getMax()) || (min != AAxis.this.getMin()))) {
      this.m_accessor.m_chart.propertyChange(new PropertyChangeEvent(rangePolicy,
          IRangePolicy.PROPERTY_RANGE, new Range(min, max), this.m_rangePolicy.getRange()));
    }

    this.m_propertyChangeSupport.firePropertyChange(IAxis.PROPERTY_RANGEPOLICY, old, rangePolicy);
  }

  /**
   * Set wether scale values are started from major ticks.
   * <p>
   * 
   * @param majorTick
   *          true if scale values shall start with a major tick.
   * @see AAxis#setMajorTickSpacing(double)
   */
  public void setStartMajorTick(final boolean majorTick) {
    this.m_startMajorTick = majorTick;
  }

  /**
   * @deprecated use {@link #getAxisTitle()} and on the result
   *             {@link IAxis.AxisTitle#setTitle(String)}
   */
  @Deprecated
  public final String setTitle(final String title) {
    final String result = this.getAxisTitle().setTitle(title);
    return result;
  }

  /**
   * Sets the title painter of this axis which is by default <code>
   * {@link info.monitorenter.gui.chart.axistitlepainters.AxisTitlePainterDefault}
   * </code>.
   * <p>
   * 
   * @deprecated use {@link #getAxisTitle()} and on the result
   *             {@link IAxis.AxisTitle#setTitlePainter(IAxisTitlePainter)}
   *             instead.
   */
  @Deprecated
  public final IAxisTitlePainter setTitlePainter(final IAxisTitlePainter painter) {
    final IAxisTitlePainter result = this.getAxisTitle().setTitlePainter(painter);
    return result;
  }

  /**
   * Set the visibility of this axis.
   * <p>
   * 
   * @param visible
   *          true to show, false to hide
   */
  public void setVisible(final boolean visible) {
    this.m_visible = visible;
  }

  /**
   * Returns the translation of the mouse event coordinates of the given mouse
   * event to the value within the chart for the dimension (x,y) covered by this
   * axis.
   * <p>
   * Note that the mouse event has to be an event fired on the correspondinig
   * chart component!
   * <p>
   * 
   * @param mouseEvent
   *          a mouse event that has been fired on this component.
   * 
   * @return the translation of the mouse event coordinates of the given mouse
   *         event to the value within the chart for the dimension covered by
   *         this axis (x or y) or null if no calculations could be performed as
   *         the chart was not painted before.
   * 
   * @throws IllegalArgumentException
   *           if the given mouse event is out of the current graphics context
   *           (not a mouse event of the chart component).
   */
  public double translateMousePosition(final MouseEvent mouseEvent) throws IllegalArgumentException {
    return this.getAccessor().translateMousePosition(mouseEvent);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#translatePxToValue(int)
   */
  public double translatePxToValue(final int pixel) {
    return this.m_accessor.translatePxToValue(pixel);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxis#translateValueToPx(double)
   */
  public final int translateValueToPx(final double value) {
    return this.m_accessor.translateValueToPx(value);
  }

  /**
   * Removes this axis as a listener for all property change events of the given
   * trace that are needed here.
   * <p>
   * TODO: stick to <code>{@link AAxis#listen2Trace(ITrace2D)}</code>.
   * <p>
   * 
   * @param trace
   *          the trace to not listen to any more.
   */
  private void unlisten2Trace(final ITrace2D trace) {
    if (this.getAccessor().getDimension() == Chart2D.X) {
      trace.removePropertyChangeListener(ITrace2D.PROPERTY_MAX_X, this);
      trace.removePropertyChangeListener(ITrace2D.PROPERTY_MIN_X, this);
    } else {
      trace.removePropertyChangeListener(ITrace2D.PROPERTY_MAX_Y, this);
      trace.removePropertyChangeListener(ITrace2D.PROPERTY_MIN_Y, this);
    }
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_COLOR, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_STROKE, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_VISIBLE, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_ZINDEX, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_PAINTERS, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_ERRORBARPOLICY, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_ERRORBARPOLICY_CONFIGURATION, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_ZINDEX, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_NAME, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_TRACEPOINT, this);
    trace.removePropertyChangeListener(ITrace2D.PROPERTY_POINT_CHANGED, this);
  }

  /**
   * Internal routine for an axis to not listen to the new given axis title any
   * more.
   * <p>
   * Factored out to have a better overview of event handling (vs. putting this
   * code in setters).
   * <p>
   * 
   * @param axisTitle
   *          the axis title not to listen to any more.
   */
  private void unListenToAxisTitle(final AxisTitle axisTitle) {
    // This is the case when the axis is created: m_axisTitle is null then.
    if (axisTitle != null) {
      axisTitle.removePropertyChangeListener(AxisTitle.PROPERTY_TITLE, this);
      axisTitle.removePropertyChangeListener(AxisTitle.PROPERTY_TITLEFONT, this);
      axisTitle.removePropertyChangeListener(AxisTitle.PROPERTY_TITLEPAINTER, this);
    }
  }

}
