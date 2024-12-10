import os
import sys
import subprocess

# Attempt to be backwards compatible
if sys.version_info >= (3, 6):
    from os import PathLike
elif sys.version_info >= (3, 4):
    from pathlib import Path as PathLike
else:
    # All we do to the Path is call str on it, so this is a harmless fallback.
    PathLike = str

if sys.version_info >= (3, 5):
    import typing
    # Not including `bytes` like other APIs in the union as the native interface does not accept a byte string.
    # Makes more sense for the user to .decode(encoding) the bytes than us.
    PathArgument = typing.Union[str, PathLike]
else:
    PathArgument = object


from lcm import _lcm
from lcm._lcm import LCM, LCMSubscription

class Event(object):
    """Data structure representing a single event in an LCM EventLog
    """

    def __init__ (self, eventnum, timestamp, channel, data):
        """
        Initializer
        """

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

class EventLog(object):
    """EventLog is a class for reading and writing LCM log files in Python.

An EventLog opened for reading supports the iterator protocol, with each call
to next() returning the next L{Event<lcm.Event>} in the log.

@undocumented: __iter__
    """
    def __init__ (self, path:PathArgument, mode = "r", overwrite = False):
        """
        Initializer

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

        if isinstance(path, PathLike):
            path = str(path)

        self.c_eventlog = _lcm.EventLog (path, mode)
        self.f = None

    def seek (self, filepos):
        """
        Positions the internal file pointer at the next event that
        starts at or is after byte offset filepos.

        @param filepos: byte offset from start of log

        @return: None
        """
        return self.c_eventlog.seek (filepos)

    def seek_to_timestamp (self, timestamp):
        """Seek (approximately) to a particular timestamp.

        @param eventlog The log file object
        @param ts Timestamp of the target event in the log file.

        @return: None
        """
        return self.c_eventlog.seek_to_timestamp (timestamp)

    def size (self):
        """
        @return: the total size of the log file, in bytes
        @rtype: int
        """
        return self.c_eventlog.size ()

    def close (self):
        """
        Closes the log file.  After an EventLog is closed, it is essentially
        useless

        @return: None
        """
        return self.c_eventlog.close ()

    def write_event (self, utime, channel, data):
        """
        Writes an event to the log file.  Log file must be openeed in write
        mode.

        @param utime:    Microseconds since 00:00:00 Jan 1, 1970 UTC
        @param channel:  Channel name corresponding to the event
        @param data:     data bytes

        @return: None
        """
        return self.c_eventlog.write_event (utime, channel, data)

    def read_next_event (self):
        """
        @return: the next L{Event<lcm.Event>} in the log file.
        @rtype: L{Event<lcm.Event>}
        """
        tup = self.c_eventlog.read_next_event ()
        if not tup: return None
        return Event (*tup)

    def __iter__ (self):
        self.c_eventlog.seek (0)
        return self

    def __next__ (self):
        """
        Python 2.6 - 3.x version for iterators

        @rtype: L{Event<lcm.Event>}
        """
        return self.next()

    def next (self):
        """
        @rtype: L{Event<lcm.Event>}
        """
        next_evt = self.read_next_event ()
        if not next_evt:
            raise StopIteration
        return next_evt

    def tell (self):
        """
        @return: the current position of the internal file pointer, in bytes
        @rtype: int
        """
        return self.c_eventlog.ftell ()

LCM_BIN_DIR = os.path.join(os.path.dirname(__file__), '..', 'bin')

def run_script(name: str, args) -> int:
    try:
        return subprocess.call([os.path.join(LCM_BIN_DIR, name), *args], close_fds=False)
    except KeyboardInterrupt:
        return 0

def run_example():
    raise SystemExit(run_script('lcm-example', sys.argv[1:]))

def run_gen():
    raise SystemExit(run_script('lcm-gen', sys.argv[1:]))

def run_logfilter():
    raise SystemExit(run_script('lcm-logfilter', sys.argv[1:]))

def run_logger():
    raise SystemExit(run_script('lcm-logger', sys.argv[1:]))

def run_logplayer():
    raise SystemExit(run_script('lcm-logplayer', sys.argv[1:]))

def run_logplayer_gui():
    raise SystemExit(run_script('lcm-logplayer-gui', sys.argv[1:]))

def run_sink():
    raise SystemExit(run_script('lcm-sink', sys.argv[1:]))

def run_source():
    raise SystemExit(run_script('lcm-source', sys.argv[1:]))

def run_spy():
    raise SystemExit(run_script('lcm-spy', sys.argv[1:]))

def run_tester():
    raise SystemExit(run_script('lcm-tester', sys.argv[1:]))
