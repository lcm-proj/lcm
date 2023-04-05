"""
How to tinker with this demo:
 * To adjust the size of the fleet, change `N_ROBOTS`
 * To consider log data that has embedded images, see the comment
     "Embedded images!"
 * See `gen_flight()` for the logic behind generating a single LCM Log file.
 * Toggle `N_CPUS` if you'd like to limit the number of CPUs used.  Note that
    if you run the demo on machine with a large number of CPUs
    (e.g. an m5.metal ), you may need to expand the JVM heap size and/or
    adjust JVM pointer compression.
 * Want to try snappy or gzip with Parquet?  Search for 
    "compression='uncompressed'" in the code.
"""
import os
import sys

# NB: LCM demo code must be on PYTHONPATH
# sys.path.append('/opt/lcm/examples/python')
# We include it in the docker container for now.

from contextlib import contextmanager
import random
import time

import exlcm
import lcm


###############################################################################
### General Utils

import multiprocessing
N_CPUS = multiprocessing.cpu_count()

# Basic logger just for demo.  
_LOGS = {}
def create_log(name='demo'):
  global _LOGS
  if name not in _LOGS:
    import logging
    LOG_FORMAT = "%(asctime)s\t%(name)-4s %(process)d : %(message)s"
    log = logging.getLogger(name)
    log.setLevel(logging.INFO)
    console_handler = logging.StreamHandler(sys.stderr)
    console_handler.setFormatter(logging.Formatter(LOG_FORMAT))
    log.addHandler(console_handler)
    _LOGS[name] = log
  return _LOGS[name]
log = create_log()

def mkdir_p(path):
  import errno
  try:
    os.makedirs(path)
  except OSError as exc:
    if exc.errno == errno.EEXIST and os.path.isdir(path):
      pass
    else:
      raise

def all_files_recursive(root_dir, pattern='**/*'):
  try:
    import pathlib
  except ImportError:
    import pathlib2 as pathlib
    # TODO use six?

  return [
    str(path) # pathlib uses PosixPath thingies ...
    for path in pathlib.Path(root_dir).rglob(pattern)
    if path.is_file()
  ]

###############################################################################
### Utils for Creating LCM Logs

MAX_FLIGHT_TIME_SEC = 300

## General Utils
def gen_t(st, max_sec=MAX_FLIGHT_TIME_SEC, min_sec=0, hz=10):
  for t_sec in range(int(random.random() * max_sec) + min_sec):
    for h in range(hz):
      yield st + t_sec + h * (1. / hz)

N_ROBOTS = 100
ROBOTS = ['r_%s' % str(i) for i in range(int(N_ROBOTS))]

## Meta
def gen_meta_seq(st, robot):
  import uuid
  build = str(random.random)[:10]
  flight_id = str(uuid.uuid4())
  clips = int(3 * random.random())
  for t in gen_t(st, min_sec=1):
    msg = exlcm.meta_t()
    msg.timestamp = int(t * 1e6)
    msg.robot = robot
    msg.build = build
    msg.clips = clips # Hacky but whatev
    msg.flight_id = flight_id
    yield msg

## Tracks
def gen_track(t, c):
  msg = exlcm.track_t()
  msg.timestamp = int(t * 1e6)
  msg.confidence = float(c)

  # Try to fool compression
  msg.position = (
    random.random(), random.random(), random.random())
  msg.orientation = (
    random.random(), random.random(), random.random(), random.random())
  msg.cls = "moof" + str(random.random())[:2]
  msg.velocity = random.random()
  return msg

def gen_track_seq(st, include_drop=False):
  t = st
  for t in gen_t(st, hz=10):
    yield gen_track(t, 1)
  if include_drop:
    yield gen_track(t + 0.1, 0)


## Pose
def gen_smooth_pose(st):
  frame = 'world'
  for t in gen_t(st, hz=100):
    msg = exlcm.pose_t()
    msg.timestamp = int(t * 1e6)
    msg.frame = frame

    msg.position = (
      random.random(), random.random(), random.random())
    msg.orientation = (
      random.random(), random.random(), random.random(), random.random())
    yield msg

