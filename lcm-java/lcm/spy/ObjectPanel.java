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

public class ObjectPanel extends JPanel
{
    String name;
    Object o;
    int lastwidth = 400;
    int lastheight = 100;

    class Section
    {
        int x0, y0, x1, y1; // bounding coordinates for sensitive area
        boolean collapsed;
    }

    ArrayList<Section> sections = new ArrayList<Section>();

    public ObjectPanel(String name)
    {
        this.name = name;

        addMouseListener(new MyMouseAdapter());
    }

    class PaintState
    {
        Color indentColors[] = new Color[] {new Color(255,255,255), new Color(230,230,255), new Color(200,200,255)};
        Graphics g;
        FontMetrics fm;

        int indent_level;
        int color_level;
        int y;
        int textheight;

        int x[] = new int[3]; // tab stops
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

                String tok = cs.collapsed ? "+" : "-";
                g.setColor(Color.white);
                g.fillRect(x[0] + indent_level*indentpx, y, 1, 1);
                g.setColor(Color.black);

                g.drawString(tok, x[0] + indent_level*indentpx, y);
                g.drawString(type,  x[0] + indent_level*indentpx + 10, y);
                g.drawString(name,  x[1], y);
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

    public void setObject(Object o)
    {
        this.o = o;
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

        ps.g = g;
        ps.fm = fm;
        ps.textheight = 15;
        ps.y = ps.textheight;
        ps.indent_level=1;
        ps.x[0] = 0;
        ps.x[1] = Math.min(200, width/3);
        ps.x[2] = Math.min(ps.x[1]+200, 2*width/3);

        if (o != null)
            paintRecurse(g, ps, "", o.getClass(), o, false);

        ps.finish();
        if (ps.y != lastheight) {
            lastheight = ps.y;
            invalidate();
            getParent().validate();
        }
    }

    void paintRecurse(Graphics g, PaintState ps, String name, Class cls, Object o, boolean isstatic)
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
            ps.drawStrings(cls.getName(), name, o.toString(), isstatic);

        } else if (o instanceof Enum) {

            ps.drawStrings(cls.getName(), name, ((Enum) o).name(), isstatic);

        } else if (cls.equals(String.class)) {

            ps.drawStrings("String", name, o.toString(), isstatic);

        } else if (cls.isArray())  {

            int sz = Array.getLength(o);
            int sec = ps.beginSection(cls.getComponentType()+"[]", name+"["+sz+"]", "");

            for (int i = 0; i < sz; i++)
                paintRecurse(g, ps, name+"["+i+"]", cls.getComponentType(), Array.get(o, i), isstatic);

            ps.endSection(sec);

        } else {

            // it's a compound type. recurse.
            int sec = ps.beginSection(cls.getName(), name, "");

            // it's a class
            Field fs[] = cls.getFields();
            for (Field f : fs) {
                try {
                    paintRecurse(g, ps, f.getName(), f.getType(), f.get(o), isstatic || ((f.getModifiers()&Modifier.STATIC) != 0));
                } catch (Exception ex) {
                }
            }

            ps.endSection(sec);
        }
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
        }
    }
}
