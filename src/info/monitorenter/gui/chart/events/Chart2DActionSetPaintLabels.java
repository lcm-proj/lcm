/*
 *  AxisActionSetPaintLabels.java of project jchart2d.
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 00:13:29.
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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JCheckBoxMenuItem;

/**
 * <code>Action</code> that invokes
 * {@link info.monitorenter.gui.chart.Chart2D#setPaintLabels(boolean)} on a
 * constructor given {@link info.monitorenter.gui.chart.Chart2D}.
 * <p>
 * 
 * This action is not used by the context menu labels of
 * {@link info.monitorenter.gui.chart.views.ChartPanel} because that instance
 * deactivates this feature in order to use a custom {@link javax.swing.JLabel}
 * that triggers a popup menu for trace controls.
 * <p>
 * 
 * <h2>Caution</h2>
 * This implementation only works if assigned to a
 * {@link javax.swing.JCheckBoxMenuItem}: It assumes that the source instance
 * given to {@link #actionPerformed(ActionEvent)} within the action event is of
 * that type as the state information (turn paint labels on or off) is needed.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.8 $
 */
public class Chart2DActionSetPaintLabels
    extends AChart2DAction {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = 2032168563789294815L;

  /**
   * Create an <code>Action</code> that accesses the axis, identifies itself
   * with the given action String and invokes
   * {@link info.monitorenter.gui.chart.Chart2D#setPaintLabels(boolean)} on the
   * chart upon selection.
   * 
   * @param chart
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public Chart2DActionSetPaintLabels(final Chart2D chart, final String description) {
    super(chart, description);
    chart.addPropertyChangeListener(Chart2D.PROPERTY_PAINTLABELS, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
    boolean state = item.getState();
    this.m_chart.setPaintLabels(state);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_PAINTLABELS)) {
      this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, evt.getOldValue(),
          evt.getNewValue());
    }
  }
}
