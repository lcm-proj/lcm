/*
 *  ColorMutable.java of project jchart2d, a mutable color. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on Jun 10, 2011
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/util/ColorMutable.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.3 $
 */

package info.monitorenter.gui.util;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Mutable <code>{@link Color}</code> implementation.
 * <p>
 * 
 * The color is wrapped by the logic to fake mutability. As long as the color is
 * not set setting of values will not have an effect but only be stored until
 * color is set and then applied to it.
 * <p>
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public class ColorMutable {

  /** Transparency value. */
  private int m_alpha = -1;

  /** Blue value. */
  private int m_blue = -1;

  /** The wrapped color. */
  private Color m_color;

  /** Green value. */
  private int m_green = -1;

  /** Red value. */
  private int m_red = -1;

  /**
   * Sets the color to the graphics context if it is different and returns the
   * previous one of the graphics context or null if there was no change.
   * <p>
   * 
   * @param g
   *          the graphics context to use. 
   * 
   * @return the previously configured color of the graphics context or null if
   *         nothing was done.
   */
  public synchronized Color applyColorIfChange(final Graphics g) {
    Color result = null;
    if (this.m_color != null) {
      final Color gColor = g.getColor();
      if (!gColor.equals(this.m_color)) {
        result = gColor;
        g.setColor(this.m_color);
      }
    }
    return result;
  }
  
  /**
   * Sets the color to the graphics context.
   * <p>
   * 
   * @param g
   *          the graphics context to use. 
   * 
   * @return the previously configured color of the graphics context or null if
   *         nothing was done.
   */
  public synchronized Color applyColorUnconditionally(final Graphics g) {
    Color result = null;
    if (this.m_color != null) {
      final Color gColor = g.getColor();
      result = gColor;
      g.setColor(this.m_color);
    }
    return result;
  }
  

  /**
   * Asserts that the given value is between 0 and 255.
   * <p>
   * 
   * @param value
   *          the value to check.
   */
  private void assertColorValue(final int value) {
    if (!((value >= 0) && (value <= 255))) {
      throw new RuntimeException("Argument has to be between 0 and 255. It is " + value);
    }

  }

  /**
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
    final ColorMutable other = (ColorMutable) obj;
    if (this.m_alpha != other.m_alpha) {
      return false;
    }
    if (this.m_blue != other.m_blue) {
      return false;
    }
    if (this.m_color == null) {
      if (other.m_color != null) {
        return false;
      }
    } else if (!this.m_color.equals(other.m_color)) {
      return false;
    }
    if (this.m_green != other.m_green) {
      return false;
    }
    if (this.m_red != other.m_red) {
      return false;
    }
    return true;
  }

  /**
   * Returns the alpha value of the wrapped color.
   * <p>
   * 
   * @return the alpha value of the wrapped color.
   */

  public int getAlpha() {
    return this.m_alpha;
  }

  /**
   * Returns the blue value of the wrapped color.
   * <p>
   * 
   * @return the blue value of the wrapped color.
   */
  public int getBlue() {
    return this.m_blue;
  }

  /**
   * Returns the wrapped color.
   * <p>
   * 
   * This will be <code>null</code> if no color has been set before even if
   * other values have been set before.
   * <p>
   * 
   * @return the wrapped color.
   */
  public Color getColor() {
    return this.m_color;
  }

  /**
   * Returns the green value of the wrapped color.
   * <p>
   * 
   * @return the green value of the wrapped color.
   */
  public int getGreen() {
    return this.m_green;
  }

  /**
   * Returns the red value of the wrapped color.
   * <p>
   * 
   * @return the red value of the wrapped color.
   */
  public int getRed() {
    return this.m_red;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.m_alpha;
    result = prime * result + this.m_blue;
    result = prime * result + ((this.m_color == null) ? 0 : this.m_color.hashCode());
    result = prime * result + this.m_green;
    result = prime * result + this.m_red;
    return result;
  }

  /**
   * Sets the transparency to use for painting.
   * <p>
   * This value will be fold into color. If color has not been configured before
   * it will not have any effect (until a color is set).
   * <p>
   * Caution: using a value greater 0 may cost a multiple cpu load!
   * <p>
   * 
   * @param alpha
   *          a transparency value between 0 and 255.
   * 
   * @return the previous transparency used.
   */
  public synchronized int setAlpha(final int alpha) {
    final int result = this.m_alpha;
    this.assertColorValue(alpha);
    this.m_alpha = alpha;
    if (this.m_color == null) {
      // nop, the value is cached for the first time use when a color is set.
    } else {
      // also directly implant the transparency value to the color.
      final int orgTransparencyInt = this.m_color.getAlpha();
      if (alpha != orgTransparencyInt) {
        final Color transparentColor = new Color(this.m_color.getRed(), this.m_color.getGreen(),
            this.m_color.getBlue(), alpha);
        this.setColor(transparentColor);
      }
    }
    return result;
  }

  /**
   * Sets the blue to use for painting.
   * <p>
   * This value will be fold into color. If color has not been configured before
   * it will not have any effect (until a color is set).
   * <p>
   * 
   * @param blue
   *          a blue value between 0 and 255.
   * 
   * @return the previous blue used.
   */
  public synchronized int setBlue(final int blue) {
    final int result = this.m_blue;
    this.assertColorValue(blue);
    this.m_blue = blue;
    if (this.m_color == null) {
      // nop, the value is cached for the first time use when a color is set.
    } else {
      // also directly implant the transparency value to the colour.
      final int orgvalue = this.m_color.getBlue();
      if (blue != orgvalue) {
        final Color newColor = new Color(this.m_color.getRed(), this.m_color.getGreen(), blue,
            this.m_color.getAlpha());
        this.setColor(newColor);
      }
    }
    return result;
  }

  /**
   * Sets the color to use.
   * <p>
   * If any other setters have been invoked before (e.g.
   * <code>{@link #setAlpha(int)}</code>) and the internal color was null those
   * values will be implanted to the new color before overtaking it.
   * <p>
   * 
   * @param color
   *          the new color to use as base for modifications.
   * 
   * @return the previous color or null if none was set.
   */
  public synchronized Color setColor(final Color color) {
    final Color result = this.m_color;
    Color use = color;
    if (this.m_color == null) {

      int red = color.getRed();
      int green = color.getGreen();
      int blue = color.getBlue();
      int alpha = color.getAlpha();
      boolean change = false;
      if (this.m_red != -1) {
        red = this.m_red;
        change = true;
      }
      if (this.m_green != -1) {
        green = this.m_green;
        change = true;
      }
      if (this.m_blue != -1) {
        blue = this.m_blue;
        change = true;
      }
      if (this.m_alpha != -1) {
        alpha = this.m_alpha;
        change = true;
      }
      if (change) {
        use = new Color(red, green, blue, alpha);
      }
    }
    this.m_color = use;
    return result;
  }

  /**
   * Sets the green to use for painting.
   * <p>
   * This value will be fold into color. If color has not been configured before
   * it will not have any effect (until a color is set).
   * <p>
   * 
   * @param green
   *          a green value between 0 and 255.
   * 
   * @return the previous green used.
   */
  public synchronized int setGreen(final int green) {
    final int result = this.m_green;
    this.assertColorValue(green);
    this.m_green = green;
    if (this.m_color == null) {
      // nop, the value is cached for the first time use when a color is set.
    } else {
      // also directly implant the transparency value to the colour.
      final int orgvalue = this.m_color.getGreen();
      if (green != orgvalue) {
        final Color newColor = new Color(this.m_color.getRed(), green, this.m_color.getBlue(),
            this.m_color.getAlpha());
        this.setColor(newColor);
      }
    }
    return result;
  }

  /**
   * Sets the red to use for painting.
   * <p>
   * This value will be fold into color. If color has not been configured before
   * it will not have any effect (until a color is set).
   * <p>
   * 
   * @param red
   *          a red value between 0 and 255.
   * 
   * @return the previous red used.
   */
  public synchronized int setRed(final int red) {
    final int result = this.m_red;
    this.assertColorValue(red);
    this.m_red = red;
    if (this.m_color == null) {
      // nop, the value is cached for the first time use when a color is set.
    } else {
      // also directly implant the new value to the color.
      final int orgvalue = this.m_color.getRed();
      if (red != orgvalue) {
        final Color newColor = new Color(this.m_red, this.m_color.getGreen(), this.m_color
            .getBlue(), this.m_color.getAlpha());
        this.setColor(newColor);
      }
    }
    return result;
  }

}
