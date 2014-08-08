package lcm.spy;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import java.lang.reflect.*;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.traces.Trace2DLtd;

public class ObjectPanel extends JPanel
{
    String name;
    Object o;
    long utime; // time of this message's arrival
    int lastwidth = 500;
    int lastheight = 100;

    class Section
    {
        int x0, y0, x1, y1; // bounding coordinates for sensitive area
        boolean collapsed;
        HashMap<String, Chart2D> sparklines;
        
        public Section()
        {
            sparklines = new HashMap<String, Chart2D>();
        }
    }

    ArrayList<Section> sections = new ArrayList<Section>();

    public ObjectPanel(String name)
    {
        this.name = name;
        this.setLayout(null); // oh I hate to do this
        
        addMouseListener(new MyMouseAdapter());
    }

    class PaintState
    {
        Color indentColors[] = new Color[] {new Color(255,255,255), new Color(230,230,255), new Color(200,200,255)};
        Graphics g;
        FontMetrics fm;
        JPanel panel;
        
        int indent_level;
        int color_level;
        int y;
        int textheight;

        int x[] = new int[4]; // tab stops
        int indentpx = 20; // pixels per indent level

        int maxwidth;

        int nextsection = 0;

        int collapse_depth = 0;

        public int beginSection(String type, String name, String value)
        {
            // allocate a new section number and make sure there's
            // an entry for us to use in the sections array.
            int section = nextsection++;
            Section cs;
            if (section == sections.size()) {
                cs = new Section();
                sections.add(cs);
            }

            cs = sections.get(section);

            // Some enclosing section is collapsed, exit before drawing anything.

            if (collapse_depth == 0)
            {
                // we're not currently collapsed. Draw the header (at least.)
                beginColorBlock();
                spacer();

                Font of = g.getFont();
                g.setFont(of.deriveFont(Font.BOLD));
                FontMetrics fm = g.getFontMetrics();

                String tok = cs.collapsed ? "+" : "-";
                g.setColor(Color.white);
                g.fillRect(x[0] + indent_level*indentpx, y, 1, 1);
                g.setColor(Color.black);

                String type_split[] = type.split("\\.");
                String drawtype = type_split[type_split.length - 1];

                int type_len = fm.stringWidth(drawtype);
                int name_len = fm.stringWidth(name);

                int tok_pixidx = x[0] + indent_level*indentpx;
                int type_pixidx = x[0] + indent_level*indentpx + 10;

                g.drawString(tok, tok_pixidx, y);
                g.drawString(drawtype, type_pixidx, y);

                // check if type field is too long. put name on new line if yes
                if (type_pixidx + type_len > x[1])
                    y+= textheight;
                g.drawString(name,  x[1], y);

                // check if name field is too long.  put value on new line if yes
                // No need to put it on a new line if value is NULL
                if (x[1] + name_len > x[2] && value.length() > 0)
                    y+= textheight;
                g.drawString(value, x[2], y);

                g.setFont(of);

                // set up the coordinates where clicking will toggle whether
                // we are collapsed.
                cs.x0 = x[0];
                cs.x1 = getWidth();
                cs.y0 = y - textheight;
                cs.y1 = y;

                y += textheight;

                indent();
            }
            else
            {
                // no clicking area.
                cs.x0 = 0; cs.x1 = 0; cs.y0 = 0; cs.y1 = 0;
            }


            // if this section is collapsed, stop drawing.
            if (sections.get(section).collapsed)
                collapse_depth ++;

            return section;
        }

        public void endSection(int section)
        {
            Section cs = sections.get(section);
            cs.y1 = y;

            // if this section is collapsed, resume drawing.
            if (sections.get(section).collapsed)
                collapse_depth --;

            unindent();
            spacer();
            endColorBlock();
            spacer();
        }

        public void drawStrings(String type, String name, String value, boolean isstatic)
        {
            if (collapse_depth > 0)
                return;

            Font of = g.getFont();
            if (isstatic)
                g.setFont(of.deriveFont(Font.ITALIC));

            g.drawString(type,  x[0] + indent_level*indentpx, y);
            g.drawString(name,  x[1], y);
            g.drawString(value, x[2], y);

            y+= textheight;

            g.setFont(of);
        }
        
