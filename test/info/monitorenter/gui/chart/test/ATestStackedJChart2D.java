/*
 *  ATestJChart2D.java, base class for testing two charts stacked on top 
 *  od each other. 
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
package info.monitorenter.gui.chart.test;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.dialogs.ModalDialog;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Base class for testing JChart2D.
 * <p>
 * 
 * @author Bill Schoolfield
 * 
 */
public abstract class ATestStackedJChart2D extends TestCase {

    /** The x axis configured for the test chart. */
    protected AAxis<?> m_axisX1;
    /** The y axis configured for the test chart. */
    protected AAxis<?> m_axisY1;
    /** The traces configured for the test chart. */
    protected ITrace2D[] m_traces1;
    /** The test chart. */
    protected Chart2D m_chart1;
    /** The x axis configured for the 2nd test chart. */
    protected AAxis<?> m_axisX2;
    /** The y axis configured for the 2nd test chart. */
    protected AAxis<?> m_axisY2;
    /** The traces configured for the 2nd test chart. */
    protected ITrace2D[] m_traces2;
    /** The test 2nd chart. */
    protected Chart2D m_chart2;
    /** The frame to show the test chart. */
    protected JFrame m_frame;

    /**
     * Constructor with the test name.
     * <p>
     * 
     * @param arg0
     *          the name of the test.
     */
    public ATestStackedJChart2D(final String arg0) {
        super(arg0);
    }

    /**
     * Implement and return an instance of the type to test.
     * <p>
     * 
     * @return the <code>{@link AAxis}</code> to test.
     */
    protected abstract AAxis<?> createAxisX();

    /**
     * Implement and return an instance of the type to test.
     * <p>
     * 
     * @return the y axis to test.
     */
    protected abstract AAxis<?> createAxisY();

    /**
     * Implement and return the instances of the type to test.
     * <p>
     * 
     * @return the traces to test.
     */
    protected abstract ITrace2D[] createTraces();

    /**
     * Template method that fills the configured trace for the 1st chart with data.
     * <p>
     * 
     * @param trace2D
     *          this class will use the internal configured trace for the test.
     */
    protected abstract void fillTrace1(ITrace2D trace2D);

    /**
     * Template method that fills the configured trace for the 2nd chart with data.
     * <p>
     * 
     * @param trace2D
     *          this class will use the internal configured trace for the test.
     */
    protected abstract void fillTrace2(ITrace2D trace2D);

    /**
     * Returns the 1st chart.
     * <p>
     * 
     * @return the 1st chart
     */
    public final Chart2D getChart1() {
        return this.m_chart1;
    }

    /**
     * Returns the 2nd chart.
     * <p>
     * 
     * @return the 2nd chart
     */
    
    public final Chart2D getChart2() {
        return this.m_chart2;
    }

    /**
     * Returns the traces for 1st chart.
     * <p>
     * 
     * @return the traces for 1st chart
     */
    public final ITrace2D[] getTraces1() {
        return this.m_traces1;
    }
    
    /**
     * Returns the traces for the 2nd chart.
     * <p>
     * 
     * @return the traces for the 2nd chart
     */
    public final ITrace2D[] getTraces2() {
        return this.m_traces2;
    }

    /**
     * Sets up a chart and shows it in a frame.
     * <p>
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.m_axisX1 = this.createAxisX();
        this.m_axisX1.setPaintGrid(true);
        this.m_axisY1 = this.createAxisY();
        this.m_axisY1.setPaintGrid(true);
        this.m_axisX2 = this.createAxisX();
        this.m_axisX2.setPaintGrid(true);
        this.m_axisY2 = this.createAxisY();
        this.m_axisY2.setPaintGrid(true);


        this.m_traces1 = this.createTraces();
        this.m_chart1 = new Chart2D();

        this.m_chart1.setAxisXBottom(this.m_axisX1, 0);
        this.m_chart1.setAxisYLeft(this.m_axisY1, 0);
        for (int i = 0; i < this.m_traces1.length; i++) {
            this.m_chart1.addTrace(this.m_traces1[i]);
        }
        Assert.assertNotSame(this.m_axisX1, this.m_axisY1);
        for (int i = 0; i < this.m_traces1.length; i++) {
            this.fillTrace1(this.m_traces1[i]);
        }

        this.m_traces2 = this.createTraces();
        this.m_chart2 = new Chart2D();

        this.m_chart2.setAxisXBottom(this.m_axisX2, 0);
        this.m_chart2.setAxisYLeft(this.m_axisY2, 0);
        for (int i = 0; i < this.m_traces2.length; i++) {
            this.m_chart2.addTrace(this.m_traces2[i]);
        }
        Assert.assertNotSame(this.m_axisX2, this.m_axisY2);
        for (int i = 0; i < this.m_traces2.length; i++) {
            this.fillTrace2(this.m_traces2[i]);
        }

        this.m_axisY2.setPixelXRight(this.m_axisY1.getPixelXRight());
        this.m_chart2.setSynchronizedXStartChart(this.m_chart1);

        this.m_frame = new JFrame();
        this.m_frame.setLayout(new GridLayout(0, 1));
        this.m_frame.getContentPane().add(this.m_chart1);
        this.m_frame.getContentPane().add(this.m_chart2);

        this.m_frame.setSize(400, 600);
        this.m_frame.setVisible(true);
        Thread.sleep(1000);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText("Does the result look ok?");
        ModalDialog dialog = new ModalDialog(this.m_frame, "Judge operation ", textArea);
        dialog.setSize(new Dimension(400, 100));
        dialog.showDialog();
        boolean failure = false;
        if (!dialog.isOk()) {
            failure = true;

        }
        if (failure) {
            Assert.fail("Operation test was judged as a failure. ");
        }

        super.tearDown();
        this.m_frame.setVisible(false);
        this.m_frame.dispose();
    }
}
