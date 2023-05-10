/*
 *  Showcase.java, the main demo application of project jchart2d.
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
import info.monitorenter.gui.chart.controls.LayoutFactory;
import info.monitorenter.gui.chart.events.Chart2DActionSaveImageSingleton;
import info.monitorenter.gui.chart.io.ADataCollector;
import info.monitorenter.gui.chart.io.RandomDataCollectorOffset;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;
import info.monitorenter.util.Range;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Advanced demonstration applet for jchart2d.
 * <p>
 * Please right click on the chart and on the label for the traces to see popup
 * menus that offer the freshest new features.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.15 $
 * 
 */
public final class Showcase
    extends JApplet { 

  /**
   * Panel with controls for the chart.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.15 $
   */
  final class ControlPanel
      extends JPanel {
    /**
     * Generated for <code>serialVersionUID</code>.
     */
    private static final long serialVersionUID = 3257005441048129846L;

    /** Slider for the maximum amount of points to show. */
    private JSlider m_amountPointsSlider;

    /** Button to clear data from the chart. */
    private JButton m_clear;

    /**
     * <p>
     * The <code>JComboBox</code> used to choose the color of the chart.
     * </p>
     */
    private JComboBox m_colorChooser;

    /** The slider for choosing the speed of adding new points. */
    private JSlider m_latencyTimeSlider;

    /** Button for the action of capturing a snapshot image. */
    private JButton m_snapshot;

    /** Button to start or stop data collection. */
    private JButton m_startStop;

    /**
     * Defcon.
     * <p>
     */
    protected ControlPanel() {
      // create the components:
      this.setBackground(Color.WHITE);
      this.createAmountPointSlider();
      this.createLatencySlider();
      this.createStartStopButton();
      this.createSnapShotButton();
      this.createClearButton();
      this.createColorChooserButton();

      // Layouting: Vertical Grid Layout for putting the sliders...
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(this.m_amountPointsSlider);
      this.add(this.m_latencyTimeSlider);
      // GridLayout stretches components (no respect for their preferred size):
      // Small trick by inserting another component with different Layout.
      JComponent stretch = new JPanel();
      stretch.setBackground(Color.WHITE);
      stretch.setLayout(new BoxLayout(stretch, BoxLayout.X_AXIS));
      stretch.add(Box.createHorizontalGlue());
      stretch.add(this.m_startStop);
      stretch.add(Box.createHorizontalGlue());
      stretch.add(this.m_clear);
      if (this.m_snapshot != null) {
        // for applet usage snapshot is null!
        stretch.add(Box.createHorizontalGlue());
        stretch.add(this.m_snapshot);
      }
      stretch.add(Box.createHorizontalGlue());
      stretch.add(this.m_colorChooser);
      stretch.add(Box.createHorizontalGlue());
      this.add(stretch);
    }

    /**
     * Helper to create a slider for maximum amount of points to show.
     * <p>
     */
    private void createAmountPointSlider() {
      // amountPointsSlider
      this.m_amountPointsSlider = new JSlider(10, 410);
      this.m_amountPointsSlider.setBackground(Color.WHITE);
      // find the value of max points:
      int maxPoints = Showcase.this.getTrace().getMaxSize();
      this.m_amountPointsSlider.setValue(maxPoints);
      this.m_amountPointsSlider.setMajorTickSpacing(40);
      this.m_amountPointsSlider.setMinorTickSpacing(20);
      this.m_amountPointsSlider.setSnapToTicks(true);
      this.m_amountPointsSlider.setPaintLabels(true);
      this.m_amountPointsSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory
          .createEtchedBorder(), "Amount of points.", TitledBorder.LEFT, TitledBorder.BELOW_TOP));
      this.m_amountPointsSlider.setPaintTicks(true);
      this.m_amountPointsSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(final ChangeEvent e) {
          JSlider source = (JSlider) e.getSource();
          // Only if not currently dragged...
          if (!source.getValueIsAdjusting()) {
            int value = source.getValue();
            Showcase.this.getTrace().setMaxSize(value);
          }
        }
      });

    }

    /**
     * Helper to create a button for clearing data from the chart.
     * <p>
     */
    private void createClearButton() {
      // clear Button
      this.m_clear = new JButton("clear");
      this.m_clear.setBackground(Color.WHITE);
      this.m_clear.setBackground(Color.WHITE);
      this.m_clear.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          Showcase.this.clearTrace();
        }
      });

    }

    /**
     * Helper to create a button for choosing trace colors.
     * <p>
     */
    private void createColorChooserButton() {
      // color chooser JComboBox
      this.m_colorChooser = new JComboBox();
      this.m_colorChooser.setBackground(Color.WHITE);

      /**
       * Color with a name.
       * <p>
       * 
       * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
       * @version $Revision: 1.15 $
       */
      final class ColorItem
          extends Color {
        /**
         * Generated <code>serialVersionUID</code>.
         */
        private static final long serialVersionUID = 3257854281104568629L;

        /** The name of the color. */
        private String m_name;

        /**
         * Creates an instance with the given color and it's name.
         * <p>
         * 
         * @param c
         *          the color to use.
         * 
         * @param name
         *          the name of the color.
         */
        public ColorItem(final Color c, final String name) {
          super(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
          this.m_name = name;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
          return this.m_name;
        }
      }
      this.m_colorChooser.addItem(new ColorItem(Color.BLACK, "black"));
      this.m_colorChooser.addItem(new ColorItem(Color.BLUE, "blue"));
      this.m_colorChooser.addItem(new ColorItem(Color.CYAN, "cyan"));
      this.m_colorChooser.addItem(new ColorItem(Color.DARK_GRAY, "darg gray"));
      this.m_colorChooser.addItem(new ColorItem(Color.GRAY, "gray"));
      this.m_colorChooser.addItem(new ColorItem(Color.GREEN, "green"));
      this.m_colorChooser.addItem(new ColorItem(Color.LIGHT_GRAY, "light gray"));
      this.m_colorChooser.addItem(new ColorItem(Color.MAGENTA, "magenta"));
      this.m_colorChooser.addItem(new ColorItem(Color.ORANGE, "orange"));
      this.m_colorChooser.addItem(new ColorItem(Color.PINK, "pink"));
      this.m_colorChooser.addItem(new ColorItem(Color.RED, "red"));
      this.m_colorChooser.addItem(new ColorItem(Color.YELLOW, "yellow"));

      this.m_colorChooser.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent ae) {
          Color color = (Color) ((JComboBox) ae.getSource()).getSelectedItem();
          Showcase.this.getTrace().setColor(color);
        }
      });
      this.m_colorChooser.setSelectedIndex(10);
      this.m_colorChooser.setMaximumSize(new Dimension(200, this.m_clear.getMaximumSize().height));

    }

    /**
     * Helper to create a slider for speed of adding new points.
     * <p>
     */

    private void createLatencySlider() {
      // Latency slider:
      this.m_latencyTimeSlider = new JSlider(10, 210);
      this.m_latencyTimeSlider.setBackground(Color.WHITE);
      this.m_latencyTimeSlider.setValue((int) Showcase.this.getCollector().getLatency());
      this.m_latencyTimeSlider.setMajorTickSpacing(50);
      this.m_latencyTimeSlider.setMinorTickSpacing(10);
      this.m_latencyTimeSlider.setSnapToTicks(true);
      this.m_latencyTimeSlider.setPaintLabels(true);
      this.m_latencyTimeSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory
          .createEtchedBorder(), "Latency for adding points.", TitledBorder.LEFT,
          TitledBorder.BELOW_TOP));
      this.m_latencyTimeSlider.setPaintTicks(true);

      this.m_latencyTimeSlider.addChangeListener(new ChangeListener() {
        public void stateChanged(final ChangeEvent e) {
          JSlider source = (JSlider) e.getSource();
          // Only if not currently dragged...
          if (!source.getValueIsAdjusting()) {
            int value = source.getValue();
            Showcase.this.getCollector().setLatency(value);
          }
        }
      });
    }

    /**
     * Helper to create a button for taking snapshot images.
     * <p>
     */
    private void createSnapShotButton() {
      // the button for snapshot:
      this.m_snapshot = new JButton(Chart2DActionSaveImageSingleton.getInstance(
          Showcase.this.m_chart, "Save image"));
      this.m_snapshot.setBackground(Color.WHITE);
    }

    /**
     * Helper to create a button to start and stop button for data collection.
     * <p>
     */
    private void createStartStopButton() {
      // Start stop Button
      this.m_startStop = new JButton("start");
      this.m_startStop.setBackground(Color.WHITE);
      this.m_startStop.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          JButton source = (JButton) e.getSource();
          if (Showcase.this.getCollector().isRunning()) {
            Showcase.this.stopData();
            source.setText("start");
          } else {
            Showcase.this.startData();
            source.setText("stop");
          }
          source.invalidate();
          source.repaint();
        }
      });
    }
  }

  /**
   * Generated <code>serialVersionUID</code>.
   */
  private static final long serialVersionUID = 3904676068135678004L;

  /**
   * Main entry that uses the applet initialization.
   * <p>
   * 
   * @param args
   *          ignored.
   * 
   * @see #init()
   */
  public static void main(final String[] args) {
    JFrame frame = new JFrame("Showcase");
    Showcase showcase = new Showcase();
    showcase.init();
    frame.getContentPane().add(showcase);
    frame.setSize(400, 600);
    // Enable the termination button [cross on the upper right edge]:
    frame.addWindowListener(new WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setVisible(true);
  }

  /** The char to use. */
  protected Chart2D m_chart;

  /** The data collector to use. */
  private transient ADataCollector m_collector;

  /** The trace to use. */
  private Trace2DLtd m_trace;

  /**
   * Defcon.
   * 
   */
  public Showcase() {
    super();
  }

  /**
   * Clears the internal trace.
   * <p>
   */
  public synchronized void clearTrace() {
    this.getTrace().removeAllPoints();
  }

  /**
   * Returns the chart.
   * <p>
   * 
   * @return the chart to use.
   */
  public Chart2D getChart() {
    return this.m_chart;
  }

  /**
   * Returns the collector to use.
   * <p>
   * 
   * @return the collector to use.
   */
  public ADataCollector getCollector() {
    return this.m_collector;
  }

  /**
   * Returns the trace.
   * <p>
   * 
   * @return the trace.
   */
  public Trace2DLtd getTrace() {
    return this.m_trace;
  }

  /**
   * @see java.applet.Applet#init()
   */
  @Override
  public void init() {
    super.init();
    Chart2D chart = new Chart2D();
    this.setChart(chart);
    this.setSize(new Dimension(600, 500));
    this.m_chart.getAxisX().setPaintGrid(true);
    this.m_chart.getAxisY().setPaintGrid(true);
    chart.getAxisY().setRangePolicy(new RangePolicyMinimumViewport(new Range(-20, +20)));
    chart.setGridColor(Color.LIGHT_GRAY);
    this.setTrace(new Trace2DLtd(100));
    this.getTrace().setName("random");
    this.getTrace().setPhysicalUnits("Milliseconds", "random value");
    this.getTrace().setColor(Color.RED);
    chart.addTrace(this.getTrace());
    Container content = this.getContentPane();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    LayoutFactory factory = LayoutFactory.getInstance();
    ChartPanel chartpanel = new ChartPanel(chart);

    this.setJMenuBar(factory.createChartMenuBar(chartpanel, false));

    content.add(chartpanel);
    content.addPropertyChangeListener(chartpanel);
    this.setCollector(new RandomDataCollectorOffset(this.getTrace(), 50));
    content.add(new ControlPanel());
  }

  /**
   * Sets the chart to use.
   * <p>
   * 
   * I would never code this but applets won't access private members and
   * Checkstyle does not accept non-private members.
   * <p>
   * 
   * So it is only accepted if the member is null. Don't try calling.
   * <p>
   * 
   * @param chart2D
   *          the chart to use.
   */
  public void setChart(final Chart2D chart2D) {
    if (this.m_chart == null) {
      this.m_chart = chart2D;
    }
  }

  /**
   * Sets the collector to use.
   * <p>
   * 
   * @param collector
   *          the collector to use.
   */
  private void setCollector(final RandomDataCollectorOffset collector) {
    this.m_collector = collector;
  }

  /**
   * Sets the trace to use.
   * <p>
   * 
   * This will do nothing if the internal trace has been set before and is only
   * intended for the applet which needs public members or setters.
   * <p>
   * 
   * @param trace
   *          the trace to use.
   */
  public void setTrace(final Trace2DLtd trace) {
    if (this.m_trace == null) {
      this.m_trace = trace;
    }
  }

  /**
   * Starts data collection.
   * <p>
   * 
   */
  public synchronized void startData() {
    if (!this.getCollector().isRunning()) {
      this.getCollector().start();
    }
  }

  /**
   * Stops data collection.
   * 
   * <p>
   */
  public synchronized void stopData() {
    if (this.getCollector().isRunning()) {
      this.getCollector().stop();
    }
  }
}
