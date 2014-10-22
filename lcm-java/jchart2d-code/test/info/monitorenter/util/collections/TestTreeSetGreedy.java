/*
 *  TestTreeSetGreedy.java of project jchart2d - Junit test case 
 *  for class TreeSetGreedy. 
 *  Copyright (C) Achim Westermann, created on 16.05.2005, 18:58:11
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
package info.monitorenter.util.collections;

import info.monitorenter.gui.chart.ITrace2D;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Junit test case for class <code>{@link TreeSetGreedy}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public class TestTreeSetGreedy
    extends TestCase {

  /**
   * Helper class for adding as element to the tested
   * <code>{@link TreeSetGreedy}</code>.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
   * 
   * 
   * @version $Revision: 1.8 $
   */
  final class Element implements IComparableProperty {

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -615304987571740852L;
    
    /** The internal comparable. */
    private Number m_compare = new Integer(ITrace2D.ZINDEX_MAX);

    /**
     * Defcon.
     * <p>
     */
    public Element() {
      super();
    }

    /**
     * @see info.monitorenter.util.collections.IComparableProperty#getComparableProperty()
     */
    public Number getComparableProperty() {
      return this.m_compare;
    }

    /**
     * @see info.monitorenter.util.collections.IComparableProperty#setComparableProperty(java.lang.Number)
     */
    public void setComparableProperty(final Number n) {
      this.m_compare = n;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      StringBuffer ret = new StringBuffer(this.m_compare.toString());
      return ret.toString();
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
    suite.setName(TestTreeSetGreedy.class.getName());

    suite.addTest(new TestTreeSetGreedy("testAdd"));
    suite.addTest(new TestTreeSetGreedy("testAdd10RemoveCenter"));
    suite.addTest(new TestTreeSetGreedy("testAddEqual10"));
    suite.addTest(new TestTreeSetGreedy("testAddEqual2"));
    suite.addTest(new TestTreeSetGreedy("testAddEqual3"));
    suite.addTest(new TestTreeSetGreedy("testAddEqual5"));
    suite.addTest(new TestTreeSetGreedy("testAddIdentical2"));
    suite.addTest(new TestTreeSetGreedy("testAddRemoveEqual10"));
    suite.addTest(new TestTreeSetGreedy("testAddRemoveEqual2"));
    suite.addTest(new TestTreeSetGreedy("testAddRemoveEqual3"));
    suite.addTest(new TestTreeSetGreedy("testAddRemoveEqual5"));
    suite.addTest(new TestTreeSetGreedy("testMultiThreadingAddRemove"));

    return suite;
  }

  /** The instance to test. */
  protected TreeSetGreedy<IComparableProperty> m_test;

  /**
   * Creates an instance with the given name.
   * <p>
   * 
   * @param testName
   *          the name of the test case.
   */
  public TestTreeSetGreedy(final String testName) {
    super(testName);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.m_test = new TreeSetGreedy<IComparableProperty>();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    this.m_test = null;
  }

  /**
   * Add an element and ensure size is 1.
   * <p>
   * 
   */
  public void testAdd() {
    Assert.assertNotNull(this.m_test);

    this.m_test.add(new Element());
    Assert.assertEquals(1, this.m_test.size());
  }

  /**
   * Add 1,1,1,1 and remove one element that is in the center of the order
   * (assuming that they will get different numbers due to correct add
   * mechanism).
   * <p>
   * 
   */
  public void testAdd10RemoveCenter() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();
    IComparableProperty e4 = new Element();
    IComparableProperty e5 = new Element();
    IComparableProperty e6 = new Element();
    IComparableProperty e7 = new Element();
    IComparableProperty e8 = new Element();
    IComparableProperty e9 = new Element();
    IComparableProperty e10 = new Element();

    // add
    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);
    this.m_test.add(e4);
    this.m_test.add(e5);
    this.m_test.add(e6);
    this.m_test.add(e7);
    this.m_test.add(e8);
    this.m_test.add(e9);
    this.m_test.add(e10);

    // remove

    this.m_test.remove(e6);

    System.out.println("testAdd10RemoveCenter");
    System.out.println(this.m_test);

    Assert.assertEquals("Unexpected size: " + this.m_test, this.m_test.size(), 9);
  }

  /**
   * Add 1,1,1,1,1 and test, wether all numbers are different.
   * <p>
   * 
   */
  public void testAddEqual10() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();
    IComparableProperty e4 = new Element();
    IComparableProperty e5 = new Element();
    IComparableProperty e6 = new Element();
    IComparableProperty e7 = new Element();
    IComparableProperty e8 = new Element();
    IComparableProperty e9 = new Element();
    IComparableProperty e10 = new Element();

    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);
    this.m_test.add(e4);
    this.m_test.add(e5);
    this.m_test.add(e6);
    this.m_test.add(e7);
    this.m_test.add(e8);
    this.m_test.add(e9);
    this.m_test.add(e10);

    System.out.println("testAddEqual10");
    System.out.println(this.m_test);
    Assert.assertEquals(10, this.m_test.size());
  }

  /**
   * Add two distinct <code>{@link IComparableProperty}</code> elements with
   * equal comparable and ensure size is 2.
   * <p>
   * 
   */
  public void testAddEqual2() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();

    this.m_test.add(e1);
    this.m_test.add(e2);
    System.out.println("testAddEqual2");
    System.out.println(this.m_test);
    Assert.assertEquals(this.m_test.size(), 2);
  }

  /**
   * Add 1,1,1 and test, wether all numbers are different.
   * <p>
   */
  public void testAddEqual3() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();

    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);

    System.out.println("testAddEqual3");
    System.out.println(this.m_test);
    Assert.assertEquals(3, this.m_test.size());
  }

  /**
   * Add 1,1,1,1,1 and test, wether all numbers are different.
   * <p>
   * 
   */
  public void testAddEqual5() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();
    IComparableProperty e4 = new Element();
    IComparableProperty e5 = new Element();

    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);
    this.m_test.add(e4);
    this.m_test.add(e5);

    System.out.println("testAddEqual5");
    System.out.println(this.m_test);
    Assert.assertEquals(5, this.m_test.size());
  }

  /**
   * Add two identical elements and ensure that the 2nd operation fails.
   * <p>
   * 
   */
  public void testAddIdentical2() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();

    this.m_test.add(e1);
    boolean success = this.m_test.add(e1);
    System.out.println("testAddIdentical");
    System.out.println(this.m_test);
    Assert.assertFalse(success);

  }

  /**
   * Add 1,1,1,1,1,1,1,1,1,1, remove them and ensure that no elements remain.
   * <p>
   * 
   */
  public void testAddRemoveEqual10() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();
    IComparableProperty e4 = new Element();
    IComparableProperty e5 = new Element();
    IComparableProperty e6 = new Element();
    IComparableProperty e7 = new Element();
    IComparableProperty e8 = new Element();
    IComparableProperty e9 = new Element();
    IComparableProperty e10 = new Element();

    // add
    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);
    this.m_test.add(e4);
    this.m_test.add(e5);
    this.m_test.add(e6);
    this.m_test.add(e7);
    this.m_test.add(e8);
    this.m_test.add(e9);
    this.m_test.add(e10);

    // remove
    this.m_test.remove(e1);
    this.m_test.remove(e2);
    this.m_test.remove(e3);
    this.m_test.remove(e4);
    this.m_test.remove(e5);
    this.m_test.remove(e6);
    this.m_test.remove(e7);
    this.m_test.remove(e8);
    this.m_test.remove(e9);
    this.m_test.remove(e10);

    System.out.println("testAddRemoveEqual10");
    System.out.println(this.m_test);

    Assert.assertEquals("Unexpected remaining elements: " + this.m_test, this.m_test.size(), 0);
  }

  /**
   * Add two distinct <code>{@link IComparableProperty}</code> elements with
   * equal comparable, remove them and ensure size is 0.
   * <p>
   * 
   */
  public void testAddRemoveEqual2() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();

    // add
    this.m_test.add(e1);
    this.m_test.add(e2);

    // remove
    this.m_test.remove(e1);
    this.m_test.remove(e2);

    System.out.println("testAddRemoveEqual2");
    System.out.println(this.m_test);

    Assert.assertEquals("Unexpected remaining elements: " + this.m_test, this.m_test.size(), 0);
  }

  /**
   * Add 1,1,1, remove then and ensure that zero elements remain.
   * <p>
   * 
   */
  public void testAddRemoveEqual3() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();

    // add
    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);

    // remove
    this.m_test.remove(e1);
    this.m_test.remove(e2);
    this.m_test.remove(e3);

    System.out.println("testAddRemoveEqual3");
    System.out.println(this.m_test);

    Assert.assertEquals("Unexpected remaining elements: " + this.m_test, this.m_test.size(), 0);
  }

  /**
   * Add 1,1,1,1,1, remove them and ensure that no elements remain.
   * <p>
   * 
   */
  public void testAddRemoveEqual5() {
    Assert.assertNotNull(this.m_test);
    IComparableProperty e1 = new Element();
    IComparableProperty e2 = new Element();
    IComparableProperty e3 = new Element();
    IComparableProperty e4 = new Element();
    IComparableProperty e5 = new Element();

    // add
    this.m_test.add(e1);
    this.m_test.add(e2);
    this.m_test.add(e3);
    this.m_test.add(e4);
    this.m_test.add(e5);

    // remove
    this.m_test.remove(e1);
    this.m_test.remove(e2);
    this.m_test.remove(e3);
    this.m_test.remove(e4);
    this.m_test.remove(e5);

    System.out.println("testAddRemoveEqual5");
    System.out.println(this.m_test);

    Assert.assertEquals("Unexpected remaining elements: " + this.m_test, this.m_test.size(), 0);
  }

  /**
   * Starting 20 Threads that will remove and add inital equal elements (with an
   * internal comparableProperty of {@link ITrace2D#ZINDEX_MAX}) each 50 times
   * with arbitrary sleep times. Each Thread will ensure that it's own
   * {@link IComparableProperty} will be removed from the {@link TreeSetGreedy}
   * after removing it and assert this by calling the remove call a 2nd time and
   * looking for the returned boolean. The main Thread will wait until all
   * Threads have terminated.
   * 
   * 
   */
  public void testMultiThreadingAddRemove() {

    /**
     * Helper class that iteratively adds an element, asserts that the operation
     * was successful and then removes it again and asserts again that removing
     * was successful.
     * <p>
     * 
     * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
     * 
     * 
     * @version $Revision: 1.8 $
     */
    class TreeSetGreedyAddRemover
        extends Thread {

      /** Amount of instances to use concurrently. */
      public static final int INSTANCES = 30;

      /** Amount of maximum sleep time between add / remove cycles in ms. */
      protected static final int MAX_SLEEP = 2000;

      /** Element to add / remove. */
      private IComparableProperty m_element = new Element();

      /** Exception caught. */
      protected Exception m_failure = null;

      /** Amount of add / remove cycles per instance. */
      private final int m_iterations = 20;

      /** Flag to stop this Thread at a checked codepoint. */
      protected boolean m_stop = false;

      /**
       * Defcon.
       * <p>
       */
      public TreeSetGreedyAddRemover() {
        super();
      }

      /**
       * @see java.lang.Thread#run()
       */
      @Override
      public void run() {
        boolean success = false;
        long sleep;
        for (int i = 0; i < this.m_iterations && !this.m_stop; i++) {
          try {
            System.out.println(this.getName() + " adding: " + this.m_element);
            success = TestTreeSetGreedy.this.m_test.add(this.m_element);
            try {
              Assert.assertTrue("Add operation unsuccessful!", success);
            } catch (Exception fail) {
              fail.printStackTrace(System.err);
              this.m_failure = fail;
            }
            sleep = (long) (Math.random() * TreeSetGreedyAddRemover.MAX_SLEEP);
            System.out.println(this.getName() + " sleeping for : " + sleep + " ms.");
            Thread.sleep(sleep);
            System.out.println(this.getName() + " removing: " + this.m_element);
            success = TestTreeSetGreedy.this.m_test.remove(this.m_element);
            try {
              Assert.assertTrue("Remove operation unsuccessful!", success);
            } catch (Exception fail) {
              fail.printStackTrace(System.err);
              this.m_failure = fail;
            }
            success = TestTreeSetGreedy.this.m_test.remove(this.m_element);
            try {
              // avoid ConcurrentModificationException for toString() of test:
              Assert.assertFalse("A remove operation of my own removed element was successful! ", success);
            } catch (Exception fail) {
              fail.printStackTrace(System.err);
              this.m_failure = fail;
            }
          } catch (InterruptedException e) {
            Assert.fail("Caught an InterruptedExcetpion: " + e.toString());
          } catch (Exception fail) {
            fail.printStackTrace(System.err);
            this.m_failure = fail;
          }

        }
      }
    }

    TreeSetGreedyAddRemover[] threads = new TreeSetGreedyAddRemover[TreeSetGreedyAddRemover.INSTANCES];
    for (int i = 0; i < TreeSetGreedyAddRemover.INSTANCES; i++) {
      threads[i] = new TreeSetGreedyAddRemover();
    }

    for (int i = 0; i < TreeSetGreedyAddRemover.INSTANCES; i++) {
      threads[i].setDaemon(true);
    }
    for (int i = 0; i < TreeSetGreedyAddRemover.INSTANCES; i++) {
      threads[i].start();
    }

    boolean allFinished = false;
    while (!allFinished) {
      allFinished = true;
      for (int i = 0; i < TreeSetGreedyAddRemover.INSTANCES; i++) {
        if (threads[i].m_failure != null) {
          // clean output: stop threads, wait for them to finish and then fail.
          Exception failure = threads[i].m_failure;
          for (int j = 0; j < TreeSetGreedyAddRemover.INSTANCES; j++) {
            threads[j].m_stop = true;
          }
          try {
            Thread.sleep(TreeSetGreedyAddRemover.MAX_SLEEP + 1000);
          } catch (InterruptedException e1) {
            e1.printStackTrace();
          }
          StringWriter trace = new StringWriter();
          PrintWriter tracePrint = new PrintWriter(trace);

          failure.printStackTrace(tracePrint);
          tracePrint.flush();
          tracePrint.close();
          Assert.fail(trace.toString());
        }
        allFinished &= !threads[i].isAlive();
      }
      try {
        Thread.sleep(400);
      } catch (InterruptedException e) {
        Assert.fail("Caught an InterruptedExcetpion: " + e.toString());
      }
    }
    System.out.println("testMultiThreadingAddRemove() finished.");

  }
}
