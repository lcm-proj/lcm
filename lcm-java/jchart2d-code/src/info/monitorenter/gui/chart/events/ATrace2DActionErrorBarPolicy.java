/*
 *  Trace2DActionAErrorBarPolicy, abstract action to act 
 *  with an IErrorBarPolicy on an ITrace2D.
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

import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;

/**
 * Abstract <code>Action</code> to act with an IErrorBarPolicy on an
 * <code>{@link ITrace2D}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.4 $
 */
public abstract class ATrace2DActionErrorBarPolicy extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = -6043978306287017533L;

  /** The error bar policy to use. */
  private IErrorBarPolicy< ? > m_errorBarPolicy;

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
  public ATrace2DActionErrorBarPolicy(final ITrace2D trace, final String description,
      final IErrorBarPolicy< ? > errorBarPolicy) {
    super(trace, description);
    this.m_errorBarPolicy = errorBarPolicy;
  }

  /**
   * Returns the error bar policy that is used by this action.
   * <p>
   * 
   * @return the error bar policy that is used by this action.
   */
  final IErrorBarPolicy< ? > getErrorBarPolicy() {
    return this.m_errorBarPolicy;
  }

}
