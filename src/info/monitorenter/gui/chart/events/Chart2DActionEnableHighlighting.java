/*
 *  Chart2DActionEnableHighlighting.java of project jchart2d
 *  Copyright (c) 2011 Achim Westermann.
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
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;

/**
 * <code>Action</code> that invokes <code>{@link Chart2D#enablePointHighlighting(boolean)}</code>
 * upon selection by the triggering <code>{@link AbstractButton}</code>.<p>
 * 
 * Also this <code>Action</p> will track the state <code>{@link Chart2D#isEnabledPointHighlighting()}</code> 
 * as a listener of the chart and inform all listeners of this action for the event <code>{@link LayoutFactory.PropertyChangeCheckBoxMenuItem#PROPERTY_SELECTED}</code> 
 * with the new state.
 * To listen to this action just have a look at the usage of <code>{@link LayoutFactory.SelectionPropertyAdaptSupport}</code>.<p>
 * 
 * <h2>Caution</h2> This implementation only works if assigned to a trigger that
 * descends from {@link AbstractButton} (e.g.
 * {@link javax.swing.JCheckBoxMenuItem} or {@link JRadioButtonMenuItem}): It
 * assumes that the source instance given to
 * {@link #actionPerformed(ActionEvent)} within the action event is of that type
 * as the state information (selected) is needed.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.4 $
 */
public class Chart2DActionEnableHighlighting extends AChart2DAction {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = -5816028313134616682L;

  /**
   * Creates an <code>Action</code> that will invoke
   * <code>{@link Chart2D#enablePointHighlighting(boolean)}</code> with the
   * state of the triggering <code>{@link ItemSelectable}</code>
   * <p>
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public Chart2DActionEnableHighlighting(Chart2D chart, String description) {
    super(chart, description);
    chart.addPropertyChangeListener(Chart2D.PROPERTY_POINT_HIGHLIGHTING_ENABLED, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    AbstractButton item = (JCheckBoxMenuItem) e.getSource();
    boolean state = item.isSelected();
    this.m_chart.enablePointHighlighting(state);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_POINT_HIGHLIGHTING_ENABLED)) {
      this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, evt.getOldValue(),
          evt.getNewValue());
    }
  }
}
