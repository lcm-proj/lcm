/*
 * ITrace2D, the interface for all traces used by the Chart2D.
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
package info.monitorenter.gui.chart;

import java.awt.Color;
import java.awt.Stroke;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

/**
 * An interface used by <code>Chart2D</code>. ITrace2D contains the values to
 * display, the color for displaying and a suitable label. It may be seen as a
 * trace of the <code>Chart2D</code> that displays it. <br>
 * Implementations may be optimized for different use- cases: <br>
 * RingBuffers for fast changing data to keep the amount of trace points and
 * consumption of memory constant, internal Lists for allowing the sorting the
 * internal <code>TracePoint2D</code>- instances or Arrays for unordered Data
 * (in the order of adding) for fast performance. Even an <code>ITrace2D</code>
 * constructed by a "function- Object" may be thinkable.
 * <p>
 * There are various constraints for Traces: <br>
 * - ordered by x-values <br>
 * - ordered by the order of <code>addPoint</code>- invocation (control form
 * outside) <br>
 * - unique, single x- values <br>
 * - limitation of tracepoints <br>
 * - time- critical (fast- changing tracepoints) <br>
 * <br>
 * Therefore there are various <code>ITrace2D</code>- implementations. Read
 * their description to find the one you need. Some may not have been written
 * yet.
 * <p>
 * {@link java.lang.Comparable} should be implemented by using the internal
 * property zIndex (see {@link #getZIndex()}, {@link #setZIndex(Integer)}).
 * <p>
 * <h3>Property Change events</h3>
 * The following <code>{@link java.beans.PropertyChangeEvent}</code> may be
 * fired to <code>{@link PropertyChangeListener}</code> instances that register
 * themselves with
 * <code>{@link #addPropertyChangeListener(String, PropertyChangeListener)}</code>.
 * <table * border="0">
 * <tr>
 * <th><code>getPropertyName()</code></th>
 * <th><code>getSource()</code></th>
 * <th><code>getOldValue()</code></th>
 * <th><code>getNewValue()</code></th>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_ZINDEX}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.lang.Number}</code>, the old value</td>
 * <td><code>{@link java.lang.Number}</code>, the new value</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_MAX_X}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.lang.Double}</code>, the old value</td>
 * <td><code>{@link java.lang.Double}</code>, the new value</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_MIN_X}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.lang.Double}</code>, the old value</td>
 * <td><code>{@link java.lang.Double}</code>, the new value</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_MAX_Y}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.lang.Double}</code>, the old value</td>
 * <td><code>{@link java.lang.Double}</code>, the new value</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_MIN_Y}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.lang.Double}</code>, the old value</td>
 * <td><code>{@link java.lang.Double}</code>, the new value</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_TRACEPOINT}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.TracePoint2D}</code>, the
 * instance that was removed</td>
 * <td><code>null</code>, indication that an instance was removed</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_TRACEPOINT}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>null</code>, indication that a value was added</td>
 * <td><code>{@link info.monitorenter.gui.chart.TracePoint2D}</code>, the new
 * instance that was added, identifying that an instance was removed</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_VISIBLE}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.lang.Boolean}</code>, the old state.</td>
 * <td><code>{@link java.lang.Boolean}</code>, the new state.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_PAINTERS}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>null</code>, indicating that a painter was added.</td>
 * <td><code>{@link info.monitorenter.gui.chart.ITracePainter}</code>, the new
 * painter.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_PAINTERS}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.ITracePainter}</code>, the old
 * painter.</td>
 * <td><code>null</code>, indicating that a painter was removed.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_POINT_HIGHLIGHTERS_CHANGED}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>null</code>, indicating that a point highlighter was added.</td>
 * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>, the
 * new highlighter.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_POINT_HIGHLIGHTERS_CHANGED}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>, the
 * old highlighter.</td>
 * <td><code>null</code>, indicating that a point highlighter was removed.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_PAINTERS}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.IPointPainter}</code>, the
 * old highlighter.</td>
 * <td><code>null</code>, indicating that a highlighter was removed.</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_STROKE}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.awt.Stroke}</code>, the old stroke.</td>
 * <td><code>{@link java.awt.Stroke}</code>, the new stroke.</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_COLOR}</code></td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link java.awt.Color}</code>, the new color.</td>
 * <td><code>{@link java.awt.Color}</code>, the new color.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_ERRORBARPOLICY}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>null</code>, indicating that an error bar policy was added.</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code>, the new
 * error bar policy.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_ERRORBARPOLICY}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code>, the old
 * error bar policy.</td>
 * <td><code>null</code>, indicating that an error bar policy was removed.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_ERRORBARPOLICY_CONFIGURATION}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that notifies the change of the configured
 * error bar policy.</td>
 * <td>null</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code>, the
 * instance with the configuration change.</td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_POINT_CHANGED}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that notifies the change of the contained
 * trace point.</td>
 * <td><code>null</code> because it would be too much overhead to store the old
 * point in a additional instance as the original is modified in
 * <code>{@link TracePoint2D#setLocation(java.awt.geom.Point2D)}</code>.</td>
 * <td><code>{@link TracePoint2D}</code> the point whose location was modified.</td>
 * </tr>
 * <tr>
 * <td><code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_NAME}</code></td>
 * <td><code>{@link ITrace2D}</code> that notifies the change.</td>
 * <td><code>{@link String}</code>, the old value of
 * <code>{@link #getName()}</code></td>
 * <td><code>{@link String}</code>, the new value of
 * <code>{@link #getName()}</code></td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_PHYSICALUNITS}</code>
 * </td>
 * <td><code>{@link ITrace2D}</code> that notifies the change.</td>
 * <td><code>{@link String}</code>, the old value of
 * <code>{@link #getPhysicalUnits()}</code></td>
 * <td><code>{@link String}</code>, the new value of
 * <code>{@link #getPhysicalUnits()}</code></td>
 * </tr>
 * <tr>
 * <td>
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#PROPERTY_LABEL}</code>
 * (triggered by <code>{@link #setName(String)}</code> and
 * <code>{@link #setPhysicalUnits(String, String)}</code>)</td>
 * <td><code>{@link ITrace2D}</code> that notifies the change.</td>
 * <td><code>{@link String}</code>, the old value of
 * <code>{@link #getLabel()}</code></td>
 * <td><code>{@link String}</code>, the new value of
 * <code>{@link #getLabel()}</code></td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.41 $
 */
