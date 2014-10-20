/*
 * ErrorBarPainterEditPanel.java of project jchart2d, a panel for configuration of a single ErrorBarConfigurable. Copyright (c) 2007 - 2011
 * Achim Westermann, created on 09:50:20.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * If you modify or optimize the code in a useful way please let me know. Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart.controls.errorbarwizard;

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IPointPainterConfigurableUI;
import info.monitorenter.gui.chart.events.ErrorBarPainterActionSetSegmentColor;
import info.monitorenter.gui.chart.events.ErrorBarPainterActionSetSegmentPainter;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.pointpainters.PointPainterLine;
import info.monitorenter.gui.util.ColorIcon;
import info.monitorenter.util.FileUtil;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * A panel for selection of {@link info.monitorenter.gui.chart.IErrorBarPainter#setStartPointPainter(IPointPainterConfigurableUI)}
 * {@link info.monitorenter.gui.chart.IErrorBarPainter#setEndPointPainter(info.monitorenter.gui.chart.IPointPainterConfigurableUI)} and
 * {@link info.monitorenter.gui.chart.IErrorBarPainter#setConnectionPainter(info.monitorenter.gui.chart.IPointPainterConfigurableUI)} .
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.16 $
 */
public class ErrorBarPainterEditPanel extends JPanel {

    /**
     * Base implementation that allows selection of an <code>{@link info.monitorenter.gui.chart.IPointPainter}</code> and it's
     * <code>{@link java.awt.Color}</code> and holds the parental <code>{@link IErrorBarPainter}</code> to assign the painter to (as a
     * segment: start, end or connetion).
     * <p>
     * 
     * Implementations have to add the action listeners that defines to which segment of the <code>{@link IErrorBarPainter}</code>. the
     * configured painter will set to.
     * <p>
     * 
     * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
     * 
     * 
     * @version $Revision: 1.16 $
     */
    private class SegmentChooserPanel extends JPanel {

        /**
         * An adaptor that registers itself as a <code>{@link PropertyChangeListener}</code> on an
         * <code>{@link IErrorBarPainter.ISegment}</code> and sets the color to the constructor given <code>{@link ColorIcon}</code>.
         * <p>
         * 
         * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
         * 
         * 
         * @version $Revision: 1.16 $
         */
        final class ColorIconUpdater implements PropertyChangeListener {

            /** The color icon to adapt to the color of the segment. */
            private ColorIcon m_adaptee;

            /**
             * Constructor with the adaptee which will stick to the color of the given segment.
             * <p>
             * 
             * @param adaptee
             *            the color icon to adapt the color property of the segment to.
             */
            protected ColorIconUpdater(final ColorIcon adaptee) {
                this.m_adaptee = adaptee;
            }

            /**
             * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
             */
            public void propertyChange(final PropertyChangeEvent evt) {
                String property = evt.getPropertyName();
                if (ErrorBarPainterEditPanel.SegmentChooserPanel.this.m_segment.getPropertySegmentColor().equals(property)) {
                    this.m_adaptee.setColor((Color) evt.getNewValue());
                }
            }

        }

        /** Generated <code>serialVersionUID</code>. */
        private static final long serialVersionUID = 6645527616687209066L;

        /** The error bar painter segment to configure. */
        protected IErrorBarPainter.ISegment m_segment;

