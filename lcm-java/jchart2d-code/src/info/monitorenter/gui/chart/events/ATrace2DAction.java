/*
 *  ATrace2DAction.java, base for actions to trigger on traces.
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

import java.beans.PropertyChangeListener;

import info.monitorenter.gui.chart.ITrace2D;

import javax.swing.AbstractAction;

/**
 * The base class that connects triggered actions with an {@link ITrace2D}
 * instance.
 * <p>
 * Every subclass may access it's constructor-given <code>ITrace2D</code>
 * instance as protected member <code>m_trace</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.6 $
 * 
 */
public abstract class ATrace2DAction extends AbstractAction implements PropertyChangeListener {
  
  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 374572347443757594L;
 
  /** The trace to trigger the action upon. */
  protected ITrace2D m_trace;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies
   * itself with the given action String.
   * 
   * @param trace
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public ATrace2DAction(final ITrace2D trace, final String description) {
    super(description);
    this.m_trace = trace;
  }
}
