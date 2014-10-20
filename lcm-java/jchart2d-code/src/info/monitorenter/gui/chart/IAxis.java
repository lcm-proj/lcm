/*
 *  IAxis.java of project jchart2d, interface for an axis of the 
 *  Chart2D.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart;

import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axistitlepainters.AxisTitlePainterDefault;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Set;

/**
 * Interface for an axis of the {@link info.monitorenter.gui.chart.Chart2D}.
 * <p>
 * 
 * @param <T>
 *          Subtypes may be more picky which scale policies the accept to
 *          disallow incorrect scales: This supports it (see
 *          {@link IAxis#setAxisScalePolicy(IAxisScalePolicy)}).
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.39 $
 */
public interface IAxis<T extends IAxisScalePolicy> extends Serializable {

  /**
   * Represents a title of an axis.
   * <p>
   * 
   * @author Achim Westermann
   * @version $Revision: 1.39 $
   * @since 3.0.0
   */
  public final class AxisTitle implements Cloneable, Serializable {

    /**
     * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
     * title font.
     */
    public static final String PROPERTY_TITLE = "IAxis.AxisTitle.PROPERTY_TITLE";

    /**
     * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
     * title font.
     */
    public static final String PROPERTY_TITLECOLOR = "IAxis.AxisTitle.PROPERTY_TITLECOLOR";

    /**
     * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
     * title font.
     */
    public static final String PROPERTY_TITLEFONT = "IAxis.AxisTitle.PROPERTY_TITLEFONT";

    /**
     * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
     * title font.
     */
    public static final String PROPERTY_TITLEPAINTER = "IAxis.AxisTitle.PROPERTY_TITLEPAINTER";

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -7734801964168791096L;

    /** Internal support for property change management. */
    private PropertyChangeSupport m_propertyChangeSupport = new PropertyChangeSupport(this);

    /** The title. */
    private String m_title;

    /** The title color to use, defaults to <code>{@link Color#BLACK}</code>. */
    private Color m_titleColor = Color.BLACK;

    /** The optional title font to use - null by default. */
    private Font m_titleFont;

    /** The painter of this axis title. */
    private IAxisTitlePainter m_titlePainter;

    /**
     * Creates an instance with the given title backed by a
     * <code>{@link AxisTitlePainterDefault}</code>.
     * <p>
     * 
     * @param title
     *          the title to use.
     */
    public AxisTitle(final String title) {
      this(title, new AxisTitlePainterDefault());
    }

    /**
     * Creates an instance with the given title backed by the given painter.
     * <p>
     * 
     * @param title
     *          the title to use.
     * @param painter
     *          the painter to use.
     */
    public AxisTitle(final String title, final IAxisTitlePainter painter) {
      this.m_title = title;
      this.m_titlePainter = painter;
    }

    /**
     * Add a listener for the given property.
     * <p>
     * The following <code>{@link java.beans.PropertyChangeEvent}</code> types
     * should be fired to listeners:<br/>
     * <table border="0">
     * <tr>
     * <th><code>getPropertyName()</code></th>
     * <th><code>getSource()</code></th>
     * <th><code>getOldValue()</code></th>
     * <th><code>getNewValue()</code></th>
     * </tr>
     * <tr>
     * <td><code>{@link IAxis.AxisTitle#PROPERTY_TITLE}</code></td>
     * <td><code>{@link IAxis.AxisTitle}</code> that changed</td>
     * <td><code>{@link String}</code>, the old value.</td>
     * <td><code>{@link String}</code>, the new value.</td>
     * </tr>
     * <tr>
     * <td><code>{@link IAxis.AxisTitle#PROPERTY_TITLEFONT}</code></td>
     * <td><code>{@link IAxis.AxisTitle}</code> that changed</td>
     * <td><code>{@link Font}</code>, the old value.</td>
     * <td><code>{@link Font}</code>, the new value.</td>
     * </tr>
     * <tr>
     * <td><code>{@link IAxis.AxisTitle#PROPERTY_TITLEPAINTER}</code></td>
     * <td><code>{@link IAxis.AxisTitle}</code> that changed</td>
     * <td><code>{@link IAxisTitlePainter}</code>, the old value.</td>
     * <td><code>{@link IAxisTitlePainter}</code>, the new value.</td>
     * </tr>
     * <tr>
     * <td><code>{@link IAxis.AxisTitle#PROPERTY_TITLECOLOR}</code></td>
     * <td><code>{@link IAxis.AxisTitle}</code> that changed</td>
     * <td><code>{@link Color}</code>, the old value.</td>
     * <td><code>{@link Color}</code>, the new value.</td>
     * </tr>
     * </table>
     * <p>
     * 
     * 
     * 
     * @param propertyName
     *          the property to be informed about changes.
     * @param listener
     *          the listener that will be informed.
     */
    public void addPropertyChangeListener(final String propertyName,
        final PropertyChangeListener listener) {
      this.m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
      Object result = super.clone();
      return result;
    }

