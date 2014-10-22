/*
 * AnnotationPanel.java, a moveable panel containing annotations. Copyright (C) Achim Westermann.
 * 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on 13.02.2009
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/annotations/bubble/AnnotationBubble.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.9 $
 */

package info.monitorenter.gui.chart.annotations.bubble;

import info.monitorenter.gui.chart.annotations.AAnnotationContentComponent;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.util.UIUtil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * A movable container for annotations.
 * <p>
 * 
 */
public class AnnotationBubble extends JPanel {

    /**
     * Title Bar of <code>{@link AnnotationBubble}</code>.
     * <p>
     * 
     */
    public class AnnotationTitleBar extends JPanel {

        /** Generated <code>serialVersionUID</code>. **/
        private static final long serialVersionUID = 4182197132940971837L;

        /** close button in upper right (icon). **/
        private JButton m_closeButton;

        /**
         * Defcon.
         * <p>
         */
        public AnnotationTitleBar() {
            super();
            this.setOpaque(true);
            this.setBackground(AnnotationBubble.this.getBackground());
            // Dimension heightLimit = new Dimension(Integer.MAX_VALUE, 20);
            // this.setMaximumSize(heightLimit);
            // this.setPreferredSize(new Dimension(200, 20));
            // this.setMinimumSize(new Dimension(20, 20));
            DragListener dragListener = AnnotationBubble.this.m_dragListener;
            this.addMouseListener(dragListener);
            this.addMouseMotionListener(dragListener);
            // this.setBorder(BorderFactory.createLineBorder(Color.black, 1));
            Action closeAction = new AbstractAction() {
                /** Generated <code>serialVersionUID</code>. **/
                private static final long serialVersionUID = 800535446184152788L;

                /**
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(final ActionEvent e) {
                    AnnotationBubble.this.setVisible(false);
                    AnnotationBubble.this.m_chartPanel.remove(AnnotationBubble.this);
                }

            };
            Icon closeIcon = UIManager.getIcon("InternalFrame.closeIcon");
            // TODO: this is since 1.6 only, add backward compatibility
            closeAction.putValue(Action.SMALL_ICON, closeIcon);

            this.m_closeButton = new JButton(closeAction);
            this.m_closeButton.setContentAreaFilled(false);
            this.m_closeButton.setBorderPainted(false);
            this.m_closeButton.setFocusable(false);
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(Box.createHorizontalGlue());
            this.add(this.m_closeButton);
        }

        /**
         * @see javax.swing.JComponent#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize() {
            Dimension result = null;
            if (this.isPreferredSizeSet()) {
                result = super.getPreferredSize();
            } else {
                Dimension closeButtonDim = this.m_closeButton.getPreferredSize();
                Font font = this.getFont();
                FontMetrics fontMetrics = this.getFontMetrics(font);
                int fontHeight = fontMetrics.getHeight();
                int closeButtonHeight = (int) closeButtonDim.getHeight();

                int height = Math.max(fontHeight, closeButtonHeight);
                result = new Dimension((int) closeButtonDim.getWidth(), height);

            }
            return result;
        }

        /**
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public final void paint(final Graphics g) {
            super.paint(g);
        }

        /**
         * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
         */
        @Override
        protected final void paintBorder(final Graphics g) {
            super.paintBorder(g);
        }

        /**
         * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
         */
        @Override
        protected final void paintChildren(final Graphics g) {
            super.paintChildren(g);
        }

        /**
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(final Graphics g) {
            Color backupColor = g.getColor();
            g.setColor(Color.BLACK);
            Dimension size = this.getSize();

            // paint into clip (y) to only have the upper part of the border painted:
            g.drawRoundRect(0, 0, size.width - 1, size.height + 10, 10, 10);
            if (!AnnotationBubble.this.isDragged() || !AnnotationBubble.this.isOutlineDragMode()) {
                g.setColor(this.getBackground());
                g.fillRoundRect(1, 1, size.width - 2, size.height + 10, 10, 10);
            }
            g.setColor(backupColor);
            // super.paintComponent(g);
        }

    }

    /**
     * Listens for drag events on the chart to move the annotation panel.
     */
    public class DragListener implements MouseMotionListener, MouseListener {
        /** Needed to track relative dragments. */
        protected Point m_lastDragPosition;

        /**
         * Defcon.
         */
        protected DragListener() {
            // nop
        }

        /**
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(final MouseEvent e) {
            // nop
        }

        /**
         * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
         */
        public void mouseDragged(final MouseEvent e) {
            if (this.m_lastDragPosition != null) {

                // TODO: replace this with e.getLocationOnScreen(e) in 1.6:
                // this is the 1.5 alternative:
                Point pointNow = UIUtil.getLocationOnScreen(e);
                double deltaX = pointNow.getX() - this.m_lastDragPosition.getX();
                double deltaY = pointNow.getY() - this.m_lastDragPosition.getY();
                Point panelPos = AnnotationBubble.this.getLocation();
                panelPos.x += deltaX;
                panelPos.y += deltaY;
                AnnotationBubble.this.setLocation(panelPos);
                this.m_lastDragPosition = pointNow;
            }
        }

        /**
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(final MouseEvent e) {
            // nop
        }

        /**
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(final MouseEvent e) {
            // nop
        }

        /**
         * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
         */
        public void mouseMoved(final MouseEvent e) {
            // nop
        }

        /**
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        public void mousePressed(final MouseEvent e) {
            // TODO: replace this with e.getLocationOnScreen(e) in 1.6:
            // this is the 1.5 alternative:
            this.m_lastDragPosition = UIUtil.getLocationOnScreen(e);
        }

        /**
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(final MouseEvent e) {
            this.m_lastDragPosition = null;
            AnnotationBubble.this.repaint();
        }
    }

    /** Internal padding for content box. */
    public static final int PADDING = 8;

    /** Only held here to know whether annotation is dragged. */
    private DragListener m_dragListener;

    /** If true, only the outline will be painted when dragging. */
    private boolean m_outlineDragMode = true;

    /** Generated <code>serialVersionUID</code>. **/
    private static final long serialVersionUID = -3668420139916533725L;

    /** The chart listening on. */
    protected ChartPanel m_chartPanel;

    /** Content area ref. */
    protected AAnnotationContentComponent m_content;

    /** Title bar ref or null, if no title bar is used. */
    protected JComponent m_titleBar;

    /**
     * Creates an instance for the given chart.
     * <p>
     * 
     * @param chartPanel
     *            the chart panel this instance is added to as an annotation.
     * 
     * @param annotationContent
     *            the content area (information container) of the visible annotation information.
     * 
     * @param useDragListenerOnAnnotationContent
     *            if true the content area that contains the information of the annotation will support drag and drop as well as a basic
     *            right click popup menu.
     * 
     * @param useTitleBar
     *            if true, a title bar with close button for the annotation will be shown.
     */
    public AnnotationBubble(final ChartPanel chartPanel, final AAnnotationContentComponent annotationContent,
                    final boolean useDragListenerOnAnnotationContent, final boolean useTitleBar) {
        super();

        this.m_chartPanel = chartPanel;
        this.m_content = annotationContent;
        this.setBackground(new Color(0xff, 0xfe, 0xe9, 0x88));
        this.m_dragListener = new DragListener();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        if (useTitleBar) {
            this.m_titleBar = new AnnotationTitleBar();
            // dragPanel.setBackground(this.getBackground());
            this.add(this.m_titleBar);
        }
        this.calculateSize();
        if (useDragListenerOnAnnotationContent) {
            annotationContent.addMouseMotionListener(this.m_dragListener);
            annotationContent.addMouseListener(this.m_dragListener);
            annotationContent.setBackground(this.getBackground());
        }

        this.add(annotationContent);
        this.add(Box.createVerticalStrut(PADDING));
    }

    /**
     * Internal size adaption to the content components preferred dimension.
     */
    private void calculateSize() {

        Dimension d = this.m_content.getPreferredSize();
        double width = d.getWidth() + 4;
        double height = d.getHeight() + 4;
        if (this.m_titleBar != null) {
            height += this.m_titleBar.getPreferredSize().getHeight();
        }
        this.setSize(new Dimension((int) width, (int) height));

    }

    /**
     * Check whether this annotation is currently draggged.
     * <p>
     * 
     * @return true if this annotation is currently dragged.
     */
    public boolean isDragged() {
        boolean result = null != this.m_dragListener.m_lastDragPosition;
        return result;
    }

    /**
     * Returns the outlineDragMode.
     * <p>
     * 
     * @return the outlineDragMode
     */
    public final boolean isOutlineDragMode() {
        return this.m_outlineDragMode;
    }

    /**
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public final void paint(final Graphics g) {
        super.paint(g);
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(final Graphics g) {
        Color backupColor = g.getColor();
        g.setColor(Color.BLACK);
        Dimension size = this.getSize();
        g.drawRoundRect(0, 0, size.width - 1, size.height - 1, 10, 10);
        if (!AnnotationBubble.this.isDragged() || !AnnotationBubble.this.isOutlineDragMode()) {
            g.setColor(this.getBackground());
            g.fillRoundRect(1, 1, size.width - 2, size.height - 2, 8, 8);
        }
        g.setColor(backupColor);

        // super.paintComponent(g);
    }

    /**
     * Sets the outlineDragMode.
     * <p>
     * 
     * @param outlineDragMode
     *            the outlineDragMode to set
     */
    public final void setOutlineDragMode(final boolean outlineDragMode) {
        this.m_outlineDragMode = outlineDragMode;
    }

    /**
     * Sets the transparency of the annotation.
     * <p>
     * Caution: When using a value greater 0 dragging the annotation may cost a multiple cpu load and dragging speed! In this case consider
     * setting the paintOnDragging property to false;
     * 
     * @param zeroToOneAlpha
     *            a transparency value between 0.0 and 1.0.
     */
    public void setTransparency(final double zeroToOneAlpha) {
        assert (zeroToOneAlpha >= 0.0 && zeroToOneAlpha < 1.0);
        Color bgColor = this.getBackground();
        synchronized (this) {
            // avoid alpha channel as dragging transparent annotations is very
            // expensive:
            if (zeroToOneAlpha != 0) {
                int alphaInt = (int) zeroToOneAlpha * 255;
                if (bgColor.getAlpha() != alphaInt) {
                    this.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), alphaInt));
                }
            } else {
                if (bgColor.getAlpha() > 0) {
                    this.setBackground(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()));
                }
            }
        }
    }

}
