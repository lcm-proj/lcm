package lcm.lcm;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.regex.*;
import java.nio.*;

public class TCPService
{
    ServerSocket serverSocket;

    AcceptThread acceptThread;
    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
    ReadWriteLock clients_lock = new ReentrantReadWriteLock();

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
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
            long endtime = System.currentTimeMillis();
            double dt = (endtime - starttime) / 1000.0;
            starttime = endtime;
            System.out.printf("%10.3f : %10.1f kB/s, %d clients\n",(endtime - inittime)/1000.0, bytesCount/1024.0/dt, clients.size());
            bytesCount = 0;
        }
        // interrupt signal received
        closeResources();
    }

    private void closeResources() throws IOException {
        acceptThread.interrupt();
        serverSocket.close();
        synchronized(clients) {
            for (ClientThread clientThread : clients) {
                clientThread.closeResources();
            }
        }
    }


    public void relay(byte channel[], byte data[])
    {
        // synchronously send to all clients.
        String chanstr = new String(channel);
        try {
            clients_lock.readLock().lock();
            for (ClientThread client : clients) {
                client.send(chanstr, channel, data);
            }
        } finally {
            clients_lock.readLock().unlock();
        }
    }

    class AcceptThread extends Thread
    {
        public void run()
        {
            while (!Thread.interrupted()) {
                try {
                    Socket clientSock = serverSocket.accept();

                    ClientThread client = new ClientThread(clientSock);
                    client.start();

                    try {
                        clients_lock.writeLock().lock();
                        clients.add(client);
                    } finally {
                        clients_lock.writeLock().unlock();
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

        class SubscriptionRecord
        {
            String  regex;
            Pattern pat;
            SubscriptionRecord(String regex) {
                this.regex = regex;
                this.pat = Pattern.compile(regex);
            }
        }

        ArrayList<SubscriptionRecord> subscriptions = new ArrayList<SubscriptionRecord>();
        ReadWriteLock subscriptions_lock = new ReentrantReadWriteLock();

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
                    } else if(type == TCPProvider.MESSAGE_TYPE_SUBSCRIBE) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        try {
                            subscriptions_lock.writeLock().lock();
                            subscriptions.add(new SubscriptionRecord(new String(channel)));
                        } finally {
                            subscriptions_lock.writeLock().unlock();
                        }
                    } else if(type == TCPProvider.MESSAGE_TYPE_UNSUBSCRIBE) {
                        int channellen = ins.readInt();
                        byte channel[] = new byte[channellen];
                        ins.readFully(channel);
                        String re = new String(channel);
                        try {
                            subscriptions_lock.writeLock().lock();
                            for(int i=0, n=subscriptions.size(); i<n; i++) {
                                if(subscriptions.get(i).regex.equals(re)) {
                                    subscriptions.remove(i);
                                    break;
                                }
                            }
                        } finally {
                            subscriptions_lock.writeLock().unlock();
                        }
                    }
                }
            } catch (IOException ex) {
            }

            ///////////////////////
            // Something bad happened, close this connection.
            try {
                closeResources();
            } catch (IOException ex) {
            }

            try {
                clients_lock.writeLock().lock();
                clients.remove(this);
            } finally {
                clients_lock.writeLock().unlock();
            }
        }

        public void closeResources() throws IOException {
            sock.close();
        }

        public void send(String chanstr, byte channel[], byte data[])
        {
            try {
                subscriptions_lock.readLock().lock();
                for(SubscriptionRecord sr : subscriptions) {
                    if(sr.pat.matcher(chanstr).matches()) {
                        synchronized(outs) {
                            outs.writeInt(TCPProvider.MESSAGE_TYPE_PUBLISH);
                            outs.writeInt(channel.length);
                            outs.write(channel);
                            outs.writeInt(data.length);
                            outs.write(data);
                            outs.flush();
                            return;
                        }
                    }
                }
            } catch (IOException ex) {
            } finally {
                subscriptions_lock.readLock().unlock();
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
