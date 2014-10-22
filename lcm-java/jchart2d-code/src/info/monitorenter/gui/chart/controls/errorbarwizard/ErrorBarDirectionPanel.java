/*
 *  ErrorBarDirectionPanel.java of project jchart2d, a panel 
 *  for selection of the directions of error bars. 
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 09:50:20.
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
package info.monitorenter.gui.chart.controls.errorbarwizard;

import info.monitorenter.gui.chart.IErrorBarPolicy;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel for selection of the directions of error bars.
 * <p>
 * See
 * <code>{@link info.monitorenter.gui.chart.errorbars.AErrorBarPolicyConfigurable}</code>
 * for more information on the configurable directions of error bars.
 * <p>
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.8 $
 */
public class ErrorBarDirectionPanel
    extends JPanel {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 7474825906798331943L;

  /**
   * Creates an instance that hooks as a control (view & controller) to the
   * error bar policy model instance.
   * <p>
   * 
   * @param errorBarPolicy
   *          the error bar policy to control directions of display of.
   */
  public ErrorBarDirectionPanel(final IErrorBarPolicy<?> errorBarPolicy) {

    super();

    this.setBorder(BorderFactory.createEtchedBorder());
    this.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(2, 2, 2, 2);
    // The checkboxes for dimension / direction
    // Negative x error
    JLabel label = new JLabel("Negative X");
    label.setToolTipText("Show error bars in negative x direction.");
   
    this.add(label, gbc);

    JCheckBox box = new JCheckBox();
    if (errorBarPolicy.isShowNegativeXErrors()) {
      box.setSelected(true);
    }
    box.addActionListener(new ActionListener() {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(final ActionEvent e) {
        JCheckBox checkbox = (JCheckBox) e.getSource();
        boolean boxState = checkbox.isSelected();
        boolean change = boxState != errorBarPolicy.isShowNegativeXErrors();
        if (change) {
          errorBarPolicy.setShowNegativeXErrors(boxState);
        }
      }
    });

    gbc.gridx = 1;
    this.add(box, gbc);

    // Negative y error
    label = new JLabel("Negative Y");
    label.setToolTipText("Show error bars in negative y direction.");
    gbc.gridx = 2;
    this.add(label, gbc);
    box = new JCheckBox();
    if (errorBarPolicy.isShowNegativeYErrors()) {
      box.setSelected(true);
    }
    box.addActionListener(new ActionListener() {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(final ActionEvent e) {
        JCheckBox checkbox = (JCheckBox) e.getSource();
        boolean boxState = checkbox.isSelected();
        boolean change = boxState != errorBarPolicy.isShowNegativeYErrors();
        if (change) {
          errorBarPolicy.setShowNegativeYErrors(boxState);
        }
      }
    });

    gbc.gridx = 3;
    this.add(box, gbc);

    // fill a dummy component that may be resized:
    gbc.gridx = 4;
    gbc.gridheight = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    this.add(Box.createHorizontalGlue(), gbc);
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    gbc.gridheight = 1;
    // Positive x error
    label = new JLabel("Positive X");
    label.setToolTipText("Show error bars in positive x direction.");
    gbc.gridx = 0;
    gbc.gridy++;
    this.add(label, gbc);
    box = new JCheckBox();
    if (errorBarPolicy.isShowPositiveXErrors()) {
      box.setSelected(true);
    }
    box.addActionListener(new ActionListener() {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(final ActionEvent e) {
        JCheckBox checkbox = (JCheckBox) e.getSource();
        boolean boxState = checkbox.isSelected();
        boolean change = boxState != errorBarPolicy.isShowPositiveXErrors();
        if (change) {
          errorBarPolicy.setShowPositiveXErrors(boxState);
        }
      }
    });

    gbc.gridx = 1;
    this.add(box, gbc);

    // Positive y error
    label = new JLabel("Positive Y");
    label.setToolTipText("Show error bars in positive y direction.");
    gbc.gridx = 2;
    this.add(label, gbc);
    box = new JCheckBox();
    if (errorBarPolicy.isShowPositiveYErrors()) {
      box.setSelected(true);
    }
    box.addActionListener(new ActionListener() {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(final ActionEvent e) {
        JCheckBox checkbox = (JCheckBox) e.getSource();
        boolean boxState = checkbox.isSelected();
        boolean change = boxState != errorBarPolicy.isShowPositiveYErrors();
        if (change) {
          errorBarPolicy.setShowPositiveYErrors(boxState);
        }
      }
    });

    gbc.gridx = 3;
    this.add(box, gbc);
  }

}
