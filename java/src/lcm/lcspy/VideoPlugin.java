package lcm.lcspy;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.*;

import lcm.lc.*;
import lcm.lcmtypes.*;
import lcm.util.*;

/** A plugin for viewing video_t data **/
public class VideoPlugin implements LCSpyPlugin
{
    public boolean canHandle(Class cls)
    {
	if (cls.equals(image_t.class))
	    return true;

	return false;
    }

    class MyAction extends AbstractAction
    {
	ChannelData cd;
	JDesktopPane jdp;

	public MyAction(JDesktopPane jdp, ChannelData cd)
	{
	    super("Video Viewer");
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

    class Viewer extends JInternalFrame implements LCSubscriber
    {
	ChannelData cd;
	JImage ji;

	public Viewer(ChannelData cd)
	{
	    super("Video: "+cd.name, true, true);
	    this.cd = cd;

	    setLayout(new BorderLayout());
	    ji = new JImage(null, true);
	    add(ji, BorderLayout.CENTER);
	    setSize(400,300);
	    setVisible(true);

	    LC.getSingleton().subscribe(cd.name, this);
	}

	BufferedImage handleRAW(image_t v)
	{
	    BufferedImage bi = new BufferedImage(v.width, v.height, BufferedImage.TYPE_INT_RGB);

	    for (int y = 0; y < v.height; y++) {
		for (int x = 0; x < v.width; x++) {
		    bi.setRGB(x, y, grayToRGB(v.image[x+y*v.stride]));
		}
	    }

	    return bi;
	}

	BufferedImage handleJPEG(image_t v)
	{
	    try {
		return ImageIO.read(new ByteArrayInputStream(v.image));
	    } catch (IOException ex) {
		return null;
	    }
	}

	public void handleImage(image_t v)
	{
	    if (v.width==0 || v.height==0)
		return;

	    BufferedImage bi = null;

	    switch (v.pixelformat) 
		{
		case 1196444237:
		    bi = handleJPEG(v);
		    break;

		default:
		    bi = handleRAW(v);
		    break;

		}

	    ji.setImage(bi);
	}
	
	final int grayToRGB(byte v)
	{
	    int g = v&0xff;
	    return (g<<16)|(g<<8)|g;
	}

	public void messageReceived(String channel, DataInputStream ins)
	{
	    try {
		image_t v = new image_t(ins);
		handleImage(v);
	    } catch (IOException ex) {
		System.out.println("ex: "+ex);
		return;
	    }
	}
    }
}
