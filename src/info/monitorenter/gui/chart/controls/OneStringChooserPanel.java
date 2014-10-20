/*
 *  OneStringChooserPanel.java of project jchart2d, a panel that allows 
 *  to input a <code>String</code> via 
 *  a normal javax.swing.JTextField instance. 
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
 * A panel that allows to input a <code>String</code> via 
 * a normal 
 * <code>{@link javax.swing.JTextField}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.4 $
 */
public class OneStringChooserPanel extends JPanel {

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 208040286473528841L;

  /** Input text field for the first value to configure. */
  private JTextField m_value;

  /**
   * Creates an instance with the given label and corresponding 
   * text field with the given initial value.
   * <p>
   * 
   * @param label
   *          the label in front of the input text field.
   *          
   * @param presetValue
   *          the preset value for the text field.
   */
  public OneStringChooserPanel(final String label, final String presetValue) {

    super();

    // Layout prepared for more than one widget...
    this.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    JLabel jLabel1 = new JLabel(label);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(2, 2, 2, 2);
    this.add(jLabel1, gbc);
    this.m_value = new JTextField(presetValue, 20);
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx = 1;
    this.add(this.m_value, gbc);
  }

  /**
   * Returns the current selected string value.
   * <p>
   * 
   * @return the current selected string value.
   */
  public String getValue() {
    return this.m_value.getText();
  }
}
