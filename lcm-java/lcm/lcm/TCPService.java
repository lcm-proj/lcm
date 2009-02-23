package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.nio.*;

public class TCPService
{
    ServerSocket serverSocket;

    AcceptThread acceptThread;
    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

    int bytesCount = 0;

    public TCPService(int port) throws IOException
    {
	serverSocket = new ServerSocket(port);
	//	sock.setReuseAddress(true);
	//	sock.setLoopbackMode(false); // true *disables* loopback

	acceptThread = new AcceptThread();
	acceptThread.start();

	long inittime = System.currentTimeMillis();
	long starttime = System.currentTimeMillis();
	while (true) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException ex) {
	    }
	    long endtime = System.currentTimeMillis();
	    double dt = (endtime - starttime) / 1000.0;
	    starttime = endtime;
	    System.out.printf("%10.3f : %10.1f kB/s, %d clients\n",(endtime - inittime)/1000.0, bytesCount/1024.0/dt, clients.size());
	    bytesCount = 0;
	}
    }

    public void relay(byte channel[], byte data[])
    {
	// synchronously send to all clients.
	synchronized(clients) {
	    for (ClientThread client : clients) {
		client.send(channel, data);
	    }
	}
    }

    class AcceptThread extends Thread
    {
	public void run()
	{
	    while (true) {
		try {
		    Socket clientSock = serverSocket.accept();

		    ClientThread client = new ClientThread(clientSock);
		    client.start();

		    synchronized(clients) {
			clients.add(client);
		    }
		} catch (IOException ex) {
		}
	    }
	}
    }

    class ClientThread extends Thread
    {
	Socket sock;
	DataInputStream ins;
	DataOutputStream outs;

	public ClientThread(Socket sock) throws IOException
	{
	    this.sock = sock;

	    ins = new DataInputStream(sock.getInputStream());
	    outs = new DataOutputStream(sock.getOutputStream());

	    outs.writeInt(TCPProvider.MAGIC_SERVER);
	    outs.writeInt(TCPProvider.VERSION);
	}

	public void run()
	{
	    ///////////////////////
	    // read messages until something bad happens.	    
	    try {
		while (true) {
		    int type = ins.readInt();
		    if (type == TCPProvider.MESSAGE_TYPE_PUBLISH) {

			int channellen = ins.readInt();
			byte channel[] = new byte[channellen];
			ins.readFully(channel);

			int datalen = ins.readInt();
			byte data[] = new byte[datalen];
			ins.readFully(data);

			TCPService.this.relay(channel, data);

			bytesCount += channellen + datalen + 8;
		    }
		}
	    } catch (IOException ex) {
	    }

	    ///////////////////////
	    // Something bad happened, close this connection.
	    try {
		sock.close();
	    } catch (IOException ex) {
	    }

	    synchronized(clients) {
		clients.remove(this);
	    }
	}

	public synchronized void send(byte channel[], byte data[])
	{
	    try {
		outs.writeInt(TCPProvider.MESSAGE_TYPE_PUBLISH);
		outs.writeInt(channel.length);
		outs.write(channel);
		outs.writeInt(data.length);
		outs.write(data);
		outs.flush();
	    } catch (IOException ex) {
	    }
	}
    }

    public static void main(String args[])
    {
	try {
	    int port = 7700;
	    if (args.length > 0)
		port = Integer.parseInt(args[0]);
	    new TCPService(port);
	} catch (IOException ex) {
	    System.out.println("Ex: "+ex);
	}
    }
}
