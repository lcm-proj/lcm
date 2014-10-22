/*
 *  AAnnotationContentComponent.java of project jchart2d, a special 
 *  JComponent for rendering annotation content. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on 02.02.2009
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/annotations/AAnnotationContentComponent.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.6 $
 */

package info.monitorenter.gui.chart.annotations;

import java.awt.Graphics;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;

import javax.swing.JPanel;

/**
 * 
 * A special <code>{@link javax.swing.JComponent}</code> for rendering
 * annotation content.
 * <p>
 * 
 * The methods <code>{@link #paint(java.awt.Graphics)}</code> and
 * <code>{@link #paintComponents(java.awt.Graphics)}</code> are finalized to
 * enforce implementations to use
 * <code>{@link #paintAnnotation(Graphics, Chart2D, ITrace2D, ITracePoint2D)}</code> for
 * custom rendering. The finalized methods will invoke the latter method with
 * the appropriate parameters.
 * <p>
 * 
 * The visible annotation that will contain this "content panel" will respect
 * the method <code>{@link #getPreferredSize()}</code> of implementations and
 * enlarge it's bounds to guarantee that all content is shown.
 * <p>
 * 
 * @author <a href="achim.westermann@gmx.de">Achim Westermann</a>
 * 
 */
public abstract class AAnnotationContentComponent extends JPanel {

  /**
   * The trace point this annotation is related to.
   * <p>
   */
  private ITracePoint2D m_annotatedPoint;

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 3147218476248666442L;

  /**
   * Creates an instance that is related to the given point to annotate.
   * <p>
   * 
   * @param point
   *          the point to annotate.
   */
  public AAnnotationContentComponent(final ITracePoint2D point) {
    this.m_annotatedPoint = point;
  }

  /**
   * Returns the annotatedPoint.
   * <p>
   * 
   * @return the annotatedPoint
   */
  public final synchronized ITracePoint2D getAnnotatedPoint() {
    return this.m_annotatedPoint;
  }

  /**
   * Finalized to enforce using
   * <code>{@link #paintAnnotation(Graphics, Chart2D, ITrace2D, ITracePoint2D)}</code>.
   * 
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public final synchronized void paint(final Graphics g) {
    super.paint(g);
  }

  /**
   * Paint this annotation for the given chart, trace and point.
   * <p>
   * The arguments may help to render information about the point that is
   * annotated.
   * <p>
   * 
   * @param g
   *          the graphics context.
   * 
   * @param chart
   *          the chart to annotate.
   * 
   * @param trace
   *          the trace to annotate.
   * 
   * @param point
   *          the point to annotate.
   */
  public abstract void paintAnnotation(Graphics g, Chart2D chart, ITrace2D trace,
      ITracePoint2D point);

  /**
   * Finalized to enforce using
   * <code>{@link #paintAnnotation(Graphics, Chart2D, ITrace2D, ITracePoint2D)}</code>.
   * 
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected final synchronized void paintComponent(final Graphics g) {
    super.paintComponent(g);
    // NPE candidate, but must never be null by contract. So exceptions are
    // welcome to fix programming errors.
    ITrace2D trace = this.m_annotatedPoint.getListener();
    Chart2D chart = trace.getRenderer();
    this.paintAnnotation(g, chart, trace, this.m_annotatedPoint);
  }

  /**
   * Sets the annotatedPoint.
   * <p>
   * 
   * @param annotatedPoint
   *          the annotatedPoint to set
   */
  public final synchronized void setAnnotatedPoint(final ITracePoint2D annotatedPoint) {
    this.m_annotatedPoint = annotatedPoint;
    // Potential NPE, but this is considered a programming error so exceptions
    // are welcome:
    Chart2D chart = this.m_annotatedPoint.getListener().getRenderer();
    chart.setRequestedRepaint(true);
  }

}
