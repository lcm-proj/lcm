/*
 * ObjectRecorder2Trace2DAdpater, an adapter which enables drawing timestamped
 * values inspected by the ObjectRecorder on a Chart2D.
 * Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * If you modify or optimize the code in a useful way please let me know.
 * Achim.Westermann@gmx.de
 */

package info.monitorenter.reflection;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.util.TimeStampedValue;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple adapter that allows displaying of timestamped values from an
 * inspection of the
 * <code>{@link info.monitorenter.reflection.ObjectRecorder}</code> on a
 * Chart2D.
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 */
public class ObjRecorder2Trace2DAdapter implements ChangeListener {

  /** The field name to inspect. */
  private final String m_fieldname;

  /** The source inspector to connect to the trace. */
  private final ObjectRecorder m_inspector;

  /**
   * The starting timestamp of this inspection that is used to put the timestamp
   * into relation to the first inspection.
   */
  private final long m_start = System.currentTimeMillis();

  /** The target trace to use. */
  private final ITrace2D m_view;

  /**
   * Creates a bridge from the given field of the given instance to inspect to
   * the trace.
   * <p>
   * 
   * @param view
   *          the target trace that will show the inspected value.
   * 
   * @param toinspect
   *          the instance to inpsect.
   * 
   * @param fieldname
   *          the field on the instance to inspect.
   * 
   * @param interval
   *          the interval of inspections in ms.
   */
  public ObjRecorder2Trace2DAdapter(final ITrace2D view, final Object toinspect,
      final String fieldname, final long interval) {
    this.m_view = view;
    this.m_fieldname = fieldname;
    this.m_inspector = new ObjectRecorder(toinspect, interval);
    this.m_inspector.addChangeListener(this);
  }

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
    final ObjRecorder2Trace2DAdapter other = (ObjRecorder2Trace2DAdapter) obj;
    if (this.m_fieldname == null) {
      if (other.m_fieldname != null) {
        return false;
      }
    } else if (!this.m_fieldname.equals(other.m_fieldname)) {
      return false;
    }
    if (this.m_inspector == null) {
      if (other.m_inspector != null) {
        return false;
      }
    } else if (!this.m_inspector.equals(other.m_inspector)) {
      return false;
    }
    if (this.m_start != other.m_start) {
      return false;
    }
    if (this.m_view == null) {
      if (other.m_view != null) {
        return false;
      }
    } else if (!this.m_view.equals(other.m_view)) {
      return false;
    }
    return true;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_fieldname == null) ? 0 : this.m_fieldname.hashCode());
    result = prime * result + ((this.m_inspector == null) ? 0 : this.m_inspector.hashCode());
    result = prime * result + (int) (this.m_start ^ (this.m_start >>> 32));
    result = prime * result + ((this.m_view == null) ? 0 : this.m_view.hashCode());
    return result;
  }

  /**
   * Sets the interval for inspections in ms.
   * <p>
   * 
   * @param interval
   *          the interval for inspections in ms.
   */
  public void setInterval(final long interval) {
    this.m_inspector.setInterval(interval);
  }

  /**
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(final ChangeEvent e) {
    TimeStampedValue last;
    try {
      last = this.m_inspector.getLastValue(this.m_fieldname);
    } catch (final Exception f) {
      f.printStackTrace();
      return;
    }
    if (last != null) {
      double tmpx;
      double tmpy;
      tmpx = last.getTime() - this.m_start;
      tmpy = Double.parseDouble(last.getValue().toString());
      this.m_view.addPoint(tmpx, tmpy);
    }
  }
}
