/*
 *  UnitFactory.java, Singleton that caches instances of whole unit- systems 
 *  and provides you with the matching unit for a maximum value.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Singleton that caches instances of whole unit- systems and provides you with
 * the matching unit for a maximum value.
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.10 $
 * 
 * @see info.monitorenter.util.units.IUnitSystem
 * 
 */
public final class UnitFactory
    extends Object {

  /** Singleton instance. */
  private static UnitFactory instance;

  /**
   * Marker unit that represents a "non-unit" that does not modify anything in
   * {@link AUnit#getValue(double)}.
   */
  public static final AUnit UNCHANGED = new UnitUnchanged();

  /** Cache for {@link IUnitSystem} instances. */
  private static final Map<String, List<AUnit>> UNITSYSTEMS = new HashMap<String, List<AUnit>>();

  /**
   * Singleton retrieval method.
   * <p>
   * 
   * @return the unique instance within the current VM.
   */
  public static UnitFactory getInstance() {
    if (UnitFactory.instance == null) {
      UnitFactory.instance = new UnitFactory();
    }
    return UnitFactory.instance;
  }

  /**
   * Singleton constructor.
   * <p>
   * 
   */
  private UnitFactory() {
    // nop
  }

  /**
   * Returns the unit for the given argument absolute max.
   * <p>
   * 
   * The unit is chosen in a way that
   * 
   * 
   * @param absoluteMax
   *            the absolute maximum value that has to be put into relation to
   *            the unit to retrieve.
   * @param units
   *            the UnitSystem to use.
   * @return the unit for the given argument absolute max.
   */
  public AUnit getUnit(final double absoluteMax, final IUnitSystem units) {
    AUnit result = UnitFactory.UNCHANGED;
    List<AUnit> choice;
    // lazy initialization
    choice = UnitFactory.UNITSYSTEMS.get(units.getClass().getName());
    if (choice == null) {
      choice = this.initUnitSystem(units);
    }

    // in case we are below the lowest unit (absoluteMax < peek.getFactor() of
    // the first) start from the lowest unit.
    result = choice.get(0);
    // Now to find the right unit.
    for (AUnit peek : choice) {
      if (absoluteMax < (peek.getFactor())) {
        // return the previous unit iterated in case we hit the ceiling:
        break;
      }
      result = peek;
    }
    // highest unit available
    return result;
  }

  /**
   * Returns a list of all different {@link AUnit} instances available in the
   * given unit system.
   * <p>
   * 
   * @param unitsystem
   *            the unit system of interest.
   * 
   * @return a list of all different {@link AUnit} instances available in the
   *         given unit system.
   */
  public List<AUnit> getUnits(final IUnitSystem unitsystem) {
    List<AUnit> choice = UnitFactory.UNITSYSTEMS.get(unitsystem.getClass().getName());
    // lazy initialization
    if (choice == null) {
      choice = this.initUnitSystem(unitsystem);
    }
    return choice;

  }

  /**
   * Internally loads and caches the unit system.
   * <p>
   * 
   * @param units
   *            the unit system to initialize.
   * 
   * @return the list of {@link AUnit} instances of the given unit system.
   */
  private List<AUnit> initUnitSystem(final IUnitSystem units) {
    List<AUnit> choice = new LinkedList<AUnit>();
    Class<?>[] clazzs = units.getUnits();
    AUnit unit = null;
    AUnit previous = null;
    for (int i = 0; i < clazzs.length; i++) {
      if (!AUnit.class.isAssignableFrom(clazzs[i])) {
        System.err.println("UnitFactory: wrong class " + clazzs[i].getName() + " delivered by "
            + units.getClass().getName());
        continue;
      } else {
        try {
          unit = (AUnit) clazzs[i].newInstance();
          choice.add(unit);
          if (previous != null) {
            previous.m_nextHigherUnit = unit;
            unit.m_nexLowerUnit = previous;
          }
          previous = unit;
        } catch (InstantiationException e) {
          System.err.println("UnitFactory encountered problems by instantiation of "
              + clazzs[i].getName());
        } catch (IllegalAccessException f) {
          System.err.println("UnitFactory has no access to " + clazzs[i].getName());
        }
      }
    }
    // hardcoded minsearch sort:
    double tmpfactori;
    double tmpfactorj;
    int min;
    int stop = choice.size();
    for (int i = 0; i < stop - 1; i++) {
      min = i;
      tmpfactori = choice.get(i).getFactor();
      for (int j = i + 1; j < stop; j++) {
        tmpfactorj = choice.get(j).getFactor();
        if (tmpfactorj < tmpfactori) {
          tmpfactori = tmpfactorj;
          min = j;
        }
      }
      choice.add(i, choice.remove(min));
    }
    UnitFactory.UNITSYSTEMS.put(units.getClass().getName(), choice);
    return choice;
  }
}
