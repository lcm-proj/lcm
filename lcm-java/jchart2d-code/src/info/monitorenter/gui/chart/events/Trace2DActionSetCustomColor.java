/*
 *  Trace2DActionSetCustomColor, action to set a custom (runtime chosen) 
 *  color to a ITrace2D.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JColorChooser;

/**
 * <code>Action</code> that sets a custom color of the corresponding trace by
 * showing a modal color chooser.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.5 $
 */
public final class Trace2DActionSetCustomColor extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3904680491952451890L;

  /**
   * Reference to the last custom color chosen to check wether the corresponding
   * menu is selected.
   */
  private Color m_lastChosen;

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
   *          {@link  javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link  javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   * @param trigger
   *          the <code>Component</code> the modal color chooser dialog will
   *          be related to.
   *          <p>
   */
  public Trace2DActionSetCustomColor(final ITrace2D trace, final String description,
      final Component trigger) {
    super(trace, description);
    this.m_trigger = trigger;
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_COLOR, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    Color chosen = JColorChooser.showDialog(this.m_trigger, "choose color for "
        + this.m_trace.getName(), this.m_trace.getColor());
    this.m_lastChosen = chosen;
    this.m_trace.setColor(chosen);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(ITrace2D.PROPERTY_COLOR)) {
      Color newValue = (Color) evt.getNewValue();
      if (newValue.equals(this.m_lastChosen)) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(false), new Boolean(true));
      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(true), new Boolean(false));
      }
    }
  }
}
