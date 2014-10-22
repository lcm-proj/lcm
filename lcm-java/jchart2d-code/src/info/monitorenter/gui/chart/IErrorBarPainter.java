/*
 *  IErrorBarPainter.java of project jchart2d, interface for a 
 *  renderer of error bars. 
 *  Copyright (c) 2004 - 2011 Achim Westermann.
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

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

/**
 * Interface for a renderer of error bars.
 * <p>
 * It contains a similar method to
 * {@link info.monitorenter.gui.chart.ITracePainter#paintPoint(int, int, int, int, Graphics, ITracePoint2D)}
 * with an additional {@link info.monitorenter.gui.chart.IErrorBarPixel} for
 * getting information about the error to render.
 * <p>
 * A visible error bar consists of three parts:
 * <ul>
 * <li>The start point</li>
 * <li>The end point</li>
 * <li>The segment connecting the start point and the end point</li>
 * </ul>
 * This interface offers to configure these three parts by
 * {@link info.monitorenter.gui.chart.IPointPainter} instances (which are used
 * by {@link info.monitorenter.gui.chart.ITracePainter} implementations too.
 * <p>
 * If one of these three parts (see the setters) is null, the corresponding part
 * should not be painted.
 * <p>
 * A further configuration is to define the colors of these parts by the
 * according setters.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.27 $
 */
public interface IErrorBarPainter extends Serializable {

  /**
   * Facace interface for accessing a connection segment of
   * <code>{@link info.monitorenter.gui.chart.errorbars.ErrorBarPainter}</code>.
   * <p>
   * This is the <code>{@link IPointPainter}</code> and the color of that
   * segment.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * @version $Revision: 1.27 $
   */
  public interface ISegment extends Serializable {
    /**
     * Registers the given property change listener to receive
     * <code>{@link java.beans.PropertyChangeEvent}</code> instances upon a
     * change of the given property.
     * <p>
     * Note that implementations should delegate this call to
     * <code>{@link IErrorBarPainter#addPropertyChangeListener(String, PropertyChangeListener)}</code>
     * (the instance that belongs to this inner non-static class) for integrity
     * with the fact that an <code>{@link IErrorBarPainter.ISegment}</code> is
     * just a facade to the outer class.
     * <p>
     * 
     * @param property
     *          one of the properties: {@link #getPropertySegmentColor()},
     *          {@link #getPropertySegmentPointPainter()}.
     * @param listener
     *          the instances interested in a change of the property.
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener);

    /**
     * Returns the color of this segment or null if none has been configured in the underlying 
     * <code>{@link IPointPainterConfigurableUI}</code>.
     * <p>
     * 
     * @return the color of this segment or null if none has been configured in the underlying 
     * <code>{@link IPointPainterConfigurableUI}</code>.
     */
    public Color getColor();

    /**
     * Returns a descriptive name that may be used in UI interfaces.
     * <p>
     * 
     * @return a descriptive name that may be used in UI interfaces.
     */
    public String getName();

    /**
     * Returns the point painter of this segment.
     * <p>
     * 
     * @return the point painter used.
     */
    public IPointPainterConfigurableUI< ? > getPointPainter();

    /**
     * Returns the property name of the color property to use with
     * <code>{@link #addPropertyChangeListener(String, PropertyChangeListener)}</code>
     * .
     * <p>
     * This is the
     * <code>{@link IErrorBarPainter#PROPERTY_CONNECTION_COLOR}</code> for the
     * instance returned by
     * <code>{@link IErrorBarPainter#getSegmentConnection()}</code>,
     * <code>{@link IErrorBarPainter#PROPERTY_ENDPOINT_COLOR}</code> for the
     * instance returned by
     * <code>{@link IErrorBarPainter#getSegmentEnd()}</code> and
     * <code>{@link IErrorBarPainter#PROPERTY_STARTPOINT_COLOR}</code> for the
     * instance returned by
     * <code>{@link IErrorBarPainter#getSegmentStart()}</code> for integrity
     * with those properties meaning the same.
     * <p>
     * 
     * @return the property name of the color property to use with <code>
     *         {@link #addPropertyChangeListener(String, PropertyChangeListener)}
     *         </code>.
     */
    public String getPropertySegmentColor();

