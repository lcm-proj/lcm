/*
 *  ErrorBarPaintersPanel.java of project jchart2d, a panel 
 *  that displays all ErrorBarPainterConfigurable instances of an IErrorBarPolicy 
 *  and offers their edit and remove buttons as well as 
 *  an add button for a new IErrorBarPainter. 
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

import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.events.ErrorBarPainterActionEdit;
import info.monitorenter.gui.chart.events.ErrorBarPolicyActionAddPainter;
import info.monitorenter.gui.chart.events.ErrorBarPolicyActionRemovePainter;
import info.monitorenter.util.FileUtil;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that displays all
 * {@link info.monitorenter.gui.chart.errorbars.ErrorBarPainter} instances of an
 * {@link info.monitorenter.gui.chart.IErrorBarPolicy} and offers their edit and
 * remove buttons as well as an add button for a new
 * {@link info.monitorenter.gui.chart.IErrorBarPainter} instance to configure.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.11 $
 */
public class ErrorBarPaintersPanel
    extends JPanel implements PropertyChangeListener {

  /**
   * A panel that displays a single
   * {@link info.monitorenter.gui.chart.errorbars.ErrorBarPainter} instance of
   * an {@link info.monitorenter.gui.chart.IErrorBarPolicy} and offers their
   * edit and remove buttons as well as an add button for a new
   * {@link info.monitorenter.gui.chart.errorbars.ErrorBarPainter} instance to
   * configure.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.11 $
   */
  public class ErrorBarPainterConfigurablePanel
      extends JPanel implements PropertyChangeListener {

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = 1055140441129248409L;

    /** The error bar painter instance under control. */
    private IErrorBarPainter m_errorBarPainter;

    /**
     * Creates an instance that displays as a control (view & controller) to the
     * error bar painter instance.
     * <p>
     * 
     * @param errorBarPainter
     *            the error bar painter to control.
     * 
     * @param errorBarPolicy
     *            the parent error bar policy for the remove action.
     * 
     */
    public ErrorBarPainterConfigurablePanel(final IErrorBarPainter errorBarPainter,
        final IErrorBarPolicy<?> errorBarPolicy) {

      super();
      this.m_errorBarPainter = errorBarPainter;

      // layout
      this.setLayout(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.gridx = 0;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;
      gbc.gridy = 0;
      gbc.insets = new Insets(2, 2, 2, 2);

      String painterName;
      JButton editButton;

      // TODO: Instable to use file extension cut semantics for getting a
      // class name without package prefix.
      painterName = FileUtil.cutExtension(errorBarPainter.getClass().getName()).getValue();
      editButton = new JButton(new ErrorBarPainterActionEdit(errorBarPainter, "Edit", this));
      // layout:
      this.add(new JLabel(painterName), gbc);
      gbc.gridx = 1;
      this.add(editButton, gbc);
      gbc.gridx = 2;
      JButton removeButton;
      removeButton = new JButton(new ErrorBarPolicyActionRemovePainter(errorBarPolicy, "Remove",
          errorBarPainter));
      this.add(removeButton, gbc);
      gbc.gridx = 0;
      gbc.gridy++;

      // event handling: remove us from UI if removed from error bar policy:
      errorBarPolicy.addPropertyChangeListener(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER, this);
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent evt) {
      String property = evt.getPropertyName();
      if (IErrorBarPolicy.PROPERTY_ERRORBARPAINTER.equals(property)) {
        IErrorBarPolicy<?> errorBarPolicy = (IErrorBarPolicy<?>) evt.getSource();
        IErrorBarPainter oldValue = (IErrorBarPainter) evt.getOldValue();
        IErrorBarPainter newValue = (IErrorBarPainter) evt.getNewValue();
        if (oldValue == this.m_errorBarPainter) {
          if (newValue == null) {
            // store the parent for layout redo:
            Component parent = this.getParent();
            // remove
            ErrorBarPaintersPanel.this.remove(this);
            // also as a listener:
            errorBarPolicy.removePropertyChangeListener(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER,
                this);
            // invalidate / repaint parent: This has to be done at the level
            // of the error bar wizard level: we are a child of
            // errorbarpainterspanel ->
            // two steps
            if (parent != null) {
              parent = parent.getParent();
              if (parent != null) {
                parent.invalidate();
                parent.validate();
                parent.repaint(500);
              }
            }
          }
        }
      }
    }
  }

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 2293007395124251482L;

  /** The add painter button. */
  private JButton m_addButton;

  /**
   * Creates an instance that hooks as a control (view & controller) to the
   * error bar policy model instance.
   * <p>
   * 
   * @param errorBarPolicy
   *            the error bar policy to control directions of display of.
   */
  public ErrorBarPaintersPanel(final IErrorBarPolicy<?> errorBarPolicy) {

    super();

    this.setBorder(BorderFactory.createEtchedBorder());
    this.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.gridy = 0;
    gbc.insets = new Insets(2, 2, 2, 2);

    // adding a row for each IErrorBarPainter:
    Iterator<IErrorBarPainter> itErrorBarPainters = errorBarPolicy.getErrorBarPainters().iterator();
    IErrorBarPainter painter;

    while (itErrorBarPainters.hasNext()) {
      painter = itErrorBarPainters.next();
      this.add(new ErrorBarPainterConfigurablePanel(painter, errorBarPolicy), gbc);
      gbc.gridy++;
    }

    // add the add button
    this.m_addButton = new JButton(new ErrorBarPolicyActionAddPainter(errorBarPolicy, "Add"));
    this.add(this.m_addButton, gbc);
    // register for add operations to add a new painter row:
    errorBarPolicy.addPropertyChangeListener(IErrorBarPolicy.PROPERTY_ERRORBARPAINTER, this);
  }

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(final PropertyChangeEvent evt) {
    String property = evt.getPropertyName();
    if (IErrorBarPolicy.PROPERTY_ERRORBARPAINTER.equals(property)) {
      IErrorBarPainter oldPainter = (IErrorBarPainter) evt.getOldValue();
      IErrorBarPainter newPainter = (IErrorBarPainter) evt.getNewValue();
      if (oldPainter == null) {
        // painter was added, do a trick to add a new row and push down
        // the add button:
        IErrorBarPolicy<?> errorBarPolicy = (IErrorBarPolicy<?>) evt.getSource();
        GridBagLayout layout = (GridBagLayout) this.getLayout();
        GridBagConstraints gbc = layout.getConstraints(this.m_addButton);
        gbc.gridy++;
        layout.setConstraints(this.m_addButton, gbc);
        gbc.gridy--;
        this.add(new ErrorBarPainterConfigurablePanel(newPainter, errorBarPolicy), gbc);
        // find the parent of this component and invalidate:
        Component parent = this.getParent();
        if (parent != null) {
          parent.invalidate();
          parent.validate();
          parent.repaint(400);
        }
      }
    }

  }
}
