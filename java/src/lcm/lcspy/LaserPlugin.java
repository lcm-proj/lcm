package lcm.lcspy;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import javax.swing.*;

import lcm.lc.*;
import lcm.lcmtypes.*;

import lcm.util.*;

/** A plugin for viewing laser_t data **/
public class LaserPlugin implements LCSpyPlugin
{
    boolean filledin = true;
    static final double MAX_ZOOM = 1024;
    static final double MIN_ZOOM = 4;
    static final double MAX_RANGE = 75.0;  // ignore returns longer than this (meters)

    public boolean canHandle(Class cls)
    {
	if (cls.equals(laser_t.class))
	    return true;

	return false;
    }

    class MyAction extends AbstractAction
    {
	ChannelData cd;
	JDesktopPane jdp;

	public MyAction(JDesktopPane jdp, ChannelData cd)
	{
	    super("Laser Viewer");
	    this.jdp = jdp;
	    this.cd = cd;
	}

	public void actionPerformed(ActionEvent e) 
	{
	    Viewer v = new Viewer(cd);
	    jdp.add(v);
	    v.toFront();
	}
    }

    public Action getAction(JDesktopPane jdp, ChannelData cd)
    {
	return new MyAction(jdp, cd);
    }

    class LaserPane extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, KeyListener
    {
	laser_t l;
	double tx, ty;
	AffineTransform T ;
	ParameterGUI pg;

	public LaserPane(ParameterGUI pg)
	{
	    this.pg = pg;

	    addMouseWheelListener(this);
	    addMouseListener(this);
	    addMouseMotionListener(this);
	    addKeyListener(this);

	    setFocusable(true);
	    grabFocus();
	}

	public double getScale()
	{
	    return T.getScaleX();
	}

	public void setData(laser_t l)
	{
	    this.l = l;
	    repaint();
	}

	public void paint(Graphics gin)
	{
	    Graphics2D g = (Graphics2D) gin;

	    int width = getWidth(), height = getHeight();

	    if (T == null) {
		T = new AffineTransform();
		T.translate(width/2, height);
		T.scale(8, -8);
	    }

	    g.setColor(Color.black);
	    g.fillRect(0,0,width,height);

	    if (l==null)
		return;

	    g.transform(T);
	    double scale = getScale();

	    double maxrange = 0;

	    // draw the filled-in polygon of the laser scan
	    if (pg.gb("volume"))
		{
		    g.setColor(new Color(0, 0, 100));
		    GeneralPath p = new GeneralPath();
		    p.moveTo(0,0);
		    for (int i = 0; i < l.nranges; i++) {
			if (l.ranges[i] > MAX_RANGE)
			    continue;

			if (l.ranges[i] < 100)
			    maxrange = Math.max(maxrange, l.ranges[i]);
			double theta = l.rad0+i*l.radstep+Math.PI/2;
			double x = l.ranges[i]*Math.cos(theta);
			double y = l.ranges[i]*Math.sin(theta);
			p.lineTo((float) x, (float) y);
		    }
		    p.closePath();
		    g.fill(p);
		    
		    g.setStroke(new BasicStroke((float) (2.0/scale)));
		    
		    g.setColor(new Color(0, 0, 255));
		    g.draw(p);
		}
	    
	    ///////// draw laser returns as dots 

	    // values for sick LMS291-S05
	    double min_intensity = 7500, max_intensity = 12000;

	    if (pg.gb("normalized_intensities")) {
		min_intensity = 0;
		max_intensity = 1;
	    }

	    ColorMapper colormap = new ColorMapper(new int[] {0x0000ff,
							      0x008080,
							      0x808000,
							      0xff0000},
		min_intensity,
		max_intensity);
	    
	    {
		double r = 4.0/scale;
		if (!filledin)
		    r *= 2;

		for (int i = 0; i < l.nranges; i++) {
		    if (l.ranges[i] > MAX_RANGE)
			continue;
		    
		    if (!pg.gb("intensity") || l.nintensities != l.nranges) {
			g.setColor(Color.yellow);
		    } else {
			g.setColor(new Color(colormap.map(l.intensities[i])));
			/*			
			float v = (float) ((l.intensities[i] - min_intensity) / (max_intensity - min_intensity));
			v = Math.min(v, 1);
			v = Math.max(v, 0);
			v = .2f + .8f*v;
			g.setColor(new Color(v,v,v)); */
		    }
		    
		    double theta = l.rad0 + i*l.radstep+Math.PI/2;
		    double x = l.ranges[i]*Math.cos(theta);
		    double y = l.ranges[i]*Math.sin(theta);
		    Ellipse2D e = new Ellipse2D.Double(x-r/2,y-r/2,r,r);
		    g.fill(e);
		}
	    }

	    ///////// draw reference rings
	    {
		g.setStroke(new BasicStroke((float) (1.0/scale))); // 1px
		g.setFont(g.getFont().deriveFont((float) (12.0/scale)).deriveFont(AffineTransform.getScaleInstance(1, -1)));

		int maxring = (int) Math.max(maxrange,Math.sqrt(width*width/4 + height*height)/scale);

		for (int i = 1; i < maxring; i++)
		    {
			double r = i;
			Ellipse2D e = new Ellipse2D.Double(-r,-r, 2*r, 2*r);
			if (i%10==0) {
			    g.setColor(new Color(150,150,150));
			}
			else if (i%5==0) {
			    g.setColor(new Color(100,100,100));
			    //			    if (scale < 25)
			    //				continue;
			}
			else {
			    g.setColor(new Color(40,40,40));
			    if (scale <5)
				continue;
			}

			g.draw(e);
		    }

		// draw ring labels
		g.setColor(new Color(200,200,200));
		for (int i = 1; i < maxring; i++)
		    {
			double r = i;
			if (i%10==0 || (i%5==0 && maxring < 15))
			    g.drawString(""+i, (float) (r + 2/scale), (float) 0);
		    }			

	    }

	    ///////// draw the robot
 	    {
		GeneralPath p = new GeneralPath();
		double b = 10/scale, h = 18/scale; // robot base size and height (a triangle)
		p.moveTo((float) (-b/2), 0);
		p.lineTo((float) b/2, 0);
		p.lineTo(0, (float) h);
		p.closePath();
		g.setColor(Color.cyan);
		g.fill(p);
	    }
	}

