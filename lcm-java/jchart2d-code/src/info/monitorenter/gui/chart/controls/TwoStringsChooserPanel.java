/*
 *  TwoStringsChooserPanel.java of project jchart2d, a panel that allows 
 *  to input two <code>Strings</code> via 
 *  normal javax.swing.JTextField instances. 
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
package info.monitorenter.gui.chart.controls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel that allows to input two <code>Strings</code> via 
 * normal 
 * <code>{@link javax.swing.JTextField}</code>. 
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.7 $
 */
public class TwoStringsChooserPanel extends JPanel {

  /** Input text field for the first value to configure. * */
  private JTextField m_value1;

  /** Input text field for the second value to configure. * */
  private JTextField m_value2;

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 5185791493833091309L;

  /**
   * Creates an instance with the given labels and corresponding 
   * text fields with the given initial values.
   * <p>
   * 
   * @param label1
   *          1 the label in front of the first input text field.
   * @param presetValue1
   *          the preset value for the first text field.
   * @param label2
   *          2 the label in front of the second input text field.
   * @param presetValue2
   *          the preset value for the second text field.
   */
  public TwoStringsChooserPanel(final String label1, final String presetValue1,
      final String label2, final String presetValue2) {

    super();

    // complex layout needed for ensuring that
    // both labels are displayed vertically stacked but
    // with the same distance to their text fields regardless
    // of their label width:
    this.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    JLabel jLabel1 = new JLabel(label1);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(2, 2, 2, 2);
    this.add(jLabel1, gbc);
    this.m_value1 = new JTextField(presetValue1, 20);
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx = 1;
    this.add(this.m_value1, gbc);
    JLabel jLabel2 = new JLabel(label2);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 1;
    this.add(jLabel2, gbc);
    this.m_value2 = new JTextField(presetValue2, 20);
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx = 1;
    this.add(this.m_value2, gbc);
  }

  /**
   * Returns the current selected string value 1.
   * <p>
   * 
   * @return the current selected string value 1.
   */
  public String getValue1() {
    return this.m_value1.getText();
  }

  /**
   * Returns the current selected string value 2.
   * <p>
   * 
   * @return the current selected string value 2.
   */
  public String getValue2() {
    return this.m_value2.getText();
  }
}