public interface ITrace2D extends PropertyChangeListener, Comparable<ITrace2D>, Serializable {

  /**
   * Simple struct just for allowing to return a trace point along with a
   * weighted distance.
   * <p>
   * TODO: change this to an interface and implement in abstract base class to
   * hide constructor.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * @version $Revision: 1.41 $
   */
  public final class DistancePoint {
    /** Constant for unfound distance. */
    public static final DistancePoint EMPTY = new DistancePoint();

    /** The measured distance. */
    private double m_distance;

    /** The point with the distance. */
    private ITracePoint2D m_point;

    /**
     * Defcon.
     */
    public DistancePoint() {
      // nop.
    }

    /**
     * @return the distance.
     */
    public final double getDistance() {
      return this.m_distance;
    }

    /**
     * @return the point.
     */
    public final ITracePoint2D getPoint() {
      return this.m_point;
    }

    /**
     * @param manhattandistance
     *          the manhattandistance to set
     */
    public final void setDistance(final double manhattandistance) {
      this.m_distance = manhattandistance;
    }

    /**
     * @param point
     *          the point to set
     */
    public final void setPoint(final ITracePoint2D point) {
      this.m_point = point;
    }

  }

  /**
   * The property key defining the <code>color</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_COLOR = "ITrace2D.PROPERTY_COLOR";

  /**
   * The property key defining a change of <code>{@link IErrorBarPolicy}</code>
   * instances contained.
   * <p>
   * This is fired from <code>{@link #addErrorBarPolicy(IErrorBarPolicy)}</code>, <code>{@link #removeErrorBarPolicy(IErrorBarPolicy)}</code> and
   * <code>{@link #setErrorBarPolicy(IErrorBarPolicy)}</code>.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   */
  public static final String PROPERTY_ERRORBARPOLICY = "ITrace2D.PROPERTY_ERRORBARPOLICY";

  /**
   * The property key defining a change of the configuration of a contained
   * <code>{@link IErrorBarPolicy}</code>.
   * <p>
   * This is fired whenever an <code>IErrorBarPolicy</code> notifies this
   * instance of a configuration change via an event for
   * <code>{@link IErrorBarPolicy#PROPERTY_CONFIGURATION}</code>.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   */
  public static final String PROPERTY_ERRORBARPOLICY_CONFIGURATION = "ITrace2D.PROPERTY_ERRORBARPOLICY_CONFIGURATION";

  /**
   * The property key defining the <code>label</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   * 
   * This is no real property as the <code>{@link #getLabel()}</code> access two
   * other fields.
   * <p>
   */
  public static final String PROPERTY_LABEL = "ITrace2D.PROPERTY_LABEL";

