/*
 *  UnitSystemSI.java, unit system for the "International System of Units" (abbr. SI).
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
 * The unit system for the "International System of Units" (SI).
 * <p>
 *
 * @see info.monitorenter.util.units.AUnit
 *
 * @see info.monitorenter.util.units.UnitFactory
 *
 * @see info.monitorenter.util.units.IUnitSystem
 *
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 *
 * @version $Revision: 1.7 $
 */
public final class UnitSystemSI implements IUnitSystem {

  /** Singleton instance. */
  private static IUnitSystem instance;

  /** The unit classes of this system. */
  private static final Class<?>[] UNITS = new Class[] {UnitFemto.class, UnitNano.class,
      UnitMicro.class, UnitMilli.class, UnitUnchanged.class, UnitKilo.class, UnitMega.class,
      UnitGiga.class, UnitTera.class, UnitPeta.class };

  /**
   * Singleton retrieval method.
   * <p>
   *
   * @return the unique instance within the current VM.
   */
  public static IUnitSystem getInstance() {
    if (UnitSystemSI.instance == null) {
      UnitSystemSI.instance = new UnitSystemSI();
    }
    return UnitSystemSI.instance;
  }

  /**
   * Singleton constructor.
   * <p>
   *
   */
  private UnitSystemSI() {
    // nop
  }

  /**
   * @see info.monitorenter.util.units.IUnitSystem#getUnits()
   */
  public Class<?>[] getUnits() {
    int len = UnitSystemSI.UNITS.length;
    Class<?>[] result = new Class[len];
    System.arraycopy(UnitSystemSI.UNITS, 0, result, 0, len);
    return result;
  }
}
