import os

import _lcm
from _lcm import LCM, LCMSubscription

class Event:
    """Data structure representing a single event in an LCM EventLog
    """

    def __init__ (self, eventnum, timestamp, channel, data):
        self.eventnum = eventnum 
        """Event number"""

        self.timestamp = timestamp 
        """Microseconds since 00:00:00 Jan 1, 1970 UTC marking the time at
        which the message was originally received
        """
        self.channel = channel
        """Channel on which message was received"""

        self.data = data
        """Binary string containing raw message data"""

class EventLog:
    """EventLog is a class for reading and writing LCM log files in Python.

An EventLog opened for reading supports the iterator protocol, with each call
to next() returning the next Event in the log.
    """
    def __init__ (self, path, mode = "r", overwrite = False):
        """EventLog(path, mode = "r", overwrite = False) --> EventLog

        @param path:  Path to the logfile to open
        @param mode:  Open the log for reading ('r') or writing ('w')
        @param overwrite:  If mode is 'w', and the filename at path already 
        exists, then EventLog will truncate and overwrite the file if this
        parameter is set to True.  Otherwise, EventLog refuses to overwrite
        existing files and raises a ValueError.
        """
        if mode not in [ "r", "w" ]:
            raise ValueError ("invalid event log mode")

        if mode == "w" and os.path.exists(path) and not overwrite:
            raise ValueError ("Refusing to overwrite existing log file "
                    "unless overwrite is set to True")

        self.mode = mode

        self.c_eventlog = _lcm.EventLog (path, mode)
        self.f = None

    def seek (self, filepos):
        """seek(filepos) --> None
        @param filepos: byte offset from start of log

        Positions the internal file pointer at the next event that
        starts at or is after byte offset filepos.
        """
        return self.c_eventlog.seek (filepos)

    def size (self):
        """size() --> int

        @return the total size of the log file, in bytes
        """
        return self.c_eventlog.size ()

    def close (self):
        """close() --> None

        Closes the log file.  After an EventLog is closed, it is essentially
        useless
        """
        return self.c_eventlog.close ()

    def write_event (self, utime, channel, data):
        """write_event(utime, channel, data) --> None
        @param utime:    Microseconds since 00:00:00 Jan 1, 1970 UTC
        @param channel:  Channel name corresponding to the event
        @param data:     data bytes

        Writes an event to the log file.  Log file must be openeed in write
        mode.
        """
        return self.c_eventlog.write_event (utime, channel, data)

    def read_next_event (self):
        tup = self.c_eventlog.read_next_event ()
        if not tup: return None
        return Event (*tup)
    
    def __iter__ (self):
        self.c_eventlog.seek (0)
        return self
    
    def next (self):
        next_evt = self.read_next_event ()
        if not next_evt:
            raise StopIteration
        return next_evt

    def tell (self):
        """tell() --> int

        @return the current position of the internal file pointer
        """
        return self.c_eventlog.ftell ()
