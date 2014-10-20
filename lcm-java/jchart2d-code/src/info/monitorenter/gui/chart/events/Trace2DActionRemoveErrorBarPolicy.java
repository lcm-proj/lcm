/*
 *  Trace2DActionRemoveErrorBarPolicy, action to remove an IErrorBarPolicy 
 *  from an ITrace2D.
 *  Copyright (C) 2007 - 2011 Achim Westermann
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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;

/**
 * <code>Action</code> to remove an IErrorBarPolicy from an
 * <code>{@link ITrace2D}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.5 $
 */
public final class Trace2DActionRemoveErrorBarPolicy extends ATrace2DActionErrorBarPolicy {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = -6819371146517378618L;

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
   * @param errorBarPolicy
   *          the error bar policy to use by this action.
   * 
   */
  public Trace2DActionRemoveErrorBarPolicy(final ITrace2D trace, final String description,
      final IErrorBarPolicy< ? > errorBarPolicy) {
    super(trace, description, errorBarPolicy);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    this.m_trace.removeErrorBarPolicy(this.getErrorBarPolicy());
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // TODO Auto-generated method stub
  }
}