def gen_crash_pose(st):
  msg = None
  for msg in gen_smooth_pose(st):
    yield msg
  
  # Generate the crash!
  # Well there should be a big change in accel / jerk, but we'll pretend
  # position is adequate.
  msg.position = (
    msg.position[0] * 1000,
    msg.position[1] * 1000,
    msg.position[2] * 1000,
  )
  yield msg

## Images
N_CAMERAS = 2
CAMERAS = ['camera_%s' % str(i) for i in range(N_CAMERAS)]
CAMERAS.append('camera_4k')
_CAM_TO_IMAGE = {}
def gen_images(st, cam):
  hz = 30 if cam == 'camera_4k' else 50
  for t in gen_t(st, hz=hz):
    msg = exlcm.image_t()
    msg.timestamp = int(t * 1e6)
    msg.camera = cam

    # Embedded images!
    # Omit realistic-sized image buffers for speed of demo; we also don't
    # need to demo image I/O right now.  
    if cam == 'camera_4k':
      msg.width = 10#3840
      msg.height = 20#2178
      msg.depth = 3
    else:
      msg.width = 5#500
      msg.height = 10#200
      msg.depth = 3
    
    global _CAM_TO_IMAGE
    if cam not in _CAM_TO_IMAGE:
      import numpy as np
      data = np.random.rand(
                    msg.height,
                    msg.width,
                    msg.depth)
      data = data.astype(np.uint8)
      _CAM_TO_IMAGE[cam] = data
    msg.data = _CAM_TO_IMAGE[cam]
    # print cam
    yield msg

## Flights
def gen_flight():
  robot = random.choice(ROBOTS)
  st = time.time() + int(7 * 24 * 3600 * random.random())
  
  has_lost = random.random() < 0.2
  has_crash = random.random() < 0.1
  chan_to_gen = {
    'track': gen_track_seq(st, include_drop=has_lost),
    'pose': gen_crash_pose(st) if has_crash else gen_smooth_pose(st),
    'meta': gen_meta_seq(st, robot),
  }
  for cam in CAMERAS:
    chan_to_gen[cam] = gen_images(st, cam)
  
  def to_events(chan, gen):
    for msg in gen:
      yield (msg.timestamp, chan, msg)
  
  import heapq
  args = (to_events(chan, gen) for chan, gen in chan_to_gen.iteritems())
  for ev in heapq.merge(*args):
    yield ev

def write_flight(path):
  # log = create_log('wf') # Need this instantiated in spark worker for demo
  l = lcm.EventLog(path, "w", overwrite=True)
  for i, (t, chan, msg) in enumerate(gen_flight()):
    l.write_event(t, chan, msg.encode())
    # if i % 100 == 0:
    #   log.info('wrote %s messages to %s' % (i, path))
  l.close()
  # log.info("Wrote to %s" % path)



###############################################################################
### Creating / Reading LCM Logs

def create_flights(spark, n=2 * len(ROBOTS), outdir='/tmp/flights_raw'):
  if os.path.exists(outdir):
    log.info("Using existing raw flight logs at %s" % outdir)
    return
  
  log.info("\n\n\nCreating %s flights in %s\n\n\n" % (n, outdir))
  mkdir_p(outdir)

  # Create `n` flight log files in parallel via Spark
  jobs = spark.sparkContext.parallelize(range(n), numSlices=n)
  jobs.foreach(
    lambda i: write_flight(os.path.join(outdir, 'log_%s.lcm' % i)))
  
  log.info("\n\n\nCreated flights in %s\n\n\n" % outdir)


CHANNEL_TO_MESSAGE_TYPE = {
  'meta': exlcm.meta_t,
  'pose': exlcm.pose_t,
  'track': exlcm.track_t,
}
CHANNEL_TO_MESSAGE_TYPE.update(
  (cam, exlcm.image_t)
  for cam in CAMERAS
) 

def get_meta_attr(lcm_log, attr):
  for event in lcm_log:
    if event.channel == 'meta':
      m = exlcm.meta_t.decode(event.data)
      return getattr(m, attr)
  return None

