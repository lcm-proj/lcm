/*
 *  Trace2DActionSetVisible, action to control the visibility of an ITrace2D.
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

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JCheckBoxMenuItem;

/**
 * <p>
 * Performs the action of setting a trace visible (
 * {@link info.monitorenter.gui.chart.ITrace2D#setVisible(boolean)}) with the
 * constructor given boolean.
 * </p>
 * <p>
 * <b>This action only may be assigned to a
 * {@link javax.swing.JCheckBoxMenuItem}</b> <br>
 * The <em>source</em> <code>Object</code> of <code>ActionEvent</code>
 * that is received in {@link #actionPerformed(ActionEvent)} is casted to this
 * type to get the boolean state. If this <code>Action</code> is used with
 * other <code>JComponent</code> instances <code>ClassCastExceptions</code>
 * will be thrown!
 * </p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.5 $
 */
public class Trace2DActionSetVisible extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3689069560279937078L;

  /**
   * Create an <code>Action</code> that accesses the chart and identifies
   * itself with the given action String.
   * <p>
   * 
   * @param trace
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public Trace2DActionSetVisible(final ITrace2D trace, final String description) {
    super(trace, description);
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_VISIBLE, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
    this.m_trace.setVisible(item.getState());
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(ITrace2D.PROPERTY_VISIBLE)) {
      this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, evt.getOldValue(),
          evt.getNewValue());
    }
  }
}
