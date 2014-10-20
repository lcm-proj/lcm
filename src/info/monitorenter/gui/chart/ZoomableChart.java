/*
 *  ZoomableChart.java of project jchart2d, a chart enriched 
 *  by zoom functionality in x dimension. 
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart;

import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyUnbounded;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

/**
 * <code>{@link info.monitorenter.gui.chart.Chart2D}</code> enriched by a
 * zoom-functionality in the x and y dimension.
 * <p>
 * 
 * @author Alessio Sambarino (Contributor)
 * @author Klaus Pesendorfer (Bugfix contributor) 
 * @author Anadi Mishra (Bugfix contributor) 
 * 
 * @version $Revision: 1.19 $
 * 
 */
public class ZoomableChart extends Chart2D implements MouseListener, MouseMotionListener {

  /**
   * Generated <code>serial version UID</code>.
   * <p>
   */
  private static final long serialVersionUID = 8799808716942023907L;

  /**
   * Store the last mouse click and test in the mouseDragged-method which
   * mouse-button was clicked.
   */
  private int m_lastPressedButton;

  /** The starting point of the mouse drag operation (click, then move). */
  private Point2D m_startPoint;

  /**
   * Range policy used to zoom out to the minimum bounds that show every data
   * point.
   */
  private IRangePolicy m_zoomAllRangePolicy = new RangePolicyUnbounded();

  /** The area to zoom. */
  private Rectangle2D m_zoomArea;

  /**
   * Defcon.
   * <p>
   */
  public ZoomableChart() {

    super();

    this.addMouseListener(this);
    this.addMouseMotionListener(this);
  }

  /**
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(final MouseEvent e) {
    // nop.

  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  public void mouseDragged(final MouseEvent e) {
    if (this.m_lastPressedButton != MouseEvent.BUTTON1) {
      return;
    }

    if ((e.getY() < 20) || (e.getY() > this.getYChartStart()) || (e.getX() < 20)
        || (e.getX() > this.getXChartEnd())) {
      // nop

    } else {
      double startX;
      double endX;
      double dimX;
      double dimY;
      double startY;
      double endY;

      // x-coordinate
      if (e.getX() > this.m_startPoint.getX()) {
        startX = this.m_startPoint.getX();
        endX = e.getX();
      } else {
        startX = e.getX();
        endX = this.m_startPoint.getX();
      }

      if (startX < this.getXChartStart()) {
        startX = this.getXChartStart();
      }

      if (endX > (this.getWidth() - 20)) {
        endX = this.getWidth() - 20;
      }

      // y-coordinate
      if (e.getY() > this.m_startPoint.getY()) {
        startY = this.m_startPoint.getY();
        endY = e.getY();
      } else {
        startY = e.getY();
        endY = this.m_startPoint.getY();
      }

      if (startY > this.getYChartStart()) {
        startY = this.getYChartStart();
      }

      if (endY > (this.getHeight() - 20)) {
        endY = this.getHeight() - 20;
      }

      dimX = endX - startX;
      dimY = endY - startY;

      this.m_zoomArea = new Rectangle2D.Double(startX, startY, dimX, dimY);

      this.setRequestedRepaint(true);
    }
  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(final MouseEvent e) {
    // nop.
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(final MouseEvent e) {
    // nop.
  }

  /**
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  public void mouseMoved(final MouseEvent e) {
    // nop.
  }

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(final MouseEvent e) {
    this.m_startPoint = new Point2D.Double(e.getX(), e.getY());
    this.m_lastPressedButton = e.getButton();
  }

  /**
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  public void mouseReleased(final MouseEvent e) {

    if (this.m_zoomArea != null) {
      // Modified by Anadi Mishra on 20110911
      // IAxis axisX = this.getAxisX();
      // IAxis axisY = this.getAxisY();

      // x-coordinate
      double startPx = this.m_zoomArea.getX();
      double endPx = this.m_zoomArea.getX() + this.m_zoomArea.getWidth();

      // double xAxisMin = axisX.translatePxToValue((int) startPx);
      // double xAxisMax = axisX.translatePxToValue((int) endPx);

      // modified by Fabalabs (KP) 20060914: zoom also in y-direction
      // y-coordinate
      double startPy = this.m_zoomArea.getY();
      double endPy = this.m_zoomArea.getY() + this.m_zoomArea.getHeight();

      // double yAxisMin = axisY.translatePxToValue((int) startPy);
      // double yAxisMax = axisY.translatePxToValue((int) endPy);

      // do not zoom extremly small areas (does not work properly because of
      // calculation)
      if ((endPx - startPx) < 20 || (endPy - startPy) < 20) {
        this.m_zoomArea = null;
        this.setRequestedRepaint(true);
        return;
      }

      List<IAxis<?>> axisList = this.getAxes();
      for (Iterator<IAxis<?>> i = axisList.iterator(); i.hasNext();) {
        IAxis<?> iAxis = i.next();
        if ((Chart2D.CHART_POSITION_BOTTOM == iAxis.getAxisPosition())
            || (Chart2D.CHART_POSITION_TOP == iAxis.getAxisPosition())) {// its
          // x
          // axis
          this.zoom(iAxis, startPx, endPx);
        }
        if ((Chart2D.CHART_POSITION_LEFT == iAxis.getAxisPosition())
            || (Chart2D.CHART_POSITION_RIGHT == iAxis.getAxisPosition())) {// its
          // x
          // axis
          this.zoom(iAxis, startPy, endPy);
        }
      }

      // this.zoom(xAxisMin, xAxisMax, yAxisMin, yAxisMax);
    }
  }

  /**
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected synchronized void paintComponent(final Graphics g) {

    super.paintComponent(g);

    Graphics2D g2 = (Graphics2D) g;

    if (this.m_zoomArea != null) {
      g2.draw(this.m_zoomArea);
      g2.setPaint(new Color(255, 255, 0, 100));
      g2.fill(this.m_zoomArea);
    }
  }

  /**
   * Zooms to the selected bounds in x-axis.
   * <p>
   * 
   * @param xmin
   *          the lower x bound.
   * 
   * @param xmax
   *          the upper x bound.
   */
  public void zoom(final double xmin, final double xmax) {

    this.m_zoomArea = null;

    IAxis<?> axis = this.getAxisX();
    IRangePolicy zoomPolicy = new RangePolicyFixedViewport(new Range(xmin, xmax));
    axis.setRangePolicy(zoomPolicy);
  }