    /**
     * Returns the height of this axis title in px with respect to the current
     * title of the given axis.
     * <p>
     * 
     * @param axis
     *          the instance this title painter is working for.
     * @param g2d
     *          needed for size informations (e.g. font widths).
     * @return the height of this axis title in px with respect to the current
     *         title of the given axis.
     */
    public int getHeight(final IAxis<?> axis, final Graphics2D g2d) {
      return this.m_titlePainter.getHeight(axis, g2d);
    }

    /**
     * Returns an array of all the listeners that were added to the this
     * instance with
     * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
     * <p>
     * 
     * @return an array of all the listeners that were added to the this
     *         instance with
     *         {@link #addPropertyChangeListener(String, PropertyChangeListener)}
     *         .
     * @param propertyName
     *          The name of the property being listened to.
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(final String propertyName) {
      return this.m_propertyChangeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * Returns the title or <code>null</code> if there was no title configured
     * before.
     * <p>
     * 
     * @return the title or <code>null</code> if there was no title configured
     *         before.
     */
    public final String getTitle() {
      return this.m_title;
    }

    /**
     * Returns the color used for painting the title.
     * <p>
     * Default is <code>{@link Color#BLACK}</code>.
     * <p>
     * 
     * @return the color used for painting the title.
     */
    public Color getTitleColor() {
      return this.m_titleColor;
    }

    /**
     * Returns the optional font used for painting the title or null if not
     * configured.
     * <p>
     * 
     * @return the font used for painting the title or null if not configured.
     */
    public Font getTitleFont() {
      return this.m_titleFont;
    }

    /**
     * Returns the titlePainter.
     * <p>
     * 
     * @return the titlePainter
     */
    public final IAxisTitlePainter getTitlePainter() {
      return this.m_titlePainter;
    }

    /**
     * Returns the width of this axis title in px with respect to the current
     * title of the given axis.
     * <p>
     * 
     * @param axis
     *          the instance this title painter is working for.
     * @param g2d
     *          needed for size informations (e.g. font widths).
     * @return the width of this axis title in px with respect to the current
     *         title of the given axis.
     */
    public int getWidth(final IAxis<?> axis, final Graphics2D g2d) {
      return this.m_titlePainter.getWidth(axis, g2d);
    }

    /**
     * Remove a PropertyChangeListener for a specific property. If
     * <code>listener</code> was added more than once to the same event source
     * for the specified property, it will be notified one less time after being
     * removed. If <code>propertyName</code> is null, no exception is thrown and
     * no action is taken. If <code>listener</code> is null, or was never added
     * for the specified property, no exception is thrown and no action is
     * taken.
     * 
     * @param property
     *          The name of the property that was listened on.
     * @param listener
     *          The PropertyChangeListener to be removed.
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(final String property,
        final PropertyChangeListener listener) {
      this.m_propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    /**
     * Sets the title or <code>null</code> if there should be no title.
     * <p>
     * 
     * @param title
     *          the title or <code>null</code> if no title should be used.
     * @return the previous title or null if there was none before.
     */
    public final String setTitle(final String title) {
      String old = this.m_title;
      this.m_title = title;
      this.m_propertyChangeSupport.firePropertyChange(AxisTitle.PROPERTY_TITLE, old,
          this.m_title);
      return old;
    }

    /**
     * Sets the title color to use.
     * <p>
     * Default is <code>{@link Color#BLACK}</code>.
     * <p>
     * 
     * @param color
     *          the color to use for the title.
     */
    public void setTitleColor(final Color color) {
      Color old = this.m_titleColor;
      this.m_titleColor = color;
      this.m_propertyChangeSupport.firePropertyChange(AxisTitle.PROPERTY_TITLECOLOR, old,
          this.m_titleColor);
    }

    /**
     * Sets the optional title font to use.
     * <p>
     * 
     * @param font
     *          the font to use for the title.
     */
    public void setTitleFont(final Font font) {
      Font old = this.m_titleFont;
      this.m_titleFont = font;
      this.m_propertyChangeSupport.firePropertyChange(AxisTitle.PROPERTY_TITLEFONT, old,
          this.m_titleFont);
    }

