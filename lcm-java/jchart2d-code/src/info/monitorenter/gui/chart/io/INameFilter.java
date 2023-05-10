/*
 *
 *  INameFilter.java  of project jchart2d, a filter working with URLs instead of just Files. 
 *  Copyright (C) 2004 - 2011 Achim Westermann, created on 03.07.2004, 22:18:53
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

/**
 * A story on the side: There are:
 * <ul>
 * <li>{@link java.io.FileFilter}
 * <li>{@link java.io.FilenameFilter}
 * <li>{@link javax.swing.filechooser.FileFilter}
 * </ul>
 * <p>
 * Dumb all over hein? Well here's another one. Because we cannot stick to File
 * instances when working with URL's .
 * <p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.3 $
 *
 */
public interface INameFilter {
  /**
   * Accept the file denoted by the given url String.
   * <p>
   *
   * @param urlstring
   *          a String in url format denoting a file.
   *
   * @return true if the file denoted by the given url String is accepted.
   *
   */
  public boolean accept(String urlstring);

}
