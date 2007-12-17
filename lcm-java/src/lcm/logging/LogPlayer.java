package lcm.logging;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import lcm.lc.*;
import lcm.util.*;

import static java.awt.GridBagConstraints.*;

/** A GUI implementation of a log player allowing seeking. **/
public class LogPlayer extends JComponent
{
    static 
    {
	System.setProperty("java.net.preferIPv4Stack", "true");
	System.out.println("LC: Disabling IPV6 support");
    }

    Log log;
    JButton playButton = new JButton("Play");

    static final int POS_MAX = 10000;
    //    JSlider position = new JSlider(0, POS_MAX, 0);
    JLabel  posLabel = new JLabel("Event 0");
    JLabel  timeLabel = new JLabel("Time 0.0s");
    JLabel logName = new JLabel("---");

    static final int LOG_MAGIC = 0xEDA1DA01;

    PlayerThread player = null;

    LC lc;

    double speeds[] = new double[] { 0, 0.01, 0.1, 0.5, 1, 2, 4, 8, 32};
    JSlider speedSlider = new JSlider(1, speeds.length-1, 4);

    long timeOffset = 0;

    JFileChooser jfc = new JFileChooser();

    String currentLogPath;

    double total_seconds; // an estimate of how many seconds there are in the file

    BlockingQueue<QueuedEvent> events = new LinkedBlockingQueue<QueuedEvent>();

    Object sync = new Object();

    interface QueuedEvent
    {
	public void execute(LogPlayer lp);
    }

    class QueueThread extends Thread
    {
	public void run()
	{
	    while (true) {
		try {
		    QueuedEvent qe = events.take();
		    //		    System.out.printf("%15d : "+qe+"\n", System.currentTimeMillis());
		    qe.execute(LogPlayer.this);
		} catch (InterruptedException ex) {
		}
	    }
	}
    }

    /** We have events coming from all over the place: the UI, UDP
	events, callbacks from the scrubbers. To keep things sanely
	thread-safe, all of these things simply queue events which are
	processed in-order. doStop, doPlay, doStep, doAnything can
	only be called from these events.
    **/
    class PlayPauseEvent implements QueuedEvent
    {
	boolean toggle = false;
	boolean playstate;

	public PlayPauseEvent()
	{
	    this.toggle = true;
	}

	public PlayPauseEvent(boolean playstate)
	{
	    this.playstate = playstate;
	}

	public void execute(LogPlayer lp)
	{
	    if (toggle) 
		{
		    if (player!=null)
			doStop();
		    else
			doPlay();
		}
	    else
		{
		    if (playstate)
			doPlay();
		    else
			doStop();
		}
	}
    }

    // seek, preserving the current play/pause state
    class SeekEvent implements QueuedEvent
    {
	double pos;

	public SeekEvent(double pos)
	{
	    this.pos = pos;
	}

	public void execute(LogPlayer lp)
	{
	    boolean player_was_running = (player != null);

	    if (player_was_running)
		doStop();

	    doSeek(pos);

	    if (player_was_running)
		doPlay();
	}
    }

    class StepEvent implements QueuedEvent
    {
	public void execute(LogPlayer lp)
	{
	    doStep();
	}
    }

    class Filter implements Comparable<Filter>
    {
	String inchannel;
	String outchannel;
	boolean enabled = true;

	public int compareTo(Filter f)
	{
	    return inchannel.compareTo(f.inchannel);
	}
    }

    FilterTableModel filterTableModel = new FilterTableModel();
    ArrayList<Filter> filters = new ArrayList<Filter>();
    // JTable calls upon filterTableModel which calls upon filters...
    // which needs to exist before that!
    JTable filterTable = new JTable(filterTableModel);
    HashMap<String,Filter> filterMap = new HashMap<String,Filter>();

    JTextField inchannel = new JTextField();
    JTextField outchannel = new JTextField();

    JScrubber js = new JScrubber();

    boolean show_absolute_time = false;

    JButton stepButton = new JButton("Step");
    JTextField stepChannelField = new JTextField("");

