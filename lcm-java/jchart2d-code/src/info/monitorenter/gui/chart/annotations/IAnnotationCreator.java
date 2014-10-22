/*
 *  IAnnotation.java of project jchart2d, interface for visible annotations.
 *  Copyright (C) Achim Westermann, created on 20.04.2005, 09:45:59
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
package info.monitorenter.gui.chart.annotations;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.io.Serializable;

import javax.swing.JComponent;

/**
 * An interface for creation of visible annotations.
 * <p>
 * 
 * This is a creational interface that allows implementors to return a
 * <code>{@link JComponent}</code> that will be used as "content area" of the
 * visible annotation.
 * <p>
 * 
 * This factory is configured to every <code>{@link Chart2D}</code> instance by
 * calling
 * <code>{@link ChartPanel#setAnnotationCreator(IAnnotationCreator)}</code>.
 * <p>
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.1 $
 */
public interface IAnnotationCreator extends Serializable {

  /**
   * Factory method to create a <code>JComponent</code> that annotates the given
   * <code>ITracePoint</code>.
   * <p>
   * 
   * The returned component will be the "content area" of the visible
   * annotation. It's method
   * <code>{@link java.awt.Component#getPreferredSize()}</code> will be used to
   * define the overall size of the visible annotation.
   * <p>
   * 
   * The returned component may have it's own mouse listeners because dragging
   * of the annotation will be supported by a separate title bar part. However
   * it may also use the given <code>dragListener</code> via
   * <code>{@link  java.awt.Component#addMouseListener(java.awt.event.MouseListener)}</code>
   * <b>and</b>
   * <code>{@link java.awt.Component#addMouseMotionListener(java.awt.event.MouseMotionListener)}</code>
   * if the "content area" of the visible annotation should only support
   * dragging the annotation and a right - click menu for basic operations.
   * <p>
   * 
   * The implementation should build a complete annotation view with all
   * required event listeners that contains at least the annotation content
   * component that given here.
   * <p>
   * 
   * @param chart
   *          the chart panel to add the annotation to, potentially needed for
   *          the removal of the annotation view when it's optional title bar
   *          close icon is pressed.
   * 
   * @param point
   *          the point that is annotated.
   * 
   * @param useDragListenerOnAnnotationContent
   *          if true the content area that contains the information of the
   *          annotation will support drag and drop as well as a basic right
   *          click popup menu.
   * 
   * @param annotationPainter
   *          the content component of the annotation that will display the
   *          information of the annotation.
   * 
   * @param useTitleBar
   *          if true, a title bar with close button for the annotation will be
   *          shown.
   * 
   * @return the component that will be displayed as an annotation of the given
   *         point.
   */
  public JComponent createAnnotationView(final ChartPanel chart, final ITracePoint2D point,
      final AAnnotationContentComponent annotationPainter,
      final boolean useDragListenerOnAnnotationContent, final boolean useTitleBar);

}
