/*
 *  TestRingBufferArrayFast.java of project jchart2d, Junit 
 *  test for class RingBufferArrayFast. 
 *  Copyright (c) 2007 Achim Westermann, created on 06:29:12.
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
package info.monitorenter.util.collections;

import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Testcase for {@link info.monitorenter.util.collections.RingBufferArrayFast}.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.10 $
 */
public class TestRingBufferArrayFast
    extends TestCase {

  /**
   * Test suite for this test class.
   * <p>
   * 
   * @return the test suite
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();
    suite.setName(TestRingBufferArrayFast.class.getName());

    suite.addTest(new TestRingBufferArrayFast("testAdd"));
    suite.addTest(new TestRingBufferArrayFast("testIteratorF2L"));
    suite.addTest(new TestRingBufferArrayFast("testIteratorL2F"));
    suite.addTest(new TestRingBufferArrayFast("testSetBufferSize"));
    suite.addTest(new TestRingBufferArrayFast("testSize"));
    suite.addTest(new TestRingBufferArrayFast("testHashCode"));

    return suite;
  }

  /**
   * Creates a test case with the given name.
   * <p>
   * 
   * @param testName
   *            the name of the test case.
   */
  public TestRingBufferArrayFast(final String testName) {
    super(testName);
  }

  /**
   * Test method for {@link info.monitorenter.util.collections.RingBufferArrayFast#add(Object)}.
   * <p>
   */
  public void testAdd() {
    RingBufferArrayFast<Integer> ringBuffer = new RingBufferArrayFast<Integer>(1);
    System.out.println("Adding 1 element to a buffer of size 1.");
    ringBuffer.add(new Integer(0));
    Assert.assertEquals(1, ringBuffer.size());

  }

  /**
   * Test method for {@link info.monitorenter.util.collections.RingBufferArrayFast#iteratorF2L()}.
   * <p>
   */
  public void testIteratorF2L() {
    IRingBuffer<Integer> ringBuffer = new RingBufferArrayFast<Integer>(10);
    System.out.println("Adding 2 elements to a buffer of size 10");
    for (int i = 0; i < 2; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(2, ringBuffer.size());
    int value = 1;
    Iterator<Integer> it = ringBuffer.iteratorF2L();
    Integer removed;
    while (it.hasNext()) {
      removed = it.next();
      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value--;
    }

    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    value = 0;
    System.out.println("Adding 1 element to a buffer of size 10");
    for (int i = 0; i < 1; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(1, ringBuffer.size());
    it = ringBuffer.iteratorF2L();
    while (it.hasNext()) {
      removed = it.next();
      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value--;
    }

    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    value = 9;
    System.out.println("Adding 10 elements to a buffer of size 10");
    for (int i = 0; i < 10; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(10, ringBuffer.size());
    it = ringBuffer.iteratorF2L();
    while (it.hasNext()) {
      removed = it.next();

      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value--;
    }

    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    value = 11;
    System.out.println("Adding 12 elements to a buffer of size 10");
    for (int i = 0; i < 12; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(10, ringBuffer.size());
    it = ringBuffer.iteratorF2L();
    while (it.hasNext()) {
      removed = it.next();

      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value--;
    }

    System.out.println("Testing for side effects of hasNext()...");
    it = ringBuffer.iteratorF2L();
    for (int i = 0; i < 100; i++) {
      Assert.assertTrue(it.hasNext());
    }

    System.out.println("Testing hasNext() with iterator f2l on empty buffer...");
    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    Assert.assertTrue(ringBuffer.isEmpty());
    it = ringBuffer.iteratorF2L();
    Assert.assertFalse(it.hasNext());

    System.out.println("Testing hasNext() with iterator l2f on empty buffer...");
    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    Assert.assertTrue(ringBuffer.isEmpty());
    it = ringBuffer.iteratorL2F();
    Assert.assertFalse(it.hasNext());
  }

  /**
   * Test method for {@link info.monitorenter.util.collections.RingBufferArrayFast#iteratorL2F()}.
   * <p>
   */
  public void testIteratorL2F() {
    IRingBuffer<Integer> ringBuffer = new RingBufferArrayFast<Integer>(10);
    System.out.println("Adding 2 elements to a buffer of size 10");
    for (int i = 0; i < 2; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(2, ringBuffer.size());
    int value = 0;
    for (Integer removed : ringBuffer) {
      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value++;
    }

    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    value = 0;
    System.out.println("Adding 1 element to a buffer of size 10");
    for (int i = 0; i < 1; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(1, ringBuffer.size());
    for (Integer removed : ringBuffer) {
      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value++;
    }

    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    value = 0;
    System.out.println("Adding 10 elements to a buffer of size 10");
    for (int i = 0; i < 10; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(10, ringBuffer.size());
    for (Integer removed : ringBuffer) {
      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(removed.intValue(), value);
      value++;
    }

    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    value = 2;
    System.out.println("Adding 12 elements to a buffer of size 10");
    for (int i = 0; i < 12; i++) {
      ringBuffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(10, ringBuffer.size());
    for (Integer removed : ringBuffer) {
      Assert.assertNotNull("Element no. " + value + " is null.", removed);
      // tests the order of the iterator:
      Assert.assertEquals(value, removed.intValue());
      value++;
    }

    System.out.println("Testing for side effects of hasNext()...");
    Iterator<Integer> it = ringBuffer.iteratorL2F();
    for (int i = 0; i < 100; i++) {
      Assert.assertTrue(it.hasNext());
    }

    System.out.println("Testing hasNext() with iterator on empty buffer...");
    ringBuffer.clear();
    Assert.assertEquals(0, ringBuffer.size());
    Assert.assertTrue(ringBuffer.isEmpty());
    it = ringBuffer.iteratorL2F();
    Assert.assertFalse(it.hasNext());
  }

  /**
   * Test method for
   * {@link info.monitorenter.util.collections.RingBufferArrayFast#setBufferSize(int)}.
   * <p>
   */
  public void testSetBufferSize() {
    IRingBuffer<Integer> buffer = new RingBufferArrayFast<Integer>(3);
    Assert.assertEquals(3, buffer.getBufferSize());
    for (int i = 0; i < 3; i++) {
      buffer.add(Integer.valueOf(i));
    }
    Assert.assertEquals(3, buffer.size());
    System.out.println("before setting size from 3 to 4: " + buffer.toString());
    buffer.setBufferSize(4);
    System.out.println("after setting size from 3 to 4: " + buffer.toString());
    Assert.assertEquals(3, buffer.size());
    Assert.assertEquals(4, buffer.getBufferSize());

    buffer.setBufferSize(2);
    Assert.assertEquals(2, buffer.size());
    Assert.assertEquals(2, buffer.getBufferSize());
    Iterator<Integer> it = buffer.iteratorL2F();
    Assert.assertEquals(1, it.next().intValue());
    Assert.assertEquals(2, it.next().intValue());
    Assert.assertFalse(it.hasNext());
  }
  
  /**
   * Test method for
   * {@link info.monitorenter.util.collections.RingBufferArrayFast#hashCode()}.
   * <p>
   */
  public void testHashCode() {
    final IRingBuffer<Integer> buffer1 = new RingBufferArrayFast<Integer>(4);
    for (int i = 0; i < 2; i++) {
      buffer1.add(Integer.valueOf(i));
    }
    final IRingBuffer<Integer> buffer2 = buffer1;
    buffer1.add(Integer.valueOf(3));
    Assert.assertEquals("Hashcode is different.", buffer1.hashCode(), buffer2.hashCode());
  }

  /**
   * Test method for {@link info.monitorenter.util.collections.RingBufferArrayFast#size()}.
   * <p>
   */
  public void testSize() {

    RingBufferArrayFast<Integer> ringBuffer = new RingBufferArrayFast<Integer>(100);
    System.out
        .println("Adding 100 elements to a buffer with capacity of 100 with size assertions.");
    for (int i = 0; i < 100; i++) {
      Assert.assertEquals(i, ringBuffer.size());
      ringBuffer.add(Integer.valueOf(i));
    }
    System.out
        .println("Adding 10 elements to a full buffer with capacity of 100 with size assertions.");
    for (int i = 0; i < 10; i++) {
      ringBuffer.add(Integer.valueOf(i));
      Assert.assertEquals(100, ringBuffer.size());
    }

  }

}
