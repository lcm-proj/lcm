/*
 *  Messages.java of project jchart2d, Localization bundle. 
 *  Copyright (C) 2004 - 2011, Achim Westermann
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/util/Messages.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.6 $
 */

package info.monitorenter.gui.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message bundle for externalization.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public class Messages {
  
  /** Constant for the bundle name. */
  private static final String BUNDLE_NAME = "info.monitorenter.gui.util.messages"; 

  /** Bundle constant. */
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

  /**
   * Returns the external message for the given key. <p>
   * 
   * @param key key of the message. 
   * 
   * @return the external message for the given key.
   */
  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }

  /**
   * Utility class constructor.
   */
  private Messages() {
    // nop
  }
}