    /**
     * Sets the titlePainter.
     * <p>
     * 
     * @param titlePainter
     *          the titlePainter to set.
     * @return the previous title painter or null if there was none before.
     */
    public final IAxisTitlePainter setTitlePainter(final IAxisTitlePainter titlePainter) {
      IAxisTitlePainter old = this.m_titlePainter;
      this.m_titlePainter = titlePainter;
      this.m_propertyChangeSupport.firePropertyChange(AxisTitle.PROPERTY_TITLEPAINTER, old,
          this.m_titlePainter);
      return old;
    }

  }

  /**
   * The bean property <code>constant</code> identifying a change of the
   * internal set of <code>{@link ITrace2D}</code> instances.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>IAxis</code>.
   * <p>
   * See {@link #addPropertyChangeListener(String, PropertyChangeListener)}  for property change events fired.
   */
  public static final String PROPERTY_ADD_REMOVE_TRACE = "IAxis.PROPERTY_ADD_REMOVE_TRACE";

  /**
   * The bean property <code>constant</code> identifying a change of the
   * axis scale policy. 
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>IAxis</code>.
   * <p>
   * See {@link #addPropertyChangeListener(String, PropertyChangeListener)} for property change events fired.
   */
  public static final String PROPERTY_AXIS_SCALE_POLICY_CHANGED = "IAxis.PROPERTY_AXIS_SCALE_POLICY_CHANGED";
  
  /**
   * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
   * <code>{@link IAxisTitlePainter}</code>.
   */
  public static final String PROPERTY_LABELFORMATTER = "IAxis.PROPERTY_LABELFORMATTER";

  /**
   * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
   * paint grid flag.
   */
  public static final String PROPERTY_PAINTGRID = "IAxis.PROPERTY_PAINTGRID";

  /**
   * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
   * paint scale flag.
   */
  public static final String PROPERTY_PAINTSCALE = "IAxis.PROPERTY_PAINTSCALE";

  /**
   * Constant for a <code>{@link java.beans.PropertyChangeEvent}</code> of the
   * range policy.
   */
  public static final String PROPERTY_RANGEPOLICY = "IAxis.PROPERTY_RANGEPOLICY";

  /**
   * Add a listener for the given property.
   * <p>
   * The following {@link java.beans.PropertyChangeEvent} types should be fired
   * to listeners:<br>
   * <table border="0">
   * <tr>
   * <th><code>getPropertyName()</code></th>
   * <th><code>getSource()</code></th>
   * <th><code>getOldValue()</code></th>
   * <th><code>getNewValue()</code></th>
   * </tr>
   * <tr>
   * <td>
   * <code>{@link info.monitorenter.gui.chart.IAxis#PROPERTY_ADD_REMOVE_TRACE}</code>
   * </td>
   * <td><code>{@link IAxis}</code> that changed</td>
   * <td><code>null</code> - a new trace was added.</td>
   * <td><code>{@link ITrace2D}</code>, the new trace.</td>
   * </tr>
   * <tr>
   * <td>
   * <code>{@link info.monitorenter.gui.chart.IAxis#PROPERTY_ADD_REMOVE_TRACE}</code>
   * </td>
   * <td><code>{@link IAxis}</code> that changed</td>
   * <td><code>{@link ITrace2D}</code>, the old trace.</td>
   * <td><code>null</code> - the trace was removed.</td>
   * </tr>
   * <tr>
   * <td>
   * <code>{@link info.monitorenter.gui.chart.IAxis#PROPERTY_RANGEPOLICY}</code>
   * </td>
   * <td><code>{@link IAxis}</code> that changed</td>
   * <td><code>{@link IRangePolicy}</code>, the old value.</td>
   * <td><code>{@link IRangePolicy}</code>, the new value.</td>
   * </tr>
   * <tr>
   * <td>
   * <code>{@link info.monitorenter.gui.chart.IAxis#PROPERTY_PAINTGRID}</code></td>
   * <td><code>{@link IAxis}</code> that changed</td>
   * <td><code>{@link Boolean}</code>, the old value.</td>
   * <td><code>{@link Boolean}</code>, the new value.</td>
   * </tr>
   * <tr>
   * <td>
   * <code>{@link info.monitorenter.gui.chart.IAxis#PROPERTY_LABELFORMATTER}</code>
   * </td>
   * <td><code>{@link IAxis}</code> that changed</td>
   * <td><code>{@link IAxisLabelFormatter}</code>, the old value or null if
   * there was no formatter before.</td>
   * <td><code>{@link IAxisLabelFormatter}</code>, the new value.</td>
   * </tr>
   * <tr>
   * <td><code>{@link IAxis#PROPERTY_AXIS_SCALE_POLICY_CHANGED}</code></td>
   * <td><code>{@link IAxis}</code> that changed</td>
   * <td><code>{@link IAxisScalePolicy}</code>, the old value.</td>
   * <td><code>{@link IAxisScalePolicy}</code>, the new value.</td>
   * </tr>
   * </table>
   * <p>
   * 
   * @param propertyName
   *          the property to be informed about changes.
   * @param listener
   *          the listener that will be informed.
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Adds a trace that belongs to this axis.
   * <p>
   * 
   * Adding a trace that is already contained may be problematic, so an
   * exception should be raised in that case to warn you that your code is doing
   * unnecessary to malicious operations.
   * <p>
   * 
   * @param trace
   *          the trace to add.
   * @return true if the trace was added, false else.
   */
  public boolean addTrace(ITrace2D trace);

