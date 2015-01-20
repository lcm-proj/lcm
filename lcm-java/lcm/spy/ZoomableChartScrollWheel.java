package lcm.spy;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ZoomableChart;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.gui.chart.axis.AxisLinear;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import javax.swing.*;

/**
 * Chart that supports panning and zooming in the Google-maps style.
 *
 */
public class ZoomableChartScrollWheel extends ZoomableChart
{
    private double mouseDownStartX, mouseDownStartY, mouseDownValPerPxX, mouseDownMinX, mouseDownMaxX;
    private ArrayList<Double> mouseDownValPerPxY = new ArrayList<Double>();
    private ArrayList<Double> mouseDownMinY = new ArrayList<Double>();
    private ArrayList<Double> mouseDownMaxY = new ArrayList<Double>();
    
    private long lastFocusTime = -1;
    private JFrame frame = null;
    
    // internal color list
    private ArrayList<Color> colors = new ArrayList<Color>();

    // color index
    private int colorNum = 0;
    
    // we need a list of the axes on the right, which we update ourselves
    private ArrayList<AAxis> rightYAxis = new ArrayList<AAxis>();
    
    private JPopupMenu popup = new JPopupMenu();
    
    ChartData chartData;
    
    /**
     * Constructor, taking in a chartData so that we can set up the chart 
     * 
     * @param chartData global data about all charts displayed in lcm-spy
     */
    public ZoomableChartScrollWheel(ChartData chartData)
    {
        this.addMouseWheelListener(new MyMouseWheelListener(this));
        
        this.getAxisX().setPaintGrid(true);
        this.getAxisY().setPaintGrid(true);
        this.setUseAntialiasing(true);
        this.setGridColor(Color.LIGHT_GRAY);
        this.getAxisX().getAxisTitle().setTitle("Time (sec)");
        this.getAxisY().getAxisTitle().setTitle("");
        this.chartData = chartData;
        
        colors.add(Color.RED);
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        colors.add(Color.MAGENTA);
        colors.add(Color.CYAN);
        colors.add(Color.ORANGE);
        colors.add(Color.GREEN);
        
        this.setFixedWidthXAxisFormat();
        
        
        this.setMinPaintLatency(16); // cap the frame-rate at 60fps
    }
    
