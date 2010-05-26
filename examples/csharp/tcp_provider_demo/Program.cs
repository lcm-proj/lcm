using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LCM;

namespace LCM.Examples
{
    /// <summary>
    /// Demo listener and sender, demonstrating C#.NET TCP provider implementation
    /// </summary>
    class TCPProviderDemo
    {
        public static void Main(string[] args)
        {
            LCM.LCM lcm;

            try
            {
                lcm = new LCM.LCM(new string[] { "tcpq://127.0.0.1:7700" });
                // allow the TCP provider reader to connect to the server before the subscription attempt
                System.Threading.Thread.Sleep(500);
                lcm.SubscribeAll(new SimpleSubscriber());

                while (true)
                {
                    try
                    {
                        System.Threading.Thread.Sleep(1000);

                        Console.WriteLine("SEND: TEST");
                        lcm.Publish("TEST", "foobar");
                    }
                    catch (Exception ex)
                    {
                        Console.Error.WriteLine("Ex: " + ex);
                    }
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
            }
        }
    }
}