        public void drawStringsAndGraph(Class cls, String name, Object o, boolean isstatic,
                int sec)
        {
            if (collapse_depth > 0)
                return;

            if (isstatic)
            {
                drawStrings(cls.getName(), name, o.toString(), isstatic);
                return;
            }
            
            Font of = g.getFont();

            g.drawString(cls.getName(),  x[0] + indent_level*indentpx, y);
            g.drawString(name,  x[1], y);
            g.drawString(o.toString(), x[2], y);
            
            // draw the graph
            double value = Double.NaN;
            
            if (o instanceof Double)
                value = (Double) o;
            else if (o instanceof Float)
                value = (Float) o;
            else if (o instanceof Integer)
                value = (Integer) o;
            else if (o instanceof Long)
                value = (Long) o;

            if (!Double.isNaN(value))
            {
                // see if we already have a sparkline for this item
                
                Section cs = sections.get(sec);
                
                Chart2D chart = cs.sparklines.get(name);
                ITrace2D trace;
                
                if (chart == null)
                {
                    // first instance of this graph, so create it
                    
                    chart = new Chart2D();
                    cs.sparklines.put(name, chart);
                    
                    trace = new Trace2DLtd(name);
                    
                    chart.addTrace(trace);
                    chart.setPaintLabels(false);
                    chart.setBackground(new Color(230,230,255));
                    chart.getAxisX().getAxisTitle().setTitle("");
                    chart.getAxisX().setPaintScale(false);
                    chart.getAxisY().getAxisTitle().setTitle("");
                    chart.getAxisY().setPaintScale(false);
                    chart.setSize(500, 500);
                    chart.setLocation(x[3], y+200*sec);
                    
                    //panel.add(chart);
                    
                } else {
                    trace = chart.getTraces().first();
                }
                
                // add the data to our trace
                trace.addPoint((double)utime/1000000.0d, value);
                
                // draw the graph
                DrawSparkline(x[3], y, trace);

                
                
                
                //g.drawString(String.valueOf(thisValue), x[3], y);
            }
            /*
            } else {
                drawStrings(cls.getName(), name, o.toString(), isstatic);
                return;
            }
*/
            y+= textheight;

            g.setFont(of);
        }
        
        public void DrawSparkline(int x, int y, ITrace2D trace)
        {
            if (trace.getSize() < 2)
            {
                return;
            }
            
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            Iterator<ITracePoint2D> iter = trace.iterator();
            
            final int height = textheight;
            double width = 150;
            
            width = width * ((double)trace.getSize() / (double) trace.getMaxSize());
            
            if (trace.getMaxX() == trace.getMinX())
            {
                // no time series, don't draw anything
                return;
            }
                    
            if (trace.getMaxY() == trace.getMinY())
            {
                // divide by zero error coming up!
                // bail and draw a straight line down the center of the graph
                
                g2.drawLine(x, y-(int)((double)height/(double)2), x+(int)width, y-(int)((double)height/(double)2));
                return;
            }
            
            // decide on the main axis scale
            double xscale = width / (trace.getMaxX() - trace.getMinX());
            double yscale = height / (trace.getMaxY() - trace.getMinY());
            
            
            
            boolean first = true;
            
            double lastX = 0, lastY = 0, thisX, thisY;
            
            while (iter.hasNext())
            {
                ITracePoint2D point = iter.next();
                
                if (first) 
                {
                    first = false;
                    lastX = (point.getX() - trace.getMinX()) * xscale + x;
                    lastY = y - (point.getY() - trace.getMinY()) * yscale;
                } else {
                    thisX = (point.getX() - trace.getMinX()) * xscale + x;
                    thisY = y - (point.getY() - trace.getMinY()) * yscale;
                    
                    g2.drawLine((int)lastX, (int)lastY, (int)thisX, (int)thisY);
                    lastX = thisX;
                    lastY = thisY;
                }
            }
            
        }
        

        public void spacer()
        {
            if (collapse_depth > 0)
                return;

            y+= textheight/2;
        }

        public void beginColorBlock()
        {
            if (collapse_depth > 0)
                return;

            color_level++;
            g.setColor(indentColors[color_level%indentColors.length]);
            g.fillRect(x[0] + indent_level*indentpx - indentpx/2, y - fm.getMaxAscent(), getWidth(), getHeight());
            g.setColor(Color.black);
        }