    /**
     * Creates a new frame for this trace.  Called either by ObjectPanel to create
     * a new graph or from the right-click menu to move a trace to a new graph.
     * 
     * @param chartData global chart data for all of lcm-spy
     * @param trace data that this chart should display
     */
    public static void newChartFrame(final ChartData chartData, final ITrace2D trace)
    {
        JFrame frame = new JFrame(trace.getName());
        
        final ZoomableChartScrollWheel newChart = new ZoomableChartScrollWheel(chartData);
        
        trace.setColor(newChart.popColor());
        
        newChart.addTrace(trace);
        newChart.updateRightClickMenu();
        
        chartData.getCharts().add(newChart);
        
        Container content = frame.getContentPane(); 
        content.add(newChart);
        
        newChart.addFrameFocusTimer(frame);
        
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                for (ITrace2D trace : newChart.getTraces())
                {
                    ((Trace2DLtd)trace).setMaxSize(chartData.sparklineChartSize);
                }
                chartData.getCharts().remove(newChart);
            }
        });
        
        frame.setSize(600, 500);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
    
    /**
     * Shows the right-click menu if appropriate.
     * 
     * @param e MouseEvent to process
     * @return true if the right-click menu was shown
     */
    private boolean maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            popup.show(e.getComponent(), e.getX(), e.getY());
            return true;
        }
        return false;
    }
    
    /**
     * Gets the next color for a new trace.  Use this to keep colors as different
     * as possible.  Increments the color counter.
     * 
     * @return next color to use for a trace
     */
    public Color popColor()
    {
        Color thisColor = colors.get(colorNum % colors.size());
        colorNum++;
        return thisColor;
    }
    
    /**
     * Adds the newest trace color back onto the stack.
     */
    public void pushColor()
    {
        colorNum--;
    }
    
    /**
     * Updates the right click menu to allow for moving
     * traces around.  Should be called immediately after adding
     * a new trace.
     */
    public void updateRightClickMenu()
    {
        // zap the old right click menu
        popup = new JPopupMenu();
        
        Iterator<ITrace2D> iter = this.getTraces().iterator();
        
        boolean firstFlag = true;
        
        StringBuilder frameTitle = new StringBuilder();
        
        while (iter.hasNext())
        {
            final ITrace2D trace = iter.next();
            
            JMenuItem topItem = new JMenuItem(trace.getName());
            topItem.setEnabled(false);
            
            if (!firstFlag)
            {
                popup.addSeparator();
            }
            
            popup.add(topItem);
            popup.addSeparator();
            
            boolean rightTraceFlag = false;
            
            for (final AAxis axis : rightYAxis)
            {
                if (axis.getTraces().contains(trace))
                {
                    // this trace is in the extra Y axis area
                    
                    JMenuItem newItem = new JMenuItem("    to main axis");
                    
                    newItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e)
                        {
                            ZoomableChartScrollWheel.this.removeAxisYRight(axis);
                            ZoomableChartScrollWheel.this.removeTrace(trace);
                            //rightYAxis.remove(axis);
                            ZoomableChartScrollWheel.this.addTrace(trace);
                            ZoomableChartScrollWheel.this.updateRightClickMenu();
                        }
                    });
                    
                    popup.add(newItem);
                    rightTraceFlag = true;
                    break;
                }
            }
            
            if (rightTraceFlag == false)
            {
                // this trace is on the normal Y axis
                JMenuItem newItem = new JMenuItem("    to separate axis");
                
                if (this.getAxisY().getTraces().size() < 2)
                {
                    newItem.setEnabled(false);
                }
                
                newItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        AxisLinear newAxis = new AxisLinear();
                        ZoomableChartScrollWheel.this.removeTrace(trace);
                        ZoomableChartScrollWheel.this.addAxisYRight(newAxis);
                        ZoomableChartScrollWheel.this.addTrace(trace,
                                ZoomableChartScrollWheel.this.getAxisX(), newAxis);
                        ZoomableChartScrollWheel.this.updateRightClickMenu();
                    }
                });
                
                popup.add(newItem);
            }
            
            JMenuItem moveWindowItem = new JMenuItem("    move to new window");
            moveWindowItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    for (AAxis axisL : rightYAxis)
                    {
                        if (axisL.getTraces().contains(trace))
                        {
                            ZoomableChartScrollWheel.this.removeAxisYRight(axisL);
                            break;
                        }
                    }
                    
                    ZoomableChartScrollWheel.this.removeTrace(trace);
                    ZoomableChartScrollWheel.this.updateRightClickMenu();
                    
                    ZoomableChartScrollWheel.newChartFrame(chartData, trace);
                    
                    
                }
            });
            
            
            JMenuItem delItem = new JMenuItem("    remove");
            delItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    for (AAxis axisL : rightYAxis)
                    {
                        if (axisL.getTraces().contains(trace))
                        {
                            ZoomableChartScrollWheel.this.removeAxisYRight(axisL);
                            break;
                        }
                    }
                    
                    ZoomableChartScrollWheel.this.removeTrace(trace);
                    ZoomableChartScrollWheel.this.updateRightClickMenu();
                }
            });
            
            if (this.getAxisX().getTraces().size() < 2)
            {
                delItem.setEnabled(false);
                moveWindowItem.setEnabled(false);
            }
            
            popup.add(moveWindowItem);
            popup.add(delItem);
            
            if (!firstFlag)
            {
                frameTitle.append(", ");
            }
            frameTitle.append(trace.getName());
            
            firstFlag = false;
        }
        
        if (this.frame != null)
        {
            this.frame.setTitle(frameTitle.toString());
        }
    }
    

    public void addAxisYRight(AAxis<?> axisY)
    {
        super.addAxisYRight(axisY);
        
        rightYAxis.add(axisY);
    }
    
    public boolean removeAxisYRight(IAxis<?> axisY)
    {
        rightYAxis.remove(axisY);
        
        return super.removeAxisYRight(axisY);
    }
    
    /**
     * Move this frame to the front to get the user's attention
     */
    public void toFront()
    {
        if (frame != null)
        {
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    frame.toFront();
                    frame.repaint();
                }
            });
        }
    }
    
    /**
     * Saves the time this window was last in focus.  Allows us to put new traces
     * on the last chart that was in focus
     * 
     * @param frame frame to add the focus timer to
     */
    public void addFrameFocusTimer(JFrame frame)
    {
        this.frame = frame;
        
        this.frame.addWindowFocusListener(new WindowFocusListener()
        {
            public void windowGainedFocus(WindowEvent we)
            {
                lastFocusTime = System.nanoTime()/1000;
            }

            public void windowLostFocus(WindowEvent we)
            {
                lastFocusTime = System.nanoTime()/1000;
            }
        });
    }
    
    /**
     * Returns the last time this frame was focused.
     * 
     * @return the last time the frame was focused
     */
    public long getLastFocusTime() { return lastFocusTime; }
    
    /**
     * Handle mouse press events
     * 
     */
    public void mousePressed(MouseEvent e)
    {
        if (maybeShowPopup(e))
        {
            return;
        }
        
        IAxis xAxis = this.getAxisX();
        IAxis yAxis = this.getAxisY();
        
        double xAxisRange = xAxis.getRange().getExtent();
        
        mouseDownValPerPxY.clear();
        mouseDownMinY.clear();
        mouseDownMaxY.clear();
        
        mouseDownStartX = e.getX();
        mouseDownStartY = e.getY();
        
        double xAxisWidth = this.getXChartEnd() - this.getXChartStart();
        double yAxisHeight = this.getYChartStart() - this.getYChartEnd();
        
        mouseDownValPerPxX = xAxisRange / xAxisWidth;
        
        mouseDownMinX = xAxis.getMin();
        mouseDownMaxX = xAxis.getMax();
        
        double yAxisRange = yAxis.getRange().getExtent();
        
        mouseDownValPerPxY.add(yAxisRange / yAxisHeight);
        mouseDownMinY.add(yAxis.getMin());
        mouseDownMaxY.add(yAxis.getMax());
        
        for (AAxis yAxisRight : rightYAxis)
        {
            double yAxisRangeRight = yAxisRight.getMax() - yAxisRight.getMin();
            mouseDownValPerPxY.add(yAxisRangeRight / yAxisHeight);
            mouseDownMinY.add(yAxisRight.getMin());
            mouseDownMaxY.add(yAxisRight.getMax());
        }
        
    }
    
    /**
     * Pan the chart when the mouse is dragged.
     */
    public void mouseDragged(MouseEvent e)
    {
        // move the view
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != MouseEvent.BUTTON3_DOWN_MASK)
        {
            dragChart(e);
        }
    }
    
    /**
     * Handle mouse release events, including double-click and right click.
     */
    public void mouseReleased(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            this.zoomAll();
            setFixedWidthXAxisFormat();
            
            e.consume();
        } else {
            maybeShowPopup(e);
        }
    }
    
    /**
     * Implements panning the chart on mouse drag.
     * 
     * @param e MouseEvent to process
     */
    private void dragChart(MouseEvent e)
    {
        
        double deltaPxX = e.getX() - mouseDownStartX;
        double deltaPxY = e.getY() - mouseDownStartY;
        
        
        
        double deltaX = deltaPxX * mouseDownValPerPxX;
        double deltaY = deltaPxY * mouseDownValPerPxY.get(0);
        
        if (Double.isNaN(mouseDownMinX) || Double.isNaN(mouseDownMaxX) || Double.isNaN(mouseDownMinY.get(0))
                || Double.isNaN(mouseDownMaxY.get(0)) ||Double.isNaN(deltaX) || Double.isNaN(deltaY)
                || Double.isInfinite(mouseDownMinX) || Double.isInfinite(mouseDownMaxX) || Double.isInfinite(mouseDownMinY.get(0))
                || Double.isInfinite(mouseDownMaxY.get(0)) ||Double.isInfinite(deltaX) || Double.isInfinite(deltaY))
        {
            return;
        }
        
        zoom(mouseDownMinX - deltaX, mouseDownMaxX - deltaX,
                mouseDownMinY.get(0) + deltaY, mouseDownMaxY.get(0) + deltaY);
        
        setVariableWidthXAxisFormat();
        
        // do moving for right Y axes
        for (int i = 0; i < rightYAxis.size(); i++)
        {
            AAxis axis = rightYAxis.get(i);
            
            double deltaYRight = deltaPxY * mouseDownValPerPxY.get(i+1);
            
            zoom(axis, axis.translateValueToPx(mouseDownMinY.get(i+1) + deltaYRight),
                    axis.translateValueToPx(mouseDownMaxY.get(i+1) + deltaYRight));
            
        }
        
        
    }
    
    /**
     * Variable-width x-axis formatting causes jumps when data is "rolling in"
     * because the labels change width, which causes the X-axis to change width
     * which causes visual disruption.  When using zoomAll, change to a fixed
     * width format on the x-axis.
     */
    private void setFixedWidthXAxisFormat()
    {
        DecimalFormat fixedWidthFormat = new DecimalFormat("#");
        LabelFormatterNumber fixedWidthFormatter = new LabelFormatterNumber(fixedWidthFormat);
        this.getAxisX().setFormatter(fixedWidthFormatter);
    }
    
    /**
     * When data is not "rolling in" use a varible format for
     * maximum flexibility in display.
     */
    private void setVariableWidthXAxisFormat()
    {
        DecimalFormat variableWidthFormat = new DecimalFormat();
        LabelFormatterNumber variableWidthFormatter = new LabelFormatterNumber(variableWidthFormat);
        this.getAxisX().setFormatter(variableWidthFormatter);
    }
    
    
    
    public class MyMouseWheelListener implements MouseWheelListener
    {
        private ZoomableChartScrollWheel chart;
        
        public MyMouseWheelListener(ZoomableChartScrollWheel chart)
        {
            this.chart = chart;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            
            int notches = e.getWheelRotation();
         
            IAxis xAxis = chart.getAxisX();
            IAxis yAxis = chart.getAxisY();
            double xAxisRange = xAxis.getRange().getExtent();
            double yAxisRange = yAxis.getRange().getExtent();
            
            double zoomFactor;
            
            if (notches > 0)
            {
                zoomFactor = notches * 1.2;
            } else {
                zoomFactor = -notches * 0.8;
            }
            
            double xSqSize = xAxisRange *  zoomFactor;
            double ySqSize = yAxisRange *  zoomFactor;
            
            
            // compute percentage of chart the mouse pointer is at
            double xPercent = ((double) e.getX() - (double) chart.getXChartStart()) / (double)(chart.getXChartEnd() - chart.getXChartStart()); 
            double yPercent = ((double) e.getY() - (double) chart.getYChartEnd()) / (double)(chart.getYChartStart() - chart.getYChartEnd());
            
            // compute new bounds with the percentages remaining the same so whatever
            // is under the cursor will stay under the cursor
            
            
            // the left most value should be the value the pixel is at minus half of the square size
            double xValueUnderCursor = xAxis.translatePxToValue(e.getX());
            
            double xMin = xValueUnderCursor - xSqSize * xPercent;
            double xMax = xValueUnderCursor + xSqSize * (1 - xPercent);
            
            
            double yValueUnderCursor = yAxis.translatePxToValue(e.getY());
            
            double yMin = yValueUnderCursor - ySqSize * (1 - yPercent);
            double yMax = yValueUnderCursor + ySqSize * yPercent;
            
            if (Double.isNaN(xMin) || Double.isNaN(xMax) || Double.isNaN(yMin) || Double.isNaN(yMax))
            {
                return;
            }
            
            chart.zoom(xMin, xMax, yMin, yMax);
            
            
            
            // also zoom right hand Y axes
            
            for (int i = 0; i < rightYAxis.size(); i++)
            {
                AAxis axis = rightYAxis.get(i);
                
                double axisRange = axis.getMax() - axis.getMin();
                double sqSize =axisRange * zoomFactor;
                double underCursor = axis.translatePxToValue(e.getY());
                
                double minVal = underCursor - sqSize * (1 - yPercent);
                double maxVal = underCursor + sqSize * yPercent;
                
                zoom(axis, axis.translateValueToPx(minVal), axis.translateValueToPx(maxVal));
                
            }
            
            setVariableWidthXAxisFormat();
            
            
            
        }
    }
    
}
