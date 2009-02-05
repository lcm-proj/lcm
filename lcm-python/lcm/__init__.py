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
        """Microseconds since the epoch marking the time at which the message
        was originally received
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
        return self.c_eventlog.seek (filepos)

    def size (self):
        return self.c_eventlog.size ()

    def close (self):
        return self.c_eventlog.close ()

    def write_event (self, utime, channel, data):
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
        return self.c_eventlog.ftell ()

EVENTLOG_MAGIC = 0xEDA1DA01L

class _PyEventLog:
    def __init__ (self, path, mode = "r", overwrite = False):
        if mode not in [ "r", "w" ]:
            raise ValueError ("invalid event log mode")

        if mode == "w" and os.path.exists(path) and not overwrite:
            raise ValueError ("Refusing to overwrite existing log file "
                    "unless overwrite is set to True")
        self.mode = mode
        self.c_eventlog = None
        self.f = file (path, mode)

    def seek (self, filepos):
        if self.mode != "r":
            raise RuntimeError ("seek not supported in write mode")
        self.f.seek (filepos)

    def size (self):
        if self.mode == "r":
            curpos = self.f.tell ()
            self.f.seek (0, 2)
            result = self.f.tell ()
            self.f.seek (curpos, 0)
            return result
        else:
            return self.f.tell ()

    def close (self):
        self.f.close ()

    def write_event (self, utime, channel, data):
        raise NotImplementedError ()

    def read_next_event (self):
        if self.mode != "r":
            raise RuntimeError("read not supported in write mode")
        testmagic = 0
        while testmagic != MAGIC:
            byte = self.f.read (1)
            if len(byte) == 0: return None

            r = struct.unpack ("B", byte)[0]
            testmagic = ((testmagic << 8) & 0xffffffff) | r

        fmt = ">qqii"
        header = self.f.read (struct.calcsize (fmt))
        eventnum, timestamp, channellen, datalen = \
                struct.unpack (fmt, header)
        channel = self.f.read (channellen)
        if len(channel) != channellen: return None
        data = self.f.read (datalen)
        if len(data) != datalen: return None

        return Event (eventnum, timestamp, channel, data)
    
    def __iter__ (self):
        self.f.seek (0)
        return self
    
    def next (self):
        next_evt = self.read_next_event ()
        if not next_evt:
            raise StopIteration
        return next_evt

    def tell (self):
        return self.f.tell ()
