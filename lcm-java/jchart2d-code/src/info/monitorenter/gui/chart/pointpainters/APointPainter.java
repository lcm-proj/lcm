/*
 *  APointPainter.java of project jchart2d, adapter class 
 *  that implements the optional methods of the interface 
 *  IPointPainter with "no operations". 
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

import info.monitorenter.gui.chart.IPointPainter;
import info.monitorenter.gui.chart.IPointPainterConfigurableUI;
import info.monitorenter.gui.util.ColorMutable;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * Adapter class that implements optional methods of
 * <code>{@link IPointPainter}</code> as "no operation".
 * <p>
 * 
 * @param <T>
 *          needed for generic comparable<T>.
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.13 $
 * 
 * @since 3.0.0
 */
public abstract class APointPainter<T extends IPointPainterConfigurableUI<T>> implements
    IPointPainterConfigurableUI<T> {
  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = -8279972259015294590L;

  /** Color used for paint operations. */
  private final ColorMutable m_color = new ColorMutable();

  /** Color used for fill operations. */
  private final ColorMutable m_colorFill = new ColorMutable();

  /** Stroke used for paint operations. */
  private Stroke m_stroke = null;

  /**
   * Default constructor (sets the consumed by paint flag to false).
   * <p>
   */
  public APointPainter() {
    // nop
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public final int compareTo(final T arg0) {
    return this.hashCode() - arg0.hashCode();
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#endPaintIteration(java.awt.Graphics)
   */
  public void endPaintIteration(final Graphics g2d) {
    // nop
  }

  /**
   * Caution: <code>{@link ClassCastException}</code> thrown if wrong type
   * given.
   * <p>
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  // @SuppressWarnings("unchecked")
  // @Override
  // public final boolean equals(Object arg0) {
  // boolean result = false;
  // if (arg0.getClass() == this.getClass()) {
  // result = this.compareTo((T) arg0) == 0;
  // }
  // return result;
  // }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final APointPainter< ? > other = (APointPainter< ? >) obj;
    if (this.m_color == null) {
      if (other.m_color != null) {
        return false;
      }
    } else if (!this.m_color.equals(other.m_color)) {
      return false;
    }
    if (this.m_colorFill == null) {
      if (other.m_colorFill != null) {
        return false;
      }
    } else if (!this.m_colorFill.equals(other.m_colorFill)) {
      return false;
    }
    if (this.m_stroke == null) {
      if (other.m_stroke != null) {
        return false;
      }
    } else if (!this.m_stroke.equals(other.m_stroke)) {
      return false;
    }
    return true;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#getColor()
   */
  public Color getColor() {
    return this.m_color.getColor();
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#getColorFill()
   */
  public Color getColorFill() {
    return this.m_colorFill.getColor();
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#getStroke()
   */
  public Stroke getStroke() {
    return this.m_stroke;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#getTransparency()
   */
  public int getTransparency() {
    return this.m_color.getAlpha();
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#getTransparencyFill()
   */
  public int getTransparencyFill() {
    return this.m_colorFill.getAlpha();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_color == null) ? 0 : this.m_color.hashCode());
    result = prime * result + ((this.m_colorFill == null) ? 0 : this.m_colorFill.hashCode());
    result = prime * result + ((this.m_stroke == null) ? 0 : this.m_stroke.hashCode());
    return result;
  }

  /**
   * Installs the color to the graphics context if and only if a color has been
   * set.
   * <p>
   * 
   * @see #setColor(Color)
   * 
   * @param g
   *          the graphics context to use.
   * 
   * @return the previous color of the graphics context or <code>null</code> if
   *         no action was taken.
   */
  protected Color installColor(final Graphics g) {
    return this.m_color.applyColorIfChange(g);
  }

  /**
   * Installs the fill color to the graphics context if and only if a fill color
   * has been set.
   * <p>
   * 
   * @see #setColorFill(Color)
   * 
   * @param g
   *          the graphics context to use.
   * 
   * @return the previous color of the graphics context or <code>null</code> if
   *         no action was taken.
   */
  protected Color installColorFill(final Graphics g) {
    return this.m_colorFill.applyColorUnconditionally(g);
  }

  /**
   * Installs the stroke to the graphics context if and only if a stroke has
   * been set.
   * <p>
   * 
   * @see #setStroke(Stroke)
   * 
   * @param g
   *          the graphics context to use.
   * 
   * @return the previous stroke of the graphics context or <code>null</code> if
   *         no action was taken.
   */
  protected Stroke installStroke(final Graphics g) {
    Stroke result = null;
    if (this.m_stroke != null) {
      if (g instanceof Graphics2D) {
        final Graphics2D g2d = (Graphics2D) g;
        result = g2d.getStroke();
        g2d.setStroke(this.m_stroke);
      } else {
        System.out.println("Cannot use stroke as given graphic context is of wrong type: "
            + g.getClass().getName());
      }
    }
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#setColor(java.awt.Color)
   */
  public Color setColor(final Color color) {
    final Color result = this.m_color.setColor(color);
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#setColorFill(java.awt.Color)
   */
  public Color setColorFill(final Color fillColor) {
    return this.m_colorFill.setColor(fillColor);
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#setStroke(java.awt.Stroke)
   */
  public Stroke setStroke(final Stroke stroke) {
    final Stroke result = this.m_stroke;
    this.m_stroke = stroke;
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#setTransparency(int)
   */
  public int setTransparency(final int transparency0to255) {
    return this.m_color.setAlpha(transparency0to255);
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainterConfigurableUI#setTransparencyFill(int)
   */
  public int setTransparencyFill(final int transparency0to255) {
    return this.m_colorFill.setAlpha(transparency0to255);
  }

  /**
   * @see info.monitorenter.gui.chart.IPointPainter#startPaintIteration(java.awt.Graphics)
   */
  public void startPaintIteration(final Graphics g2d) {
    // nop
  }

}
