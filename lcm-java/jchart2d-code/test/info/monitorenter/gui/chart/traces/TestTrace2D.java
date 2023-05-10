/*
 *  TestTrace2D.java of project jchart2d - 
 *  a test case for class TestTrace2D. 
 *  Copyright (C) Achim Westermann, created on 23.04.2005, 08:21:12
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Junit test case for <code>{@link Trace2DSimple}</code>.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 */
public class TestTrace2D extends TestCase {
	/**
	 * Test suite for this test class.
	 * <p>
	 * 
	 * @return the test suite
	 */
	public static Test suite() {

		TestSuite suite = new TestSuite();
		suite.setName(TestTrace2D.class.getName());

		suite.addTest(new TestTrace2D("testAddRemoveManyTrace2DLtd"));
		suite.addTest(new TestTrace2D("testAddRemoveManyTrace2DSimple"));
		suite.addTest(new TestTrace2D("testMemoryLeakTrace2PointDListeners"));
		suite.addTest(new TestTrace2D("testPropertyChange"));

		return suite;
	}

	/**
	 * Creates a test case with the given name.
	 * <p>
	 * 
	 * @param testName
	 *            the name of the test case.
	 */
	public TestTrace2D(final String testName) {
		super(testName);
	}

	/**
	 * <p>
	 * Add several <code>Trace2DLtd</code> instances and remove them (procedure
	 * two times) and check for zero traces remaining within the chart.
	 * </p>
	 * 
	 */
	public void testAddRemoveManyTrace2DLtd() {
		Chart2D chart = new Chart2D();
		ArrayList<ITrace2D> traces = new ArrayList<ITrace2D>(5);

		for (int i = 0; i < 5; i++) {
			traces.add(new Trace2DLtd("Trace " + i));
		}

		for (int i = 0; i < 5; i++) {
			chart.addTrace(traces.get(i));
		}

		Iterator<ITrace2D> tracesIt = chart.getTraces().iterator();
		StringBuffer msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');
		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 5, chart.getTraces().size());
		for (int i = 0; i < 5; i++) {
			chart.removeTrace(traces.get(i));
		}

		tracesIt = chart.getTraces().iterator();
		msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');
		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 0, chart.getTraces().size());

		for (int i = 0; i < 5; i++) {
			chart.addTrace(traces.get(i));
		}

		tracesIt = chart.getTraces().iterator();
		msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');
		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 5, chart.getTraces().size());
		for (int i = 0; i < 5; i++) {
			chart.removeTrace(traces.get(i));
		}

		tracesIt = chart.getTraces().iterator();
		msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');

		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 0, chart.getTraces().size());

	}

	/**
	 * <p>
	 * Add several <code>Trace2DSimple</code> instances and remove them
	 * (procedure two times) and check for zero traces remaining within the
	 * chart.
	 * </p>
	 * 
	 */
	public void testAddRemoveManyTrace2DSimple() {
		Chart2D chart = new Chart2D();
		ArrayList<ITrace2D> traces = new ArrayList<ITrace2D>(5);

		for (int i = 0; i < 5; i++) {
			traces.add(new Trace2DSimple("Trace " + i));
		}

		for (int i = 0; i < 5; i++) {
			chart.addTrace(traces.get(i));
		}

		Iterator<ITrace2D> tracesIt = chart.getTraces().iterator();
		StringBuffer msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');
		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 5, chart.getTraces().size());
		for (int i = 0; i < 5; i++) {
			chart.removeTrace(traces.get(i));
		}

		tracesIt = chart.getTraces().iterator();
		msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append((tracesIt.next()).getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');
		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 0, chart.getTraces().size());

		for (int i = 0; i < 5; i++) {
			chart.addTrace(traces.get(i));
		}

		tracesIt = chart.getTraces().iterator();
		msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');
		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 5, chart.getTraces().size());
		for (int i = 0; i < 5; i++) {
			chart.removeTrace(traces.get(i));
		}

		tracesIt = chart.getTraces().iterator();
		msg = new StringBuffer("[");
		while (tracesIt.hasNext()) {
			msg.append(tracesIt.next().getName());
			if (tracesIt.hasNext()) {
				msg.append(',');
			}
		}
		msg.append(']');

		Assert.assertEquals("Wrong number of traces contained: "
				+ msg.toString(), 0, chart.getTraces().size());

	}

	/**
	 * Adds and removes a trace point to a trace and asserts that zero listeners
	 * are in the trace point afterwards.
	 * <p>
	 */
	public void testMemoryLeakTrace2PointDListeners() {
		Chart2D dummyChart = new Chart2D();
		ITrace2D trace = new Trace2DSimple();
		dummyChart.addTrace(trace);
		ITracePoint2D point = new TracePoint2D(1, 1);
		trace.addPoint(point);
		Assert.assertEquals(1, trace.getSize());
		trace.removePoint(point);
		Assert.assertEquals(0, trace.getSize());
	}

	/**
	 * <p>
	 * Register <code>PropertyChangeListener</code> instances on a for different
	 * properties on a <code>Char2D</code>, fire property changes a check for
	 * <code>PropertyChangeEvent</code> instances being fired or not if they
	 * should not be fired.
	 * </p>
	 * 
	 */
	public void testPropertyChange() {
		Chart2D chart = new Chart2D();
		/**
		 * Helper class to detect if <code>{@link PropertyChangeEvent}</code>
		 * are received as expected.
		 * <p>
		 * 
		 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
		 * 
		 * @version $Revision: 1.6 $
		 */
		class PropertyChangeDetector implements PropertyChangeListener {

			/** The caught property change event. */
			private PropertyChangeEvent m_event = null;

			/**
			 * Defcon.
			 * <p>
			 */
			public PropertyChangeDetector() {
				super();
			}

			/**
			 * Returns the last <code>{@link PropertyChangeEvent}</code>
			 * received and clears it internally.
			 * <p>
			 * 
			 * @return the last <code>{@link PropertyChangeEvent}</code>
			 *         received
			 */
			public PropertyChangeEvent consumeEvent() {
				PropertyChangeEvent ret = this.m_event;
				this.m_event = null;
				return ret;
			}

			/**
			 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
			 */
			public void propertyChange(final PropertyChangeEvent evt) {
				this.m_event = evt;
			}
		}

		// test font trigger a "font" change
		PropertyChangeDetector fontListener = new PropertyChangeDetector();
		chart.addPropertyChangeListener("font", fontListener);
		chart.setFont(GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAllFonts()[0]);
		Assert
				.assertNotNull(
						"setFont(Font) on Chart2D did not trigger a PropertyChange for property \"font\". ",
						fontListener.consumeEvent());
		// trigger a different change:
		chart.setBackground(Color.GREEN);
		Assert
				.assertNull(
						"setColor(Color) on Chart2D did trigger a PropertyChange for property \"font\".",
						fontListener.consumeEvent());
	}

}