  /**
   * The property key defining the <code>maxX</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_MAX_X = "ITrace2D.PROPERTY_MAX_X";

  /**
   * The property key defining the <code>maxY</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_MAX_Y = "ITrace2D.PROPERTY_MAX_Y";

  /**
   * The property key defining the <code>minX</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_MIN_X = "ITrace2D.PROPERTY_MIN_X";

  /**
   * The property key defining the <code>minY</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_MIN_Y = "ITrace2D.PROPERTY_MIN_Y";

  /**
   * The property key defining the <code>name</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_NAME = "ITrace2D.PROPERTY_NAME";

  /**
   * The property key defining a change in the set of <code>
   * {@link ITracePainter}</code>
   * instances. Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_PAINTERS = "ITrace2D.PROPERTY_PAINTERS";

  /**
   * The property key defining the <code>physicalUnits</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_PHYSICALUNITS = "ITrace2D.PROPERTY_PHYSICALUNITS";

  /**
   * The property key defining any change of a location of a contained <code>
   * {@link TracePoint2D} </code>
   * .
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * Property change event with this property will be fired if method
   * <code>{@link TracePoint2D#setLocation(java.awt.geom.Point2D)}</code> or
   * <code>{@link TracePoint2D#setLocation(double, double)}</code> of any
   * contained point is invoked.
   * <p>
   */
  public static final String PROPERTY_POINT_CHANGED = "ITrace2D.PROPERTY_POINT_CHANGED";

  /**
   * The property key defining a change in the set of <code>
   * {@link IPointPainter}</code>
   * instances. Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_POINT_HIGHLIGHTERS_CHANGED = "ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED";

  /**
   * The property key defining the <code>stroke</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_STROKE = "ITrace2D.PROPERTY_STROKE";

  /**
   * The property key defining a change in the collection of <code>
   * {@link TracePoint2D}</code>
   * instances within this trace. Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_TRACEPOINT = "ITrace2D.PROPERTY_TRACEPOINT";

  /**
   * The property key defining the <code>visible</code> property. Use in
   * combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   */
  public static final String PROPERTY_VISIBLE = "ITrace2D.PROPERTY_VISIBLE";

  /**
   * The property key defining the <code>zIndex</code> property.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   */
  public static final String PROPERTY_ZINDEX = "ITrace2D.PROPERTY_ZINDEX";

  /**
   * The minimum value for property zIndex: 0.
   * 
   * @see #getZIndex()
   * @see #setZIndex(Integer)
   */
  public static final int Z_INDEX_MIN = 0;

  /**
   * The maximum value for property zIndex: 100.
   * <p>
   * the descriptive name for this trace.
   * <p>
   * 
   * @see #getZIndex()
   * @see #setZIndex(Integer)
   */
  public static final int ZINDEX_MAX = 100;

  /**
   * Adds a trace that "should" compute values from the points added via <code>
   * {@link #addPoint(ITracePoint2D)}</code>
   * .
   * <p>
   * The given trace will be informed in case an add operation on this trace
   * succeeds via <code>{@link #addPoint(ITracePoint2D)}</code>.
   * 
   * @param trace
   *          the trace that will calculate it's points from the added points of
   *          this trace.
   */
  public void addComputingTrace(ITrace2D trace);

  /**
   * Adds the given error bar policy to the internal set of error bar policies.
   * <p>
   * It will be the last error bar policy to render (most forward on screen).
   * <p>
   * 
   * @param errorBarPolicy
   *          the error bar policy to add for rendering this trace's error bars.
   * @return true if the painter was added (same instance was not contained
   *         before).
   */
  public boolean addErrorBarPolicy(IErrorBarPolicy< ? > errorBarPolicy);

  /**
   * Adds a trace point to the internal data.
   * <p>
   * <b>Warning</b>:<br/>
   * Do not call this method before this trace has been added to a chart or you
   * will not succeed as the chart is needed to get the proper <code>
   *             {@link Chart2D#getTracePointProvider()}</code>.
   * <p>
   * 
   * 
   * @see #addPoint(ITracePoint2D p)
   * @param x
   *          the x-value of the point to add.
   * @param y
   *          the y-value of the point to add.
   * 
   * @return true if the operation was successful, false else.
   * 
   **/
  public boolean addPoint(double x, double y);

