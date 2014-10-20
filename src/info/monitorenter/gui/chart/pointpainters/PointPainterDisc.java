/*
 *  PointPainterDisc.java of project jchart2d, paints round points. 
 *  Copyright (c) 2006 - 2011 Achim Westermann, created on 03.09.2006 20:27:06.
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
package info.monitorenter.gui.chart.pointpainters;

import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Renders points in form of a disc with configurable diameter.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.22 $
 */
public class PointPainterDisc extends APointPainter<PointPainterDisc> {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -6317473632026920774L;

  /** The diameter of the discs to paint. */
  private int m_discSize;

  /**
   * Cached m_discSize divided by two to save division for each point to render.
   */
  private int m_halfDiscSize;

  /**
   * Creates an instance with a default disc size of 4.
   * <p>
   */
  public PointPainterDisc() {
    this.setDiscSize(4);
  }

  /**
   * Creates an instance with the given disc diameter.
   * 
   * @param diameter
   *          the disc size in pixel to use.
   */
  public PointPainterDisc(final int diameter) {
    this.setDiscSize(diameter);
  }

  /**
   * @see info.monitorenter.gui.chart.pointpainters.APointPainter#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final PointPainterDisc other = (PointPainterDisc) obj;
    if (this.m_discSize != other.m_discSize) {
      return false;
    }
    if (this.m_halfDiscSize != other.m_halfDiscSize) {
      return false;
    }
    return true;
  }

  /**
   * Returns the diameter of the discs to paint in pixel.
   * <p>
   * 
   * @return the diameter of the discs to paint in pixel.
   */
  public int getDiscSize() {
    return this.m_discSize;
  }

  /**
   * @see info.monitorenter.gui.chart.pointpainters.APointPainter#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + this.m_discSize;
    result = prime * result + this.m_halfDiscSize;
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#paintPoint(int, int, int,
   *      int, java.awt.Graphics, info.monitorenter.gui.chart.ITracePoint2D)
   */
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics g, final ITracePoint2D original) {
    final Stroke backupStroke = this.installStroke(g);
    Color backupColor = null;
    Color test = this.installColorFill(g);
    // filling is desired as fill color has been set (if not null):
    if (test != null) {
      backupColor = test;
      // get the width of the stroke:
      // int strokeWidth = 1;
      // int halfstrokeWidth = 1;
      // Stroke stroke = this.getStroke();
      // if (stroke != null) {
      // if (stroke instanceof BasicStroke) {
      // BasicStroke basicStroke = (BasicStroke) stroke;
      // strokeWidth = (int) Math.ceil(basicStroke.getLineWidth());
      // halfstrokeWidth = (int) Math.ceil(basicStroke.getLineWidth() / 2);
      // // System.out.println("Strokewidth: " + strokeWidth);
      // }
      // }
      // g.fillOval(absoluteX - this.m_halfDiscSize + halfstrokeWidth, absoluteY
      // - this.m_halfDiscSize
      // + halfstrokeWidth, this.m_discSize - strokeWidth, this.m_discSize -
      // strokeWidth);
      g.fillOval(absoluteX - this.m_halfDiscSize, absoluteY - this.m_halfDiscSize, this.m_discSize,
          this.m_discSize);
    }
    test = this.installColor(g);
    if (backupColor == null) {
      // Only take backup color if it was not already returned from installing
      // fill color:
      backupColor = test;
    } else {
      // if fill color was installed but no color was specified we have to
      // revert to backup color here:
      if (test == null) {
        g.setColor(backupColor);
        backupColor = null;
      }
    }
    g.drawOval(absoluteX - this.m_halfDiscSize, absoluteY - this.m_halfDiscSize, this.m_discSize,
        this.m_discSize);

    if (backupStroke != null) {
      // cast is legal as installation would have failed and have returned null
      // if not possible:
      ((Graphics2D) g).setStroke(backupStroke);
    }
    if (backupColor != null) {
      g.setColor(backupColor);
    }
  }

  /**
   * Sets the diameter of the discs to paint in pixel.
   * <p>
   * 
   * @param discSize
   *          the diameter of the discs to paint in pixel.
   */
  public void setDiscSize(final int discSize) {
    this.m_discSize = discSize;
    this.m_halfDiscSize = this.m_discSize / 2;
  }

}
