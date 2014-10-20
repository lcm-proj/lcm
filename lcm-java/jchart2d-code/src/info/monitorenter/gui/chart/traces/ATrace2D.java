/*
 *  ATrace2D.java , base class for ITrace2D implementations of jchart2d.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart.traces;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.IPointPainter;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.ITracePointProvider;
import info.monitorenter.gui.chart.traces.painters.TracePainterPolyline;
import info.monitorenter.util.SerializationUtility;
import info.monitorenter.util.StringUtil;
import info.monitorenter.util.math.MathUtil;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

/**
 * The abstract basic implementation of
 * <code>{@link info.monitorenter.gui.chart.ITrace2D}</code> that provides the
 * major amount of aspects needed in order to work correctly together with
 * <code>{@link info.monitorenter.gui.chart.Chart2D}</code>.
 * <p>
 * Caching of minimum and maximum bounds, property change support, the complex
 * z-Index handling (incorporates calls to internals of <code>Chart2D</code>,
 * default naming, bound management and event handling are covered here.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.75 $
 */
public abstract class ATrace2D implements ITrace2D, Comparable<ITrace2D> {

  /**
   * Instance counter for read-access in subclasses.
   */
  private static int instanceCount = 0;

  /** Generated <code>serialVersionUID</code>. * */
  private static final long serialVersionUID = -3955095612824507919L;

  /**
   * Returns the instanceCount for all <code>ATrace2D</code> subclasses.
   * 
   * @return Returns the instanceCount for all <code>ATrace2D</code> subclasses.
   */
  public static int getInstanceCount() {
    return ATrace2D.instanceCount;
  }

  /**
   * {@link javax.swing.event.ChangeListener} instances (mainly
   * <code>Char2D</code> instances that are interested in changes of internal
   * <code>ITracePoint2D</code> instances.
   */
  private final List<ChangeListener> m_changeListeners = new LinkedList<ChangeListener>();

  /** The color property. */
  private Color m_color = Color.black;

  /** The list of traces that compute their values from this trace. */
  protected List<ITrace2D> m_computingTraces = new LinkedList<ITrace2D>();

  /** The internal set of the error bar policies to use. */
  private Set<IErrorBarPolicy< ? >> m_errorBarPolicies = new TreeSet<IErrorBarPolicy< ? >>();

  /**
   * Needed for special treatment of cached values in case of empty state (no
   * points).
   */
  private boolean m_firsttime = true;

  /**
   * Cached maximum x value for performance improvement.
   */
  protected double m_maxX;

  /**
   * Cached maximum x value with error bar extension for performance
   * improvement.
   */
  protected double m_maxXErrorBar = -Double.MAX_VALUE;

  /**
   * Cached maximum y value for performance improvement.
   */
  protected double m_maxY;

  /**
   * Cached maximum y value with error bar extension for performance
   * improvement.
   */
  protected double m_maxYErrorBar = -Double.MAX_VALUE;

  /**
   * Cached minimum x value for performance improvement.
   */
  protected double m_minX;

  /**
   * Cached minimum x value with error bar extension for performance
   * improvement.
   */
  protected double m_minXErrorBar = Double.MAX_VALUE;

  /**
   * Cached minimum y value for performance improvement.
   */
  protected double m_minY;

  /**
   * Cached minimum y value with error bar extension for performance
   * improvement.
   */
  protected double m_minYErrorBar = Double.MAX_VALUE;

  /**
   * The name property.
   */
  protected String m_name = "";

  /**
   * The physical unit property for x dimension.
   */
  protected String m_physicalUnitsX = "";

  /**
   * The physical unit property for x dimension.
   */
  protected String m_physicalUnitsY = "";

  /** The internal set of point highlighters to use. */
  private Set<IPointPainter< ? >> m_pointHighlighters;

  /**
   * The instance that add support for firing <code>PropertyChangeEvents</code>
   * and maintaining <code>PropertyChangeListeners</code>.
   * <p>
   */
  protected PropertyChangeSupport m_propertyChangeSupport = new SwingPropertyChangeSupport(this);

  /**
   * The <code>Chart2D</code> this trace is added to. Needed for
   * synchronization.
   */
  protected Object m_renderer = Boolean.FALSE;

  /**
   * The stroke property.
   */
  private transient Stroke m_stroke;

  /** The internal set of trace painters to use. */
  private Set<ITracePainter< ? >> m_tracePainters;

  /**
   * The visible property.
   */
  private boolean m_visible = true;

  /**
   * The zIndex property.
   */
  private Integer m_zIndex = Integer.valueOf(ITrace2D.Z_INDEX_MIN + ATrace2D.instanceCount);

