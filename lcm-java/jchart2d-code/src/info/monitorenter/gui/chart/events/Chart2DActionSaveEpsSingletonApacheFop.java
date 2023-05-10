/*
 *  Chart2DActionSaveEpsSingleton, 
 *  singleton action that saves the chart to an encapsulated postscript
 *  file.
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
import info.monitorenter.gui.chart.io.FileFilterExtensions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;

/**
 * Singleton <code>Action</code> that saves the current chart to an encapsulated
 * postscript file at the the location specified by showing a modal file chooser
 * save dialog.
 * <p>
 * Only one instance per target component may exist.
 * <p>
 * 
 * @see info.monitorenter.gui.chart.events.Chart2DActionSetCustomGridColor
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.4 $
 */
public final class Chart2DActionSaveEpsSingletonApacheFop {

  /**
   * Dummy action that is always disabled.
   * <p>
   * 
   */
  private static class ActionDisabledDummy extends AbstractAction {
    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 5537391176736852739L;

    /**
     * Creates an instance with the given name.
     * <p>
     * 
     * @param name
     *          the name to use.
     */
    public ActionDisabledDummy(final String name) {
      super(name);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
      // nop

    }

    /**
     * @see AbstractAction#isEnabled()
     */
    @Override
    public boolean isEnabled() {
      return false;
    }

  }

  /**
   * Implementation of the action that depends on jlibeps and must not be class
   * loaded in case the corresponding classes are missing in the classpath.
   * 
   */

  private static final class Chart2DActionSaveEps extends AChart2DAction {

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 4165725826047395414L;

    /**
     * The <code>JFileChooser</code> used to choose the location for saving
     * snapshot images.
     * <p>
     */
    private JFileChooser m_filechooser;

    /**
     * Create an <code>Action</code> that accesses the trace and identifies
     * itself with the given action String.
     * <p>
     * 
     * @param chart
     *          the target the action will work on
     * @param colorName
     *          the descriptive <code>String</code> that will be displayed by
     *          {@link javax.swing.AbstractButton} subclasses that get this
     *          <code>Action</code> assigned (
     *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}
     *          ).
     */
    public Chart2DActionSaveEps(final Chart2D chart, final String colorName) {
      super(chart, colorName);
      chart.addPropertyChangeListener(Chart2D.PROPERTY_GRID_COLOR, this);
      // configure the file chooser:
      this.m_filechooser = new JFileChooser();
      this.m_filechooser.setAcceptAllFileFilterUsed(false);

    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
      // clear file filters (uncool API)
      FileFilter[] farr = this.m_filechooser.getChoosableFileFilters();
      for (int i = 0; i < farr.length; i++) {
        this.m_filechooser.removeChoosableFileFilter(farr[i]);
      }

      String extension = "eps";
      this.m_filechooser
          .addChoosableFileFilter(new FileFilterExtensions(new String[] {extension }));
      int ret = this.m_filechooser.showSaveDialog(this.m_chart);
      if (ret == JFileChooser.APPROVE_OPTION) {
        File file = new File(this.m_filechooser.getSelectedFile().getAbsolutePath() + "."
            + extension);
        // get the encoding
        try {
          FileOutputStream outStream = new FileOutputStream(file);
          
          EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(true);
          g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
          g2d.setupDocument(outStream, this.m_chart.getWidth(),this.m_chart.getHeight()); 

//          org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D g = new org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D(
//              "Title", outStream, 0, 0, this.m_chart.getWidth(), this.m_chart.getHeight());
          //g.scale(0.24, 0.24);

          
          this.m_chart.paint(g2d);
//          g.flush();
//          g.close();
          outStream.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }

    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent evt) {
      // nop
    }
  }

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -7446202109342192546L;

  /** Flag set whenever the proper jar file (apache-xmlgraphics-commons) is in the classpath. */
  public static final boolean EPS_SUPPORTED;

  /**
   * Map for instances.
   */
  private static Map<String, Action> instances = new HashMap<String, Action>();

  static {
    Class< ? > test = null;
    try {
      // Do a fake operation that will not be inlined by the compiler:
      test = Class.forName("org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D");

    } catch (Throwable ncde) {
      // nop
    } finally {
      if (test != null) {
        EPS_SUPPORTED = true;
      } else {
        EPS_SUPPORTED = false;
      }
    }
  }

  /**
   * Returns the single instance for the given component, potentially creating
   * it.
   * <p>
   * If an instance for the given component had been created the description
   * String is ignored.
   * <p>
   * 
   * @param chart
   *          the target the action will work on
   * @param actionName
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * @return the single instance for the given component.
   */
  public static Action getInstance(final Chart2D chart, final String actionName) {
    Action result = Chart2DActionSaveEpsSingletonApacheFop.instances.get(Chart2DActionSaveEpsSingletonApacheFop
        .key(chart));
    if (result == null) {
      if (Chart2DActionSaveEpsSingletonApacheFop.EPS_SUPPORTED) {
        result = new Chart2DActionSaveEps(chart, actionName);
      } else {
        result = new ActionDisabledDummy(actionName);
      }
      Chart2DActionSaveEpsSingletonApacheFop.instances.put(Chart2DActionSaveEpsSingletonApacheFop.key(chart), result);
    }
    return result;
  }

  /**
   * Creates a key for the component for internal storage.
   * <p>
   * 
   * @param chart
   *          the chart to generate the storage key for.
   * @return a storage key unique for the given chart instance.
   */
  private static String key(final Chart2D chart) {
    return chart.getClass().getName() + chart.hashCode();
  }

  /**
   * Singleton constructor.
   * <p>
   */
  private Chart2DActionSaveEpsSingletonApacheFop() {
    // nop
  }
}
