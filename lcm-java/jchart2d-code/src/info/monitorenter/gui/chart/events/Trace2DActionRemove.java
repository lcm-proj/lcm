/*
 *  Trace2DActionRemove, action to remove a trace from a chart.
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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * <code>Action</code> that removes the constructor given
 * <code>{@link info.monitorenter.gui.chart.ITrace2D}</code> from the
 * corresponding <code>{@link info.monitorenter.gui.chart.Chart2D}</code>.
 * <p>
 * 
 * This action is "destructive" - the trace will be lost. There exists no
 * counterpart to add a trace yet (there is no fixed data format / source for
 * creating a chart).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 */
public final class Trace2DActionRemove
    extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = -6161504244812708443L;

  /**
   * Create an <code>Action</code> that removes the given the trace from it's
   * chart upon action.
   * <p>
   * 
   * @param trace
   *          the target the action will work on.
   * 
   * @param name
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public Trace2DActionRemove(final ITrace2D trace, final String name) {
    super(trace, name);
    Icon closeIcon = UIManager.getDefaults().getIcon("InternalFrame.paletteCloseIcon");
    if (closeIcon != null) {
      this.putValue(Action.SMALL_ICON, closeIcon);
    }
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    if (this.m_trace != null) {
      Chart2D chart = this.m_trace.getRenderer();
      if (chart != null) {

        chart.removeTrace(this.m_trace);
        this.m_trace = null;

      } else {
        // make the trace gcable... this is not mandatory as
        // the whole menu will be dropped from above and make
        // everything gcable when trace is removed.
        this.m_trace = null;
      }
    }
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop
  }
}
