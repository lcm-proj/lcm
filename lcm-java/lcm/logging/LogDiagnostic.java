package lcm.logging;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import lcm.lcm.*;
import lcm.util.*;

public class LogDiagnostic
{
    public static void main(String args[])
    {
        try {
            main_ex(args);
        } catch (IOException ex) {
            System.out.println("ex: "+ex);
        }
    }

    public static void main_ex(String args[]) throws IOException
    {
        Log log = new Log(args[0], "r");

        long last_utime = 0;

        while (true) {
            Log.Event e = log.readNext();

            long dutime = e.utime - last_utime;
            if (dutime < 0 && last_utime != 0)
                System.out.printf("%15d Negative utime (%10d)\n", e.utime, dutime);
            if (dutime > 1000000)
                System.out.printf("%15d Large utime    (%10d)\n", e.utime, dutime);

            last_utime = e.utime;
        }

    }
}
