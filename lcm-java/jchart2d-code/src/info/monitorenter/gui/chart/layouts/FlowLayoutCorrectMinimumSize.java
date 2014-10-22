/*  FlowLayoutCorrectMinimumSize.java of project jchart2d, <purpose>
 *  Copyright (c) 2006 - 2011 Achim Westermann, created on 08.10.2006 14:44:18.
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
package info.monitorenter.gui.chart.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * A flow layout that claims the correct height of the component managed in case
 * the available width is known. The standard <code>{@link java.awt.FlowLayout}</code> does
 * not claim the correct size but chooses the maximum width of all components to
 * render which is worthless as the fact of flow breaks is not taken into
 * account.
 * <p>
 * 
 * This class is inspired by the sun class
 * <code>{@link java.awt.FlowLayout}</code> with modifications to the methods
 * <code>{@link #preferredLayoutSize(Container)}</code> and
 * <code>{@link #minimumLayoutSize(Container)}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * @version $Revision: 1.13 $
 */
public class FlowLayoutCorrectMinimumSize extends FlowLayout {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 5192358035459949687L;

  /**
   * Constructs a new <code>FlowLayout</code> with the specified alignment and
   * a default 5-unit horizontal and vertical gap. The value of the alignment
   * argument must be one of <code>FlowLayout.LEFT</code>,
   * <code>FlowLayout.RIGHT</code>, <code>FlowLayout.CENTER</code>,
   * <code>FlowLayout.LEADING</code>, or <code>FlowLayout.TRAILING</code>.
   * 
   * @param align
   *            the alignment value
   */
  public FlowLayoutCorrectMinimumSize(final int align) {
    this(align, 5, 5);
  }

  /**
   * Creates a new flow layout manager with the indicated alignment and the
   * indicated horizontal and vertical gaps.
   * <p>
   * The value of the alignment argument must be one of
   * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
   * <code>FlowLayout.CENTER</code>, <code>FlowLayout.LEADING</code>, or
   * <code>FlowLayout.TRAILING</code>.
   * 
   * @param align
   *            the alignment value
   * @param hgap
   *            the horizontal gap between components and between the components
   *            and the borders of the <code>Container</code>
   * @param vgap
   *            the vertical gap between components and between the components
   *            and the borders of the <code>Container</code>
   */
  public FlowLayoutCorrectMinimumSize(final int align, final int hgap, final int vgap) {
    super(align, hgap, vgap);
  }

