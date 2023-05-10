/*
 *  Chart2DActionSetName, action for setting the name of the chart.
 *  Copyright (C) 2007 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JOptionPane;


/**
 * <p>
 * <code>Action</code> that sets a custom name of the corresponding chart (
 * {@link info.monitorenter.gui.chart.Chart2D#setName(String)})
 * by showing a modal String chooser.
 * </p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.4 $
 */
public final class Chart2DActionSetName extends AChart2DAction {
  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3691034370412916788L;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies
   * itself with the given action String.
   * <p>
   *
   * @param chart
   *          the target the action will work on.
   *
   * @param chartName
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public Chart2DActionSetName(final Chart2D chart, final String chartName) {
    super(chart, chartName);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    String chosen = JOptionPane.showInputDialog(this.m_chart,
        "Please input the new Name for chart " + this.m_chart.getName(), "Set chart name",
        JOptionPane.PLAIN_MESSAGE);
    this.m_chart.setName(chosen);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop as this action will not be used
    // by checkbox or radio button menu items that have a state.
  }
}
