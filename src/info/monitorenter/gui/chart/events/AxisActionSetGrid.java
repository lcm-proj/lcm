/*
 *  AxisActionSetRangePolicy.java of project jchart2d
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
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractButton;

/**
 * <code>Action</code> that invokes
 * {@link info.monitorenter.gui.chart.axis.AAxis#setPaintGrid(boolean)} on a
 * constructor given {@link info.monitorenter.gui.chart.axis.AAxis}.
 * <p>
 * 
 * <h2>Caution</h2>
 * This implementation only works if assigned to a {@link AbstractButton}: It
 * assumes that the source instance given to
 * {@link #actionPerformed(ActionEvent)} within the action event is of that type
 * as the state information (turn grid visible or turn grid invisible) is
 * needed.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.14 $
 */
public class AxisActionSetGrid extends AAxisAction {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = -5816028313134616682L;

  /**
   * Create an <code>Action</code> that accesses the chart's axis by argument
   * <code>axis</code> and identifies itself with the given action String and
   * invokes
   * {@link info.monitorenter.gui.chart.axis.AAxis#setPaintGrid(boolean)} on the
   * axis upon selection.
   * <p>
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * @param axis
   *          needed to identify the axis of the chart: one of {@link Chart2D#X}
   *          , {@link Chart2D#Y}.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public AxisActionSetGrid(final Chart2D chart, final String description, final int axis) {
    super(chart, description, axis);

    if (axis == Chart2D.X) {
      chart.getAxisX().addPropertyChangeListener(IAxis.PROPERTY_PAINTGRID, this);
      chart.addPropertyChangeListener(Chart2D.PROPERTY_AXIS_X, this);
    } else if (axis == Chart2D.Y) {
      chart.getAxisY().addPropertyChangeListener(IAxis.PROPERTY_PAINTGRID, this);
      chart.addPropertyChangeListener(Chart2D.PROPERTY_AXIS_Y, this);
    }
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    AbstractButton item = (AbstractButton) e.getSource();
    boolean state = item.isSelected();
    this.getAxis().setPaintGrid(state);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    // will check for an axis replacement and transfer listening to the new axis if so: 
    super.propertyChange(evt);
    String property = evt.getPropertyName();
    if (property.equals(IAxis.PROPERTY_PAINTGRID)) {
      // someone else changed the paintgrid property via API: Inform the outer menu item UI:
      this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, evt.getOldValue(),
          evt.getNewValue());
    }
  }
}