        /**
         * Creates a panel that offers configuration of the given error bar painter.
         * <p>
         * 
         * @param errorBarPainterSegement
         *            the error bar painter segment to configure.
         */
        public SegmentChooserPanel(final IErrorBarPainter.ISegment errorBarPainterSegement) {

            super();
            this.m_segment = errorBarPainterSegement;

            this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), errorBarPainterSegement.getName(),
                            TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION));
            // complex layout needed for ensuring that
            // both labes are displayed vertically stacked but
            // with the same distance to their text fields regardless
            // of their label width:
            this.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.gridx = 0;

            // Obtaining model values and required data:
            // Color selector:
            final AbstractAction colorAction = new ErrorBarPainterActionSetSegmentColor(errorBarPainterSegement, this, "color");

            // The select box for segment painters:
            // TODO: maybe check not to allow point painters that connect start and
            // end (e.g. line) to use in start and end segment.
            JComboBox painterSelector = new JComboBox();
            IPointPainterConfigurableUI<?> pointPainter;
            IPointPainterConfigurableUI<?> usedPainter = errorBarPainterSegement.getPointPainter();
            Action action;

            pointPainter = null;
            boolean painterSelected = false;
            action = new ErrorBarPainterActionSetSegmentPainter(errorBarPainterSegement, pointPainter, "Empty");
            painterSelector.addItem(action);
            if (usedPainter == null) {
                painterSelector.setSelectedItem(action);
            }

            pointPainter = new PointPainterLine();
            action = new ErrorBarPainterActionSetSegmentPainter(errorBarPainterSegement, pointPainter, FileUtil.cutExtension(
                            pointPainter.getClass().getName()).getValue());
            painterSelector.addItem(action);

            if (usedPainter != null && pointPainter.getClass().equals(usedPainter.getClass())) {
                painterSelector.setSelectedItem(action);
                painterSelected = true;
            }

            pointPainter = new PointPainterDisc();
            action = new ErrorBarPainterActionSetSegmentPainter(errorBarPainterSegement, pointPainter, FileUtil.cutExtension(
                            pointPainter.getClass().getName()).getValue());
            painterSelector.addItem(action);
            if (usedPainter != null && pointPainter.getClass().equals(usedPainter.getClass())) {
                painterSelector.setSelectedItem(action);
                painterSelected = true;
            }

            // handling events of the combo box:
            painterSelector.addActionListener(new ActionListener() {

                /**
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                public void actionPerformed(final ActionEvent e) {
                    JComboBox source = (JComboBox) e.getSource();
                    Action currentAction = (Action) source.getSelectedItem();
                    currentAction.actionPerformed(e);
                    if(currentAction.getValue(Action.NAME).equals("Empty")) {
                        colorAction.setEnabled(false);
                    } else {
                        colorAction.setEnabled(true);
                    }
                }
            });

            this.add(painterSelector, gbc);

            // Color selector config:
            ColorIcon colorIcon = new ColorIcon(errorBarPainterSegement.getColor());
            colorAction.putValue(Action.SMALL_ICON, colorIcon);
            colorAction.setEnabled(painterSelected);
            JButton colorChooserButton = new JButton(colorAction);
            gbc.gridy = 1;
            // allow the color icon to update itself when the segment color changes:
            errorBarPainterSegement.addPropertyChangeListener(errorBarPainterSegement.getPropertySegmentColor(), new ColorIconUpdater(
                            colorIcon));
            this.add(colorChooserButton, gbc);
        }
    }

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -6564631494967160532L;

    /** The error bar painter to configure with segments. */
    private IErrorBarPainter m_errorBarPainter;

    /**
     * Creates a panel that offers configuration of the given error bar painter.
     * <p>
     * 
     * @param errorBarPainter
     *            the error bar painter to configure.
     */
    public ErrorBarPainterEditPanel(final IErrorBarPainter errorBarPainter) {

        super();
        this.m_errorBarPainter = errorBarPainter;

        // complex layout needed for ensuring that
        // both labels are displayed vertically stacked but
        // with the same distance to their text fields regardless
        // of their label width:
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridy = 0;
        gbc.insets = new Insets(2, 2, 2, 2);

        // start segment:
        this.add(new SegmentChooserPanel(this.m_errorBarPainter.getSegmentStart()), gbc);

        // connection segment:
        gbc.gridy = 1;
        this.add(new SegmentChooserPanel(this.m_errorBarPainter.getSegmentConnection()), gbc);

        // end segment:
        gbc.gridy = 2;
        this.add(new SegmentChooserPanel(this.m_errorBarPainter.getSegmentEnd()), gbc);
    }

}
