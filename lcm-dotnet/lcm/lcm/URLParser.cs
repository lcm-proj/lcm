using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace LCM.LCM
{	
    /// <summary>
    /// Internal URL parsing engine
    /// </summary>
	public class URLParser
	{
		private Dictionary<string, string> parameters = new Dictionary<string, string>();
		
        /// <summary>
        /// URL parser constructor
        /// </summary>
        /// <param name="url">URL to parse</param>
		public URLParser(string url)
		{
			string[] provNetworkargs = url.Split(new string[] { "://" }, 2, StringSplitOptions.RemoveEmptyEntries);
            if (provNetworkargs.Length < 2)
            {
                throw new System.ArgumentException("URLParser: Invalid URL: " + url);
            }
			string[] networkArgs = provNetworkargs[1].Split('?');
			
			parameters.Add("protocol", provNetworkargs[0]);
			
			if (networkArgs[0].Length > 0)
			{
				parameters.Add("network", networkArgs[0]);
			}
			
			if (networkArgs.Length > 1)
			{
				string[] keyvalues = networkArgs[1].Split('&');

				for (int i = 0; i < keyvalues.Length; i++)
				{
					string[] toks = keyvalues[i].Split('=');

                    if (toks.Length != 2)
                    {
                        System.Console.Error.WriteLine("Invalid key-value pair in URL : " + keyvalues[i]);
                    }
                    else
                    {
                        parameters.Add(toks[0], toks[1]);
                    }
				}
			}
		}
		
        /// <summary>
        /// Get value of a parameter
        /// </summary>
        /// <param name="key">parameter name</param>
        /// <returns>parameter value</returns>
		public string Get(string key)
		{
            string result;
            parameters.TryGetValue(key, out result);
            return result;
		}

        /// <summary>
        /// Get value of a parameter
        /// </summary>
        /// <param name="key">parameter name</param>
        /// <param name="def">default value</param>
        /// <returns>parameter value</returns>
        public string Get(string key, string def)
		{
            string result;
            parameters.TryGetValue(key, out result);

            if (result == null)
            {
                return def;
            }

            return result;
		}

        /// <summary>
        /// Get value of a parameter
        /// </summary>
        /// <param name="key">parameter name</param>
        /// <param name="def">default value</param>
        /// <returns>parameter value</returns>
        public int Get(string key, int def)
		{
            string v = Get(key);

            if (v == null)
            {
                return def;
            }

			return Int32.Parse(v);
		}

        /// <summary>
        /// Get value of a parameter
        /// </summary>
        /// <param name="key">parameter name</param>
        /// <param name="def">default value</param>
        /// <returns>parameter value</returns>
        public bool Get(string key, bool def)
		{
            string v = Get(key);

            if (v == null)
            {
                return def;
            }

			return Boolean.Parse(v) || v.Equals("1");
		}

        /// <summary>
        /// Get value of a parameter
        /// </summary>
        /// <param name="key">parameter name</param>
        /// <param name="def">default value</param>
        /// <returns>parameter value</returns>
        public double Get(string key, double def)
		{
            string v = Get(key);

			if (v == null)
            {
				return def;
            }

			return Double.Parse(v);
		}
		
        /*
		public static void Main(string[] args)
		{
			URLParser u = null;

			if (args.Length < 1)
			{
				string env = Environment.GetEnvironmentVariable("LCM_DEFAULT_URL");
				if (env != null)
                {
					u = new URLParser(env);
                }
				else
				{
					Console.Error.WriteLine("Must specify URL");
					Environment.Exit(1);
				}
			}
			else
			{
				u = new URLParser(args[0]);
			}
			
			foreach (string key in u.parameters.Keys)
			{
                string val;
                u.parameters.TryGetValue(key, out val);
                Console.Error.WriteLine(String.Format("param %15s: %s\n", key, val));
			}
		}
        */
	}
}