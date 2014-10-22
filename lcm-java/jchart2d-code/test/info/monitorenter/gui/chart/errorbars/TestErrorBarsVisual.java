/*
 *  TestErrorBarsVisual.java of project jchart2d, <enterpurposehere>. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on Jan 3, 2011
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/test/info/monitorenter/gui/chart/errorbars/TestErrorBarsVisual.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.4 $
 */

package info.monitorenter.gui.chart.errorbars;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPolicy;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.pointpainters.PointPainterLine;
import info.monitorenter.gui.chart.test.ATestChartOperations;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.Color;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Visual tests for package
 * <code>{@link info.monitorenter.gui.chart.errorbars}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 **/
public class TestErrorBarsVisual extends ATestChartOperations {

  /**
   * Constructor with the test name.
   * <p>
   * 
   * @param arg0
   *          the name of the test.
   */
  public TestErrorBarsVisual(final String arg0) {
    super(arg0);

  }

  /**
   * Uses <code>{@link ErrorBarPolicyAbsoluteSummation}</code> and prompts for
   * visual judgment.
   * <p>
   */
  public void testErrorBarPolicyAbsoluteSummation() {
    ATestChartOperations.AChartOperation operation = new AChartOperation(

    "Sets ErrorBarPolicyAbsoluteSummation(0.00001, 10)") {
      /**
       * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#action(info.monitorenter.gui.chart.Chart2D)
       */
      public Object action(final Chart2D chart) {

        // Create error bar policy and assign it:
        IErrorBarPolicy< ? > errorBarPolicy = new ErrorBarPolicyAbsoluteSummation(0.00001, 10);// new_err);
        errorBarPolicy.setShowNegativeYErrors(true);
        errorBarPolicy.setShowPositiveYErrors(true);
        errorBarPolicy.setShowNegativeXErrors(true);
        errorBarPolicy.setShowPositiveXErrors(true);
        // configure how error bars are rendered with an error bar
        // painter:
        IErrorBarPainter errorBarPainter = new ErrorBarPainter();
        errorBarPainter.setEndPointPainter(new PointPainterDisc());
        // errorBarPainter.setStartPointPainter(new PointPainterDisc());
        errorBarPainter.setEndPointColor(Color.GREEN);
        errorBarPainter.setConnectionPainter(new PointPainterLine());
        errorBarPainter.setConnectionColor(Color.GREEN);
        // add the painter to the policy
        errorBarPolicy.setErrorBarPainter(errorBarPainter);
        // add the policy to the trace:
        chart.getTraces().first().setErrorBarPolicy(errorBarPolicy);
        return null;
      }

      /**
       * @see info.monitorenter.gui.chart.test.ATestChartOperations.AChartOperation#createChartInstance()
       */
      @Override
      public Chart2D createChartInstance() {

        Chart2D result = new Chart2D();
        return result;
      }

   

      @Override
      public ITrace2D[] createTraces() {
        ITrace2D trace = new Trace2DSimple();

        return new ITrace2D[] {trace };
      }
    };
    this.setTestOperation(operation);
  }

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestErrorBarsVisual.class.getName());
    suite.addTest(new TestErrorBarsVisual("testErrorBarPolicyAbsoluteSummation"));
    return suite;
  }

}
