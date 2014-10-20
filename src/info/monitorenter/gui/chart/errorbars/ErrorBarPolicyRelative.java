/*
 *  ErrorBarPolicyRelative.java of project jchart2d, configurable 
 *  info.monitorenter.gui.chart.IErrorBarPolicy that adds a 
 *  relative error to the points to render.
 *  Copyright (c) 2007 - 2011 Achim Westermann, created on 10.08.2006 19:37:54.
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
package info.monitorenter.gui.chart.errorbars;

import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Configurable <code>{@link info.monitorenter.gui.chart.IErrorBarPolicy}</code>
 * that adds a relative error (relative to the absolute values) to the points to
 * render.
 * <p>
 * 
 * You should not use this if you have a small value range but very high values:
 * The relative value will cause exteremely long lines (be much higher than the
 * value range to display) and fold the trace to a minimum line.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.23 $
 */
public class ErrorBarPolicyRelative extends AErrorBarPolicyConfigurable {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = 1031825382468141565L;

  /** The relative x error to render. */
  private double m_relativeXError = 0.02;

  /** The relative y error to render. */
  private double m_relativeYError = 0.02;

  /**
   * Creates an instance with the given relative errors.
   * <p>
   * 
   * The relative error is related to the absolut x and y values to render. It
   * has to be between 0.0 and 1.0.
   * <p>
   * 
   * @param relativeError
   *          the relative error value between 0.0 and 1.0 for x and y
   *          dimension.
   * 
   * @throws IllegalArgumentException
   *           if the argument is not between 0.0 and 1.0.
   * 
   * @see #ErrorBarPolicyRelative(double, double)
   */
  public ErrorBarPolicyRelative(final double relativeError) throws IllegalArgumentException {
    this(relativeError, relativeError);
  }

