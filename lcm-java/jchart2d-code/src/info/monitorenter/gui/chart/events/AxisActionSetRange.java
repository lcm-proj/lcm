/*
 *  AxisActionSetRange.java of project jchart2d
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 20:30:06.
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
import info.monitorenter.gui.chart.controls.RangeChooserPanel;
import info.monitorenter.gui.chart.dialogs.ModalDialog;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

/**
 * <code>Action</code> that sets the range of an
 * {@link info.monitorenter.gui.chart.axis.AAxis} of a chart (
 * {@link info.monitorenter.gui.chart.Chart2D}) that will be used by it's
 * viewport (
 * {@link info.monitorenter.gui.chart.axis.AAxis#setRangePolicy(info.monitorenter.gui.chart.IRangePolicy)}
 * ) by showing a modal range chooser.
 * <p>
 * 
 * This only works if the bislider.jar file is in the classpath.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * 
 * @version $Revision: 1.12 $
 */
public class AxisActionSetRange extends AAxisAction {

  /**
   * Flag set whenever the proper jar file (apache-xmlgraphics-commons) is in
   * the classpath.
   */
  public static final boolean RANGE_CHOOSER_SUPPORTED;

  static {
    Class< ? > test = null;
    try {
      // Do a fake operation that will not be inlined by the compiler:
      test = Class.forName("com.visutools.nav.bislider.BiSlider");

    } catch (Throwable ncde) {
      // nop
    } finally {
      if (test != null) {
        RANGE_CHOOSER_SUPPORTED = true;
      } else {
        RANGE_CHOOSER_SUPPORTED = false;
      }
    }
  }

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3258694286479406393L;

  /**
   * Create an <code>Action</code> that accesses the chart's axis by argument
   * <code>axis</code> and identifies itself with the given action String.
   * <p>
   * 
   * @param chart
   *          the owner of the axis to trigger actions upon.
   * 
   * @param axis
   *          needed to identify the axis of the chart: one of {@link Chart2D#X}
   *          , {@link Chart2D#Y}.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public AxisActionSetRange(final Chart2D chart, final String description, final int axis) {
    super(chart, description, axis);
    if (!RANGE_CHOOSER_SUPPORTED) {
      this.setEnabled(false);
    }
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {

    IAxis<?> axis = this.getAxis();
    RangeChooserPanel rangePanel = new RangeChooserPanel(axis.getRangePolicy().getRange());
    ModalDialog dialog = new ModalDialog(axis.getAccessor().getChart(), "Choose a range",
        rangePanel);
    dialog.showDialog();
    axis.setRange(rangePanel.getRange());
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    // will check for an axis replacement and transfer listening to the new axis if so: 
    super.propertyChange(evt);
    // nop as this action will not be used
    // by checkbox or radio button menu items that have a state.
  }
}