  /**
   * Adds the given <code>TracePoint2D </code> to the internal data.
   * <p>
   * Try to pass instances of <code>TracePoint2D</code> to this instance instead
   * of invoking <code>{@link #addPoint(double, double)}</code> to increase
   * performance. Else the given point has to be copied into such an instance
   * from the other method and delegated to this method.
   * <p>
   * Implementations decide whether the point will be accepted or not. So they
   * have to update the internal properties <code>minX</code>, <code>maxX</code>,<code>maxY</code> and <code>minY</code> and also care about firing
   * property change events for those properties by method
   * 
   * <code>{@link java.beans.PropertyChangeSupport#firePropertyChange(java.beans.PropertyChangeEvent)}</code>.
   * <p>
   * 
   * @param p
   *          the point to add.
   * @return true if the operation was successful, false else.
   */
  public boolean addPoint(ITracePoint2D p);

  /**
   * Adds the given point painter to the internal set of point highlighters.
   * <p>
   * It will be the last point painter to paint highlighting if highlighting is
   * active.
   * <p>
   * 
   * @param highlighter
   *          the highlighter to add for highlighting this trace.
   * 
   * @return true if the highlighter was added (class of instance not contained
   *         before).
   */
  public boolean addPointHighlighter(IPointPainter< ? > highlighter);

  /**
   * Registers a property change listener that will be informed about changes of
   * the property identified by the given <code>propertyName</code>.
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
   * Adds the given trace painter to the internal set of trace painters.
   * <p>
   * It will be the last painter to paint (most forward).
   * <p>
   * 
   * @param painter
   *          the painter to add for rendering this trace.
   * @return true if the painter was added (class of instance not contained
   *         before).
   */
  public boolean addTracePainter(ITracePainter< ? > painter);

  /**
   * Returns true if the given painter is contained in this compound painter.
   * <p>
   * 
   * @param painter
   *          the painter to check whether it is contained.
   * @return true if the given painter is contained in this compound painter.
   */
  public boolean containsTracePainter(final ITracePainter< ? > painter);

  /**
   * Method to trigger by <code>{@link TracePoint2D#setLocation(double, double)}
   * </code>, <code>{@link #addPoint(ITracePoint2D)}</code>
   * or <code>
   * {@link #removePoint(ITracePoint2D)}</code>.
   * <p>
   * Bound checks are performed and property change events for the properties
   * <code>{@link ITrace2D#PROPERTY_MAX_X}</code>,
   * <code>{@link ITrace2D#PROPERTY_MIN_X}</code>,
   * <code>{@link ITrace2D#PROPERTY_MAX_Y}</code> and
   * <code>{@link ITrace2D#PROPERTY_MIN_Y}</code> are fired if the add bounds
   * have changed due to the modification of the point.
   * <p>
   * 
   * @param changed
   *          the point that has been changed which may be a newly added point
   *          (from <code>{@link #addPoint(ITracePoint2D)}</code>, a removed one
   *          or a modified one.
   * @param state
   *          one of {<code>{@link ITracePoint2D#STATE_ADDED},
   *          {@link ITracePoint2D#STATE_CHANGED},
   *          {@link ITracePoint2D#STATE_REMOVED}</code> to inform about the
   *          type of change.
   */
  public void firePointChanged(final ITracePoint2D changed, final int state);

  /**
   * Because the color is data common to a trace of a <code>Chart2D</code> it is
   * stored here.
   * <p>
   * On the other hand only the corresponding <code>Chart2D </code> may detect
   * the same color chosen for different <code>IChart2D</code> instances to be
   * displayed. Therefore it is allowed to return null. This is a message to the
   * <code>Chart2D</code> to leave it the choice of the color. Then the
   * <code>Chart2D</code> will chose a color not owned by another
   * <code>ITrace2D</code> instance managed and assign it to the null- returning
   * instance.
   * <p>
   * The <code>Chart2D </code> will often call this method. So try to cache the
   * value in implementation and only check on modifications of
   * <code>TracePoint</code> instances or on <code>add</code>- invocations for
   * changes.
   * <p>
   * 
   * @return The chosen java.awt.Color or null if the decision for the color
   *         should be made by the corresponding <code>Chart2D</code>.
   */
  public Color getColor();

  /**
   * Returns the <code>Set&lt;{@link IErrorBarPolicy}&gt;</code> that will be used to render
   * error bars for this trace.
   * <p>
   * 
   * @return the <code>Set&lt;{@link IErrorBarPolicy}&gt;</code> that will be
   *         used to render error bars for this trace.
   */
  public Set<IErrorBarPolicy< ? >> getErrorBarPolicies();