  /**
   * Returns the accessor to the chart.
   * <p>
   * 
   * @return the accessor to the chart.
   */
  public abstract AAxis<?>.AChart2DDataAccessor getAccessor();

  /**
   * Returns the constant for the position of this axis for the chart.
   * <p>
   * 
   * @return {@link Chart2D#CHART_POSITION_LEFT},
   *         {@link Chart2D#CHART_POSITION_RIGHT},
   *         {@link Chart2D#CHART_POSITION_TOP},
   *         {@link Chart2D#CHART_POSITION_BOTTOM} or -1 if this axis is not
   *         assigned to a chart.
   */
  public int getAxisPosition();

  /**
   * Returns the title of this axis.
   * <p>
   * 
   * @return the axis title used.
   */
  public IAxis.AxisTitle getAxisTitle();

  /**
   * Removes the title of this axis.
   * <p>
   * 
   * Prefer this method instead of <code>{@link #getAxisTitle()}</code> if you
   * want to drop the axis title as this method also "unlistens" this axis from
   * it's title.
   * <p>
   * 
   * @return the removed title.
   */
  public IAxis.AxisTitle removeAxisTitle();

  /**
   * Returns the constant for the dimension this axis stands for in the chart.
   * <p>
   * 
   * @return {@link Chart2D#X}, {@link Chart2D#Y} or -1 if this axis is not
   *         assigned to a chart.
   */
  public int getDimension();

  /**
   * Returns the String constant for the dimension this axis stands for in the chart.
   * <p>
   * 
   * @return "X", "Y" or <code>null</code> if not assigned to a <code>{@link Chart2D}</code>. 
   **/ 
  public String getDimensionString();

  /**
   * Returns the formatter for labels.
   * <p>
   * 
   * @return the formatter for labels.
   */
  public abstract IAxisLabelFormatter getFormatter();

  /**
   * Returns the height in pixel this axis needs to paint itself.
   * <p>
   * This includes the axis line, it's ticks and labels and it's title.
   * <p>
   * <b>Note:</b></br/> For an y axis the hight only includes the overhang it
   * needs on the upper edge for painting a complete lable, not the complete
   * space it needs for the complete line.
   * <p>
   * 
   * @param g2d
   *          needed for font metric information.
   * @return the height in pixel this axis needs to paint itself.
   */
  public int getHeight(Graphics g2d);

  /**
   * Get the major tick spacing for label generation.
   * <p>
   * 
   * @return the major tick spacing for label generation.
   * @see #setMajorTickSpacing(double)
   */

  public abstract double getMajorTickSpacing();

  /**
   * Returns the maximum value from all traces of this axis with respect to the
   * installed range policy.
   * <p>
   * 
   * @return the maximum value from all traces of this axis with respect to the
   *         installed range policy.
   */
  public double getMax();

  /**
   * Returns the maximum value of all
   * <code>{@link info.monitorenter.gui.chart.TracePoint2D}</code> instances in
   * all <code>{@link ITrace2D}</code> instances in this axis regardless of the
   * configured <code>{@link IRangePolicy}</code> (see
   * <code>{@link IAxis#setRangePolicy(IRangePolicy)}</code>). The returned
   * value is either in x or y dimension - depending on the dimension this axis
   * is working in for the chart.
   * <p>
   * 
   * @return the maximum value of all <code>
   *         {@link info.monitorenter.gui.chart.TracePoint2D}</code> instances
   *         in all <code>{@link ITrace2D}</code> instances in this axis
   *         regardless of the configured <code>{@link IRangePolicy}</code> (see
   *         <code>{@link IAxis#setRangePolicy(IRangePolicy)}</code>).
   */
  public double getMaxValue();

  /**
   * Returns the minimum value of all traces of this axis with respect to the
   * installed range policy.
   * <p>
   * 
   * @return the minimum value of all traces of this axis with respect to the
   *         installed range policy.
   */
  public double getMin();

