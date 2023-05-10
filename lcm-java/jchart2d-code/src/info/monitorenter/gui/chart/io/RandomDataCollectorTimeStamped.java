/*
 *
 *  RandomDataCollectorTimeStamped.java  of project jchart2d, collects time stamped 
 *  random data for demo purposes. 
 *  Copyright (C) 2005 - 2011 Achim Westermann, created on 20.03.2005, 16:59:10
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
package info.monitorenter.gui.chart.io;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.TracePoint2D;



/**
 * Data collector that collects values with an absolute timestamp as x value and
 * a random jumping y value.
 * <p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.9 $
 */
public class RandomDataCollectorTimeStamped extends ADataCollector {

  /** The last collected y value. */
  private double m_y = 0.0;

  /**
   * Creates an instance that will collect every latency ms a point and add it
   * to the trace.
   * <p>
   *
   * @param trace
   *          the trace to add collected points to.
   *
   * @param latency
   *          the interval in ms for collecting points.
   */
  public RandomDataCollectorTimeStamped(final ITrace2D trace, final int latency) {
    super(trace, latency);
  }

  /**
   * @see ADataCollector#collectData()
   */
  @Override
  public ITracePoint2D collectData() {
    double rand = Math.random();
    boolean add = (rand >= 0.5) ? true : false;
    this.m_y = (add) ? this.m_y + Math.random() : this.m_y - Math.random();
    return new TracePoint2D(System.currentTimeMillis(), this.m_y);
  }
}
