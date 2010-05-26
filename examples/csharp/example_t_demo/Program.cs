using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LCM;

namespace LCM.Examples
{
    /// <summary>
    /// Demo listener, demonstrating interoperability with other implementations
    /// Just run this listener and use some example_t message sender (e.g. c demo send-message)
    /// </summary>
    class ExampleDemo
    {
        public static void Main(string[] args)
        {
            LCM.LCM lcm;

            try
            {
                lcm = new LCM.LCM();

                lcm.SubscribeAll(new SimpleSubscriber());

                while (true)
                {
                    System.Threading.Thread.Sleep(1000);
                }
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
                    LCMTypes.example_t msg = new LCMTypes.example_t(dins);

                    Console.WriteLine("Received message of the type example_t:");
                    Console.WriteLine("  timestamp   = {0:D}", msg.timestamp);
                    Console.WriteLine("  position    = ({0:N}, {1:N}, {2:N})",
                            msg.position[0], msg.position[1], msg.position[2]);
                    Console.WriteLine("  orientation = ({0:N}, {1:N}, {2:N}, {3:N})",
                            msg.orientation[0], msg.orientation[1], msg.orientation[2],
                            msg.orientation[3]);
                    Console.Write("  ranges:");
                    for (int i = 0; i < msg.num_ranges; i++)
                        Console.Write(" {0:D}", msg.ranges[i]);
                    Console.WriteLine("");
                }
            }
        }
    }
}
