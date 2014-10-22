/*
 * UIUtil.java of project jchart2d, utility class for UI / Layout operations. 
 * Copyright (C) 2004 - 2011 Achim Westermann.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA* If you modify or optimize the code in a useful way please let
 * me know. Achim.Westermann@gmx.de
 */
package info.monitorenter.util;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 * Utility class for UI / layout operations.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.11 $
 */
public final class UIUtil {

  /**
   * Finds the window of the given component.
   * <p>
   * 
   * This will be the top-level frame for components that are contained directly
   * in that window. For components that are contained in
   * <code>{@link java.awt.Dialog}</code> windows the dialog window will be
   * returned. For components that are contained in
   * <code>{@link javax.swing.JMenu} </code> trees the window that triggers that
   * menu will be returned.
   * <p>
   * 
   * @param component
   *          the component to find the master the JFrame of.
   * 
   * @return the frame of the given component.
   */
  public static Window findDialogWindow(final Component component) {
    Window result = null;
    Component comp = component;
    if (comp instanceof Window) {
      result = (Window) comp;
    } else {
      if (component instanceof JPopupMenu) {
        comp = ((JPopupMenu) component).getInvoker();
      } else {
        comp = component.getParent();
      }

      result = UIUtil.findDialogWindow(comp);
    }
    return result;
  }

  /**
   * Finds the frame of the given component.
   * <p>
   * 
   * The component may be contained in a <code>
   * {@link javax.swing.JDialog}</code>
   * (with support for modal dialogs) and still it's frame (the top level window
   * of the application) will be found. Support for components launched from
   * <code>{@link JPopupMenu}</code> instances is included.
   * <p>
   * This also works for nested <code>{@link javax.swing.JMenu}</code> /
   * <code>{@link javax.swing.JMenuItem}</code> trees.
   * <p>
   * 
   * @param component
   *          the component to find the master the JFrame of.
   * 
   * @return the frame of the given component.
   */
  public static Frame findFrame(final Component component) {

    Frame result = null;
    Component comp = component;
    if (comp instanceof JFrame) {
      result = (JFrame) comp;
    } else {
      if (component instanceof JPopupMenu) {
        comp = ((JPopupMenu) component).getInvoker();
      } else {
        comp = component.getParent();
      }

      result = UIUtil.findFrame(comp);
    }
    return result;
  }

  /**
   * Finds the parent <code>JPopupMenu</code> of the given component, it it is
   * contained in the component tree of one.
   * <p>
   * 
   * @param component
   *          a potential sub component of a popup menu.
   * 
   * @return the popup menu of the given component or null.
   */
  public static JPopupMenu findPopupMenu(final Component component) {

    JPopupMenu result = null;
    Component comp = component;
    if (component instanceof JPopupMenu) {
      result = ((JPopupMenu) component);
    } else {
      comp = component.getParent();
      if (comp != null) {
        result = UIUtil.findPopupMenu(comp);
      }
    }
    return result;
  }

  /**
   * Finds the top level parent <code>JPopupMenu</code> of the given component,
   * it it is contained in the component tree of one.
   * <p>
   * 
   * <code>JPopupMenu</code> trees may contain many sub menu instances.
   * <p>
   * 
   * @param component
   *          a potential sub component of a popup menu.
   * 
   * @return the popup menu of the given component or null.
   */
  public static JPopupMenu findTopLevelPopupMenu(final Component component) {

    JPopupMenu result = null;
    Component comp = component;
    if (component instanceof JPopupMenu) {
      result = ((JPopupMenu) component);
      comp = result.getInvoker();
    } else {
      comp = component.getParent();

    }
    // try to search for a higher popup:
    if (comp != null) {
      JPopupMenu higher = UIUtil.findTopLevelPopupMenu(comp);
      if (higher != null) {
        result = higher;
      }
    }

    return result;
  }

  /**
   * This is a workaround for the missing call
   * <code>{@link Point#getLocationOnScreen(MouseEvent e)}</code> in pre jdk
   * 1dot6.
   * <p>
   * 
   * @param e
   *          needed to get the location on screen of.
   * 
   * @return the absolute location of the mouse event in the window (vs.
   *         component).
   * 
   * @deprecated Replace with Point.getLocationOnScreen(MouseEvent) as soon as
   *             jdk 1.6 is used.
   * 
   */
  @Deprecated
  public static Point getLocationOnScreen(MouseEvent e) {
    Component comp = e.getComponent();
    Point screenCompLoc = comp.getLocationOnScreen();
    Point result = new Point(screenCompLoc.x + e.getX(), screenCompLoc.y + e.getY());
    return result;
  }

  /**
   * Utility class constructor.
   * <p>
   * 
   */
  private UIUtil() {
    // nop
  }
}