  /**
   * Get the minor tick spacing for label generation.
   * <p>
   * 
   * @return the minor tick spacing for label generation.
   * @see #setMinorTickSpacing(double)
   */
  public abstract double getMinorTickSpacing();

  /**
   * Returns the minimum value of all
   * <code>{@link info.monitorenter.gui.chart.TracePoint2D}</code> instances in
   * all <code>{@link ITrace2D}</code> instances in this axis regardless of the
   * configured <code>{@link IRangePolicy}</code> (see
   * <code>{@link IAxis#setRangePolicy(IRangePolicy)}</code>). The returned
   * value is either in x or y dimension - depending on the dimension this axis
   * is working in for the chart.
   * <p>
   * 
   * @return the minimum value of all <code>
   *         {@link info.monitorenter.gui.chart.TracePoint2D}</code> instances
   *         in all <code>{@link ITrace2D}</code> instances in this axis
   *         regardless of the configured <code>{@link IRangePolicy}</code> (see
   *         <code>{@link IAxis#setRangePolicy(IRangePolicy)}</code>).
   */
  public double getMinValue();

  /**
   * Returns the left pixel of this axis coordinate in the graphic context of
   * the current paint operation.
   * <p>
   * Note that this value is only valid throughout a
   * <code>{@link Chart2D#paint(java.awt.Graphics)}</code> invocation.
   * 
   * @return the left pixel coordinate of this axis in the graphic context of
   *         the current paint operation.
   */
  public int getPixelXLeft();

  /**
   * Returns the right pixel coordinate of this axis in the graphic context of
   * the current paint operation.
   * <p>
   * Note that this value is only valid throughout a
   * <code>{@link Chart2D#paint(java.awt.Graphics)}</code> invocation.
   * 
   * @return the right pixel coordinate of this axis in the graphic context of
   *         the current paint operation.
   */
  public int getPixelXRight();

  /**
   * Returns the bottom pixel coordinate of this axis in the graphic context of
   * the current paint operation.
   * <p>
   * Note that this value is only valid throughout a
   * <code>{@link Chart2D#paint(java.awt.Graphics)}</code> invocation.
   * 
   * @return the bottom pixel coordinate of this axis in the graphic context of
   *         the current paint operation.
   */
  public int getPixelYBottom();

  /**
   * Returns the top pixel coordinate of this axis in the graphic context of the
   * current paint operation.
   * <p>
   * Note that this value is only valid throughout a
   * <code>{@link Chart2D#paint(java.awt.Graphics)}</code> invocation.
   * 
   * @return the top pixel coordinate of this axis in the graphic context of the
   *         current paint operation.
   */
  public int getPixelYTop();

  /**
   * Returns an array of all the listeners that were added to the this instance
   * with {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   * 
   * @return an array of all the listeners that were added to the this instance
   *         with
   *         {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * @param propertyName
   *          The name of the property being listened to.
   * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
   */
  public PropertyChangeListener[] getPropertyChangeListeners(String propertyName);

  /**
   * Returns the range lasting from <code>{@link IAxis#getMin()}</code> to
   * <code>{@link IAxis#getMax()}</code>.
   * <p>
   * This method is used by the Chart2D to scale it's values during painting.
   * <p>
   * Caution: This method does not necessarily return the Range configured with
   * {@link #setRange(Range)}. The internal {@link IRangePolicy} is taken into
   * account.
   * <p>
   * 
   * @return the range corresponding to the upper and lower bound of the values
   *         that will be displayable on this Axis of the Chart2D.
   * @see #setRangePolicy(IRangePolicy)
   */
  public abstract Range getRange();

  /**
   * Returns the range policy of this axis.
   * <p>
   * 
   * @return the range policy of this axis.
   */
  public IRangePolicy getRangePolicy();

  /**
   * Scales the given absolute value into a value between 0 and 1.0 (if it is in
   * the range of the data).
   * <p>
   * If the given absolute value is not in the display- range of the
   * <code>Chart2D</code>, negative values or values greater than 1.0 may
   * result.
   * <p>
   * 
   * @param absolute
   *          a value in the real value range of the corresponding chart.
   * @return a value between 0.0 and 1.0 that is mapped to a position within the
   *         chart.
   */
  public abstract double getScaledValue(final double absolute);

  /**
   * Returns the axis scale policy which controls the position and distance of the ticks to draw. 
   * <p>
   * 
   * @return the axis scale policy which controls the position and distance of the ticks to draw. 
   */
  public IAxisScalePolicy getAxisScalePolicy();
  
