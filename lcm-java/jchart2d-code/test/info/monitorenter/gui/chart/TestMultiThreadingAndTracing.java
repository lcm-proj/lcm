/*
 *  TestMultiThreadingAndTracing.java of project jchart2d - a Junit 
 *  Test that tests many concurrent Threads with their traces writing 
 *  to the same chart. 
 *  Copyright (C) Achim Westermann, created on 10.05.2005, 22:52:54
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

import info.monitorenter.gui.chart.traces.Trace2DLtd;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Multiple <code>Producers</code> concurrently add points to an amount of
 * randomly shared <code>ITrace2D</code> instances.
 * <p>
 * One <code>Consumer</code> invokes paint on the <code>Chart2D</code> thus
 * allowing to drop pending changes stored in it.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public class TestMultiThreadingAndTracing extends TestMultithreading {

  /**
   * Runnable that will take a random break between 0 and
   * <code>sleepRange</code> milliseconds and then consume added <code>
   * {@link TracePoint2D}</code> (by <code>{@link Producer}</code>) by invoking
   * <code>{@link Chart2D#paint(java.awt.Graphics)}</code>.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * 
   * @version $Revision: 1.11 $
   */
  class Consumer extends TestMultithreading.Consumer {

    /** The maximum of milliseconds between two add operations. */
    private long m_sleepRange;

    /** Flag to allow stopping this Thread at a defined consitant code position. */
    private boolean m_stop = false;

    /**
     * Creates an instance that will take a random break between 0 and
     * <code>sleepRange</code> milliseconds between two add operations.
     * <p>
     * 
     * @param sleepRange
     *          the maximum of milliseconds between two add operations.
     */
    Consumer(final long sleepRange) {
      super(sleepRange);
    }

    /**
     * @see info.monitorenter.gui.chart.TestMultithreading.Consumer#run()
     */
    @Override
    public void run() {
      MockGraphics2D mockGraphics = new MockGraphics2D();
      while (!(this.m_stop || TestMultiThreadingAndTracing.this.isAllProducersFinished())) {
        try {
          Thread.sleep((long) (Math.random() * this.m_sleepRange));
        } catch (InterruptedException e) {
          e.printStackTrace();
          this.m_stop = true;
        }
        System.out.println('[' + this.getClass().getName() + "] painting...");
        TestMultiThreadingAndTracing.this.m_chart.paint(mockGraphics);
      }
    }

  }

  /**
   * Producer implementation that sleeps a random range of milliseconds within
   * <code>{@link #PRODUCER_SLEEPRANGE}</code> and then adds a newly created
   * <code>{@link TracePoint2D}</code> to a random picked <code>{@link ITrace2D}
   * </code> for <code>{@link #PRODUCER_ADD_POINT_AMOUNT}</code> times.
   * <p>
   */
  class Producer extends TestMultithreading.Producer {
    /** The maximum of milliseconds between two add operations. */
    private long m_sleepRange;

    /** Flag to stop the producer. */
    private boolean m_stop = false;

    /** The amount to add. */
    private long m_toAdd;

    /**
     * <p>
     * Constructs a producer that will add <code>toAdd</code> points with random
     * breaks of milliseconds between <code>maxSleep</code> and zero.
     * </p>
     * 
     * @param toAdd
     *          the amount of points to add
     * @param sleepRange
     *          the maxium time in milliseconds the Thread will sleep between
     *          two points added
     */
    Producer(final long toAdd, final long sleepRange) {
      super(toAdd, sleepRange);
    }

    /**
     * @see info.monitorenter.gui.chart.TestMultithreading.Producer#run()
     */
    @Override
    public void run() {
      ITracePoint2D point;
      ITrace2D tmpTrace;
      while (this.m_toAdd > 0 && !this.m_stop) {
        try {
          Thread.sleep((long) (Math.random() * this.m_sleepRange));
        } catch (InterruptedException e) {
          e.printStackTrace();
          this.m_stop = true;
        }
        tmpTrace = TestMultiThreadingAndTracing.this.pickRandomTrace();
        if (this.m_toAdd % 10 == 0) {
          System.out.println('[' + this.getName() + "] adding point to " + tmpTrace.getName()
              + "... " + this.m_toAdd + " to go...");
        }
        point = new TracePoint2D(this.m_toAdd, this.m_toAdd);
        TestMultiThreadingAndTracing.this.m_weakMap.put(point, point.toString());
        tmpTrace.addPoint(point);
        this.m_toAdd--;
      }
    }
  }

  /** <code>{@link ITrace2D}</code> class to use instances of for the test. */
  protected static final Class< ? > TRACE_CLASS = Trace2DLtd.class;

  // configuration
  /** Amount of traces ( same as threads ) to concurrently paint on the chart. */
  protected static final int TRACES_AMOUNT = 10;

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestMultiThreadingAndTracing.class.getName());

    suite.addTest(new TestMultiThreadingAndTracing("testTrace2DLtd"));

    return suite;
  }

  /** The traces of the concurrent threads to test. */
  protected List<ITrace2D> m_traces;

  // ////////////////////////////
  // Helper methods
  // ////////////////////////////

  /**
   * Creates a test case with the given name.
   * <p>
   * 
   * @param testName
   *          the name of the test case.
   */
  public TestMultiThreadingAndTracing(final String testName) {
    super(testName);
  }

  /**
   * Picks a random trace of the internal list of traces.
   * <p>
   * 
   * @return a random trace of the internal list of traces.
   */
  ITrace2D pickRandomTrace() {
    int index = (int) Math.round(Math.random() * TestMultiThreadingAndTracing.TRACES_AMOUNT);
    return this.m_traces.get(index);
  }

  // ////////////////////////////
  // Worker classes
  // ////////////////////////////

  /**
   * Writes a report to <code>{@link System#out}</code>.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.TestMultithreading#report()
   */
  @Override
  void report() {
    long keys = this.m_weakMap.size();
    System.out.println("Points remaining in the weakMap: " + keys);
    System.out.println("System.runFinalization()... ");
    System.runFinalization();
    System.out.println("System.gc()... ");
    System.gc();
    keys = this.m_weakMap.size();
    System.out.println("Points remaining in the weakMap: " + keys);
    keys = 0;
    for (ITracePoint2D point : this.m_weakMap.keySet()) {
      keys++;
      System.out.println("Point " + point.toString() + " was not dropped.");
    }
    System.out.println("Points remaining in the weakMap: " + keys);
    Assert.assertFalse("There are " + keys
        + " TracePoint2D instances not deleted from the WeakHashMap.", keys > this.m_trace
        .getMaxSize());
  }

  /**
   * Creates producers, consumers, traces and other members for the test run.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.TestMultithreading#setUp()
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @Override
  public void setUp() throws Exception {
    this.m_trace = (ITrace2D) TestMultiThreadingAndTracing.TRACE_CLASS.newInstance();
    this.m_chart = new Chart2D();
    this.m_weakMap = new WeakHashMap<ITracePoint2D, String>();
    this.m_producers = new LinkedList<TestMultithreading.Producer>();
    for (int add = PRODUCER_AMOUNT; add > 0; add--) {
      this.m_producers.add(new Producer(PRODUCER_ADD_POINT_AMOUNT, PRODUCER_SLEEPRANGE));
    }

    this.m_traces = new LinkedList<ITrace2D>();
    ITrace2D tmpTrace;
    for (int add = TestMultiThreadingAndTracing.TRACES_AMOUNT; add > 0; add--) {
      tmpTrace = (ITrace2D) TestMultiThreadingAndTracing.TRACE_CLASS.newInstance();
      tmpTrace.setName("Trace-" + add);
      this.m_traces.add(tmpTrace);
    }

    Assert.assertTrue(this.m_chart.getTraces().size() == 0);
    // add all traces
    for (ITrace2D trace : this.m_traces) {
      this.m_chart.addTrace(trace);
    }
  }

  /**
   * Cleans up / frees handles.
   * <p>
   * 
   * @see info.monitorenter.gui.chart.TestMultithreading#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    this.m_traces = null;
  }

}