  /**
   * Returns true if this trace has error bars configured.
   * <p>
   * If this returns false, no error bars will be painted for this trace.
   * <p>
   * 
   * @return true if this trace has error bars configured.
   */
  public boolean getHasErrorBars();

  /**
   * Callback method for the <code>Chart2D</code> that returns a
   * <code>String</code> describing the label of the <code>ITrace2D</code> that
   * will be displayed below the drawing area of the <code>Chart2D</code>.
   * <p>
   * This method should be implemented and finalized ASAP in the inheritance
   * tree and rely on the property <code>name</code> and
   * <code>physicalUnits</code>.
   * <p>
   * 
   * @return a String describing the Axis being accessed.
   */
  public String getLabel();

  /**
   * <p>
   * Returns the maximum amount of {@link TracePoint2D} instances that may be
   * added. For implementations that limit the maximum amount this is a
   * reasonable amount. Non-limiting implementations should return
   * {@link Integer#MAX_VALUE}. This allows to detect the unlimitedness. Of
   * course no implementation could store that amount of points.
   * </p>
   * 
   * @return The maximum amount of {@link TracePoint2D} instances that may be
   *         added.
   */
  public int getMaxSize();

  /**
   * Returns the maximum value to be displayed on the x- axis of the
   * <code>Chart2D</code>. Implementations should be synchronized for
   * multithreaded use. No exception is thrown. In case of empty data (no
   * tracepoints) 0 should be returned, to let the Chart2D know.
   * <p>
   * The <code>Chart2D </code> will often call this method. So try to cache the
   * value in implementation and only check on modifications of
   * <code>TracePoint</code> instances or on <code>add</code>- invocations for
   * changes.
   * <p>
   * 
   * @return the maximum value of the internal data for the x- dimension.
   */
  public double getMaxX();

  /**
   * Returns the maximum value to be displayed on the y- axis of the Chart2D.
   * Implementations should be synchronized for multithreaded use. No exception
   * is thrown. In case of empty data (no tracepoints) 0 should be returned.
   * (watch division with zero).
   * <p>
   * 
   * @return the maximum value of the internal data for the y- dimension.
   */
  public double getMaxY();

  /**
   * Returns the minimum value to be displayed on the x- axis of the Chart2D.
   * <p>
   * Implementations should be synchronized for multithreaded use. No exception
   * is thrown. In case of empty data (no tracepoints) 0 should be returned.
   * (watch division with zero).
   * <p>
   * The <code>Chart2D </code> will often call this method. So try to cache the
   * value in implementation and only check on modifications of
   * <code>TracePoint</code> instances or on <code>add</code>- invocations for
   * changes.
   * <p>
   * 
   * @return the minimum value of the internal data for the x- dimension.
   */

  double getMinX();

  /**
   * Returns the minimum value to be displayed on the y- axis of the Chart2D.
   * <p>
   * Implementations should be synchronized for multithreaded use. No exception
   * is thrown. In case of empty data (no tracepoints) 0 should be returned.
   * (watch division with zero).
   * <p>
   * The <code>Chart2D </code> will often call this method. So try to cache the
   * value in implementation and only check on modifications of
   * <code>TracePoint</code> instances or on <code>add</code>- invocations for
   * changes.
   * </p>
   * 
   * @return the minimum value of the internal data for the y- dimension.
   */

  double getMinY();

  /**
   * Returns the name of this trace.
   * <p>
   * 
   * @return the name of this trace.
   * @see #setName(String s)
   */
  public String getName();

  /**
   * Returns the nearest point to the given normalized value coordinates of this
   * trace in Euclid distance.
   * <p>
   * Please note that the arguments must be normalized value coordinates like
   * provided by a <code>{@link TracePoint2D#getScaledX()}</code> or the
   * division of a pixel coordinate by the total pixel range of the chart.
   * <p>
   * Using the Manhattan distance is much faster than Euclid distance as it only
   * includes basic addition an absolute value for computation per point (vs.
   * square root, addition and quadrature for Euclid distance). However the
   * euclid distance spans a circle for the nearest points which is visually
   * more normal for end users than the Manhattan distance which forms a rhombus
   * and reaches far distances in only one dimension.
   * <p>
   * 
   * @param x
   *          the x value as a normalized value between 0 and 1.0.
   * @param y
   *          the x value as a normalized value between 0 and 1.0.
   * 
   * @return the nearest point to the given normalized value coordinates of this
   *         trace in Euclid distance.
   */
  public DistancePoint getNearestPointEuclid(double x, double y);

