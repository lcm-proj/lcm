using System;
using LCM.LCM;
using LCMTypes;

namespace LCM.Examples
{
    /// <summary>
    /// Demo listener, see LCM .NET tutorial for more information
    /// </summary>
    class TemperatureDisplay : LCMSubscriber
    {
        public void MessageReceived(LCM.LCM lcm, string channel, LCMDataInputStream ins)
        {
            if (channel == "HALLWAY_TEMPERATURE")
            {
                try
                {
                    temperature_t temp = new temperature_t(ins);
                    Console.WriteLine("The temperature is: " + temp.deg_celsius);
                }
                catch (System.IO.IOException ex)
                {
                    Console.Error.WriteLine("Error decoding temperature message: " + ex);
                }
            }
        }

        public static void Main(string[] args)
        {
            LCM.LCM myLCM;

            try
            {
                myLCM = new LCM.LCM();

                myLCM.Subscribe("HALLWAY_TEMPERATURE", new TemperatureDisplay());

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
    }
}
