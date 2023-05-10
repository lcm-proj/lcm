/*
 *  ErrorBarPolicyActionRemovePainter.java of project jchart2d, action 
 *  that removes the given IErrorBarPainter from the given IErrorBarPolicy. 
 *  Copyright (c) 2006 - 2011 Achim Westermann, created on 09.12.2006 00:14:25.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart.events;

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.util.UIUtil;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * Action that removes the constructor given
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code> from he
 * constructor given
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.10 $
 */
public class ErrorBarPolicyActionRemovePainter extends AErrorBarPolicyAction {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -2354747899391850767L;

  /** The painter to remove from the error bar policy. */
  private final IErrorBarPainter m_errorBarPainter;

  /**
   * Create an instance that accesses the error bar policy to remove a painter
   * from it with the given action String.
   * 
   * @param errorBarPolicy
   *          the target the action will work on.
   * 
   * @param errorBarpainter
   *          the error bar painter to remove from the given error bar policy.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public ErrorBarPolicyActionRemovePainter(final IErrorBarPolicy< ? > errorBarPolicy,
      final String description, final IErrorBarPainter errorBarpainter) {
    super(errorBarPolicy, description);
    this.m_errorBarPainter = errorBarpainter;

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    // update UI, these two steps have to be performed before
    // the remove operation - the window will not be found afterwards:
    Component component = (Component) e.getSource();
    Window dialog = UIUtil.findFrame(component);

    // remove operation:
    this.m_errorBarPolicy.removeErrorBarPainter(this.m_errorBarPainter);

    // update UI, final step:
    dialog.pack();
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // TODO Auto-generated method stub
  }

}
