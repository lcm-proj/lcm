using System;
using LCM.LCM;
using LCMTypes;

namespace LCM.Examples
{
    /// <summary>
    /// Demo transmitter, see LCM .NET tutorial for more information
    /// </summary>
    class TemperatureTransmit
    {
        public static void Main(string[] args)
        {
            LCM.LCM myLCM = LCM.LCM.Singleton;

            while (true)
            {
                try
                {
                    temperature_t temp = new temperature_t();
                    temp.utime = DateTime.Now.Ticks / 10;
                    temp.deg_celsius = 25.0 + 5 * Math.Sin(DateTime.Now.Ticks / 10000000.0);

                    myLCM.Publish("HALLWAY_TEMPERATURE", temp);

                    System.Threading.Thread.Sleep(1000);
                }
                catch (Exception ex)
                {
                    Console.Error.WriteLine("Ex: " + ex);
                }
            }
        }
    }
}