  /**
   * Zooms the axis to the pixel value of start and end points.
   * <p>
   * Does not check for the position of axis so the caller must take care to
   * provide start x and end x for horizontal and start y and end y for vertical
   * axes.
   * 
   * @param axis
   *          the axis to zoom.
   * 
   * @param startP
   *          the start coordinate in the dimension of the given axis in pixel
   *          coords.
   * 
   * @param endP
   *          the end coordinate in the dimension of the given axis in pixel
   *          coords.
   */
  public void zoom(IAxis<?> axis, final double startP, final double endP) {

    this.m_zoomArea = null;

    double axisMin = axis.translatePxToValue((int) startP);
    double axisMax = axis.translatePxToValue((int) endP);

    IRangePolicy zoomPolicy = new RangePolicyFixedViewport(new Range(axisMin, axisMax));
    axis.setRangePolicy(zoomPolicy);
  }

  /**
   * Zooms to the selected bounds in both directions.
   * <p>
   * 
   * @param xmin
   *          the lower x bound (value of chart (vs. pixel of screen)).
   * 
   * @param xmax
   *          the upper x bound (value of chart (vs. pixel of screen)).
   * 
   * @param ymin
   *          the lower y bound (value of chart (vs. pixel of screen)).
   * 
   * @param ymax
   *          the upper y bound (value of chart (vs. pixel of screen)).
   */
  public void zoom(final double xmin, final double xmax, final double ymin, final double ymax) {

    this.m_zoomArea = null;

    IAxis<?> axisX = this.getAxisX();
    IRangePolicy zoomPolicyX = new RangePolicyFixedViewport(new Range(xmin, xmax));
    axisX.setRangePolicy(zoomPolicyX);

    IAxis<?> axisY = this.getAxisY();
    IRangePolicy zoomPolicyY = new RangePolicyFixedViewport(new Range(ymin, ymax));
    axisY.setRangePolicy(zoomPolicyY);
  }

  /**
   * Resets the zooming area to a range that displays all data.
   * <p>
   */
  public void zoomAll() {
    List<IAxis<?>> axisList = this.getAxes();
    for (Iterator<IAxis<?>> i = axisList.iterator(); i.hasNext();) {
      IAxis<?> iAxis =  i.next();
      iAxis.setRangePolicy(this.m_zoomAllRangePolicy);
    }
  }
}