  /**
   * Returns the nearest point to the given normalized value coordinates of this
   * trace in Manhattan distance.
   * <p>
   * Please note that the arguments must be normalized value coordinates like
   * provided by a <code>{@link TracePoint2D#getScaledX()}</code> or the
   * division of a pixel coordinate by the total pixel range of the chart.
   * <p>
   * Using the Manhattan distance is much faster than Euclid distance as it only
   * includes basic addition an absolute value for computation per point (vs.
   * square root, addition and quadrature for Euclid distance).
   * <p>
   * 
   * @param x
   *          the x value as a normalized value between 0 and 1.0.
   * @param y
   *          the x value as a normalized value between 0 and 1.0.
   * 
   * @return the nearest point to the given normalized value coordinates of this
   *         trace in Manhattan distance.
   */
  public DistancePoint getNearestPointManhattan(double x, double y);

  /**
   * Returns the concatenation <code>[x: "{@link #getPhysicalUnitsX()}", y: "
   * {@link #getPhysicalUnitsY()}"]</code>.
   * <p>
   * 
   * @return the concatenation <code>[x: "{@link #getPhysicalUnitsX()}", y: "
   *         {@link #getPhysicalUnitsY()}"]</code>.
   * @see #setPhysicalUnits(String x,String y)
   */
  public String getPhysicalUnits();

  /**
   * Returns the physical unit string value for the x dimension.
   * <p>
   * 
   * @return the physical unit string value for the x dimension.
   * @see #setPhysicalUnits(String x,String y)
   */
  public String getPhysicalUnitsX();

  /**
   * Returns the physical unit string value for the y dimension.
   * <p>
   * 
   * @return the physical unit string value for the y dimension.
   * @see #setPhysicalUnits(String x,String y)
   */
  public String getPhysicalUnitsY();

  /**
   * Returns the <code>Set&lt;{@link IPointPainter}&gt;</code> that may be used to highlight
   * points of this trace.
   * <p>
   * This is used by the point highlighting feature:
   * <code>{@link Chart2D#enablePointHighlighting(boolean)}</code>
   * 
   * @return the <code>Set&lt;{@link IPointPainter}&gt;</code> that may be used
   *         to highlight points.
   */
  public Set<IPointPainter< ? >> getPointHighlighters();

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
   * @return Returns the renderer.
   */
  public Chart2D getRenderer();

  /**
   * Returns the amount of {@link TracePoint2D} instances currently contained.
   * <p>
   * 
   * @return The amount of <code>{@link TracePoint2D}</code> instances currently
   *         contained.
   */
  public int getSize();

  /**
   * Returns the Stroke that is used to render this instance.
   * <p>
   * 
   * @return the Stroke that is used to render this instance.
   * @see #setStroke(Stroke)
   */
  public Stroke getStroke();

  /**
   * Returns the <code>Set&lt;{@link ITracePainter}&gt;</code> that will be used to paint
   * this trace.
   * <p>
   * 
   * The original set should be returned by contract to allow adding painters
   * "silently" (vs. using
   * <code>{@link ITrace2D#addTracePainter(ITracePainter)}</code>).
   * <p>
   * 
   * @return the <code>Set&lt;{@link ITracePainter}&gt;</code> that will be used
   *         to paint this trace.
   */
  public Set<ITracePainter< ? >> getTracePainters();

  /**
   * The z-index defines the order in which this instance will be painted.
   * <p>
   * A higher value will bring it more "to the front".
   * <p>
   * 
   * @return the z-index that will define the order in which this instance will
   *         be painted.
   */
  public Integer getZIndex();

  /**
   * Returns false if internal <code>{@link TracePoint2D}</code> instances are
   * contained or true if not.
   * <p>
   * 
   * @return <tt>false</tt> if internal <code>{@link TracePoint2D}</code>
   *         instances are contained or <tt>true</tt> if not.
   */
  public boolean isEmpty();

  /**
   * Returns true if this instance should be rendered.
   * <p>
   * 
   * @return true if this instance should be rendered.
   */
  public boolean isVisible();

  /**
   * Returns an <code>Iterator</code> over the internal <code>
   * {@link TracePoint2D}</code>
   * instances.
   * <p>
   * Implementations should be synchronized. This method is meant to allow
   * modifications of the intenal <code>TracePoint2D</code> instances, so the
   * original points should be returned.
   * <p>
   * There is no guarantee that changes made to the contained tracepoints will
   * be reflected in the display immediately. The order the iterator returns the
   * <code>TracePoint2D</code> instances decides how the <code>Chart2D</code>
   * will paint the trace.
   * <p>
   * 
   * @return an <code>Iterator</code> over the internal <code>
   *         {@link TracePoint2D}</code> instances.
   */
  public Iterator<ITracePoint2D> iterator();

