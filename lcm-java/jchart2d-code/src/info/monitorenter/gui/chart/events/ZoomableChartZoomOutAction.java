/*
 *  ZoomableChartZoomOutAction, action for zooming out a ZoomableChart.
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

package info.monitorenter.gui.chart.events;

import info.monitorenter.gui.chart.ZoomableChart;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * <code>Action</code> for zooming a <code>{@link  ZoomableChart}</code> 
 * to the original size (zoom out).
 * <p>
 * 
 * @author <a href="mailto:vpappas@gmail.com">Vasilis Pappas </a>
 * 
 * @version $Revision: 1.4 $
 */
public final class ZoomableChartZoomOutAction
    extends AZoomableChartAction {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = 1663463025252405898L;

  /**
   * Create an <code>Action</code> that zooms out a zoomable chart.
   * <p>
   * 
   * @param chart
   *          the target the action will work on.
   * 
   * @param description
   *          the description of this action to show in the UI.
   * 
   */
  public ZoomableChartZoomOutAction(final ZoomableChart chart, final String description) {

    super(chart, description);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {

    this.m_chart.zoomAll();
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {

    // nop as this action will not be used
    // by checkbox or radio button menu items that have a state.
  }

}
