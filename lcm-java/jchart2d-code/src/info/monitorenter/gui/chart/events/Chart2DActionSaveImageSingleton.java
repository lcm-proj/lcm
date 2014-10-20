/*
 *  Chart2DActionSaveImageSingleton, 
 *  singleton action that saves the chart to an image.
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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Singleton <code>Action</code> that saves the current chart to an image at the location
 * specified by showing a modal file chooser save dialog.
 * <p>
 * Only one instance per target component may exist.
 * <p>
 * 
 * @see info.monitorenter.gui.chart.events.Chart2DActionSetCustomGridColor
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.12 $
 */
public final class Chart2DActionSaveImageSingleton
    extends AChart2DAction {
  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = -2800571545563022874L;

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
  public static Chart2DActionSaveImageSingleton getInstance(final Chart2D chart,
      final String actionName) {
    Chart2DActionSaveImageSingleton result = Chart2DActionSaveImageSingleton.instances
        .get(Chart2DActionSaveImageSingleton.key(chart));
    if (result == null) {
      result = new Chart2DActionSaveImageSingleton(chart, actionName);
      Chart2DActionSaveImageSingleton.instances.put(Chart2DActionSaveImageSingleton.key(chart),
          result);
    }
    return result;
  }

  /**
   * The <code>JFileChooser</code> used to choose the location for saving snapshot images.
   * <p>
   */
  private JFileChooser m_filechooser;

  /**
   * Map for instances.
   */
  private static Map<String, Chart2DActionSaveImageSingleton> instances = new HashMap<String, Chart2DActionSaveImageSingleton>();

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
  private Chart2DActionSaveImageSingleton(final Chart2D chart, final String colorName) {
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
    // Immediately get the image:
    BufferedImage img = this.m_chart.snapShot();
    // clear file filters (uncool API)

    FileFilter[] farr = this.m_filechooser.getChoosableFileFilters();
    for (int i = 0; i < farr.length; i++) {
      this.m_filechooser.removeChoosableFileFilter(farr[i]);
    }
    // collect capable writers by format name (API even gets worse!)
    String[] encodings = ImageIO.getWriterFormatNames();
    Set<String> writers = new TreeSet<String>();
    ImageTypeSpecifier spec = ImageTypeSpecifier.createFromRenderedImage(img);
    Iterator<ImageWriter> itWriters;
    for (int i = 0; i < encodings.length; i++) {
      itWriters = ImageIO.getImageWriters(spec, encodings[i]);
      if (itWriters.hasNext()) {
        writers.add(encodings[i].toLowerCase());
      }
    }
    // add the file filters:
    Iterator<String> itWriterFormats = writers.iterator();
    String extension;
    while (itWriterFormats.hasNext()) {
      extension = itWriterFormats.next();
      this.m_filechooser
          .addChoosableFileFilter(new FileFilterExtensions(new String[] {extension }));
    }

    int ret = this.m_filechooser.showSaveDialog(this.m_chart);
    if (ret == JFileChooser.APPROVE_OPTION) {
      File file = this.m_filechooser.getSelectedFile();
      // get the encoding
      extension = this.m_filechooser.getFileFilter().getDescription().substring(2);
      ImageWriter imgWriter = ImageIO.getImageWritersBySuffix(extension).next();
      // parameters for the writer:
      ImageWriteParam params = imgWriter.getDefaultWriteParam();
      if (params.canWriteCompressed()) {
        params.setCompressionMode(ImageWriteParam.MODE_DISABLED);
        // params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // params.setCompressionQuality(1.0f);
      }
      try {
        imgWriter.setOutput(new FileImageOutputStream(new File(file.getAbsolutePath() + "."
            + extension)));
        imgWriter.write(img);
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
