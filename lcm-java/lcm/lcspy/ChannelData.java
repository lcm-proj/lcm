package lcm.lcspy;

import javax.swing.*;

/** LCSpy managed information about a channel. **/
public class ChannelData
{
    String      name;
    Class       cls;
    int         row;
    
    long        nreceived;
    int         nerrors;
    long        last_utime;    // when was last message received?
    
    long        min_interval;  // written periodically by HzThread
    long        max_interval;
    double      bandwidth;     // bytes per second
    
    JInternalFrame viewerFrame;
    ObjectPanel viewer;
    
    // below: used by HzThread
    double      hz;
    long        hz_last_utime;
    long        hz_last_nreceived;
    long        hz_min_interval;
    long        hz_max_interval;
    long        hz_bytes;
}
