/*
 *  UnitMilli.java, unit for milli prefix.
 *  Copyright (C) 2004 - 2011 Achim Westermann.
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
package info.monitorenter.util.units;

/**
 * Milli unit, 10 <sup>-3 </sup>.
 * <p>
 *
 * @see info.monitorenter.util.units.AUnit
 *
 * @see info.monitorenter.util.units.UnitFactory
 *
 * @see info.monitorenter.util.units.IUnitSystem
 *
 * @see info.monitorenter.util.units.UnitSystemSI
 *
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 *
 * @version $Revision: 1.4 $
 */
public final class UnitMilli extends AUnit {
  
  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 7021486051162126177L;

  /**
   * Defcon.
   * <p>
   *
   */
  public UnitMilli() {
    this.m_factor = 0.001;
    this.m_unitName = "m";
  }
}
