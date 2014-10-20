/*
 *  AxisScalePolicyAutomaticBestFit.java of project jchart2d, <enterpurposehere>. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on Apr 22, 2011
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/codetemplates.xml,v $
 * Date   : $Date: 2009/02/24 16:45:41 $
 * Version: $Revision: 1.2 $
 */

package info.monitorenter.gui.chart.axis.scalepolicy;

import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.LabeledValue;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.util.Range;

import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

/**
 * Scale policy implementation that ensures the following:
 * <ul>
 * <li>No label will overwrite the following label.</li>
 * <li>No two labels will have the same value.</li>
 * <li>Every tick will exactly show the value without rounding errors.</li>
 * <li>Always the closest next possible tick is chosen regardless whether it is
 * a major tick or a minor tick (subject to change in favor of major ticks)</li>
 * </ul>
 * <p>
 * 
 * While this strategy is quite comfortable and prevents visual oddities there
 * are some consequences to it:
 * 
 * <ul>
 * <li>Major ticks are not guaranteed to be shown. This is because a label of a
 * minor tick label may need so much space that the following major tick has to
 * be skipped (subject to change)</li>
 * <li>Detailed control is not easy. E.g. if you want to enforce more ticks to
 * show up you could:
 * <ul>
 * <li>Set an {@link LabelFormatterNumber} via
 * {@link IAxis#setFormatter(IAxisLabelFormatter)} that formats little to no
 * digits. But this could have both effects: More labels as the labels take less
 * space or less labels as the value range is so little that an increased
 * formatted value is possible only little times within that range.</li>
 * <li>Choose major and minor ticks via
 * {@link IAxis#setMinorTickSpacing(double)} and
 * {@link IAxis#setMajorTickSpacing(double)}</li>
 * </ul>
 * </li>
 * <li>Performance is not the best. This is because the space for a label has to
 * be computed and pixels have to be transformed from and to the value domain.</li>
 * </ul>
 * <p>
 * 
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */

public class AxisScalePolicyManualTicks implements IAxisScalePolicy {

  /**
   * Just a helper to create a labeled value instance.
   * <p>
   * 
   * @param value
   *          the value use.
   * 
   * @param majorTickSpacing
   *          if value % majorTickSpacing is zero then the resulting <code>
   *          {@link LabeledValue}</code> will be judged as a major tick.
   * 
   * @param axis
   *          needed to re-parse the value into the label string by using the
   *          formatter of it.
   * 
   * @return the value rounded to minor or major ticks.
   */
  protected LabeledValue createLabeledValue(final double value, final double majorTickSpacing,
      final IAxis<?> axis) {
    final LabeledValue ret = new LabeledValue();
    ret.setValue(value);
    if (value % majorTickSpacing == 0) {
      ret.setMajorTick(true);
    } else {
      ret.setMajorTick(false);
    }

    // format label string.
    ret.setLabel(axis.getFormatter().format(ret.getValue()));
    // as formatting rounds too, reparse value so that it is exactly at the
    // point the label string describes.
    ret.setValue(axis.getFormatter().parse(ret.getLabel()).doubleValue());
    return ret;
  }

  /**
   * Returns the labels for this axis.
   * <p>
   * 
   * 
   * @return the labels for the axis.
   */
  protected List<LabeledValue> getLabels(final IAxis<?> axis) {
    final List<LabeledValue> collect = new LinkedList<LabeledValue>();
    double minorTickSpacing = axis.getMinorTickSpacing();
    double majorTickSpacing = axis.getMajorTickSpacing();

    if (minorTickSpacing > 0) {

      final Range domain = axis.getRange();
      final double min = domain.getMin();
      final double max = domain.getMax();
      String oldLabelName = "";
      LabeledValue label;
      final double range = max - min;
      double value;
      if (axis.isStartMajorTick()) {
        value = ((int) (min / majorTickSpacing)) * (majorTickSpacing);
        if (value < min) {
          value += majorTickSpacing;
        }
      } else {
        value = ((int) (min / minorTickSpacing)) * (minorTickSpacing);
      }
      String labelName = "start";
      int loopStop = 0;
      // first tick, manual init
      while ((value <= max) && (loopStop < 100)) {
        if (loopStop == 99) {
          if (AAxis.DEBUG) {
            System.out.println(axis.getAccessor().toString() + " axis: loop to high");
          }
        }
        if (oldLabelName.equals(labelName)) {
          if (AAxis.DEBUG) {
            System.out.println("constant Label " + labelName);
          }
        }
        label = this.createLabeledValue(value, majorTickSpacing, axis);

        oldLabelName = labelName;
        labelName = label.getLabel();
        value = label.getValue();

        loopStop++;
        if ((value <= max) && (value >= min)) {
          collect.add(label);
        } else if (value > max) {
          if (AAxis.DEBUG) {
            System.out.println("Dropping label (too high) : (" + label + ")[max: " + max + "]");
          }
        } else if (value < min) {
          if (AAxis.DEBUG) {
            System.out.println("Dropping label (too low) : (" + label + ")[min: " + min + "]");
          }
        }
        value += minorTickSpacing;
      }
      final int stop = collect.size();

      for (int i = 0; i < stop; i++) {
        label = collect.get(i);
        label.setValue((label.getValue() - min) / range);
      }
    }
    return collect;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisScalePolicy#getScaleValues(java.awt.Graphics,
   *      info.monitorenter.gui.chart.IAxis)
   */
  public List<LabeledValue> getScaleValues(final Graphics g2d, final IAxis<?> axis) {
    return this.getLabels(axis);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisScalePolicy#initPaintIteration(info.monitorenter.gui.chart.IAxis)
   */
  public void initPaintIteration(IAxis<?> axis) {
    // nop
  }

}
