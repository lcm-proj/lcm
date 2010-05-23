package lcm.spy;

import javax.swing.*;

/** LCSpy managed information about a channel. **/
public class ChannelData
{
    public String      name;
    public Class       cls;
    public long        fingerprint;   // lcm type fingerprint
    public int         row;

    public long        nreceived;
    public int         nerrors;
    public long        last_utime;    // when was last message received?

    public long        min_interval;  // written periodically by HzThread
    public long        max_interval;
    public double      bandwidth;     // bytes per second

    public JInternalFrame viewerFrame;
    public ObjectPanel viewer;

    // below: used by HzThread
    public double      hz;
    public long        hz_last_utime;
    public long        hz_last_nreceived;
    public long        hz_min_interval;
    public long        hz_max_interval;
    public long        hz_bytes;

    public Object      last;         // last received object on this channel.
}
