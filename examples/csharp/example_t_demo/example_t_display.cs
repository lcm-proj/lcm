using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LCM;

namespace LCM.Examples
{
    /// <summary>
    /// Demo listener, demonstrating interoperability with other implementations
    /// Just run this listener and use any of the example_t message senders
    /// </summary>
    class ExampleDisplay
    {
        public static void Main(string[] args)
        {
            LCM.LCM myLCM;

            try
            {
                myLCM = new LCM.LCM();

                myLCM.SubscribeAll(new SimpleSubscriber());

                while (true)
                    System.Threading.Thread.Sleep(1000);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Ex: " + ex);
                Environment.Exit(1);
            }
        }

        internal class SimpleSubscriber : LCM.LCMSubscriber
        {
            public void MessageReceived(LCM.LCM lcm, string channel, LCM.LCMDataInputStream dins)
            {
                Console.WriteLine("RECV: " + channel);

                if (channel == "EXAMPLE")
                {
                    exlcm.example_t msg = new exlcm.example_t(dins);

                    Console.WriteLine("Received message of the type example_t:");
                    Console.WriteLine("  timestamp   = {0:D}", msg.timestamp);
                    Console.WriteLine("  position    = ({0:N}, {1:N}, {2:N})",
                            msg.position[0], msg.position[1], msg.position[2]);
                    Console.WriteLine("  orientation = ({0:N}, {1:N}, {2:N}, {3:N})",
                            msg.orientation[0], msg.orientation[1], msg.orientation[2],
                            msg.orientation[3]);
                    Console.Write("  ranges      = [ ");
                    for (int i = 0; i < msg.num_ranges; i++)
                    {
                        Console.Write(" {0:D}", msg.ranges[i]);
                        if (i < msg.num_ranges-1)
                            Console.Write(", ");
                    }
                    Console.WriteLine(" ]");
                    Console.WriteLine("  name         = '" + msg.name + "'");
                    Console.WriteLine("  enabled      = '" + msg.enabled + "'");
                }
            }
        }
    }
}
