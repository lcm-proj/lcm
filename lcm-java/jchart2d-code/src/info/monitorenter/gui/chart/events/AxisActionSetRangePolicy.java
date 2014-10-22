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
import info.monitorenter.gui.chart.IRangePolicy;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;
import info.monitorenter.util.Range;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * Action that sets a constructor given
 * {@link info.monitorenter.gui.chart.IRangePolicy} to a constructor given
 * {@link info.monitorenter.gui.chart.axis.AAxis}.
 * <p>
 * 
 * <h2>Warning</h2>
 * This <code>Action</code> currently is only intended to be
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.16 $
 */
public class AxisActionSetRangePolicy
    extends AAxisAction {

  /**
   * Generated <code>serial version UID</code>.
   */
  private static final long serialVersionUID = -3093734349885438197L;

  /**
   * The range policy to set to the axis upon invocation of
   * {@link #actionPerformed(ActionEvent)}.
   */
  private final transient IRangePolicy m_rangePolicy;

  /**
   * Create an <code>Action</code> that accesses the axis, identifies itself
   * with the given action String and sets the given
   * {@link info.monitorenter.gui.chart.IRangePolicy} to the axis upon
   * selection.
   * <p>
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * @param axis
   *          needed to identify the axis of the chart: one of {@link Chart2D#X},
   *          {@link Chart2D#Y}.
   * 
   * @param rangePolicy
   *          the range policy to set oon the axis.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public AxisActionSetRangePolicy(final Chart2D chart, final String description, final int axis,
      final IRangePolicy rangePolicy) {
    super(chart, description, axis);
    this.m_rangePolicy = rangePolicy;
    this.getAxis().addPropertyChangeListener(IAxis.PROPERTY_RANGEPOLICY, this);

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    // initially configure the range to show all data (in case a fixed
    // view port is used):
    IAxis<?> axis = this.getAxis();
    Range actualRange = new Range(axis.getMinValue(), axis.getMaxValue());
    this.m_rangePolicy.setRange(actualRange);
    this.m_rangePolicy.setRange(actualRange);
    this.getAxis().setRangePolicy(this.m_rangePolicy);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    // will check for an axis replacement and transfer listening to the new axis if so: 
    super.propertyChange(evt);
    
    String property = evt.getPropertyName();
    if (property.equals(IAxis.PROPERTY_RANGEPOLICY)) {
      Class< ? > rangepolicyClass = evt.getNewValue().getClass();
      Boolean oldValue;
      Boolean newValue;
      if (rangepolicyClass == this.m_rangePolicy.getClass()) {
        oldValue = new Boolean(false);
        newValue = new Boolean(true);
      } else {
        oldValue = new Boolean(true);
        newValue = new Boolean(false);
      }
      this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, oldValue, newValue);
    }
  }
}