  /**
   * Creates an instance with the given relative errors.
   * <p>
   * 
   * The relative error is related to the absolut x and y values to render. It
   * has to be between 0.0 and 1.0.
   * <p>
   * 
   * @param relativeXError
   *          the relative x error value between 0.0 and 1.0.
   * 
   * @param relativeYError
   *          the relative y error value between 0.0 and 1.0.
   * 
   * @throws IllegalArgumentException
   *           if the argument is not between 0.0 and 1.0.
   */
  public ErrorBarPolicyRelative(final double relativeXError, final double relativeYError)
      throws IllegalArgumentException {
    this.setRelativeXError(relativeXError);
    this.setRelativeYError(relativeYError);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final ErrorBarPolicyRelative other = (ErrorBarPolicyRelative) obj;
    if (Double.doubleToLongBits(this.m_relativeXError) != Double
        .doubleToLongBits(other.m_relativeXError)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_relativeYError) != Double
        .doubleToLongBits(other.m_relativeYError)) {
      return false;
    }
    return true;
  }

  /**
   * @see info.monitorenter.gui.chart.errorbars.AErrorBarPolicyConfigurable#getCustomConfigurator()
   */
  @Override
  public JComponent getCustomConfigurator() {
    final JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEtchedBorder());

    panel.setLayout(new GridBagLayout());
    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(2, 2, 2, 2);

    final JLabel xErrorLable = new JLabel("Relative X error (%) ");

    panel.add(xErrorLable, gbc);

    final SpinnerModel numberXModel = new SpinnerNumberModel(ErrorBarPolicyRelative.this
        .getRelativeXError() * 100, 0, 100, 1);
    final JSpinner xErrorSelector = new JSpinner(numberXModel);

    gbc.gridx = 1;
    gbc.weightx = 0.5;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel.add(xErrorSelector, gbc);

    // fill a dummy component that may be resized:
    gbc.gridx = 2;
    gbc.gridheight = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    panel.add(Box.createHorizontalGlue(), gbc);
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;

    final JLabel yErrorLable = new JLabel("Relative Y error (%) ");

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridheight = 1;

    panel.add(yErrorLable, gbc);

    final SpinnerModel numberYModel = new SpinnerNumberModel(ErrorBarPolicyRelative.this
        .getRelativeYError() * 100, 0, 100, 1);
    final JSpinner yErrorSelector = new JSpinner(numberYModel);

    gbc.gridx = 1;
    gbc.weightx = 0.5;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    panel.add(yErrorSelector, gbc);

    // actions:
    xErrorSelector.addChangeListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        final JSpinner spinner = (JSpinner) e.getSource();
        final SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        final Number number = model.getNumber();
        ErrorBarPolicyRelative.this.setRelativeXError(number.doubleValue() / 100);
      }
    });

    yErrorSelector.addChangeListener(new ChangeListener() {
      public void stateChanged(final ChangeEvent e) {
        final JSpinner spinner = (JSpinner) e.getSource();
        final SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        final Number number = model.getNumber();
        ErrorBarPolicyRelative.this.setRelativeYError(number.doubleValue() / 100);
      }
    });

    return panel;
  }

  /**
   * Returns the relative x error between 0 and 1.
   * <p>
   * 
   * @return the relative x error.
   */
  public final double getRelativeXError() {
    return this.m_relativeXError;
  }

  /**
   * Returns the relative y error between 0 and 1.
   * <p>
   * 
   * @return the relative y Error.
   */
  public final double getRelativeYError() {
    return this.m_relativeYError;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#getXError(double)
   */
  public final double getXError(final double xValue) {
    return this.m_relativeXError * xValue;
  }

  /**
   * @see info.monitorenter.gui.chart.IErrorBarPolicy#getYError(double)
   */
  public final double getYError(final double yValue) {
    return this.m_relativeYError * yValue;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(this.m_relativeXError);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(this.m_relativeYError);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.errorbars.AErrorBarPolicyConfigurable#internalGetNegativeXError(int,
   *      int, info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected int internalGetNegativeXError(final int xPixel, final int yPixel,
      final ITracePoint2D original) {
    final double error = (xPixel - this.getTrace().getRenderer().getXChartStart())
        * this.m_relativeXError;
    final int result = (int) Math.round(xPixel - error);
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.errorbars.AErrorBarPolicyConfigurable#internalGetNegativeYError(int,
   *      int, info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected int internalGetNegativeYError(final int xPixel, final int yPixel,
      final ITracePoint2D original) {
    final int error = (int) Math.round((this.getTrace().getRenderer().getYChartStart() - yPixel)
        * this.m_relativeYError);
    return yPixel + error;

  }

  /**
   * @see info.monitorenter.gui.chart.errorbars.AErrorBarPolicyConfigurable#internalGetPositiveXError(int,
   *      int, info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected int internalGetPositiveXError(final int xPixel, final int yPixel,
      final ITracePoint2D original) {
    final double error = (xPixel - this.getTrace().getRenderer().getXChartStart())
        * this.m_relativeXError;
    final int result = (int) Math.round(xPixel + error);
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.errorbars.AErrorBarPolicyConfigurable#internalGetPositiveYError(int,
   *      int, info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  protected int internalGetPositiveYError(final int xPixel, final int yPixel,
      final ITracePoint2D original) {
    // y pixel are bigger the lower the value is, so inversion
    // is needed here:
    final int error = (int) Math.round((this.getTrace().getRenderer().getYChartStart() - yPixel)
        * this.m_relativeYError);
    return yPixel - error;
  }

  /**
   * Sets the relative X error to add to each error bar.
   * <p>
   * 
   * The relative error is related to the absolute x values to render. It has to
   * be between 0.0 and 1.0.
   * <p>
   * 
   * @param relativeXError
   *          a value between 0.0 and 1.0.
   * 
   * @throws IllegalArgumentException
   *           if the argument is not between 0.0 and 1.0.
   */
  public final void setRelativeXError(final double relativeXError) throws IllegalArgumentException {
    if ((relativeXError <= 0.0) || (relativeXError >= 1.0)) {
      throw new IllegalArgumentException("Given relative error (" + relativeXError
          + ")has to be between 0.0 and 1.0.");
    }
    final boolean change = this.m_relativeXError != relativeXError;
    if (change) {
      this.m_relativeXError = relativeXError;
      this.firePropertyChange(IErrorBarPolicy.PROPERTY_CONFIGURATION, null, null);
    }
  }

  /**
   * Sets the relative Y error to add to each error bar.
   * <p>
   * 
   * The relative error is related to the absolut y values to render. It has to
   * be between 0.0 and 1.0.
   * <p>
   * 
   * @param relativeYError
   *          a value between 0.0 and 1.0.
   * 
   * @throws IllegalArgumentException
   *           if the argument is not between 0.0 and 1.0.
   */
  public final void setRelativeYError(final double relativeYError) throws IllegalArgumentException {
    if ((relativeYError <= 0.0) || (relativeYError >= 1.0)) {
      throw new IllegalArgumentException("Given relative error (" + relativeYError
          + ")has to be between 0.0 and 1.0.");
    }
    final boolean change = this.m_relativeYError != relativeYError;
    if (change) {
      this.m_relativeYError = relativeYError;
      this.firePropertyChange(IErrorBarPolicy.PROPERTY_CONFIGURATION, null, null);
    }
  }
}
