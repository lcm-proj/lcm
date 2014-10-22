/*
 *  HSBColor.java, translates RGB colors into Hue-Saturation-Brightness 
 *  colors.
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.util;

/**
 * Color that internally works with the Hue Saturation Luminance color space.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 */
public class HSBColor implements java.io.Serializable, Cloneable {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3257288036910903863L;

  /**
   * Inspired by
   * <code>float[] java.awt.Color.RGBtoHSB(int r,int g, int b, float[]hsbvals)</code>
   * except that algorithm is tuned <br>
   * Testing results showed about 25% speed up. Therefore the sources have
   * become harder to understand.
   * 
   * @param color
   *          the <code>java.awt.Color</code> (that follows the RGB model) and
   *          should be transformed to a color instance in the
   *          hue-saturation-luminance model.
   * 
   * @return the transformed values of the RGB colors in that order:
   *         hue,saturation,brightness.
   */
  public static HSBColor rgbToHSB(final java.awt.Color color) {
    // TODO: Fix alpha treatment!!!!
    final int rgb = color.getRGB();
    final int r = (rgb & 0xFF0000) >> 16;
    final int g = (rgb & 0xFF00) >> 8;
    final int b = (rgb & 0xFF);
    final HSBColor ret = new HSBColor();

    final int cmax = (r >= g) ? (r >= b) ? r : b : (g >= b) ? g : b;
    final int cmin = (r <= g) ? (r <= b) ? r : b : (g <= b) ? g : b;
    ret.m_lum = (cmax) / 255f;
    if (cmax != cmin) {
      final float difference = (cmax - cmin);
      ret.m_sat = difference / (cmax);
      if (r == cmax) {
        ret.m_hue = (g - b) / difference;
      } else if (g == cmax) {
        ret.m_hue = (b - r) / difference + 2.0f;
      } else {
        ret.m_hue = (r - g) / difference + 4.0f;
      }
      ret.m_hue /= 6.0f;
      if (ret.m_hue < 0) {
        ret.m_hue += 1.0f;
      }
    } else {
      ret.m_sat = 0;
      ret.m_hue = 0;
    }
    ret.m_alpha = color.getAlpha();
    return ret;
  }

  /** Hue value between 0.0 and 1.0. */
  protected double m_hue;

  /** Luminance value between 0.0 and 1.0. */
  protected double m_lum;

  /** Saturation value between 0.0 and 1.0. */
  protected double m_sat;

  /**
   * The unused alpha channel between 0 and 255: stored here for allow
   * java.awt.Color instances to be transformed to instances of this class and
   * be re - transformed with preserving their alpha setting.
   */
  protected double m_alpha;

  /**
   * Constructor for internal use only.
   * <p>
   */
  private HSBColor() {
    // nop
  }

  /**
   * Creates an instance with the given values for hue saturation and luminance.
   * <p>
   * 
   * @param hue
   *          the hue component of the HSBColor
   * @param saturation
   *          the saturation component of the HSBColor
   * @param brightness
   *          the brightness component of the HSBColor
   */
  HSBColor(final double hue, final double saturation, final double brightness) {
    this(hue, saturation, brightness, 255);
  }

  /**
   * Creates an instance with the given values for hue saturation, luminance and
   * alpha.
   * <p>
   * 
   * @param hue
   *          the hue component of the HSBColor
   * 
   * @param saturation
   *          the saturation component of the HSBColor
   * 
   * @param brightness
   *          the brightness component of the HSBColor
   * 
   * @param alpha
   *          the alpha channed between 0.0 and 1.0.
   */
  HSBColor(final double hue, final double saturation, final double brightness, final int alpha) {
    this.m_hue = hue;
    this.m_sat = saturation;
    this.m_lum = brightness;
    this.m_alpha = alpha;
  }

  /**
   * Creates an instance transformed from the rgb color.
   * <p>
   * 
   * @param rgbcolor
   *          standard java rgb color.
   */
  public HSBColor(final java.awt.Color rgbcolor) {
    final int rgb = rgbcolor.getRGB();
    final int r = (rgb & 0xFF0000) >> 16;
    final int g = (rgb & 0xFF00) >> 8;
    final int b = (rgb & 0xFF);
    final int cmax = (r >= g) ? (r >= b) ? r : b : (g >= b) ? g : b;
    final int cmin = (r <= g) ? (r <= b) ? r : b : (g <= b) ? g : b;
    this.m_lum = (cmax) / 255f;
    if (cmax != cmin) {
      final float difference = (cmax - cmin);
      this.m_sat = difference / (cmax);
      if (r == cmax) {
        this.m_hue = (g - b) / difference;
      } else if (g == cmax) {
        this.m_hue = (b - r) / difference + 2.0;
      } else {
        this.m_hue = (r - g) / difference + 4.0;
      }
      this.m_hue /= 6.0;
      if (this.m_hue < 0) {
        this.m_hue += 1.0;
      }
    } else {
      this.m_sat = 0;
      this.m_hue = 0;
    }

    this.m_alpha = rgbcolor.getAlpha();
  }

  /**
   * Clone implementation.
   * <p>
   * 
   * Following statements are true: <br>
   * <code>
   *  x.clone() != x
   *  x.clone().getClass() == x.getClass()
   *  x.clone().equals(x)
   * </code> A deep copy of this HSBColor is returned.
   * <p>
   * 
   * @return an instance copied from this one.
   */
  @Override
  public Object clone() {
    HSBColor result = null;
    try {
      result = (HSBColor) super.clone();
      result.m_hue = this.m_hue;
      result.m_lum = this.m_lum;
      result.m_sat = this.m_sat;
      result.m_alpha = this.m_alpha;
    } catch (final CloneNotSupportedException e) {
      result = new HSBColor((float) this.m_hue, (float) this.m_sat, (float) this.m_lum, (int) Math
          .round(this.m_alpha));
    }
    return result;
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
    final HSBColor other = (HSBColor) obj;
    if (Double.doubleToLongBits(this.m_alpha) != Double.doubleToLongBits(other.m_alpha)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_hue) != Double.doubleToLongBits(other.m_hue)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_lum) != Double.doubleToLongBits(other.m_lum)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_sat) != Double.doubleToLongBits(other.m_sat)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the transformation of this color to the rgb color.
   * <p>
   * 
   * @return the transformation of this color to the rgb color.
   */

  public java.awt.Color getRGBColor() {
    final int rgb = java.awt.Color.HSBtoRGB((float) this.m_hue, (float) this.m_sat,
        (float) this.m_lum);
    // This does not work as it filters out the alpha channel!
    // return new java.awt.Color(rgb);
    final int r = (rgb & 0xff0000) >> 16;
    final int g = (rgb & 0x00ff00) >> 8;
    final int b = (rgb & 0x0000ff);
    return new java.awt.Color(r, g, b, (int) Math.round(this.m_alpha));

  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(this.m_alpha);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_hue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_lum);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_sat);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