    /**
     * Returns the property name of the point painter property to use with
     * <code>{@link #addPropertyChangeListener(String, PropertyChangeListener)}</code>
     * .
     * <p>
     * This is the <code>{@link IErrorBarPainter#PROPERTY_CONNECTION}</code> for
     * the instance returned by
     * <code>{@link IErrorBarPainter#getSegmentConnection()}</code>,
     * <code>{@link IErrorBarPainter#PROPERTY_ENDPOINT}</code> for the instance
     * returned by <code>{@link IErrorBarPainter#getSegmentEnd()}</code> and
     * <code>{@link IErrorBarPainter#PROPERTY_STARTPOINT}</code> for the
     * instance returned by
     * <code>{@link IErrorBarPainter#getSegmentStart()}</code> for integrity
     * with those properties meaning the same.
     * <p>
     * 
     * @return the property name of the color property to use with <code>
     *         {@link #addPropertyChangeListener(String, PropertyChangeListener)}
     *         </code>.
     */
    public String getPropertySegmentPointPainter();

    /**
     * Sets the color of this segment.
     * <p>
     * 
     * @param color
     *          the color to use.
     */
    public void setColor(Color color);

    /**
     * Sets the point painter of this segment.
     * <p>
     * 
     * @param pointPainter
     *          the point painter to use.
     */
    public void setPointPainter(IPointPainterConfigurableUI< ? > pointPainter);
  }

  /**
   * The property key defining the <code>connection</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_CONNECTION = "IErrorBarPainter.PROPERTY_CONNECTION";

  /**
   * The property key defining the <code>connectionColor</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_CONNECTION_COLOR = "IErrorBarPainter.PROPERTY_CONNECTION_COLOR";

  /**
   * The property key defining the <code>endPointPainter</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_ENDPOINT = "IErrorBarPainter.PROPERTY_ENDPOINT";

  /**
   * The property key defining the <code>endPointColor</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_ENDPOINT_COLOR = "IErrorBarPainter.PROPERTY_ENDPOINT_COLOR";

  /**
   * The property key defining the <code>startPointPaint</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_STARTPOINT = "IErrorBarPainter.PROPERTY_STARTPOINT";

  /**
   * The property key defining the <code>startPointColor</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_STARTPOINT_COLOR = "IErrorBarPainter.PROPERTY_STARTPOINT_COLOR";

  /**
   * Registers a property change listener that will be informed about changes of
   * the property identified by the given <code>propertyName</code>.
   * <p>
   * <h3>PropertyChangeEvents</h3>
   * {@link java.beans.PropertyChangeListener} instances may be added via
   * {@link javax.swing.JComponent#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
   * . They inherit the properties to listen from
   * {@link java.awt.Container#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
   * . Additionally more <code>PropertyChangeEvents</code> should be triggered
   * <b>(contract for implementation!)</b>.
   * <p>
   * <h3>Property Change events</h3>
   * The following {@link java.beans.PropertyChangeEvent} instances will be
   * fired to registered {@link java.beans.PropertyChangeListener} instances.
   * <table border="0">
   * <tr>
   * <th><code>getPropertyName()</code></th>
   * <th><code>getSource()</code></th>
   * <th><code>getOldValue()</code></th>
   * <th><code>getNewValue()</code></th>
   * <th>comment</th>
   * </tr>
   * <tr>
   * <td><code>{@link #PROPERTY_ENDPOINT}</code></td>
   * <td><code>{@link IErrorBarPainter}</code> that changed.</td>
   * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>.</td>
   * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>.</td>
   * <td>Note that null as the old value indicates a new painter. Null as the
   * new value is valid and means that the end point rendering is turned off.</td>
   * </tr>
   * <tr>
   * <td><code>{@link #PROPERTY_STARTPOINT}</code></td>
   * <td><code>{@link IErrorBarPainter}</code> that changed.</td>
   * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>.</td>
   * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>.</td>
   * <td>Note that null as the old value indicates a new painter. Null as the
   * new value is valid and means that the start point rendering is turned off.</td>
   * </tr>
   * <tr>
   * <td><code>{@link #PROPERTY_CONNECTION}</code></td>
   * <td><code>{@link IErrorBarPainter}</code> that changed.</td>
   * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>.</td>
   * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>.</td>
   * <td>Note that null as the old value indicates a new painter. Null as the
   * new value is valid and means that the connection segment point rendering is
   * turned off.</td>
   * </tr>
   * <tr>
   * <td><code>{@link #PROPERTY_ENDPOINT_COLOR}</code></td>
   * <td><code>{@link IErrorBarPainter}</code> that changed.</td>
   * <td><code>{@link java.awt.Color}</code>.</td>
   * <td><code>{@link java.awt.Color}</code>.</td>
   * <td>Note that null as the old value indicates a new color different from
   * the default. Null as the new value is valid and means that the end point
   * color is switched to default.</td>
   * </tr>
   * <tr>
   * <td><code>{@link #PROPERTY_STARTPOINT_COLOR}</code></td>
   * <td><code>{@link IErrorBarPainter}</code> that changed.</td>
   * <td><code>{@link java.awt.Color}</code>.</td>
   * <td><code>{@link java.awt.Color}</code>.</td>
   * <td>Note that null as the old value indicates a new color different from
   * the default. Null as the new value is valid and means that the start point
   * color is switched to default.</td>
   * </tr>
   * <tr>
   * <td><code>{@link #PROPERTY_CONNECTION_COLOR}</code></td>
   * <td><code>{@link IErrorBarPainter}</code> that changed.</td>
   * <td><code>{@link java.awt.Color}</code>.</td>
   * <td><code>{@link java.awt.Color}</code>.</td>
   * <td>Note that null as the old value indicates a new color different from
   * the default. Null as the new value is valid and means that the connection
   * segment color is switched to default.</td>
   * </tr>
   * </table>
   * <p>
   * 
   * @param propertyName
   *          the name of the property the listener is interested in
   * @param listener
   *          a listener that will only be informed if the property identified
   *          by the argument <code>propertyName</code> changes
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Returns the color of the connection segment or null if unconfigured.
   * <p>
   * 
   * @return the color of the connection segment or null if unconfigured.
   */
  public Color getConnectionColor();

