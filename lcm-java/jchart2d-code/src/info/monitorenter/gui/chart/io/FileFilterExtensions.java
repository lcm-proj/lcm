/*
 *
 *  FileFilterExtensions.java, a FileFilter implementation that
 *  filters files by their extension.
 *  Copyright (C) 2004 - 2011 Achim Westermann, created on 01.07.2004, 12:18:29
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
package info.monitorenter.gui.chart.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * <p>
 * Configureable implementation of {@link javax.swing.filechooser.FileFilter}
 * that filters files by their extension (e.g.: ".txt").
 * </p>
 * <p>
 * The extension Strings are provided to the constructor (no configuration of
 * initialized instance provided yet) and have to be the sole extension without
 * the dot.
 * </p>
 * <p>
 * This class is most often used to configure {@link javax.swing.JFileChooser}
 * dialogs. Therefore it accepts all directories to allow browsing.
 * </p>
 * <h3>Example usage:</h3>
 * <p>
 * 
 * <code>
 *  
 *    ...
 *    JFileChooser fileChooser = new JFileChooser();
 *    FileFilter soundFileFilter = new FileFilterExtensions(new String[]{&quot;wav&quot;,&quot;mp3&quot;});
 *    fileChooser.setFileFilter(soundFileFilter);
 *    ...
 *  
 * </code>
 * 
 * </p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de>Achim Westermann </a>
 */
public final class FileFilterExtensions
    extends FileFilter implements INameFilter {
  /** Filename extensions to filter for. */
  private String[] m_extensions;

  /** Flag for windows OS. */
  private boolean m_isWindows = false;

  /**
   * Creates an instance that will accept files with the given extensions.
   * <p>
   * 
   * @param extensionsWithoutDot
   *            A String[] containing extension strings without the dot like:
   *            <nobr><code>new String[]{"bat","txt","dict"}</code> </nobr>.
   * @throws IllegalArgumentException
   *             if the given extensions are inivalid.
   */
  public FileFilterExtensions(final String[] extensionsWithoutDot) throws IllegalArgumentException {
    this.verify(extensionsWithoutDot);
    this.m_extensions = new String[extensionsWithoutDot.length];
    System.arraycopy(extensionsWithoutDot, 0, this.m_extensions, 0, extensionsWithoutDot.length);
    this.m_isWindows = this.isWindows();
  }

  /**
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
  @Override
  public boolean accept(final File pathname) {
    boolean result;
    if (pathname.isDirectory()) {
      result = true;
    } else {
      result = this.acceptNoDirectory(pathname.getAbsolutePath());
    }
    return result;
  }

  /**
   * @see INameFilter#accept(String)
   */
  public boolean accept(final String urlstring) {
    boolean result;
    if (this.isDirectory(urlstring)) {
      result = true;
    } else {

      result = this.acceptNoDirectory(urlstring);
    }
    return result;
  }

  /**
   * Accept method used for files.
   * <p>
   * 
   * @param noDirFileNoURL
   *            the path to the file.
   * @return true if the file denoted by the given path is accepted.
   */
  private boolean acceptNoDirectory(final String noDirFileNoURL) {
    boolean ret = false;
    // search for extension without dot.
    StringTokenizer tokenizer = new StringTokenizer(noDirFileNoURL, ".");
    // a dot, because verify will not allow these
    String extension = "no.txt";
    // tokens: won't accept, if no extension in
    // pathname.
    while (tokenizer.hasMoreElements()) {
      extension = tokenizer.nextToken();
    }
    for (int i = this.m_extensions.length - 1; i >= 0; i--) {
      if (this.m_extensions[i].equals(extension)) {
        ret = true;
        break;
      }
    }
    return ret;
  }

  /**
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  @Override
  public String getDescription() {
    StringBuffer ret = new StringBuffer();
    int len = this.m_extensions.length;
    for (int i = 0; i < len; i++) {
      ret.append("*.").append(this.m_extensions[i]);
      if (i < (len - 1)) {
        ret.append(",");
      }
    }
    return ret.toString();
  }

  /**
   * Returns true if the given String denotes a directory.
   * <p>
   * 
   * @param urlstring
   *            the url format String pointing to a file.
   * @return true if the given String denotes a directory.
   */
  private boolean isDirectory(final String urlstring) {
    boolean ret = false;
    boolean isURL = false;
    try {
      // try, if URL (expensive):
      new URL(urlstring);
      isURL = true;
    } catch (MalformedURLException e) {
      // nop.
    }

    int lastDot = urlstring.lastIndexOf('.');
    int lastSeparator;
    // Could be minimized but is more talking this way.
    if (isURL) {
      lastSeparator = urlstring.lastIndexOf('/');
    } else {
      if (this.m_isWindows) {
        lastSeparator = urlstring.lastIndexOf('\\');
      } else {
        lastSeparator = urlstring.lastIndexOf('/');
      }
    }

    if (lastSeparator == -1) {
      if (lastDot == -1) {
        // top host without a path.
        ret = true;
      } else {
        ret = false;
      }
    } else {
      if (lastDot == -1) {
        ret = true;
      } else if (lastDot > (lastSeparator + 1)) {
        ret = false;
      } else {
        ret = true;
      }
    }
    return ret;
  }

  /**
   * Needed for {@link #isDirectory(String)}: We cannot use
   * {@link System#getProperty(java.lang.String)} to determine file separators
   * in applet context. That would possibly throw an SecurityAccessException.
   * <p>
   * 
   * @return true if current OS is windows.
   */
  private boolean isWindows() {
    boolean ret = false;
    File[] roots = FileSystemView.getFileSystemView().getRoots();
    for (int i = 0; i < roots.length; i++) {
      if (roots[i].getAbsolutePath().indexOf(':') != -1) {
        ret = true;
        break;
      }
    }
    return ret;
  }

  /**
   * Verifies the given extensions for valid format.
   * <p>
   * 
   * @param extensions
   *            The array with the Strings of extensions.
   * @throws IllegalArgumentException
   *             If a String of the array is null or contains a dot ('.').
   */
  private void verify(final String[] extensions) throws IllegalArgumentException {
    String current;
    StringBuffer msg = new StringBuffer();
    for (int i = extensions.length - 1; i >= 0; i--) {
      current = extensions[i];
      if (current == null) {
        msg.append("Extension at index " + i + " is null!\n");
      } else if (current.indexOf('.') != -1) {
        msg.append("Extension \"" + current + "\" contains a dot!\n");
      }
    }
    if (msg.length() > 0) {
      throw new IllegalArgumentException(msg.toString());
    }
  }
}