  /**
   * Sets the axis scale policy which controls the position and distance of the
   * ticks to draw.
   * <p>
   * 
   * @param axisScalePolicy
   *          the axis scale policy which controls the position and distance of
   *          the ticks to draw to use.
   * 
   * @return the previous axis scale policy that was used before.
   */
  public IAxisScalePolicy setAxisScalePolicy(T axisScalePolicy);
  
  /**
   * Returns the title or <code>null</code> if there was no title configured
   * before.
   * <p>
   * 
   * @return the title or <code>null</code> if there was no title configured
   *         before.
   * @see #getTitlePainter()
   * @deprecated use {@link #getAxisTitle()} and on the result
   *             {@link IAxis.AxisTitle#getTitle()}.
   */
  @Deprecated
  public String getTitle();

  /**
   * Returns the instance that will paint the title of this axis.
   * <p>
   * 
   * @deprecated this method might be dropped because the painter should be of
   *             no concern.
   * @return the instance that will paint the title of this axis.
   */
  @Deprecated
  public IAxisTitlePainter getTitlePainter();

  /**
   * Returns a <code>{@link Set}&lt;{@link ITrace2D}&gt;</code> with all traces covered by
   * this axis.
   * <p>
   * <b>Caution!</b><br/>
   * The original internal modifiable set is returned for performance reasons
   * and by contract (to allow removing traces) so do not mess with it to avoid
   * ugly unpredictable side effects!
   * <p>
   * 
   * @return a <code>{@link Set}&lt;{@link ITrace2D}&gt;</code> with all traces
   *         covered by this axis.
   */
  public Set<ITrace2D> getTraces();

  /**
   * Returns the width in pixel this axis needs to paint itself.
   * <p>
   * This includes the axis line, it's ticks and labels and it's title.
   * <p>
   * <b>Note:</b></br/> For an x axis the width only includes the overhang it
   * needs on the right edge for painting a complete label, not the complete
   * space it needs for the complete line.
   * <p>
   * 
   * @param g2d
   *          needed for font metric information.
   * @return the width in pixel this axis needs to paint itself.
   */
  public int getWidth(Graphics g2d);

  /**
   * Returns true if this axis is responsible for rendering the scale of the
   * given trace (<code>{@link IAxis#addTrace(ITrace2D)}</code> was called on
   * this instance with the given trace).
   * <p>
   * 
   * @param trace
   *          the trace to check for containment.
   * @return true if this axis is responsible for rendering the scale of the
   *         given trace (<code>{@link IAxis#addTrace(ITrace2D)}</code> was
   *         called on this instance with the given trace).
   */
  public boolean hasTrace(ITrace2D trace);

  /**
   * Allows to perform expensive calculations for various values that are used
   * by many calls throughout a paint iterations.
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
  public void initPaintIteration();

  /**
   * Returns true if the bounds in the given dimension of all
   * {@link info.monitorenter.gui.chart.TracePoint2D} instances of all internal
   * {@link ITrace2D} instances have changed since all points have been
   * normalized to a value between 0 and 1 or true if this axis has different
   * range since the last call to <code>{@link IAxis#scale()}</code>.
   * <p>
   * 
   * @return true if the bounds in the given dimension of all
   *         {@link info.monitorenter.gui.chart.TracePoint2D} instances of all
   *         internal {@link ITrace2D} instances have changed since all points
   *         have been normalized to a value between 0 and 1 or true if this
   *         axis has different range since the last call to <code>
   *         {@link IAxis#scale()}</code>.
   */
  public boolean isDirtyScaling();

  /**
   * Returns wether the x grid is painted or not.
   * <p>
   * 
   * @return wether the x grid is painted or not.
   */
  public abstract boolean isPaintGrid();

  /**
   * Returns whether the scale for this axis should be painted or not.
   * <p>
   * 
   * @return whether the scale for this axis should be painted or not.
   */
  public abstract boolean isPaintScale();

  /**
   * Check wether scale values are started from major ticks.
   * <p>
   * 
   * @return true if scale values start from major ticks.
   * @see info.monitorenter.gui.chart.axis.AAxis#setMajorTickSpacing(double)
   */
  public abstract boolean isStartMajorTick();

  /**
   * Check whether this axis is visible, i.e. needs to be painted on the chart
   * 
   * @return the visibility state of this axis
   */
  public boolean isVisible();

  /**
   * Renders the axis line along with title, scale, scale labels and unit label.
   * <p>
   * This should only be called from <code>{@link Chart2D}</code>, all other
   * uses may cause damaged UI or deadlocks.
   * <p>
   * 
   * @param g2d
   *          the graphics context to use.
   */
  public void paint(final Graphics g2d);

