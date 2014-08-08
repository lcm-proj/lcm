package lcm.spy;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ZoomableChart;

import javax.swing.*;

import lcm.spy.ObjectPanel.SparklineData;

public class ZoomableChartScrollWheel extends ZoomableChart
{
    double mouseDownStartValX, mouseDownStartX, mouseDownStartValY, mouseDownStartY;
    double mouseDownValPerPxX, mouseDownValPerPxY;
    double mouseDownMinX, mouseDownMaxX, mouseDownMinY, mouseDownMaxY;
    
    public ZoomableChartScrollWheel()
    {
        this.addMouseWheelListener(new MyMouseWheelListener(this));
    }
    
    public void mousePressed(MouseEvent e)
    {
        IAxis xAxis = this.getAxisX();
        IAxis yAxis = this.getAxisY();
        
        double xAxisRange = xAxis.getMax() - xAxis.getMin();
        double yAxisRange = yAxis.getMax() - yAxis.getMin();
        
        mouseDownStartX = e.getX();
        mouseDownStartY = e.getY();
        
        mouseDownStartValX = xAxis.translatePxToValue(e.getX());
        mouseDownStartValY = yAxis.translatePxToValue(e.getY());
        
        double xAxisWidth = this.getXChartEnd() - this.getXChartStart();
        double yAxisHeight = this.getYChartStart() - this.getYChartEnd();
        
        mouseDownValPerPxX = xAxisRange / xAxisWidth;
        mouseDownValPerPxY = yAxisRange / yAxisHeight;
        
        mouseDownMinX = xAxis.getMin();
        mouseDownMaxX = xAxis.getMax();
        mouseDownMinY = yAxis.getMin();
        mouseDownMaxY = yAxis.getMax();
    }
    
    public void mouseDragged(MouseEvent e)
    {
        // move the view
        dragChart(e);
    }
    
    public void mouseReleased(MouseEvent e)
    {
        if (e.getClickCount() == 2)
        {
            this.zoomAll();
            e.consume();
        }
    }
    
    private void dragChart(MouseEvent e)
    {
        
        double deltaPxX = e.getX() - mouseDownStartX;
        double deltaPxY = e.getY() - mouseDownStartY;
        
        
        
        double deltaX = deltaPxX * mouseDownValPerPxX;
        double deltaY = deltaPxY * mouseDownValPerPxY;
        
        zoom(mouseDownMinX - deltaX, mouseDownMaxX - deltaX,
                mouseDownMinY + deltaY, mouseDownMaxY + deltaY);
        
    }
    
    
    
    public class MyMouseWheelListener implements MouseWheelListener
    {
        private ZoomableChartScrollWheel chart;
        
        public MyMouseWheelListener(ZoomableChartScrollWheel chart)
        {
            this.chart = chart;
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            
            int notches = e.getWheelRotation();
         
            IAxis xAxis = chart.getAxisX();
            IAxis yAxis = chart.getAxisY();
            double xAxisRange = xAxis.getMax() - xAxis.getMin();
            double yAxisRange = yAxis.getMax() - yAxis.getMin();
            
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
            
            chart.zoom(xMin, xMax, yMin, yMax);
            
        }
    }
    
}