  /**
   * Returns the painter for the connection segment of the error bar.
   * <p>
   * 
   * @return The painter for the connection segment of the error bar.
   */
  public IPointPainter< ? > getConnectionPainter();

  /**
   * Returns the color of the end point or null if unconfigured.
   * <p>
   * 
   * @return the color of the end point or null if unconfigured.
   */
  public Color getEndPointColor();

  /**
   * Returns the painter for the end point of the error bar.
   * <p>
   * 
   * @return The painter for the end point of the error bar.
   */
  public IPointPainterConfigurableUI< ? > getEndPointPainter();

  /**
   * Returns all property change listeners for the given property.
   * <p>
   * 
   * @param property
   *          one of the constants with the <code>PROPERTY_</code> prefix
   *          defined in this class or subclasses.
   * @return the property change listeners for the given property.
   */
  public PropertyChangeListener[] getPropertyChangeListeners(String property);

  /**
   * Returns the facade instance for accessing the connection segment of this
   * configurable error bar painter.
   * <p>
   * 
   * @return the facade instance for accessing the connection segment of this
   *         configurable error bar painter.
   */
  public ISegment getSegmentConnection();

  /**
   * Returns the facade instance for accessing the end segment of this
   * configurable error bar painter.
   * <p>
   * 
   * @return the facade instance for accessing the end segment of this
   *         configurable error bar painter.
   */
  public ISegment getSegmentEnd();

  /**
   * Returns the facade instance for accessing the start segment of this
   * configurable error bar painter.
   * <p>
   * 
   * @return the facade instance for accessing the start segment of this
   *         configurable error bar painter.
   */
  public ISegment getSegmentStart();

  /**
   * Returns the color of the start point or null if unconfigured.
   * <p>
   * 
   * 
   * @return the color of the start point or null if unconfigured.
   */
  public Color getStartPointColor();

  /**
   * Returns the painter for the start point of the error bar.
   * <p>
   * 
   * @return the painter for the start point of the error bar.
   */
  public IPointPainterConfigurableUI< ? > getStartPointPainter();

