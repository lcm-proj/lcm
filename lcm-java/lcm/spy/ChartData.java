package lcm.spy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

public class ChartData {
	private long startuTime;
	private LinkedList<ZoomableChartScrollWheel> charts = new LinkedList<ZoomableChartScrollWheel>();
	private ArrayList<Color> colors = new ArrayList<Color>();
	
	private int colorNum = 0;
	
	public ChartData(long startuTime)
	{
		this.startuTime = startuTime;
		
		colors.add(Color.RED);
		colors.add(Color.BLACK);
		colors.add(Color.BLUE);
		colors.add(Color.ORANGE);
		colors.add(Color.MAGENTA);
		colors.add(Color.CYAN);
		colors.add(Color.GREEN);
		
		
	}
	
	public LinkedList<ZoomableChartScrollWheel> getCharts()
	{
		return charts;
	}
	
	public Color popColor()
	{
		colorNum ++;
		return colors.get(colorNum % colors.size());
	}
	
	public long getStartTime()
	{
		return startuTime;
	}
}
