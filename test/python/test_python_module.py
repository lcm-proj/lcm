import lcm
from os import path
from tempfile import TemporaryDirectory

CHANNEL = 'test/channel'
DATA = bytes(3)
RECEIVED_MESSAGE = False


def my_handler(channel, data):
    assert channel == CHANNEL
    assert data == DATA
    global RECEIVED_MESSAGE
    RECEIVED_MESSAGE = True


def test_lcm():
    lc = lcm.LCM()
    subscription = lc.subscribe(CHANNEL, my_handler)
    lc.publish(CHANNEL, DATA)
    lc.handle()
    assert RECEIVED_MESSAGE
    lc.unsubscribe(subscription)


def test_event():
    event_number = 0
    timestamp = 1
    event = lcm.Event(event_number, timestamp, CHANNEL, DATA)
    assert event.eventnum == event_number
    assert event.timestamp == timestamp
    assert event.channel == CHANNEL
    assert event.data == DATA


def test_event_log():
    with TemporaryDirectory() as temp_dir:
        filename = f'{temp_dir}/test.log'
        # Create a log, write an event, and close it.
        log = lcm.EventLog(filename, 'w', True)
        assert log.mode == 'w'
        assert log.size() == 0
        assert log.tell() == 0
        event = lcm.Event(0, 1, CHANNEL, DATA)
        log.write_event(event.timestamp, event.channel, event.data)
        log.close()
        assert path.isfile(filename)

        # Check log that was written.
        log = lcm.EventLog(filename)
        assert log.mode == 'r'
        assert log.size() > 0
        assert log.tell() == 0
        read_event = log.read_next_event()
        # End of log
        assert log.tell() == log.size()
        assert event.eventnum == read_event.eventnum
        assert event.timestamp == read_event.timestamp
        assert event.channel == read_event.channel
        assert event.data == read_event.data
        log.close()


if __name__ == '__main__':
    test_lcm()
    test_event()
    test_event_log()
