/*
 *  PopupListener, general purpose popup trigger that connects JPopupMenus to mouse events.
 *  Copyright (C) 2004 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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
package info.monitorenter.gui.chart.events;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPopupMenu;

/**
 * A general purpose <code>PopupListener</code>.
 * <p>
 * It is used to connect <code>JPopupMenu</code> instances with the components
 * retrieved from factory methods (of factory
 * {@link info.monitorenter.gui.chart.controls.LayoutFactory}).
 * <p>
 * 
 * Note that instances have to be registered as a listener on components via
 * {@link java.awt.Component#addMouseListener(java.awt.event.MouseListener)} to
 * make it working.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.10 $
 */
public final class PopupListener extends MouseAdapter {
  /**
   * Needed for looking up the listener when the popup menu is found: Especially
   * this listener records the mouse events while popup - triggered actions
   * cannot access the location of the popup because it is invisible when the
   * action is triggered.
   */
  private static Map<JPopupMenu, PopupListener> listenerLookup = new HashMap<JPopupMenu, PopupListener>();

  /**
   * Returns the listener for the given popup or null, if there is no listener
   * for that popup.
   * <p>
   * 
   * @param popup
   *          the popup to search the listener for.
   * 
   * @return the listener for the given popup or null, if there is no listener
   *         for that popup.
   */
  public static PopupListener lookup(final JPopupMenu popup) {
    return PopupListener.listenerLookup.get(popup);
  }

  /** The popup to open. */
  private JPopupMenu m_popup;

  /** Reference to the last mouse event that triggered a popup. */
  private MouseEvent m_lastPopupMouseEvent;

  /**
   * Creates an instance that will show the given popup upon a right mouse click
   * on a {@link javax.swing.JComponent} this instance will be registered as
   * listener to.
   * <p>
   * 
   * @param popup
   *          the popup to show upon a right mouse click on a
   *          {@link javax.swing.JComponent} this instance will be registered as
   *          listener to.
   * 
   * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
   */
  public PopupListener(final JPopupMenu popup) {
    this.m_popup = popup;
    PopupListener.listenerLookup.put(popup, this);
  }

  /**
   * Returns the lastPopupMouseEvent.
   * <p>
   * 
   * @return the lastPopupMouseEvent
   */
  public final MouseEvent getLastPopupMouseEvent() {
    return this.m_lastPopupMouseEvent;
  }

  /**
   * @return the popup menu.
   */
  public final JPopupMenu getPopup() {
    return this.m_popup;
  }

  /**
   * Helper that triggers the popup display in a system - dependant manner.
   * <p>
   * 
   * On windows a right mouse click will trigger the popup display.
   * <p>
   * 
   * @param me
   *          the mouse event fired.
   */
  private void maybeShopwPopup(final MouseEvent me) {
    if (me.isPopupTrigger()) {
      this.m_lastPopupMouseEvent = me;
      this.m_popup.show(me.getComponent(), me.getX(), me.getY());

    }
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(final MouseEvent me) {
    this.maybeShopwPopup(me);
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(final MouseEvent me) {
    this.maybeShopwPopup(me);
  }
}
