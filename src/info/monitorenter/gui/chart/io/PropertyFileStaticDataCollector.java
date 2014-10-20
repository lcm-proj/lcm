/*
 *  PropertyFileStaticDataCollector, a collector for data 
 *  to display in static charts.
 *  Copyright (C) 2004 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Data collector that collects data in form of
 * {@link info.monitorenter.gui.chart.ITracePoint2D} instances from a property
 * file ( {@link java.util.Properties}).
 * <p>
 * 
 * @author Achim Westermann
 */
public class PropertyFileStaticDataCollector extends AStaticDataCollector {

  /**
   * The input stream in {@link java.util.Properties} format where
   * {@link info.monitorenter.gui.chart.TracePoint2D} are parsed from.
   * <p>
   */
  private InputStream m_source;

  /**
   * Constructor with target trace and property file.
   * <p>
   * 
   * @param trace
   *            the target trace to add data to.
   * 
   * @param propertyFileStream
   *            the stream of the file in the {@link java.util.Properties}
   *            format where {@link info.monitorenter.gui.chart.TracePoint2D}
   *            instances (key is x, value is y) is parsed from.
   */
  public PropertyFileStaticDataCollector(final ITrace2D trace, final InputStream propertyFileStream) {
    super(trace);
    this.m_source = propertyFileStream;
  }

  /**
   * @see info.monitorenter.gui.chart.io.AStaticDataCollector#collectData()
   */
  @Override
  public void collectData() throws FileNotFoundException, IOException {
    Properties props = new Properties();
    props.load(this.m_source);
    Set<Map.Entry<Object, Object>> entries =  props.entrySet();
    List<ITracePoint2D> sortList = new LinkedList<ITracePoint2D>();
    for (Map.Entry<Object, Object> entry : entries) {
      sortList.add(new TracePoint2D(Double.parseDouble((String) entry.getKey()), Double
          .parseDouble((String) entry.getValue())));

    }
    Collections.sort(sortList);
    for (ITracePoint2D point : sortList) {
      this.m_trace.addPoint(point);
    }
  }
}