def log_to_rows(path):
  l = lcm.EventLog(path, "r")

  robot = get_meta_attr(l, 'robot')
  flight_id = get_meta_attr(l, 'flight_id')
  if robot is None:
    log.info("Skipping log file %s no robot!" % path)
    return

  for i, event in enumerate(l):
    assert event.channel in CHANNEL_TO_MESSAGE_TYPE
    msg_cls = CHANNEL_TO_MESSAGE_TYPE[event.channel]
    msg = msg_cls.decode(event.data)
    
    # Extract data fields for the message
    msg_as_dict = dict(
      (k, getattr(msg, k))
      for k in msg.__slots__)
    
    # For demo purposes, we'll shard by minute
    minute = int(event.timestamp / (60 * 1e6))

    msg_as_dict.update({
      'robot': robot,
      'flight_id': flight_id,
      'minute': minute,
      'channel': event.channel,
      'path': path,
    })

    # Rather than use pyspark.sql.Row, which assumes a uniform schema,
    # we'll have Spark auto-deduce table schema from Python dicts
    # (at the cost of some CPU).
    yield msg_as_dict

def logs_to_parquet(spark, indir, outdir, logs_per_chunk=2*N_CPUS):
  if os.path.exists(outdir):
    log.info("Using pre-existing parquet files in %s" % outdir)
    return

  # Get all the LCM log file paths to process
  paths = all_files_recursive(indir, pattern='*.lcm')

  # Since we don't want to load all the LCM log files into memory
  # (and that wouldn't really help because LCM requires using
  # a filesystem API to iterate over a log file), we'll convert the
  # log files in groups.  We have two immediate options here:
  #   1) Iterate over groups of files in a python `for` loop
  #   2) Use Spark Streaming to process a 'stream' of files
  # While approach #1 is much simpler, approach #2 is slightly more 
  # realistic for production, and gives us an opportunity to demo
  # Spark Streaming.  Thus we use approach #2 below.
  log.info("Going to convert %s paths ..." % len(paths))
  path_chunks = [
    paths[i:i+logs_per_chunk]
    for i in range(0, len(paths), logs_per_chunk)
  ]
  log.info("... in %s chunks ..." % len(path_chunks))

  from pyspark.streaming import StreamingContext
  ssc = StreamingContext(spark.sparkContext, 1)

  rdds = [spark.sparkContext.parallelize(chunk) for chunk in path_chunks]
  stream = ssc.queueStream(rdds)

  c = [0]
  def save_parquet(t, rdd):
    log = create_log('worker')
    rdd = rdd.flatMap(log_to_rows)
    if rdd.isEmpty():
      log.info('Empty Queue! Stopping')
      ssc.stop(stopSparkContext=False, stopGraceFully=False)
        # NB: yes this is the official way to trigger a stop ...
      return
    
    rdd = rdd.cache()
    log.info('Chunk %s: Writing %s rows to %s ...' % (
      c[0], rdd.count(), outdir))
    spark.createDataFrame(rdd, samplingRatio=1).write.parquet(
      outdir,
      mode='append',
      partitionBy=(
        'robot',
        'minute',
        'channel'),
      compression='uncompressed')
    log.info('... done')
    c[0] += 1
  
  start = time.time()
  
  stream.foreachRDD(save_parquet)
  ssc.start()
  ssc.awaitTermination()
    # NB: Above call blocks until the job thread stops the streaming context
  
  end = time.time()
  
  def gross_size_GB(paths):
    return sum(os.stat(p).st_size for p in paths) * 1e-9
  
  log.info(
    "Done converting LCM logs to parquet (in %s minutes)" % (
      (end - start) / 60))
  log.info("LCM log gross size: %s GBytes (%s files)" % 
    (gross_size_GB(paths), len(paths)))
  pq_files = all_files_recursive(outdir)
  log.info("Parquet files gross size: %s GBytes (%s files)" % 
    (gross_size_GB(pq_files), len(pq_files)))


###############################################################################
### Compute Metrics!

