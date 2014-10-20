/*
 *  Trace2DLtd, a RingBuffer- based fast implementation of a ITrace2D.
 *  Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de
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
package info.monitorenter.gui.chart.traces;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.util.collections.IRingBuffer;
import info.monitorenter.util.collections.RingBufferArrayFast;

import java.util.Iterator;

/**
 * Additional to the Trace2DSimple the Trace2DLimited adds the following
 * functionality:
 * <p>
 * <ul>
 * <li>The amount of internal tracepoints is limited to the maxsize, passed to
 * the constructor.</li>
 * <li>If a new tracepoint is inserted and the maxsize has been reached, the
 * tracepoint residing for the longest time in this trace is thrown away.</li>
 * </UL>
 * Take this implementation to display frequently changing data (nonstatic, time
 * - dependant values). You will avoid a huge growing amount of tracepoints that
 * would increase the time for scaling and painting until system hangs or
 * java.lang.OutOfMemoryError is thrown.
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.19 $
 */
public class Trace2DLtd extends ATrace2D implements ITrace2D {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -6664475237146326176L;

  /**
   * Internal fast FIFO buffer implementation based upon indexed access to an
   * array.
   */
  protected IRingBuffer<ITracePoint2D> m_buffer;

  /**
   * Constructs an instance with a default buffer size of 100.
   * <p>
   */
  public Trace2DLtd() {
    this(100);
  }

  /**
   * Constructs an instance with a buffersize of maxsize and a default name.
   * <p>
   * 
   * @param maxsize
   *          the buffer size for the maximum amount of points that will be
   *          shown.
   */
  public Trace2DLtd(final int maxsize) {
    this(maxsize, Trace2DLtd.class.getName() + "-" + ATrace2D.getInstanceCount());
  }

  /**
   * Constructs an instance with a buffersize of maxsize and a default name.
   * <p>
   * 
   * @param maxsize
   *          the buffer size for the maximum amount of points that will be
   *          shown.
   * 
   * @param name
   *          the name that will be displayed for this trace.
   */
  public Trace2DLtd(final int maxsize, final String name) {
    this.m_buffer = new RingBufferArrayFast<ITracePoint2D>(maxsize);
    this.setName(name);
  }

  /**
   * Creates an instance with a default buffersize of 100 and the given name.
   * <p>
   * 
   * @param name
   *          the name that will be displayed for the trace.
   */
  public Trace2DLtd(final String name) {
    this(100, name);
  }

  /**
   * @see ATrace2D#addPointInternal(info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected boolean addPointInternal(final ITracePoint2D p) {

    final ITracePoint2D removed = this.m_buffer.add(p);
    double tmpx;
    double tmpy;
    if (removed != null) {
      tmpx = removed.getX();
      tmpy = removed.getY();
      if (tmpx >= this.m_maxX) {
        tmpx = this.m_maxX;
        this.maxXSearch();
        this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(tmpx), new Double(this.m_maxX));
      } else if (tmpx <= this.m_minX) {
        tmpx = this.m_minX;
        this.minXSearch();
        this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(tmpx), new Double(this.m_minX));
      }
      if (tmpy >= this.m_maxY) {
        tmpy = this.m_maxY;
        this.maxYSearch();
        this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(tmpy), new Double(this.m_maxY));
      } else if (tmpy <= this.m_minY) {
        tmpy = this.m_minY;
        this.minYSearch();
        this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(tmpy), new Double(this.m_minY));
      }
      // scale the new point, check for new bounds!
      this.firePointAdded(p);
      // inform computing traces of removal:
      if (this.m_computingTraces.size() > 0) {
        for (final ITrace2D trace : this.m_computingTraces) {
          trace.removePoint(removed);
        }
      }
    }
    return true;
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
    final Trace2DLtd other = (Trace2DLtd) obj;
    if (this.m_buffer == null) {
      if (other.m_buffer != null) {
        return false;
      }
    } else if (!this.m_buffer.equals(other.m_buffer)) {
      return false;
    }
    return true;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#getMaxSize()
   */
  public int getMaxSize() {
    return this.m_buffer.getBufferSize();
  }

  /**
   * Returns the acutal amount of points in this trace.
   * <p>
   * 
   * @return the acutal amount of points in this trace.
   * 
   * @see info.monitorenter.gui.chart.ITrace2D#getSize()
   */
  public int getSize() {
    return this.m_buffer.size();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.m_buffer == null) ? 0 : this.m_buffer.hashCode());
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#isEmpty()
   */
  public boolean isEmpty() {
    return this.m_buffer.isEmpty();
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#iterator()
   */
  public Iterator<ITracePoint2D> iterator() {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("Trace2DLtd.iterator, 0 locks");
    }
    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("Trace2DLtd.iterator, 1 lock");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("Trace2DLtd.iterator, 2 locks");
        }
        return this.m_buffer.iteratorL2F();
      }
    }
  }

  /**
   * @see info.monitorenter.gui.chart.ITrace2D#removeAllPoints()
   */
  @Override
  public void removeAllPointsInternal() {
    this.m_buffer.clear();
  }

  /**
   * Returns null always because internally a ring buffer is used which does not
   * allow removing of values because that would break the contract of a ring
   * buffer.
   * <p>
   * 
   * @param point
   *          the point to remove.
   * 
   * @return null always because internally a ring buffer is used which does not
   *         allow removing of values because that would break the contract of a
   *         ring buffer.
   * 
   */
  @Override
  protected ITracePoint2D removePointInternal(final ITracePoint2D point) {
    return null;
  }

  /**
   * Sets the maximum amount of points that may be displayed.
   * <p>
   * 
   * Don't use this too often as decreases in size may cause expensive array
   * copy operations and new searches on all points for bound changes.
   * <p>
   * 
   * TODO: Only search for bounds if size is smaller than before, debug and
   * test.
   * 
   * @param amount
   *          the new maximum amount of points to show.
   */
  public final void setMaxSize(final int amount) {
    if (Chart2D.DEBUG_THREADING) {
      System.out.println("Trace2DLtd.setMaxSize, 0 locks");
    }

    this.ensureInitialized();
    synchronized (this.m_renderer) {
      if (Chart2D.DEBUG_THREADING) {
        System.out.println("Trace2DLtd.setMaxSize, 1 lock");
      }
      synchronized (this) {
        if (Chart2D.DEBUG_THREADING) {
          System.out.println("Trace2DLtd.setMaxSize, 2 locks");
        }
        this.m_buffer.setBufferSize(amount);

        final double xmin = this.m_minX;
        this.minXSearch();
        if (this.m_minX != xmin) {
          this.firePropertyChange(ITrace2D.PROPERTY_MIN_X, new Double(xmin),
              new Double(this.m_minX));
        }

        final double xmax = this.m_maxX;
        this.maxXSearch();
        if (this.m_maxX != xmax) {
          this.firePropertyChange(ITrace2D.PROPERTY_MAX_X, new Double(xmax),
              new Double(this.m_maxX));
        }

        final double ymax = this.m_maxY;
        this.maxYSearch();
        if (this.m_maxY != ymax) {
          this.firePropertyChange(ITrace2D.PROPERTY_MAX_Y, new Double(ymax),
              new Double(this.m_maxY));
        }

        final double ymin = this.m_minY;
        this.minYSearch();
        if (this.m_minY != ymin) {
          this.firePropertyChange(ITrace2D.PROPERTY_MIN_Y, new Double(ymin),
              new Double(this.m_minY));
        }
      }
    }
  }
}
