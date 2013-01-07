package lcm.logging;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import lcm.lcm.*;
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
    JButton playButton = new JButton("Play ");
    JButton stepButton = new JButton("Step");
    JButton fasterButton;
    //    JButton fasterButton = new JButton(">>");
    JButton slowerButton; // = new JButton("<<");
    JLabel  speedLabel = new JLabel("1.0", JLabel.CENTER);
    double  speed = 1.0;

    static final int POS_MAX = 10000;

    JLabel  posLabel = new JLabel("Event 0");
    JLabel  timeLabel = new JLabel("Time 0.0s");
    JLabel actualSpeedLabel = new JLabel("1.0x");

    JLabel logName = new JLabel("---");

    PlayerThread player = null;

    LCM lcm;

    /** The time of the first event in the current log **/
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
                    qe.execute(LogPlayer.this);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    /** We have events coming from all over the place: the UI, UDP
        events, callbacks from the scrubbers. To keep things sanely
        thread-safe, all of these things simply queue events which are
        processed in-order. doStop, doPlay, doStep, do(Anything) can
        only be called from the queue thread.
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

    Pattern filteredPattern;
    boolean invertFilteredPattern;
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

    JTextField stepChannelField = new JTextField("");

    // faster/slower would be better as semi-log.
    static final double slowerSpeed(double v)
    {
        return v/2;
    }

    static final double fasterSpeed(double v)
    {
        return v*2;
    }

    void setSpeed(double v)
    {
        v = Math.max(1.0/1024, v); // minimum supported speed (0.000977x)
        speedLabel.setText(String.format("%.3f", v));
        speed = v;
    }
    
    void setChannelFilter(String channelFilterRegex)
    {
    	filteredPattern = Pattern.compile(channelFilterRegex);
    }
    void invertChannelFilter(){
    	invertFilteredPattern = true;
    }

    public LogPlayer(String lcmurl) throws IOException
    {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        filteredPattern = null;
        invertFilteredPattern = false;
        
        Insets insets = new Insets(0,0,0,0);
        int row = 0;

        logName.setText("No log loaded");
        logName.setFont(new Font("SansSerif", Font.PLAIN, 10));

        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        posLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        actualSpeedLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

        fasterButton = new JButton(new ImageIcon(makeArrowImage(Color.blue, getBackground(), false)));
        fasterButton.setRolloverIcon(new ImageIcon(makeArrowImage(Color.magenta, getBackground(), false)));
        fasterButton.setPressedIcon(new ImageIcon(makeArrowImage(Color.red, getBackground(), false)));
        fasterButton.setBorderPainted(false);
        fasterButton.setContentAreaFilled(false);
        // Borders keep appearing when the buttons are pressed. Not sure why.
        //fasterButton.setBorder(null); //new javax.swing.border.EmptyBorder(0,0,0,0));

        slowerButton = new JButton(new ImageIcon(makeArrowImage(Color.blue, getBackground(), true)));
        slowerButton.setRolloverIcon(new ImageIcon(makeArrowImage(Color.magenta, getBackground(), true)));
        slowerButton.setPressedIcon(new ImageIcon(makeArrowImage(Color.red, getBackground(), true)));
        slowerButton.setBorderPainted(false);
        slowerButton.setContentAreaFilled(false);

        Font buttonFont = new Font("SansSerif", Font.PLAIN, 10);
        fasterButton.setFont(buttonFont);
        slowerButton.setFont(buttonFont);
        playButton.setFont(buttonFont);
        stepButton.setFont(buttonFont);

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(1,3,0,0));
        p.add(slowerButton);
        p.add(speedLabel);
        p.add(fasterButton);
        //                         x  y    w          h  fillx   filly   anchor     fill        insets,                  ix,   iy
        add(logName,
            new GridBagConstraints(0, row, 1,         1, 0.0,    0.0,    WEST,      NONE,       insets,                  0,    0));

        add(playButton,
            new GridBagConstraints(1, row, 1,         1, 0.0,    0.0,    CENTER,    NONE,       insets,                  0,    0));
        add(stepButton,
            new GridBagConstraints(2, row, 1,         1, 0.0,    0.0,    CENTER,    NONE,       insets,                  0,    0));

        add(p,
            new GridBagConstraints(3, row, REMAINDER, 1, 0.0,    0.0,    EAST,      NONE,       insets,                  0,    0));
        row++;

        add(js,
            new GridBagConstraints(0, row, REMAINDER, 1, 1.0,    0.0,    CENTER,    HORIZONTAL, new Insets(0, 5, 0, 5),  0,    0));
        row++;

        add(timeLabel,
            new GridBagConstraints(0, row, 1,         1, 0.0,    0.0,    WEST,      NONE,       new Insets(0, 10, 0, 0), 0,    0));
        add(actualSpeedLabel,
            new GridBagConstraints(1, row, 1,         1, 0.0,    0.0,    WEST,      NONE,       new Insets(0, 10, 0, 0), 0,    0));
        add(posLabel,
            new GridBagConstraints(3, row, 1,         1, 0.0,    0.0,    EAST,      NONE,       new Insets(0,0, 0, 10),  0,    0));
        row++;

        add(new JScrollPane(filterTable),
            new GridBagConstraints(0, row, REMAINDER, 1, 1.0,    1.0,    CENTER,    BOTH,       new Insets(0,0, 0, 0),   0,    0));
        row++;

        /// spacers

        add(Box.createHorizontalStrut(90),
            new GridBagConstraints(0, row, 1,         1, 0.0,    0.0,    WEST,    NONE,       insets,                  0,    0));
        add(Box.createHorizontalStrut(100),
            new GridBagConstraints(1, 0,   1,         1, 0.0,    0.0,    WEST,    NONE,       insets,                  0,    0));

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
            new GridBagConstraints(0, row, 2, 1, 0.0, 0.0, CENTER, NONE,
                                   insets, 0, 0));

        add(stepPanel,
            new GridBagConstraints(2, row, REMAINDER, 1, 1.0,    0.0,    CENTER,    HORIZONTAL, new Insets(0, 5, 0, 5),  0,    0));
        //	position.addChangeListener(new MyChangeListener());
        setPlaying(false);

        fasterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSpeed(fasterSpeed(speed));
            }});
        slowerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setSpeed(slowerSpeed(speed));
            }});

        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                events.offer(new PlayPauseEvent());
            }});

        stepButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                events.offer(new StepEvent());
            }
	    });

        if(null == lcmurl)
            lcm = new LCM();
        else
            lcm = new LCM(lcmurl);

        logName.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2)
                    openDialog();
            }
	    });
        timeLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                show_absolute_time = ! show_absolute_time;
            }
        });

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

                inlog.seekPositionFraction(p0);
                while (inlog.getPositionFraction() < p1) {
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
                    } else if (cmd.equals("PLAY")) {
                        events.offer(new PlayPauseEvent(true));
                    } else if (cmd.equals("PAUSE")) {
                        events.offer(new PlayPauseEvent(false));
                    } else if (cmd.equals("STEP")) {
                        events.offer(new StepEvent());
                    } else if (cmd.equals("FASTER")) {
                        setSpeed(fasterSpeed(speed));
                    } else if (cmd.equals("SLOWER")) {
                        setSpeed(slowerSpeed(speed));
                    } else if (cmd.startsWith("BACK")) {
                        double seconds = Double.parseDouble(cmd.substring(4));
                        double pos = log.getPositionFraction() - seconds/total_seconds;
                        events.offer(new SeekEvent(pos));
                    } else if (cmd.startsWith("FORWARD")) {
                        double seconds = Double.parseDouble(cmd.substring(7));
                        double pos = log.getPositionFraction() + seconds/total_seconds;
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
            setLog(jfc.getSelectedFile().getPath(), true);
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
    
    Filter addChannelFilter(String channel, boolean enabledByDefault){
        Filter f = new Filter();
        f.inchannel = channel;
        f.outchannel = channel;
        if (filteredPattern== null)
        	f.enabled = enabledByDefault;
        else
        	f.enabled = !(invertFilteredPattern ^ filteredPattern.matcher(channel).matches());
        filterMap.put(f.inchannel, f);
        filters.add(f);
        Collections.sort(filters);
        filterTableModel.fireTableDataChanged();
        return f;
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
                if (filteredPattern != null) //disable if either the saved value, or the filter say it should be disabled
                	f.enabled = f.enabled && !(invertFilteredPattern ^ filteredPattern.matcher(f.inchannel).matches());
                
            }
            if (toks[0].equals("ZOOMFRAC"))
                js.setZoomFraction(Double.parseDouble(toks[1]));
        }

        filterTableModel.fireTableDataChanged();
    }
    
    void populateChannelFilters()
    {
        try {
        	long logStartUTime = -1;
            while (true)
            {
                Log.Event e = log.readNext();               
                if (logStartUTime<0)
                	logStartUTime = e.utime;
                if (e.utime-logStartUTime >30*1e6 ){ //only scan through the first 30sec of the log
                	break;
                }

                Filter f = filterMap.get(e.channel);
                if (f == null) {
                	addChannelFilter(e.channel, !invertFilteredPattern);
                }

            }
        } catch (EOFException ex) {
        	//System.err.println("Breaking at end of log");
        } catch (IOException ex) {
            System.err.println("Exception: "+ex);
        }
        try{
        //rewind to beginning of the log
        	log.seekPositionFraction(0);
        } catch (IOException ex) {
        	System.err.println("Exception: "+ex);
        }
    }

    void setLog(String path, boolean startPlaying) throws IOException
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

            log.seekPositionFraction(.10);
            Log.Event e10 = log.readNext();

            log.seekPositionFraction(.90);
            Log.Event e90 = log.readNext();

            total_seconds = (e90.utime - e10.utime)/1000000.0 / 0.8;
            System.out.printf("Total seconds: %f\n", total_seconds);

            log.seekPositionFraction(0);

        } catch (IOException ex) {
            System.out.println("exception: "+ex);
        }

        loadPreferences(path+".jlp");
        if (startPlaying)
        	doPlay();
        else{
        	populateChannelFilters();
        }
    }

    void setPlaying(boolean t)
    {
        playButton.setText(t ? "Pause" : "Play");

        stepButton.setEnabled(!t);
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
    }

    void doStep()
    {
        if (player != null)
            return;

        player = new PlayerThread(stepChannelField.getText());
        player.start();
    }

    void doSeek(double ratio)
    {
        assert (player == null);

        if (ratio < 0)
            ratio = 0;
        if (ratio > 1)
            ratio = 1;

        try {
            log.seekPositionFraction(ratio);
            Log.Event e = log.readNext();
            log.seekPositionFraction(ratio);
            js.set(log.getPositionFraction());

            lastSystemTime = 0; // reset log-play statistics.
            updateDisplay(e);
        } catch (IOException ex) {
            System.out.println("exception: "+ex);
        }
    }

    long lastEventTime;
    long lastSystemTime;

    void updateDisplay(Log.Event e)
    {
        if (show_absolute_time) {
            java.text.SimpleDateFormat df =
                new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss.S z");
            Date timestamp = new Date(e.utime / 1000);
            timeLabel.setText(df.format(timestamp));
        } else {
            timeLabel.setText(String.format("%.3f s", (e.utime - timeOffset)/1000000.0));
        }
        posLabel.setText(""+e.eventNumber);

        long systemTime = System.currentTimeMillis();
        double dt = (systemTime - lastSystemTime)/1000.0;
        if (dt > 0.5) {
            double actualSpeed = (e.utime - lastEventTime) / 1000000.0 / dt;
            lastEventTime = e.utime;
            lastSystemTime = systemTime;

            actualSpeedLabel.setText(String.format("%.2f x", actualSpeed));
        }
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

            double lastspeed = 0;

            synchronized (sync) {
                setPlaying(true);
            }

            try {
                while (!stopflag)
                {
                    Log.Event e = log.readNext();

                    if (speed != lastspeed) {
                        //System.out.printf("Speed changed. Old %12.6f new %12.6f\n",
                        //                  lastspeed, speed);
                        logOffset = e.utime;
                        localOffset = System.nanoTime()/1000;
                        lastspeed = speed;
                    }

                    long logRelativeTime = (long) (e.utime - logOffset);
                    long now = System.nanoTime();
                    long clockRelativeTime = now/1000 - localOffset;

                    // we don't support playback below a rate of 1/1024x
                    long speed_scale = (long) Math.max(1, (speed*1024.0));
                    long waitTime = (1024*logRelativeTime/speed_scale - clockRelativeTime);

                    long waitms = waitTime / 1000;
                    waitms = Math.max(0, waitms);

                    /*System.out.printf("Now 0x%016X ns, %12.6fx playback, %8d/1024 playback, %10dus rel log time, %10dus, %10dus rel clock, "+
                                      "wait %10dus (%10dms)\n",
                                      now,
                                      speed,
                                      speed_scale,
                                      logRelativeTime,
                                      1024*logRelativeTime/speed_scale,
                                      clockRelativeTime,
                                      waitTime,
                                      waitms);*/

                    last_e_utime = e.utime;

                    try {
                        // We might have a very long wait, but
                        // only sleep for relatively short amounts
                        // of time so that we remain responsive to
                        // seek/speed changes.
                        while (waitms > 0 && !stopflag) {

                            if (waitms > 50) {
                                Thread.sleep(50);
                                waitms -= 50;
                            }
                            else {
                                Thread.sleep(waitms);
                                waitms = 0;
                            }
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
                    	f = addChannelFilter(e.channel, !invertFilteredPattern);
                    }

                    if (f.enabled && f.outchannel.length() > 0)
                        lcm.publish(f.outchannel, e.data, 0, e.data.length);

                    js.set(log.getPositionFraction());

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

    public static void usage()
    {
        System.err.println("usage: lcm-logplayer-gui [options] [log-name]");
        System.err.println("");
        System.err.println("lcm-logplayer-gui is the Lightweight Communications and Marshalling");
        System.err.println("log playback tool.  It provides a graphical user interface for playing logfiles");
        System.err.println("recorded with lcm-logger.  Features include random access, different playback ");
        System.err.println("speeds, channel suppression and remapping, and more.");
        System.err.println("");
        System.err.println("Options:");
        System.err.println("  -l, --lcm-url=URL      Use the specified LCM URL");
        System.err.println("  -p, --paused           Start with the log paused.");
        System.err.println("  -f, --filter=CHAN      Disable channels that match the regex in CHAN.");
        System.err.println("                         (default: \"\")");
        System.err.println("  -v, --invert-filter    Invert the filtering regex. Only enable channels");
        System.err.println("                         matching CHAN.");
        System.err.println("  -h, --help             Shows this help text and exits");
        System.err.println("");
        System.exit(1);
    }

    public static void main(String args[])
    {
        JFrame f;

        // check if the JRE is supplied by gcj, and warn the user if it is.
        if(System.getProperty("java.vendor").indexOf("Free Software Foundation") >= 0) {
            System.err.println("WARNING: Detected gcj. The LCM log player is not known to work well with gcj.");
            System.err.println("         The Sun JRE is recommended.");
        }

        String lcmurl = null;
        String logFile = null;
        boolean startPaused = false;
        int optind;
        String channelFilterRegex = null;
        boolean invertChannelFilter = false;
        for(optind=0; optind<args.length; optind++) {
            String c = args[optind];
            if(c.equals("-h") || c.equals("--help")) {
                usage();
            } else if(c.equals("-l") || c.equals("--lcm-url") || c.startsWith("--lcm-url=")) {
                String optarg = null;
                if(c.startsWith("--lcm-url=")) {
                    optarg=c.split("=")[1];
                } else if(optind < args.length) {
                    optind++;
                    optarg = args[optind];
                }
                if(null == optarg) {
                    usage();
                } else {
                    lcmurl = optarg;
                }
            } else if (c.equals("-p") || c.equals("--paused")){
            	startPaused = true;
            }else if(c.equals("-f") || c.equals("--filter") || c.startsWith("--filter=")) {
            	String optarg = null;
                if(c.startsWith("--filter=")) {
                    optarg=c.split("=")[1];
                } else if(optind < args.length) {
                    optind++;
                    optarg = args[optind];
                }
                if(null == optarg) {
                    usage();
                } else {
                	channelFilterRegex = optarg;
                }
            } else if (c.equals("-v") || c.equals("--invert-filter")){
            	invertChannelFilter = true;
            } else if (c.startsWith("-")){
                usage();
            } else if (logFile!=null) //there should only be 1 non-flag argument
            	usage();
            else {
            	logFile = c;
            }
        }

        try {
            p = new LogPlayer(lcmurl);
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

            if (channelFilterRegex!=null)
            	p.setChannelFilter(channelFilterRegex);
            if (invertChannelFilter)
            	p.invertChannelFilter();

            if (logFile !=null)
                p.setLog(logFile, !startPaused);
            else
                p.openDialog();

        } catch (IOException ex) {
            System.out.println("Exception: "+ex);
        }
    }

    static BufferedImage makeArrowImage(Color fillColor, Color backgroundColor, boolean flip)
    {
        int height = 18, width = 18;
        BufferedImage im = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = im.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //	g.setColor(backgroundColor);

        g.setColor(new Color(0,0,0,0));
        //	g.setColor(new Color(0,0,255,128));
        g.fillRect(0,0,width,height);

        if (flip) {
            g.translate(width-1, height/2);
            g.scale(-height/2, height/2);
        } else {
            g.translate(0, height/2);
            g.scale(height/2, height/2);
        }

        g.setStroke(new BasicStroke(0f));
        GeneralPath gp = new GeneralPath();
        gp.moveTo(0,-1);
        gp.lineTo(1,0);
        gp.lineTo(0,1);
        gp.lineTo(0,-1);

        g.setColor(fillColor);
        g.fill(gp);
        g.setColor(Color.black);
        //	g.draw(gp);

        g.translate(.75, 0);

        g.setColor(fillColor);
        g.fill(gp);
        g.setColor(Color.black);
        //	g.draw(gp);

        return im;
    }

}