def compute_and_show_metrics(spark, flight_data_path):
  log.info("Loading flight data from %s" % flight_data_path)
  spark.read.parquet(flight_data_path).createOrReplaceTempView('flights')

  QUERIES = (
    ('Message Stats',
    """
      SELECT
        COUNT(*) AS total_rows,
        COUNT(DISTINCT flight_id) AS total_flights,
        COUNT(DISTINCT robot) AS num_robots
      FROM flights
    """),
    
    ('Channel Stats',
    """
      SELECT
        channel,
        COUNT(*) AS total_rows
      FROM flights
      GROUP BY channel
      ORDER BY total_rows DESC
    """),

    ('Crash Statistics',
      # We'll use a window function to compute crashes, which involve a 
      # dramatic change in pose between time steps
    """
    SELECT 
      COUNT(t_sec) AS total_crashes,
      COUNT(t_sec) /
        ( (MAX(t_sec) - MIN(t_sec)) / (60 * 60) )
        AS crashes_per_hour,
      COUNT(t_sec) / COUNT(DISTINCT flight_id) AS crashes_per_flight
    FROM (
        SELECT
          flight_id,
          timestamp / 1e6 AS t_sec,
          position._1 AS cur_position,
          ( 
            LAG(position, 1) OVER (
                  PARTITION BY flight_id ORDER BY timestamp DESC) 
          )._1 AS prev_position
        FROM flights
        WHERE
          channel = 'pose'
      )
    WHERE
      cur_position >= 500 * prev_position
    """),

    ('Subject Loss Statistics',
      # NB: a subject loss occurs when we have a `track` with zero confidence
    """
    SELECT 
      COUNT(DISTINCT t_sec) AS total_subject_losses,
      COUNT(DISTINCT t_sec) /
        ( (MAX(t_sec) - MIN(t_sec)) / (60 * 60) )
        AS subject_losses_per_hour,
      COUNT(DISTINCT t_sec) / 
        (SELECT COUNT(DISTINCT flight_id) FROM flights)
        AS losses_per_flight
    FROM (
        SELECT
          flight_id,
          timestamp / 1e6 AS t_sec
        FROM flights
        WHERE
          channel = 'track' AND confidence = 0
      )
    """),

    ('Video Clip Count Statistics',
      # NB: raw clip counts are just in `meta` messages
    """
    SELECT 
      SUM(clips) AS total_clips,
      SUM(clips) /
        ( (MAX(t_sec) - MIN(t_sec)) / (30 * 24 * 60 * 60) )
        AS clips_per_month
    FROM (
        SELECT
          timestamp / 1e6 AS t_sec,
          clips
        FROM flights
        WHERE channel = 'meta'
      )
    """),
  )

  start = time.time()
  for qname, query in QUERIES:
    print
    print
    print qname
    spark.sql(query).show()
  end = time.time()
  log.info("Computed stats in %s sec" % (end - start))

###############################################################################
### Spark Utils

@contextmanager
def spark_session():
  import pyspark
  builder = pyspark.sql.SparkSession.builder
  builder = builder.master(os.environ.get('SPARK_MASTER', 'local[%s]' % N_CPUS))
  spark = builder.getOrCreate()
  spark.sparkContext.setLogLevel('ERROR')
  yield spark


###############################################################################
### Demo Driver

def create_parser():
  import argparse
  parser = argparse.ArgumentParser(
                    description=(
                      "Run a demo that: (1) creates synthetic LCM logs, "
                      "(2) converts the logs to Parquet, and "
                      "(3) runs sample SQL queries to compute stats"))
  parser.add_argument(
    '--flights-raw', default='/tmp/flights_raw',
    help='Save raw flight LCM logs in this dir [default %(default)s]')
  parser.add_argument(
    '--flights-parquet', default='/tmp/flights_parquet',
    help='Save converted flight logs in this dir [default %(default)s]')
  return parser

def main(opts):
  with spark_session() as spark:
    create_flights(spark, outdir=opts['flights_raw'])
    logs_to_parquet(spark, opts['flights_raw'], opts['flights_parquet'])
    compute_and_show_metrics(spark, opts['flights_parquet'])

if __name__ == '__main__':
  parser = create_parser()
  args = parser.parse_args()
  main(args.__dict__)