  /**
   * Lays out the container. This method lets each <i>visible</i> component
   * take its preferred size by reshaping the components in the target container
   * in order to satisfy the alignment of this <code>FlowLayout</code> object.
   * 
   * @param target
   *            the specified component being laid out
   * @see Container
   * @see java.awt.Container#doLayout
   */
  @Override
  public void layoutContainer(final Container target) {
    synchronized (target.getTreeLock()) {
      int hgap = this.getHgap();
      int vgap = this.getVgap();

      Insets insets = target.getInsets();
      int maxwidth = target.getWidth() - (insets.left + insets.right + hgap * 2);
      int nmembers = target.getComponentCount();
      int x = 0;
      int y = insets.top + vgap;
      int rowh = 0;
      int start = 0;

      boolean ltr = target.getComponentOrientation().isLeftToRight();

      for (int i = 0; i < nmembers; i++) {
        Component m = target.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          m.setSize(d.width, d.height);

          if ((x == 0) || ((x + d.width) <= maxwidth)) {
            if (x > 0) {
              x += hgap;
            }
            x += d.width;
            rowh = Math.max(rowh, d.height);
          } else {
            this.moveComponents(target, insets.left + hgap, y, maxwidth - x, rowh, start, i, ltr);
            x = d.width;
            y += vgap + rowh;
            rowh = d.height;
            start = i;
          }
        }
      }
      this.moveComponents(target, insets.left + hgap, y, maxwidth - x, rowh, start, nmembers, ltr);
    }
  }

  /**
   * Returns the minimum dimensions needed to layout the <i>visible</i>
   * components contained in the specified target container.
   * 
   * @param target
   *            the container that needs to be laid out
   * @return the minimum dimensions to lay out the subcomponents of the
   *         specified container
   * @see #preferredLayoutSize
   * @see java.awt.Container
   * @see java.awt.Container#doLayout
   */
  @Override
  public Dimension minimumLayoutSize(final Container target) {
    synchronized (target.getTreeLock()) {
      int hgap = this.getHgap();
      int vgap = this.getVgap();
      Insets insets = target.getInsets();
      int maxwidth = target.getWidth() - (insets.left + insets.right + hgap * 2);

      int nmembers = target.getComponentCount();
      boolean firstVisibleComponent = true;

      int rowWidth = 0;
      int maxRowWidth = 0;
      int height = 0;
      for (int i = 0; i < nmembers; i++) {
        Component m = target.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getMinimumSize();
          if (firstVisibleComponent) {
            height = d.height;
            firstVisibleComponent = false;
          }
          if (rowWidth + hgap + d.width > maxwidth) {
            maxRowWidth = Math.max(rowWidth, maxRowWidth);
            height += (vgap + d.height);
            rowWidth = d.width;
          } else {
            rowWidth += (hgap + d.width);
          }
        }
      }

      Dimension dim = new Dimension(maxRowWidth, height);

      dim.width += insets.left + insets.right + hgap * 2;
      dim.height += insets.top + insets.bottom + vgap * 2;
      return dim;
    }
  }

  /**
   * Centers the elements in the specified row, if there is any slack.
   * 
   * @param target
   *            the component which needs to be moved
   * @param x
   *            the x coordinate
   * @param y
   *            the y coordinate
   * @param width
   *            the width dimensions
   * @param height
   *            the height dimensions
   * @param rowStart
   *            the beginning of the row
   * @param rowEnd
   *            the the ending of the row
   * @param ltr
   *            if true, left to right alignment is used.
   */
  private void moveComponents(final Container target, final int x, final int y, final int width,
      final int height, final int rowStart, final int rowEnd, final boolean ltr) {
    synchronized (target.getTreeLock()) {
      int xMod = x;
      int hgap = this.getHgap();
      switch (this.getAlignment()) {
        case LEFT:
          xMod += ltr ? 0 : width;
          break;
        case CENTER:
          xMod += width / 2;
          break;
        case RIGHT:
          xMod += ltr ? width : 0;
          break;
        case LEADING:
          break;
        case TRAILING:
          xMod += width;
          break;
        default:
          break;
      }
      for (int i = rowStart; i < rowEnd; i++) {
        Component m = target.getComponent(i);
        if (m.isVisible()) {
          if (ltr) {
            m.setLocation(xMod, y + (height - m.getHeight()) / 2);
          } else {
            m
                .setLocation(target.getWidth() - xMod - m.getWidth(), y + (height - m.getHeight())
                    / 2);
          }
          xMod += m.getWidth() + hgap;
        }
      }
    }
  }

  /**
   * Returns the preferred dimensions for this layout given the <i>visible</i>
   * components in the specified target container.
   * 
   * @param target
   *            the container that needs to be laid out
   * @return the preferred dimensions to lay out the subcomponents of the
   *         specified container
   * @see Container
   * @see #minimumLayoutSize
   * @see java.awt.Container#getPreferredSize
   */
  @Override
  public Dimension preferredLayoutSize(final Container target) {
    synchronized (target.getTreeLock()) {
      int hgap = this.getHgap();
      int vgap = this.getVgap();
      Insets insets = target.getInsets();
      int maxwidth = target.getWidth() - (insets.left + insets.right + hgap * 2);

      int nmembers = target.getComponentCount();
      boolean firstVisibleComponent = true;

      int rowWidth = 0;
      int maxRowWidth = 0;
      int height = 0;
      for (int i = 0; i < nmembers; i++) {
        Component m = target.getComponent(i);
        if (m.isVisible()) {
          Dimension d = m.getPreferredSize();
          if (firstVisibleComponent) {
            height = d.height;
            firstVisibleComponent = false;
          }
          if (rowWidth + hgap + d.width > maxwidth) {
            maxRowWidth = Math.max(rowWidth, maxRowWidth);
            height += (vgap + d.height);
            rowWidth = d.width;
          } else {
            rowWidth += (hgap + d.width);
          }
        }
      }

      Dimension dim = new Dimension(maxRowWidth, height);

      dim.width += insets.left + insets.right + hgap * 2;
      dim.height += insets.top + insets.bottom + vgap * 2;
      return dim;
    }
  }
}
