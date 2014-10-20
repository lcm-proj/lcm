/*
 * ErrorBarPolicyMultiAction.java of project jchart2d, 
 * highly proprietary error bar action that changes the behaviour 
 * depending on the <code>{@link JMenu}</code> from which it is triggered.
 * Copyright (C) 2007 - 2011 Achim Westermann, created on 17.08.2007 21:02:52.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 * If you modify or optimize the code in a useful way please let me know.
 * Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart.events;

import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.controls.LayoutFactory.BasicPropertyAdaptSupport;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Highly proprietary error bar action that changes the behavior depending on
 * the <code>{@link JMenu}</code> from which it is triggered.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.8 $
 */
public final class ErrorBarPolicyMultiAction extends ATrace2DActionErrorBarPolicy {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -4976003066220869828L;

  /** The action to perform if the event is triggered by an add menu item. */
  private Action m_addAction;

  /**
   * The menu for add operation items, needed to remove the add item from when
   * an add operation was triggered and add an add item when a remove operation
   * was triggered.
   */
  private JMenu m_addMenu;

  /** The action to perform if the event is triggered by a remove menu item. */
  private Action m_editAction;

  /**
   * The menu for edit operation items, needed to remove the edit item from when
   * a remove operation was triggered and add an edit item when an add operation
   * was triggered.
   */
  private JMenu m_editMenu;

  /** The action to perform if the event is triggered by a remove menu item. */
  private Action m_removeAction;

  /**
   * The menu for remove operation items, needed to remove the remove item from
   * when a remove operation was triggered and add a remove item when an add
   * operation was triggered.
   */
  private JMenu m_removeMenu;

  /**
   * Create an <code>Action</code> that accesses the trace and identifies itself
   * with the given action String.
   * <p>
   * 
   * @param trace
   *          the target the action will work on.
   * 
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   * 
   * @param errorBarPolicy
   *          the error bar policy to use by this action.
   * 
   * @param addMenu
   *          The menu for add operation items, needed to remove the add item
   *          from when an add operation was triggered and add an add item when
   *          a remove operation was triggered.
   * 
   * @param removeMenu
   *          The menu for remove operation items, needed to remove the remove
   *          item from when a remove operation was triggered and add a remove
   *          item when an add operation was triggered.
   * 
   * @param editMenu
   *          The menu for edit operation items, needed to remove the edit item
   *          from when a remove operation was triggered and add an edit item
   *          when an add operation was triggered.
   * 
   */
  public ErrorBarPolicyMultiAction(final ITrace2D trace, final String description,
      final IErrorBarPolicy< ? > errorBarPolicy, final JMenu addMenu, final JMenu removeMenu,
      final JMenu editMenu) {
    super(trace, description, errorBarPolicy);
    this.m_addAction = new Trace2DActionAddErrorBarPolicy(trace, description, errorBarPolicy);
    this.m_removeAction = new Trace2DActionRemoveErrorBarPolicy(trace, description, errorBarPolicy);
    this.m_editAction = new ErrorBarPolicyActionShowWizard(errorBarPolicy, description);
    this.m_addMenu = addMenu;
    this.m_removeMenu = removeMenu;
    this.m_editMenu = editMenu;
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    JMenuItem item = (JMenuItem) e.getSource();
    /*
     * This is horrible since early java versions! item.getParent() returns the
     * "magic" PopupMenu item.getAccessibleContext().getAccessibleParent()
     * returns the parent JMenu but not any more in java 1.6.
     */
    JPopupMenu popup = (JPopupMenu) item.getParent();
    JMenu menu = (JMenu) popup.getInvoker();
    String text = menu.getText();
    if (text.equals("+")) {
      // add action:

      JMenuItem removeItem;
      JMenuItem editItem;
      // add a new JMenuItem to the remove menu and to the edit menu:
      if (item instanceof LayoutFactory.PropertyChangeMenuItem) {
        removeItem = new LayoutFactory.PropertyChangeMenuItem(
            ((LayoutFactory.PropertyChangeMenuItem) item).getUIAdaptee(), this, new BasicPropertyAdaptSupport.RemoveAsListenerFromComponentIfTraceIsDropped(this.m_trace));
        editItem = new LayoutFactory.PropertyChangeMenuItem(
            ((LayoutFactory.PropertyChangeMenuItem) item).getUIAdaptee(), this, new BasicPropertyAdaptSupport.RemoveAsListenerFromComponentIfTraceIsDropped(this.m_trace));
      } else {
        removeItem = new JMenuItem(this);
        editItem = new JMenuItem(this);
      }

      // do the adding to the model:
      this.m_addAction.actionPerformed(e);
      // also open the edit screen for the new error bar policy:
      this.m_editAction.actionPerformed(e);
      // this has to be done after actionPerformed because parent frame search
      // will hit
      // null else as action is triggered by add menu:
      menu.remove(item);
      this.m_removeMenu.add(removeItem);
      this.m_editMenu.add(editItem);
    } else if (text.equals("-")) {
      // remove action:

      // add an add menu item:
      JMenuItem addItem;
      if (item instanceof LayoutFactory.PropertyChangeMenuItem) {
        addItem = new LayoutFactory.PropertyChangeMenuItem(
            ((LayoutFactory.PropertyChangeMenuItem) item).getUIAdaptee(), this, new BasicPropertyAdaptSupport.RemoveAsListenerFromComponentIfTraceIsDropped(this.m_trace));
      } else {
        addItem = new JMenuItem(this);
      }
      this.m_addMenu.add(addItem);

      this.m_removeAction.actionPerformed(e);
      // this has to be done after actionPerformed because parent frame search
      // will hit
      // null else as action is triggered by add menu:
      menu.remove(item);
      // remove also the edit menu, this is a bit trickier:
      String menuItemText = item.getText();
      Component[] components = this.m_editMenu.getMenuComponents();
      JMenuItem editMenuItem;
      for (int i = 0; i < components.length; i++) {
        if (components[i] instanceof JMenuItem) {
          if (menuItemText.equals(((JMenuItem) components[i]).getText())) {
            editMenuItem = (JMenuItem) components[i];
            this.m_editMenu.remove(editMenuItem);
            break;
          }
        }
      }

    } else {
      this.m_editAction.actionPerformed(e);
    }
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop
  }

}
