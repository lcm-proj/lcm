# LCM -> Parquet -> Spark SQL

To use this demo:
1) Drop into a Dockerized shell:
```
$ ./tool.py --shell
```

2) Inside that Dockerized shell, run the demo:
```
$ python demo.py
```

You should eventually see something like the
[sample output](#sample-output) copied below.

## Why Parquet?

### Background: ROS Bags were a Good Idea

ROS Bags offer the same abstraction as LCM Log files: one can record
a series of timestamped binary blobs (messages) to a file and organize
them by "topic" (or "channel").  While the abstraction is a perfect fit
for on-robot uses, the implemenation of the "log file" has immense impact
real-world off-robot use.

ROS Bags use a rather complicated format that is utterly terrible for spinning
disks-- simply opening a rosbag (`rosbag.Bag()` ctor) initiates a lengthy 
series of random seeks all over the file.  Opening a ROS Bag file on a spinning
disk is an order of magnitude slower than opening said file on an SSD.
Moreover, without detailed knowledge of the ROS Bag encoding, it is impossible
to seek into a file and read a single message.  

ROS Bag files are optimized for (1) fast serial replay and (2) mitigating the
costs of lossless compression on said replay.  ROS Bag files also have an index
component to aid in filtering (by time and topic).  Most importantly, 
ROS Bags are not very composable with systems outside of ROS, there is low
maintenance on the ROS codebase, and ROS only provides Bag implementations in
C++ and Python (and the Python impl is very very slow).

But the ROS Bag design sounds like a good idea, at least in theory, right?
Yes!  But!  *Parquet* offers nearly all the same features, yet in a
widely-adopted, exensible, actively maintained, and modern form.  Moreover, 
Parquet was designed to support generic SQL query workloads, including 
e.g. optimized `GROUP BY`, `COUNT`s, and `SUM`s.  ROS Bags were not designed
with these uses in mind.

### But wait, this demo is about LCM!

In contrast to ROS, LCM does *not* use a hyper-complicated log file encoding.
LCM log files use a very simple (and `mmap`-friendly) linear encoding.
(However, it's important to note the open source C++ implementation of LCM
requires a POSIX filesystem...).  While this format is more friendly to
developers, it still lacks some key features of Parquet, including:
  * Dictionary compression (e.g. repeats of the same raw string are stored
      only once in the file)
  * Lossless file compression (e.g. lz4 / gzip)
  * Embedding of message schemas into log files

There is one other *very* key feature of Parquet that LCM (and ROS)
are missing: Parquet is friendly to object-stores (e.g S3 / GCS / Swift).
In particular, by reading only the index portion of a Parquet file, one can
compute the byte offsets in the file that are necessary to read specific
rows / cells.  Thus reads that only touch a subset of the file (e.g. via
a "push-down" filter) can skip reading part of the file entirely.  This feature
is critical for use in an object store (like S3) because one must read files
(objects) in chunks over the network, so "seeks" are extremely expensive, and
fetching the entire object (block) can be prohibitive.

### Suggestion: convert LCM to Parquet for analytics

This demo presents a proof of concept for tranforming LCM Log files to Parquet.
Through Parquet, we're able to observe a 5x space reduction over LCM Log files
(due to *not gzip* but *dictionary compression*), and we're able to run
complicated queries over 8 million rows in seconds on a laptop.

The solution has the following main perks:
 * The demo includes synthesis of LCM Log files that may realistically
    represent the logs generated from a fleet of robots.
 * The demo uses Spark for arbitrary horizontal scaling.
 * The demo uses Spark's built-in schema deduction, so no LCM message file
    schemas need be translated (or ported) to Spark / Parquet.
 * Spark SQL provides a viable SQL Engine for running typical OLAP
    queries (included in the demo).

Areas for improvement:
 * The reading of LCM log files can likely be made much faster and more
    flexible (as discussed below).
 * One might wish to translate LCM message schemas to Parquet (or some
    other format).  Possible solutons discussed below.
 * This demo does *not* use lossless file compression (e.g. `gzip`) nor
    a production-oriented SQL Engine (e.g. Presto or Athena).  Discussion
    below.


## Discussion

### How can we read LCM Log files efficiently?

The LCM log file format is (in theory) `mmap` friendly, but the reference
C++ implementation (and thus also the Python impl) requires a POSIX filesystem
interface to log data.  If one stores LCM log files in an object store
(e.g. S3), or on a fleet of robots, then accessing messages will be tricky.

NB: LCM does include a fairly clean snippet of code
[in the Java impl](lcm-java/lcm/logging/Log.java) that may be ported in order
to read LCM Events from an arbitrary byte buffer.

#### My LCM Logs live on a fleet of robots!

Note that Spark supports interop with message queues such as Kafka, and
Databricks offers
[Spark interop for SQS](https://docs.databricks.com/spark/latest/structured-streaming/sqs.html).

One could convert portions of this demo to build a back-end service
that accepts LCM log data Events and transforms them into Parquet data.
Spark Streaming may be useful here as it can help scale horizontally
the data transformation load.

#### My LCM Logs live on S3!

##### I don't want to write a Hadoop InputFormat

Use [S3 Fuse](https://github.com/s3fs-fuse/s3fs-fuse) to mount your S3
bucket on all Spark Workers. S3 Fuse will transparently download and cache
(on the local filesystem, not in memory!) the log files as needed.  While
there is tremendous latency associated with this solution, and this solution
does *not* advertise data locality to Spark, the solution nevertheless scales
quite effectively in practice (and has relatively low devops cost).  

##### I do want to write a Hadoop InputFormat

Using the LCM [Java impl](lcm-java/lcm/logging/Log.java), one should be able
to write a Hadoop InputFormat that would allow Spark (and Hadoop, Hive, and
friends) to read LCM Log files natively.  Using and deploying said code
is not the easiest, but this approach allows composition with arbitrary 
Hadoop-compatible data stores as well as substantial performance gains.
[Elephant Bird](https://github.com/twitter/elephant-bird) has some examples
that are relevant to writing bespoke `InputFormat`s.

#### My LCM Logs live on GCS/GFS!

See the S3Fuse recommendation above, and try [Alluxio](https://www.alluxio.io/)
as a FUSE client to GCS/GFS.  In practice, the other GCS FUSE clients
(including Google's own) have been buggy and/or inoperable.



### Isn't Spark SQL a relatively slow SQL Engine?

While Spark SQL is relatively efficient for this demo (our query suite
takes 16 seconds to run over 8.4m rows), one would likely see an order
of magnitude faster performance from a more traditional SQL Database
such as Postgres or MySQL.  Postgres and MySQL have difficulty scaling
to tables containing 1TB+ of data without extensions (e.g. 
Citus, MemSQL, MySQL merge tables).  Moreover, cloud instances running
these databases can be very expensive.

The purose of this demo is to showcase SQL performance against data in
Parquet files, which can be stored in an object store (e.g. S3 or GFS).
Compute needed to execute SQL queries against these files can then be
spent as needed (and scalably) through the SQL engines of Spark, Hive, 
Presto, Athena, etc.


### Wait, why is my data in Parquet format so much smaller vs LCM?!

Parquet leverages dictionary compression: repeated values for a cell in
a column can often be 'compressed' using a method similar to run-length
encoding.  SQL engines that read Parquet (e.g. Spark) are aware of and 
leverage this compression to elide some CPU and memory costs of executing
queries.  Dictionary compression also saves disk space.  In this demo,
some LCM message fields never change (e.g. the 'frame' member of the pose
message) and thus can be compressed.  In the demo we often see a ~5x space
reduction due to dictionary compression alone.

Parquet *additionally* supports lossless block-level compression, e.g.
`lz4`, `snappy`, and `gzip`.  For simplicity (and reduction of 3rd party
library dependencies), we do not use lossless compression in this demo.
 

### Do I need to partition my Parquet files in any magic way to ensure fast reads?

This demo leverages time- and channel-based partitioning to ensure that
SQL reads are highly parallelizeable.  Reads to specific channels thus
never touch data in unneeded channels.  This feature is especially
important if the log data includes large binary blobs (e.g. images).  The
demo as-is does not generate / use data with *large* images, but you can
explore this aspect if you'd like-- see the comment at the top
of `demo.py`.


### How realistic is the synthetic data?

Probably not very relaistic, but there are several knobs embedded in the code.

The heuristic for determining a "crash" is half-baked (or not even).  For
the demo, we assume a crash is fast and dramatic change in robot pose.  We
show how to detect simple changes using an SQL Window Function.  While the
logic interpeting the pose change is not realistic, here the demo
serves to show the efficiency of Window Functions on Parquet, even
with Spark SQL.  For production, one might want to write a more complicated
utility using e.g. pyspark window functions and/or UDFs.


# Sample Output

```
2019-05-13 13:32:06,233 demo 27048 : Going to convert 200 paths ...
2019-05-13 13:32:06,233 demo 27048 : ... in 9 chunks ...
2019-05-13 13:32:22,460 worker 27048 : Chunk 0: Writing 1009975 rows to /tmp/flights_parquet ...
/usr/local/lib/python2.7/dist-packages/pyspark/sql/session.py:366: UserWarning: Using RDD of dict to inferSchema is deprecated. Use pyspark.sql.Row instead
  warnings.warn("Using RDD of dict to inferSchema is deprecated. "
2019-05-13 13:33:10,432 worker 27048 : ... done                                 
2019-05-13 13:33:27,584 worker 27048 : Chunk 1: Writing 1058100 rows to /tmp/flights_parquet ...
2019-05-13 13:34:19,534 worker 27048 : ... done                                 
2019-05-13 13:34:35,945 worker 27048 : Chunk 2: Writing 940789 rows to /tmp/flights_parquet ...
2019-05-13 13:35:21,939 worker 27048 : ... done                                 
2019-05-13 13:35:39,883 worker 27048 : Chunk 3: Writing 1013308 rows to /tmp/flights_parquet ...
2019-05-13 13:36:30,276 worker 27048 : ... done                                 
2019-05-13 13:36:47,080 worker 27048 : Chunk 4: Writing 1021791 rows to /tmp/flights_parquet ...
2019-05-13 13:37:35,252 worker 27048 : ... done                                 
2019-05-13 13:37:51,621 worker 27048 : Chunk 5: Writing 988937 rows to /tmp/flights_parquet ...
2019-05-13 13:38:39,659 worker 27048 : ... done                                 
2019-05-13 13:38:55,775 worker 27048 : Chunk 6: Writing 1069845 rows to /tmp/flights_parquet ...
2019-05-13 13:39:45,949 worker 27048 : ... done                                 
2019-05-13 13:40:03,143 worker 27048 : Chunk 7: Writing 993994 rows to /tmp/flights_parquet ...
2019-05-13 13:40:52,533 worker 27048 : ... done                                 
2019-05-13 13:40:57,917 worker 27048 : Chunk 8: Writing 337933 rows to /tmp/flights_parquet ...
2019-05-13 13:41:15,302 worker 27048 : ... done                                 
2019-05-13 13:41:15,324 worker 27048 : Empty Queue! Stopping
2019-05-13 13:41:17,332 demo 27048 : Done converting LCM logs to parquet (in 9.18053078254 minutes)
2019-05-13 13:41:17,333 demo 27048 : LCM log gross size: 1.821781128 GBytes (200 files)
2019-05-13 13:41:17,946 demo 27048 : Parquet files gross size: 0.398125959 GBytes (8794 files)
2019-05-13 13:41:17,947 demo 27048 : Loading flight data from /tmp/flights_parquet


Message Stats
+----------+-------------+----------+                                           
|total_rows|total_flights|num_robots|
+----------+-------------+----------+
|   8434672|          200|        17|
+----------+-------------+----------+



Channel Stats
+---------+----------+                                                          
|  channel|total_rows|
+---------+----------+
|     pose|   3546412|
| camera_0|   1779800|
| camera_1|   1626800|
|camera_4k|    841920|
|    track|    332900|
|     meta|    306840|
+---------+----------+



Crash Statistics
+-------------+----------------+------------------+                             
|total_crashes|crashes_per_hour|crashes_per_flight|
+-------------+----------------+------------------+
|         3584|       25.038532|             17.92|
+-------------+----------------+------------------+



Subject Loss Statistics
+--------------------+-----------------------+-----------------+                
|total_subject_losses|subject_losses_per_hour|losses_per_flight|
+--------------------+-----------------------+-----------------+
|                  60|               0.534068|              0.3|
+--------------------+-----------------------+-----------------+



Video Clip Count Statistics
+-----------+---------------+
|total_clips|clips_per_month|
+-----------+---------------+
|     462720| 2327042.853737|
+-----------+---------------+

2019-05-13 13:41:35,233 demo 27048 : Computed stats in 16.7663180828 sec
```