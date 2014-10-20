/*
 *  StringUtil, utility class for string operations.
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

import java.lang.reflect.Array;
import java.util.List;

/**
 * Nice static helpers for working with Strings.
 * <p>
 * Maybe not always the fastest solution to call in here, but working. Also
 * usable for seeing examples and cutting code for manual inlining.
 * <p>
 * 
 * @author Achim.Westermann@gmx.de
 * 
 * @version $Revision: 1.10 $
 */
public final class StringUtil {
  
  /** Singleton instance. */
  private static StringUtil instance = null;

  /**
   * Appends the given amount of spaces to the String.
   * <p>
   * 
   * Not intended for big append -operations because in a loop alway just one
   * space is added.
   * <p>
   * 
   * @param s
   *          the base String to append spaces to.
   * 
   * @param count
   *          the amount of spaces to append.
   * 
   * @return a String consisting of s and count trailing whitespaces.
   */
  public static final String appendSpaces(final String s, final int count) {
    StringBuffer tmp = new StringBuffer(s);
    for (int i = 0; i < count; i++) {
      tmp.append(" ");
    }
    return tmp.toString();
  }

  /**
   * Little String output - helper that modifies the given LinkedList by getting
   * it's Objects and replace them by their toString() - representation.
   * <p>
   * 
   * What is special? <br>
   * If an Object in the given List is an Array (of Objects or primitive data
   * types) reflection will be used to create a String - representation of them.
   * The changes are reflected in the Objects that are in the given List. So
   * keep a reference to it. If you are sure, that your List does not contain
   * Arrays do not use this method to avoid overhead.
   * <p>
   * 
   * Avoid structural modifications (remove) of the list while using this
   * method. This method or better: the given List is only thread - safe if the
   * list is synchronized.
   * <p>
   * 
   * A clever VM (hotspot) will be able to inline this function because of void
   * return.
   * <p>
   * 
   * @param objects
   *          the List of objects that will be changed to a list of the String
   *          representation of the Objects with respect to special array
   *          treatment.
   * 
   */
  public static final void listOfArraysToString(final List<Object> objects) {
    if (objects == null) {
      return;
    }
    int stop = objects.size();
    for (int i = 0; i < stop; i++) {
      objects.add(i, StringUtil.arrayToString(objects.remove(i)));
    }
  }

  /**
   * If the given Object is no Array, it's toString - method is invoked.
   * Primitive type - Arrays and Object - Arrays are introspected using
   * java.lang.reflect.Array. Convention for creation fo String -
   * representation: <p>
   * 
   * @see StringUtil#arrayToString(Object, String)
   * 
   * @param isArr
   *          The Array to represent as String.
   * 
   * @return a String-representation of the Object.
   * 
   * 
   */
  public static final String arrayToString(final Object isArr) {
    String result = StringUtil.arrayToString(isArr, ",");
    return result;
  }
  
  /**
   * If the given Object is no Array, it's toString - method is invoked.
   * Primitive type - Arrays and Object - Arrays are introspected using
   * java.lang.reflect.Array. Convention for creation for String -
   * representation: <br>
   * 
   * <code>
   * // Primitive arrays: 
   * &quot;[&quot;+isArr[0]+&quot;&lt;separator&gt;&quot;+isArr[1]+.. ..+isArr[isArr.length-1]+&quot;]&quot;
   *     
   *     
   * //Object arrays :  
   * &quot;[&quot;+isArr[0].toString()+&quot;&lt;separator&gt;&quot;+.. ..+isArr[isArr.length-1].toString+&quot;]&quot;
   * // Two or three - dimensional Arrays are not supported
   * //(should be reflected in a special output method, e.g.as a field)
   * 
   * // other Objects:    
   * toString()
   * </code>
   * 
   * @param separator put in-between each array element in the resulting string. 
   * 
   * @param isArr
   *          The Array to represent as String.
   * 
   * @return a String-representation of the Object.
   * 
   * 
   */
  public static final String arrayToString(final Object isArr, final String separator) {
    String result;
    if (isArr == null) {
      result = "null";
    } else {
      Object element;
      StringBuffer tmp = new StringBuffer();
      try {
        int length = Array.getLength(isArr);
        tmp.append("[");
        for (int i = 0; i < length; i++) {
          element = Array.get(isArr, i);
          if (element == null) {
            tmp.append("null");
          } else {
            tmp.append(element.toString());
          }
          if (i < length - 1) {
            tmp.append(separator);
          }
        }
        tmp.append("]");
        result = tmp.toString();
      } catch (ArrayIndexOutOfBoundsException bound) {
        // programming mistake or bad Array.getLength(obj).
        tmp.append("]");
        result = tmp.toString();

      } catch (IllegalArgumentException noarr) {
        result = isArr.toString();
      }
    }
    return result;
  }

  /**
   * Returns the system - dependent line separator.
   * <p>
   * 
   * Only call this method once (not in a loop) and keep the result.
   * <p>
   * 
   * @return the system - dependent line separator.
   */
  public static String getNewLine() {
    return  System.getProperty("line.separator");
  }

