package lcm.spy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Global class allowing multiple charts to know about each other and make intelligent
 * decisions based on that.  For example, if I want to graph a new line, I can try to
 * make it a different color than any other lines.
 * 
 * Also ensures that we do not try to
 * create two charts based on the same data backend, which can cause conflicts.
 * 
 * @author abarry
 *
 */
public class ChartData
{
    private long startuTime; // start time of lcm-spy, which all X-axis are based off of
    
    // list of all charts displayed
    private LinkedList<ZoomableChartScrollWheel> charts = new LinkedList<ZoomableChartScrollWheel>();
    
    // internal color list
    private ArrayList<Color> colors = new ArrayList<Color>();

    // global color index
    private int colorNum = 0;

    // constants for setting how much data we keep for each type of graph
    public final int sparklineChartSize = 500;
    public final int detailedSparklineChartSize = 2000;

    /**
     * Constructor for ChartData.  Initializes color list and sets the start time of lcm-spy
     * 
     * @param startuTime lcm-spy start time to base each x-axis off of
     */
    public ChartData(long startuTime)
    {
        this.startuTime = startuTime;

        colors.add(Color.RED);
        colors.add(Color.BLACK);
        colors.add(Color.BLUE);
        colors.add(Color.MAGENTA);
        colors.add(Color.CYAN);
        colors.add(Color.ORANGE);
        colors.add(Color.GREEN);

    }

    /**
     * Returns all charts being displayed
     * 
     * @return all chrats being displayed
     */
    public LinkedList<ZoomableChartScrollWheel> getCharts()
    {
        return charts;
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
     * Get start time in microseconds.
     * 
     * @return  start time of lcm-spy in microseconds
     */
    public long getStartTime()
    {
        return startuTime;
    }
}
