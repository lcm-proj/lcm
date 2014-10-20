/*
 *  IAxisTitlePainter.java of project jchart2d, interface for an painter of the 
 *  title of an axis of the Chart2D.
 *  Copyright 2004 - 2011 (C) Achim Westermann.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart;

import java.awt.Graphics;
import java.io.Serializable;

/**
 * Interface for an painter of the title of an axis of the Chart2D.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 2.2.1
 * 
 */
public interface IAxisTitlePainter extends Serializable {

 

  /**
   * Returns the height of this axis title in px with respect to the current
   * title of the given axis title.
   * <p>
   * 
   * @param axis
   *          the instance this title painter is working for.
   *          
   * @param g2d
   *          needed for size informations (e.g. font widths).
   * 
   * @return the height of this axis title in px with respect to the current
   *         title of the given axis.
   */
  public int getHeight(final IAxis<?> axis, final Graphics g2d);


  /**
   * Returns the width of this axis title in px with respect to the current
   * title of the given axis.
   * <p>
   * 
   * @param axis
   *          the instance this title painter is working for.
   * 
   * @param g2d
   *          needed for size informations (e.g. font widths).
   * 
   * @return the width of this axis title in px with respect to the current
   *         title of the given axis.
   */
  public int getWidth(final IAxis<?> axis, final Graphics g2d);

  /**
   * Invoked to let implementations paint the given title of the given axis.
   * <p>
   * 
   * Implementations should make use of the information about the axis
   * coordinates (start pixel,end pixel) and the graphics context (for font
   * dimensions) to do it right.
   * <p>
   * 
   * @param axis
   *          the axis to paint the title of.
   * @param g
   *          needed for size informations.
   */
  public void paintTitle(final IAxis<?> axis, final Graphics g);

}
