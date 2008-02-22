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

import lcm.util.*;
import java.lang.reflect.*;

import lcm.lcm.*;

/** Spy main class. **/
public class Spy
{
    JFrame       jf;
    LCM          lcm;
    JDesktopPane jdp;

    LCMHandlerDatabase handlers;

    HashMap<String,  ChannelData> channelMap = new HashMap<String, ChannelData>();
    ArrayList<ChannelData>        channelList = new ArrayList<ChannelData>();

    ChannelTableModel _channelTableModel = new ChannelTableModel();
    TableSorter  channelTableModel = new TableSorter(_channelTableModel);
    JTable channelTable = new JTable(channelTableModel);

    ArrayList<SpyPlugin> plugins = new ArrayList<SpyPlugin>();

    JButton clearButton = new JButton("Clear");

    public Spy() throws IOException
    {
	jf = new JFrame("LC Spy");
	jdp = new JDesktopPane();
	jdp.setBackground(new Color(0, 0, 160));
	jf.setLayout(new BorderLayout());
	jf.add(jdp, BorderLayout.CENTER);

	jf.setSize(1024, 768);
	jf.setVisible(true);

	//	sortedChannelTableModel.addMouseListenerToHeaderInTable(channelTable);
	channelTableModel.setTableHeader(channelTable.getTableHeader());
	channelTableModel.setSortingStatus(0, TableSorter.ASCENDING);

	handlers = new LCMHandlerDatabase();

	TableColumnModel tcm = channelTable.getColumnModel();
	tcm.getColumn(0).setMinWidth(140);
	tcm.getColumn(1).setMinWidth(140);
        tcm.getColumn(2).setMaxWidth(100);
        tcm.getColumn(3).setMaxWidth(100);
        tcm.getColumn(4).setMaxWidth(100);
        tcm.getColumn(5).setMaxWidth(100);
        tcm.getColumn(6).setMaxWidth(100);

	JInternalFrame jif = new JInternalFrame("Channels", true);
	jif.setLayout(new BorderLayout());
	jif.add(channelTable.getTableHeader(), BorderLayout.PAGE_START);
	// XXX weird bug, if clearButton is added after JScrollPane, we get an error.
	jif.add(clearButton, BorderLayout.SOUTH);
	jif.add(new JScrollPane(channelTable), BorderLayout.CENTER);

	jif.setSize(800,600);
	jif.setVisible(true);
	jdp.add(jif);

	lcm = new LCM();
	lcm.subscribeAll(new MySubscriber());

	new HzThread().start();

	clearButton.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent e)
		{
		    channelMap.clear();
		    channelList.clear();
		    channelTableModel.fireTableDataChanged();
		}
	    });
	
	channelTable.addMouseListener(new MouseAdapter()
	    {
		public void mouseClicked(MouseEvent e)
		{
		    int mods=e.getModifiersEx();
		    
		    if (e.getButton()==3)
			{
			    showPopupMenu(e);
			}
		    else if (e.getClickCount() == 2)
			{
			    Point p = e.getPoint();
			    int row = rowAtPoint(p);

			    ChannelData cd = channelList.get(row);
			    boolean got_one = false;
			    for (SpyPlugin plugin : plugins)
				{
				    if (!got_one && plugin.canHandle(cd.fingerprint)) {
					plugin.getAction(jdp, cd).actionPerformed(null);
					got_one = true;
				    }
				}

			    if (!got_one)
				createViewer(channelList.get(row));
			}
		}
	    });

	jf.addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    System.out.println("Spy quitting");
		    System.exit(0);
		}
	    });

	ClassDiscoverer.findClasses(new PluginClassVisitor());
	System.out.println("Found "+plugins.size()+" plugins");
	for (SpyPlugin plugin : plugins) {
	    System.out.println(" "+plugin);
	}
    }
    
    class PluginClassVisitor implements ClassDiscoverer.ClassVisitor
    {
	public void classFound(String jar, Class cls)
	{
	    Class interfaces[] = cls.getInterfaces();
	    for (Class iface : interfaces) {
 		if (iface.equals(SpyPlugin.class)) {
		    try {
			Constructor c = cls.getConstructor(new Class[0]);
			SpyPlugin plugin = (SpyPlugin) c.newInstance(new Object[0]);
			plugins.add(plugin);
		    } catch (Exception ex) {
			System.out.println("ex: "+ex);
		    }
		}

	    }

	}
    }

    void createViewer(ChannelData cd)
    {
	if (cd.viewerFrame != null && !cd.viewerFrame.isVisible())
	    {
		cd.viewerFrame.dispose();
		cd.viewer = null;
	    }
			    
	if (cd.viewer == null) {
	    cd.viewerFrame = new JInternalFrame(cd.name, true, true);

	    cd.viewer = new ObjectPanel(cd.name, cd.cls, null);

	    //	cd.viewer = new ObjectViewer(cd.name, cd.cls, null);
	    cd.viewerFrame.setLayout(new BorderLayout());
	    cd.viewerFrame.add(new JScrollPane(cd.viewer), BorderLayout.CENTER);

	    jdp.add(cd.viewerFrame);
	    cd.viewerFrame.setSize(440,400);
	    cd.viewerFrame.setVisible(true);
	} else {
	    cd.viewerFrame.setVisible(true);
	}
    }

    static final long utime_now()
    {
	return System.nanoTime()/1000;
    }

    class ChannelTableModel extends AbstractTableModel
    {
	public int getColumnCount()
	{
	    return 8;
	}

	public int getRowCount()
	{
	    return channelList.size();
	}

	public Object getValueAt(int row, int col)
	{
	    ChannelData cd = channelList.get(row);
	    if (cd == null)
		return "";

	    switch (col)
		{
		case 0:
		    return cd.name;
		case 1:
		    if (cd.cls == null)
			return String.format("?? %016x", cd.fingerprint);

		    String s = cd.cls.getName();
		    return s.substring(s.lastIndexOf('.')+1);

		case 2:
		    return ""+cd.nreceived;
		case 3:
		    return String.format("%6.2f", cd.hz);
		case 4:
		    return String.format("%6.2f ms",1000.0/cd.hz); // cd.max_interval/1000.0);
		case 5:
		    return String.format("%6.2f ms",(cd.max_interval - cd.min_interval)/1000.0);
		case 6:
		    return String.format("%6.2f KB/s", (cd.bandwidth/1024.0));
		case 7:
		    return ""+cd.nerrors;
		}
	    return "???";
	}

	public String getColumnName(int col)
	{
	    switch (col) 
		{
		case 0:
		    return "Channel";
		case 1:
		    return "Type";
		case 2:
		    return "Num Msgs";
		case 3:
		    return "Hz";
		case 4:
		    return "1/Hz";
		case 5:
		    return "Jitter";
		case 6:
		    return "Bandwidth";
		case 7:
		    return "Errors";
		}
	    return "???";
	}

    }

    class MySubscriber implements LCMSubscriber
    {
	public void messageReceived(LCM lcm, String channel, DataInputStream dins)
	{
	    Object o = null;
	    ChannelData cd = channelMap.get(channel);
	    int msg_size = 0;

	    try {
		msg_size = dins.available();
		long fingerprint = dins.readLong();
		dins.reset();

		Class cls = handlers.getClassByFingerprint(fingerprint);

		if (cd == null) {
		    cd = new ChannelData();
		    cd.name = channel;
		    cd.cls = cls;
		    cd.fingerprint = fingerprint;
		    cd.row = channelList.size();

		    synchronized(channelList) {
			channelMap.put(channel, cd);
			channelList.add(cd);
			_channelTableModel.fireTableDataChanged();
		    }

		} else {
		    if (cls != null && cd.cls != null && !cd.cls.equals(cls)) {
			System.out.println("WARNING: Class changed for channel "+channel);
			cd.nerrors++;
		    }
		}

		long utime = utime_now();
		long interval = utime - cd.last_utime;
		cd.hz_min_interval = Math.min(cd.hz_min_interval, interval);
		cd.hz_max_interval = Math.max(cd.hz_max_interval, interval);
		cd.hz_bytes += msg_size;
		cd.last_utime = utime;
		
		cd.nreceived++;

		o = cd.cls.getConstructor(DataInputStream.class).newInstance(dins);

		if (cd.viewer != null) 
		    cd.viewer.setObject(o);

	    } catch (NullPointerException ex) {
		cd.nerrors++;
	    } catch (IOException ex) {
		cd.nerrors++;
		System.out.println("Spy.messageReceived ex: "+ex);
	    } catch (NoSuchMethodException ex) {
		cd.nerrors++;
		System.out.println("Spy.messageReceived ex: "+ex);
	    } catch (InstantiationException ex) {
		cd.nerrors++;
		System.out.println("Spy.messageReceived ex: "+ex);
	    } catch (IllegalAccessException ex) {
		cd.nerrors++;
		System.out.println("Spy.messageReceived ex: "+ex);
	    } catch (InvocationTargetException ex) {
		cd.nerrors++;
		// these are almost always spurious
		//System.out.println("ex: "+ex+"..."+ex.getTargetException());
	    }
	    
	}
    }

    class HzThread extends Thread
    {
	public HzThread()
	{
	    setDaemon(true);
	}

	public void run()
	{
	    while (true) 
		{
		    long utime = utime_now();

		    synchronized(channelList)
			{
			    for (ChannelData cd : channelList)
				{
				    long diff_recv = cd.nreceived - cd.hz_last_nreceived;
				    cd.hz_last_nreceived = cd.nreceived;
				    long dutime = utime - cd.hz_last_utime;
				    cd.hz_last_utime = utime;
				    
				    cd.hz = diff_recv / (dutime/1000000.0);
				    
				    cd.min_interval = cd.hz_min_interval;
				    cd.max_interval = cd.hz_max_interval;
				    cd.hz_min_interval = 9999;
				    cd.hz_max_interval = 0;
				    cd.bandwidth = cd.hz_bytes / (dutime/1000000.0);
				    cd.hz_bytes = 0;
				}
			}

		    int selrow = channelTable.getSelectedRow();
		    channelTableModel.fireTableDataChanged();
		    if (selrow >= 0)
			channelTable.setRowSelectionInterval(selrow, selrow);

		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException ex) {
		    }
		}
	}

    }

    class DefaultViewer extends AbstractAction
    {
	ChannelData cd;

	public DefaultViewer(ChannelData cd)
	{
	    super("Structure Viewer...");
	    this.cd = cd;
	}

	public void actionPerformed(ActionEvent e)
	{
	    createViewer(cd);
	}
    }

    int rowAtPoint(Point p)
    {
	int physicalRow = channelTable.rowAtPoint(p);

	return channelTableModel.modelIndex(physicalRow);
    }

    public void showPopupMenu(MouseEvent e)
    {
	Point p = e.getPoint();
	int row = rowAtPoint(p);
	ChannelData cd = channelList.get(row);
	JPopupMenu jm = new JPopupMenu("Viewers");

	int prow = channelTable.rowAtPoint(p);
	channelTable.setRowSelectionInterval(prow, prow);

	jm.add(new DefaultViewer(cd));

	if (cd.cls != null)
	    {
		for (SpyPlugin plugin : plugins)
		    {
			if (plugin.canHandle(cd.fingerprint))
			    jm.add(plugin.getAction(jdp, cd));
		    }
	    }

	jm.show(channelTable, e.getX(), e.getY());
    }

    public static void main(String args[])
    {
	try {
	    new Spy();
	} catch (IOException ex) {
	    System.out.println(ex);
	}
    }
}
