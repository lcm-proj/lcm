/*
 *  Trace2DActionSetStroke, action to set a Stroke on an ITrace2D.
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

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory.PropertyChangeCheckBoxMenuItem;

import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * <p>
 * <code>Action</code> that sets a constructor-given zIndex to the
 * corresponding trace.
 * </p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.6 $
 */
public final class Trace2DActionSetStroke extends ATrace2DAction {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3978986583057707570L;

  /**
   * The stroke to set.
   */
  private final transient Stroke m_stroke;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies
   * itself with the given action String.
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
   * @param stroke
   *          the stroke property to set to the corresponding trace (see
   *          {@link ITrace2D#setStroke(Stroke)})
   */
  public Trace2DActionSetStroke(final ITrace2D trace, final String description, final Stroke stroke) {
    super(trace, description);
    this.m_stroke = stroke;
    trace.addPropertyChangeListener(ITrace2D.PROPERTY_STROKE, this);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    this.m_trace.setStroke(this.m_stroke);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (property.equals(ITrace2D.PROPERTY_STROKE)) {
      Stroke newValue = (Stroke) evt.getNewValue();
      // added
      if (newValue.equals(this.m_stroke)) {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(false), new Boolean(true));
      } else {
        this.firePropertyChange(PropertyChangeCheckBoxMenuItem.PROPERTY_SELECTED,
            new Boolean(true), new Boolean(false));
      }
    }
  }
}
