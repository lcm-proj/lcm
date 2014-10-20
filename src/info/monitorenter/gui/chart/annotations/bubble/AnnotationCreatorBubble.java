/*
 *  AnnotationCreatorBubble.java of project jchart2d, factory implementation 
 *  for annotation view creation in tool tip bubble style 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on 13.02.2009
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/annotations/bubble/AnnotationCreatorBubble.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.4 $
 */

package info.monitorenter.gui.chart.annotations.bubble;

import javax.swing.JComponent;

import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.annotations.AAnnotationContentComponent;
import info.monitorenter.gui.chart.annotations.IAnnotationCreator;
import info.monitorenter.gui.chart.views.ChartPanel;

/**
 * Factory implementation for annotation view creation in tool tip bubble style.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 **/
public final class AnnotationCreatorBubble implements IAnnotationCreator {

  /** Singleton instance. */
  private static IAnnotationCreator instance;

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = -339733222044962043L;

  /**
   * Singleton retrieval method.
   * <p>
   * 
   * @return the sole instance in this VM.
   */
  public static IAnnotationCreator getInstance() {
    if (AnnotationCreatorBubble.instance == null) {
      AnnotationCreatorBubble.instance = new AnnotationCreatorBubble();
    }
    return AnnotationCreatorBubble.instance;
  }

  /**
   * Singleton constructor.
   * <p>
   */
  private AnnotationCreatorBubble() {
    // nop
  }

  /**
   * @see info.monitorenter.gui.chart.annotations.IAnnotationCreator#createAnnotationView(info.monitorenter.gui.chart.views.ChartPanel,
   *      info.monitorenter.gui.chart.ITracePoint2D,
   *      info.monitorenter.gui.chart.annotations.AAnnotationContentComponent,
   *      boolean, boolean)
   */
  public JComponent createAnnotationView(final ChartPanel chart, final ITracePoint2D point,
      final AAnnotationContentComponent annotationPainter,
      final boolean useDragListenerOnAnnotationContent, final boolean useTitleBar) {
    AnnotationBubble annotationPanel = new AnnotationBubble(chart, annotationPainter,
        useDragListenerOnAnnotationContent, useTitleBar);
    return annotationPanel;

  }
}
