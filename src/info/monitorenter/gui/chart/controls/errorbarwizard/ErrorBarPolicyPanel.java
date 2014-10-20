/*
 *  ErrorBarPolicyPanel.java of project jchart2d, a panel 
 *  for selection of an error bar policy. 
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

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A panel for selection and custom configuration of an
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.9 $
 */
public class ErrorBarPolicyPanel
    extends JPanel {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 5185411570993974756L;

  /**
   * Creates a panel that offers controls the given error bar policy.
   * <p>
   * 
   * @param errorBarPolicy
   *          the error bar policy to control.
   */
  public ErrorBarPolicyPanel(final IErrorBarPolicy<?> errorBarPolicy) {

    super();

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
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // TODO: add the custom config panel of the concrete
    // error bar policy here:
    JComponent customConfigControl = errorBarPolicy.getCustomConfigurator();
    if (customConfigControl != null) {
      this.add(customConfigControl, gbc);
    }

    // The checkboxes for dimension / direction:
    gbc.gridy++;
    gbc.gridx = 0;
    this.add(new ErrorBarDirectionPanel(errorBarPolicy), gbc);

    // The available painters:
    gbc.gridy++;
    this.add(new ErrorBarPaintersPanel(errorBarPolicy), gbc);

  }

}