  /**
   * Routine for painting the title of this axis.
   * <p>
   * <b>Intended for <code>{@link Chart2D}</code> only!!!</b>
   * 
   * @param g2d
   *          needed for painting.
   * @return the width consumed in pixel for y axis, the height consumed in
   *         pixel for x axis.
   */
  public int paintTitle(final Graphics g2d);

  /**
   * Convenience method for removing all contained <code>{@link ITrace2D}</code>
   * instances of this axis.
   * <p>
   * Implementations should fire a
   * <code>{@link java.beans.PropertyChangeEvent}</code> for the
   * <code>{@link java.beans.PropertyChangeEvent#getPropertyName()}</code>
   * <code>{@link IAxis#PROPERTY_ADD_REMOVE_TRACE}</code> for every single trace
   * removed. This is done best by delegating this call to several calls to
   * <code>{@link #removeTrace(ITrace2D)}</code>.
   * <p>
   * 
   * @return a shallow copy of the set of traces that were contained before.
   */
  public Set<ITrace2D> removeAllTraces();

  /**
   * Remove a PropertyChangeListener for a specific property. If
   * <code>listener</code> was added more than once to the same event source for
   * the specified property, it will be notified one less time after being
   * removed. If <code>propertyName</code> is null, no exception is thrown and
   * no action is taken. If <code>listener</code> is null, or was never added
   * for the specified property, no exception is thrown and no action is taken.
   * 
   * @param property
   *          The name of the property that was listened on.
   * @param listener
   *          The PropertyChangeListener to be removed.
   * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener);

  /**
   * Removes the given trace from this axis.
   * <p>
   * A <code>{@link java.beans.PropertyChangeEvent}</code> for the
   * <code>{@link java.beans.PropertyChangeEvent#getPropertyName()}</code>
   * <code>{@link IAxis#PROPERTY_ADD_REMOVE_TRACE}</code> has to be fired on the
   * registered <code>{@link PropertyChangeListener}</code> for the trace
   * removed.
   * <p>
   * 
   * @param trace
   *          the trace to remove from this axis.
   * @return true if the given trace could be removed from this axis, false
   *         else.
   */
  public boolean removeTrace(ITrace2D trace);

  /**
   * Scales all <code>{@link ITrace2D}</code> instances in the dimension
   * represented by this axis.
   * <p>
   * This method is not deadlock - safe and should be called by the
   * <code>{@link Chart2D}</code> only!
   * <p>
   */
  public void scale();

  /**
   * Scales the given <code>{@link ITrace2D}</code> in the dimension represented
   * by this axis.
   * <p>
   * This method is not deadlock - safe and should be called by the
   * <code>{@link Chart2D}</code> only!
   * <p>
   * 
   * @param trace
   *          the trace to scale.
   */
  public void scaleTrace(final ITrace2D trace);

  /**
   * Sets the title of this axis.
   * <p>
   * 
   * @param axisTitle
   *          the axis title to use.
   */
  public void setAxisTitle(final IAxis.AxisTitle axisTitle);

  /**
   * Sets the formatter to use for labels.
   * <p>
   * 
   * @param formatter
   *          The formatter to set.
   */
  public abstract void setFormatter(final IAxisLabelFormatter formatter);

  /**
   * This method sets the major tick spacing for label generation.
   * <p>
   * Only values between 0.0 and 100.0 are allowed.
   * <p>
   * The number that is passed in represents the distance, measured in values,
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
  public abstract void setMajorTickSpacing(final double majorTickSpacing);

  /**
   * This method sets the minor tick spacing for label generation.
   * <p>
   * The number that is passed-in represents the distance, measured in values,
   * between each minor tick mark. If you have a trace with a range from 0 to 10
   * and the minor tick spacing is set to 2, you will get major ticks next to
   * the following values: 0, 2, 4, 6, 8, 10. If a major tick hits the same
   * values the tick will be a major ticks. For this example: if a major tick
   * spacing is set to 5 you will only get minor ticks for: 2, 4, 6, 8.
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
  public abstract void setMinorTickSpacing(final double minorTickSpacing);

  /**
   * Set wether the grid in this dimension should be painted or not.
   * <p>
   * A repaint operation for the chart is triggered.
   * <p>
   * 
   * @param grid
   *          true if the grid should be painted or false if not.
   */

  public abstract void setPaintGrid(boolean grid);

  /**
   * Set if the scale for this axis should be shown.
   * <p>
   * 
   * @param show
   *          true if the scale on this axis should be shown, false else.
   */
  public abstract void setPaintScale(final boolean show);

