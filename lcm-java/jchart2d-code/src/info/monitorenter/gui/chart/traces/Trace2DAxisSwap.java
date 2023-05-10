/*
 *  Trace2DAxisSwap.java  jchart2d
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

import java.awt.Color;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;

/**
 * A delegator / proxy that delegates all calls to an internal constructor-given
 * ITrace2d and swaps the data of the added Point2D instances.
 * <p>
 * x values become y values and vice versa. Performance is bad, as unnecessary
 * instances are created (each TracePoint2D is instantiated twice) so this
 * instance is for debugging / testing purposes only.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.34 $
 */
public class Trace2DAxisSwap implements ITrace2D, Comparable<ITrace2D> {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -5987864541853522562L;

  /** The delegate instance to decorate with axis swapping. */
  private final ITrace2D m_delegate;

  /**
   * Creates an instance that will swap the axis of the given delegate.
   * <p>
   * 
   * @param trace
   *          the delegate instance to decorate with axis swapping.
   */
  public Trace2DAxisSwap(final ITrace2D trace) {
    if (trace == null) {
      throw new IllegalArgumentException("Argument must not be null.");
    }
    if (trace.getClass() == this.getClass()) {
      throw new IllegalArgumentException("Nesting of " + this.getClass().getName()
          + " is unnecessary and may be harmful.");
    }
    this.m_delegate = trace;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addComputingTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public void addComputingTrace(final ITrace2D trace) {
    this.m_delegate.addComputingTrace(trace);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addErrorBarPolicy(info.monitorenter.gui.chart.IErrorBarPolicy)
   */
  public boolean addErrorBarPolicy(final IErrorBarPolicy< ? > errorBarPolicy) {
    return this.m_delegate.addErrorBarPolicy(errorBarPolicy);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addPoint(double, double)
   */
  public boolean addPoint(final double x, final double y) {
    final ITracePoint2D p = this.getRenderer().getTracePointProvider().createTracePoint(x, y);
    return this.addPoint(p);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addPoint(info.monitorenter.gui.chart.ITracePoint2D)
   */
  public boolean addPoint(final ITracePoint2D p) {
    final ITracePoint2D swap = (ITracePoint2D) p.clone();
    swap.setLocation(p.getY(), p.getX());
    return this.m_delegate.addPoint(swap);
  }

  public boolean addPointHighlighter(final IPointPainter< ? > highlighter) {
    return this.m_delegate.addPointHighlighter(highlighter);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(final String propertyName,
      final PropertyChangeListener listener) {
    this.m_delegate.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#addTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public boolean addTracePainter(final ITracePainter< ? > painter) {
    return this.m_delegate.addTracePainter(painter);
  }

  /**
   * @param o
   *          the trace to compare to.
   * @return see interface.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(final ITrace2D o) {
    return this.m_delegate.compareTo(o);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#containsTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public boolean containsTracePainter(final ITracePainter< ? > painter) {
    return this.m_delegate.containsTracePainter(painter);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#firePointChanged(info.monitorenter.gui.chart.ITracePoint2D,
   *      int)
   */
  public void firePointChanged(final ITracePoint2D changed, final int state) {
    this.m_delegate.firePointChanged(changed, state);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getColor()
   */
  public Color getColor() {
    return this.m_delegate.getColor();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getErrorBarPolicies()
   */
  public Set<IErrorBarPolicy< ? >> getErrorBarPolicies() {
    return this.m_delegate.getErrorBarPolicies();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getHasErrorBars()
   */
  public boolean getHasErrorBars() {
    return this.m_delegate.getHasErrorBars();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getLabel()
   */
  public String getLabel() {
    return this.m_delegate.getLabel();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMaxSize()
   */
  public int getMaxSize() {
    return this.m_delegate.getMaxSize();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMaxX()
   */
  public double getMaxX() {
    return this.m_delegate.getMaxX();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMaxY()
   */
  public double getMaxY() {
    return this.m_delegate.getMaxY();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMinX()
   */
  public double getMinX() {
    return this.m_delegate.getMinX();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMinY()
   */
  public double getMinY() {
    return this.m_delegate.getMinY();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getName()
   */
  public String getName() {
    return this.m_delegate.getName();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getNearestPointEuclid(double,
   *      double)
   */
  public DistancePoint getNearestPointEuclid(final double x, final double y) {
    return this.m_delegate.getNearestPointEuclid(x, y);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getNearestPointManhattan(double,
   *      double)
   */
  public DistancePoint getNearestPointManhattan(final double x, final double y) {
    return this.m_delegate.getNearestPointManhattan(x, y);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPhysicalUnits()
   */
  public String getPhysicalUnits() {
    return this.m_delegate.getPhysicalUnits();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPhysicalUnitsX()
   */
  public String getPhysicalUnitsX() {
    return this.m_delegate.getPhysicalUnitsX();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPhysicalUnitsY()
   */
  public String getPhysicalUnitsY() {
    return this.m_delegate.getPhysicalUnitsY();
  }

  public Set<IPointPainter< ? >> getPointHighlighters() {
    return this.m_delegate.getPointHighlighters();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getPropertyChangeListeners(java.lang.String)
   */
  public PropertyChangeListener[] getPropertyChangeListeners(final String property) {
    return this.m_delegate.getPropertyChangeListeners(property);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getRenderer()
   */
  public Chart2D getRenderer() {
    return this.m_delegate.getRenderer();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getSize()
   */
  public int getSize() {
    return this.m_delegate.getSize();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getStroke()
   */
  public Stroke getStroke() {
    return this.m_delegate.getStroke();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getTracePainters()
   */
  public Set<ITracePainter< ? >> getTracePainters() {
    return this.m_delegate.getTracePainters();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getZIndex()
   */
  public Integer getZIndex() {
    return this.m_delegate.getZIndex();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#isEmpty()
   */
  public boolean isEmpty() {
    return this.m_delegate.isEmpty();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#isVisible()
   */
  public boolean isVisible() {
    return this.m_delegate.isVisible();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#iterator()
   */
  public Iterator<ITracePoint2D> iterator() {
    return this.m_delegate.iterator();
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    this.m_delegate.propertyChange(evt);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeAllPointHighlighters()
   */
  public Set<IPointPainter< ? >> removeAllPointHighlighters() {
    return this.m_delegate.removeAllPointHighlighters();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeAllPoints()
   */
  public void removeAllPoints() {
    this.m_delegate.removeAllPoints();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeComputingTrace(info.monitorenter.gui.chart.ITrace2D)
   */
  public boolean removeComputingTrace(final ITrace2D trace) {
    return this.m_delegate.removeComputingTrace(trace);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeErrorBarPolicy(info.monitorenter.gui.chart.IErrorBarPolicy)
   */
  public boolean removeErrorBarPolicy(final IErrorBarPolicy< ? > errorBarPolicy) {
    return this.m_delegate.removeErrorBarPolicy(errorBarPolicy);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removePoint(info.monitorenter.gui.chart.ITracePoint2D)
   */
  public boolean removePoint(final ITracePoint2D point) {
    return this.m_delegate.removePoint(point);
  }

  public boolean removePointHighlighter(final IPointPainter< ? > highlighter) {
    return this.m_delegate.removePointHighlighter(highlighter);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    this.m_delegate.removePropertyChangeListener(listener);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removePropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(final String property,
      final PropertyChangeListener listener) {
    this.m_delegate.removePropertyChangeListener(property, listener);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public boolean removeTracePainter(final ITracePainter< ? > painter) {
    return this.m_delegate.removeTracePainter(painter);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setColor(java.awt.Color)
   */
  public void setColor(final Color color) {
    this.m_delegate.setColor(color);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setErrorBarPolicy(info.monitorenter.gui.chart.IErrorBarPolicy)
   */
  public Set<IErrorBarPolicy< ? >> setErrorBarPolicy(final IErrorBarPolicy< ? > errorBarPolicy) {
    return this.m_delegate.setErrorBarPolicy(errorBarPolicy);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setName(java.lang.String)
   */
  public void setName(final String name) {
    this.m_delegate.setName(name);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setPhysicalUnits(java.lang.String,
   *      java.lang.String)
   */
  public void setPhysicalUnits(final String xunit, final String yunit) {
    this.m_delegate.setPhysicalUnits(xunit, yunit);
  }

  public Set<IPointPainter< ? >> setPointHighlighter(final IPointPainter< ? > highlighter) {
    return this.m_delegate.setPointHighlighter(highlighter);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setRenderer(info.monitorenter.gui.chart.Chart2D)
   */
  public void setRenderer(final Chart2D renderer) {
    this.m_delegate.setRenderer(renderer);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setStroke(java.awt.Stroke)
   */
  public void setStroke(final Stroke stroke) {
    this.m_delegate.setStroke(stroke);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setTracePainter(info.monitorenter.gui.chart.ITracePainter)
   */
  public Set<ITracePainter< ? >> setTracePainter(final ITracePainter< ? > painter) {
    return this.m_delegate.setTracePainter(painter);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setVisible(boolean)
   */
  public void setVisible(final boolean visible) {
    this.m_delegate.setVisible(visible);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#setZIndex(java.lang.Integer)
   */
  public void setZIndex(final Integer zIndex) {
    this.m_delegate.setZIndex(zIndex);
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsErrorBars()
   */
  public boolean showsErrorBars() {
    return this.m_delegate.showsErrorBars();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsNegativeXErrorBars()
   */
  public boolean showsNegativeXErrorBars() {
    return this.m_delegate.showsNegativeXErrorBars();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsNegativeYErrorBars()
   */
  public boolean showsNegativeYErrorBars() {
    return this.m_delegate.showsNegativeYErrorBars();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsPositiveXErrorBars()
   */
  public boolean showsPositiveXErrorBars() {
    return this.m_delegate.showsPositiveXErrorBars();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#showsPositiveYErrorBars()
   */
  public boolean showsPositiveYErrorBars() {
    return this.m_delegate.showsPositiveYErrorBars();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.m_delegate.toString();
  }

}
