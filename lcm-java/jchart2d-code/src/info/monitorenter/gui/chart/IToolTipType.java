/*
 *  IToolTipType.java of project jchart2d, interface for tool tips of the chart. 
 *  Copyright (C) 2004 - 2011, Achim Westermann.
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/IToolTipType.java,v $
 * Date   : $Date: 2011/01/14 08:36:10 $
 * Version: $Revision: 1.5 $
 */

package info.monitorenter.gui.chart;

import java.awt.event.MouseEvent;

/**
 * Defines the tool tips to display on a <code>{@link Chart2D}</code>.<p>
 * 
 * @see Chart2D#setToolTipType(IToolTipType)
 * 
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 **/
public interface IToolTipType {

  /**
   * Returns a description of this tool tip type (for UI display).<p> 
   * 
   * @return a description of this tool tip type (for UI display).
   */
  public abstract String getDescription();
  
  /**
   * The base class implementation that returns the tool tip text for the
   * given mouse event which is a NONE implementation here.
   * <p>
   * 
   * @param chart
   *          the chart for computation of tool tips.
   * @param me
   *          the corresponding mouse event.
   * @return the tool tip text for the given mouse event.
   */
  public abstract String getToolTipText(final Chart2D chart, final MouseEvent me);

}