  /**
   * Paint the error bar for the point given by absolute coordinates on the
   * given graphic context.
   * <p>
   * Basic implementations should modularize further and allow configuration for
   * the way of painting the connection segments, the start point (origin) and
   * end point of the error bar with implementations of {@link IPointPainter}.
   * <p>
   * 
   * @param absoluteX
   *          the ready to use x value for the point to paint.
   * 
   * @param absoluteY
   *          the ready to use y value for the point to paint.
   * 
   * @param errorBar
   *          contains the data for the errors to render.
   * 
   * @param original
   *          the original trace point this error bar is painted for.
   * 
   * @param g
   *          the graphic context to paint on.
   * 
   * @see IErrorBarPixel
   */
  public void paintErrorBar(final int absoluteX, final int absoluteY, final ITracePoint2D original,
      final Graphics g, final IErrorBarPixel errorBar);

  /**
   * Unregisters a property change listener that has been registered for
   * listening on all properties.
   * <p>
   * 
   * @param listener
   *          a listener that will only be informed if the property identified
   *          by the argument <code>propertyName</code> changes
   */
  public void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a property change listener for listening on the given property.
   * <p>
   * 
   * @param property
   *          one of the constants with teh <code>PROPERTY_</code> prefix
   *          defined in this class or subclasses.
   * @param listener
   *          the listener for this property change.
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener);

  /**
   * Sets the color for the connection segment.
   * <p>
   * If this is not used or null is provided, the color of the corresponding
   * trace will be used. If no underlying connection painter exists nothing will be done. 
   * <p>
   * 
   * @param connectionColor
   *          The connection segment color to set.
   */
  public void setConnectionColor(Color connectionColor);

  /**
   * Note that the choice for the right point painter has to be taken with care:
   * It is senseless to use an implementation that does not interconnect both
   * coordinates given to
   * {@link IPointPainter#paintPoint(int, int, int, int, Graphics, ITracePoint2D)}
   * .
   * <p>
   * Choosing a
   * {@link info.monitorenter.gui.chart.pointpainters.PointPainterLine} will
   * have the same visual effect as setting such an instance for the start point
   * painter.
   * <p>
   * Currently the only useful choice is the
   * {@link info.monitorenter.gui.chart.pointpainters.PointPainterLine} or null
   * (to make the connection segment invisible). But the interface is open
   * enough to use implementations that would paint interpolated dots, discs,
   * squares,... whatever you think of (contribute!).
   * <p>
   * 
   * @param connectionPainter
   *          The connection segmentPainter to set.
   */
  public void setConnectionPainter(final IPointPainterConfigurableUI< ? > connectionPainter);

  /**
   * Sets the color for the end point.
   * <p>
   * If this is not used or null is provided, the color of the corresponding
   * trace will be used. If no underlying end point painter exists nothing will be done. 
   * <p>
   * 
   * @param endPointColor
   *          The end point color to set.
   */
  public void setEndPointColor(Color endPointColor);

  /**
   * Sets the painter for the end point of the error bar.
   * <p>
   * Note that the choice for the right point painter has to be taken with care:
   * It is senseless to use an implementation that interconnects both
   * coordinates given to
   * {@link IPointPainter#paintPoint(int, int, int, int, Graphics, ITracePoint2D)}.
   * <p>
   * Choosing a
   * {@link info.monitorenter.gui.chart.pointpainters.PointPainterLine} will
   * have the same visual effect as setting such an instance for the connection
   * segment painter.
   * <p>
   * 
   * @param endPointPainter
   *          The end point painter to set.
   */
  public void setEndPointPainter(final IPointPainterConfigurableUI< ? > endPointPainter);

  /**
   * Sets the color for the start point.
   * <p>
   * If this is not used or null is provided, the color of the corresponding
   * trace will be used. If no underlying start point painter exists nothing will be done. 
   * <p>
   * 
   * @param startPointColor
   *          The start point color to set.
   */
  public void setStartPointColor(Color startPointColor);

  /**
   * Note that the choice for the right point painter has to be taken with care:
   * It is senseless to use an implementation that interconnects both
   * coordinates given to
   * {@link IPointPainter#paintPoint(int, int, int, int, Graphics, ITracePoint2D)}
   * .
   * <p>
   * Choosing a
   * {@link info.monitorenter.gui.chart.pointpainters.PointPainterLine} will
   * have the same visual effect as setting such an instance for the connection
   * segment painter.
   * <p>
   * 
   * @param startPointPainter
   *          The startPointPainter to set.
   */
  public void setStartPointPainter(final IPointPainterConfigurableUI< ? > startPointPainter);

}
