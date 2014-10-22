/*
 * Chart2D, a component for displaying ITrace2D instances. 
 * Copyright (C) 2004 - 2011 Achim Westermann, Achim.Westermann@gmx.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * If you modify or optimize the code in a useful way please let me know. Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart;

import info.monitorenter.gui.chart.ITrace2D.DistancePoint;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.axistickpainters.AxisTickPainterDefault;
import info.monitorenter.gui.chart.events.Chart2DActionPrintSingleton;
import info.monitorenter.util.Range;
import info.monitorenter.util.StringUtil;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.Timer;

/**
 * <code> Chart2D</code> is a component for displaying the data contained in a
 * <code>ITrace2D</code>. It inherits many features from
 * <code>javax.swing.JPanel</code> and allows specific configuration. <br>
 * In order to simplify the use of it, the scaling, labeling and choosing of
 * display- range is done automatically which flattens the free configuration.
 * <p>
 * There are several default settings that may be changed in
 * <code>Chart2D</code><br>
 * <ul>
 * <li>The display range is chosen always big enough to show every
 * <code>TracePoint2D</code> contained in the all <code>ITrace2d</code>
 * instances connected. This is because the
 * {@link info.monitorenter.gui.chart.IAxis} of the chart (for x and y) use by
 * default a
 * {@link info.monitorenter.gui.chart.rangepolicies.RangePolicyUnbounded}. To
 * change this, get the axis of the chart to change (via {@link #getAxisX()},
 * {@link #getAxisY()}) and invoke
 * {@link info.monitorenter.gui.chart.IAxis#setRangePolicy(IRangePolicy)} with
 * the desired view port behavior.
 * <li>During the <code>paint()</code> operation every <code>TracePoint2D</code>
 * is taken from the <code>ITrace2D</code>- instance exactly in the order, it's
 * iterator returns them. From every <code>TracePoint2D</code> then a line is
 * drawn to the next. <br>
 * Unordered traces may cause a weird display. Choose the right implementation
 * of <code>ITrace2D</code> to avoid this. To change this line painting behavior
 * you can use custom renderers at the level of traces via
 * {@link info.monitorenter.gui.chart.ITrace2D#addTracePainter(ITracePainter)}
 * or
 * {@link info.monitorenter.gui.chart.ITrace2D#setTracePainter(ITracePainter)}.
 * <li>If no scaling is chosen, no grids will be painted. See:
 * <code>{@link IAxis#setPaintScale(boolean)}</code> This allows saving of many
 * computations.
 * <li>The distance of the scalepoints is always big enough to display the
 * labels fully without overwriting each other.</li>
 * </ul>
 * <p>
 * <h3>Demo- code:</h3>
 * 
 * <pre>
 * 
 *   ...
 *   Chart2D test = new Chart2D();
 *   JFrame frame = new JFrame(&quot;Chart2D- Debug&quot;);
 *            
 *   frame.setSize(400,200);
 *   frame.setVisible(true);
 *   ITrace2D atrace = new Trace2DLtd(100);
 *   ...
 *   &lt;further configuration of trace&gt;
 *   ...
 *   test.addTrace(atrace);
 *   ....
 *   while(expression){
 *     atrace.addPoint(adouble,bdouble);
 *     ....
 *   }
 * </pre>
 * 
 * <p>
 * <h3>PropertyChangeEvents</h3>
 * {@link java.beans.PropertyChangeListener} instances may be added via
 * {@link javax.swing.JComponent#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
 * . They inherit the properties to listen from
 * {@link java.awt.Container#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
 * . Additionally more <code>PropertyChangeEvents</code> are triggered.
 * <p>
 * As the set of traces inside this class is a collection (and no single
 * property) the {@link java.beans.PropertyChangeEvent} fired for a change of
 * properties property will contain a reference to the <code>Chart2D</code>
 * instance as well as the <code>ITrace2D</code> (if involved in the change). <br>
 * <table width="100%">
 * <tr>
 * <th ><code>getPropertyName()</code></th>
 * <th><code>getSource()</code></th>
 * <th><code>getOldValue()</code></th>
 * <th><code>getNewValue()</code></th>
 * <th>occurrence</th>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_BACKGROUND_COLOR}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link java.awt.Color}</td>
 * <td>{@link java.awt.Color}</td>
 * <td>if a change of the background color occurs.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_X}</td>
 * <td>{@link Chart2D}</td>
 * <td>null</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>if a new axis is added in x dimension ({@link #addAxisXBottom(AAxis)}, @link
 * {@link #addAxisXTop(AAxis)}).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_X}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>null</td>
 * <td>if an axis is removed in x dimension ({@link #removeAxisXBottom(IAxis)},
 * {@link #removeAxisXTop(IAxis)}).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_Y}</td>
 * <td>{@link Chart2D}</td>
 * <td>null</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>if a new axis is added in y dimension ({@link #addAxisYLeft(AAxis)},
 * {@link #addAxisYRight(AAxis)}).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_X_BOTTOM_REPLACE}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>if a axis is replaced in bottom x dimension (
 * {@link #setAxisXBottom(AAxis, int)}).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_X_TOP_REPLACE}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>if a axis is replaced in top x dimension (
 * {@link #setAxisXTop(AAxis, int)}).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_Y_LEFT_REPLACE}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>if a axis is replaced in left y dimension (
 * {@link #setAxisYLeft(AAxis, int)}).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_AXIS_Y_RIGHT_REPLACE}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>{@link info.monitorenter.gui.chart.IAxis}</td>
 * <td>if a axis is replaced in right y dimension (
 * {@link #setAxisYRight(AAxis, int)} ).</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_GRID_COLOR}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link java.awt.Color}</td>
 * <td>{@link java.awt.Color}</td>
 * <td>if a change of the grid color occurs.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_ADD_REMOVE_TRACE}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link ITrace2D}</td>
 * <td>{@link ITrace2D}</td>
 * <td>
 * If a change of the traces occurs. If the old value is null a new trace has
 * been added. If the new value is null, oldvalue trace has been removed. If
 * both are null this is a bug.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_PAINTLABELS}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link java.lang.Boolean}</td>
 * <td>{@link java.lang.Boolean}</td>
 * <td>if a change of the paint labels flag occurs.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_TOOLTIP_TYPE}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link IToolTipType}</td>
 * <td>{@link IToolTipType}</td>
 * <td>if a change of the tool tip type occurs.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_POINT_HIGHLIGHTING_ENABLED}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link Boolean}</td>
 * <td>{@link Boolean}</td>
 * <td>if point highlighting is enabled/disabled.</td>
 * </tr>
 * <tr>
 * <td>{@link ITrace2D#PROPERTY_POINT_HIGHLIGHTERS_CHANGED}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link IPointPainter}</td>
 * <td>null</td>
 * <td>if a point highlighter was added to any of the currently contained
 * traces.</td>
 * </tr>
 * <tr>
 * <td>{@link ITrace2D#PROPERTY_POINT_HIGHLIGHTERS_CHANGED}</td>
 * <td>{@link Chart2D}</td>
 * <td>null</td>
 * <td>{@link IPointPainter}</td>
 * <td>if a point highlighter was removed from any of the currently contained
 * traces.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_ANTIALIASING_ENABLED}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link Boolean}</td>
 * <td>{@link Boolean}</td>
 * <td>if antialiasing is enabled/disabled.</td>
 * </tr>
 * <tr>
 * <td>{@link #PROPERTY_POINTFINDER}</td>
 * <td>{@link Chart2D}</td>
 * <td>{@link IPointFinder}</td>
 * <td>{@link IPointFinder}</td>
 * <td>if {@link Chart2D#setPointFinder(IPointFinder)} caused a change.</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.142.2.1 $
 */

