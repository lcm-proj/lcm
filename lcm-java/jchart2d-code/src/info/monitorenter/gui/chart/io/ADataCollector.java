/*
 * AbstractDataCollector.java jchart2d Copyright (C) 2004 - 2011 Achim Westermann, created
 * on 10.12.2004, 14:48:09 
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
 */
package info.monitorenter.gui.chart.io;

import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;


/**
 * A simple Runnable that continuously collects data every latency time period
 * and adds it to the internal ITrace2D instance.
 * <p>
 * Extend from this class and override the method {@link #collectData()}.
 * <p>
 * Set it up with code like:
 * 
 * <pre>
 *       Chart2D chart = new Chart2D();
 *       ITrace2D trace = &lt;initialization&gt;
 *       chart.addTrace(trace);
 *       // Put the chart in your UI...
 *       // ...
 *       AbstractDataCollector collector = new &lt;subtypename&gt;(200,trace);
 *       collector.start();
 * </pre>
 * <p>
 * 
 * <h3>Caution</h3>
 * Calling <code>new Thread(collector).start()</code> is disallowed and will
 * throw an exception as it would allow several Threads to run a collector. Use
 * the {@link #start()} instead.
 * <p>
 * <b>Always connect the trace to a chart first before starting the collector for that trace!</b>
 * (deadlock prevention will raise an exception else).
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * @version $Revision: 1.11 $
 */
public abstract class ADataCollector implements Runnable {

  /** Flag to check wether the collector is running. */
  private boolean m_isRunning = false;

  /** The interval for data collection of a single point. */
  private long m_latency = 400;

  /** This flag controls stopping / starting the thread that is used. */
  private boolean m_stop = true;

  /**
   * The thread that is created in {@link #start()}.
   * <p>
   * If someone tries to: <code>new Thread(collector).start()</code> instead
   * of <code>collector.start()</code> an exception will be thrown.
   */
  private Thread m_thread;

  /** The trace to add collected data to. */
  private ITrace2D m_trace;

  /**
   * Creates an instance that will collect every latency ms a point and add it
   * to the trace.
   * <p>
   * 
   * @param trace
   *          the trace to add collected points to.
   * @param latency
   *          the interval in ms for collecting points.
   */
  public ADataCollector(final ITrace2D trace, final long latency) {
    super();
    this.m_latency = latency;
    this.m_trace = trace;
  }

  /**
   * <p>
   * Override this method. It will be invoked in intervals of the configured
   * latency time. The TracePoint2D that is returned will be added to the
   * constructor given ITrace2D.
   * </p>
   * <p>
   * Keep your implementation fast. If the computations performed here take
   * longer than the latency time that desired refresh rate will not be reached.
   * </p>
   * 
   * @return the collected point.
   */
  public abstract ITracePoint2D collectData();

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.stop();
  }

  /**
   * Returns the interval in ms a point is collected.
   * <p>
   * 
   * @return the interval in ms a point is collected.
   */
  public long getLatency() {
    return this.m_latency;
  }

  /**
   * Returns the trace that is filled by this collector.
   * <p>
   * 
   * @return Returns the trace.
   */
  public ITrace2D getTrace() {
    return this.m_trace;
  }

  /**
   * Returns true if this datacollector currently is running.
   * <p>
   * 
   * @return true if this datacollector currently is running.
   */
  public boolean isRunning() {
    return this.m_isRunning;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run() {
    if (Thread.currentThread() != this.m_thread) {
      throw new IllegalStateException(
          "You cannot start an own thread for data collectors. Use collector.start()!");
    }
    this.m_isRunning = true;
    long lasttime;
    this.m_stop = false;
    ITracePoint2D point;
    while (!this.m_stop) {
      lasttime = System.currentTimeMillis();
      point = this.collectData();
      this.m_trace.addPoint(point);
      try {
        Thread.sleep(Math.max(this.m_latency - System.currentTimeMillis() + lasttime, 0));
      } catch (InterruptedException e) {
        this.stop();
      }
      if (Thread.interrupted()) {
        this.stop();
      }
    }
    this.m_isRunning = false;
  }

  /**
   * Sets the interval for collecting points in ms.
   * <p>
   * 
   * @param latency
   *          the interval for collecting points in ms.
   */
  public void setLatency(final long latency) {
    this.m_latency = latency;
  }

  /**
   * <p>
   * Starts a Thread using this {@link Runnable}.
   * </p>
   * <p>
   * This method will not start a new Thread if the current one is still
   * running. If you prefer to use your own Threads (e.g. from a ThreadPool)
   * prefer:
   * 
   * <pre>
   *       AbstractDataCollector collector = new &lt;subtypename&gt;(200,trace);
   *       new Thread(collector).start();
   * </pre>
   * 
   * or more abstract (as proposed for Thread improvement reasons:
   * 
   * <pre>
   *       AbstractDataCollector collector = new &lt;subtypename&gt;(200,trace);
   *       &lt;getSomeThreadInstance&gt;(collector).start();
   * </pre>
   * 
   * </p>
   */
  public void start() {
    if (this.m_stop) {
      this.m_thread = new Thread(this);
      this.m_thread.start();
    }
  }

  /**
   * Stops this Thread. Data collection will end when finished the current loop.
   * <p>
   * Note that your application may
   * <ol>
   * <li>run into deadlocks (blocking IO,...)
   * <li>face memory problems
   * </ol>
   * if the AbstractDataCollector implementation fetches and removes data from
   * 1) a limited buffer or a 2) unlimited buffer. This behaviour will of course
   * not appear if the data is not read from a queue where it has to be removed
   * from.
   * <p>
   */
  public void stop() {
    this.m_stop = true;
  }

}