  /**
   * Clears all internal point highlighters used.
   * <p>
   * 
   * Returns the <code>Set&lt;{@link IPointPainter}&gt;</code> that was used to highlight
   * points.
   * <p>
   * This is used by the point highlighting feature:
   * <code>{@link Chart2D#enablePointHighlighting(boolean)}</code>
   * 
   * @return the <code>Set&lt;{@link IPointPainter}&gt;</code> that was be used
   *         to highlight points.
   */
  public Set<IPointPainter< ? >> removeAllPointHighlighters();

  /**
   * Removes all internal <code>TracePoint2D</code>.{@link #isEmpty()} will
   * return true afterwards.
   * <p>
   */
  public void removeAllPoints();

  /**
   * Remove a trace that "should" compute values from the points added via
   * <code>{@link #addPoint(ITracePoint2D)}</code>.
   * <p>
   * 
   * @param trace
   *          the trace that will calculate it's points from the added points of
   *          this trace.
   * @return true if the given trace was removed (recognized by the means of
   *         <code>{@link Object#equals(Object)}</code>).
   */
  public boolean removeComputingTrace(ITrace2D trace);

  /**
   * Removes the given error bar policy from the internal set of error bar
   * policies.
   * <p>
   * 
   * @param errorBarPolicy
   *          the error bar policy to remove.
   * @return true if the painter was removed (same instance contained before).
   */
  public boolean removeErrorBarPolicy(IErrorBarPolicy< ? > errorBarPolicy);

  /**
   * Removes the given point from this trace.
   * <p>
   * 
   * @param point
   *          the point to remove.
   * @return true if the remove opertation was successful, false else.
   */
  public boolean removePoint(ITracePoint2D point);

