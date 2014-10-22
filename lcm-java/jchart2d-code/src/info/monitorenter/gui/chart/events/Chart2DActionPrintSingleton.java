/*
 *  Chart2DActionPrintSingleton, 
 *  singleton action that prints the chart with a dialog.
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

import info.monitorenter.gui.chart.Chart2D;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton <code>Action</code> that prints the current chart by showing a modal print dialog.
 * <p>
 * Only one instance per target component may exist.
 * <p>
 * 
 * @see info.monitorenter.gui.chart.events.Chart2DActionSetCustomGridColor
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.7 $
 */
public final class Chart2DActionPrintSingleton
    extends AChart2DAction {

  /** Generated <code>serialVersionUID</code>. * */
  private static final long serialVersionUID = 3475108617927262279L;

  /**
   * Map for instances.
   */
  private static Map<String, Chart2DActionPrintSingleton> instances = new HashMap<String, Chart2DActionPrintSingleton>();

  /**
   * Returns the single instance for the given component or null, if it is not existing.
   * <p>
   * 
   * @param chart
   *            the target the action will work on
   * @return the single instance for the given component or null.
   */
  public static Chart2DActionPrintSingleton getInstance(final Chart2D chart) {
    Chart2DActionPrintSingleton result = Chart2DActionPrintSingleton.instances
        .get(Chart2DActionPrintSingleton.key(chart));
    return result;
  }

  /**
   * Returns the single instance for the given component, potentially creating it.
   * <p>
   * If an instance for the given component had been created the description String is ignored.
   * <p>
   * 
   * @param chart
   *            the target the action will work on
   * @param actionName
   *            the descriptive <code>String</code> that will be displayed by
   *            {@link javax.swing.AbstractButton} subclasses that get this <code>Action</code>
   *            assigned ( {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * @return the single instance for the given component.
   */
  public static Chart2DActionPrintSingleton getInstance(final Chart2D chart, final String actionName) {
    Chart2DActionPrintSingleton result = Chart2DActionPrintSingleton.getInstance(chart);
    if (result == null) {
      result = new Chart2DActionPrintSingleton(chart, actionName);
      Chart2DActionPrintSingleton.instances.put(Chart2DActionPrintSingleton.key(chart), result);
    }
    return result;
  }

  /**
   * Creates a key for the component for internal storage.
   * <p>
   * 
   * @param chart
   *            the chart to generate the storage key for.
   * @return a storage key unique for the given chart instance.
   */
  private static String key(final Chart2D chart) {
    return chart.getClass().getName() + chart.hashCode();
  }

  /**
   * This is set to true when this action is triggered internally.
   * <p>
   * This signals the chart that is painting that a print cycle is running triggered by this action
   * and the chart should be printed on the whole page.
   * <p>
   * Requesting this value has to be synchronized together with the method
   * <code>{@link Chart2D#paint(java.awt.Graphics)}</code> method and reset to false from there.
   * <p>
   */
  private transient boolean m_printWholePage = false;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies itself with the given
   * action String.
   * <p>
   * 
   * @param chart
   *            the target the action will work on
   * @param colorName
   *            the descriptive <code>String</code> that will be displayed by
   *            {@link javax.swing.AbstractButton} subclasses that get this <code>Action</code>
   *            assigned ( {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  private Chart2DActionPrintSingleton(final Chart2D chart, final String colorName) {
    super(chart, colorName);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {

    PrinterJob job = PrinterJob.getPrinterJob();
    job.setPrintable(this.m_chart);
    boolean ok = job.printDialog();
    if (ok) {
      this.m_printWholePage = true;
      try {
        job.print();
      } catch (PrinterException ex) {
        /* The job did not successfully complete */
      } finally {
        this.m_printWholePage = false;
        this.m_chart.resetPrintMode();
      }
    }
  }

  /**
   * Returns true if this action triggered a print request for the corresponding chart.
   * <p>
   * 
   * @return true if this action triggered a print request for the corresponding chart.
   */
  public final boolean isPrintWholePage() {
    return this.m_printWholePage;
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop
  }

  /**
   * Resets the printing modus for the corresponding chart triggered by this action.
   * <p>
   * Has to be called by <code>{@link Chart2D#paint(java.awt.Graphics)}</code> when printing was
   * triggered from here.
   * <p>
   */
  public final void resetPrintWholePage() {
    this.m_printWholePage = false;
  }
}