  /**
   * Returns the singleton instance of this class.
   * <p>
   * 
   * This method is useless for now as all methods are static. It may be used in
   * future if VM-global configuration will be put to the state of the instance.
   * <p>
   * #
   * 
   * @return the singleton instance of this class.
   */
  public static StringUtil instance() {
    if (StringUtil.instance == null) {
      StringUtil.instance = new StringUtil();
    }
    return StringUtil.instance;
  }

  /**
   * Returns true if the argument is null or consists of whitespaces only.
   * <p>
   * 
   * @param test
   *          the <code>String</code> to test.
   * 
   * @return true if the argument is null or consists of whitespaces only.
   */
  public static boolean isEmpty(final String test) {
    boolean result;
    if (test == null) {
      result = true;
    } else {
      result = test.trim().length() == 0;
    }
    return result;
  }

  /**
   * Returns the maximum length of a {@link Object#toString()} result in
   * characters within the given List.
   * <p>
   * 
   * No data is changed in the given List at all. But the String -
   * representation of all Objects, even Arrays is build to inspect. <br>
   * Convention for creation fo String - representation: <br>
   * 
   * <pre>
   *        Primitive Arrays : as performed by this classes @see #ArrayToString.
   *        Object Arrays    : as performed by this classes @see #ArrayToString
   *        other Objects    : toString()  (as performed by this classes @see #ArrayToString).
   * </pre>
   * 
   * @param objects
   *          the <code>List&lt;Object&gt;</code> to inspect for the maximum
   *          length of a {@link Object#toString()} result.
   * 
   * @return The length of the longest String - representation of an Object in
   *         the List <b>or 0 if objects was null or of size 0. </b>
   */
  public static final int longestStringRepresentation(final List<Object> objects) {
    int result;
    if (objects == null) {
      result = 0;
    } else {
      int maxsize = 0;
      int tint = 0;
      String tmp;
      int stop = objects.size();
      for (int i = 0; i < stop; i++) {
        tmp = StringUtil.arrayToString(objects.get(i));
        tint = tmp.length();
        if (tint > maxsize) {
          maxsize = tint;
        }
      }
      // maximum size known.
      result = maxsize;
    }
    return result;
  }

  /**
   * Appends the necessary amount of spaces to the string until it has the givn
   * length. No Exception is thrown, if the length of the String is shorter than
   * the given length, but nothing will happen and a message will be printed to
   * the System.out.
   * 
   * 
   * @param s
   *          the String to expand.
   * @param length
   *          the desired length of the String to be returned.
   * @return A String that represents the content of the old String including
   *         extra whitespaces.
   */
  public static final String setSize(final String s, final int length) {
    String result = s;
    int oldlen = s.length();
    if (oldlen > length) {
      System.err.println("greenpeace.util.setSize(String s,int length): length (" + length
          + ") is smaller than s.length(" + oldlen + ") : " + s);
    } else {
      int tofill = length - oldlen;
      result = StringUtil.appendSpaces(s, tofill);
    }
    return result;
  }

  /**
   * Modifies the given LinkedList by getting it's Objects and replace them by
   * their toString() - representation concatenated with the necessary amount of
   * white spaces that every String in the List will have the same amount of
   * characters.
   * <p>
   * 
   * Only use this method in following case: <br>
   * <ul>
   * <li>You have got an AbstractList or subclass containing Objects you do not
   * know.</li>
   * <li>You want to transform all Elements in the List to Strings.</li>
   * <li>There might be Array's in the Object (whose toString method will return
   * only their hashcode).</li>
   * </ul>
   * <p>
   * 
   * What happens?
   * <ul>
   * <li>All Objects, even primitive Arrays in your List will be turned to
   * String- representation.</li>
   * <li>The result will replace the old position of the Object in the given
   * List. The old Objects will get lost!</li>
   * <li>All Strings will be filled with whitespaces until each String has the
   * same length.</li>
   * </ul>
   * At least this method will be speeded up by a hotspot VM by inlining this
   * method. <br>
   * <i>An example: <br>
   * You just retrieved data from a db using jdbc and a generic class
   * (resultset.getObject()). You want to print the data to System.out or to
   * save it to a file, but do not know, if Arrays are contained. You want the
   * output to be formatted (each line). </i>
   * <p>
   * 
   * @param objects
   *          contains the Objects to replace by their toString()
   *          representation.
   */
  public static final void toLongestString(final List<Object> objects) {
    if (objects == null) {
      return;
    }
    int maxsize = 0;
    int tint = 0;
    String tmp;
    int stop = objects.size();
    for (int i = 0; i < stop; i++) {
      StringUtil.arrayToString(objects.get(i));
      tmp = (String) objects.get(i);
      tint = tmp.length();
      if (tint > maxsize) {
        maxsize = tint;
      }
      objects.add(i, tmp);
    }
    // maximum size known.
    for (int i = 0; i < stop; i++) {
      objects.add(i, StringUtil.setSize((String) objects.remove(i), maxsize));
    }
  }

  /**
   * Avoids creation from outside for singleton support.
   * <p>
   * 
   */
  private StringUtil() {
    // nop
  }
}