  /**
   * Defcon.
   * <p>
   */
  public ATrace2D() {
    super();
    ATrace2D.instanceCount++;
    this.m_tracePainters = new LinkedHashSet<ITracePainter< ? >>();
    this.m_tracePainters.add(new TracePainterPolyline());
    this.m_pointHighlighters = new LinkedHashSet<IPointPainter< ? >>();
    this.m_stroke = new BasicStroke(1f);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addComputingTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public void addComputingTrace(final ITrace2D trace) {
    this.m_computingTraces.add(trace);
  }

  /**
   * Ensures that no deadlock due to a missing internal chart reference may
   * occur.
   * 
   * @throws IllegalStateException
   *           if this trace is not assigned to a chart.
   * 
   */
  protected final void ensureInitialized() {
    if (this.m_renderer == Boolean.FALSE) {
      throw new IllegalStateException("Connect this trace (" + this.getName()
          + ") to a chart first before this operation (undebuggable deadlocks might occur else)");
    } else {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(this.getClass() + "(" + Thread.currentThread().getName()
            + ") this.m_renderer: " + this.m_renderer);
      }
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addErrorBarPolicy(info.monitorenter.gui.chart.IErrorBarPolicy)
   */
  public final boolean addErrorBarPolicy(final IErrorBarPolicy< ? > errorBarPolicy) {
    boolean result = false;
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("addErrorBarPolicy, 0 locks");
    }
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("addErrorBarPolicy, 1 lock");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("addErrorBarPolicy, 2 locks");
        }
        result = this.m_errorBarPolicies.add(errorBarPolicy);
        if (result) {
          errorBarPolicy.setTrace(this);
          errorBarPolicy.addPropertyChangeListener(IErrorBarPolicy.PROPERTY_CONFIGURATION, this);
          this.expandErrorBarBounds();
          this.firePropertyChange(ITrace2D.PROPERTY_ERRORBARPOLICY, null, errorBarPolicy);
        }
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addPoint(double, double)
   */
  public final boolean addPoint(final double x, final double y) {

    boolean result = false;
    ITracePoint2D p = null;
    final Chart2D chart = this.getRenderer();
    if (chart != null) {
      final ITracePointProvider pointProvider = chart.getTracePointProvider();
      if (pointProvider != null) {
        p = pointProvider.createTracePoint(x, y);
      }
    }
    if (p != null) {
      result = this.addPoint(p);
    } else {
      // TODO: write a logging here?
      final RuntimeException ex = new IllegalStateException(
          "Unable to addPoint because trace is not assigned to chart yet. Call chart.addTrace(trace) before!!!");
      throw ex;
    }
    return result;
  }

  /**
   * Add the given point to this <code>ITrace2D</code>.
   * <p>
   * This implementation performs caching of minimum and maximum values for x
   * and y and the delegates to {@link #addPointInternal(ITracePoint2D)} that
   * has to perform the "real" add operation.
   * <p>
   * Property change events are fired as described in method
   * <code>{@link #firePointAdded(ITracePoint2D)}</code>.
   * <p>
   * 
   * @see #firePointChanged(ITracePoint2D, int)
   * @param p
   *          the <code>TracePoint2D</code> to add.
   * @return true if the operation was successful, false else.
   */
  public final boolean addPoint(final ITracePoint2D p) {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName() + ", ATrace2D.addPoint, 0 locks");
    }
    boolean accepted = false;
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        if (!(this.m_renderer instanceof Chart2D)) {
          throw new RuntimeException(
              "Call chart.setTrace(trace) first before adding points or you might run into deadlocks!");
        }
        System.out.println(Thread.currentThread().getName() + ", ATrace2D.addPoint, 1 lock");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println(Thread.currentThread().getName() + ", ATrace2D.addPoint, 2 locks");
        }
        accepted = this.addPointInternal(p);
        if (accepted) {
          // fires property changes for max/min x/y:
          this.expandErrorBarBounds();
          // min max bounds exceeded?
          this.firePointAdded(p);
          p.setListener(this);
          // inform computing traces:
          if (this.m_computingTraces.size() > 0) {
            final Iterator<ITrace2D> it = this.m_computingTraces.iterator();
            ITrace2D trace;
            while (it.hasNext()) {
              trace = it.next();
              trace.addPoint(p);
            }
          }
        }
        if (this.m_firsttime) {
          // MAX events / members are done already from the
          // firePointAdded()->firePointChanged() method,
          // this is only the special case that a new point also marks the
          // minimum.
          // Don't move this code block before the firePointAdded or
          // the minimum of the chart will be higher than the maximum
          // which causes an infinite loop in AxisAutoUnit!
          this.m_minX = p.getX();
          this.m_minY = p.getY();
          this.m_maxX = p.getX();
          this.m_maxY = p.getY();
          final Double zero = new Double(0);
          this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, zero, new Double(this.m_minX));
          this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, zero, new Double(this.m_minY));

          this.m_firsttime = false;
        }

      }
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName()
            + ", ATrace2D.addPoint, freed 1 lock,  1 lock remaining.");
      }

    }
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName()
          + ", ATrace2D.addPoint, freed 1 lock,  0 locks remaining.");
    }
    return accepted;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addPointHighlighter(info.monitorenter.gui.chart.IPointPainter)
   */
  public boolean addPointHighlighter(final IPointPainter< ? > highlighter) {
    boolean result = false;
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        result = this.m_pointHighlighters.add(highlighter);
        if (result) {
          this.firePropertyChange(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, null, highlighter);
        }
      }
    }
    return result;
  }

  /**
   * <p>
   * Override this template method for the custom add operation that depends on
   * the policies of the implementation.
   * </p>
   * <p>
   * No property change events have to be fired by default. If this method
   * returns <code>true</code> the outer logic of the calling method
   * <code>{@link #addPoint(ITracePoint2D)}</code> will perform bound checks for
   * the new point and fire property changes as described in method
   * <code>{@link #firePointChanged(ITracePoint2D, int)}</code>.
   * </p>
   * <p>
   * In special cases - when additional modifications to the internal set of
   * points take place (e.g. a further point gets removed) this method should
   * return false (regardless whether the new point was accepted or not) and
   * perform bound checks and fire the property changes as mentioned above
   * "manually".
   * </p>
   * 
   * @param p
   *          the point to add.
   * @return true if the given point was accepted or false if not.
   */
  protected abstract boolean addPointInternal(ITracePoint2D p);

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public final void addPropertyChangeListener(final String propertyName,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public boolean addTracePainter(final ITracePainter< ? > painter) {
    boolean result = false;
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        result = this.m_tracePainters.add(painter);
        if (result) {
          this.firePropertyChange(ITrace2D.PROPERTY_PAINTERS, null, painter);
        }
      }
    }
    return result;
  }

  /**
   * @param o
   *          the trace to compare to.
   * @return see interface.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public final int compareTo(final ITrace2D o) {
    // compare by z-index
    int result = this.getZIndex().compareTo(o.getZIndex());
    // for equal z-indices be more fine grained or else within a set traces
    // would get lost!
    if (result == 0) {
      final int me = this.hashCode();
      final int it = o.hashCode();
      result = me - it;
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#containsTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public boolean containsTracePainter(final ITracePainter< ? > painter) {
    return this.m_tracePainters.contains(painter);
  }

 

  /**
   * Internally expands all bounds according to potential error bars.
   */
  private void expandErrorBarBounds() {
    final boolean requiresErrorBarCalculation = !this.isEmpty();
    if (requiresErrorBarCalculation) {
      boolean change;
      this.ensureInitialized();
      synchronized (this.m_renderer) {
        synchronized (this) {
          if (this.showsPositiveXErrorBars()) {
            change = this.expandMaxXErrorBarBounds();
            if (change) {
              this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, this, new Double(this.getMaxX()));
            }
          } else {
            if (this.m_maxXErrorBar != -Double.MAX_VALUE) {
              this.m_maxXErrorBar = -Double.MAX_VALUE;
              this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, this, new Double(this.getMaxX()));
            }
          }
          if (this.showsPositiveYErrorBars()) {
            change = this.expandMaxYErrorBarBounds();
            if (change) {
              this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, this, new Double(this.getMaxY()));
            }
          } else {
            if (this.m_maxYErrorBar != -Double.MAX_VALUE) {
              this.m_maxYErrorBar = -Double.MAX_VALUE;
              this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, this, new Double(this.getMaxY()));
            }
          }
          if (this.showsNegativeXErrorBars()) {
            change = this.expandMinXErrorBarBounds();
            if (change) {
              this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, this, new Double(this.getMinX()));
            }
          } else {
            if (this.m_minXErrorBar != Double.MAX_VALUE) {
              this.m_minXErrorBar = Double.MAX_VALUE;
              this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, this, new Double(this.getMinX()));
            }
          }
          if (this.showsNegativeYErrorBars()) {
            change = this.expandMinYErrorBarBounds();
            if (change) {
              this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, this, new Double(this.getMinY()));
            }
          } else {
            if (this.m_minYErrorBar != Double.MAX_VALUE) {
              this.m_minYErrorBar = Double.MAX_VALUE;
              this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, this, new Double(this.getMinY()));
            }
          }
        }
      }
    }
  }

  /**
   * Internally takes into account that in case of error bars to render the
   * maximum x value will be different.
   * <p>
   * Returns true if a change to <code>{@link #getMaxX()}</code> was done.
   * <p>
   * 
   * @return true if a change to <code>{@link #getMaxX()}</code> was done.
   */
  private boolean expandMaxXErrorBarBounds() {
    final Chart2D chart = this.getRenderer();
    boolean change = false;
    double errorBarMaxXCollect = -Double.MAX_VALUE;
    if (chart != null) {
      double errorBarMaxX = -Double.MAX_VALUE;
      for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
        if (errorBarPolicy.isShowPositiveXErrors()) {
          // was it turned on?
          errorBarMaxX = errorBarPolicy.getXError(this.m_maxX);
          if (errorBarMaxX > errorBarMaxXCollect) {
            errorBarMaxXCollect = errorBarMaxX;
          }
        }
      }
    }
    final double absoluteMax = errorBarMaxXCollect + this.m_maxX;
    if (!MathUtil.assertEqual(this.m_maxXErrorBar, absoluteMax, 0.00000001)) {
      this.m_maxXErrorBar = absoluteMax;
      change = true;
    }
    return change;
  }

  /**
   * Internally takes into account that in case of error bars to render the
   * maximum y value will be different.
   * <p>
   * Returns true if a change to <code>{@link #getMaxY()}</code> was done.
   * <p>
   * 
   * @return true if a change to <code>{@link #getMaxY()}</code> was done.
   */
  private boolean expandMaxYErrorBarBounds() {
    final Chart2D chart = this.getRenderer();
    boolean change = false;
    double errorBarMaxYCollect = -Double.MAX_VALUE;
    if (chart != null) {
      double errorBarMaxY = -Double.MAX_VALUE;
      for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
        if (errorBarPolicy.isShowPositiveYErrors()) {
          errorBarMaxY = errorBarPolicy.getYError(this.m_maxY);
          if (errorBarMaxY > errorBarMaxYCollect) {
            errorBarMaxYCollect = errorBarMaxY;
          }
        }
      }
    }
    final double absoluteMax = errorBarMaxYCollect + this.m_maxY;
    if (!MathUtil.assertEqual(this.m_maxYErrorBar, absoluteMax, 0.00000001)) {
      this.m_maxYErrorBar = absoluteMax;
      change = true;
    }
    return change;

  }

  /**
   * Internally takes into account that in case of error bars to render the
   * minimum x value will be different.
   * <p>
   * Returns true if a change to <code>{@link #getMinX()}</code> was done.
   * <p>
   * 
   * @return true if a change to <code>{@link #getMinX()}</code> was done.
   */
  private boolean expandMinXErrorBarBounds() {
    final Chart2D chart = this.getRenderer();
    boolean change = false;
    double errorBarMinXCollect = -Double.MAX_VALUE;
    if (chart != null) {
      double errorBarMinX = -Double.MAX_VALUE;
      for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
        if (errorBarPolicy.isShowNegativeXErrors()) {
          errorBarMinX = errorBarPolicy.getXError(this.m_minX);
          if (errorBarMinX > errorBarMinXCollect) {
            errorBarMinXCollect = errorBarMinX;
          }
        }
      }
    }
    final double absoluteMin = this.m_minX - errorBarMinXCollect;
    if (!MathUtil.assertEqual(this.m_minXErrorBar, absoluteMin, 0.00000001)) {
      this.m_minXErrorBar = absoluteMin;
      change = true;
    }
    return change;
  }

  /**
   * Internally takes into account that in case of error bars to render the
   * minimum y value will be different.
   * <p>
   * Returns true if a change to <code>{@link #getMinY()}</code> was done.
   * <p>
   * 
   * @return true if a change to <code>{@link #getMinY()}</code> was done.
   */
  private boolean expandMinYErrorBarBounds() {
    final Chart2D chart = this.getRenderer();
    boolean change = false;
    double errorBarMinYCollect = -Double.MAX_VALUE;
    if (chart != null) {
      double errorBarMinY = -Double.MAX_VALUE;
      for (final IErrorBarPolicy< ? > errorBarPolicy : this.getErrorBarPolicies()) {
        if (errorBarPolicy.isShowNegativeYErrors()) {
          // calculate the error
          errorBarMinY = errorBarPolicy.getYError(this.m_minY);
          if (errorBarMinY > errorBarMinYCollect) {
            errorBarMinYCollect = errorBarMinY;
          }
        }
      }
    }
    final double absoluteMin = this.m_minY - errorBarMinYCollect;
    if (!MathUtil.assertEqual(this.m_minYErrorBar, absoluteMin, 0.00000001)) {
      this.m_minYErrorBar = absoluteMin;
      change = true;
    }
    return change;
  }

  /**
   * Decreases internal instance count by one.
   * <p>
   * 
   * @throws Throwable
   *           if something goes wrong.
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    ATrace2D.instanceCount--;
  }

  /**
   * Fire property change events related to an added point.
   * <p>
   * A property change event for property
   * <code>{@link ITrace2D#PROPERTY_TRACEPOINT}</code> with null as the old
   * value and the new point as the new value is fired. This allows e.g.
   * rescaling of those instances (instead of having to rescale a whole trace).
   * <p>
   * Additionally before this property change, property change events for bounds
   * are fired as described in method
   * <code>{@link #firePointChanged(ITracePoint2D, int)}</code>.
   * <p>
   * 
   * @param added
   *          the point that was added.
   */
  protected void firePointAdded(final ITracePoint2D added) {
    this.firePointChanged(added, ITracePoint2D.STATE_ADDED);
    this.firePropertyChange(ITrace2D.PROPERTY_TRACEPOINT, null, added);
  }

  /**
   * Method triggered by <code>{@link ITracePoint2D#setLocation(double, double)}
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
   * If <code>state</code> is <code>{@link ITracePoint2D#STATE_CHANGED}</code> a
   * property change event with
   * <code>{@link ITrace2D#PROPERTY_POINT_CHANGED}</code> will be fired to all
   * listeners.
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
  public void firePointChanged(final ITracePoint2D changed, final int state) {
    double tmpx = changed.getX();
    double tmpy = changed.getY();
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        // for a changed point all cases (new extremum as for added case, other
        // point becomes extremum as the change point was one like in removed
        // case) have
        // to be tested. Additionally we have to fire a changd point event.
        if (ITracePoint2D.STATE_ADDED == state) {
          // add
          if (tmpx > this.m_maxX) {
            this.m_maxX = tmpx;
            this.expandMaxXErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, null, new Double(this.m_maxX));
          } else if (tmpx < this.m_minX) {
            this.m_minX = tmpx;
            this.expandMinXErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, null, new Double(this.m_minX));
          }
          if (tmpy > this.m_maxY) {
            this.m_maxY = tmpy;
            this.expandMaxYErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, null, new Double(this.m_maxY));
          } else if (tmpy < this.m_minY) {
            this.m_minY = tmpy;
            this.expandMinYErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, null, new Double(this.m_minY));
          }
        }
        if (ITracePoint2D.STATE_REMOVED == state) {
          // removal: care for extrema (<=, >=)
          if (tmpx >= this.m_maxX) {
            tmpx = this.m_maxX;
            this.maxXSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(tmpx), new Double(
                this.m_maxX));
          } else if (tmpx <= this.m_minX) {
            tmpx = this.m_minX;
            this.minXSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(tmpx), new Double(
                this.m_minX));
          }
          if (tmpy >= this.m_maxY) {
            tmpy = this.m_maxY;
            this.maxYSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(tmpy), new Double(
                this.m_maxY));
          } else if (tmpy <= this.m_minY) {
            tmpy = this.m_minY;
            this.minYSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(tmpy), new Double(
                this.m_minY));
          }
          if (this.getSize() == 0) {
            this.m_firsttime = true;
          }
        }
        if (state == ITracePoint2D.STATE_CHANGED) {
          if (tmpx < this.m_maxX) {
            final double oldMaxX = this.m_maxX;
            this.maxXSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(oldMaxX), new Double(
                this.m_maxX));
          } else if (tmpx > this.m_maxX) {
            final double oldMaxX = this.m_maxX;
            this.m_maxX = tmpx;
            this.expandMaxXErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(oldMaxX), new Double(
                this.m_maxX));
          }
          if (tmpx > this.m_minX) {
            final double oldMinX = this.m_minX;
            this.minXSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(oldMinX), new Double(
                this.m_minX));
          } else if (tmpx < this.m_minX) {
            final double oldMinX = this.m_minX;
            this.m_minX = tmpx;
            this.expandMinXErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(oldMinX), new Double(
                this.m_minX));
          }
          if (tmpy < this.m_maxY) {
            final double oldMaxY = this.m_maxY;
            this.maxYSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(oldMaxY), new Double(
                this.m_maxY));
          } else if (tmpy > this.m_maxY) {
            final double oldMaxY = this.m_maxY;
            this.m_maxY = tmpy;
            this.expandMaxYErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(oldMaxY), new Double(
                this.m_maxY));
          }
          if (tmpy > this.m_minY) {
            final double oldMinY = this.m_minY;
            this.minYSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(oldMinY), new Double(
                this.m_minY));
          } else if (tmpy < this.m_minY) {
            final double oldMinY = this.m_minY;
            this.m_minY = tmpy;
            this.expandMinYErrorBarBounds();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(oldMinY), new Double(
                this.m_minY));
          }
          this.firePropertyChange(ITrace2D.PROPERTY_POINT_CHANGED, null, changed);
        }
      }
    }
  }

  /**
   * Fire property change events related to a removed point.
   * <p>
   * A property change event for property
   * <code>{@link ITrace2D#PROPERTY_TRACEPOINT}</code> with a point as the old
   * value and null as the new value is fired. This allows e.g. rescaling of
   * those instances (instead of having to rescale a whole trace).
   * <p>
   * Additionally before this property change, property change events for bounds
   * are fired as described in method
   * <code>{@link #firePointChanged(ITracePoint2D, int)}</code>.
   * <p>
   * 
   * @param removed
   *          the point that was removed.
   */
  protected void firePointRemoved(final ITracePoint2D removed) {
    this.firePointChanged(removed, ITracePoint2D.STATE_REMOVED);
    this.firePropertyChange(ITrace2D.PROPERTY_TRACEPOINT, removed, null);
  }

  /**
   * Fires a property change event to the registered listeners.
   * <p>
   * 
   * @param property
   *          one of the <code>PROPERTY_XXX</code> constants defined in <code>
   *          {@link ITrace2D}</code>.
   * @param oldvalue
   *          the old value of the property.
   * @param newvalue
   *          the new value of the property.
   */
  protected final void firePropertyChange(final String property, final Object oldvalue,
      final Object newvalue) {
    if (property.equals(ITrace2D.PROPERTY_MAX_X) || property.equals(ITrace2D.PROPERTY_MAX_Y)
        || property.equals(ITrace2D.PROPERTY_MIN_X) || property.equals(ITrace2D.PROPERTY_MIN_Y)
        || property.equals(ITrace2D.PROPERTY_TRACEPOINT)
        || property.equals(ITrace2D.PROPERTY_POINT_CHANGED)) {
      if (!Thread.holdsLock(this.m_renderer)) {
        throw new RuntimeException("Acquire a lock on the corresponding chart first!");
      }
      if (!Thread.holdsLock(this)) {
        throw new RuntimeException("Acquire a lock on this trace first!");
      }

      if (Chart2D.DEBUG_THREADING) {
        System.out.println("trace.firePropertyChange (" + property + "), 2 locks, renderer is: "
            + this.m_renderer);
      }
    }

    this.m_propertyChangeSupport.firePropertyChange(property, oldvalue, newvalue);
  }

  /**
   * Returns a shallow copied list of the change listeners of this instance.
   * <p>
   * 
   * @return a shallow copied list of the change listeners of this instance.
   */
  public List<ChangeListener> getChangeListeners() {
    return new LinkedList<ChangeListener>(this.m_changeListeners);
  }

  /**
   * Get the <code>Color</code> this trace will be painted with.
   * <p>
   * 
   * @return the <code>Color</code> of this instance
   */
  public final Color getColor() {
    return this.m_color;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getErrorBarPolicies()
   */
  public final Set<IErrorBarPolicy< ? >> getErrorBarPolicies() {
    return this.m_errorBarPolicies;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getHasErrorBars()
   */
  public final boolean getHasErrorBars() {
    boolean result = false;
    if (this.m_errorBarPolicies.size() > 0) {
      for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
        if (errorBarPolicy.getErrorBarPainters().size() > 0) {
          result |= errorBarPolicy.isShowNegativeXErrors();
          if (result) {
            break;
          }
          result |= errorBarPolicy.isShowPositiveXErrors();
          if (result) {
            break;
          }
          result |= errorBarPolicy.isShowNegativeYErrors();
          if (result) {
            break;
          }
          result |= errorBarPolicy.isShowPositiveYErrors();
          if (result) {
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns a label for this trace.
   * <p>
   * The label is constructed of
   * <ul>
   * <li>The name of this trace ({@link #getName()}).</li>
   * <li>The physical unit of this trace ({@link #getPhysicalUnits()}).</li>
   * </ul>
   * <p>
   * 
   * @return a label for this trace.
   * @see ITrace2D#getLabel()
   * @see #getName()
   * @see #getPhysicalUnits()
   */
  public final String getLabel() {
    String name = this.getName();
    final String physunit = this.getPhysicalUnits();
    if (!(StringUtil.isEmpty(name)) && (!StringUtil.isEmpty(physunit))) {
      name = new StringBuffer(name).append(" ").append(physunit).toString();
    } else if (!StringUtil.isEmpty(physunit)) {
      name = new StringBuffer("unnamed").append(" ").append(physunit).toString();
    }
    return name;
  }

  /**
   * Returns the original maximum x- value ignoring the offsetX.
   * <p>
   * 
   * @return the original maximum x- value ignoring the offsetX.
   */
  public final double getMaxX() {
    synchronized (this.m_renderer) {
      synchronized (this) {
        double result = this.m_maxX;
        if (this.m_maxXErrorBar != -Double.MAX_VALUE) {
          result = this.m_maxXErrorBar;
        }
        return result;
      }
    }
  }

  /**
   * Returns the original maximum y- value ignoring the offsetY.
   * <p>
   * 
   * @return the original maximum y- value ignoring the offsetY.
   */

  public final double getMaxY() {
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        double result = this.m_maxY;
        if (this.m_maxYErrorBar != -Double.MAX_VALUE) {
          result = this.m_maxYErrorBar;
        }
        return result;
      }
    }
  }

  /**
   * Returns the original minimum x- value ignoring the offsetX.
   * <p>
   * 
   * @return the original minimum x- value ignoring the offsetX.
   */
  public final double getMinX() {
    synchronized (this.m_renderer) {
      synchronized (this) {
        double result = this.m_minX;
        if (this.m_minXErrorBar != Double.MAX_VALUE) {
          result = this.m_minXErrorBar;
        }
        return result;
      }
    }
  }

  /**
   * Returns the original minimum y- value ignoring the offsetY.
   * <p>
   * 
   * @return the original minimum y- value ignoring the offsetY.
   */
  public final double getMinY() {
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        double result = this.m_minY;
        if (this.m_minYErrorBar != Double.MAX_VALUE) {
          result = this.m_minYErrorBar;
        }
        return result;
      }
    }
  }

  /**
   * Returns the name of this trace.
   * <p>
   * 
   * @return the name of this trace.
   * @see ITrace2D#getName()
   * @see #setName(String s)
   */
  public final String getName() {
    return this.m_name;
  }

  /**
   * Naive implementation that iterates over every point.
   * <p>
   * Subclasses that have more insight about their internal data storage could
   * override this with a faster implementation (e.g. if the points are kept in
   * a sorted order a skip - strategy) could find the minimum faster.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.ITrace2D#getNearestPointEuclid(double,
   *      double)
   */
  public DistancePoint getNearestPointEuclid(final double x, final double y) {
    final DistancePoint result = new DistancePoint();

    final Iterator<ITracePoint2D> it = this.iterator();
    ITracePoint2D point;
    double distance;
    double shortestDistance = Double.MAX_VALUE;
    while (it.hasNext()) {
      point = it.next();
      distance = point.getEuclidDistance(x, y);
      if (distance < shortestDistance) {
        shortestDistance = distance;
        result.setPoint(point);
        result.setDistance(shortestDistance);
      }
    }
    return result;
  }

  /**
   * Naive implementation that iterates over every point.
   * <p>
   * Subclasses that have more insight about their internal data storage could
   * override this with a faster implementation (e.g. if the points are kept in
   * a sorted order a skip - strategy could find the minimum faster.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.ITrace2D#getNearestPointManhattan(double,
   *      double)
   */
  public DistancePoint getNearestPointManhattan(final double x, final double y) {
    final DistancePoint result = new DistancePoint();

    final Iterator<ITracePoint2D> it = this.iterator();
    ITracePoint2D point;
    double manhattanDistance;
    double shortestManhattanDistance = Double.MAX_VALUE;
    while (it.hasNext()) {
      point = it.next();
      manhattanDistance = point.getManhattanDistance(x, y);
      if (manhattanDistance < shortestManhattanDistance) {
        shortestManhattanDistance = manhattanDistance;
        result.setPoint(point);
        result.setDistance(shortestManhattanDistance);
      }

    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPhysicalUnits()
   */
  public final String getPhysicalUnits() {
    String result;
    if (StringUtil.isEmpty(this.m_physicalUnitsX) && StringUtil.isEmpty(this.m_physicalUnitsY)) {
      result = "";
    } else {
      result = new StringBuffer("[x: ").append(this.getPhysicalUnitsX()).append(", y: ").append(
          this.getPhysicalUnitsY()).append("]").toString();

    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPhysicalUnitsX()
   */
  public final String getPhysicalUnitsX() {
    return this.m_physicalUnitsX;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPhysicalUnitsY()
   */
  public final String getPhysicalUnitsY() {
    return this.m_physicalUnitsY;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPointHighlighters()
   */
  public final Set<IPointPainter< ? >> getPointHighlighters() {

    return this.m_pointHighlighters;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPropertyChangeListeners(String)
   */
  public PropertyChangeListener[] getPropertyChangeListeners(final String property) {
    return this.m_propertyChangeSupport.getPropertyChangeListeners(property);
  }

  /**
   * Returns the chart that renders this instance or null, if this trace is not
   * added to a chart.
   * <p>
   * The chart that renders this trace registers itself with this trace in
   * {@link Chart2D#addTrace(ITrace2D)}.
   * <p>
   * 
   * @return Returns the renderer.
   * @see Chart2D#addTrace(ITrace2D)
   */
  public final Chart2D getRenderer() {
    Chart2D result = null;
    if (this.m_renderer instanceof Chart2D) {
      result = (Chart2D) this.m_renderer;
    }
    return result;
  }

  /**
   * Get the <code>Stroke</code> object this instance will be painted with.
   * <p>
   * 
   * @return the <code>Stroke</code> object this <code>ITrace2D</code> will be
   *         painted with.
   * @see info.monitorenter.gui.chart.ITrace2D#getStroke()
   */
  public final Stroke getStroke() {
    return this.m_stroke;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getTracePainters()
   */
  public final Set<ITracePainter< ? >> getTracePainters() {

    return this.m_tracePainters;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getZIndex()
   */
  public final Integer getZIndex() {
    // More expensive but the contract of the order of values described in the
    // interface is inverted to the contract of TreeSetGreedy.
    // This is done here instead of get/set ComparableProperty
    // as those are invoked several times for each iteration
    // (and paint contains several iterations).
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName() + ", " + this.getClass().getName()
          + ".getZindex, 0 locks");
    }
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName() + ", " + this.getClass().getName()
            + ".getZindex, 1 locks");
      }

      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println(Thread.currentThread().getName() + ", " + this.getClass().getName()
              + ".getZindex, 2 locks");
        }
      }
      if (Chart2D.DEBUG_THREADING) {
        System.out.println(Thread.currentThread().getName() + ", " + this.getClass().getName()
            + ".getZindex, freed 1 lock: 1 lock remaining");
      }
    }
    if (Chart2D.DEBUG_THREADING) {
      System.out.println(Thread.currentThread().getName() + ", " + this.getClass().getName()
          + ".getZindex, freed 1 lock: 0 locks remaining");
    }
    return this.m_zIndex;
  }

 
  /**
   * @see info.monitorenter.gui.chart.ITrace2D#isVisible()
   */
  public final boolean isVisible() {
    return this.m_visible;
  }

  /**
   * Internal search for the maximum x value that is only invoked if no cached
   * value is at hand or bounds have changed by adding new points.
   * <p>
   * The result is assigned to the property maxX.
   * <p>
   * 
   * @see #getMaxX()
   */
  protected void maxXSearch() {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("trace.maxXSearch, 0 locks");
    }

    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("trace.maxXSearch, 1 locks");
      }
      double ret = -Double.MAX_VALUE;
      ITracePoint2D tmpoint = null;
      double tmp;
      final Iterator<ITracePoint2D> it = this.iterator();
      while (it.hasNext()) {
        tmpoint = it.next();
        tmp = tmpoint.getX();
        if (tmp > ret) {
          ret = tmp;
        }
      }
      this.m_maxX = ret;
    }
    // compute the extra amount in case of error bar painters:
    this.expandMaxXErrorBarBounds();
  }

  /**
   * Internal search for the maximum y value that is only invoked if no cached
   * value is at hand or bounds have changed by adding new points.
   * <p>
   * The result is assigned to the property maxY.
   * <p>
   * 
   * @see #getMaxY()
   */
  protected void maxYSearch() {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("trace.maxYSearch, 0 locks");
    }

    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("trace.maxYSearch, 1 lock");
      }

      double ret = -Double.MAX_VALUE;
      ITracePoint2D tmpoint = null;
      double tmp;
      final Iterator<ITracePoint2D> it = this.iterator();
      while (it.hasNext()) {
        tmpoint = it.next();
        tmp = tmpoint.getY();
        if (tmp > ret) {
          ret = tmp;
        }
      }
      this.m_maxY = ret;
    }
    // compute the extra amount in case of error bar painters:
    this.expandMaxYErrorBarBounds();
  }

  /**
   * Internal search for the minimum x value that is only invoked if no cached
   * value is at hand or bounds have changed by adding new points.
   * <p>
   * The result is assigned to the property minX.
   * <p>
   * 
   * @see #getMinX()
   */
  protected void minXSearch() {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("trace.minXSearch, 0 locks");
    }

    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("trace.minXSearch, 1 locks");
      }

      double ret = Double.MAX_VALUE;
      ITracePoint2D tmpoint = null;
      double tmp;
      final Iterator<ITracePoint2D> it = this.iterator();
      while (it.hasNext()) {
        tmpoint = it.next();
        tmp = tmpoint.getX();
        if (tmp < ret) {
          ret = tmp;
        }
      }
      this.m_minX = ret;
    }
    // compute the extra amount in case of error bar painters:
    this.expandMinXErrorBarBounds();
  }

  /**
   * Internal search for the minimum y value that is only invoked if no cached
   * value is at hand or bounds have changed by adding new points.
   * <p>
   * The result is assigned to the property minY.
   * <p>
   * 
   * @see #getMinY()
   */
  protected void minYSearch() {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("trace.minYSearch, 0 locks");
    }

    synchronized (this) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("trace.minYSearch, 1 locks");
      }

      double ret = Double.MAX_VALUE;
      ITracePoint2D tmpoint = null;
      double tmp;
      final Iterator<ITracePoint2D> it = this.iterator();
      while (it.hasNext()) {
        tmpoint = it.next();
        tmp = tmpoint.getY();
        if (tmp < ret) {
          ret = tmp;
        }
      }
      this.m_minY = ret;

      // compute the extra amount in case of error bar painters:
      this.expandMinYErrorBarBounds();
    }

  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    if (IErrorBarPolicy.PROPERTY_CONFIGURATION.equals(evt.getPropertyName())) {
      this.expandErrorBarBounds();
      this
          .firePropertyChange(ITrace2D.PROPERTY_ERRORBARPOLICY_CONFIGURATION, null, evt.getSource());
    }

  }

  /**
   * Provides serialization support.
   * 
   * @param stream
   *          the input stream.
   * @throws IOException
   *           if there is an I/O error.
   * @throws ClassNotFoundException
   *           if there is a classpath problem.
   */
  private void readObject(final ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.defaultReadObject();
    this.m_stroke = SerializationUtility.readStroke(stream);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeAllPointHighlighters()
   */
  public Set<IPointPainter< ? >> removeAllPointHighlighters() {
    final Set<IPointPainter< ? >> result = new LinkedHashSet<IPointPainter< ? >>();
    for (final IPointPainter< ? > highlighter : this.getPointHighlighters()) {
      this.removePointHighlighter(highlighter);
      result.add(highlighter);
    }
    return result;
  }

  /**
   * Changes the internal state to empty to allow that the caching of bounds is
   * cleared and delegates the call to {@link #removeAllPointsInternal()}.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.ITrace2D#removeAllPoints()
   */
  public final void removeAllPoints() {
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {

        this.m_firsttime = true;
        this.removeAllPointsInternal();
        // property changes:
        double oldValue = this.m_maxX;
        this.m_maxX = 0;
        this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(oldValue), new Double(
            this.m_maxX));
        oldValue = this.m_maxY;
        this.m_maxY = 0;
        this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(oldValue), new Double(
            this.m_maxY));
        oldValue = this.m_minX;
        this.m_minX = 0;
        this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(oldValue), new Double(
            this.m_minX));
        oldValue = this.m_minY;
        this.m_minY = 0;
        this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(oldValue), new Double(
            this.m_minY));

        // inform computing traces:
        for (final ITrace2D trace : this.m_computingTraces) {
          trace.removeAllPoints();
        }
      }
    }
  }

  /**
   * Override this template method for the custom remove operation that depends
   * on the <code>Collection</code> used in the implementation.
   * <p>
   * No change events have to be fired, this is done by {@link ATrace2D}.
   * <p>
   */
  protected abstract void removeAllPointsInternal();

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeComputingTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public boolean removeComputingTrace(final ITrace2D trace) {
    final boolean result = this.m_computingTraces.remove(trace);
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeErrorBarPolicy(info.monitorenter.gui.chart.IErrorBarPolicy)
   */
  public boolean removeErrorBarPolicy(final IErrorBarPolicy< ? > errorBarPolicy) {
    boolean result = false;
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("addErrorBarPolicy, 0 locks");
    }
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("addErrorBarPolicy, 1 lock");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("addErrorBarPolicy, 2 locks");
        }

        result = this.m_errorBarPolicies.remove(errorBarPolicy);
        if (result) {
          errorBarPolicy.setTrace(null);
          errorBarPolicy.removePropertyChangeListener(IErrorBarPolicy.PROPERTY_CONFIGURATION, this);
          this.expandErrorBarBounds();
          this.firePropertyChange(ITrace2D.PROPERTY_ERRORBARPOLICY, errorBarPolicy, null);
        }
      }
    }
    return result;
  }

  /**
   * Remove the given point from this <code>ITrace2D</code>.
   * <p>
   * This implementation performs caching of minimum and maximum values for x
   * and y and the delegates to
   * <code>{@link #removePointInternal(ITracePoint2D)}</code> that has to
   * perform the "real" add remove operation.
   * <p>
   * Property change events are fired as described in method
   * <code>{@link #firePointRemoved(ITracePoint2D)}</code>.
   * <p>
   * 
   * @param point
   *          the <code>TracePoint2D</code> to remove.
   * @return true if the removal succeeded, false else: this could be that the
   *         given point was not contained.
   * @see #firePointChanged(ITracePoint2D, int)
   */
  public boolean removePoint(final ITracePoint2D point) {
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("removePoint, 0 locks");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("removePoint, 1 lock");
        }
        final ITracePoint2D removed = this.removePointInternal(point);
        if (removed != null) {

          double tmpx = removed.getX();
          double tmpy = removed.getY();
          // System.out.println("Trace2DLtd.addPoint() removed point!");
          if (tmpx >= this.m_maxX) {
            tmpx = this.m_maxX;
            this.maxXSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(tmpx), new Double(
                this.m_maxX));
          } else if (tmpx <= this.m_minX) {
            tmpx = this.m_minX;
            this.minXSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(tmpx), new Double(
                this.m_minX));
          }
          if (tmpy >= this.m_maxY) {
            tmpy = this.m_maxY;
            this.maxYSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(tmpy), new Double(
                this.m_maxY));
          } else if (tmpy <= this.m_minY) {
            tmpy = this.m_minY;
            this.minYSearch();
            this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(tmpy), new Double(
                this.m_minY));
          }

          this.firePointRemoved(removed);
          removed.setListener(null);
          // inform computing traces:
          for (final ITrace2D trace : this.m_computingTraces) {
            trace.removePoint(removed);
          }
        }
        return removed != null;
      }
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removePointHighlighter(info.monitorenter.gui.chart.IPointPainter)
   */
  public boolean removePointHighlighter(final IPointPainter< ? > higlighter) {

    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        boolean result = false;
        result = this.m_pointHighlighters.remove(higlighter);
        if (result) {
          this.firePropertyChange(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, higlighter, null);
        }
        return result;
      }
    }
  }

  /**
   * Override this template method for the custom remove operation that depends
   * on the internal storage the implementation.
   * <p>
   * The returned point may be the same as the given. But some "computing"
   * traces like
   * <code>{@link info.monitorenter.gui.chart.traces.computing.Trace2DArithmeticMean}</code>
   * will internally delete a different point and return that one.
   * <p>
   * No property change events have to be fired by default. If this method
   * returns <code>null</code> the outer logic of the calling method
   * <code>{@link #removePoint(ITracePoint2D)}</code> will perform bound checks
   * for the returned point and fire property changes for the properties
   * <code>{@link ITrace2D#PROPERTY_MAX_X}</code>,
   * <code>{@link ITrace2D#PROPERTY_MIN_X}</code>,
   * <code>{@link ITrace2D#PROPERTY_MAX_Y}</code> and
   * <code>{@link ITrace2D#PROPERTY_MIN_Y}</code>.
   * <p>
   * In special cases - when additional modifications to the internal set of
   * points take place (e.g. a further point get added) this method should
   * return false (regardless whether the old point was really removed or not)
   * and perform bound checks and fire the property changes as mentioned above
   * "manually".
   * <p>
   * 
   * @param point
   *          the point to remove.
   * @return null if unsuccessful (and no events should be fired) or the point
   *         that actually was removed (in case different than the given one it
   *         should be somehow related to the given one).
   */
  protected abstract ITracePoint2D removePointInternal(final ITracePoint2D point);

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_propertyChangeSupport.removePropertyChangeListener(property, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public boolean removeTracePainter(final ITracePainter< ? > painter) {
    boolean result = false;
    if (this.m_tracePainters.size() > 1) {
      result = this.m_tracePainters.remove(painter);
      if (result) {
        this.firePropertyChange(ITrace2D.PROPERTY_PAINTERS, painter, null);
      }

    } else {
      // nop: if not contained operation will not be successful, if contained
      // not allowed.
    }
    return result;
  }

  /**
   * <p>
   * Set the <code>Color</code> this trace will be painted with.
   * </p>
   * 
   * @param color
   *          the <code>Color</code> this trace will be painted with.
   */
  public final void setColor(final Color color) {
    final Color oldValue = this.m_color;
    this.m_color = color;
    if (!this.m_color.equals(oldValue)) {
      this.firePropertyChange(ITrace2D.PROPERTY_COLOR, oldValue, this.m_color);
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setErrorBarPolicy(info.monitorenter.gui.chart.IErrorBarPolicy)
   */
  public final Set<IErrorBarPolicy< ? >> setErrorBarPolicy(final IErrorBarPolicy< ? > errorBarPolicy) {
    final Set<IErrorBarPolicy< ? >> result = this.m_errorBarPolicies;
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("setErrorBarPolicy, 0 locks");
    }
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("setErrorBarPolicy, 1 lock");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("setErrorBarPolicy, 2 locks");
        }
        this.m_errorBarPolicies = new TreeSet<IErrorBarPolicy< ? >>();
        final boolean added = this.m_errorBarPolicies.add(errorBarPolicy);
        if (added) {
          errorBarPolicy.setTrace(this);
          this.expandErrorBarBounds();
          errorBarPolicy.addPropertyChangeListener(IErrorBarPolicy.PROPERTY_CONFIGURATION, this);
          this.firePropertyChange(ITrace2D.PROPERTY_ERRORBARPOLICY, null, errorBarPolicy);
        }
        // now remove this from the previous instances:
        for (final IErrorBarPolicy< ? > oldPolicy : result) {
          oldPolicy.setTrace(null);
          errorBarPolicy.removePropertyChangeListener(IErrorBarPolicy.PROPERTY_CONFIGURATION, this);
          this.firePropertyChange(ITrace2D.PROPERTY_ERRORBARPOLICY, oldPolicy, null);
        }
      }
    }
    return result;
  }

  /**
   * Sets the descriptive name for this trace.
   * <p>
   * If the given argument is null or consists of whitespaces only the lable for
   * this trace might become invisible (depending on physical units set).
   * <p>
   * 
   * @param name
   *          the descriptive name for this trace.
   * @see info.monitorenter.gui.chart.ITrace2D#setName(java.lang.String)
   */
  public final void setName(final String name) {
    final String oldValue = this.m_name;
    final String oldLabel = this.getLabel();
    this.m_name = name;
    final String newLabel = this.getLabel();
    this.firePropertyChange(ITrace2D.PROPERTY_LABEL, oldLabel, newLabel);
    this.firePropertyChange(ITrace2D.PROPERTY_NAME, oldValue, this.m_name);

  }

  /**
   * @see ITrace2D#setPhysicalUnits(String, String)
   */
  public final void setPhysicalUnits(final String xunit, final String yunit) {
    final String oldValue = this.getPhysicalUnits();
    final String oldLabel = this.getLabel();
    this.m_physicalUnitsX = xunit;
    this.m_physicalUnitsY = yunit;
    final String newValue = this.getPhysicalUnits();
    final String newLabel = this.getLabel();
    this.firePropertyChange(ITrace2D.PROPERTY_LABEL, oldLabel, newLabel);
    this.firePropertyChange(ITrace2D.PROPERTY_PHYSICALUNITS, oldValue, newValue);
  }

  /**
   * 
   * @see info.monitorenter.gui.chart.ITrace2D#setPointHighlighter(info.monitorenter.gui.chart.IPointPainter)
   */
  public final Set<IPointPainter< ? >> setPointHighlighter(final IPointPainter< ? > highlighter) {
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      synchronized (this) {
        Set<IPointPainter< ? >> result = this.m_pointHighlighters;
        this.m_pointHighlighters = new LinkedHashSet<IPointPainter< ? >>();

        final boolean added = this.m_pointHighlighters.add(highlighter);
        if (added) {
          for (final IPointPainter< ? > rem : result) {
            this.firePropertyChange(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, rem, null);
          }
          this.firePropertyChange(ITrace2D.PROPERTY_POINT_HIGHLIGHTERS_CHANGED, null, highlighter);

        } else {
          // roll back: will never happen, but here for formal reason
          this.m_pointHighlighters = result;
          result = null;
        }
        return result;
      }
    }
  }

  /**
   * Allows the chart this instance is painted by to register itself.
   * <p>
   * This is internally required for synchronization and re-ordering due to
   * z-Index changes.
   * <p>
   * 
   * @param renderer
   *          the chart that paints this instance.
   */
  public final void setRenderer(final Chart2D renderer) {
    final boolean requiresErrorBarCalculation = this.m_renderer != renderer;
    this.m_renderer = renderer;
    if (requiresErrorBarCalculation) {
      this.expandErrorBarBounds();
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setStroke(java.awt.Stroke)
   */
  public final void setStroke(final Stroke stroke) {
    if (stroke == null) {
      throw new IllegalArgumentException("Argument must not be null.");
    }
    final Stroke oldValue = this.m_stroke;
    this.m_stroke = stroke;
    if (!this.m_stroke.equals(oldValue)) {
      this.firePropertyChange(ITrace2D.PROPERTY_STROKE, oldValue, this.m_stroke);
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public final Set<ITracePainter< ? >> setTracePainter(final ITracePainter< ? > painter) {
    Set<ITracePainter< ? >> result = this.m_tracePainters;
    this.m_tracePainters = new TreeSet<ITracePainter< ? >>();
    final boolean added = this.m_tracePainters.add(painter);
    if (added) {
      for (final ITracePainter< ? > rem : result) {
        this.firePropertyChange(ITrace2D.PROPERTY_PAINTERS, rem, null);

      }
      this.firePropertyChange(ITrace2D.PROPERTY_PAINTERS, null, painter);

    } else {
      // roll back: will never happen, but here for formal reason
      this.m_tracePainters = result;
      result = null;
    }
    return result;
  }

  /**
   * <p>
   * Set the visible property of this instance.
   * </p>
   * <p>
   * Invisible <code>ITrace2D</code> instances (visible == false) will not be
   * painted.
   * </p>
   * 
   * @param visible
   *          the visible property of this instance to set.
   * @see info.monitorenter.gui.chart.ITrace2D#setVisible(boolean)
   */
  public final void setVisible(final boolean visible) {
    final boolean oldValue = this.m_visible;
    this.m_visible = visible;
    if (oldValue != this.m_visible) {
      this.firePropertyChange(ITrace2D.PROPERTY_VISIBLE, Boolean.valueOf(oldValue), Boolean
          .valueOf(this.m_visible));
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setZIndex(java.lang.Integer)
   */
  public final void setZIndex(final Integer zIndex) {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("trace.setZIndex, 0 locks");
    }

    if (!zIndex.equals(this.m_zIndex)) {
      this.ensureInitialized();
      synchronized (this.m_renderer) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("trace.setZIndex, 1 lock");
        }

        final Integer oldValue = this.m_zIndex;
        synchronized (this) {
          if (Chart2D.DEBUG_THREADING) {
            System.out.println("trace.setZIndex, 2 locks");
          }
          this.m_zIndex = Integer.valueOf(zIndex.intValue());
          this.firePropertyChange(ITrace2D.PROPERTY_ZINDEX, oldValue, this.m_zIndex);
        }
      }
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsErrorBars()
   */
  public boolean showsErrorBars() {
    boolean result = false;
    for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
      if (errorBarPolicy.getErrorBarPainters().size() > 0) {
        if (errorBarPolicy.isShowNegativeXErrors() || errorBarPolicy.isShowNegativeYErrors()
            || errorBarPolicy.isShowPositiveXErrors() || errorBarPolicy.isShowPositiveYErrors()) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsNegativeXErrorBars()
   */
  public boolean showsNegativeXErrorBars() {
    boolean result = false;
    for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
      if (errorBarPolicy.getErrorBarPainters().size() > 0) {
        if (errorBarPolicy.isShowNegativeXErrors()) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsNegativeYErrorBars()
   */
  public boolean showsNegativeYErrorBars() {
    boolean result = false;
    for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
      if (errorBarPolicy.getErrorBarPainters().size() > 0) {
        if (errorBarPolicy.isShowNegativeYErrors()) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsPositiveXErrorBars()
   */
  public boolean showsPositiveXErrorBars() {
    boolean result = false;
    for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
      if (errorBarPolicy.getErrorBarPainters().size() > 0) {
        if (errorBarPolicy.isShowPositiveXErrors()) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsPositiveYErrorBars()
   */
  public boolean showsPositiveYErrorBars() {
    boolean result = false;
    for (final IErrorBarPolicy< ? > errorBarPolicy : this.m_errorBarPolicies) {
      if (errorBarPolicy.getErrorBarPainters().size() > 0) {
        if (errorBarPolicy.isShowPositiveYErrors()) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Returns <code>{@link #getName()}.</code>
   * <p>
   * 
   * @return <code>{@link #getName()}</code>.
   */
  @Override
  public String toString() {
    return this.getName();
  }

  /**
   * Provides serialization support.
   * 
   * @param stream
   *          the output stream.
   * @throws IOException
   *           if there is an I/O error.
   */
  private void writeObject(final ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    SerializationUtility.writeStroke(this.m_stroke, stream);
  }
}
