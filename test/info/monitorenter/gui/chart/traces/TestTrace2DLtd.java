/*
 *  TestTrace2DLtd.java, a Junit test case for Trace2DLtd.
 *  Copyright (C) Achim Westermann, created on 23.04.2005, 08:21:12
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
package info.monitorenter.gui.chart.traces;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.TracePoint2D;

import java.util.Map;
import java.util.WeakHashMap;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A Junit test case for <code>{@link Trace2DLtd}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 */
public class TestTrace2DLtd extends TestCase {
	/**
	 * Test suite for this test class.
	 * <p>
	 * 
	 * @return the test suite.
	 */
	public static Test suite() {

		TestSuite suite = new TestSuite();
		suite.setName(TestTrace2DLtd.class.getName());

		suite.addTest(new TestTrace2DLtd("testMemoryLeak"));

		return suite;
	}

	/**
	 * Creates a test case with the given name.
	 * <p>
	 * 
	 * @param testName
	 *            the name of the test case.
	 */
	public TestTrace2DLtd(final String testName) {
		super(testName);
	}

	/**
	 * Adds 1000000 <code>{@link TracePoint2D}</code> instances to a
	 * <code>{@link Trace2DLtd}</code> and asserts that not more points than
	 * <code>{@link Trace2DLtd#getMaxSize()} </code> are remaining in memory.
	 * <p>
	 * 
	 */
	public void testMemoryLeak() {
		Chart2D dummyChart = new Chart2D();
		int traceSize = 10;
		ITrace2D trace = new Trace2DLtd(traceSize);
		dummyChart.addTrace(trace);
		long max = 1000000;
		long percentModulo = max / 20;
		System.out.println("Adding " + max
				+ " points to a Trace2DLtd and a WeakHashMap...");
		ITracePoint2D point;
		Map<ITracePoint2D, String> weakMap = new WeakHashMap<ITracePoint2D, String>();
		for (long i = 0; i < max; i++) {
			point = new TracePoint2D(i, i);
			trace.addPoint(point);
			weakMap.put(point, point.toString());
			if (i % percentModulo == 0) {
				System.out.println((i * 100 / max) + " %");
			}
		}
		System.out.println("Dropping point reference (but holding trace)...");
		point = null;
		// trace=null;
		long keys = weakMap.size();
		System.out.println("Points remaining in the weakMap: " + keys);
		System.out.println("System.runFinalization()... ");
		System.runFinalization();
		System.out.println("System.gc()... ");
		System.gc();
		keys = 0;
		for (ITracePoint2D pt : weakMap.keySet()) {
			keys++;
			System.out.println("Point " + pt.toString() + " was not dropped.");
		}
		System.out.println("Points remaining in the weakMap: " + keys);
		Assert.assertFalse("There are " + keys
				+ " TracePoint2D instances not deleted from the WeakHashMap.",
				keys > traceSize);
	}
}
