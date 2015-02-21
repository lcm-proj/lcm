UDP Multicast Setup {#multicast_setup}
====
\brief Getting maximum performance on your LAN or local host

# Using LCM on a single host {#multicast_single_host}

Since LCM uses UDP Multicast as a transport mechanism, a valid multicast route
must always be defined.  This means that to use LCM, even for
inter-application communication on a single host, <em>you must have a
multicast-enabled network interface</em>.  If your computer is already
connected to the Internet, LCM will generally "just work" because the default
route will allow LCM to find the correct network interface for multicast
traffic.

If your computer is not connected to any network, you may need to explicitly
enable multicast traffic by adding multicast entries to your system's routing
table.  On Linux, you can setup the loopback interface for multicast with the
following commands:

\verbatim
  sudo ifconfig lo multicast
  sudo route add -net 224.0.0.0 netmask 240.0.0.0 dev lo
\endverbatim
    
Remember, you must always do this to use LCM if your machine is <em>not
connected</em> to any external network.

# Using LCM across multiple hosts {#multicast_multihost}

## Choosing a TTL {#multicast_ttl}

LCM by default uses a time-to-live (TTL) value of 0.  This will prevent any
LCM packets from being transmitted on the wire.  Only local applications will
see them.  Choose a value of 1 for the entire subnet to see the traffic.  Even
larger values will enable the packets to pass through routers.  However, these
routers must be set up with multicast routing tables in order to successfully
relay multicast traffic.
    
There are two ways to set the TTL.  First, LCM constructors in all languages
provide a way to set the TTL (e.g. see lcm_create()), so you can set this in
your code.  Second, if your program does not explicitly set its provider URL,
then you can use the <tt>LCM_DEFAULT_URL</tt> environment variable.  For
exaple, in the bash shell:
\verbatim
  export LCM_DEFAULT_URL=udpm://239.255.76.67:7667?ttl=1
\endverbatim

## Using IGMP snooping {#multicast_igmp}

When the multicast protocol was designed, it was intended that on local
subnets it would act like broadcast traffic.  However, for high-bandwidth
applications, this may not be desirable since it will degrade the bandwidth
available for hosts not participating in the multicast group.

Switch manufacturers have come up with a solution to this problem called "IGMP
snooping".  IGMP snooping consists of two parts.  First, the switch must
monitor network traffic across its ports and decode any IGMP packets that deal
with multicast group subscriptions.  The switch can then send multicast
traffic for a particular group only to those ports where a subscription
request has been observed.  The second part is IGMP querying.  Hosts will only
send out subscription requests upon first subscribing or when queried by a
router.  In order for the switch to maintain its state, these queries must be
sent every few minutes.  If there is no router on your subnet sending these
queries, you must configure the switch to send out "fake queries".  Without
this, the hosts will eventually stop sending IGMP subscription requests, and
the switch will "fail open" causing all multicast traffic to be broadcast
again.

## Firewalls {#multicast_firewalls}

If your operating system is running a firewall, then be sure to check its
settings and make sure that it is allowing UDP Multicast traffic to get
through.  Specifically, make sure the incoming packets are allowed on the
multicast group and port you're using (defined in the LCM URL).  The exact
method of doing this depends heavily on the specific firewall software
installed, so general instructions can't be given here.

## Mixed-speed networks {#multicast_mixed_speed}

If you have a subnet with devices of different speeds, such as 10Mbps links
mixed with 100Mbps links, be extra careful using multicast.  LCM traffic will
be sent to all devices (unless you enable IGMP snooping, above).  If two
100Mbps devices are exchanging more than 10Mbps of traffic, this traffic will
be relayed to the slow device by the switch, even if that device does not run
LCM.  This can overwhelm the bandwidth of that link, and depending on the
switch, may even cause back pressure on the 100Mbps devices.  That means, not
only will the 10Mbps link be swamped, but the 100Mbps devices will be unable
to transmit faster than 10Mbps because their send queues will start filling up
and cause blocking writes in the LCM API.

The easiest solution to this problem is to enable IGMP snooping, as described
above.  If that is not available, sometimes it is sufficient to upgrade from
consumer-grade switches to managed switches, which often have better buffering
strategies.  As a last resort, separating the slow devices from high-speed
devices using two levels of switches can also improve the situation.

# Kernel UDP receive buffer sizing {#multicast_kernel_buffer}

When used on a properly shielded local area network, the most common source of
dropped and lost packets is not electrical disturbances.  Instead, it will
usually be packet buffers that have reached capacity and cannot store any more
packets.

Operating system kernels typically allocate both a <em>send buffer</em> and
<em>receive buffer</em> for a network socket.  The send buffer stores packets
that the operating system has agreed to transmit (i.e. a call to \c send() was
successful) but that it hasn't actually yet transmitted over the network.  The
receive buffer stores packets that the operating system has received from the
network, but that the application hasn't yet retrieved.  In both cases, the
buffers have a maximum capacity, and no new packets can be sent or received if
that capacity is reached.  In the send case, a call to
\c send() blocks until there is space in the buffer (or fails with
\c EAGAIN for non-blocking sockets).  In the receive case, incoming
packets are simply discarded.
    
When LCM is used in a high-bandwidth application, it may become necessary to
increase both the default and the maximum kernel receive buffer size to avoid
dropped packets.
    
Configuring the kernel can be done without rebooting, but requires
superuser privileges. The following table demonstrates how to do this and set
a 2MB maximum buffer size.
    
<table>
<tr><th>OS</th><th>Maximum</th><th>Default</th>
<tr><td>Linux</td>
    <td><tt>sysctl -w net.core.rmem_max=2097152</tt></td>
    <td><tt>sysctl -w net.core.rmem_default=2097152</tt></td>
</tr><tr>
    <td>OS/X, FreeBSD, etc.</td>
    <td><tt>sysctl -w kern.ipc.maxsockbuf=2097152</tt></td>
    <td>??</td>
</tr>
</tr><tr>
<td>Windows</td>
<td></td>
<td>Set registry keys:
<pre>
[HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\services\\AFD\\Parameters]
"DefaultReceiveWindow"=dword:00200000
"DefaultSendWindow"=dword:00200000
</pre>

See also:
1. https://technet.microsoft.com/en-us/library/bb726981.aspx#EBAA
2. http://stackoverflow.com/questions/18985816/change-default-socket-buffer-size-under-windows
</td>
</tr>
</table>

Most GNU/Linux distributions also allow setting these permanently using the
file <tt>/etc/sysctl.conf</tt>.  To do this, add the following
lines into the file somewhere:

\verbatim
    net.core.rmem_max=2097152
    net.core.rmem_default=2097152
\endverbatim
    
Note that OS/X users may need to increase the incoming
(<tt>net.inet.udp.recvspace</tt>) and outgoing
(<tt>net.inet.udp.maxdgram</tt>) datagram sizes in order to send
larger messages. This can be done with
the following commands:

\verbatim
    sysctl -w net.inet.udp.recvspace=209715
    sysctl -w net.inet.udp.maxdgram=65500
\endverbatim

In our initial projects, which involved typical data rates of 10 MB/s, a 2 MB
receive buffer was generally sufficient.

\note http://www.29west.com/docs/THPM/udp-buffer-sizing.html is also a good source of information on UDP buffer sizing.

# Firewalls {#multicast_firewalls}

Systems with overzealous firewalls may prevent UDP multicast traffic from
reaching an LCM process.  If you are having trouble receiving messages across
the network, check your firewall settings.