    public LogPlayer() throws IOException
    {
	setLayout(new GridBagLayout());
	GridBagConstraints gbc = new GridBagConstraints();

	Insets insets = new Insets(0,0,0,0);
	int row = 0;

	//	speedSlider.setMaximumSize(new Dimension(100,50));
	Dictionary speedSliderDictionary = new Hashtable();
	for (int i = 0; i < speeds.length; i++) {
	    JLabel l = new JLabel(speeds[i]+"x");
	    l.setFont(new Font("SansSerif", Font.PLAIN, 10));
	    speedSliderDictionary.put(i, l);
	}

	logName.setText("No log loaded");
	logName.setFont(new Font("SansSerif", Font.PLAIN, 10));

	speedSlider.setLabelTable(speedSliderDictionary);
	speedSlider.setMinorTickSpacing(1);
	speedSlider.setMajorTickSpacing(1);
	speedSlider.setPaintTicks(true);
	speedSlider.setPaintLabels(true);
	speedSlider.setSnapToTicks(true);
	speedSlider.setMinimumSize(new Dimension(200,40));
	speedSlider.setMaximumSize(new Dimension(400, 40));
	timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
	posLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

	//                         x  y    w          h  fillx   filly   anchor     fill        insets,                  ix,   iy
	add(logName,
	    new GridBagConstraints(0, row, 3,         1, 0.0,    0.0,    WEST,      NONE,       insets,                  0,    0));
	add(speedSlider,
	    new GridBagConstraints(3, row, REMAINDER, 1, 1.0,    0.0,    EAST,      HORIZONTAL, insets,                  0,    0));
	row++;

	add(playButton,           
	    new GridBagConstraints(0, row, 1,         1, 0.0,    0.0,    CENTER,    NONE,       insets,                  0,    0));

	add(js,
	    new GridBagConstraints(2, row, REMAINDER, 1, 1.0,    0.0,    CENTER,    HORIZONTAL, new Insets(0, 5, 0, 5),  0,    0));
	row++;

	add(timeLabel,            
	    new GridBagConstraints(2, row, 1,         1, 0.0,    0.0,    WEST,      NONE,       new Insets(0, 10, 0, 0), 0,    0));
	add(posLabel,             
	    new GridBagConstraints(3, row, 1,         1, 0.0,    0.0,    EAST,      NONE,       new Insets(0,0, 0, 10),  0,    0));
	row++;

	add(new JScrollPane(filterTable),
	    new GridBagConstraints(0, row, REMAINDER, 1, 1.0,    1.0,    CENTER,    BOTH,       new Insets(0,0, 0, 0),   0,    0));
	row++;

	add(Box.createHorizontalStrut(90),
	    new GridBagConstraints(0, row, 1,         1, 0.0,    0.0,    CENTER,    NONE,       insets,                  0,    0));

	///////////////////////////
	row++;
	JPanel stepPanel = new JPanel(new BorderLayout());
	stepPanel.add(new JLabel("Channel Prefix: "), BorderLayout.WEST);
	stepPanel.add(stepChannelField, BorderLayout.CENTER);

        JButton toggleAllButton = new JButton("Toggle Selected");
        toggleAllButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
                    int[] rowIndices = filterTable.getSelectedRows();
                    for (int i = 0; i < rowIndices.length; ++i) {
                        Filter f = filters.get(rowIndices[i]);
                        f.enabled = !f.enabled;
                    }
                    filterTableModel.fireTableDataChanged();
                    for (int i = 0; i < rowIndices.length; ++i) {
                        filterTable.addRowSelectionInterval(rowIndices[i],
                                                            rowIndices[i]);
                    }
		}});
        add(toggleAllButton,
            new GridBagConstraints(0, row, 1, 1, 0.0, 0.0, CENTER, NONE,
                                   insets, 0, 0));

	add(stepButton,
	    new GridBagConstraints(1, row, 1,         1, 0.0,    0.0,    CENTER,    NONE,       insets,                  0,    0));
	add(stepPanel,
	    new GridBagConstraints(2, row, REMAINDER, 1, 1.0,    0.0,    CENTER,    HORIZONTAL, new Insets(0, 5, 0, 5),  0,    0));
	//	position.addChangeListener(new MyChangeListener());
	setPlaying(false);

	playButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    events.offer(new PlayPauseEvent());
		}});
 
	stepButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    events.offer(new StepEvent());
		}
	    });
				     
	lc = LC.getSingleton();
	lc.stopReader();

	logName.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    if (e.getClickCount()==2)
			openDialog();
		}
	    });
	timeLabel.addMouseListener(new MouseAdapter() {
	    public void mouseClicked(MouseEvent e) {
		show_absolute_time = ! show_absolute_time;
//		updateDisplay();
	    }
	});

	/*	position.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    doStop();
		}
	    });
	*/
	js.set(0);
	js.addScrubberListener(new MyScrubberListener());

	filterTable.getColumnModel().getColumn(2).setMaxWidth(50);
	playButton.setEnabled(false);

	new UDPThread().start();
	new QueueThread().start();
    }

    class MyScrubberListener implements JScrubberListener
    {
	public void scrubberMovedByUser(JScrubber js, double x)
	{
	    events.offer(new SeekEvent(x));
	}

	public void scrubberPassedRepeat(JScrubber js, double from_pos, double to_pos)
	{
	    events.offer(new SeekEvent(to_pos));
	}

	public void scrubberExportRegion(JScrubber js, double p0, double p1)
	{
	    System.out.printf("Export %15f %15f\n", p0, p1);

            String outpath = getOutputFileFromDialog();
            if (outpath == null)
              return;

	    System.out.println("Exporting to "+outpath);
	    try {
		Log inlog = new Log(log.getPath(), "r");
		Log outlog = new Log(outpath, "rw");

		inlog.seekPercent(p0);
		while (inlog.getPercent() < p1) {
		    Log.Event e = inlog.readNext();
                    Filter f = filterMap.get(e.channel);
                    if (f != null && f.enabled)
                        outlog.write(e);
		}
		inlog.close();
		outlog.close();
		System.out.printf("Done!\n");

	    } catch (IOException ex) {
		System.out.println("Exception: "+ex);
	    }
	}
    }

    // remote control via UDP packets
    class UDPThread extends Thread
    {
	public void run()
	{
	    DatagramSocket sock;
	    DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);

	    try {
		sock = new DatagramSocket(53261, Inet4Address.getByName("127.0.0.1"));
	    } catch (SocketException ex) {
		System.out.println("Exception: "+ex);
		return;
	    } catch (UnknownHostException ex) {
		System.out.println("Exception: "+ex);
		return;
	    }

	    while (true) 
		{
		    try {
			sock.receive(packet);
			String cmd = new String(packet.getData(), 0, packet.getLength());
			cmd = cmd.trim();

			if (cmd.equals("PLAYPAUSETOGGLE")) {
			    events.offer(new PlayPauseEvent());
			} else if (cmd.equals("STEP")) {
			    events.offer(new StepEvent());
			} else if (cmd.equals("FASTER")) {
			    speedSlider.setValue(Math.min(speedSlider.getValue()+1, speedSlider.getMaximum()));
			} else if (cmd.equals("SLOWER")) {
			    speedSlider.setValue(Math.max(speedSlider.getValue()-1, 0));
			} else if (cmd.startsWith("BACK")) {
			    double seconds = Double.parseDouble(cmd.substring(4));
			    double pos = log.getPercent() - seconds/total_seconds;
			    events.offer(new SeekEvent(pos));
			} else if (cmd.startsWith("FORWARD")) {
			    double seconds = Double.parseDouble(cmd.substring(7));
			    double pos = log.getPercent() + seconds/total_seconds;
			    events.offer(new SeekEvent(pos));
			} else {
			    System.out.println("Unknown remote command: "+cmd);
			}
		    } catch (IOException ex) {
		    }
		}
	}
    }

    String getOutputFileFromDialog()
    {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION)
            return null;
        return chooser.getSelectedFile().getPath();
    }

    void openDialog()
    {
	doStop();
	int res = jfc.showOpenDialog(this);
	if (res != JFileChooser.APPROVE_OPTION)
	    return;
	
	try {
	    setLog(jfc.getSelectedFile().getPath());
	} catch (IOException ex) {
	    System.out.println("Exception: "+ex);
	}
    }

    void savePreferences() throws IOException
    {
	if (currentLogPath == null)
	    return;

	String path = currentLogPath+".jlp";

	FileWriter fouts = new FileWriter(path);
	BufferedWriter outs = new BufferedWriter(fouts);

	ArrayList<JScrubber.Bookmark> bookmarks = js.getBookmarks();
	for (JScrubber.Bookmark b : bookmarks) {
	    String type = "PLAIN";
	    if (b.type == JScrubber.BOOKMARK_LREPEAT)
		type = "LREPEAT";
	    if (b.type == JScrubber.BOOKMARK_RREPEAT)
		type = "RREPEAT";
	    outs.write("BOOKMARK "+type+" "+b.position+"\n");
	}

	outs.write("ZOOMFRAC "+js.getZoomFraction()+"\n");

	for (Filter f : filters) {
	    outs.write("CHANNEL " + f.inchannel+" "+f.outchannel+" "+f.enabled+"\n");
	}
	outs.close();
	fouts.close();
    }

    void loadPreferences(String path) throws IOException
    {
	BufferedReader ins;

	js.clearBookmarks();
	filterMap.clear();
	filters.clear();

	try {
	    ins = new BufferedReader(new FileReader(path));
	} catch (FileNotFoundException ex) {
	    // no error; just a no-op
	    return;
	}

	String line;
	while ((line = ins.readLine()) != null) {
	    String toks[] = line.split("\\s+");

	    if (toks[0].equals("BOOKMARK") && toks.length==3) {
		int type = JScrubber.BOOKMARK_PLAIN;
		if (toks[1].equals("RREPEAT"))
		    type = JScrubber.BOOKMARK_RREPEAT;
		if (toks[1].equals("LREPEAT"))
		    type = JScrubber.BOOKMARK_LREPEAT;

		js.addBookmark(type, Double.parseDouble(toks[2]));
	    }
	    if (toks[0].equals("CHANNEL") && toks.length==4) {
		Filter f = filterMap.get(toks[1]);
		if (f == null) {
		    f = new Filter();
		    f.inchannel = toks[1];
		    f.outchannel = toks[1];
		    filterMap.put(toks[1], f);
		    filters.add(f);
		}
		f.outchannel = toks[2];
		f.enabled = Boolean.parseBoolean(toks[3]);
	    }
	    if (toks[0].equals("ZOOMFRAC"))
		js.setZoomFraction(Double.parseDouble(toks[1]));
	}

	filterTableModel.fireTableDataChanged();
    }

    void setLog(String path) throws IOException
    {
	if (currentLogPath != null)
	    savePreferences();

	currentLogPath = path;
	log = new Log(path, "r");
	logName.setText(new File(path).getName());

	try {
	    Log.Event e = log.readNext();
	    timeOffset = e.utime;
	    playButton.setEnabled(true);

	    log.seekPercent(.10);
	    Log.Event e10 = log.readNext();

	    log.seekPercent(.90);
	    Log.Event e90 = log.readNext();

	    total_seconds = (e90.utime - e10.utime)/1000000.0 / 0.8;
	    System.out.printf("Total seconds: %f\n", total_seconds);

	    log.seekPercent(0);

	} catch (IOException ex) {
	    System.out.println("exception: "+ex);
	}

	loadPreferences(path+".jlp");
 	doPlay();
   }

    void setPlaying(boolean t)
    {
	playButton.setText(t ? "Pause" : "Play");
    }

    // the player can stop automatically on error or EOF; we thus have
    // a potential race condition between auto-stops and requested
    // stops.
    //
    // We protect these two with 'sync'.
    void doStop()
    {
	PlayerThread pptr;

	synchronized(sync) {
	    if (player == null)
		return;

	    pptr = player;
	    pptr.requestStop();
	}

	try {
	    pptr.join();
	} catch (InterruptedException ex) {
	    System.out.println("Exception: "+ex);
	}
    }

    void doPlay()
    {
	if (player != null)
	    return;
	
	player = new PlayerThread();
	player.start();
	
	setPlaying(true);
    }

    void doStep()
    {
	if (player != null)
	    return;
	
	player = new PlayerThread(stepChannelField.getText());
	player.start();
	
	setPlaying(true);
    }

    void doSeek(double ratio)
    {
	assert (player == null);

	if (ratio < 0)
	    ratio = 0;
	if (ratio > 1)
	    ratio = 1;

	try {
	    log.seekPercent(ratio);
	    Log.Event e = log.readNext();
	    log.seekPercent(ratio);
	    js.set(log.getPercent());

	    updateDisplay(e);
	} catch (IOException ex) {
	    System.out.println("exception: "+ex);
	}
    }

    void updateDisplay(Log.Event e)
    {
	if(show_absolute_time) {
	    java.text.SimpleDateFormat df = 
		new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss.S z");
	    Date timestamp = new Date(e.utime / 1000);
	    timeLabel.setText(df.format(timestamp));
	} else {
	    timeLabel.setText(String.format("%.3f s", (e.utime - timeOffset)/1000000.0));
	}
	posLabel.setText(""+e.eventNumber);
    }

    class PlayerThread extends Thread
    {
	boolean stopflag = false;
	String stopOnChannel;

	public PlayerThread()
	{
	}

	public PlayerThread(String stopOnChannel)
	{
	    this.stopOnChannel = stopOnChannel;
	}

	public void requestStop()
	{
	    stopflag = true;
	}

	public void run()
	{
	    long lastTime = 0;
	    long lastDisplayTime = 0;
	    long localOffset = 0;
	    long logOffset = 0;
	    long last_e_utime = 0;

	    int lastspeedidx = -1;

	    try {
		while (!stopflag) 
		    {
			Log.Event e;
			
			e = log.readNext();

			int speedidx = speedSlider.getValue();

			if (speedidx!=lastspeedidx) {
			    logOffset = e.utime;
			    localOffset = System.nanoTime()/1000;
			    lastspeedidx = speedidx;
			    //			    System.out.printf("%15d : Adjusting index\n", System.currentTimeMillis());
			}

			long logRelativeTime = (long) (e.utime - logOffset);
			long clockRelativeTime = System.nanoTime()/1000 - localOffset;

			long speed_scale = (long) (speeds[speedidx]*16.0);
			long waitTime = logRelativeTime - speed_scale*clockRelativeTime/16;

			long maxWaitTime = 250000;

			if (waitTime > maxWaitTime) {
			    System.out.printf("Hole in log from %15d - %15d (%15.3f seconds)\n",
					      last_e_utime, e.utime, (e.utime - last_e_utime)/1000000.0);
			    waitTime = maxWaitTime;
			    lastspeedidx = -1;
			}
			last_e_utime = e.utime;

			try {
			    if (waitTime > 0) {
				long sleepns = (long) (waitTime*1000);
				int sleepms = (int) (sleepns/1000000);
				sleepns -= sleepms*1000000;
				Thread.sleep(sleepms, (int) sleepns);
			    }
			} catch (InterruptedException ex) {
			    System.out.println("Interrupted");
			}

			// During the sleep, other threads might have
			// run that have asked us to stop (a
			// jscrubber.userset in particular); recheck
			// the stop flag before we blindly proceed.
			// (This ameliorates but does not solve an
			// intrinsic race condition)
			if (stopflag)
			    break;

			Filter f = filterMap.get(e.channel);
			if (f == null) {
			    f = new Filter();
			    f.inchannel = e.channel;
			    f.outchannel = e.channel;
			    filterMap.put(f.inchannel, f);
			    filters.add(f);
			    Collections.sort(filters);
			    filterTableModel.fireTableDataChanged();
			}

			if (f.enabled && f.outchannel.length() > 0)
			    lc.publish(f.outchannel, e.data, 0, e.data.length);

			js.set(log.getPercent());

			// redraw labels no faster than 10 Hz
			long curTime = System.currentTimeMillis();
			if (curTime - lastDisplayTime > 100) {
			    updateDisplay(e);
			    lastDisplayTime = curTime;
			}

			if (stopOnChannel != null && e.channel.startsWith(stopOnChannel)) {
			    stopflag = true;
			    break;
			}
		    }
	    } catch (EOFException ex) {
		stopflag = true;
	    } catch (IOException ex) {
		System.out.println("Exception: "+ex);
		stopflag = true;
	    }

	    synchronized (sync) {
		setPlaying(false);
		player = null;
	    }
	}

    }

    class FilterTableModel extends AbstractTableModel
    {
	public int getRowCount()
	{
	    return filters.size();
	}

	public int getColumnCount()
	{
	    return 3;
	}

	public String getColumnName(int column)
	{
	    switch(column)
		{
		case 0:
		    return "Log channel";
		case 1:
		    return "Playback channel";
		case 2:
		    return "Enable";
		}
	    return "??";
	}

	public Class getColumnClass(int column)
	{
	    switch (column)
		{
		case 0:
		case 1:
		    return String.class;
		case 2:
		    return Boolean.class;
		}
	    
	    return null;
	}

	public Object getValueAt(int row, int column)
	{
	    Filter f = filters.get(row);
	    switch(column)
		{
		case 0:
		    return f.inchannel;
		case 1:
		    return f.outchannel;
		case 2:
		    return f.enabled;
		}
	    return "??";
	}

	public boolean isCellEditable(int row, int column)
	{
	    return (column==1) || (column==2);
	}

	public void setValueAt(Object v, int row, int column)
	{
	    Filter f = filters.get(row);

	    if (column == 1)
		f.outchannel = (String) v;
	    if (column == 2)
		f.enabled = (Boolean) v;
	}
    }

    static LogPlayer p;

    public static void main(String args[])
    {
	JFrame f;

	try {
	    p = new LogPlayer();
	    f = new JFrame("LogPlayer");
	    f.setLayout(new BorderLayout());
	    f.add(p, BorderLayout.CENTER);
	    f.pack();
	    f.setSize(f.getWidth(),300);
	    f.setVisible(true);

	    f.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			try {
			    p.savePreferences();
			} catch (IOException ex) {
			    System.out.println("Couldn't save preferences: "+ex);
			}
			System.exit(0);
		    }});
	    
	    if (args.length > 0)
		p.setLog(args[0]);
	    else
		p.openDialog();

	} catch (IOException ex) {
	    System.out.println("Exception: "+ex);
	}
    }
}
