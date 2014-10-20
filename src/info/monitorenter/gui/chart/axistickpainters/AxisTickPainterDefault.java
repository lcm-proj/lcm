/*
 * AxisTickPainterDefault.java,  default IAxisTickPainter 
 * implementation for a tick painter that uses all given arguments 
 * (no proprietary behavior)
 * Copyright (C) 2007 - 2011 Achim Westermann, Achim.Westermann@gmx.de
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * If you modify or optimize the code in a useful way please let me know.
 * Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart.axistickpainters;

import info.monitorenter.gui.chart.IAxisTickPainter;

import java.awt.Graphics;

/**
 * Default implementation for a tick painter that uses all given arguments (no
 * proprietary behaviour).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 * 
 */
public class AxisTickPainterDefault implements IAxisTickPainter {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 4182009686170740710L;

  /** The length of a major tick in pixel. */
  private static final int MAJOR_TICK_LENGTH = 5;

  /** The length of a minor tick in pixel. */
  private static final int MINOR_TICK_LENGTH = 2;

  /**
   * Defcon.
   * <p>
   */
  public AxisTickPainterDefault() {
    // nop
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTickPainter#getMajorTickLength()
   */
  public int getMajorTickLength() {

    return AxisTickPainterDefault.MAJOR_TICK_LENGTH;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTickPainter#getMinorTickLength()
   */
  public int getMinorTickLength() {
    return AxisTickPainterDefault.MINOR_TICK_LENGTH;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTickPainter#paintXLabel(int, int,
   *      java.lang.String, java.awt.Graphics)
   */
  public void paintXLabel(final int x, final int y, final String label, final Graphics g) {
    g.drawString(label, x, y);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTickPainter#paintXTick(int, int,
   *      boolean, boolean, java.awt.Graphics)
   */
  public void paintXTick(final int x, final int y, final boolean isMajorTick,
      final boolean isBottomSide, final Graphics g) {
    if (isMajorTick) {
      if (isBottomSide) {
        g.drawLine(x, y, x, y + AxisTickPainterDefault.MAJOR_TICK_LENGTH);
      } else {
        g.drawLine(x, y, x, y - AxisTickPainterDefault.MAJOR_TICK_LENGTH);
      }
    } else {
      if (isBottomSide) {
        g.drawLine(x, y, x, y + AxisTickPainterDefault.MINOR_TICK_LENGTH);
      } else {
        g.drawLine(x, y, x, y - AxisTickPainterDefault.MINOR_TICK_LENGTH);
      }
    }
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTickPainter#paintYLabel(int, int,
   *      java.lang.String, java.awt.Graphics)
   */
  public void paintYLabel(final int x, final int y, final String label, final Graphics g) {
    g.drawString(label, x, y);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisTickPainter#paintYTick(int, int,
   *      boolean, boolean, java.awt.Graphics)
   */
  public void paintYTick(final int x, final int y, final boolean isMajorTick,
      final boolean isLeftSide, final Graphics g) {
    if (isMajorTick) {
      if (isLeftSide) {
        g.drawLine(x, y, x - AxisTickPainterDefault.MAJOR_TICK_LENGTH, y);
      } else {
        g.drawLine(x, y, x + AxisTickPainterDefault.MAJOR_TICK_LENGTH, y);
      }
    } else {
      if (isLeftSide) {
        g.drawLine(x, y, x - AxisTickPainterDefault.MINOR_TICK_LENGTH, y);
      } else {
        g.drawLine(x, y, x + AxisTickPainterDefault.MINOR_TICK_LENGTH, y);
      }
    }
  }
}
