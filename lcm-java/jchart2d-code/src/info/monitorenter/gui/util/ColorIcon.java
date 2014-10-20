/*
 *  ColorIcon.java of project jchart2d, an icon that displays a 
 *  rectangular color.
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
package info.monitorenter.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * An icon that displays a rectangular <code>{@link java.awt.Color}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.7 $
 */
public class ColorIcon implements Icon {
  /** The color for the icon. */
  private Color m_color;

  /**
   * Creates an icon with the given color.
   * <p>
   * 
   * @param color
   *          the color of this icon.
   */
  public ColorIcon(final Color color) {
    this.m_color = color;
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
    final ColorIcon other = (ColorIcon) obj;
    if (this.m_color == null) {
      if (other.m_color != null) {
        return false;
      }
    } else if (!this.m_color.equals(other.m_color)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the color of this icon.
   * <p>
   * 
   * @return the color of this icon.
   */
  public final Color getColor() {
    return this.m_color;
  }

  /**
   * @see javax.swing.Icon#getIconHeight()
   */
  public int getIconHeight() {
    return 10;
  }

  /**
   * @see javax.swing.Icon#getIconWidth()
   */
  public int getIconWidth() {
    return 10;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_color == null) ? 0 : this.m_color.hashCode());
    return result;
  }

  /**
   * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int,
   *      int)
   */
  public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
    final Color old = g.getColor();
    // border:
    g.setColor(c.getBackground().darker());
    g.drawRect(x, y, 10, 10);
    // color:
    g.setColor(this.m_color);
    g.fillRect(x + 1, y + 1, 9, 9);
    g.setColor(old);
  }

  /**
   * Sets the color of this icon.
   * <p>
   * 
   * @param color
   *          The color of this icon to set.
   */
  public final void setColor(final Color color) {
    this.m_color = color;
  }

}
