package lcm.lcspy;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

import lc.*;
import lcmtypes.*;

/** A plugin for viewing laser_t data **/
public class RadarPluginRaw implements LCSpyPlugin
{
    Random r = new Random();
    static final double MAX_ZOOM = 1024;
    static final double MIN_ZOOM = 4;
    static final double MAX_RANGE = 200;
    static final double MAX_THETA = Math.toRadians(9);
    static final double THETA_OFFSET = Math.PI/2;
    
    public boolean canHandle(Class cls)
    {
	if (cls.equals(canbus_list_t.class))
	    return true;

	return false;
    }

    class MyAction extends AbstractAction
    {
	ChannelData cd;
	JDesktopPane jdp;

	public MyAction(JDesktopPane jdp, ChannelData cd)
	{
	    super("Radar Viewer");
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

    class RadarPane extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, KeyListener
    {
	double tx, ty;
	AffineTransform T ;

	int MAX_TARGETS = 100;
	RadarData      rdata = new RadarData();
	Color          target_colors[] = new Color[MAX_TARGETS];

	public RadarPane()
	{
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

	    g.transform(T);
	    double scale = getScale();

	    g.setStroke(new BasicStroke(0.025f));

	    for (int i = 0; i < rdata.records.length; i++)
		{
		    RadarData.RadarRecord rr = rdata.records[i];
		    Color c = target_colors[i];

		    if (c==null)
			{
			    c = new Color(50+r.nextInt(205), 50+r.nextInt(205), 50+r.nextInt(205));
			    target_colors[i] = c;
			}

		    if (rr==null)
			continue;

		    g.setColor(c);

		    float size = (float) Math.max(0.3, 2*rr.target_power/32768);
		    float x = (float) (rr.range * Math.cos(rr.bearing_rad+THETA_OFFSET));
		    float y = (float) (rr.range * Math.sin(rr.bearing_rad+THETA_OFFSET));
		    
		    Ellipse2D e = new Ellipse2D.Double(x-size/2,y-size/2,
						       size, size);
		    g.fill(e);

		    if (rr.is_mature)
			{
			    g.setColor(Color.yellow);
			    g.draw(e);
			}

		    // draw vectors
		    if (true)
			{
			    GeneralPath p = new GeneralPath();
			    if (rr.range_rate > 0)
				{
				    p.moveTo(x, y+size/2.0f);
				    p.lineTo(x, (float) (y+size/2.0f + rr.range_rate*0.5));
				}
			    else
				{
				    p.moveTo(x, y-size/2.0f);
				    p.lineTo(x, (float) (y-size/2.0f + rr.range_rate*0.5));
				}

			    g.setColor(c);
			    g.draw(p);
			}
		}

	    // draw FOV
	    if (true)
		{
		    GeneralPath p = new GeneralPath();
		    p.moveTo((float) (MAX_RANGE*Math.cos(MAX_THETA+THETA_OFFSET)), 
			     (float) (MAX_RANGE*Math.sin(MAX_THETA+THETA_OFFSET)));
		    p.lineTo(0,0);
		    p.lineTo((float) (MAX_RANGE*Math.cos(-MAX_THETA+THETA_OFFSET)), 
			     (float) (MAX_RANGE*Math.sin(-MAX_THETA+THETA_OFFSET)));
		    g.setColor(Color.darkGray);
		    g.draw(p);
		}


	    /*
	    // draw the filled-in polygon of the laser scan
	    if (filledin)
		{
		    g.setColor(new Color(0, 0, 100));
		    GeneralPath p = new GeneralPath();
		    p.moveTo(0,0);
		    for (int i = 0; i < l.nranges; i++) {
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
	    
	    ///////// draw laser returns as dots in yellow
	    {
		g.setColor(Color.yellow);

		double r = 2.0/scale;
		if (!filledin)
		    r *= 2;

		for (int i = 0; i < l.nranges; i++) {
		    double theta = l.rad0 + i*l.radstep+Math.PI/2;
		    double x = l.ranges[i]*Math.cos(theta);
		    double y = l.ranges[i]*Math.sin(theta);
		    Ellipse2D e = new Ellipse2D.Double(x-r/2,y-r/2,r,r);
		    g.fill(e);
		}
	    }
	    */

	    ///////// draw reference rings
	    {
		g.setStroke(new BasicStroke((float) (1.0/scale))); // 1px
		g.setFont(g.getFont().deriveFont((float) (12.0/scale)).deriveFont(AffineTransform.getScaleInstance(1, -1)));

		//		int maxring = (int) Math.max(maxrange,Math.sqrt(width*width/4 + height*height)/scale);
		int maxring = 200; 

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
			    g.drawString(""+i, 0, (float) (r + 2/scale));
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
	RadarPane rp;

	public Viewer(ChannelData cd)
	{
	    super("Radar: "+cd.name, true, true);
	    this.cd = cd;

	    setLayout(new BorderLayout());
	    rp = new RadarPane();
	    add(rp, BorderLayout.CENTER);
	    setSize(250,400);
	    setVisible(true);

	    try {
		LC.getSingleton().subscribe(cd.name, this);
	    } catch (IOException ex) {
		System.out.println("ex: "+ex);
	    }
	}

	public void messageReceived(String channel, DataInputStream ins)
	{
	    try {
		canbus_list_t cbl = new canbus_list_t(ins);
		rp.rdata.update(cbl);
		//		rp.targets[(int) rt.target_id] = rt;
		rp.repaint();
	    } catch (IOException ex) {
		System.out.println("RadarPluginRaw ex: "+ex);
		return;
	    }
	}
    }

    class RadarData
    {
	static final int MAX_TRACKS = 21;

	class RadarRecord
	{
	    // filtered data
	    double range_rate;
	    double range;
	    double bearing_rad;

	    boolean is_bridge;
	    boolean is_sidelobe;
	    boolean is_fwd_truck;
	    boolean is_mature;

	    int track_combined;
	    int track_count;

	    double angle_left_rad;
	    double angle_right_rad;

	    // unfiltered data
	    double target_power;
	    double unfilt_range_rate;
	    double unfilt_range;
	    double unfilt_bearing_rad;

	}

	RadarRecord records[];

	public RadarData()
	{
	    records = new RadarRecord[MAX_TRACKS];
	    for (int i = 0; i < records.length; i++)
		records[i] = new RadarRecord();
	}

	public void update(canbus_list_t cbl)
	{
	    for (int i = 0; i < cbl.num_messages; i++)
		update(cbl.messages[i]);
	}

	public int u16(byte d[], int offset)
	{
	    int v =  ((d[offset]&0xff)<<8) + (d[offset+1]&0xff);

	    return v;
	}

	public int s16(byte d[], int offset)
	{
	    int v = u16(d, offset);
	    if ((v & 0x8000) != 0)
		v = - (v^0xffff) - 1;

	    return v;
	}

	public void update(canbus_t cb)
	{
	    switch (cb.id)
		{
		case 0x603: 
		    {
			int id = cb.data[0];
			
			synchronized(records[id]) {
			    records[id].range_rate  = s16(cb.data, 3) / 128.0;
			    records[id].range       = s16(cb.data, 1) / 128.0;
			    records[id].bearing_rad = Math.toRadians(-cb.data[5] * .1);
			    records[id].unfilt_bearing_rad = Math.toRadians(-cb.data[6] * .1);
			}
		    }
		    break;

		case 0x604:
		    {
			int id = cb.data[0];
			
			synchronized (records[id]) {
			    records[id].unfilt_range = u16(cb.data, 1) / 128.0;
			    records[id].target_power = u16(cb.data, 3);
			    records[id].track_count = cb.data[5] & 0xff;
			    records[id].is_bridge = (cb.data[5] & (1 << 4)) != 0;
			    records[id].is_sidelobe = (cb.data[5] & (1 << 5)) != 0;
			    records[id].is_fwd_truck = (cb.data[5] & (1 << 6)) != 0;
			    records[id].is_mature = (cb.data[5] & (1 << 7)) != 0;
			    records[id].track_combined = cb.data[6];
			}
		    }
		    break;

		case 0x612:
		    {
			int id = cb.data[0];

			synchronized (records[id]) {
			    records[id].unfilt_range_rate = s16(cb.data, 1) / 128.0;
			    records[id].angle_left_rad = Math.toRadians(cb.data[3]*0.1);
			    records[id].angle_right_rad = Math.toRadians(cb.data[4]*0.1);
			    records[id].unfilt_bearing_rad = Math.toRadians(cb.data[6]*.1);
			}
		    }
		    break;
		}
	}
    }
}
