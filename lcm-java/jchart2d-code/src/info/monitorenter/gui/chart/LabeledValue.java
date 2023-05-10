/*
 *  LabeledValue.java, a c struct for sth.. 
 *  Copyright (C) 2004 - 2011 Achim Westermann.
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
package info.monitorenter.gui.chart;

/**
 * A double value along with it's label.
 * <p>
 * 
 * Very primitive class comparable to a c struct.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.7 $
 */
public class LabeledValue {

  /** The flag showing if this label is a major tick. */
  protected boolean m_isMajorTick = false;

  /** The label. */
  protected String m_label;

  /** The corresponding value scaled to a position between 0.0 and 1.0. */
  protected double m_value;

  /**
   * Internal defcon.
   * <p>
   */
  public LabeledValue() {
    // nop
  }

  /**
   * Creates an instance with the given valu and the label for it.
   * <p>
   * 
   * @param value
   *          the value of this label.
   * 
   * @param label
   *          the String representation of this label.
   */
  LabeledValue(final double value, final String label) {
    this.m_value = value;
    this.m_label = label;
  }

  /**
   * Returns the label String.
   * <p>
   * 
   * @return the label String.
   */
  public String getLabel() {
    return this.m_label;
  }

  /**
   * Returns the value of this label which is scaled to lie between 0.0 and 1.0.
   * <p>
   * 
   * @return the value of this label which is scaled to lie between 0.0 and 1.0.
   */
  public double getValue() {
    return this.m_value;
  }

  /**
   * Returns true if this label is a major tick, false else.
   * <p>
   * 
   * @return true if this label is a major tick, false else.
   * 
   * @see info.monitorenter.gui.chart.axis.AAxis#setMajorTickSpacing(double)
   * 
   * @see info.monitorenter.gui.chart.axis.AAxis#setMinorTickSpacing(double)
   */
  public boolean isMajorTick() {
    return this.m_isMajorTick;
  }

  /**
   * Sets the label String.
   * <p>
   * 
   * @param label
   *          the label String.
   */
  public void setLabel(final String label) {
    this.m_label = label;
  }

  /**
   * Set this label as a major tick.
   * <p>
   * 
   * @param isMajorTick
   *          the major tick state to set.
   * 
   * @see info.monitorenter.gui.chart.axis.AAxis#setMajorTickSpacing(double)
   * 
   * @see info.monitorenter.gui.chart.axis.AAxis#setMinorTickSpacing(double)
   */
  public void setMajorTick(final boolean isMajorTick) {
    this.m_isMajorTick = isMajorTick;
  }

  /**
   * Sets the value.
   * <p>
   * 
   * @param value
   *          The value to set.
   */
  public final void setValue(final double value) {
    this.m_value = value;
  }

  /**
   * Returns the concatenation of the label string, ':' and the value's String
   * representation.
   * <p>
   * 
   * @return the concatenation of the label string, ':' and the value's String
   *         representation.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return new StringBuffer().append(this.m_label).append(" : ").append(this.m_value).toString();
  }
}
