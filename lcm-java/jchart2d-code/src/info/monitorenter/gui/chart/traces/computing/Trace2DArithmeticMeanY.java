/*
 *  Trace2DArithmeticMeanY.java of project jchart2d, a trace 
 *  that accumulates the latest n y values added to a single point with the arithmetic 
 *  mean y value and the latest x value. 
 *  Copyright 2005 - 2011 (C) Achim Westermann.
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
package info.monitorenter.gui.chart.traces.computing;

import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.traces.ATrace2D;
import info.monitorenter.util.collections.IRingBuffer;
import info.monitorenter.util.collections.RingBufferArrayFast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A trace that accumulates the latest n y values added to points with
 * the arithmetic mean y value and the latest x value.
 * <p>
 * 
 * This trace will not reduce the amount of n points added to one carrying the arithmetic mean but 
 * always add a point that has the average of the recent added n points.
 * <p>
 * 
 * Please note that this trace scan be used in two modes:
 * <ol>
 * <li>Stand alone: <br/>
 * Add the <code>ITrace2D</code> implementation to a chart and add data points
 * to it as normal.</li>
 * <li>Computing trace: <br/>
 * Add the <code>ITrace2D</code> implementation as a computing trace to an
 * existing trace via
 * <code>{@link info.monitorenter.gui.chart.ITrace2D#addComputingTrace(info.monitorenter.gui.chart.ITrace2D)}</code>
 * and only add data points to the original trace. Add the computing trace to
 * the same chart and updates of the original trace will be reflected on the
 * computing trace as well.</li>
 * </ol>
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.4 $
 * 
 * 
 */
public class Trace2DArithmeticMeanY extends ATrace2D {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -4365986306182830082L;

  /** The buffer for the points about to be merged. */
  private final IRingBuffer<ITracePoint2D> m_pointBuffer;

  /** The internal list of points to render. */
  private final List<ITracePoint2D> m_points = new LinkedList<ITracePoint2D>();

  /**
   * The amount of n recent points to buffer. private int m_pointBufferSize; /**
   * Constructor with the given amount of points to merge into one point with
   * their arithmetic mean.
   * <p>
   * 
   * @param arithmenticMeanSpan
   *          the amount of points to merge into one point with their arithmetic
   *          mean.
   */
  public Trace2DArithmeticMeanY(final int arithmenticMeanSpan) {
    super();
    this.m_pointBuffer = new RingBufferArrayFast<ITracePoint2D>(arithmenticMeanSpan);
  }

  /**
   * @see info.monitorenter.gui.chart.traces.ATrace2D#addPointInternal(info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected boolean addPointInternal(final ITracePoint2D p) {
    this.m_pointBuffer.add(p);
    final ITracePoint2D cumulate = this.getArithmeticMean();
    final boolean result = this.m_points.add(cumulate);
    this.firePointAdded(cumulate);
    return result;
  }

  /**
   * Returns a point with the arithmetic mean values for x and y computed of the
   * last n added points (n was constructor - given).
   * <p>
   * 
   * @return a point with the arithmetic mean values for x and y computed of the
   *         last n added points (n was constructor - given).
   */
  private ITracePoint2D getArithmeticMean() {
    double x = 0;
    double y = 0;
    ITracePoint2D result;
    for (final ITracePoint2D point : this.m_pointBuffer) {
      result = point;
      x = result.getX();
      y += result.getY();
    }
    int divisor = this.m_pointBuffer.size();
    if (divisor == 0) {
      divisor = 1;
    }
    y /= divisor;
    result = this.getRenderer().getTracePointProvider().createTracePoint(x, y);
    return result;

  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMaxSize()
   */
  public int getMaxSize() {
    return Integer.MAX_VALUE;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getSize()
   */
  public int getSize() {

    return this.m_points.size();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#isEmpty()
   */
  public boolean isEmpty() {
    return this.m_points.isEmpty();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#iterator()
   */
  public Iterator<ITracePoint2D> iterator() {
    return this.m_points.iterator();
  }

  /**
   * @see info.monitorenter.gui.chart.traces.ATrace2D#removeAllPointsInternal()
   */
  @Override
  protected void removeAllPointsInternal() {
    this.m_pointBuffer.clear();
    this.m_points.clear();

  }

  /**
   * @see info.monitorenter.gui.chart.traces.ATrace2D#removePointInternal(info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected ITracePoint2D removePointInternal(final ITracePoint2D point) {
    final ITracePoint2D result = this.m_points.remove(0);
    return result;
  }

}
