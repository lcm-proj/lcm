using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using LCM.LCM;

namespace LCM.Server
{
    /// <summary>
    /// Simple TCP provider server implementation
    /// </summary>
    class Server
    {
        public static void Main(string[] args)
        {
            try
            {
                int port = 7700;
                if (args.Length > 0)
                {
                    port = Int32.Parse(args[0]);
                }

                new TCPService(port);
            }
            catch (System.IO.IOException ex)
            {
                Console.Error.WriteLine("Ex: " + ex);
            }
        }
    }
}
