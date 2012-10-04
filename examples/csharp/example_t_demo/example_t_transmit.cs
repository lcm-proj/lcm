using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LCM;

namespace LCM.Examples
{
    /// <summary>
    /// Demo transmitter, see LCM .NET tutorial for more information
    /// </summary>
    class ExampleTransmit
    {
        public static void Main(string[] args)
        {
            try
            {
                LCM.LCM myLCM = LCM.LCM.Singleton;

                exlcm.example_t msg = new exlcm.example_t();
                TimeSpan span = DateTime.Now - new DateTime(1970, 1, 1);
                msg.timestamp = span.Ticks * 100;
                msg.position = new double[] { 1, 2, 3 };
                msg.orientation = new double[] { 1, 0, 0, 0 };
                msg.num_ranges = 15;
                msg.ranges = new short[msg.num_ranges];
                for (int i = 0; i < msg.num_ranges; i++)
                {
                    msg.ranges[i] = (short) i;
                }
                msg.name = "example string";
                msg.enabled = true;

                myLCM.Publish("EXAMPLE", msg);
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Ex: " + ex);
            }
        }
    }
}