  /**
   * Removes the given point highlighter, if it's class is contained.
   * <p>
   * 
   * @param highlighter
   *          the highlighter to remove.
   * 
   * @return true if a point highlighter of the class of the given argument was
   *         removed.
   */
  public boolean removePointHighlighter(final IPointPainter< ? > highlighter);

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
   *          one of the constants with the <code>PROPERTY_</code> prefix
   *          defined in this class or subclasses.
   * @param listener
   *          the listener for this property change.
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener);

  /**
   * Removes the given trace painter, if it's class is contained and if more
   * painters are remaining.
   * <p>
   * 
   * @param painter
   *          the trace painter to remove.
   * @return true if a trace painter of the class of the given argument was
   *         removed.
   */
  public boolean removeTracePainter(final ITracePainter< ? > painter);

  /**
   * Set a <code>java.awt.Color</code> for this trace.
   * <p>
   * 
   * @param color
   *          the <tt>Color</tt> to set.
   */
  public void setColor(Color color);

  /**
   * Replaces all internal error bar policies by the new one.
   * <p>
   * 
   * @param errorBarPolicy
   *          the new sole painter to use.
   * @return the <code>Set&lt;{@link IErrorBarPolicy}&gt;</code> that was used
   *         before.
   */
  public Set<IErrorBarPolicy< ? >> setErrorBarPolicy(IErrorBarPolicy< ? > errorBarPolicy);

  /**
   * Assingns a specific name to the <code>ITrace2D</code> which will be
   * displayed by the <code>Chart2D</code>.
   * <p>
   * 
   * @param name
   *          the name for this trace.
   */
  public void setName(String name);

  /**
   * Assigns a specific String representing the physical unit to the <code>
   *  ITrace2D</code>
   * (e.g. Volt, Ohm, lux, ...) which will be displayed by the <code>
   * {@link Chart2D}</code>
   * <p>
   * 
   * @param xunit
   *          the physical unit for the x axis.
   * @param yunit
   *          the physical unit for the y axis.
   */
  public void setPhysicalUnits(final String xunit, final String yunit);

  /**
   * Replaces all internal point highlighters by the new one.
   * <p>
   * 
   * @param highlighter
   *          the new sole highlighter to use.
   * 
   * @return the <code>Set&lt;{@link IPointPainter}&gt;</code> that was used
   *         before or null if nothing changed.
   */
  public Set<IPointPainter< ? >> setPointHighlighter(IPointPainter< ? > highlighter);

  /**
   * This is a callback from {@link Chart2D#addTrace(ITrace2D)} and must not be
   * invoked from elsewhere (needed for synchronization).
   * <p>
   * Not the best design to put this to an interface, but Char2D should handle
   * this interface only.
   * <p>
   * 
   * @param renderer
   *          The renderer to set.
   */
  public void setRenderer(Chart2D renderer);

  /**
   * Allows to specify the rendering of the ITrace2D. This Stroke will be
   * assigned to the {@link java.awt.Graphics2D} by the rendering
   * {@link Chart2D} when painting this instance.
   * <p>
   * 
   * @param stroke
   *          the stroke to use for painting this trace.
   */
  public void setStroke(Stroke stroke);

  /**
   * Replaces all internal trace painters by the new one.
   * <p>
   * 
   * @param painter
   *          the new sole painter to use.
   * @return the <code>Set&lt;{@link ITracePainter}&gt;</code> that was used
   *         before.
   */
  public Set<ITracePainter< ? >> setTracePainter(ITracePainter< ? > painter);

  /**
   * Set the visibility. If argument is false, this instance will not be
   * rendered by a Chart2D.
   * <p>
   * 
   * @param visible
   *          true if this trace should be painted, false else.
   */
  public void setVisible(boolean visible);

  /**
   * Sets the internal z-index property. This decides the order in which
   * different traces within the same <code>{@link Chart2D}</code> are painted.
   * <p>
   * The higher the given value is the more this trace will be brought to front.
   * <p>
   * The value must not be lower than {@link #Z_INDEX_MIN}(0) and higher than
   * {@link #ZINDEX_MAX}(100).
   * <p>
   * This might not be tested for increased performance but ignoring these
   * bounds may result in wrong ordering of display.
   * <p>
   * 
   * @param zIndex
   *          the z index of this trace - the lower the value the more in front
   *          the trace will appear amongst other traces in the same chart.
   * @see #getZIndex()
   */
  public void setZIndex(Integer zIndex);

  /**
   * Tests whether error bars are painted by this trace.
   * <p>
   * Returns true if
   * <ul>
   * <li>this trace contains {@link IErrorBarPolicy} instances.</li>
   * <li>and at least one of these instances contains at least one
   * {@link IErrorBarPainter} instance.</li>
   * </ul>
   * <p>
   * 
   * @return true if this trace renders error bars.
   */
  public boolean showsErrorBars();

  /**
   * Tests whether error bars in negative x direction are painted by this trace.
   * <p>
   * Returns true if
   * <ul>
   * <li>this trace contains at leaste one {@link IErrorBarPolicy} instance that
   * {@link IErrorBarPolicy#isShowNegativeXErrors()}.</li>
   * <li>and at least one of these instances contains at least one
   * {@link IErrorBarPainter} instance.</li>
   * </ul>
   * <p>
   * 
   * @return true if this trace renders error bars in negative x direction.
   */
  public boolean showsNegativeXErrorBars();

  /**
   * Tests whether error bars in negative y direction are painted by this trace.
   * <p>
   * Returns true if
   * <ul>
   * <li>this trace contains at leaste one {@link IErrorBarPolicy} instance that
   * {@link IErrorBarPolicy#isShowNegativeYErrors()}.</li>
   * <li>and at least one of these instances contains at least one
   * {@link IErrorBarPainter} instance.</li>
   * </ul>
   * <p>
   * 
   * @return true if this trace renders error bars in negative y direction.
   */
  public boolean showsNegativeYErrorBars();

  /**
   * Tests whether error bars in positive x direction are painted by this trace.
   * <p>
   * Returns true if
   * <ul>
   * <li>this trace contains at leaste one {@link IErrorBarPolicy} instance that
   * {@link IErrorBarPolicy#isShowPositiveXErrors()}.</li>
   * <li>and at least one of these instances contains at least one
   * {@link IErrorBarPainter} instance.</li>
   * </ul>
   * <p>
   * 
   * @return true if this trace renders error bars in positive x direction.
   */
  public boolean showsPositiveXErrorBars();

  /**
   * Tests whether error bars in positive y direction are painted by this trace.
   * <p>
   * Returns true if
   * <ul>
   * <li>this trace contains at leaste one {@link IErrorBarPolicy} instance that
   * {@link IErrorBarPolicy#isShowPositiveYErrors()}.</li>
   * <li>and at least one of these instances contains at least one
   * {@link IErrorBarPainter} instance.</li>
   * </ul>
   * <p>
   * 
   * @return true if this trace renders error bars in positive y direction.
   */
  public boolean showsPositiveYErrorBars();

}
