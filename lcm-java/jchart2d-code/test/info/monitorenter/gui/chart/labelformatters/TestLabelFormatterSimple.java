/*
 *  TestChartOperationsVisual.java of project jchart2d, test case 
 *  for visually testing operations of the jchart2d API.
 *  Copyright 2007 (C) Achim Westermann, created on 04.03.2007 11:12:01.
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
package info.monitorenter.gui.chart.labelformatters;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.test.ATestChartOperations;

/**
 * Visual test of operations upon the chart with basic ui for human judgement of
 * success or failure.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.3 $
 */
public class TestLabelFormatterSimple extends ATestChartOperations {

  /**
   * Constructor with the test name.
   * <p>
   * 
   * @param arg0
   *          the name of the test.
   */
  public TestLabelFormatterSimple(final String arg0) {
    super(arg0);
  }

  /**
   * Sets a new number format to a <code>{@link LabelFormatterSimple}</code> of
   * the y axis.
   * <p>
   */
  @org.junit.Test
  public void testLabelFormatterSimple() {
    ATestChartOperations.AChartOperation operation = new AChartOperation(
        "y-axis with simple formatter and a minimal milli range: enough ticks?") {

      /**
       * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#action(info.monitorenter.gui.chart.Chart2D)
       */
      public Object action(final Chart2D chart) {
        return null;
      }

      /**
       * @see info.monitorenter.gui.chart.test.ATestChartOperations.AChartOperation#fillTrace(info.monitorenter.gui.chart.ITrace2D)
       */
      @Override
      public void fillTrace(final ITrace2D trace) {
        trace.addPoint(47, 0.103759765625);
        trace.addPoint(48, 0.106201171875);
        trace.addPoint(48, 0.10986328125);
        trace.addPoint(49, 0.106201171875);
        trace.addPoint(49, 0.091552734375);
        trace.addPoint(50, 0.091552734375);
      }

      /**
       * @see info.monitorenter.gui.chart.test.ATestChartOperations.AChartOperation#preCondition(info.monitorenter.gui.chart.Chart2D)
       */
      @Override
      public void preCondition(final Chart2D chart) throws Exception {
        chart.getAxisY().setStartMajorTick(false);
        IAxisLabelFormatter formatter = new LabelFormatterSimple();

        IAxis<?> yAxis = chart.getAxisY();
        yAxis.setFormatter(formatter);
        // force the precondition visual change to have time show.
        // else the repaint could occur after the number format has been
        // set to the LabelFormatterNumber!
        Thread.sleep(1000);
      }

    };
    this.setTestOperation(operation);

  }
}