public class Chart2D extends JPanel implements PropertyChangeListener, Iterable<ITrace2D>,
    Printable {
  /**
   * Types of tool tip.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * @version $Revision: 1.142.2.1 $
   */
  public static enum PointFinder implements IPointFinder {
    /**
     * Uses the Manhattan distance to find the nearest point.
     * <p>
     * This implementation is slower than MANHATTAN but has a search field in
     * form of a circle which is more natural for human eyes.
     * 
     * @see Chart2D#getNearestPointEuclid(int, int)
     */
    EUCLID {

      /**
       * @see info.monitorenter.gui.chart.Chart2D.PointFinder#getNearestPoint(java.awt.event.MouseEvent,
       *      info.monitorenter.gui.chart.Chart2D)
       */
      public ITracePoint2D getNearestPoint(final int mouseEventX, final int mouseEventY,
          final Chart2D chart) {
        return chart.getNearestPointEuclid(mouseEventX, mouseEventY);
      }
    },
    /**
     * Uses the Manhattan distance to find the nearest point.
     * <p>
     * This implementation is faster than EUCLID (only
     * subtractions/additions/abs) but has a search field in form of a rhombus
     * which may confuse human eyes.
     * 
     * @see Chart2D#getNearestPointManhattan(int, int)
     */
    MANHATTAN {

      /**
       * @see info.monitorenter.gui.chart.Chart2D.PointFinder#getNearestPoint(java.awt.event.MouseEvent,
       *      info.monitorenter.gui.chart.Chart2D)
       */
      public ITracePoint2D getNearestPoint(final int mouseEventX, final int mouseEventY,
          final Chart2D chart) {
        return chart.getNearestPointManhattan(mouseEventX, mouseEventY);
      }

    };

    /**
     * Default implementation always returns null.
     * <p>
     * 
     * @see info.monitorenter.gui.chart.IPointFinder#getNearestPoint(java.awt.event.MouseEvent,
     *      Chart2D)
     */
    public ITracePoint2D getNearestPoint(final MouseEvent me, final Chart2D chart) {
      return this.getNearestPoint(me.getX(), me.getY(), chart);
    }
  }

  /**
   * Types of tool tip.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * @version $Revision: 1.142.2.1 $
   */
  public static enum ToolTipType implements IToolTipType {
    /**
     * Chart data value tool tips are shown.
     * <p>
     * Note that this implementation only works correctly for one left y axis
     * and one bottom x axis as it does not search for the nearest trace.
     * Displayed values will be formatted according to the formatting of the
     * axes mentioned above.
     * <p>
     */
    DATAVALUES {

      /**
       * @see info.monitorenter.gui.chart.IToolTipType#getDescription()
       */
      @Override
      public String getDescription() {
        return "Value at cursor (rel. to 1st trace)";
      }

      /**
       * @see info.monitorenter.gui.chart.Chart2D.ToolTipType#getToolTipText(java.awt.event.MouseEvent)
       */
      @Override
      public String getToolTipText(final Chart2D chart, final MouseEvent me) {
        String result;
        ITracePoint2D tracePoint = chart.translateMousePosition(me);
        StringBuffer buffer = new StringBuffer("X: ");
        buffer.append(chart.getAxisX().getFormatter().format(tracePoint.getX())).append(" ");
        buffer.append("Y: ");
        buffer.append(chart.getAxisY().getFormatter().format(tracePoint.getY()));
        result = buffer.toString();
        return result;
      }

    },
    /** No tool tips are shown. */
    NONE,
    /** Pixel tool tips are shown (used for debugging). */
    PIXEL {

      /**
       * @see info.monitorenter.gui.chart.IToolTipType#getDescription()
       */
      @Override
      public String getDescription() {
        return "Pixel, not implemented yet";
      }

      /**
       * @see info.monitorenter.gui.chart.Chart2D.ToolTipType#getToolTipText(java.awt.event.MouseEvent)
       */
      @Override
      public String getToolTipText(final Chart2D chart, final MouseEvent me) {
        return "pixel, not implemented yet";
      }

    },
    /**
     * Snaps to the nearest <code>{@link TracePoint2D}</code> and shows it's
     * value.
     * <p>
     * Warning: due to the data structure of multiple axes this is very
     * expensive!
     * <p>
     */
    VALUE_SNAP_TO_TRACEPOINTS {

      /**
       * @see info.monitorenter.gui.chart.IToolTipType#getDescription()
       */
      @Override
      public String getDescription() {
        return "Values, snap to nearest point";
      }

      /**
       * @see info.monitorenter.gui.chart.Chart2D.ToolTipType#getToolTipText(java.awt.event.MouseEvent)
       */
      @Override
      public String getToolTipText(final Chart2D chart, final MouseEvent me) {
        String result;
        ITracePoint2D point = chart.getPointFinder().getNearestPoint(me, chart);
        /*
         * We need the axes of the point for correct formatting (expensive...).
         */
        ITrace2D trace = point.getListener();
        IAxis<?> xAxis = chart.getAxisX(trace);
        IAxis<?> yAxis = chart.getAxisY(trace);

        chart.setRequestedRepaint(true);
        StringBuffer buffer = new StringBuffer("X: ");
        buffer.append(xAxis.getFormatter().format(point.getX())).append(" ");
        buffer.append("Y: ");
        buffer.append(yAxis.getFormatter().format(point.getY()));
        result = buffer.toString();
        return result;
      }

    };

    /**
     * @see info.monitorenter.gui.chart.IToolTipType#getDescription()
     */
    public String getDescription() {
      return "None";
    }

    /**
     * @see info.monitorenter.gui.chart.IToolTipType#getToolTipText(info.monitorenter.gui.chart.Chart2D,
     *      java.awt.event.MouseEvent)
     */
    public String getToolTipText(final Chart2D chart, final MouseEvent me) {
      // NONE implementation (defined by this enum type).
      return null;
    }

  }

  /** Speaking names for axis constants - used for debugging only. */
  public static final String[] AXIX_CONSTANT_NAMES = new String[] {"dummy", "X", "Y", "X,Y" };

  /**
   * Constant describing the bottom side of the chart.
   * <p>
   * 
   * @see IAxis#getAxisPosition()
   */
  public static final int CHART_POSITION_BOTTOM = 32;

  /**
   * Constant describing the left side of the chart.
   * <p>
   * 
   * @see IAxis#getAxisPosition()
   */
  public static final int CHART_POSITION_LEFT = 4;

  /**
   * Constant describing the right side of the chart.
   * <p>
   * 
   * @see IAxis#getAxisPosition()
   */
  public static final int CHART_POSITION_RIGHT = 8;

  /**
   * Constant describing the top side of the chart.
   * <p>
   * 
   * @see IAxis#getAxisPosition()
   */
  public static final int CHART_POSITION_TOP = 16;

  /**
   * A package wide switch for debugging problems with scaling. Set to false the
   * compiler will remove the debugging statements.
   */
  public static final boolean DEBUG_SCALING = false;

  /**
   * A package wide switch for debugging problems with highlighting. Set to
   * false the compiler will remove the debugging statements.
   */
  public static final boolean DEBUG_HIGHLIGHTING = false;

  /**
   * A package wide switch for debugging problems with multithreading. Set to
   * false the compiler will remove the debugging statements.
   */
  public static final boolean DEBUG_THREADING = false;

  /**
   * The bean property <code>constant</code> identifying a change of the
   * antialiasing enabled state.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   */
  public static final String PROPERTY_ANTIALIASING_ENABLED = "Chart2D.PROPERTY_ANTIALIASING_ENABLED";

  /**
   * The bean property <code>constant</code> identifying a change of the
   * internal <code>{@link IAxis}</code> instance for the x dimension.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_AXIS_X = "Chart2D.PROPERTY_AXIS_X";

  /**
   * The bean property <code>constant</code> identifying a replacement of an
   * internal <code>{@link IAxis}</code> instance for the bottom x dimension.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_AXIS_X_BOTTOM_REPLACE = "Chart2D.PROPERTY_AXIS_X_BOTTOM_REPLACE";

  /**
   * The bean property <code>constant</code> identifying a replacement of an
   * internal <code>{@link IAxis}</code> instance for the top x dimension.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_AXIS_X_TOP_REPLACE = "Chart2D.PROPERTY_AXIS_X_TOP_REPLACE";

  /**
   * The bean property <code>constant</code> identifying a change of the
   * internal <code>{@link IAxis}</code> instance for the y dimension.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_AXIS_Y = "Chart2D.PROPERTY_AXIS_Y";

  /**
   * The bean property <code>constant</code> identifying a replacement of an
   * internal <code>{@link IAxis}</code> instance for the left y dimension.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_AXIS_Y_LEFT_REPLACE = "Chart2D.PROPERTY_AXIS_Y_LEFT_REPLACE";

  /**
   * The bean property <code>constant</code> identifying a replacement of an
   * internal <code>{@link IAxis}</code> instance for the right y dimension.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_AXIS_Y_RIGHT_REPLACE = "Chart2D.PROPERTY_AXIS_Y_RIGHT_REPLACE";

  /**
   * The bean property <code>constant</code> identifying a change of the
   * background color. <br>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * The property change events for this change are constructed and fired by the
   * superclass {@link java.awt.Container} so this constant is just for
   * clarification of the String that is related to that property.
   * <p>
   */
  public static final String PROPERTY_BACKGROUND_COLOR = "background";

  /**
   * The bean property <code>constant</code> identifying a change of the font. <br/>
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * The property change events for this change are constructed and fired by the
   * superclass {@link java.awt.Container} so this constant is just for
   * clarification of the String that is related to that property.
   * <p>
   */
  public static final String PROPERTY_FONT = "font";

  /**
   * The bean property <code>constant</code> identifying a change of the
   * foreground color. <br>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * The property change events for this change are constructed and fired by the
   * superclass {@link java.awt.Container} so this constant is just for
   * clarification of the String that is related to that property.
   * <p>
   */
  public static final String PROPERTY_FOREGROUND_COLOR = "foreground";

  /**
   * The bean property <code>constant</code> identifying a change of the grid
   * color.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   */
  public static final String PROPERTY_GRID_COLOR = "Chart2D.PROPERTY_GRID_COLOR";

  /**
   * The bean property <code>constant</code> identifying a change of traces.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   */
  public static final String PROPERTY_ADD_REMOVE_TRACE = IAxis.PROPERTY_ADD_REMOVE_TRACE;

  /**
   * The bean property <code>constant</code> identifying a change of the paint
   * labels flag.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   */
  public static final String PROPERTY_PAINTLABELS = "Chart2D.PROPERTY_PAINTLABELS";

  /**
   * The bean property <code>constant</code> identifying a change of the point
   * highlighting enabled state.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   */
  public static final String PROPERTY_POINT_HIGHLIGHTING_ENABLED = "Chart2D.PROPERTY_POINT_HIGHLIGHTING_ENABLED";

  /**
   * The bean property <code>constant</code> identifying a change of the
   * internal <code>{@link IPointFinder}</code> instance used to find the
   * nearest point corresponding to mouse events over the chart.
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * See the class description for property change events fired.
   * <p>
   */
  public static final String PROPERTY_POINTFINDER = "Chart2D.POINTFINDER";

  /**
   * The bean property <code>constant</code> identifying a change of the tool
   * tip type (<code>{Chart2D{@link #setToolTipType(IToolTipType)}</code>). <br/>
   * <p>
   * Use this constant to register a {@link java.beans.PropertyChangeListener}
   * with the <code>Chart2D</code>.
   * <p>
   * The property change events for this change are constructed and fired by the
   * superclass {@link java.awt.Container} so this constant is just for
   * clarification of the String that is related to that property.
   * <p>
   */
  public static final String PROPERTY_TOOLTIP_TYPE = "Chart2D.PROPERTY_TOOLTIP_TYPE";

  /** Generated <code>serial version UID</code>. */
  private static final long serialVersionUID = 3978425840633852978L;

  /** Constant describing the x axis (needed for scaling). */
  public static final int X = 1;

  /** Constant describing the x and y axis (needed for scaling). */
  public static final int X_Y = 3;

  /** Constant describing the y axis (needed for scaling). */
  public static final int Y = 2;

  /**
   * The bottom x axes of the chart.
   * <p>
   * The first element is always existing and is the downward compatible result
   * of the call <code>{@link Chart2D#getAxisX()}</code>.
   * <p>
   */
  private List<IAxis<?>> m_axesXBottom;

  /**
   * The top x axes of the chart.
   * <p>
   * If empty no top x axes are shown.
   * <p>
   */
  private List<IAxis<?>> m_axesXTop;

  /**
   * The left y axes of the chart.
   * <p>
   * The first element is always existing and is the downward compatible result
   * of the call <code>{@link Chart2D#getAxisY()}</code>.
   * <p>
   */
  private List<IAxis<?>> m_axesYLeft;

  /**
   * The right y axes of the chart.
   * <p>
   * If empty no right y axes are shown.
   * <p>
   */
  private List<IAxis<?>> m_axesYRight;

  /** The internal label painter for this chart. */
  private IAxisTickPainter m_axisTickPainter;

  /** The grid color. */
  private Color m_gridcolor = Color.lightGray;

  /**
   * Chart - wide setting for the ms to give a repaint operation time for
   * collecting several repaint requests into one (performance versus update
   * speed).
   * <p>
   */
  protected int m_minPaintLatency = 50;

  /**
   * The axis that is used for translation from mouse event to x value by method
   * <code>Chart2D{@link #translateMousePosition(MouseEvent)}</code>.
   * <p>
   * Defaults to the first bottom x axis.
   * <p>
   */
  private AAxis<?> m_mouseTranslationXAxis;

  /**
   * The axis that is used for translation from mouse event to y value by method
   * <code>Chart2D{@link #translateMousePosition(MouseEvent)}</code>.
   * <p>
   * Defaults to the first left y axis.
   * <p>
   */
  private AAxis<?> m_mouseTranslationYAxis;

  /**
   * When not null this format will be used within paint: then we deal with a
   * printing request.
   */
  private transient PageFormat m_pageFormat;

  /**
   * Flag that decides whether labels for traces are painted below the chart.
   */
  private boolean m_paintLabels = true;

  /**
   * The point finder used to find the nearest point corresponding to a mouse
   * event.
   */
  private IPointFinder m_pointFinder = PointFinder.EUCLID;

  /**
   * Used to track mouse motion events and highlight the nearest trace point
   * according to the point highlighters in the corresponding trace (
   * <code>{@link ITrace2D#getPointHighlighters()}</code>).
   * <p>
   * Also removes highlighters (potentially if they are exclusive) from the
   * previous highlighted point.
   * <p>
   */
  private final PointHighlighter m_pointHighlightListener = new PointHighlighter();

  /**
   * Tracks mouse motion events and highlights the nearest point in the trace.
   * <p>
   * 
   * @author <a href="mailto:achim.westermann@gmx.de">Achim Westermann</a>
   */
  final class PointHighlighter extends MouseMotionAdapter implements MouseMotionListener,
      PropertyChangeListener {

    /**
     * Default constructor, adds property change listener for point highlighters
     * to the enclosing chart.
     * <p>
     */
    public PointHighlighter() {
      /*
       * Need an identity hash map as the trace keys will change upon points
       * added thus making them "unfindable" in a map based on hashcode!
       */
      this.m_previousHighlighted = new IdentityHashMap<ITrace2D, ITracePoint2D>();
      Chart2D.this.addPropertyChangeListener(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, this);
      Chart2D.this.addPropertyChangeListener(Chart2D.PROPERTY_ADD_REMOVE_TRACE, this);

    }

    /** Needed to de-highlight previously highlighted points. */
    private Map<ITrace2D, ITracePoint2D> m_previousHighlighted;

    /**
     * Activates or deactivates point higlighting.
     * <p>
     * This handles tracking of mouse motion events of the chart.
     * <p>
     * 
     * @param onoff
     *          if true, highlighting will be enabled, if false it will be
     *          deactivated.
     * 
     * @return true if a change took place.
     */
    @SuppressWarnings("synthetic-access")
    public boolean setActive(boolean onoff) {
      boolean result = false;
      boolean isEnabled = Chart2D.this.isEnabledPointHighlighting();

      if (!onoff) {
        if (isEnabled) {
          synchronized (Chart2D.this) {
            // deactivate all previously highlighted traces:
            for (Map.Entry<ITrace2D, ITracePoint2D> entry : this.m_previousHighlighted.entrySet()) {
              synchronized (entry.getKey()) {
                Set<IPointPainter< ? >> highlighters = entry.getKey().getPointHighlighters();
                Set<IPointPainter< ? >> additionalPainters = entry.getValue()
                    .getAdditionalPointPainters();
                Iterator<IPointPainter< ? >> itAdditionasPainters = additionalPainters.iterator();
                while (itAdditionasPainters.hasNext()) {
                  IPointPainter< ? > assignedHighlighter = itAdditionasPainters.next();
                  for (IPointPainter< ? > highlighter : highlighters) {
                    // cannot check for equality as outside configured same
                    // additional point painters not working for highlighting
                    // could be erased.
                    if (assignedHighlighter == highlighter) {
                      itAdditionasPainters.remove();
                      break;
                    }
                  }
                }

              }
            }
            Chart2D.this.removeMouseMotionListener(this);
            Chart2D.this.firePropertyChange(PROPERTY_POINT_HIGHLIGHTING_ENABLED, Boolean.TRUE,
                Boolean.FALSE);
            this.m_previousHighlighted.clear();
            Chart2D.this.setRequestedRepaint(true);
            result = true;
          }
        }

      } else {
        if (!isEnabled) {
          synchronized (Chart2D.this) {
            Chart2D.this.addMouseMotionListener(this);
            Chart2D.this.firePropertyChange(PROPERTY_POINT_HIGHLIGHTING_ENABLED, Boolean.FALSE,
                Boolean.TRUE);
            Chart2D.this.setRequestedRepaint(true);
            result = true;
          }
        }
      }
      return result;
    }

    /**
     * Attaches the highlighters of the trace of the point to the point: it will
     * be highlighted then.
     * <p>
     * 
     * @param point
     *          the point to highlight.
     */
    private void attachHighlighters(ITracePoint2D point) {

      ITrace2D trace = point.getListener();
      for (IPointPainter< ? > highlighter : trace.getPointHighlighters()) {
        point.addAdditionalPointPainter(highlighter);
      }
    }

    /**
     * Removes the exclusive point highlighters from the old highlighted point.
     */
    private void clearOutdatedHighlighters(final ITrace2D trace) {

      ITracePoint2D previousHighlightedPoint = this.m_previousHighlighted.remove(trace);
      if (previousHighlightedPoint != null) {
        Iterator<IPointPainter< ? >> itAdditionaPainters = previousHighlightedPoint
            .getAdditionalPointPainters().iterator();
        IPointPainter< ? > additionalPainter;
        Set<IPointPainter< ? >> highlighters = trace.getPointHighlighters();
        if (Chart2D.DEBUG_HIGHLIGHTING) {
          System.err.println("Trace " + trace.getName() + " has highlighters " + highlighters);
          ITrace2D prevHiTrace = previousHighlightedPoint.getListener();
          if (prevHiTrace != null) {
            System.err.println("Previously highlighted trace: " + prevHiTrace.getName());
          }
        }
        while (itAdditionaPainters.hasNext()) {
          additionalPainter = itAdditionaPainters.next();
          for (IPointPainter< ? > highlighter : highlighters) {
            /*
             * Cannot rely on "contains" method as comparable/equals might judge
             * an equal highlighter added externally via api as a highlighter
             */
            if (highlighter == additionalPainter) {
              itAdditionaPainters.remove();
              break;
            } else {
              if (Chart2D.DEBUG_HIGHLIGHTING) {
                System.err.println("Additional painter " + additionalPainter + " and highlighter "
                    + highlighter + " of trace " + trace + " judged not as same.");
              }
            }
          }
        }
      } else {
        if (Chart2D.DEBUG_HIGHLIGHTING) {
          System.err.println("No previous point highlighted in trace " + trace.getName());
        }
      }
    }

    /**
     * Attaches highlighters of the trace of the point next to the cursor and
     * removes highlighters from the previous highlighted point.
     * <p>
     * 
     * TODO: Peek at getToolTipText invocations: Save operations in case the
     * mouse has not moved much?
     * 
     * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
      ITracePoint2D point = Chart2D.this.getPointFinder().getNearestPoint(e, Chart2D.this);
      // don't work on empty charts:
      if (point != null) {
        ITracePoint2D previousHighlightedPoint = this.m_previousHighlighted
            .get(point.getListener());
        if (!point.equals(previousHighlightedPoint)) {
          ITrace2D trace = point.getListener();
          // avoid duplicate or no highlighting in concurrent paint situation.
          synchronized (this) {
            synchronized (trace) {
              this.clearOutdatedHighlighters(trace);
              this.attachHighlighters(point);
              this.m_previousHighlighted.put(trace, point);
              Chart2D.this.setRequestedRepaint(true);
            }
          }
        }
      }
    }

    /**
     * This is needed in case the point Highlighters of a trace (
     * <code>{@link ITrace2D#setPointHighlighter(IPointPainter)}</code>,
     * <code>{@link ITrace2D#addPointHighlighter(IPointPainter)}</code> or
     * <code>{@link ITrace2D#removePointHighlighter(IPointPainter)}</code> or
     * <code>{@link ITrace2D#removeAllPointHighlighters()}</code>) are changed
     * in order to re-attach the proper point highlighters to the highlighted
     * points.
     * <p>
     * Also a hook is installed for
     * <code>{@link Chart2D#PROPERTY_ADD_REMOVE_TRACE}</code> to clear out a
     * reference to a highlighted trace to make it garbage-collectable in case
     * the trace is removed.
     * <p>
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     * @see ITrace2D#addPointHighlighter(IPointPainter)
     * @see ITrace2D#setPointHighlighter(IPointPainter)
     * @see ITrace2D#removePointHighlighter(IPointPainter)
     * @see ITrace2D#removeAllPointHighlighters()
     */
    public void propertyChange(PropertyChangeEvent evt) {

      String property = evt.getPropertyName();
      if (ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED.equals(property)) {
        ITrace2D trace = (ITrace2D) evt.getSource();
        ITracePoint2D point = this.m_previousHighlighted.get(trace);
        if (point != null) {
          if ((evt.getNewValue() != null) && (evt.getOldValue() == null)) {
            if (this.m_previousHighlighted != null) {
              synchronized (Chart2D.this) {
                synchronized (trace) {
                  point.addAdditionalPointPainter((IPointPainter< ? >) evt.getNewValue());
                  Chart2D.this.setRequestedRepaint(true);
                }
              }
            }
          } else if ((evt.getNewValue() == null) && (evt.getOldValue() != null)) {
            if (this.m_previousHighlighted != null) {
              synchronized (Chart2D.this) {
                synchronized (trace) {
                  point.removeAdditionalPointPainter((IPointPainter< ? >) evt.getOldValue());
                  Chart2D.this.setRequestedRepaint(true);
                }
              }
            }

          } else {
            throw new RuntimeException(
                "Programming error. Unneccessary event caught: "
                    + evt
                    + ". You only have to fire this event, if a point highlighter was addded or removed.");
          }
        }
      } else if (Chart2D.PROPERTY_ADD_REMOVE_TRACE.equals(property)) {
        ITrace2D oldTrace2d = (ITrace2D) evt.getOldValue();
        if (evt.getNewValue() == null) {
          // trace was removed, so remove my potential reference:
          this.m_previousHighlighted.remove(oldTrace2d);
        }
      } else {
        throw new RuntimeException("Programming error: " + this.getClass().getName()
            + " only has to be registered to the event "
            + ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED + " on instances of type (or subtype) "
            + ITrace2D.class.getName());
      }
    }
  }

  /**
   * Internal timer for repaint control with guarantee that the interval between
   * two frames will not be lower than <code>{@link Chart2D#m_minPaintLatency}
   * </code> ms.
   * <p>
   */
  private Timer m_repainter;

  /**
   * Internal flag that stores a request for a repaint that guarantees that two
   * invocations of <code></code> will always have at least have an interval of
   * <code>{@link Chart2D#m_minPaintLatency}</code> ms.
   * <p>
   * Access to it has to be synchronized!
   */
  private boolean m_requestedRepaint;

  /**
   * Flag to remember whether this chart has synchronized it's x start
   * coordinates with another chart.
   */
  private boolean m_synchronizedXStart = false;

  /** A chart this chart will synchronize it's start coordinates in x dimension. */
  private Chart2D m_synchronizedXStartChart;

  /** Flag for showing coordinates as tool tips. */
  private IToolTipType m_toolTip = ToolTipType.NONE;

  /**
   * Internal counter for all point highlighters of all traces to allow
   * automatic enablement/disablement of highlighting (performance).
   */
  private int m_traceHighlighterCount = 0;

  /** Used to create trace point instances. */
  private ITracePointProvider m_tracePointProvider;

  /**
   * Boolean flag to turn on antialiasing.
   */
  private boolean m_useAntialiasing = false;

  /**
   * The end x pixel coordinate of the chart.
   */
  private int m_xChartEnd;

  /**
   * The start x coordinate of the chart.
   */
  private int m_xChartStart;

  /**
   * The y coordinate of the upper edge of the chart's display area in px.
   * <p>
   * The px coordinates in awt / swing start from top and increase towards the
   * bottom.
   * <p>
   */
  private int m_yChartEnd;

  /**
   * The start y coordinate of the chart.
   */
  private int m_yChartStart;

  /**
   * Creates a new chart.
   * <p>
   */
  public Chart2D() {

    // initialize the axis collections:
    this.m_axesXBottom = new LinkedList<IAxis<?>>();
    this.m_axesXTop = new LinkedList<IAxis<?>>();
    this.m_axesYLeft = new LinkedList<IAxis<?>>();
    this.m_axesYRight = new LinkedList<IAxis<?>>();

    this.setTracePointProvider(new TracePointProviderDefault());

    AAxis<?> axisX = new AxisLinear<IAxisScalePolicy>();
    this.setAxisXBottom(axisX, 0);
    axisX.getAxisTitle().setTitle("X");

    AAxis<?> axisY = new AxisLinear<IAxisScalePolicy>();
    this.setAxisYLeft(axisY, 0);
    axisY.getAxisTitle().setTitle("Y");

    this.setAxisTickPainter(new AxisTickPainterDefault());

    Font dflt = this.getFont();
    if (dflt != null) {
      this.setFont(new Font(dflt.getFontName(), dflt.getStyle(), 10));
    }
    this.getBackground();
    this.setBackground(Color.white);
    // turn off tool tips by default (performance):
    this.setToolTipType(Chart2D.ToolTipType.NONE);

    // one initial call to paint for side effect computations
    // potentially needed from outside (m_XstartChart...):
    this.setRequestedRepaint(true);

    // set a custom cursor:
    this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

    this.m_repainter = new Timer(this.m_minPaintLatency, new ActionListener() {

      /**
       * Repaints the Chart if dirty.
       * <p>
       * 
       * @param e
       *          invoked by the timer to trigger the action.
       */
      public void actionPerformed(final ActionEvent e) {
        synchronized (Chart2D.this) {
          if (Chart2D.this.isRequestedRepaint()) {
            if (Chart2D.DEBUG_THREADING) {
              System.out.println(Thread.currentThread().getName() + " triggering repaint()");
            }
            // Only here this deprecated call may be done:
            Chart2D.this.repaint(Chart2D.this.m_minPaintLatency);
            Chart2D.this.setRequestedRepaint(false);
          }
        }
      }

    });
    Timer.setLogTimers(false);
    this.m_repainter.setRepeats(true);
    this.m_repainter.setCoalesce(true);
    this.m_repainter.start();
  }

  /**
   * Adds the given x axis to the list of internal bottom x axes.
   * <p>
   * The given axis must not be contained before (e.g. as right y axis or bottom
   * x axis...).
   * <p>
   * 
   * @param axisX
   *          the additional bottom x axis.
   */
  public void addAxisXBottom(final AAxis<?> axisX) {
    this.ensureUniqueAxis(axisX);
    this.m_axesXBottom.add(axisX);
    axisX.setChart(this, Chart2D.X, Chart2D.CHART_POSITION_BOTTOM);

    this.listenToAxis(axisX);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_X, null, axisX);
    this.setRequestedRepaint(true);
  }

  /**
   * Adds the given x axis to the list of internal top x axes.
   * <p>
   * The given axis must not be contained before (e.g. as right y axis or bottom
   * x axis...).
   * <p>
   * 
   * @param axisX
   *          the additional top x axis.
   */
  public void addAxisXTop(final AAxis<?> axisX) {
    this.ensureUniqueAxis(axisX);
    this.m_axesXTop.add(axisX);
    axisX.setChart(this, Chart2D.X, Chart2D.CHART_POSITION_TOP);

    this.listenToAxis(axisX);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_X, null, axisX);
    this.setRequestedRepaint(true);
  }

  /**
   * Adds the given y axis to the list of internal left y axes.
   * <p>
   * The given axis must not be contained before (e.g. as right y axis or bottom
   * x axis...).
   * <p>
   * 
   * @param axisY
   *          the additional left y axis.
   */
  public void addAxisYLeft(final AAxis<?> axisY) {
    this.ensureUniqueAxis(axisY);
    this.m_axesYLeft.add(axisY);
    axisY.setChart(this, Chart2D.Y, Chart2D.CHART_POSITION_LEFT);

    this.listenToAxis(axisY);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_Y, null, axisY);
    this.setRequestedRepaint(true);
  }

  /**
   * Adds the given y axis to the list of internal right y axes.
   * <p>
   * The given axis must not be contained before (e.g. as right y axis or bottom
   * x axis...).
   * <p>
   * 
   * @param axisY
   *          the additional right y axis.
   */
  public void addAxisYRight(final AAxis<?> axisY) {
    this.ensureUniqueAxis(axisY);
    this.m_axesYRight.add(axisY);
    axisY.setChart(this, Chart2D.Y, Chart2D.CHART_POSITION_RIGHT);

    this.listenToAxis(axisY);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_Y, null, axisY);
    this.setRequestedRepaint(true);
  }

  /**
   * Convenience method that adds the trace to this chart with relation to the
   * first bottom x axis and the first left y axis. It will be painted (if it's
   * {@link ITrace2D#isVisible()} returns true) in this chart.
   * <p>
   * This method will trigger a {@link java.beans.PropertyChangeEvent} being
   * fired on all instances registered by
   * {@link javax.swing.JComponent#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
   * (registered with <code>String</code> argument
   * {@link IAxis#PROPERTY_ADD_REMOVE_TRACE}) on the internal bottom x axis and
   * left y axis.
   * <p>
   * 
   * @param points
   *          the trace to add.
   * @see IAxis#PROPERTY_ADD_REMOVE_TRACE
   * @see Chart2D#addTrace(ITrace2D, IAxis, IAxis)
   */
  public final void addTrace(final ITrace2D points) {
    IAxis<?> xAxis = this.m_axesXBottom.get(0);
    IAxis<?> yAxis = this.m_axesYLeft.get(0);
    this.addTrace(points, xAxis, yAxis);
  }

  /**
   * Adds the trace to this chart with relation to the given x axis and y axis.
   * It will be painted (if it's {@link ITrace2D#isVisible()} returns true) in
   * this chart.
   * <p>
   * This method will trigger a {@link java.beans.PropertyChangeEvent} being
   * fired on all instances registered by
   * {@link javax.swing.JComponent#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
   * (registered with <code>String</code> argument
   * {@link IAxis#PROPERTY_ADD_REMOVE_TRACE}) on the axis of this chart.
   * <p>
   * The given x and y axis will be responsible for computation of the scale of
   * this trace.
   * <p>
   * 
   * @param points
   *          the trace to add.
   * @param xAxis
   *          the x axis responsible for the scale of this trace - it has to be
   *          contained in this chart or an exception will be thrown.
   * @param yAxis
   *          the y axis responsible for the scale of this trace - it has to be
   *          contained in this chart or an exception will be thrown.
   * 
   * @see IAxis#PROPERTY_ADD_REMOVE_TRACE
   */
  public final void addTrace(final ITrace2D points, final IAxis<?> xAxis, final IAxis<?> yAxis) {
    if (!this.m_axesXBottom.contains(xAxis)) {
      if (!this.m_axesXTop.contains(xAxis)) {
        throw new IllegalArgumentException(
            "Given x axis ("
                + xAxis.getAxisTitle().getTitle()
                + ") has to be added to this chart first (via setAxisX(AAxis) or addAxisXBottom(AAXis) or addAxisXTop(AAXis)).");
      }
    }
    if (!this.m_axesYLeft.contains(yAxis)) {
      if (!this.m_axesYRight.contains(yAxis)) {
        throw new IllegalArgumentException(
            "Given y axis ("
                + yAxis.getAxisTitle().getTitle()
                + ") has to be added to this chart first (via setAxisY(AAxis) or addAxisYLeft(AAXis) or addAxisYRight(AAXis)).");
      }
    }
    /*
     * 
     * This lock is needed to ensure a lock on the tree is acquired before a
     * lock to the chart. Method paint will also first acquire the treeLock and
     * then has to get the lock on the chart while this code path might descend
     * into ChartPanel where Container.getComponents() (in method
     * containsTraceLabel()) will require the tree lock after having already
     * acquired the chart lock -> deadlock between paint thread and addTrace
     * thread.
     */
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName()
          + ", addTrace(ITrace2D, XAxis, YAxis): 0 locks.");
    }
    synchronized (this.getTreeLock()) {
      /*
       * synchronization necessary to avoid that a highlighter is added to the
       * trace while counting them.
       */
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName()
            + ", addTrace(ITrace2D, XAxis, YAxis): 1 locks.");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println(Thread.currentThread().getName()
              + ", addTrace(ITrace2D, XAxis, YAxis): 2 locks.");
        }
        synchronized (points) {
          if (Chart2D.DEBUG_THREADING) {
            System.out.println(Thread.currentThread().getName()
                + ", addTrace(ITrace2D, XAxis, YAxis): 3 locks.");
          }
          boolean success = false;
          success |= xAxis.addTrace(points);
          success |= yAxis.addTrace(points);
          if (success) {
            this.listenToTrace(points);
            int amountOfHighlighters = points.getPointHighlighters().size();
            this.trackHighlightingEnablement(amountOfHighlighters);
            this.firePropertyChange(IAxis.PROPERTY_ADD_REMOVE_TRACE, null, points);
          }
        }
        if (Chart2D.DEBUG_THREADING) {
          System.out.println(Thread.currentThread().getName()
              + ", addTrace(ITrace2D, XAxis, YAxis): dropped 1 lock, 2 locks remaining.");
        }

      }
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName()
            + ", addTrace(ITrace2D, XAxis, YAxis): dropped 1 lock, 1 locks remaining.");
      }
    }
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName()
          + ", addTrace(ITrace2D, XAxis, YAxis): dropped 1 lock, 0 locks remaining.");
    }
  }

  /**
   * Calculates the end x coordinate (right bound) in pixel of the chart to
   * draw.
   * <p>
   * As a side effect the {@link IAxis#setPixelXLeft(int)} is set here.
   * <p>
   * 
   * This value depends on the current <code>{@link FontMetrics}</code> used to
   * paint the x labels and the maximum amount of characters that are used for
   * the x labels (<code>{@link IAxisLabelFormatter#getMaxAmountChars()}</code>)
   * because an x label may occur on the right edge of the chart and should not
   * be clipped.
   * <p>
   * 
   * @param g2d
   *          needed for size informations.
   * @return the end x coordinate (right bound) in pixel of the chart to draw.
   */
  private int calculateXChartEnd(final Graphics g2d) {
    int result;
    result = (int) this.getSize().getWidth();

    IAxis<?> currentAxis;
    int axisWidth = 0;
    if (this.m_axesYRight.size() > 0) {
      ListIterator<IAxis<?>> it = this.m_axesYRight.listIterator(this.m_axesYRight.size());
      while (it.hasPrevious()) {
        currentAxis = it.previous();
        axisWidth = currentAxis.getWidth(g2d);
        currentAxis.setPixelXRight(result);
        if (currentAxis.isVisible()) {
          result = result - axisWidth;
        }
        currentAxis.setPixelXLeft(result);
      }
      if (result == this.getSize().getWidth()) {
        // ensure a minimum offset for when no axes are present
        result -= 20;
      }
    } else {
      // use the maximum label width of the x axes to avoid x labels
      // being clipped in case there are no right y axes:
      Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
      int xAxesMaxLabelWidth = 0;
      int tmp;
      while (it.hasNext()) {
        currentAxis = it.next();
        tmp = currentAxis.getWidth(g2d);
        if (tmp > xAxesMaxLabelWidth) {
          xAxesMaxLabelWidth = tmp;
        }
      }
      it = this.m_axesXTop.iterator();
      while (it.hasNext()) {
        currentAxis = it.next();
        tmp = currentAxis.getWidth(g2d);
        if (tmp > xAxesMaxLabelWidth) {
          xAxesMaxLabelWidth = tmp;
        }
      }
      result = result - xAxesMaxLabelWidth;
    }

    return result;
  }

  /**
   * Calculates the start x coordinate (left bound) in pixel of the chart to
   * draw.
   * <p>
   * As a side effect the {@link IAxis#setPixelXRight(int)} is set here.
   * <p>
   * This value depends on the current <code>{@link FontMetrics}</code> used to
   * paint the y labels and the maximum amount of characters that are used for
   * the y labels (<code>{@link IAxisLabelFormatter#getMaxAmountChars()}</code>
   * ).
   * <p>
   * 
   * @param g2d
   *          needed for size information.
   *          
   * @return the start x coordinate (left bound) in pixel of the chart to draw.
   */
  private int calculateXChartStart(final Graphics g2d) {
    int result = 0;
    // reverse iteration because the most left axes are the latter ones:
    ListIterator<IAxis<?>> it = this.m_axesYLeft.listIterator(this.m_axesYLeft.size());
    IAxis<?> currentAxis;
    while (it.hasPrevious()) {
      currentAxis = it.previous();
      currentAxis.setPixelXLeft(result);
      if (currentAxis.isVisible()) {
        result += currentAxis.getWidth(g2d);
      }
      currentAxis.setPixelXRight(result);
    }

    // ensure a minimum offset for e.g. when no Y axes are visible
    return result > 0 ? result : 20;
  }
  
  /**
   * Installs the offset the the left y-axes in case this chart 
   * is stacked / synchronized vertically via {@link #setSynchronizedXStartChart(Chart2D)}.<p>
   * 
   * This is only necessary for the charts whose left y axes should start more to the right than if they were 
   * all on their own and didn't have to care for other charts.
   * <p>
   * 
   * @param g2d
   *          needed for size information.
   *          
   * @return the start x coordinate (left bound) in pixel of the chart to draw.
   */
  private int installXAxisLeftOffset(final Graphics g2d, final int offset) {
    int result = offset;
    // reverse iteration because the most left axes are the latter ones:
    ListIterator<IAxis<?>> it = this.m_axesYLeft.listIterator(this.m_axesYLeft.size());
    IAxis<?> currentAxis;
    while (it.hasPrevious()) {
      currentAxis = it.previous();
      currentAxis.setPixelXLeft(result);
      if (currentAxis.isVisible()) {
        result += currentAxis.getWidth(g2d);
      }
      currentAxis.setPixelXRight(result);
    }

    // ensure a minimum offset for e.g. when no Y axes are visible
    return result > 0 ? result : 20;
  }

  /**
   * Calculates the end y coordinate (upper bound) in pixel of the chart to
   * draw.
   * <p>
   * Note that y coordinates are related to the top of a frame, so a higher y
   * value marks a visual lower chart value.
   * <p>
   * As a side effect the {@link IAxis#setPixelYBottom(int)} is set here.
   * <p>
   * The value computed here is the maximum overhang of all y axes caused by
   * their font height of their labels or the summation of all top x axis
   * heights (if this is greater) .
   * <p>
   * 
   * @param g2d
   *          needed for size informations.
   * @return the end y coordinate (upper bound) in pixel of the chart to draw.
   */
  private int calculateYChartEnd(final Graphics g2d) {
    IAxis<?> currentAxis;
    int tmp;
    int result = 0;
    int maxAxisYHeight = 0;
    int axesXTopHeight = 0;
    /*
     * 1) Find the max y axis height (this is just the overhang cause by label.
     * 
     * TODO: maybe this is too much work in case all fonts of all axes are the
     * same and every axis will give the same result for the height (debug).
     */
    Iterator<IAxis<?>> it = this.m_axesYLeft.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      tmp = currentAxis.getHeight(g2d);
      if (currentAxis.isVisible() && tmp > maxAxisYHeight) {
        maxAxisYHeight = tmp;
      }
    }
    it = this.m_axesYRight.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      tmp = currentAxis.getHeight(g2d);
      if (currentAxis.isVisible() && tmp > maxAxisYHeight) {
        maxAxisYHeight = tmp;
      }
    }

    // 2) Find the total height of top x axes
    // (and calculate the y pixel of the top axes):
    ListIterator<IAxis<?>> listIt = this.m_axesXTop.listIterator(this.m_axesXTop.size());
    int axisHeight = 0;
    while (listIt.hasPrevious()) {
      currentAxis = listIt.previous();
      currentAxis.setPixelYTop(axesXTopHeight);
      axisHeight = currentAxis.getHeight(g2d);
      if (currentAxis.isVisible()) {
        axesXTopHeight += axisHeight;
      }
      currentAxis.setPixelYBottom(axesXTopHeight);
    }

    // 3) Find the maximum result:
    result = Math.max(maxAxisYHeight, axesXTopHeight);
    // ensure minimum offset when no axes are visible
    return result > 0 ? result : 20;
  }

  /**
   * Calculates the start y coordinate (lower bound) in pixel of the chart to
   * draw.
   * <p>
   * As a side effect the {@link IAxis#setPixelYBottom(int)} is set here.
   * <p>
   * Note that y coordinates are related to the top of a frame, so a higher y
   * value marks a visual lower chart value.
   * <p>
   * 
   * @param g2d
   *          needed for size informations.
   * @param labelHeight
   *          the height of the labels below the chart.
   * @return the start y coordinate (lower bound) in pixel of the chart to draw.
   */
  private int calculateYChartStart(final Graphics g2d, final int labelHeight) {
    int result;
    result = (int) this.getSize().getHeight();
    result = result - labelHeight;
    IAxis<?> currentAxis;
    int axesXBottomHeight = 0;
    Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      currentAxis.setPixelYBottom(result);
      if (currentAxis.isVisible()) {
        result -= currentAxis.getHeight(g2d);
      }
      currentAxis.setPixelYTop(result);
    }

    result = result - axesXBottomHeight;
    if (result == this.getSize().getHeight()) {
      // ensure minimum offset when no axis are visible
      result -= 20;
    }
    return result;

  }

  /**
   * @see javax.swing.JComponent#createToolTip()
   */
  @Override
  public JToolTip createToolTip() {
    /*
     * If desired return here a HTMLToolTip that transforms the text given to
     * setTipText into a View (with BasicHTML) and sets it as the
     * putClientProperty BasicHtml.html of itself.
     */
    JToolTip result = super.createToolTip();
    return result;
  }

  /**
   * Destroys the chart.
   * <p>
   * This method is only of interest if you have an application that dynamically
   * adds and removes charts. So if you use the same Chart2D object(s) during
   * the applications lifetime there is no need to use this method.
   * <p>
   */
  public void destroy() {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("destroy, 0 locks");
    }
    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("destroy, 1 lock");
      }
      this.m_axesXBottom.clear();
      this.m_axesXBottom = null;
      this.m_axesXTop.clear();
      this.m_axesXTop = null;

      this.m_axesYLeft.clear();
      this.m_axesYLeft = null;
      this.m_axesYRight.clear();
      this.m_axesYRight = null;

      // terminate the timer
      this.m_repainter.stop();

    }
  }

  /**
   * Switches point highlighting on or off depending on the given argument.
   * <p>
   * Turning off this removes a <code>{@link MouseMotionListener}</code> and
   * therefore avoids receiving a lot of mouse events which will result in
   * potentially very expensive (many traces with many points) computations to
   * find the nearest point corresponding to the mouse pointer. So dropping the
   * point highlighting feature may result in much better real time performance.
   * <p>
   * 
   * Keep in mind that the view part of point highlighting is configured by
   * configuring the point highlighters for your traces:
   * <code>{@link ITrace2D#addPointHighlighter(IPointPainter)}</code>.
   * <p>
   * 
   * This method might trigger a {@link java.beans.PropertyChangeEvent} being
   * fired on all instances registered by
   * {@link javax.swing.JComponent#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
   * (registered with <code>String</code> argument
   * {@link Chart2D#PROPERTY_POINT_HIGHLIGHTING_ENABLED}).
   * <p>
   * 
   * @see ITrace2D#addPointHighlighter(IPointPainter)
   * 
   * @see Chart2D#isEnabledPointHighlighting()
   * 
   * @param onoff
   *          if true the closest point to the cursor will be highlighted, if
   *          false you might gain performance by having this feature turned
   *          off!
   * 
   * @return true if a change of this state did take place (you did not call
   *         this at least twice with the same argument).
   */
  public boolean enablePointHighlighting(final boolean onoff) {
    boolean result;
    result = this.m_pointHighlightListener.setActive(onoff);
    return result;
  }

  /**
   * Ensures that the axis to add is not in duty in any axis function for this
   * chart.
   * <p>
   * 
   * @param axisToAdd
   *          the axis to test.
   */
  private void ensureUniqueAxis(final IAxis<?> axisToAdd) {
    if (this.m_axesXBottom.contains(axisToAdd)) {
      throw new IllegalArgumentException("Given axis (" + axisToAdd.getAxisTitle().getTitle()
          + " is already configured as bottom x axis!");
    }
    if (this.m_axesXTop.contains(axisToAdd)) {
      throw new IllegalArgumentException("Given axis (" + axisToAdd.getAxisTitle().getTitle()
          + " is already configured as top x axis!");
    }
    if (this.m_axesYLeft.contains(axisToAdd)) {
      throw new IllegalArgumentException("Given axis (" + axisToAdd.getAxisTitle().getTitle()
          + " is already configured as left y axis!");
    }
    if (this.m_axesYRight.contains(axisToAdd)) {
      throw new IllegalArgumentException("Given axis (" + axisToAdd.getAxisTitle().getTitle()
          + " is already configured as right y axis!");
    }

  }

  /**
   * Cleanup when this instance is dropped.
   * <p>
   * 
   * @see java.lang.Object#finalize()
   * 
   * @throws Throwable
   *           if a finalizer of a superclass fails.
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.destroy();
  }

  /**
   * Returns an array with the x (position 0) and the y axis (position 1) of the
   * given trace if it is correctly set up. If the given trace is not set up
   * correctly with this chart a missing axis in one dimension will be reflected
   * in <code>null</code> on the corresponding position
   * <p>
   * 
   * @param trace
   *          the trace to find the axes of.
   * @return an array with the x (position 0) and the y axis (position 1) of the
   *         given trace if it is correctly set up.
   */
  public IAxis<?>[] findAxesOfTrace(final ITrace2D trace) {
    IAxis<?>[] result = new IAxis[2];
    // 1) find x axis:
    IAxis<?> xAxis = null;
    for (IAxis<?> axis : this.m_axesXBottom) {
      if (axis.getTraces().contains(trace)) {
        xAxis = axis;
        break;
      }
    }
    if (xAxis == null) {
      for (IAxis<?> axis : this.m_axesXTop) {
        if (axis.getTraces().contains(trace)) {
          xAxis = axis;
          break;
        }
      }
    }
    // 2) find y axis:
    IAxis<?> yAxis = null;
    for (IAxis<?> axis : this.m_axesYLeft) {
      if (axis.getTraces().contains(trace)) {
        yAxis = axis;
        break;
      }
    }
    if (yAxis == null) {
      for (IAxis<?> axis : this.m_axesYRight) {
        if (axis.getTraces().contains(trace)) {
          yAxis = axis;
          break;
        }
      }
    }
    result[0] = xAxis;
    result[1] = yAxis;
    return result;
  }

  /**
   * Returns the <code>{@link List}&lt;{@link IAxis}&gt;</code> with all axes of the chart.
   * <p>
   * 
   * @return the <code>{@link List}&lt;{@link IAxis}&gt;</code> with all axes of
   *         the chart
   */
  public final List<IAxis<?>> getAxes() {
    List<IAxis<?>> result = new LinkedList<IAxis<?>>();
    result.addAll(this.getAxesXBottom());
    result.addAll(this.getAxesXTop());
    result.addAll(this.getAxesYLeft());
    result.addAll(this.getAxesYRight());
    return result;
  }

  /**
   * Returns the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances that are
   * painted in x dimension on the bottom of the chart.
   * <p>
   * <b>Caution!</b> The original list is returned so modifications of it will
   * cause unpredictable side effects.
   * <p>
   * 
   * @return the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances
   *         that are painted in x dimension on the bottom of the chart.
   */
  public final List<IAxis<?>> getAxesXBottom() {
    return this.m_axesXBottom;
  }

  /**
   * Returns the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances that are
   * painted in x dimension on top of the chart.
   * <p>
   * <b>Caution!</b> The original list is returned so modifications of it will
   * cause unpredictable side effects.
   * <p>
   * 
   * @return the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances
   *         that are painted in x dimension on top of the chart.
   */
  public final List<IAxis<?>> getAxesXTop() {
    return this.m_axesXTop;
  }

  /**
   * Returns the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances that are
   * painted in y dimension on the left side of the chart.
   * <p>
   * <b>Caution!</b> The original list is returned so modifications of it will
   * cause unpredictable side effects.
   * <p>
   * 
   * @return the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances
   *         that are painted in y dimension on the left side of the chart.
   */
  public final List<IAxis<?>> getAxesYLeft() {
    return this.m_axesYLeft;
  }

  /**
   * Returns the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances that are
   * painted in y dimension on the right side of the chart.
   * <p>
   * <b>Caution!</b> The original list is returned so modifications of it will
   * cause unpredictable side effects.
   * <p>
   * 
   * @return the <code>{@link List}&lt;{@link IAxis}&gt;</code> with instances
   *         that are painted in y dimension on the right side of the chart.
   */
  public final List<IAxis<?>> getAxesYRight() {
    return this.m_axesYRight;
  }

  /**
   * Returns the painter for the ticks of the axis.
   * <p>
   * 
   * @return Returns the painter for the ticks of the axis.
   */
  public IAxisTickPainter getAxisTickPainter() {
    return this.m_axisTickPainter;
  }

  /**
   * Returns the first bottom axis for the x dimension.
   * <p>
   * 
   * @return the first bottom axis for the x dimension.
   */
  public final IAxis<?> getAxisX() {
    return this.m_axesXBottom.get(0);
  }

  /**
   * Returns the x axis that the given trace belongs to or null if this trace
   * does not belong to any x axis of this chart.
   * <p>
   * 
   * @param trace
   *          the trace to find the corresponding x axis of this chart for.
   * @return the x axis that the given trace belongs to or null if this trace
   *         does not belong to any x axis of this chart.
   */
  public IAxis<?> getAxisX(final ITrace2D trace) {
    IAxis<? > result = null;
    IAxis<? > current = null;
    Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
    while (it.hasNext()) {
      current = it.next();
      if (current.hasTrace(trace)) {
        result = current;
        break;
      }
    }
    if (result == null) {
      it = this.m_axesXTop.iterator();
      while (it.hasNext()) {
        current = it.next();
        if (current.hasTrace(trace)) {
          result = current;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Returns the first left axis for the y dimension.
   * <p>
   * 
   * @return the first left axis for the y dimension.
   */
  public final IAxis<?> getAxisY() {
    return this.m_axesYLeft.get(0);
  }

  /**
   * Returns the y axis that the given trace belongs to or null if this trace
   * does not belong to any y axis of this chart.
   * <p>
   * 
   * @param trace
   *          the trace to find the corresponding y axis of this chart for.
   * @return the y axis that the given trace belongs to or null if this trace
   *         does not belong to any y axis of this chart.
   */
  public IAxis<?> getAxisY(final ITrace2D trace) {
    IAxis<?> result = null;
    IAxis<?> current = null;
    Iterator<IAxis<?>> it = this.m_axesYLeft.iterator();
    while (it.hasNext()) {
      current = it.next();
      if (current.hasTrace(trace)) {
        result = current;
        break;
      }
    }
    if (result == null) {
      it = this.m_axesYRight.iterator();
      while (it.hasNext()) {
        current = it.next();
        if (current.hasTrace(trace)) {
          result = current;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Returns the color of the grid.
   * <p>
   * 
   * @return the color of the grid.
   */
  public final Color getGridColor() {
    return this.m_gridcolor;
  }

  /**
   * @see javax.swing.JComponent#getHeight()
   */
  @Override
  public int getHeight() {
    int result = -1;
    if (this.m_pageFormat != null) {
      Chart2DActionPrintSingleton printTrigger = Chart2DActionPrintSingleton.getInstance(this);
      if (printTrigger != null) {
        if (printTrigger.isPrintWholePage()) {
          int dpiScreen = Toolkit.getDefaultToolkit().getScreenResolution();
          result = (int) this.m_pageFormat.getImageableHeight() * 72 / dpiScreen;
        }
      }
    }
    if (result == -1) {
      result = super.getHeight();
    }

    return result;
  }

  /**
   * Returns the chart - wide setting for the ms to give a repaint operation
   * time for collecting several repaint requests into one (performance vs.
   * update speed).
   * <p>
   * 
   * @return the setting for the ms to give a repaint operation time for
   *         collecting several repaint requests into one (performance vs.
   *         update speed).
   */
  public synchronized int getMinPaintLatency() {
    return this.m_minPaintLatency;
  }

  /**
   * Returns the nearest <code>{@link ITracePoint2D}</code> to the given mouse
   * event's screen coordinates in Euclid distance.
   * <p>
   * This method is expensive and should not be used when rendering fast
   * changing charts with many points.
   * <p>
   * Using the Manhattan distance is much faster than Euclid distance as it only
   * includes basic addition an absolute value for computation per point (vs.
   * square root, addition and quadrature for Euclid distance). However the
   * euclid distance spans a circle for the nearest points which is visually
   * more normal for end users than the Manhattan distance which forms a rhombus
   * and reaches far distances in only one dimension.
   * <p>
   * 
   * @param mouseEventX
   *          the x pixel value relative to the chart (e.g.: <code>
   *          {@link MouseEvent#getY()}</code>).
   * 
   * @param mouseEventY
   *          the y pixel value relative to the chart (e.g.: <code>
   *          {@link MouseEvent#getY()}</code>).
   * 
   * @return the nearest <code>{@link ITracePoint2D}</code> to the given mouse
   *         event's screen coordinates.
   * 
   * @TODO: This is called twice per mouse move (tool tip and highlighter):
   *        Cache value throughout a paint iteration (delete at end)
   */
  public ITracePoint2D getNearestPointEuclid(final int mouseEventX, final int mouseEventY) {
    ITracePoint2D result = null;
    /*
     * Normalize pixel values:
     */
    double scaledX = 0;
    double scaledY = 0;
    double rangeX = this.getXChartEnd() - this.getXChartStart();
    if (rangeX != 0) {
      scaledX = ((double) mouseEventX - this.getXChartStart()) / rangeX;
    }
    double rangeY = this.getYChartStart() - this.getYChartEnd();
    if (rangeY != 0) {
      scaledY = 1.0 - ((double) mouseEventY - this.getYChartEnd()) / rangeY;
    }

    /*
     * TODO: Maybe cache this call because it searches all axes and evicts
     * duplicates of their assigned traces (subject to profiling).
     */
    Set<ITrace2D> traces = this.getTraces();
    DistancePoint distanceBean;
    DistancePoint winner = null;
    for (ITrace2D trace : traces) {
      distanceBean = trace.getNearestPointEuclid(scaledX, scaledY);
      if (winner == null) {
        winner = distanceBean;
      } else {
        if (distanceBean.getDistance() < winner.getDistance()) {
          winner = distanceBean;
        }
      }
    }
    if (winner != null) {
      result = winner.getPoint();
    }
    return result;
  }

  /**
   * Returns the nearest <code>{@link MouseEvent}</code> to the given mouse
   * event's screen coordinates in Euclid distance.
   * <p>
   * This method is expensive and should not be used when rendering fast
   * changing charts with many points.
   * <p>
   * Note that the given mouse event should be an event fired on this chart
   * component. Else results will point to the nearest point of the chart in the
   * direction of the mouse event's position.
   * <p>
   * Using the Manhattan distance is much faster than Euclid distance as it only
   * includes basic addition an absolute value for computation per point (vs.
   * square root, addition and quadrature for Euclid distance). However the
   * euclid distance spans a circle for the nearest points which is visually
   * more normal for end users than the Manhattan distance which forms a rhombus
   * and reaches far distances in only one dimension.
   * <p>
   * 
   * @param me
   *          a mouse event fired on this component.
   * @return nearest <code>{@link MouseEvent}</code> to the given mouse event's
   *         screen coordinates or <code>null</code> if the chart is empty.
   */
  public ITracePoint2D getNearestPointEuclid(final MouseEvent me) {
    return this.getNearestPointEuclid(me.getX(), me.getY());
  }

  /**
   * Returns the nearest <code>{@link ITracePoint2D}</code> to the given mouse
   * event's screen coordinates in Manhattan distance.
   * <p>
   * This method is expensive and should not be used when rendering fast
   * changing charts with many points.
   * <p>
   * 
   * Using the Manhattan distance is much faster than Euclid distance as it only
   * includes basic addition an absolute value for computation per point (vs.
   * square root, addition and quadrature for Euclid distance). However the
   * euclid distance spans a circle for the nearest points which is visually
   * more normal for end users than the Manhattan distance which forms a rhombus
   * and reaches far distances in only one dimension.
   * <p>
   * 
   * @param mouseEventX
   *          the x pixel value relative to the chart (e.g.: <code>
   *          {@link MouseEvent#getY()}</code>).
   * @param mouseEventY
   *          the y pixel value relative to the chart (e.g.: <code>
   *          {@link MouseEvent#getY()}</code>).
   * 
   * @return the nearest <code>{@link ITracePoint2D}</code> to the given mouse
   *         event's screen coordinates.
   * 
   * @TODO: This is called twice per mouse move (tool tip and highlighter):
   *        Cache value throughout a paint iteration (delete at end)
   */
  public ITracePoint2D getNearestPointManhattan(final int mouseEventX, final int mouseEventY) {
    ITracePoint2D result = null;
    /*
     * Normalize pixel values:
     */
    double scaledX = 0;
    double scaledY = 0;
    double rangeX = this.getXChartEnd() - this.getXChartStart();
    if (rangeX != 0) {
      scaledX = ((double) mouseEventX - this.getXChartStart()) / rangeX;
    }
    double rangeY = this.getYChartStart() - this.getYChartEnd();
    if (rangeY != 0) {
      scaledY = 1.0 - ((double) mouseEventY - this.getYChartEnd()) / rangeY;
    }

    /*
     * TODO: Maybe cache this call because it searches all axes and evicts
     * duplicates of their assigned traces (subject to profiling).
     */
    Set<ITrace2D> traces = this.getTraces();
    DistancePoint distanceBean;
    DistancePoint winner = null;
    for (ITrace2D trace : traces) {
      distanceBean = trace.getNearestPointManhattan(scaledX, scaledY);
      if (winner == null) {
        winner = distanceBean;
      } else {
        if (distanceBean.getDistance() < winner.getDistance()) {
          winner = distanceBean;
        }
      }
    }
    if (winner != null) {
      result = winner.getPoint();
    }
    return result;
  }

  /**
   * Returns the nearest <code>{@link MouseEvent}</code> to the given mouse
   * event's screen coordinates in Manhattan distance.
   * <p>
   * This method is expensive and should not be used when rendering fast
   * changing charts with many points.
   * <p>
   * Note that the given mouse event should be an event fired on this chart
   * component. Else results will point to the nearest point of the chart in the
   * direction of the mouse event's position.
   * <p>
   * Using the Manhattan distance is much faster than Euclid distance as it only
   * includes basic addition an absolute value for computation per point (vs.
   * square root, addition and quadrature for Euclid distance). However the
   * euclid distance spans a circle for the nearest points which is visually
   * more normal for end users than the Manhattan distance which forms a rhombus
   * and reaches far distances in only one dimension.
   * <p>
   * 
   * @param me
   *          a mouse event fired on this component.
   * @return nearest <code>{@link MouseEvent}</code> to the given mouse event's
   *         screen coordinates or <code>null</code> if the chart is empty.
   */
  public ITracePoint2D getNearestPointManhattan(final MouseEvent me) {
    return this.getNearestPointManhattan(me.getX(), me.getY());
  }

  /**
   * Returns the point finder used to find the nearest point corresponding to a
   * mouse event.
   * <p>
   * 
   * @return the point finder used to find the nearest point corresponding to a
   *         mouse event.
   */
  public IPointFinder getPointFinder() {
    return this.m_pointFinder;
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    // TODO Auto-generated method stub
    return super.getPreferredSize();
  }

  /**
   * Overridden to allow full - page printing.
   * <p>
   * 
   * @see java.awt.Component#getSize()
   */
  @Override
  public Dimension getSize() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  /**
   * Returns the chart that will be synchronized for finding the start
   * coordinate of this chart to draw in x dimension (<code>
   * {@link #getXChartStart()}</code>
   * ).
   * <p>
   * This feature is used to allow two separate charts to be painted stacked in
   * y dimension (one below the other) that have different x start coordinates
   * (because of different y labels that shift that value) with an equal
   * starting x value (thus be comparable visually if their x values match).
   * <p>
   * 
   * @return the chart that will be synchronized for finding the start
   *         coordinate of this chart to draw in x dimension (<code>
   *         {@link #getXChartStart()}</code>).
   */
  public synchronized final Chart2D getSynchronizedXStartChart() {
    return this.m_synchronizedXStartChart;
  }

  /**
   * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
   */
  @Override
  public final String getToolTipText(final MouseEvent event) {
    String result;
    result = this.m_toolTip.getToolTipText(this, event);
    if (result == null) {
      result = super.getToolTipText(event);
    }
    /*
     * Point highlighting is managed subsequently in
     * ToolTipType.VALUE_SNAP_TO_TRACEPOINTS! This is done for 2 reasons: 1.
     * Don't compute the point related to a mouse event twice. 2. Don't allow
     * highlighted points with a tool tip that shows the data of the coordinates
     * (vs. the data of the point highlighted) which might be misleading!
     */
    return result;
  }

  /**
   * Returns the type of tool tip shown.
   * <p>
   * 
   * @see Chart2D.ToolTipType#DATAVALUES
   * @see Chart2D.ToolTipType#NONE
   * @see Chart2D.ToolTipType#PIXEL
   * @see Chart2D.ToolTipType#VALUE_SNAP_TO_TRACEPOINTS
   * 
   * @return the type of tool tip shown.
   */
  public IToolTipType getToolTipType() {
    return this.m_toolTip;
  }

  /**
   * Returns the trace point creator of this chart.
   * <p>
   * 
   * @return the trace point creator of this chart.
   */
  public ITracePointProvider getTracePointProvider() {
    return this.m_tracePointProvider;
  }

  /**
   * Returns the set of traces that are currently rendered by this instance.
   * <p>
   * The instances are collected from all underlying axes. The resulting <code> 
   * {@link Set}</code>
   * is not an original set. Therefore modification methods like
   * <code>{@link Set#add(Object)}</code> or <code>{@link Set#clear()}</code>
   * will not have any effect on the setup of this chart.
   * <p>
   * 
   * @return the set of traces that are currently rendered by this instance.
   */
  public final SortedSet<ITrace2D> getTraces() {
    SortedSet<ITrace2D> result = new TreeSet<ITrace2D>();
    // 1.1) axes x bottom:
    Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
    IAxis<?> currentAxis;
    Set<ITrace2D> axisTraces;
    while (it.hasNext()) {
      currentAxis = it.next();
      axisTraces = currentAxis.getTraces();
      // addAll not feasible: assumes currentAxis.getTraces() is sorted and
      // order is lost?
      for (ITrace2D trace : axisTraces) {
        result.add(trace);
      }

      // result.addAll(currentAxis.getTraces());
    }
    // 1.2) axes x top:
    it = this.m_axesXTop.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      axisTraces = currentAxis.getTraces();
      for (ITrace2D trace : axisTraces) {
        result.add(trace);
      }
      // result.addAll(currentAxis.getTraces());
    }
    // We skip y axes as by contract every
    // trace has to be at least in one x axis
    // (not logical if trace is e.g. in y axes
    // only
    // 2.1) axes y left:
    // 2.2) axes y right:

    return result;
  }

  /**
   * @see javax.swing.JComponent#getWidth()
   */
  @Override
  public int getWidth() {
    int result = -1;
    if (this.m_pageFormat != null) {
      Chart2DActionPrintSingleton printTrigger = Chart2DActionPrintSingleton.getInstance(this);
      if (printTrigger != null) {
        if (printTrigger.isPrintWholePage()) {
          int dpiScreen = Toolkit.getDefaultToolkit().getScreenResolution();
          result = (int) this.m_pageFormat.getImageableWidth() * 72 / dpiScreen;
        }
      }
    }
    if (result == -1) {
      result = super.getWidth();
    }

    return result;
  }

  /**
   * Returns the width of the X axis in px.
   * <p>
   * 
   * @return Returns the width of the X axis in px.
   * 
   */
  public final synchronized int getXAxisWidth() {
    return this.m_xChartEnd - this.m_xChartStart;
  }

  /**
   * Returns the x coordinate of the chart's right edge in px.
   * <p>
   * 
   * @return the x coordinate of the chart's right edge in px.
   */
  public final synchronized int getXChartEnd() {
    return this.m_xChartEnd;
  }

  /**
   * Returns the x coordinate of the chart's left edge in px.
   * <p>
   * 
   * @return Returns the x coordinate of the chart's left edge in px.
   */
  public final synchronized int getXChartStart() {
    return this.m_xChartStart;
  }

  /**
   * Returns the y coordinate of the upper edge of the chart's display area in
   * px.
   * <p>
   * Pixel coordinates in awt / swing start from top and increase towards the
   * bottom.
   * <p>
   * 
   * @return The y coordinate of the upper edge of the chart's display area in
   *         px.
   */
  public final synchronized int getYChartEnd() {
    return this.m_yChartEnd;
  }

  /**
   * Returns the y coordinate of the chart's lower edge in px.
   * <p>
   * Pixel coordinates in awt / swing start from top and increase towards the
   * bottom.
   * <p>
   * 
   * @return Returns the y coordinate of the chart's lower edge in px.
   */
  public synchronized int getYChartStart() {
    return this.m_yChartStart;
  }

  /**
   * Returns true if the connection of both interpolated points would cross the
   * visible area.
   * <p>
   * 
   * Caution this method is only intended for two points that were invisible and
   * have been interpolated to a bound of the chart to avoid drawing horizontal
   * or vertical lines at the bound. In case two points that were not
   * interpolated really have a vertical or horizontal connection the result
   * would not be quite correct in terms of painting: Their line would not be
   * drawn even if it would be correct.
   * <p>
   * 
   * Also this method assumes that both points were interpolated towards a bound
   * of the chart via a connection to a previous or following point (line) which
   * means that two points interpolated to the maximum and the minimum (x or y)
   * do not have to be tested for intersections with the viewport rectangle.
   * <p>
   * 
   * @param oldpoint
   *          interpolated point.
   * 
   * @param newpoint
   *          interpolated point following on oldpoint
   * 
   * @return true if the connection of both points
   */
  private boolean hasChartIntersection(ITracePoint2D oldpoint, ITracePoint2D newpoint) {
    boolean result = true;
    // if we needed a more generic (slower) solution we had to test:
    // if x1 >= xmax && x2 >= xmax
    // Caution: getX() and getY() will always return 0.0 by now as we don't
    // backtrace values from interpolated points!
    result = (oldpoint.getScaledX() != newpoint.getScaledX() || oldpoint.getScaledY() != newpoint
        .getScaledY());
    return result;
  }

  /**
   * Internally transfers the state of the old axis to the new one.
   * <p>
   * 
   * This includes things as traces, paint grid state, title, etc..
   * <p>
   * 
   * <h4>Contract</h4> The new axis already has to be added to a chart to avoid
   * a NPE when adding the traces of the old one!
   * <p>
   * 
   * Note that listening / unlistening to the axes is handled from above as this
   * is triggered for example by {@link #setAxisXBottom(AAxis, int)} which also
   * delegates to {@link #removeAxisXBottom(IAxis)} and
   * {@link #addAxisXBottom(AAxis)}.
   * <p>
   * 
   * @param old
   *          the old axis (being removed).
   * 
   * @param axisNew
   *          the replacing new axis.
   */
  private void internalTransferAxisState(IAxis<?> old, AAxis<?> axisNew) {

    // 1. Traces:
    Set<ITrace2D> traces = old.removeAllTraces();
    /*
     * add the traces: this has to be after adding axis to avoid npe in addTrace
     * as no chart is set!
     */
    for (ITrace2D trace : traces) {
      axisNew.addTrace(trace);
    }
    // 2. Title:
    IAxis.AxisTitle title = old.removeAxisTitle();
    axisNew.setAxisTitle(title);
    // 3. Formatter:
    IAxisLabelFormatter formatter = old.getFormatter();
    axisNew.setFormatter(formatter);

    // 4. paint grid flag:
    boolean isPaintGrid = old.isPaintGrid();
    axisNew.setPaintGrid(isPaintGrid);

    // 5. paint scale flag:
    boolean isPaintScale = old.isPaintScale();
    axisNew.setPaintScale(isPaintScale);

    // 6. start major tick:
    boolean startMajorTick = old.isStartMajorTick();
    axisNew.setStartMajorTick(startMajorTick);

    // 7. visibility:
    boolean visible = old.isVisible();
    axisNew.setVisible(visible);

    // 8. Range policy:
    IRangePolicy rangePolicy = old.getRangePolicy();
    axisNew.setRangePolicy(rangePolicy);

    // 9. Range:
    Range range = old.getRange();
    axisNew.setRange(range);

    // done for now...

  }

  /**
   * Interpolates (linear) the two neighboring points.
   * <p>
   * Calling this method only makes sense if argument invisible is not null or
   * if argument visible is not null (if then invisible is null, the visible
   * point will be returned).
   * <p>
   * Visibility is determined only by their internally normalized coordinates
   * that are within [0.0,1.0] for visible points.
   * <p>
   * 
   * @param visible
   *          the visible point.
   * @param invisible
   *          the invisible point.
   * @return the interpolation towards the exceeded bound.
   */
  private ITracePoint2D interpolateVisible(final ITracePoint2D invisible,
      final ITracePoint2D visible) {

    ITracePoint2D result;
    /*
     * In the first call invisible is null because it is the previous point
     * (there was no previous point: just return the new point):
     */
    if (invisible == null) {
      result = visible;
    } else {
      /*
       * Interpolation is done by the two point form: (y - y1)/(x - x1) = (y2 -
       * y1)/(x2 - x1) solved to the missing value.
       */
      // interpolate
      double xInterpolate = Double.NaN;
      double yInterpolate = Double.NaN;
      // find the bounds that has been exceeded:
      // It is possible that two bound have been exceeded,
      // then only one interpolation will be valid:
      boolean interpolated = false;
      boolean interpolatedWrong = false;
      if (invisible.getScaledX() > 1.0) {
        // right x bound
        xInterpolate = 1.0;
        yInterpolate = (visible.getScaledY() - invisible.getScaledY())
            / (visible.getScaledX() - invisible.getScaledX()) * (1.0 - invisible.getScaledX())
            + invisible.getScaledY();
        interpolated = true;
        interpolatedWrong = Double.isNaN(yInterpolate) || yInterpolate < 0.0 || yInterpolate > 1.0;
      }
      if ((invisible.getScaledX() < 0.0) && (!interpolated || interpolatedWrong)) {
        // left x bound
        xInterpolate = 0.0;
        yInterpolate = (visible.getScaledY() - invisible.getScaledY())
            / (visible.getScaledX() - invisible.getScaledX()) * -invisible.getScaledX()
            + invisible.getScaledY();
        interpolated = true;
        interpolatedWrong = Double.isNaN(yInterpolate) || yInterpolate < 0.0 || yInterpolate > 1.0;
      }
      if ((invisible.getScaledY() > 1.0) && (!interpolated || interpolatedWrong)) {
        // upper y bound, checked
        yInterpolate = 1.0;
        xInterpolate = (1.0 - invisible.getScaledY())
            * (visible.getScaledX() - invisible.getScaledX())
            / (visible.getScaledY() - invisible.getScaledY()) + invisible.getScaledX();
        interpolated = true;
        interpolatedWrong = Double.isNaN(xInterpolate) || xInterpolate < 0.0 || xInterpolate > 1.0;

      }
      if ((invisible.getScaledY() < 0.0) && (!interpolated || interpolatedWrong)) {
        // lower y bound
        yInterpolate = 0.0;
        xInterpolate = -invisible.getScaledY() * (visible.getScaledX() - invisible.getScaledX())
            / (visible.getScaledY() - invisible.getScaledY()) + invisible.getScaledX();
        interpolated = true;
        interpolatedWrong = Double.isNaN(xInterpolate) || xInterpolate < 0.0 || xInterpolate > 1.0;
      }
      if (interpolatedWrong) {
        result = visible;
      } else {
        result = this.m_tracePointProvider.createTracePoint(0, 0);
        // transfer potential point highlighters to the synthetic point:
        for (IPointPainter< ? > highlighter : invisible.getAdditionalPointPainters()) {
          result.addAdditionalPointPainter(highlighter);
        }
        result.setScaledX(xInterpolate);
        result.setScaledY(yInterpolate);
        result.setListener(invisible.getListener());
        // TODO: do we have to compute and set the unscaled real values too?
      }
    }
    return result;
  }

  /**
   * Returns true if highlighting of the nearest point to the cursor is enabled.
   * <p>
   * 
   * @return true if highlighting of the nearest point to the cursor is enabled.
   */
  public boolean isEnabledPointHighlighting() {
    boolean isEnabled = false;
    for (MouseMotionListener listener : this.getMouseMotionListeners()) {
      if (listener == this.m_pointHighlightListener) {
        isEnabled = true;
        break;
      }
    }
    return isEnabled;
  }

  /**
   * Returns true if labels for each chart are painted below it, false else.
   * <p>
   * 
   * @return Returns if labels are painted.
   */
  public final boolean isPaintLabels() {
    return this.m_paintLabels;
  }

  /**
   * Returns the requestedRepaint.
   * <p>
   * 
   * @return the requestedRepaint
   */
  protected synchronized boolean isRequestedRepaint() {
    return this.m_requestedRepaint;
  }

  /**
   * Returns true if chart coordinates are drawn as tool tips.
   * <p>
   * 
   * @return true if chart coordinates are drawn as tool tips.
   */
  public final boolean isToolTipCoords() {
    return this.m_toolTip == Chart2D.ToolTipType.DATAVALUES;
  }

  /**
   * Returns whether antialiasing is used.
   * <p>
   * 
   * @return whether antialiasing is used.
   */
  public final boolean isUseAntialiasing() {
    return this.m_useAntialiasing;
  }

  /**
   * Returns true if the given point is in the visible drawing area of the
   * Chart2D.
   * <p>
   * If the point is null false will be returned.
   * <p>
   * This only works if the point argument has been scaled already.
   * <p>
   * 
   * @param point
   *          the point to test.
   * @return true if the given point is in the visible drawing area of the
   *         Chart2D.
   */
  public boolean isVisible(final ITracePoint2D point) {
    boolean result;
    if (point == null) {
      result = false;
    } else {
      result = !(Double.isNaN(point.getScaledX()) || Double.isNaN(point.getScaledY()))
          && !(point.getScaledX() > 1.0 || point.getScaledX() < 0.0 || point.getScaledY() > 1.0 || point
              .getScaledY() < 0.0);
    }
    return result;
  }

  /**
   * Returns an <code>Iterator</code> over the contained {@link ITrace2D}
   * instances.
   * 
   * @return an <code>Iterator</code> over the contained {@link ITrace2D}
   *         instances.
   */
  public final Iterator<ITrace2D> iterator() {
    return this.getTraces().iterator();
  }

  /**
   * Helper that adds this chart as a listener to the required property change
   * events.
   * <p>
   * 
   * @param axis
   *          the axis to listen to.
   */
  private void listenToAxis(final IAxis<?> axis) {
    axis.addPropertyChangeListener(IAxis.PROPERTY_ADD_REMOVE_TRACE, this);
    axis.addPropertyChangeListener(IAxis.PROPERTY_LABELFORMATTER, this);
    axis.addPropertyChangeListener(IAxis.PROPERTY_PAINTGRID, this);
    axis.addPropertyChangeListener(IAxis.PROPERTY_RANGEPOLICY, this);
    axis.addPropertyChangeListener(IAxis.PROPERTY_AXIS_SCALE_POLICY_CHANGED, this);
  }

  /**
   * Helper that adds this chart as a listener to the required property change
   * events.
   * <p>
   * 
   * @param trace
   *          the trace to listen to.
   */
  private void listenToTrace(final ITrace2D trace) {
    // for tracking removal/addition of point highlighters visually:
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED,
        this.m_pointHighlightListener);
    // for tracking enablement/disablement of point highlighting feature
    // (expensive mouse listener)
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, this);
  }

  /**
   * Internally sets the value of <code>{@link #getXChartStart()}</code> and
   * <code>{@link #getXChartEnd()}</code> with respect to another chart
   * synchronized to the same value.
   * <p>
   * 
   * @param g2d
   *          needed for size information.
   * @see #calculateXChartStart(Graphics)
   * @see #calculateXChartEnd(Graphics)
   */
  private void negociateXChart(final Graphics g2d) {
    if (this.m_synchronizedXStartChart != null) {
      int myXChartStart = this.calculateXChartStart(g2d);
      int otherXChartStart = this.m_synchronizedXStartChart
      .calculateXChartStart(g2d); 
      int correctionShift = Math.abs(myXChartStart - otherXChartStart);
      
      this.m_xChartStart = Math.max(this.calculateXChartStart(g2d), this.m_synchronizedXStartChart
          .calculateXChartStart(g2d));
      this.m_xChartEnd = Math.max(this.calculateXChartEnd(g2d), this.m_synchronizedXStartChart
          .calculateXChartEnd(g2d));
      synchronized (this.m_synchronizedXStartChart) {
        this.m_synchronizedXStartChart.m_xChartStart = this.m_xChartStart;
        this.m_synchronizedXStartChart.m_xChartEnd = this.m_xChartEnd;
        /*
         * Install correction shift to the one that would be naturally more to the left: 
         */
        if (myXChartStart > otherXChartStart) {
          this.m_synchronizedXStartChart.installXAxisLeftOffset(g2d, correctionShift);
        } else {
          this.installXAxisLeftOffset(g2d, correctionShift);
        }
      }
    } else {
      if (!this.m_synchronizedXStart) {
        this.m_xChartStart = this.calculateXChartStart(g2d);
        this.m_xChartEnd = this.calculateXChartEnd(g2d);
      }
    }
  }

  /**
   * 
   * This method is just overridden to ensure a lock on this instance and then a lock on 
   * <code>{@link Container#getTreeLock()}</code> just as it has to be done in that order in
   * <code>{@link #addTrace(ITrace2D, IAxis, IAxis)}</code>.
   * <p>
   * 
   * More info: This lock is needed to ensure a lock on the tree is acquired
   * before a lock to the chart. Method addTrace has a code path that might
   * descend into ChartPanel where Container.getComponents() (in method
   * containsTraceLabel()) will require the tree lock after having already
   * acquired the chart lock -> deadlock between paint thread and addTrace
   * thread when using ChartPanel.
   * <p>
   * 
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g) {
    synchronized (this.getTreeLock()) {
      super.paint(g);
    }
  }

  /**
   * A basic rule of a JComponent is: <br>
   * <b>Never invoke this method directly. </b> <br>
   * See the description of <code>
   * {@link javax.swing.JComponent#paintComponent(java.awt.Graphics)}</code>
   * for details.
   * <p>
   * If you do invoke this method you may encounter performance issues,
   * flickering UI and even deadlocks.
   * <p>
   * 
   * @param g
   *          the graphics context to use.
   */
  @Override
  protected synchronized void paintComponent(final Graphics g) {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("paint, 1 lock");
    }
    super.paintComponent(g);
    // printing ?
    if (this.m_pageFormat != null) {
      /*
       * User (0,0) is typically outside the imageable area, so we must
       * translate by the X and Y values in the PageFormat to avoid clipping
       */
      Graphics2D g2d = (Graphics2D) g;
      double startX = this.m_pageFormat.getImageableX();
      double startY = this.m_pageFormat.getImageableY();
      g2d.translate(startX, startY);
    }
    this.updateScaling(false);
    // will be used in several iterations.
    ITrace2D trace;
    Iterator<ITrace2D> traceIt;
    // painting trace labels
    this.negociateXChart(g);
    int labelHeight = this.paintTraceLabels(g);
    // finding start point of coordinate System.
    this.m_yChartStart = this.calculateYChartStart(g, labelHeight);
    this.m_yChartEnd = this.calculateYChartEnd(g);
    int rangex = this.m_xChartEnd - this.m_xChartStart;
    int rangey = this.m_yChartStart - this.m_yChartEnd;
    this.paintCoordinateSystem(g);
    // paint Traces.
    int tmpx = 0;
    int oldtmpx;
    int tmpy = 0;
    int oldtmpy;
    ITracePoint2D oldpoint = null;
    ITracePoint2D newpoint = null;
    ITracePoint2D tmppt = null;
    traceIt = this.getTraces().iterator();
    // Some operations (e.g. stroke) need Graphics2d
    Graphics2D g2d = null;
    Stroke backupStroke = null;

    if (g instanceof Graphics2D) {
      g2d = (Graphics2D) g;
      backupStroke = g2d.getStroke();
      if (this.isUseAntialiasing()) {
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }
    }

    int count = 0;
    Iterator<ITracePainter< ? >> itTracePainters;
    Iterator<IErrorBarPolicy< ? >> itTraceErrorBarPolicies;
    ITracePainter< ? > tracePainter;
    IErrorBarPolicy< ? > errorBarPolicy;
    while (traceIt.hasNext()) {
      oldpoint = null;
      newpoint = null;
      count++;
      trace = traceIt.next();
      if (trace.isVisible()) {
        synchronized (trace) {
          if (Chart2D.DEBUG_THREADING) {
            System.out.println("Chart2D.paintComponent(" + Thread.currentThread().getName()
                + "), 2 locks (lock on trace " + trace.getName() + ")");
          }
          boolean hasErrorBars = trace.getHasErrorBars();
          if (g2d != null) {
            g2d.setStroke(trace.getStroke());
          }
          g.setColor(trace.getColor());
          Set<ITracePainter< ? >> tracePainters = trace.getTracePainters();
          itTracePainters = tracePainters.iterator();
          tracePainter = null;
          while (itTracePainters.hasNext()) {
            tracePainter = itTracePainters.next();
            tracePainter.startPaintIteration(g);
          }
          if (hasErrorBars) {
            errorBarPolicy = null;
            Set<IErrorBarPolicy< ? >> errorBarPolicies = trace.getErrorBarPolicies();
            itTraceErrorBarPolicies = errorBarPolicies.iterator();
            while (itTraceErrorBarPolicies.hasNext()) {
              errorBarPolicy = itTraceErrorBarPolicies.next();
              errorBarPolicy.startPaintIteration(g);
            }
          }
          Iterator<ITracePoint2D> pointIt = trace.iterator();
          boolean newpointVisible = false;
          boolean oldpointVisible = false;
          while (pointIt.hasNext()) {
            oldpoint = newpoint;
            oldtmpx = tmpx;
            oldtmpy = tmpy;
            newpoint = pointIt.next();
            newpointVisible = this.isVisible(newpoint);
            oldpointVisible = this.isVisible(oldpoint);

            /*
             * Special case: if we have NaN just don't interpolate anything or
             * paint but just continue (and give a signal to trace painters to
             * discontinue which is neccessary for implementations that paint
             * polylines and must not accumulate polylines that have a
             * discontinuation within):
             */
            boolean isNaNNewpoint = Double.isNaN(newpoint.getX()) || Double.isNaN(newpoint.getY());
            boolean isNanOldpoint;
            if (oldpoint == null) {
              isNanOldpoint = false;
            } else {
              isNanOldpoint = Double.isNaN(oldpoint.getX()) || Double.isNaN(oldpoint.getY());
            }
            if (isNaNNewpoint || isNanOldpoint) {
              /*
               * Only discontinue when entering NaN space as calls to it for
               * subsequent NaN values would repeat the same polyline paint of
               * the last valid point in TracePainterPolyline (senseless).
               */
              if (!(isNanOldpoint) && (isNaNNewpoint)) {
                for (ITracePainter< ? > painter : trace.getTracePainters()) {
                  painter.discontinue(g2d);
                }
              }
              if (!isNaNNewpoint) {
                tmpx = this.m_xChartStart + (int) Math.round(newpoint.getScaledX() * rangex);
                tmpy = this.m_yChartStart - (int) Math.round(newpoint.getScaledY() * rangey);

              }
            } else if (!newpointVisible && !oldpointVisible) {
              // save for next loop:
              tmppt = (ITracePoint2D) newpoint.clone();
              int tmptmpx = tmpx;
              int tmptmpy = tmpy;
              /*
               * check if the interconnection of both invisible points cuts the
               * visible area:
               */
              oldpoint = this.interpolateVisible(oldpoint, newpoint);
              newpoint = this.interpolateVisible(newpoint, oldpoint);

              tmpx = this.m_xChartStart + (int) Math.round(newpoint.getScaledX() * rangex);
              tmpy = this.m_yChartStart - (int) Math.round(newpoint.getScaledY() * rangey);
              oldtmpx = this.m_xChartStart + (int) Math.round(oldpoint.getScaledX() * rangex);
              oldtmpy = this.m_yChartStart - (int) Math.round(oldpoint.getScaledY() * rangey);
              // only paint if intersection!
              if (this.hasChartIntersection(oldpoint, newpoint)) {
                // don't use error bars for interpolated points that do not
                // intersect the chart's viewport!
                this.paintPoint(oldtmpx, oldtmpy, tmpx, tmpy, true, trace, g, newpoint, false);
              }
              // restore for next loop start:
              newpoint = tmppt;
              tmpx = tmptmpx;
              tmpy = tmptmpy;
            } else if (newpointVisible && !oldpointVisible) {
              // entering the visible bounds: interpolate from old point
              // to new point
              oldpoint = this.interpolateVisible(oldpoint, newpoint);
              tmpx = this.m_xChartStart + (int) Math.round(newpoint.getScaledX() * rangex);
              tmpy = this.m_yChartStart - (int) Math.round(newpoint.getScaledY() * rangey);
              oldtmpx = this.m_xChartStart + (int) Math.round(oldpoint.getScaledX() * rangex);
              oldtmpy = this.m_yChartStart - (int) Math.round(oldpoint.getScaledY() * rangey);
              // don't use error bars for interpolated points!
              this.paintPoint(oldtmpx, oldtmpy, tmpx, tmpy, true, trace, g, newpoint, false);
            } else if (!newpointVisible && oldpointVisible) {
              // leaving the visible bounds:
              tmppt = (ITracePoint2D) newpoint.clone();
              newpoint = this.interpolateVisible(newpoint, oldpoint);
              tmpx = this.m_xChartStart + (int) Math.round(newpoint.getScaledX() * rangex);
              tmpy = this.m_yChartStart - (int) Math.round(newpoint.getScaledY() * rangey);
              // don't use error bars for interpolated points!
              this.paintPoint(oldtmpx, oldtmpy, tmpx, tmpy, true, trace, g, newpoint, false);
              // restore for next loop start:
              newpoint = tmppt;
            } else {
              // staying in the visible bounds: just paint
              tmpx = this.m_xChartStart + (int) Math.round(newpoint.getScaledX() * rangex);
              tmpy = this.m_yChartStart - (int) Math.round(newpoint.getScaledY() * rangey);
              this
                  .paintPoint(oldtmpx, oldtmpy, tmpx, tmpy, false, trace, g, newpoint, hasErrorBars);
            }
          }
          itTracePainters = trace.getTracePainters().iterator();
          while (itTracePainters.hasNext()) {
            tracePainter = itTracePainters.next();
            tracePainter.endPaintIteration(g);
          }
          if (hasErrorBars) {
            itTraceErrorBarPolicies = trace.getErrorBarPolicies().iterator();
            while (itTraceErrorBarPolicies.hasNext()) {
              errorBarPolicy = itTraceErrorBarPolicies.next();
              errorBarPolicy.endPaintIteration(g);
            }
          }
        }
      }
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("paint(" + Thread.currentThread().getName() + "), left lock on trace "
            + trace.getName());
      }
    }
    if (g2d != null) {
      g2d.setStroke(backupStroke);
    }

  }

  /**
   * Paints the axis, the scales and the labels for the chart.
   * <p>
   * <b>Caution</b> This is highly coupled code and only factored out for better
   * overview. This method may only be called by {@link #paint(Graphics)} and
   * the order of this invocation there must not be changed.
   * <p>
   * 
   * @param g2d
   *          the graphics context to use.
   */
  private void paintCoordinateSystem(final Graphics g2d) {
    // drawing the axes:
    g2d.setColor(this.getForeground());
    IAxis<?> currentAxis;
    // 1) x axes:
    // 1.1) x axes bottom:
    Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      currentAxis.paint(g2d);
    }
    // 1.2) Top x axes:
    it = this.m_axesXTop.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      currentAxis.paint(g2d);
    }

    // 2) y axes:
    // 2.1) y axes left
    it = this.m_axesYLeft.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      currentAxis.paint(g2d);
    }

    // 2.1) y axes right
    it = this.m_axesYRight.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      currentAxis.paint(g2d);
    }
  }

  /**
   * Internally renders the error bars for the given point for the given trace.
   * <p>
   * The current point to render in px is defined by the first two arguments,
   * the next point to render in px is defined by the 2nd two arguments.
   * <p>
   * 
   * @param trace
   *          needed to get the {@link IErrorBarPolicy} instances to use.
   * @param oldtmpx
   *          the x coordinate of the original point to render an error bar for.
   * @param oldtmpy
   *          the y coordinate of the original point to render an error bar for.
   * @param tmpx
   *          the x coordinate of the original next point to render an error bar
   *          for.
   * @param tmpy
   *          the y coordinate of the original next point to render an error bar
   *          for.
   * @param g2d
   *          the graphics context to use.
   * @param discontinue
   *          if a discontinuity has been taken place and all potential cached
   *          points by an <code>{@link ITracePainter}</code> (done for polyline
   *          performance boost) have to be drawn immediately before starting a
   *          new point caching.
   * @param original
   *          intended for information only, should nor be needed to paint the
   *          point neither be changed in any way!
   */
  private void paintErrorBars(final ITrace2D trace, final int oldtmpx, final int oldtmpy,
      final int tmpx, final int tmpy, final Graphics g2d, final boolean discontinue,
      final ITracePoint2D original) {
    IErrorBarPolicy< ? > errorBarPolicy;
    Iterator<IErrorBarPolicy< ? >> itTraceErrorBarPolicies = trace.getErrorBarPolicies().iterator();
    while (itTraceErrorBarPolicies.hasNext()) {
      errorBarPolicy = itTraceErrorBarPolicies.next();
      errorBarPolicy.paintPoint(oldtmpx, oldtmpy, tmpx, tmpy, g2d, original);
      if (discontinue) {
        errorBarPolicy.discontinue(g2d);
      }
    }
  }

  /**
   * Internally paints the point with respect to trace painters (
   * {@link ITracePainter}) and error bar painter ({@link IErrorBarPolicy}) of
   * the trace.
   * <p>
   * This method must not be called directly as it does not support
   * interpolation of visibility bounds (discontinuations).
   * <p>
   * 
   * @param xPxOld
   *          the x coordinate of the previous point to render in px
   *          (potentially an interpolation of it if the old point was not
   *          visible and the new point is).
   * @param yPxOld
   *          the y coordinate of the previous point to render in px
   *          (potentially an interpolation of it if the old point was not
   *          visible and the new point is).
   * @param xPxNew
   *          the x coordinate of the point to render in px (potentially an
   *          interpolation of it if the old point was visible and the new point
   *          is not).
   * @param yPxNew
   *          the y coordinate of the point to render in px (potentially an
   *          interpolation of it if the old point was visible and the new point
   *          is not).
   * @param trace
   *          needed for obtaining trace painters and error bar painters.
   * @param g2d
   *          the graphics context to use.
   * @param discontinue
   *          if a discontinuation has been taken place and all potential cached
   *          points by an <code>{@link ITracePainter}</code> (done for polyline
   *          performance boost) have to be drawn immediately before starting a
   *          new point caching.
   * @param original
   *          intended for information only, should nor be needed to paint the
   *          point neither be changed in any way!
   * @param errorBarSupport
   *          optimization that allows to skip error bar code.
   */
  private final void paintPoint(final int xPxOld, final int yPxOld, final int xPxNew,
      final int yPxNew, final boolean discontinue, final ITrace2D trace, final Graphics g2d,
      final ITracePoint2D original, final boolean errorBarSupport) {
    Iterator<ITracePainter< ? >> itTracePainters;
    ITracePainter< ? > tracePainter;
    itTracePainters = trace.getTracePainters().iterator();
    while (itTracePainters.hasNext()) {
      tracePainter = itTracePainters.next();
      tracePainter.paintPoint(xPxOld, yPxOld, xPxNew, yPxNew, g2d, original);
      Set<IPointPainter< ? >> additionalHighlighters = original.getAdditionalPointPainters();
      Iterator<IPointPainter< ? >> itPointHighlighters = additionalHighlighters.iterator();
      IPointPainter< ? > highlighter;
      while (itPointHighlighters.hasNext()) {
        highlighter = itPointHighlighters.next();
        highlighter.paintPoint(xPxNew, yPxNew, xPxNew, yPxNew, g2d, original);
      }
      if (discontinue) {
        tracePainter.discontinue(g2d);
      }
    }
    if (errorBarSupport) {
      this.paintErrorBars(trace, xPxOld, yPxOld, xPxNew, yPxNew, g2d, discontinue, original);
    }
  }

  /**
   * Internally paints the labels for the traces below the chart.
   * <p>
   * 
   * @param g2d
   *          the graphic context to use.
   * @return the amount of vertical (y) px used for the labels.
   */
  private int paintTraceLabels(final Graphics g2d) {
    int labelheight = 0;
    Dimension d = this.getSize();
    if (this.m_paintLabels) {
      ITrace2D trace;
      Iterator<ITrace2D> traceIt = this.getTraces().iterator();
      int xtmpos = this.m_xChartStart;
      int ytmpos = (int) d.getHeight() - 2;
      int remwidth = (int) d.getWidth() - this.m_xChartStart;
      int allwidth = remwidth;
      int lblwidth = 0;
      String tmplabel;
      boolean crlfdone = false;
      // finding the font- dimensions in px
      FontMetrics fontdim = g2d.getFontMetrics();
      // includes leading space
      int fontheight = fontdim.getHeight();

      if (traceIt.hasNext()) {
        labelheight += fontheight;
      }
      while (traceIt.hasNext()) {
        trace = traceIt.next();
        if (trace.isVisible()) {
          tmplabel = trace.getLabel();
          if (!StringUtil.isEmpty(tmplabel)) {
            lblwidth = fontdim.stringWidth(tmplabel) + 10;
            // conditional linebreak.
            // crlfdone avoids never doing linebreak if all
            // labels.length()>allwidth
            if (lblwidth > remwidth) {
              if (!(lblwidth > allwidth) || (!crlfdone)) {
                ytmpos -= fontheight;
                xtmpos = this.m_xChartStart;
                labelheight += fontheight;
                crlfdone = true;
                remwidth = (int) d.getWidth() - this.m_xChartStart;
              } else {
                crlfdone = false;
              }
            }
            remwidth -= lblwidth;
            g2d.setColor(trace.getColor());
            g2d.drawString(tmplabel, xtmpos, ytmpos);
            xtmpos += lblwidth;
          }
        }
      }
    }
    return labelheight;
  }

  /**
   * @see java.awt.print.Printable#print(java.awt.Graphics,
   *      java.awt.print.PageFormat, int)
   */
  public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
      throws PrinterException {
    int result;
    if (pageIndex > 0) {
      // We have only one page, and 'page' is zero-based.
      result = NO_SUCH_PAGE;
    } else {

      // mark we are in a printing - paint iteration:
      this.m_pageFormat = pageFormat;
      this.updateScaling(true);
      /* Now print the window and its visible contents */
      this.printAll(graphics);

      /* tell the caller that this page is part of the printed document */
      result = PAGE_EXISTS;
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
          .println("chart.propertyChange (" + Thread.currentThread().getName() + "), 0 locks");
    }
    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("Chart2D.propertyChange, " + evt.getPropertyName() + " ("
            + Thread.currentThread().getName() + "), 1 lock");
      }
      // TODO: use the property change reactor idiom also used in AAxis for
      // performance.
      String property = evt.getPropertyName();
      if (property.equals(IRangePolicy.PROPERTY_RANGE)) {
        // repaint
      } else if (property.equals(IRangePolicy.PROPERTY_RANGE_MAX)) {
        // repaint
      } else if (property.equals(IRangePolicy.PROPERTY_RANGE_MIN)) {
        // repaint
      } else if (property.equals(ITrace2D.PROPERTY_STROKE)) {
        /*
         * TODO: perhaps react more fine grained for the following events: just
         * repaint the trace without all the paint code (scaling, axis,...).
         * But: These property changes are triggered by humans and occur very
         * seldom. Huge work non-l&f performance improvement.
         */
      } else if (property.equals(ITrace2D.PROPERTY_COLOR)) {
        // repaint
      } else if (property.equals(IAxis.PROPERTY_LABELFORMATTER)) {
        /*
         * TODO: Maybe only repaint the axis? Much complicated work vs.
         * occassional user interaction.
         */
      } else if (property.equals(IAxis.PROPERTY_ADD_REMOVE_TRACE)) {
        /*
         * Relay the event as outsiders don't want to deal with internals
         * (listen to axes to be informed whenever a trace was added). Also:
         * repaint definetely!
         */
        this.firePropertyChange(IAxis.PROPERTY_ADD_REMOVE_TRACE, evt.getOldValue(), evt
            .getNewValue());
      } else if (property.equals(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED)) {
        int highlightersAddedOrRemoved = 0;
        if (evt.getOldValue() != null) {
          highlightersAddedOrRemoved--;
        }
        if (evt.getNewValue() != null) {
          highlightersAddedOrRemoved++;
        }
        this.trackHighlightingEnablement(highlightersAddedOrRemoved);
      } else if (property.equals(IAxis.PROPERTY_AXIS_SCALE_POLICY_CHANGED)) {
        // repaint
      } else if (property.equals(IAxis.PROPERTY_PAINTGRID)) {
        // repaint
      } else if (property.equals(IAxis.PROPERTY_RANGEPOLICY)) {
        // repaint
      } else {
        throw new IllegalStateException("Received a property change event \"" + property
            + "\" the code is not expecting (programming error).");
      }
      this.setRequestedRepaint(true);
    }
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("Chart2D.propertyChange, leaving (" + Thread.currentThread().getName()
          + "), 0 locks");
    }
  }

  /**
   * Convenience method to remove all traces from this chart.
   * <p>
   * This method is broken down to every axis contained in the trace and will
   * fire a <code>{@link PropertyChangeEvent}</code> for the
   * <code>{@link PropertyChangeEvent#getPropertyName()}</code>
   * <code>{@link IAxis#PROPERTY_ADD_REMOVE_TRACE}</code> for every single trace
   * removed to <code>{@link PropertyChangeListener}</code> of the corresponding
   * axes.
   * <p>
   * 
   * @return a non-original-backed set of distinct traces that was contained in
   *         this chart before.
   */
  public Set<ITrace2D> removeAllTraces() {

    Set<ITrace2D> result = new TreeSet<ITrace2D>();
    Set<ITrace2D> axisTraces;

    // 1.1) axes x bottom:
    Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
    IAxis<?> currentAxis;
    while (it.hasNext()) {
      currentAxis = it.next();
      axisTraces = currentAxis.removeAllTraces();
      result.addAll(axisTraces);

    }
    // 1.2) axes x top:
    it = this.m_axesXTop.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      axisTraces = currentAxis.removeAllTraces();
      result.addAll(axisTraces);
      axisTraces.clear();
    }
    /*
     * We skip "result.addAll(...) for y axes as by contract every trace has to
     * be at least in one x axis (not logical if trace is e.g. in y axes only).
     */
    // 2.1) axes y left:
    it = this.m_axesYLeft.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      axisTraces = currentAxis.removeAllTraces();
      axisTraces.clear();
    }
    // 2.2) axes y right:
    it = this.m_axesYRight.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      axisTraces = currentAxis.removeAllTraces();
      axisTraces.clear();
    }
    return result;

  }

  /**
   * Removes the given x axis from the list of internal bottom x axes.
   * <p>
   * The given axis should be contained before or false will be returned.
   * <p>
   * 
   * @param axisX
   *          the bottom x axis to remove.
   * @return true if the given axis was successfully removed or false if it was
   *         not configured as a bottom x axis before or could not be removed
   *         for another reason.
   */
  public boolean removeAxisXBottom(final IAxis<?> axisX) {
    boolean result = this.m_axesXBottom.remove(axisX);

    this.unlistenToAxis(axisX);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_X, axisX, null);
    this.setRequestedRepaint(true);
    return result;
  }

  /**
   * Removes the given x axis from the list of internal top x axes.
   * <p>
   * The given axis should be contained before or false will be returned.
   * <p>
   * 
   * @param axisX
   *          the top x axis to remove.
   * @return true if the given axis was successfully removed or false if it was
   *         not configured as a top x axis before or could not be removed for
   *         another reason.
   */
  public boolean removeAxisXTop(final IAxis<?> axisX) {
    boolean result = this.m_axesXTop.remove(axisX);

    this.unlistenToAxis(axisX);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_X, axisX, null);
    this.setRequestedRepaint(true);
    return result;
  }

  /**
   * Removes the given y axis from the list of internal left y axes.
   * <p>
   * The given axis should be contained before or false will be returned.
   * <p>
   * 
   * @param axisY
   *          the left y axis to remove.
   * @return true if the given axis was successfully removed or false if it was
   *         not configured as a left y axis before or could not be removed for
   *         another reason.
   */
  public boolean removeAxisYLeft(final IAxis<?> axisY) {

    boolean result = this.m_axesYLeft.remove(axisY);
    this.unlistenToAxis(axisY);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_Y, axisY, null);
    this.setRequestedRepaint(true);
    return result;
  }

  /**
   * Removes the given y axis from the list of internal right y axes.
   * <p>
   * The given axis should be contained before or false will be returned.
   * <p>
   * 
   * @param axisY
   *          the right y axis to remove.
   * @return true if the given axis was successfully removed or false if it was
   *         not configured as a right y axis before or could not be removed for
   *         another reason.
   */
  public boolean removeAxisYRight(final IAxis<?> axisY) {
    boolean result = this.m_axesYRight.remove(axisY);

    this.unlistenToAxis(axisY);

    this.firePropertyChange(Chart2D.PROPERTY_AXIS_Y, axisY, null);
    this.setRequestedRepaint(true);

    return result;
  }

  /**
   * Removes the given instance from this <code>Chart2D</code> if it is
   * contained.
   * <p>
   * This method will trigger a {@link java.beans.PropertyChangeEvent} being
   * fired on all instances registered by
   * {@link javax.swing.JComponent#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}
   * (registered with <code>String</code> argument
   * {@link IAxis#PROPERTY_ADD_REMOVE_TRACE} on the internal axes).
   * <p>
   * 
   * @return true if the given trace was removed successfully, false else. 
   * 
   * @param points
   *          the trace to remove.
   * @see IAxis#PROPERTY_ADD_REMOVE_TRACE
   */
  public final boolean removeTrace(final ITrace2D points) {
    boolean result = false;
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("removeTrace, 0 locks");
    }
    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("removeTrace, 1 lock");
      }
      synchronized (points) {
        // remove the trace from the axes it is potentially contained in:
        // 1) x - axes:
        Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
        IAxis<?> currentAxis;
        boolean successRemoveX = false;
        while (it.hasNext()) {
          currentAxis = it.next();
          successRemoveX = currentAxis.removeTrace(points);
          if (successRemoveX) {
            break;
          }
        }
        // was not found in bottom x axes:
        if (!successRemoveX) {
          it = this.m_axesXTop.iterator();
          while (it.hasNext()) {
            currentAxis = it.next();
            successRemoveX = currentAxis.removeTrace(points);
            if (successRemoveX) {
              break;
            }
          }
        }
        // 2) y - axes:
        boolean successRemoveY = false;
        it = this.m_axesYLeft.iterator();
        while (it.hasNext()) {
          currentAxis = it.next();
          successRemoveY = currentAxis.removeTrace(points);
        }
        // was not found in left y axes:
        if (!successRemoveY) {
          it = this.m_axesYRight.iterator();
          while (it.hasNext()) {
            currentAxis = it.next();
            successRemoveY = currentAxis.removeTrace(points);
            if (successRemoveY) {
              break;
            }
          }
        }
        boolean success = successRemoveY && successRemoveX;
        if (success ) {
          int amountofremovedhighlighters = points.getPointHighlighters().size();
          this.trackHighlightingEnablement(amountofremovedhighlighters);
          this.unlistenToTrace(points);
          this.setRequestedRepaint(true);
        }
        result = success;
        return result;
      }
    }
  }

  /**
   * @deprecated use {@link #setRequestedRepaint(boolean)}.
   * @see java.awt.Component#repaint()
   */
  @Override
  @Deprecated
  public void repaint() {
    super.repaint();
  }

  /**
   * @deprecated use {@link #setRequestedRepaint(boolean)}.
   * @see java.awt.Component#repaint(int, int, int, int)
   */
  @Override
  @Deprecated
  public void repaint(final int x, final int y, final int width, final int height) {
    super.repaint(x, y, width, height);
  }

  /**
   * @deprecated use {@link #setRequestedRepaint(boolean)}.
   * @see java.awt.Component#repaint(long)
   */
  @Override
  @Deprecated
  public void repaint(final long tm) {
    super.repaint(tm);
  }

  /**
   * @deprecated use {@link #setRequestedRepaint(boolean)}.
   * @see javax.swing.JComponent#repaint(long, int, int, int, int)
   */
  @Override
  @Deprecated
  public void repaint(final long tm, final int x, final int y, final int width, final int height) {
    super.repaint(tm, x, y, width, height);
  }

  /**
   * @deprecated use {@link #setRequestedRepaint(boolean)}.
   * @see javax.swing.JComponent#repaint(java.awt.Rectangle)
   */
  @Override
  @Deprecated
  public void repaint(final Rectangle r) {
    super.repaint(r);
  }

  /**
   * Only intended for <code>{@link Chart2DActionPrintSingleton}</code>.
   * <p>
   */
  public void resetPrintMode() {
    this.m_pageFormat = null;
    this.setRequestedRepaint(true);
  }

  /**
   * Sets the axis tick painter.
   * <p>
   * 
   * @param tickPainter
   *          The axis tick painter to set.
   */
  public synchronized void setAxisTickPainter(final IAxisTickPainter tickPainter) {
    this.m_axisTickPainter = tickPainter;
  }

  /**
   * Sets the first bottom x axis to use.
   * <p>
   * This is compatibility support for the API of jchart2d prior to 3.0.0 where
   * only one x axis was supported.
   * <p>
   * 
   * @deprecated use <code>{@link #setAxisXBottom(AAxis, int)}</code> instead.
   * 
   * @see #setAxisXBottom(AAxis, int)
   * 
   * @param axisX
   *          the first bottom x axis to use.
   * 
   * @return a copied List with the previous bottom x <code>{@link IAxis}</code>
   *         instance that was used at position 0.
   */
  @Deprecated
  public List<IAxis<?>> setAxisX(final AAxis<?> axisX) {
    List<IAxis<?>> axesBottom = new LinkedList<IAxis<?>>();
    for(IAxis<?> axis:this.m_axesXBottom) {
      // don't call remove here (concurrent modification exception), just collect:
      axesBottom.add(axis);
    }
    List<IAxis<?>> axesTop = new LinkedList<IAxis<?>>();
    
    for(IAxis<?> axis:this.m_axesXTop) {
      // don't call remove here (concurrent modification exception), just collect:
      axesTop.add(axis);
    }
    // now remove
    for(IAxis<?>axis:axesBottom) {
      this.removeAxisXBottom(axis);
    }
    for(IAxis<?>axis:axesTop) {
      this.removeAxisXTop(axis);
    }
    axesBottom.addAll(axesTop);
    return axesBottom;
  }

  /**
   * Sets the bottom x axis on the given position to use and replaces the old
   * one on that place.
   * <p>
   * This method delegates to <code>{@link #addAxisXBottom(AAxis)}</code> and
   * also uses <code>{@link #removeAxisXBottom(IAxis)}</code> in case a bottom x
   * axis was configured at the position. So the events
   * <code>{@link Chart2D#PROPERTY_AXIS_X}</code> will be fired for remove and
   * add.
   * <p>
   * 
   * Furthermore this method uses "replace - semantics". The
   * <code>{@link ITrace2D}</code> instances contained in the previous x bottom
   * axis will be implanted to this new axis. Also the title and stuff like grid
   * settings will be transferred. For this an event with property
   * <code>{@link Chart2D#PROPERTY_AXIS_X_BOTTOM_REPLACE} </code> is fired.
   * <p>
   * 
   * <b>Note</b> that <code>{@link PropertyChangeListener}s</code> of the axis will not be
   * transferred silently to the new axis but have to handle their unregistering
   * from the old axis / registering to the new axis from outside to give them
   * the chance to manage their state transitions by themselves.<br/>
   * Before the state of the old axis is transferred they will receive
   * <code>{@link PropertyChangeListener#propertyChange(PropertyChangeEvent)}</code>
   * with <code>{@link Chart2D#PROPERTY_AXIS_X_BOTTOM_REPLACE}</code> as code
   * and the old and new axis as values and have the change to change their peer
   * to listen on thus receiving the change events generated on the new axis. At
   * the moment the replace event is sent they will already have received the
   * event <code>{@link Chart2D#PROPERTY_AXIS_X}</code> event for the removal:
   * So be careful not to react on that first event by removing your listener
   * from the axis as you then will not receive the replace event!
   * <p>
   * 
   * @param axisX
   *          the first bottom x axis to use.
   * 
   * @param position
   *          the index of the axis on bottom x dimension (starting from 0).
   * 
   * @return the previous axis on that bottom x position or null.
   */
  public IAxis<?> setAxisXBottom(final AAxis<?> axisX, final int position) {
    IAxis<?> old = null;

    if (this.m_axesXBottom.size() > position) {
      // this is a replace operation!
      old = this.m_axesXBottom.get(position);
      this.removeAxisXBottom(old);
      this.firePropertyChange(PROPERTY_AXIS_X_BOTTOM_REPLACE, old, axisX);
    }
    // add anyways in case no axis has been set before (see constructor)
    this.addAxisXBottom(axisX);
    // we can only transfer state (which includes adding traces after the new
    // axis is assigned to a chart!
    if (old != null) {
      this.internalTransferAxisState(old, axisX);
    }
    this.m_mouseTranslationXAxis = axisX;
    this.setRequestedRepaint(true);
    return old;
  }

  /**
   * Sets the top x axis on the given position to use and replaces the old one
   * on that place.
   * <p>
   * This method delegates to <code>{@link #addAxisXBottom(AAxis)}</code> and
   * also uses <code>{@link #removeAxisXBottom(IAxis)}</code> in case a top x
   * axis was configured on the position. So the events
   * <code>{@link Chart2D#PROPERTY_AXIS_X}</code> will be fired for remove and
   * add.
   * <p>
   * 
   * Furthermore this method uses "replace - semantics". The
   * <code>{@link ITrace2D}</code> instances contained in the previous x top
   * axis will be implanted to this new axis. Also the title and stuff like grid
   * settings will be transferred. For this an event with property
   * <code>{@link Chart2D#PROPERTY_AXIS_Y_LEFT_REPLACE} </code> is fired.
   * <p>
   * 
   * <b>Note</b> that <code>{@link PropertyChangeListener}s</code> of the axis will not be
   * transferred silently to the new axis but have to handle their unregistering
   * from the old axis / registering to the new axis from outside to give them
   * the chance to manage their state transitions by themselves.<br/>
   * Before the state of the old axis is transferred they will receive
   * <code>{@link PropertyChangeListener#propertyChange(PropertyChangeEvent)}</code>
   * with <code>{@link Chart2D#PROPERTY_AXIS_X_TOP_REPLACE}</code> as code and
   * the old and new axis as values and have the change to change their peer to
   * listen on thus receiving the change events generated on the new axis. At
   * the moment the replace event is sent they will already have received the
   * event <code>{@link Chart2D#PROPERTY_AXIS_X}</code> event for the removal:
   * So be careful not to react on that first event by removing your listener
   * from the axis as you then will not receive the replace event!
   * <p>
   * 
   * @param axisX
   *          the top x axis to use.
   * 
   * @param position
   *          the index of the axis on top x dimension (starting from 0).
   * 
   * @return the previous axis on that bottom x position.
   */
  public IAxis<?> setAxisXTop(final AAxis<?> axisX, final int position) {
    IAxis<?> old = null;

    if (this.m_axesXTop.size() > position) {
      // this is a replace operation!
      old = this.m_axesXTop.get(position);
      this.removeAxisXTop(old);
      this.firePropertyChange(PROPERTY_AXIS_X_TOP_REPLACE, old, axisX);

    }
    // we can only transfer state (which includes adding traces after the new
    // axis is assigned to a chart!
    if (old != null) {
      this.internalTransferAxisState(old, axisX);
    }
    // add anyways in case no axis has been set before (see constructor)
    this.addAxisXTop(axisX);
    this.m_mouseTranslationXAxis = axisX;
    this.setRequestedRepaint(true);
    return old;
  }

  /**
   * Sets the first and only left y axis to use.
   * <p>
   * This is compatibility support for the API of jchart2d prior to 3.0.0 where
   * only one y axis was supported.
   * <p>
   * 
   * @deprecated use <code>{@link #setAxisYLeft(AAxis, int)}</code> instead.
   * 
   * @see #setAxisYLeft(AAxis, int)
   * 
   * @param axisY
   *          the first left y axis to use.
   * 
   * @return a copied List with the previous left y <code>{@link AAxis}</code>
   *         instance that was used at position 0.
   */
  @Deprecated
  public List<IAxis<?>> setAxisY(final AAxis<?> axisY) {
    List<IAxis<?>> axesLeft = new LinkedList<IAxis<?>>();
    for (IAxis<?> axis : this.m_axesYLeft) {
      // don't call remove here (concurrent modification exception), just
      // collect:
      axesLeft.add(axis);
    }

    List<IAxis<?>> axesRight = new LinkedList<IAxis<?>>();
    for (IAxis<?> axis : this.m_axesYRight) {
      // don't call remove here (concurrent modification exception), just
      // collect:
      axesRight.add(axis);
    }
    // now remove
    for (IAxis<?> axis : axesLeft) {
      this.removeAxisYLeft(axis);
    }
    for (IAxis<?> axis : axesRight) {
      this.removeAxisYRight(axis);
    }
    axesLeft.addAll(axesRight);
    return axesLeft;
  }

  /**
   * Sets the left y axis on the given position to use and replaces the old one
   * on that place.
   * <p>
   * This method delegates to <code>{@link #addAxisYLeft(AAxis)}</code> and also
   * uses <code>{@link #removeAxisYLeft(IAxis)}</code> in case an axis was
   * configured on the position. So the events
   * <code>{@link Chart2D#PROPERTY_AXIS_Y}</code> will be fired for remove and
   * add.
   * <p>
   * 
   * Furthermore this method uses "replace - semantics". The
   * <code>{@link ITrace2D}</code> instances contained in the previous left y
   * axis will be implanted to this new axis. Also the title and stuff like grid
   * settings will be transferred. For this an event with property
   * <code>{@link Chart2D#PROPERTY_AXIS_Y_LEFT_REPLACE} </code> is fired.
   * <p>
   * 
   * <b>Note</b> that <code>{@link PropertyChangeListener}s</code> of the axis will not be
   * transferred silently to the new axis but have to handle their unregistering
   * from the old axis / registering to the new axis from outside to give them
   * the chance to manage their state transitions by themselves.<br/>
   * Before the state of the old axis is transferred they will receive
   * <code>{@link PropertyChangeListener#propertyChange(PropertyChangeEvent)}</code>
   * with <code>{@link Chart2D#PROPERTY_AXIS_Y_LEFT_REPLACE}</code> as code and
   * the old and new axis as values and have the change to change their peer to
   * listen on thus receiving the change events generated on the new axis. At
   * the moment the replace event is sent they will already have received the
   * event <code>{@link Chart2D#PROPERTY_AXIS_Y}</code> event for the removal:
   * So be careful not to react on that first event by removing your listener
   * from the axis as you then will not receive the replace event!
   * <p>
   * 
   * @param axisY
   *          the left y axis to use.
   * 
   * @param position
   *          the index of the axis on left y dimension (starting from 0).
   * 
   * @return the previous axis on that bottom x position.
   */
  public IAxis<?> setAxisYLeft(final AAxis<?> axisY, final int position) {
    IAxis<?> old = null;
    if (this.m_axesYLeft.size() > position) {
      // this is a replace operation!
      old = this.m_axesYLeft.get(position);
      this.removeAxisYLeft(old);
      this.firePropertyChange(PROPERTY_AXIS_Y_LEFT_REPLACE, old, axisY);
    }
    /*
     * We can only add after removal or else the position argument would get
     * confused. Add anyways in case no axis has been set before (see
     * constructor)
     */
    
    this.addAxisYLeft(axisY);
    /*
     * We can only transfer state (which includes adding traces after the new
     * axis is assigned to a chart!
     */
    if (old != null) {
      this.internalTransferAxisState(old, axisY);
    }
    this.m_mouseTranslationYAxis = axisY;
    this.setRequestedRepaint(true);
    return old;
  }

  /**
   * Sets the right y axis on the given position to use and replaces the old one
   * on that place.
   * <p>
   * This method delegates to <code>{@link #addAxisYRight(AAxis)}</code> and
   * also uses <code>{@link #removeAxisYRight(IAxis)}</code> in case an axis was
   * configured on the position. So the events
   * <code>{@link Chart2D#PROPERTY_AXIS_Y}</code> will be fired for remove and
   * add.
   * <p>
   * 
   * Furthermore this method uses "replace - semantics". The
   * <code>{@link ITrace2D}</code> instances contained in the previous right y
   * axis will be implanted to this new axis. Also the title and stuff like grid
   * settings will be transferred. For this an event with property
   * <code>{@link Chart2D#PROPERTY_AXIS_Y_RIGHT_REPLACE} </code> is fired.
   * <p>
   * 
   * <b>Note</b> that <code>{@link PropertyChangeListener}s</code> of the axis will not be
   * transferred silently to the new axis but have to handle their unregistering
   * from the old axis / registering to the new axis from outside to give them
   * the chance to manage their state transitions by themselves.<br/>
   * Before the state of the old axis is transferred they will receive
   * <code>{@link PropertyChangeListener#propertyChange(PropertyChangeEvent)}</code>
   * with <code>{@link Chart2D#PROPERTY_AXIS_Y_RIGHT_REPLACE}</code> as code and
   * the old and new axis as values and have the change to change their peer to
   * listen on thus receiving the change events generated on the new axis. At
   * the moment the replace event is sent they will already have received the
   * event <code>{@link Chart2D#PROPERTY_AXIS_Y}</code> event for the removal:
   * So be careful not to react on that first event by removing your listener
   * from the axis as you then will not receive the replace event!
   * <p>
   * 
   * @param axisY
   *          the right y axis to use.
   * 
   * @param position
   *          the index of the axis on right y dimension (starting from 0).
   * 
   * @return the previous axis on that bottom x position.
   */

  public IAxis<?> setAxisYRight(final AAxis<?> axisY, final int position) {
    IAxis<?> old = null;
    if (this.m_axesYRight.size() > position) {
      // this is a replace operation!
      old = this.m_axesYRight.get(position);
      this.removeAxisYLeft(old);
      this.firePropertyChange(PROPERTY_AXIS_Y_RIGHT_REPLACE, old, axisY);
    }
    // add anyways in case no axis has been set before (see constructor)
    this.addAxisYRight(axisY);
    // we can only transfer state (which includes adding traces after the new
    // axis is assigned to a chart!
    if (old != null) {
      this.internalTransferAxisState(old, axisY);
    }
    this.m_mouseTranslationYAxis = axisY;
    this.setRequestedRepaint(true);
    return old;
  }

  /**
   * Set the grid color to use.
   * <p>
   * 
   * @param gridclr
   *          the grid color to use.
   */
  public final void setGridColor(final Color gridclr) {
    if (gridclr != null) {
      Color old = this.m_gridcolor;
      this.m_gridcolor = gridclr;
      if (!old.equals(this.m_gridcolor)) {
        this.firePropertyChange(Chart2D.PROPERTY_GRID_COLOR, old, this.m_gridcolor);
      }
      this.setRequestedRepaint(true);
    }
  }

  /**
   * Sets the ms to give a repaint operation time for collecting several repaint
   * requests into one (performance vs. update speed).
   * <p>
   * 
   * @param minPaintLatency
   *          the setting for the ms to give a repaint operation time for
   *          collecting several repaint requests into one (performance vs.
   *          update speed).
   */
  public synchronized void setMinPaintLatency(final int minPaintLatency) {
    this.m_minPaintLatency = minPaintLatency;
    this.m_repainter.setDelay(this.m_minPaintLatency);
  }

  /**
   * Decide whether labels for each chart are painted below it. If set to true
   * this will be done, else labels will be omitted.
   * <p>
   * 
   * @param paintLabels
   *          the value for paintLabels to set.
   */
  public void setPaintLabels(final boolean paintLabels) {
    final boolean change = this.m_paintLabels != paintLabels;
    this.m_paintLabels = paintLabels;
    if (change) {
      this.firePropertyChange(Chart2D.PROPERTY_PAINTLABELS, new Boolean(!paintLabels), new Boolean(
          paintLabels));
      this.setRequestedRepaint(true);
    }
  }

  /**
   * Sets the point finder used to find the nearest point corresponding to a
   * mouse event.
   * <p>
   * 
   * @see PointFinder#MANHATTAN
   * @see PointFinder#EUCLID
   * 
   * @param pointFinder
   *          the point finder used to find the nearest point corresponding to a
   *          mouse event.
   */
  public void setPointFinder(final IPointFinder pointFinder) {
    IPointFinder old = this.m_pointFinder;
    if (!this.m_pointFinder.equals(pointFinder)) {
      this.m_pointFinder = pointFinder;
      this.firePropertyChange(PROPERTY_POINTFINDER, old, this.m_pointFinder);
    }
  }

  /**
   * Sets the requestedRepaint.
   * <p>
   * Internal method to request a repaint that guarantees that two invocations
   * of <code></code> will always have at least have an interval of
   * <code>{@link Chart2D#getMinPaintLatency()}</code> ms.
   * <p>
   * Methods <code>{@link Chart2D#repaint()}, {@link Chart2D#repaint(long)}, 
   * {@link Chart2D#repaint(Rectangle)}, {@link Chart2D#repaint(int, int, int, int)} 
   * and {@link Chart2D#repaint(long, int, int, int, int)}</code> must not be
   * called from application code that has to inform the UI to update the chart
   * directly or a performance problem may arise as java awt / swing
   * implementation does not guarantee to collapse several repaint requests into
   * a single one but prefers to issue many paint invocations causing a high CPU
   * load in realtime scenarios (adding several 100 points per second to a
   * chart).
   * <p>
   * Only the internal timer may invoke the methods mentioned above.
   * <p>
   * 
   * @param requestedRepaint
   *          the requestedRepaint to set.
   */
  public final synchronized void setRequestedRepaint(final boolean requestedRepaint) {
    this.m_requestedRepaint = requestedRepaint;
  }

  /**
   * Sets the chart that will be synchronized for finding the start coordinate
   * of this chart to draw in x dimension ( <code>{@link #getXChartStart()}
   * </code>).
   * <p>
   * This feature is used to allow two separate charts to be painted stacked in
   * y dimension (one below the other) that have different x start coordinates
   * (because of different y labels that shift that value) with an equal
   * starting x value (thus be comparable visually if their x values match).
   * <p>
   * 
   * @param synchronizedXStartChart
   *          the chart that will be synchronized for finding the start
   *          coordinate of this chart to draw in x dimension (<code>
   *          {@link #getXChartStart()}</code>).
   */
  public synchronized void setSynchronizedXStartChart(final Chart2D synchronizedXStartChart) {
    this.m_synchronizedXStartChart = synchronizedXStartChart;
    this.m_synchronizedXStart = false;
    synchronized (synchronizedXStartChart) {
      synchronizedXStartChart.m_synchronizedXStart = true;
    }
  }

  /**
   * Set whether this component should display the chart coordinates as a tool
   * tip.
   * <p>
   * This turns on tool tip support (like
   * {@link javax.swing.JComponent#setToolTipText(java.lang.String)}) if
   * neccessary.
   * <p>
   * 
   * @deprecated use <code> {@link #setToolTipType(IToolTipType)} </code> with
   *             <code>{@link ToolTipType#DATAVALUES}</code> instead.
   * @param toolTipCoords
   *          The toolTipCoords to set.
   */
  @Deprecated
  public final void setToolTipCoords(final boolean toolTipCoords) {
    if (toolTipCoords) {
      this.setToolTipType(Chart2D.ToolTipType.DATAVALUES);
    } else {
      this.setToolTipType(Chart2D.ToolTipType.NONE);
    }
  }

  /**
   * Sets the type of tool tip to use.
   * <p>
   * Use <code>{@link ToolTipType#NONE}</code> to turn of tool tips.
   * <p>
   * 
   * @param toolTipType
   *          one of the available <code>{@link ToolTipType}</code> constants.
   * 
   * @see Chart2D.ToolTipType#DATAVALUES
   * @see Chart2D.ToolTipType#NONE
   * @see Chart2D.ToolTipType#PIXEL
   * @see Chart2D.ToolTipType#VALUE_SNAP_TO_TRACEPOINTS
   */
  public final void setToolTipType(final IToolTipType toolTipType) {
    if (toolTipType == Chart2D.ToolTipType.NONE) {
      // this is the hidden "unregister for tooltips trick".
      this.setToolTipText(null);
    } else {
      // this turns on tooltips (awt).
      this.setToolTipText("turnOn");
    }
    IToolTipType old = this.m_toolTip;
    this.m_toolTip = toolTipType;
    this.firePropertyChange(Chart2D.PROPERTY_TOOLTIP_TYPE, old, this.m_toolTip);
  }

  /**
   * Sets the trace point creator of this chart.
   * <p>
   * Null assignment attempts will raise an <code>{@link AssertionError}</code>.
   * <p>
   * 
   * @param tracePointProvider
   *          the trace point creator of this chart to set.
   */
  public void setTracePointProvider(final ITracePointProvider tracePointProvider) {
    assert (tracePointProvider != null);
    this.m_tracePointProvider = tracePointProvider;
  }

  /**
   * Sets whether antialiasing is used.
   * <p>
   * 
   * @param useAntialiasing
   *          true if antialiasing should be used.
   */
  public final void setUseAntialiasing(final boolean useAntialiasing) {
    if (this.m_useAntialiasing != useAntialiasing) {
      boolean oldstate = this.m_useAntialiasing;
      this.m_useAntialiasing = useAntialiasing;
      this.firePropertyChange(Chart2D.PROPERTY_ANTIALIASING_ENABLED, oldstate,
          this.m_useAntialiasing);

    }
  }

  /**
   * Returns a BufferedImage with the current width and height of the chart
   * filled with the Chart2D's graphics that may be written to a file or
   * OutputStream by using:
   * {@link javax.imageio.ImageIO#write(java.awt.image.RenderedImage, java.lang.String, java.io.File)}
   * .
   * <p>
   * If the width and height of this chart is zero (this happens when the chart
   * has not been {@link javax.swing.JComponent#setVisible(boolean)}, the chart
   * was not integrated into layout correctly or the chart's dimenision was set
   * to this value, a default of width 600 and height 400 will temporarily be
   * set (syncrhonized), the image will be rendered, the old dimension will be
   * reset and the image will be returned.<br/>
   * If you want to paint offscreen images (without displayed chart) prefer
   * invoke {@link #snapShot(int, int)} instead.
   * <p>
   * 
   * @return a BufferedImage of the Chart2D's graphics that may be written to a
   *         file or OutputStream.
   * @since 1.03 - please download versions equal or greater than
   *        jchart2d-1.03.jar.
   */
  public BufferedImage snapShot() {
    int width = this.getWidth();
    int height = this.getHeight();
    if (width <= 0 && height <= 0) {
      width = 600;
      height = 400;
    }
    return this.snapShot(width, height);
  }

  /**
   * Returns a BufferedImage with the given width and height that is filled with
   * tChart2D's graphics that may be written to a file or OutputStream by using:
   * {@link javax.imageio.ImageIO#write(java.awt.image.RenderedImage, java.lang.String, java.io.File)}
   * .
   * <p>
   * 
   * @param width
   *          the width of the image to create.
   * @param height
   *          the height of the image to create.
   * @return a BufferedImage of the Chart2D's graphics that may be written to a
   *         file or OutputStream.
   * @since 1.03 - please download versions equal or greater than
   *        jchart2d-1.03.jar.
   */
  public BufferedImage snapShot(final int width, final int height) {

    synchronized (this) {
      Dimension dsave = new Dimension(this.getWidth(), this.getHeight());
      this.setSize(new Dimension(width, height));
      BufferedImage img;
      img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = (Graphics2D) img.getGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      this.paint(g2d);
      this.setSize(dsave);
      return img;
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String result = super.toString();
    return result;
  }

  /**
   * Internal helper to track the amount of highlighters of all traces to manage
   * the enablement/disablement of the (expensive) highlighting feature.
   * <p>
   * 
   * Note that this method has to be called in synchronized state from
   * addTrace/removeTrace methods.
   * <p>
   * 
   * @param addedOrRemovedTraceHighlighters
   *          the amount of added or removed (negative) trace highlighters.
   */
  private void trackHighlightingEnablement(final int addedOrRemovedTraceHighlighters) {
    if (this.m_traceHighlighterCount <= 0) {
      if (addedOrRemovedTraceHighlighters > 0) {
        this.enablePointHighlighting(true);
      }
    } else {
      if ((addedOrRemovedTraceHighlighters < 0)
          && (Math.abs(addedOrRemovedTraceHighlighters) >= this.m_traceHighlighterCount)) {
        this.enablePointHighlighting(false);
      }
    }
    this.m_traceHighlighterCount += addedOrRemovedTraceHighlighters;
    if (this.m_traceHighlighterCount < 0) {
      System.err.println("Internal amount of point highlighters below zero: "
          + this.m_traceHighlighterCount);
    }
  }

  /**
   * Returns the translation of the mouse event coordinates of the given mouse
   * event to the value within the chart.
   * <p>
   * Note that the mouse event has to be an event fired on this component!
   * <p>
   * Note that the returned tracepoint is not a real trace point of a trace but
   * just used as a container here.
   * <p>
   * 
   * @deprecated this method is a candidate for wrong behavior when using
   *             multiple axes.
   * 
   * @param mouseEvent
   *          a mouse event that has been fired on this component.
   * @return the translation of the mouse event coordinates of the given mouse
   *         event to the value within the chart or null if no calculations
   *         could be performed as the chart was not painted before.
   * @throws IllegalArgumentException
   *           if the given mouse event does not belong to this component.
   */
  @Deprecated
  public ITracePoint2D translateMousePosition(final MouseEvent mouseEvent)
      throws IllegalArgumentException {
    if (mouseEvent.getSource() != this) {
      throw new IllegalArgumentException(
          "The given mouse event does not belong to this chart but to: " + mouseEvent.getSource());
    }
    ITracePoint2D result = null;
    double valueX = this.m_mouseTranslationXAxis.translateMousePosition(mouseEvent);
    double valueY = this.m_mouseTranslationYAxis.translateMousePosition(mouseEvent);
    result = this.m_tracePointProvider.createTracePoint(valueX, valueY);

    return result;
  }

  /**
   * Helper that removes this chart as a listener from the required property
   * change events.
   * <p>
   * 
   * @param removedAxis
   *          the axis to not listen to any more.
   */
  private void unlistenToAxis(final IAxis<?> removedAxis) {
    removedAxis.removePropertyChangeListener(IAxis.PROPERTY_ADD_REMOVE_TRACE, this);
    removedAxis.removePropertyChangeListener(IAxis.PROPERTY_LABELFORMATTER, this);
    removedAxis.removePropertyChangeListener(IAxis.PROPERTY_PAINTGRID, this);
    removedAxis.removePropertyChangeListener(IAxis.PROPERTY_RANGEPOLICY, this);
    removedAxis.removePropertyChangeListener(IAxis.PROPERTY_AXIS_SCALE_POLICY_CHANGED, this);
  }

  /**
   * Helper that removes this chart as a listener from the required property
   * change events.
   * <p>
   * 
   * @param removedTrace
   *          the trace to not listen to any more.
   */
  private void unlistenToTrace(final ITrace2D removedTrace) {
    removedTrace.removePropertyChangeListener(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED,
        this.m_pointHighlightListener);
    removedTrace.removePropertyChangeListener(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, this);
  }

  /**
   * Compares wether the bounds since last invocation have changed and
   * conditionally rescales the internal <code>{@link TracePoint2D}</code>
   * instances.
   * <p>
   * Must only be called from <code>{@link #paint(Graphics)}</code>.
   * <p>
   * The old recorded values for the bounds are set to the actual values
   * afterwards to allow detection of future changes again.
   * <p>
   * The force argument allows to enforce rescaling even if no change of data
   * bounds took place since the last scaling. This is useful if e.g. the view
   * upon the data is changed by a constraint (e.g.
   * {@link info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport}
   * ).
   * <p>
   * 
   * @param force
   *          if true no detection of changes of the data bounds as described
   *          above are performed: Rescaling is done unconditional.
   */
  private synchronized void updateScaling(final boolean force) {

    IAxis<?> currentAxis;
    // 1) bottom x axes:
    Iterator<IAxis<?>> it = this.m_axesXBottom.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      boolean changed = force;
      changed = changed || currentAxis.isDirtyScaling();
      if (changed) {
        currentAxis.initPaintIteration();
        currentAxis.scale();
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: Scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      } else {
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: No scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      }
    }
    // 2) top x axes:
    it = this.m_axesXTop.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      boolean changed = force;
      changed = changed || currentAxis.isDirtyScaling();
      if (changed) {
        currentAxis.initPaintIteration();
        currentAxis.scale();
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: Scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      } else {
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: No scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      }
    }
    // 3) left y axes:
    it = this.m_axesYLeft.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      boolean changed = force;
      changed = changed || currentAxis.isDirtyScaling();
      if (changed) {
        currentAxis.initPaintIteration();
        currentAxis.scale();
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: Scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      } else {
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: No scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      }
    }
    // 4) right y axes:
    it = this.m_axesYRight.iterator();
    while (it.hasNext()) {
      currentAxis = it.next();
      boolean changed = force;
      changed = changed || currentAxis.isDirtyScaling();
      if (changed) {
        currentAxis.initPaintIteration();
        currentAxis.scale();
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: Scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      } else {
        if (Chart2D.DEBUG_SCALING) {
          System.out.println("updateScaling: No scaling was performend for axis: "
              + currentAxis.getAxisTitle().getTitle());
        }
      }
    }
  }

}