        public void endColorBlock()
        {
            if (collapse_depth > 0)
                return;

            color_level--;
            g.setColor(indentColors[color_level%indentColors.length]);
            g.fillRect(x[0] + indent_level*indentpx -indentpx/2, y - fm.getMaxAscent(), getWidth(), getHeight());
            g.setColor(Color.black);
        }

        public void indent()
        {
            indent_level++;
        }

        public void unindent()
        {
            indent_level--;
        }

        public void finish()
        {
            g.setColor(Color.white);
            g.fillRect(0, y, getWidth(), getHeight());
        }
    }

    public void setObject(Object o, long utime)
    {
        this.o = o;
        this.utime = utime;
        repaint();
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(lastwidth, lastheight);
    }

    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    public void paint(Graphics g)
    {
        int width = getWidth(), height = getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.black);
        FontMetrics fm = g.getFontMetrics();

        PaintState ps = new PaintState();

        ps.panel = this;
        ps.g = g;
        ps.fm = fm;
        ps.textheight = 15;
        ps.y = ps.textheight;
        ps.indent_level=1;
        ps.x[0] = 0;
        ps.x[1] = Math.min(200, width/4);
        ps.x[2] = Math.min(ps.x[1]+200, 2*width/4);
        ps.x[3] = ps.x[2]+150;

        if (o != null)
            paintRecurse(g, ps, "", o.getClass(), o, false, -1);

        ps.finish();
        if (ps.y != lastheight) {
            lastheight = ps.y;
            invalidate();
            getParent().validate();
        }
    }

    void paintRecurse(Graphics g, PaintState ps, String name, Class cls, Object o, boolean isstatic, int section)
    {
        if (o == null) {
            ps.drawStrings(cls==null ? "(null)" : cls.getName(), name, "(null)", isstatic);
            return;
        }

        if (cls.equals(Byte.TYPE)) {
            ps.drawStrings(cls.getName(), name,
                           String.format("0x%02X   %03d   %+04d   %c",
                                         ((Byte)o),((Byte)o).intValue()&0x00FF,((Byte)o), ((Byte)o)&0xff),
                           isstatic);

        } else if (cls.isPrimitive()) {

            // This is our common case...
            ps.drawStringsAndGraph(cls, name, o, isstatic, section);
            //ps.drawStrings(cls.getName(), name, o.toString(), isstatic);

        } else if (o instanceof Enum) {

            ps.drawStrings(cls.getName(), name, ((Enum) o).name(), isstatic);

        } else if (cls.equals(String.class)) {

            ps.drawStrings("String", name, o.toString(), isstatic);

        } else if (cls.isArray())  {

            int sz = Array.getLength(o);
            int sec = ps.beginSection(cls.getComponentType()+"[]", name+"["+sz+"]", "");

            for (int i = 0; i < sz; i++)
                paintRecurse(g, ps, name+"["+i+"]", cls.getComponentType(), Array.get(o, i), isstatic, sec);

            ps.endSection(sec);

        } else {

            // it's a compound type. recurse.
            int sec = ps.beginSection(cls.getName(), name, "");

            // it's a class
            Field fs[] = cls.getFields();
            for (Field f : fs) {
                try {
                    paintRecurse(g, ps, f.getName(), f.getType(), f.get(o), isstatic || ((f.getModifiers()&Modifier.STATIC) != 0), sec);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace(System.out);                    
                }
            }

            ps.endSection(sec);
        }
    }
    
    public boolean isOptimizedDrawingEnabled()
    {
        return false;
    }

    class MyMouseAdapter extends MouseAdapter
    {
        public void mouseClicked(MouseEvent e)
        {
            int x = e.getX(), y = e.getY();

            int bestsection = -1;

            // find the bottom-most section that contains the mouse click.
            for (int i = 0; i < sections.size(); i++)
            {
                Section cs = sections.get(i);

                if (x>=cs.x0 && x<=cs.x1 && y>=cs.y0 && y<=cs.y1) {
                    bestsection = i;
                }
            }

            if (bestsection >= 0)
                sections.get(bestsection).collapsed ^= true;
            
            // call repaint here so the UI will update immediately instead of
            // waiting for the next piece of data
            repaint();
        }
    }
}
