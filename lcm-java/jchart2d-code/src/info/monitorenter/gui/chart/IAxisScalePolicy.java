/*
 *  IAxisScalePolicy.java of project jchart2d, <enterpurposehere>. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on Apr 22, 2011
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
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
 *
 * File   : $Source: /cvsroot/jchart2d/jchart2d/codetemplates.xml,v $
 * Date   : $Date: 2009/02/24 16:45:41 $
 * Version: $Revision: 1.2 $
 */

package info.monitorenter.gui.chart;

import java.awt.Graphics;
import java.util.List;

public interface IAxisScalePolicy {

  /**
   * Returns the array of labeled values that will be used by the
   * <code>{@link Chart2D}</code> to paint labels.
   * <p>
   * 
   * @param g2d
   *          Provides information about the graphic context (e.g. font
   *          metrics).
   * 
   * @param axis
   *          the axis to work for.
   * 
   * @return the labeled values that will be used by the <code>{@link Chart2D}
   *         </code> to paint labels.
   */
  public abstract List<LabeledValue> getScaleValues(final Graphics g2d, final IAxis<?> axis);

  /**
   * Performs expensive calculations for various values that are used by many
   * calls throughout a paint iterations.
   * <p>
   * These values are constant throughout a paint iteration by the contract that
   * no point is added removed or changed in this period. Because these values
   * are used from many methods it is impossible to calculate them at a
   * "transparent" method that may perform this caching over a paint period
   * without knowledge from outside. The first method called in a paint
   * iteration is called several further times in the iteration. So this is the
   * common hook to invoke before painting a chart.
   * <p>
   * 
   * @param axis
   *          the axis to read data from.
   */
  public void initPaintIteration(final IAxis<?> axis);

}