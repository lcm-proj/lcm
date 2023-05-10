/*
 *  ChartActionSetToolTipType.java of project jchart2d
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 00:13:29.
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
import info.monitorenter.gui.chart.IToolTipType;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * <code>Action</code> that invokes
 * {@link info.monitorenter.gui.chart.Chart2D#setToolTipType(IToolTipType)} with a
 * constructor given {@link info.monitorenter.gui.chart.IToolTipType}.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.3 $
 */
public class ChartActionSetToolTipType extends AChart2DAction {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = -5816028313134616682L;

  /** The tool tip type to use. */
  private IToolTipType m_toolTipType = null;

  /**
   * Create an <code>Action</code> that sets the constructor given
   * <code>{@link IToolTipType}</code> to the chart.
   * <p>
   * 
   * @see Chart2D#setToolTipType(IToolTipType)
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * @param toolTipType
   *          the tool tip type to use.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   */
  public ChartActionSetToolTipType(final Chart2D chart, final String description,
      final IToolTipType toolTipType) {
    super(chart, description);
    this.m_toolTipType = toolTipType;
    chart.addPropertyChangeListener(Chart2D.PROPERTY_TOOLTIP_TYPE, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    this.m_chart.setToolTipType(this.m_toolTipType);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(Chart2D.PROPERTY_TOOLTIP_TYPE)) {
      IToolTipType newType = (IToolTipType) evt.getNewValue();
      if (newType.equals(this.m_toolTipType)) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, Boolean
            .valueOf(false), Boolean.valueOf(true));

      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED, Boolean
            .valueOf(true), Boolean.valueOf(false));
      }
    }
  }
}