  /**
   * Sets a Range to use for filtering the view to the the connected Axis. Note
   * that it's effect will be affected by the internal {@link IRangePolicy}.
   * <p>
   * This must only be called from the <code>{@link Chart2D}</code> itself!
   * <p>
   * 
   * @param pixel
   *          the left pixel coordinate of this axis in the graphic context of
   *          the current paint operation.
   */
  public void setPixelXLeft(int pixel);

  /**
   * Sets the right pixel of this axis coordinate in the graphic context of the
   * current paint operation.
   * <p>
   * This must only be called from the <code>{@link Chart2D}</code> itself!
   * <p>
   * 
   * @param pixel
   *          the right pixel coordinate of this axis in the graphic context of
   *          the current paint operation.
   */
  public void setPixelXRight(int pixel);

  /**
   * Sets the bottom pixel of this axis coordinate in the graphic context of the
   * current paint operation.
   * <p>
   * This must only be called from the <code>{@link Chart2D}</code> itself!
   * <p>
   * 
   * @param pixel
   *          the bottom pixel coordinate of this axis in the graphic context of
   *          the current paint operation.
   */
  public void setPixelYBottom(int pixel);

  /**
   * Sets the top pixel of this axis coordinate in the graphic context of the
   * current paint operation.
   * <p>
   * This must only be called from the <code>{@link Chart2D}</code> itself!
   * <p>
   * 
   * @param pixel
   *          the top pixel coordinate of this axis in the graphic context of
   *          the current paint operation.
   */
  public void setPixelYTop(int pixel);

  /**
   * Sets a Range to use for filtering the view to the the connected Axis. Note
   * that it's effect will be affected by the internal {@link IRangePolicy}.
   * <p>
   * To get full control use: <br>
   * <code> setRangePolicy(new &lt;AnARangePolicy&gt;(range);</code>
   * <p>
   * 
   * @param range
   *          Range to use for filtering the view to the the connected Axis.
   * @see #getRangePolicy()
   * @see IRangePolicy#setRange(Range)
   */
  public abstract void setRange(final Range range);

  /**
   * Sets the RangePolicy.
   * <p>
   * If the given RangePolicy has an unconfigured internal Range (
   * {@link Range#RANGE_UNBOUNDED}) the old internal RangePolicy is taken into
   * account: <br>
   * If the old RangePolicy has a configured Range this is transferred to the
   * new RangePolicy.
   * <p>
   * 
   * @param rangePolicy
   *          The rangePolicy to set.
   */
  public abstract void setRangePolicy(final IRangePolicy rangePolicy);

  /**
   * Set wether scale values are started from major ticks.
   * <p>
   * 
   * @param majorTick
   *          true if scale values shall start with a major tick.
   * @see info.monitorenter.gui.chart.axis.AAxis#setMajorTickSpacing(double)
   */
  public abstract void setStartMajorTick(final boolean majorTick);

  /**
   * Sets the title of this axis will be painted by the
   * <code>{IAxisTitlePainter}</code> of this instance.
   * <p>
   * 
   * @param title
   *          the title to set.
   * @return the previous Title or <code>null</code> if there was no title
   *         configured before.
   * @see #setTitlePainter(IAxisTitlePainter)
   * @deprecated use {@link #getAxisTitle()} and on the result
   *             {@link AxisTitle#setTitle(String)}
   */
  @Deprecated
  public String setTitle(String title);

  /**
   * Sets the title painter that will paint the title of this axis.
   * <p>
   * 
   * @param painter
   *          the instance that will paint the title of this axis.
   * @return the previous title painter of this axis or null if there was none
   *         configured before.
   * @deprecated use {@link #getAxisTitle()} and on the result
   *             {@link IAxis.AxisTitle#setTitlePainter(IAxisTitlePainter)}.
   */
  @Deprecated
  public IAxisTitlePainter setTitlePainter(final IAxisTitlePainter painter);

  /**
   * Show/hide this axis.
   * <p>
   * 
   * @param visible
   *          true to paint axis, false to hide.
   */
  public void setVisible(boolean visible);

  /**
   * Transforms the given pixel value (which has to be a awt value like
   * {@link java.awt.event.MouseEvent#getY()} into the chart value.
   * <p>
   * Internal use only, the interface does not guarantee that the pixel
   * corresponds to any valid awt pixel value within the chart component.
   * <p>
   * 
   * @param pixel
   *          a pixel value of the chart component as used by awt.
   * @return the awt pixel value transformed to the chart value.
   */
  public double translatePxToValue(final int pixel);

  /**
   * Transforms the given chart data value into the corresponding awt pixel
   * value for the chart.
   * <p>
   * 
   * @param value
   *          a chart data value.
   * @return the awt pixel value corresponding to the chart data value.
   */
  public int translateValueToPx(final double value);

}
