/*
 *  Trace2DActionAddRemoveTracePainter, action to set a ITracePainter on an ITrace2D.
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

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePainter;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractButton;

/**
 * <code>Action</code> that adds or removes constructor-given
 * {@link info.monitorenter.gui.chart.ITracePainter} to the corresponding trace.
 * <p>
 * This action only works in combination with {@link AbstractButton}
 * instances that send themselves as the event object to the
 * {@link java.awt.event.ActionEvent}( {@link java.util.EventObject#getSource()}
 * ) because <code>{@link AbstractButton#isSelected()}</code> is needed for state check.
 * <p>
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.8 $
 */
public final class Trace2DActionAddRemoveTracePainter extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3978986583057707570L;

  /**
   * The stroke to set.
   */
  private ITracePainter< ? > m_tracePainter;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies itself
   * with the given action String.
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
   * @param painter
   *          the painter to add / remove from the trace.
   */
  public Trace2DActionAddRemoveTracePainter(final ITrace2D trace, final String description,
      final ITracePainter< ? > painter) {
    super(trace, description);
    this.m_tracePainter = painter;
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_PAINTERS, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    AbstractButton item = (AbstractButton) e.getSource();
    boolean state = item.isSelected();
    if (state) {
      this.m_trace.addTracePainter(this.m_tracePainter);
    } else {
      boolean success = this.m_trace.removeTracePainter(this.m_tracePainter);
      if (success) {
        // nop
      } else {
        // rewind state as this could not be done:
        // contract for ITrace2D is, that at least one renderer should be
        // defined!
        item.setSelected(true);
        item.invalidate();
        item.repaint();
      }
    }
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(ITrace2D.PROPERTY_PAINTERS)) {
      ITracePainter< ? > oldValue = (ITracePainter< ? >) evt.getOldValue();
      ITracePainter< ? > newValue = (ITracePainter< ? >) evt.getNewValue();
      // added or removed?
      if (oldValue == null) {
        // added
        if (newValue.getClass() == this.m_tracePainter.getClass()) {
          this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, new Boolean(
              false), new Boolean(true));
        }
      } else {
        // removed
        if (oldValue.getClass() == this.m_tracePainter.getClass()) {
          this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, new Boolean(
              true), new Boolean(false));
        }
      }
    }
  }
}
