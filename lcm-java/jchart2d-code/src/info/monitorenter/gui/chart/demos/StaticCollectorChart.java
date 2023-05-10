/*
 *  StaticCollectorChart.java, utility test class for jchart2d.
 *  Copyright (C) 2007 - 2011 Achim Westermann, created on 10.12.2004, 13:48:55
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
package info.monitorenter.gui.chart.demos;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.io.AStaticDataCollector;
import info.monitorenter.gui.chart.io.PropertyFileStaticDataCollector;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.util.StringUtil;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A chart to test labels. This chart uses a
 * <code>{@link info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber}</code>
 * that formats to whole integer numbers. No same labels should appear.
 * <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.6 $
 */
public class StaticCollectorChart extends JPanel {

	/**
	 * Generated <code>serialVersionUID</code>.
	 */
	private static final long serialVersionUID = 3689069555917797688L;

	/**
	 * Shows the chart in a frame and fills it with the data file that is
	 * specified by the first command line argument.
	 * <p>
	 * 
	 * @param args
	 *            arg[0] has to be a file name of a data file in
	 *            java.util.Properties format.
	 * 
	 * @throws IOException
	 *             if sth. goes wrong.
	 */
	public static void main(final String[] args) throws IOException {
		JFrame frame = new JFrame("SampleChart");
		boolean validFile = false;
		try {
			if (args.length != 0 && !StringUtil.isEmpty(args[0])) {
				File file = new File(args[0]);
				if (file.exists()) {
					if (file.isFile()) {
						validFile = true;
						InputStream stream = new FileInputStream(new File(
								args[0]));
						ITrace2D trace = new Trace2DSimple();
						AStaticDataCollector collector = new PropertyFileStaticDataCollector(
								trace, stream);

						frame.getContentPane().add(
								new StaticCollectorChart(collector));
						frame.addWindowListener(new WindowAdapter() {
							/**
							 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
							 */
							@Override
							public void windowClosing(final WindowEvent e) {
								System.exit(0);
							}
						});
						frame.setSize(600, 600);
						frame.setVisible(true);
					}
				}
			}
		} catch (Throwable f) {
			validFile = false;
			f.printStackTrace(System.err);
		}
		if (!validFile) {
			System.out
					.println("Missing program argument. Please give the location of a property-file to this program.\n Example: java -cp jchart2d-version.jar "
							+ StaticCollectorChart.class.getName()
							+ " /home/me/data.properties");
		}
	}

	/** The internal chart. */
	private Chart2D m_chart;

	/**
	 * Creates a chart that collects it's data from the given collector.
	 * <p>
	 * 
	 * @param datacollector
	 *            the data collector to use.
	 * 
	 * @throws IOException
	 *             if collecting data fails.
	 */
	public StaticCollectorChart(final AStaticDataCollector datacollector)
			throws IOException {
		this.setLayout(new BorderLayout());
		this.m_chart = new Chart2D();
		// Add the trace to the chart before data collection (because the chart
		// tells the trace which point instances to use)!!!:
		this.m_chart.addTrace(datacollector.getTrace());

		// Add all points, as it is static:
		datacollector.collectData();

		// Make it visible:
		this.add(new ChartPanel(this.m_chart), BorderLayout.CENTER);

	}

	/**
	 * Returns the chart.
	 * <p>
	 * 
	 * @return Returns the chart.
	 */
	public final Chart2D getChart() {
		return this.m_chart;
	}
}
