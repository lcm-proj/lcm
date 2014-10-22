/*
 *  SimpleDateFormatAnalyzer.java  jchart2d
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
package info.monitorenter.util;

import java.text.SimpleDateFormat;

/**
 * Hack for a <code>{@link java.text.SimpleDateFormat}</code> to get further
 * information about the fields that will be displayed.
 * <p>
 *
 * @version $Revision: 1.3 $
 *
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 *
 */
public final class SimpleDateFormatAnalyzer {

  /**
   * Returns true if the given date format displays days.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays days.
   */
  public static boolean displaysDay(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    // day in month, day in year, day of week in month, day of week.
    ret = fmt.indexOf('d') != -1 || fmt.indexOf('D') != -1 || fmt.indexOf('F') != -1
        || fmt.indexOf('E') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays hours.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays hours.
   */
  public static boolean displaysHour(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('H') != -1 || fmt.indexOf('h') != -1 || fmt.indexOf('K') != -1
        || fmt.indexOf('k') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays milliseconds.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays milliseconds.
   */
  public static boolean displaysMillisecond(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('S') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays minutes.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays minutes.
   */
  public static boolean displaysMinute(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('m') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays months.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays months.
   */
  public static boolean displaysMonth(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('M') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays seconds.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays seconds.
   */
  public static boolean displaysSecond(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('s') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays weeks.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays weeks.
   */
  public static boolean displaysWeek(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('W') != -1 || fmt.indexOf('w') != -1;
    return ret;
  }

  /**
   * Returns true if the given date format displays years.
   * <p>
   *
   * @param format
   *          the date format to check.
   *
   * @return true if the given date format displays years.
   */
  public static boolean displaysYear(final SimpleDateFormat format) {
    boolean ret = false;
    String fmt = format.toPattern();
    ret = fmt.indexOf('y') != -1;
    return ret;
  }

  /**
   * Private utility class constructor: Everything is static.
   * <p>
   */
  private SimpleDateFormatAnalyzer() {
    super();
  }
}
