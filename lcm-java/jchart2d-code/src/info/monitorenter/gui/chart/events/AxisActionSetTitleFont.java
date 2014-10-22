/*
 *  AxisActionSetTitle.java of project jchart2d, an action  implementation 
 *  to set the title font of an IAxis.
 *  Copyright (c) 2007 - 2011 Achim Westermann. 
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
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * <code>Action</code> that sets the title String of the
 * <code>{@link info.monitorenter.gui.chart.IAxisTitlePainter}</code> of the
 * <code>{@link IAxis}</code> specified by the constructor.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.6 $
 */
public class AxisActionSetTitleFont
    extends AAxisAction {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -7191047648236258328L;

  /** The title font to set when this action is triggered. */
  private Font m_titleFont;

  /**
   * Create an <code>Action</code> that accesses the chart's axis by argument
   * <code>axis</code> and identifies itself with the given action String.
   * <p>
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * @param axis
   *          needed to identify the axis of the chart: one of {@link Chart2D#X},
   *          {@link Chart2D#Y}.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   * @param font
   *          the font to set if this action is executed.
   */
  public AxisActionSetTitleFont(final Chart2D chart, final String description, final int axis,
      final Font font) {
    super(chart, description, axis);
    this.m_titleFont = font;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    this.getAxis().getAxisTitle().setTitleFont(this.m_titleFont);

  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    // will check for an axis replacement and transfer listening to the new axis if so: 
    super.propertyChange(evt);
    String property = evt.getPropertyName();
    if (property.equals(IAxis.AxisTitle.PROPERTY_TITLEFONT)) {
      Font newFont = (Font) evt.getNewValue();
      if (newFont.equals(this.m_titleFont)) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(false), new Boolean(true));

      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(true), new Boolean(false));
      }
    }
  }
}
