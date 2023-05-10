/*
 *  Trace2DActionSetName, action to set the name of an ITrace2D.
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JOptionPane;


/**
 * <code>Action</code> that sets a custom name of the corresponding trace by
 * showing a modal String chooser.
 * <p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.7 $
 */
public final class Trace2DActionSetName extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3904680491952451890L;

  /**
   * The component this instance will be registered to as a listener.
   * <p>
   *
   * @see Component#addMouseListener(java.awt.event.MouseListener)
   *
   */
  private Component m_trigger;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies
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
   * @param trigger
   *          the <code>Component</code> the modal dialog will
   *          be related to.
   */
  public Trace2DActionSetName(final ITrace2D trace, final String description,
      final Component trigger) {
    super(trace, description);
    this.m_trigger = trigger;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    String chosen = JOptionPane.showInputDialog(this.m_trigger,
        "Please input the new Name for trace " + this.m_trace.getName(), "Set trace name",
        JOptionPane.PLAIN_MESSAGE);
    if(chosen != null) {
      this.m_trace.setName(chosen);
    }
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop as this action will not be used
    // by checkbox or radio button menu items that have a state.
  }
}
