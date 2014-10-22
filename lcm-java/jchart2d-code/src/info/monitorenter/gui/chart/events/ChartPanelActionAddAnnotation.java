/*
 *  Action that adds an annotation to a ChartPanel.
 *  Copyright (C) 2007 - 2011 Achim Westermann
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

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.annotations.AAnnotationContentComponent;
import info.monitorenter.gui.chart.annotations.AnnotationContentComponentDataValues;
import info.monitorenter.gui.chart.annotations.IAnnotationCreator;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.util.UIUtil;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;

/**
 * Action that adds an annotation to a <code>{@link ChartPanel}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.9 $
 * 
 */
public class ChartPanelActionAddAnnotation extends AChartPanelAction {

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 8338223879880089342L;

  /**
   * Create an <code>Action</code> that accesses the chart and identifies itself
   * with the given action String.
   * 
   * @param chartpanel
   *          the target the action will work on
   * @param description
   *          the descriptive <code>String</code> that will be displayed by
   *          {@link javax.swing.AbstractButton} subclasses that get this
   *          <code>Action</code> assigned (
   *          {@link javax.swing.AbstractButton#setAction(javax.swing.Action)}).
   */
  public ChartPanelActionAddAnnotation(final ChartPanel chartpanel, final String description) {
    super(chartpanel, description);
  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    // find the point:
    Component source = (Component) e.getSource();
    JPopupMenu menu = UIUtil.findTopLevelPopupMenu(source);
    PopupListener plistener = PopupListener.lookup(menu);
    // TODO: use this as soon as jdk 1.6 is used:
    // Point location =
    // plistener.getLastPopupMouseEvent().getLocationOnScreen();
    Point location = plistener.getLastPopupMouseEvent().getPoint();
    Chart2D chart = this.m_chartpanel.getChart();
    ITracePoint2D point = chart.getPointFinder().getNearestPoint((int) location.getX(),
        (int) location.getY(), chart);

    IAnnotationCreator factory = this.m_chartpanel.getAnnotationCreator();

    AAnnotationContentComponent annotation = new AnnotationContentComponentDataValues(point);
    JComponent annotationPanel = factory.createAnnotationView(this.m_chartpanel, point, annotation,
        true, true);
    this.m_chartpanel.setLayer(annotationPanel, JLayeredPane.DRAG_LAYER.intValue());
    annotationPanel.setLocation(location);
    this.m_chartpanel.add(annotationPanel);
    /*
     * this.m_chartpanel.add(annotationPanel, JLayeredPane.DRAG_LAYER); does not
     * work depending on the layout manager (e.g. BorderLayout complains: cannot
     * add to layout: constraint must be a string (or null).
     */
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    // nop
  }

}
