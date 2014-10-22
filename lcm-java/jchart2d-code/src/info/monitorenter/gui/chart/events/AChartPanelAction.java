/*
 *  AChartPanelAction, base for actions to trigger on ChartPanel instances.
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

import info.monitorenter.gui.chart.views.ChartPanel;

import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

/**
 * The base class that connects triggered actions with an {@link ChartPanel}
 * instance.
 * <p>
 * Every subclass may delegate it's constructor-given <code>ChartPanel</code>
 * instance as protected member <code>m_chartpanel</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.3 $
 * 
 */
public abstract class AChartPanelAction extends AbstractAction implements PropertyChangeListener {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 5743382676218890737L;

  /** The target of this action. */
  protected ChartPanel m_chartpanel;

  /**
   * Create an <code>Action</code> that accesses the chart and identifies itself
   * with the given action String.
   * 
   * @param chartpanel
   *          the target the action will work on
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public AChartPanelAction(final ChartPanel chartpanel, final String description) {
    super(description);
    this.m_chartpanel = chartpanel;
  }

}
