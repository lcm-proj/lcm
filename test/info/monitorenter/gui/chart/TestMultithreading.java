/*
 *  TestMultithreading.java  of project jchart2d - Tests jchart2d in 
 *  multithreading use. 
 *  Copyright (C) Achim Westermann, created on 10.05.2005, 21:33:24
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Junit test that tests jchart2d in multithreading mode: Several Threads write
 * data to a weak map - a single consumer periodically renders the data on a
 * <code>{@link ITrace2D}</code> the chart.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public class TestMultithreading extends TestCase {

  /**
   * Thread that invokes paint operations with a mock graphics context (thus
   * consumes pending unscaled points) interrupted by a sleep between 0 and a
   * configurable amount of milliseconds.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   */
  class Consumer extends Thread {

    /** The maximum sleep time between two paint operations. */
    private long m_sleepRange;

    /** Flag to allow termination from outside. */
    private boolean m_stop = false;

    /**
     * Creates an instance that mock-paints the chart every
     * <code>0 ..  sleeprange</code> ms.
     * <p>
     * 
     * @param sleepRange
     *          the maximum sleep range between two rendering operations.
     */
    Consumer(final long sleepRange) {
      this.m_sleepRange = sleepRange;
    }

    /**
     * Do the job.
     * <p>
     */
    @Override
    public void run() {
      MockGraphics2D mockGraphics = new MockGraphics2D();
      while (!(this.m_stop || TestMultithreading.this.isAllProducersFinished())) {
        try {
          Thread.sleep((long) (Math.random() * this.m_sleepRange));
        } catch (InterruptedException e) {
          e.printStackTrace();
          this.m_stop = true;
        }
        System.out.println('[' + this.getClass().getName() + "] painting...");
        TestMultithreading.this.m_chart.paint(mockGraphics);
      }
    }

  }

  /**
   * Thread implementation that adds random points to the trace of the outer
   * classes's chart and takes a random sleep time between 0 and a constructor
   * given value between two add operations.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   */
  class Producer extends Thread {
    /** The amount of points to add before termination. */
    private long m_toAdd;

    /** The maximum sleep time between two add operations. */
    private long m_sleepRange;

    /** Flag to allow stopping this Thread from outside. */
    private boolean m_stop = false;

    /**
     * <p>
     * Constructs a producer that will add <code>toAdd</code> points with random
     * breaks of milliseconds between <code>maxSleep</code> and zero.
     * </p>
     * 
     * @param toAdd
     *          the amount of points to add.
     * 
     * @param sleepRange
     *          the maxium time in milliseconds the Thread will sleep between
     *          two points added.
     * 
     */
    Producer(final long toAdd, final long sleepRange) {
      this.m_toAdd = toAdd;
      this.m_sleepRange = sleepRange;
    }

    /**
     * Does the job.
     * <p>
     */
    @Override
    public void run() {
      ITracePoint2D point;
      while (this.m_toAdd > 0 && !this.m_stop) {
        try {
          Thread.sleep((long) (Math.random() * this.m_sleepRange));
        } catch (InterruptedException e) {
          e.printStackTrace();
          this.m_stop = true;
        }
        if (this.m_toAdd % 10 == 0) {
          System.out.println('[' + this.getName() + "] adding point... " + this.m_toAdd
              + " to go...");
        }
        point = new TracePoint2D(this.m_toAdd, this.m_toAdd);
        TestMultithreading.this.m_weakMap.put(point, point.toString());
        TestMultithreading.this.m_trace.addPoint(point);
        this.m_toAdd--;
      }
    }
  }

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestMultithreading.class.getName());

    suite.addTest(new TestMultithreading("testTrace2DLtd"));
    return suite;
  }

  /** The chart used for testing. */
  protected Chart2D m_chart;

  /** The trace used for testing. */
  protected ITrace2D m_trace;

  /** Weak storage of points to render. */
  protected WeakHashMap<ITracePoint2D, String> m_weakMap;

  /** List of the producer Threads (produce points to render) for statistics. */
  protected List<Producer> m_producers;

  // test configuration
  /** Amount of producers of <code>{@link TracePoint2D}</code>. */
  protected static final int PRODUCER_AMOUNT = 10;

  /**
   * Range of milliseconds to pick a random sleep time out between producing two
   * <code>{@link TracePoint2D}</code> instances.
   */
  protected static final int PRODUCER_SLEEPRANGE = 100;

  /** Amount of <code>{@link TracePoint2D}</code> to create per producer. */
  protected static final int PRODUCER_ADD_POINT_AMOUNT = 500;

  /**
   * Range of milliseconds to pick a random sleep time out between consuming two
   * <code>{@link TracePoint2D}</code>.
   */
  protected static final int CONSUMER_SLEEPRANGE = 1000;

  /**
   * The <code>{@link ITrace2D}</code> class to use an instance of for the test.
   */
  private static final Class<Trace2DLtd> TRACE_CLASS = Trace2DLtd.class;

  /**
   * Default constructor.
   * <p>
   */
  public TestMultithreading() {
    super();
  }

  /**
   * Constructor with test name.
   * <p>
   * 
   * @param testName
   */
  public TestMultithreading(final String testName) {
    super(testName);
  }

  // ////////////////////////////
  // Test methods
  // ////////////////////////////

  // ////////////////////////////
  // Helper methods
  // ////////////////////////////
  /**
   * Returns true if all producer threads have finished their work.
   * <p>
   * 
   * @return true if all producer threads have finished their work.
   */
  protected boolean isAllProducersFinished() {
    boolean ret = true;
    Iterator<Producer> it = this.m_producers.iterator();
    Producer producer;
    while (it.hasNext()) {
      producer = it.next();
      if (!producer.isAlive()) {
        it.remove();
      } else {
        ret = false;
      }
    }
    return ret;
  }

  /**
   * Prints a report on <code>{@link System#out}</code>.
   * <p>
   */
  void report() {
    long keys = this.m_weakMap.size();
    System.out.println("Points remaining in the weakMap: " + keys);
    System.out.println("System.runFinalization()... ");
    System.runFinalization();
    System.out.println("System.gc()... ");
    System.gc();
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
   * Creates producers, consumers and initializes further members.
   * <p>
   * 
   * @see junit.framework.TestCase#setUp()
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.m_chart = new Chart2D();
    this.m_weakMap = new WeakHashMap<ITracePoint2D, String>();
    this.m_producers = new LinkedList<Producer>();
    for (int add = TestMultithreading.PRODUCER_AMOUNT; add > 0; add--) {
      this.m_producers.add(new Producer(TestMultithreading.PRODUCER_ADD_POINT_AMOUNT,
          TestMultithreading.PRODUCER_SLEEPRANGE));
    }
    this.m_trace = TestMultithreading.TRACE_CLASS.newInstance();
    this.m_chart.addTrace(this.m_trace);
  }

  /**
   * Start the Producer Threads and one Consumer Thread and blocks until all
   * Threads are finished to avoid that teardown will be called and further
   * tests are executed at the same time the calling test method has initiated
   * the Threads for it's test.
   * <p>
   * 
   */
  protected void startThreads() {
    for (Thread producer : this.m_producers) {
      producer.start();
    }
    Consumer consumer = new Consumer(TestMultithreading.CONSUMER_SLEEPRANGE);

    consumer.start();
    while (!this.isAllProducersFinished() || consumer.isAlive()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    this.report();
  }

  // ////////////////////////////
  // Worker classes
  // ////////////////////////////

  /**
   * @see junit.framework.TestCase#tearDown()
   * 
   * @throws Exception
   *           if something goes wrong.
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    this.m_weakMap = null;
    this.m_chart = null;
    this.m_producers = null;
  }

  /**
   * Tests the producer / consumer scenario.
   * <p>
   */
  public void testTrace2DLtd() {

    this.startThreads();
  }
}
