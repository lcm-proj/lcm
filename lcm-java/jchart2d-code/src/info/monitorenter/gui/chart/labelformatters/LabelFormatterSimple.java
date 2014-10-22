/*
 *
 *  LabelFormatterSimple.java of project jchart2d. The most simple label formatter. 
 *  Copyright (C) 2005 - 2011 Achim Westermann, created on 20.04.2005, 09:54:15
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
package info.monitorenter.gui.chart.labelformatters;

import info.monitorenter.gui.chart.IAxisLabelFormatter;

import java.text.DecimalFormat;

/**
 * An IlabelFormatter implementation that just returns
 * <code>String.valueOf(value)</code>.
 * <p>
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 * @version $Revision: 1.10 $
 *
 */
public final class LabelFormatterSimple extends LabelFormatterNumber implements IAxisLabelFormatter {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 432931329620546628L;

  /**
   * Default constructor that limits the internal maximum fraction digits to 2
   * and the maximum integer digits to 16.
   * <p>
   *
   */
  public LabelFormatterSimple() {
    super(new DecimalFormat("#.#"));
    this.m_numberFormat.setMaximumFractionDigits(4);
    this.m_numberFormat.setMaximumIntegerDigits(16);
  }

}