	public void keyReleased(KeyEvent e) 
	{
	}

	public void keyPressed(KeyEvent e) 
	{
	    int amt = 8;

	    switch (e.getKeyCode())
		{
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_KP_RIGHT:
		    pan(-amt, 0);
		    break;

		case KeyEvent.VK_LEFT:
		case KeyEvent.VK_KP_LEFT:
		    pan(amt, 0);
		    break;

		case KeyEvent.VK_UP:
		case KeyEvent.VK_KP_UP:
		    pan(0, amt);
		    break;

		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_KP_DOWN:
		    pan(0, -amt);
		    break;
		}
	}

	public void keyTyped(KeyEvent e)
	{
	    switch (e.getKeyChar())
		{
		case 'z':
		case '-':
		    zoom(.5, new Point(getWidth()/2, getHeight()/2));
		    break;

		case 'Z':
		case '+':
		    zoom(2, new Point(getWidth()/2, getHeight()/2));
		    break;

		default:
		    //		    System.out.println("key: "+e.getKeyChar());
		}

	    switch (e.getKeyCode())
		{
		default:
		    //		    System.out.println("key: "+e.getKeyCode());

		}
	}

	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) 
	{
	    // restore default view
	    if (e.getClickCount()==2) {
		T = null;

		repaint();
		filledin = !filledin;
	    }
	}

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	Point dragBegin = null;
	public void mouseDragged(MouseEvent e)
	{
	    Point p = e.getPoint();
	    if (dragBegin != null) {
		double tx = p.getX() - dragBegin.getX();
		double ty = p.getY() - dragBegin.getY();

		pan(tx, ty);
	    }
	    dragBegin = p;
	}

	public void mousePressed(MouseEvent e) 
	{
	    dragBegin = e.getPoint();
	}

	public void mouseReleased(MouseEvent e)
	{
	    dragBegin = null;
	}

	void pan(double tx, double ty)
	{
		AffineTransform ST = AffineTransform.getTranslateInstance(tx, ty);
		T.preConcatenate(ST); // in pixel space
		repaint();
	}

	void zoom(double dscale, Point p)
	{
	    double newscale = getScale() * dscale;

	    if (newscale > MAX_ZOOM || newscale < MIN_ZOOM)
		return;

	    AffineTransform ST = new AffineTransform();
	    ST.translate(p.getX(), p.getY());
	    ST.scale(dscale, dscale);
	    ST.translate(-p.getX(), -p.getY());

	    T.preConcatenate(ST);

	    repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
	    int amount=e.getWheelRotation();
	    double dscale = 1;

	    if (amount > 0)
		dscale = 0.5;
	    else
		dscale = 2;

	    zoom(dscale, e.getPoint());
	}
    }

    class Viewer extends JInternalFrame implements LCSubscriber
    {
	ChannelData cd;
	LaserPane lp;
	ParameterGUI pg;

	public Viewer(ChannelData cd)
	{
	    super("Laser: "+cd.name, true, true);
	    this.cd = cd;

	    setLayout(new BorderLayout());
	    pg = new ParameterGUI();
	    pg.addCheckBoxes("volume", "Show volume", true,
			     "normalized_intensities", "Intensity [0,1]", false,
			     "intensity", "Display intensity", false);
	    lp = new LaserPane(pg);
	    add(lp, BorderLayout.CENTER);


	    add(pg.getPanel(), BorderLayout.SOUTH);
	    setSize(500,400);
	    setVisible(true);

	    LC.getSingleton().subscribe(cd.name, this);
	}

	public void messageReceived(String channel, DataInputStream ins)
	{
	    try {
		laser_t l = new laser_t(ins);
		lp.setData(l);
	    } catch (IOException ex) {
		System.out.println("ex: "+ex);
		return;
	    }
	}
    }
}
