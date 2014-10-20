/*
 *  ATestChartOperations.java of project jchart2d, 
 *  base class for visual method invocation test for a
 *  info.monitorenter.gui.chart.Chart2D.
 *  Copyright 2007 (C) Achim Westermann, created on 25.02.2007 18:59:08.
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
package info.monitorenter.gui.chart.test;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.dialogs.ModalDialog;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.reflect.Method;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Base class for visual method invocation test for a
 * <code>{@link info.monitorenter.gui.chart.Chart2D}</code>.
 * <p>
 * 
 * Test methods should only contain the code to set the test operation they want
 * to perform (via
 * <code>{@link #setTestOperation(ATestChartOperations.IChart2DOperation)}</code>
 * ).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * 
 * @version $Revision: 1.15 $
 */
public abstract class ATestChartOperations extends TestCase {

  /**
   * Base class for chart operations that manages the name property.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * 
   * 
   * @version $Revision: 1.15 $
   */
  public abstract class AChartOperation implements ATestChartOperations.IChart2DOperation {

    /** The name of this operation. */
    private String m_name;

    /**
     * Constructor with the operation's name.
     * <p>
     * 
     * @param name
     *          the name of this operation.
     */
    public AChartOperation(final String name) {
      this.m_name = name;
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#createChartInstance()
     */
    public Chart2D createChartInstance() {

      return new Chart2D();
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#createTraces()
     */
    public ITrace2D[] createTraces() {
      ITrace2D result = new Trace2DSimple();
      return new ITrace2D[] {result };
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#fillTrace(info.monitorenter.gui.chart.ITrace2D)
     */
    public void fillTrace(final ITrace2D trace) {
      for (int i = 0; i < 101; i++) {
        trace.addPoint(i, i * (1 + Math.random()));
      }

    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#getName()
     */
    public final String getName() {
      return this.m_name;
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#postCondition(info.monitorenter.gui.chart.Chart2D,
     *      java.lang.Object)
     */
    public void postCondition(final Chart2D chart, final Object result) throws Exception {
      // nop
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#preCondition(info.monitorenter.gui.chart.Chart2D)
     */
    public void preCondition(final Chart2D chart) throws Exception {
      // nop
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return this.getName();
    }
  }

  /**
   * A chart operation implementation that is based upon reflection.
   * <p>
   * 
   * The advantage is that it is generic as it may be used for many different
   * operations upon a chart. The disadvantages are that it may only perform a
   * single operation (e.g. no subsequent calls for doing sth. on axis or
   * traces), costs more time and will not show complilation errors upon
   * signature changes but only throw exceptions if the desired method is not
   * found any more.
   * <p>
   * 
   * Prefer other implementations for more complex operations to test than a
   * single call upon the chart.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * 
   * 
   * @version $Revision: 1.15 $
   */
  public abstract class AChartOperationReflectionBased implements
      ATestChartOperations.IChart2DOperation {

    /** The operation to test. */
    private Method m_operation;

    /** The arguments for the operation to test. */
    private Object[] m_arguments;

    /**
     * Creates an operation that will invoke the method of class {@link Chart2D}
     * specified by the name and the arguments.
     * <p>
     * 
     * @param methodName
     *          the name of the method to invoke.
     * 
     * @param arguments
     *          the arguments for the method to invoke.
     * 
     * @throws SecurityException
     *           if the method is not accessible.
     * 
     * @throws NoSuchMethodException
     *           if the method does not exist.
     */
    public AChartOperationReflectionBased(final String methodName, final Object[] arguments)
        throws SecurityException, NoSuchMethodException {
      this.m_operation = Chart2D.class.getMethod(methodName, this.toTypeArray(arguments));
      this.m_arguments = arguments;
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#action(info.monitorenter.gui.chart.Chart2D)
     */
    public Object action(final Chart2D chart) throws Exception {
      Object result = this.m_operation.invoke(chart, this.m_arguments);
      return result;
    }

    /**
     * 
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#createChartInstance()
     */
    public Chart2D createChartInstance() {

      return new Chart2D();
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#createTraces()
     */
    public ITrace2D[] createTraces() {
      ITrace2D result = new Trace2DSimple();
      long timeOffset = System.currentTimeMillis();
      for (int i = 0; i < 101; i++) {
        result.addPoint(timeOffset + i, i);
      }
      return new ITrace2D[] {result };
    }

    /**
     * @see info.monitorenter.gui.chart.test.ATestChartOperations.IChart2DOperation#getName()
     */
    public String getName() {
      return this.m_operation.getName();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return this.getName();
    }

    /**
     * Internal helper for method lookup.
     * <p>
     * 
     * @param args
     *          the argument instances for the method to invoke.
     * 
     * @return the argument types for the method to invoke.
     */
    private final Class< ? >[] toTypeArray(final Object[] args) {
      Class< ? >[] result = new Class< ? >[args.length];
      for (int i = 0; i < args.length; i++) {
        result[i] = args[i].getClass();
      }
      return result;
    }

  }

  /**
   * Encapsulation of an action to perform upon the chart to test.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * 
   * @version $Revision: 1.15 $
   */
  public interface IChart2DOperation {
    /**
     * Perform the method on the chart to test.
     * <p>
     * 
     * @param chart
     *          the chart to test the action upon.
     * 
     * @return null or a result of the operation.
     * 
     * @throws Exception
     *           if sth. goes wrong.
     */
    public Object action(final Chart2D chart) throws Exception;

    /**
     * Simply create the proper <code>{@link Chart2D}</code> instance.
     * <p>
     * This allows to test subclasses (e.g.
     * <code>{@link info.monitorenter.gui.chart.ZoomableChart}</code>) too.
     * <p>
     * 
     * @return the chart instance to test.
     */
    public Chart2D createChartInstance();

    /**
     * Create the traces here.
     * <p>
     * 
     * @return the traces to test.
     */
    public ITrace2D[] createTraces();

    /**
     * Fill the given trace with data points.
     * <p>
     * 
     * @param trace
     *          the trace to fill with data points.
     */
    public void fillTrace(ITrace2D trace);

    /**
     * Returns the name of the action to perform.
     * <p>
     * 
     * @return the name of the action to perform.
     */
    public String getName();

    /**
     * Check any pre condition for the operation to test and throw an exception
     * to signal a fail.
     * <p>
     * 
     * @param chart
     *          the chart the operation was tested on.
     * 
     * @param result
     *          the result of the operation or null if there is none.
     * 
     * @throws Exception
     *           if the postCondition was not fulfilled.
     */
    public void postCondition(final Chart2D chart, Object result) throws Exception;

    /**
     * Check any pre condition for the operation to test and throw an exception
     * to signal a fail.
     * <p>
     * 
     * @param chart
     *          the chart to invoke the operation on.
     * 
     * @throws Exception
     *           if the preCondition is not fulfilled.
     */
    public void preCondition(final Chart2D chart) throws Exception;
  }

  /** The operation to test for the current test method. */
  private IChart2DOperation m_testOperation;

  /**
   * Constructor with the test name.
   * <p>
   * 
   * @param arg0
   *          the name of the test.
   */
  public ATestChartOperations(final String arg0) {
    super(arg0);
  }

  /**
   * Test methods should only contain the code to set the test operation they
   * want to perform.
   * <p>
   * This operation will be invoked in the <code>{@link #tearDown()} </code>
   * implementation.
   * <p>
   * 
   * @param testOperation
   *          the operation to test for the current test method.
   */
  protected void setTestOperation(final ATestChartOperations.IChart2DOperation testOperation) {
    this.m_testOperation = testOperation;
  }

  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }


  /**
   * @see info.monitorenter.gui.chart.test.ATestJChart2D#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    Assert.assertNotNull(
        "Test method has to invoke setTestOperation(ATestChartOperations.IChart2DOperation)",
        this.m_testOperation);

    // this.m_axisX = this.createAxisX();
    // this.m_axisY = this.createAxisY();
    ITrace2D[] traces = this.m_testOperation.createTraces();

    Chart2D chart = this.m_testOperation.createChartInstance();
     for (int i = 0; i < traces.length; i++) {
     chart.addTrace(traces[i]);
     this.m_testOperation.fillTrace(traces[i]);
     }
    // Assert.assertNotSame(this.m_axisX, this.m_axisY);

    JFrame frame = new JFrame();
    frame.getContentPane().add(chart);
    // this.m_frame.add(new ChartPanel(this.m_chart));
    frame.setSize(400, 600);
    frame.setLocation(new Point(200, 200));
    frame.setVisible(true);

    Thread.sleep(1000);

    this.m_testOperation.preCondition(chart);

    // Modal dialog for announcing the test:
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setText(this.m_testOperation.getName());
    ModalDialog dialog = new ModalDialog(frame, "Operation to test...", textArea);
    dialog.setSize(new Dimension(400, 100));
    dialog.showDialog();
    boolean failure = false;
    Object result;
    if (dialog.isOk()) {
      result = this.m_testOperation.action(chart);
      // let tester take a look:
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ie) {
        // nop
      }
      this.m_testOperation.postCondition(chart, result);
      // Modal dialog after the test for human judgment of success:
      textArea.setText("Does the result look ok?");
      dialog.setTitle("Judge operation " + this.m_testOperation.getName());
      dialog.showDialog();
      if (!dialog.isOk()) {
        failure = true;
      }
    }
    frame.setVisible(false);
    frame.dispose();

    super.tearDown();
    if (failure) {
      Assert
          .fail("Operation test " + this.m_testOperation.getName() + " was judged as a failure. ");
    }
    this.m_testOperation = null;
    super.tearDown();
  }

}
