/*
 *  AJComponentAction, base for actions to trigger on JComponents.
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

import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

/**
 * <p>
 * The base class that connects triggered actions with an
 * {@link javax.swing.JComponent} instance.
 * </p>
 * <p>
 * Every subclass may delegate it's constructor-given <code>JComponent</code>
 * instance as protected member <code>m_component</code>.
 * </p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.5 $
 * 
 */
public abstract class AJComponentAction extends AbstractAction implements PropertyChangeListener {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = -9150128862126829805L;

  /** The target of this action. */
  protected JComponent m_component;

  /**
   * Create an <code>Action</code> that accesses the <code>JComponent</code>
   * and identifies itself with the given action String.
   * 
   * @param component
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public AJComponentAction(final JComponent component, final String description) {
    super(description);
    this.m_component = component;
  }
}
