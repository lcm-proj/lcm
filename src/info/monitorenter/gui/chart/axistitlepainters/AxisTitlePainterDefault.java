/*
 *  AxisTitlePainterDafault.java of project jchart2d, an IAxisTitlePainter 
 *  that will render titles in a default way while adapting to x or y 
 *  axis use. 
 *  Copyright 2007 - 2011 (C) Achim Westermann, created on 04.08.2007 19:40:52.
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
package info.monitorenter.gui.chart.axistitlepainters;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisTitlePainter;
import info.monitorenter.util.StringUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * An <code>{@link IAxisTitlePainter}</code> implementation that will render
 * titles in a default way while adapting to x or y axis use.
 * <p>
 * For x axis the title will be displayed centered below the axis. For y axis
 * the title will be displayed rotated by 90 degrees centered left of the axis.
 * <p>
 * 
 * The y axis rotation will only be performed when the given
 * <code>{@link Graphics}</code> Object is of type
 * <code>{@link Graphics2D}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.14 $
 */
public class AxisTitlePainterDefault implements IAxisTitlePainter {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -8076180259242501703L;

  /**
   * Defcon.
   * <p>
   */
  public AxisTitlePainterDefault() {
    super();
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTitlePainter#getHeight(info.monitorenter.gui.chart.IAxis,
   *      java.awt.Graphics)
   */
  public int getHeight(final IAxis<?> axis, final Graphics g2d) {
    int result = 0;
    IAxis.AxisTitle axisTitle = axis.getAxisTitle();
    String title = axisTitle.getTitle();
    if (!StringUtil.isEmpty(title)) {
      Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(title, g2d);
      int dimension = axis.getDimension();
      switch (dimension) {
        case Chart2D.X:
          result = (int) bounds.getHeight();
          break;
        case Chart2D.Y:
          boolean supportsRotation = g2d instanceof Graphics2D;
          if (supportsRotation) {
            // for y it's rotated by 90 degrees:
            result = (int) bounds.getWidth();
          } else {
            result = (int) bounds.getHeight();
          }
          break;
        default:
          throw new IllegalArgumentException(
              "Given axis.getDimension() is neither Chart2D.X nor Chart2D.Y!");
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTitlePainter#getWidth(info.monitorenter.gui.chart.IAxis,
   *      java.awt.Graphics)
   */
  public int getWidth(final IAxis<?> axis, final Graphics g2d) {
    int result = 0;
    IAxis.AxisTitle axisTitle = axis.getAxisTitle();
    String title = axisTitle.getTitle();
    if (!StringUtil.isEmpty(title)) {
      // incorporation of our font if there:
      Font backUpFont = g2d.getFont();
      Font titleFont = axisTitle.getTitleFont();
      if (titleFont != null) {
        g2d.setFont(titleFont);
      }
      Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(title, g2d);
      int dimension = axis.getDimension();
      switch (dimension) {
        case Chart2D.X:
          result = (int) bounds.getWidth();
          break;
        case Chart2D.Y:
          boolean supportsRotation = g2d instanceof Graphics2D;
          if (supportsRotation) {
            // for y it's rotated by 90 degrees:
            result = (int) bounds.getHeight();
          } else {
            result = (int) bounds.getWidth();
          }
          break;
        default:
          throw new IllegalArgumentException(
              "Given axis.getDimension() is neither Chart2D.X nor Chart2D.Y!");
      }
      // resetting original font if it was changed:
      if (titleFont != null) {
        g2d.setFont(backUpFont);
      }
    }

    return result;
  }

  /**
   * TODO: This will not work for multiple axis in the same dimension and
   * position (overwriting titles)!
   * 
   * @see info.monitorenter.gui.chart.IAxisTitlePainter#paintTitle(info.monitorenter.gui.chart.IAxis,
   *      java.awt.Graphics)
   */
  public void paintTitle(final IAxis<?> axis, final Graphics g) {

    IAxis.AxisTitle axisTitle = axis.getAxisTitle();
    String title = axisTitle.getTitle();
    Rectangle2D bounds;
    // manage the title font if there:
    Font titleFont = axisTitle.getTitleFont();
    Font backUpFont = g.getFont();
    Color titleColor = axisTitle.getTitleColor();
    Color backUpColor = g.getColor();

    if (titleFont != null) {
      g.setFont(titleFont);
    }
    if (titleColor != backUpColor && titleColor != null) {
      g.setColor(titleColor);
    }

    bounds = g.getFontMetrics().getStringBounds(title, g);

    Chart2D chart = axis.getAccessor().getChart();

    int dimension = axis.getDimension();
    int position = axis.getAxisPosition();
    switch (dimension) {
      case Chart2D.X:
        switch (position) {
          case Chart2D.CHART_POSITION_BOTTOM: {

            int startX = chart.getXChartStart();
            int endX = chart.getXChartEnd();
            double xspace = bounds.getWidth();
            int titleStartX = (int) ((endX - startX) / 2.0 - xspace / 2.0);
            g.drawString(title, titleStartX, axis.getPixelYBottom() - 4);
            break;
          }
          case Chart2D.CHART_POSITION_TOP: {
            int startX = chart.getXChartStart();
            int endX = chart.getXChartEnd();
            double xspace = bounds.getWidth();
            int titleStartX = (int) ((endX - startX) / 2.0 - xspace / 2.0);
            g.drawString(title, titleStartX, axis.getPixelYTop()
                + chart.getFontMetrics(chart.getFont()).getHeight());
            break;
          }
          default: {
            // nop
          }
        }
        break;
      case Chart2D.Y:
        switch (position) {
          case (Chart2D.CHART_POSITION_LEFT): {
            // check if rotation is available: this is the case for normal
            // screen painting, but for printing it is not available!
            boolean supportsRotation = g instanceof Graphics2D;
            if (supportsRotation) {
              Graphics2D g2d = (Graphics2D) g;
              int startY = chart.getYChartStart();
              int endY = chart.getYChartEnd();
              double yspace = bounds.getWidth();
              int titleStartY = (int) ((startY - endY) / 2.0 + yspace / 2.0);
              if(titleStartY <= 0){
                System.err.println("titleStartY below or equal to zero: "+titleStartY);
              } 
              int titleStartX = axis.getPixelXLeft() + chart.getFontMetrics(chart.getFont()).getHeight();
              if(titleStartX <= 0){
                System.err.println("titleStartX below or equal to zero: "+titleStartX);
              } 
              
              // store former transform for later restore:
              AffineTransform tr = g2d.getTransform();
              AffineTransform at = g2d.getDeviceConfiguration().getDefaultTransform();
              at.translate(
                  titleStartX,
                  titleStartY);
              at.rotate(-Math.PI / 2);
              g2d.setTransform(at);
              g2d.drawString(title, 0, 0);
              g2d.setTransform(tr);
            } else {
              // no rotation: display in vertical middle:
              int startY = chart.getYChartStart();
              int endY = chart.getYChartEnd();
              double yspace = bounds.getWidth();
              int titleStartY = (int) ((startY - endY) / 2.0 + yspace / 2.0);
              int titleStartX = axis.getPixelXLeft()
                  + chart.getFontMetrics(chart.getFont()).getHeight();
              g.drawString(title, titleStartX, titleStartY);

            }
            break;
          }
          case (Chart2D.CHART_POSITION_RIGHT): {
            boolean supportsRotation = g instanceof Graphics2D;
            if (supportsRotation) {
              Graphics2D g2d = (Graphics2D) g;
              int startY = chart.getYChartStart();
              int endY = chart.getYChartEnd();
              double yspace = bounds.getWidth();
              int titleStartY = (int) ((startY - endY) / 2.0 + yspace / 2.0);

              int chartLabelFontWidth = chart.getFontMetrics(chart.getFont()).charWidth('0');
              int xShiftPosition = chart.getAxisTickPainter().getMajorTickLength();
              xShiftPosition += axis.getFormatter().getMaxAmountChars() * chartLabelFontWidth;

              AffineTransform tr = g2d.getTransform();
              AffineTransform at = g2d.getDeviceConfiguration().getDefaultTransform();
              at.translate(chart.getXChartEnd() + xShiftPosition, titleStartY);
              at.rotate(-Math.PI / 2);
              g2d.setTransform(at);
              g2d.drawString(title, 0, 0);
              g2d.setTransform(tr);
            } else {
              int startY = chart.getYChartStart();
              int endY = chart.getYChartEnd();
              double yspace = bounds.getWidth();
              int titleStartY = (int) ((startY - endY) / 2.0 + yspace / 2.0);
              int chartLabelFontWidth = chart.getFontMetrics(chart.getFont()).charWidth('0');
              int xShiftPosition = chart.getAxisTickPainter().getMajorTickLength();
              xShiftPosition += axis.getFormatter().getMaxAmountChars() * chartLabelFontWidth;
              int titleStartX = xShiftPosition + chart.getXChartEnd();
              g.drawString(title, titleStartX, titleStartY);
            }
            break;
          }
          default: {
            // nop
          }
        }
        break;
      default:
        throw new IllegalArgumentException(
            "Given axis.getDimension() is neither Chart2D.X nor Chart2D.Y!");
    }
    // resetting original font if it was changed:
    if (titleFont != null) {
      g.setFont(backUpFont);
    }

    if (titleColor != backUpColor && titleColor != null) {
      g.setColor(backUpColor);
    }

  }
}
