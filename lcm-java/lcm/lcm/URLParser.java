package lcm.lcm;

import java.util.*;
import java.util.regex.*;

public class URLParser
{
    HashMap<String,String> params = new HashMap<String,String>();

    public URLParser(String url)
    {
        String prov_networkargs[] = url.split("://", 2);
        if (prov_networkargs.length < 2)
            throw new IllegalArgumentException ("URLParser: Invalid URL: "+url);
        String network_args[] = prov_networkargs[1].split("[?]");

        params.put("protocol", prov_networkargs[0]);

        if (network_args[0].length() > 0) {
            params.put("network", network_args[0]);
        }

        if (network_args.length > 1) {
            String keyvalues[] = network_args[1].split("&");
            for (int i = 0; i < keyvalues.length; i++) {
                String toks[] = keyvalues[i].split("=");
                if (toks.length != 2)
                    System.err.println("Invalid key-value pair in URL : "+keyvalues[i]);
                else
                    params.put(toks[0], toks[1]);
            }
        }
    }

    public String get(String key)
    {
        return params.get(key);
    }

    public String get(String key, String def)
    {
        if (params.get(key)==null)
            return def;

        return params.get(key);
    }

    public int get(String key, int def)
    {
        String v = params.get(key);
        if (v==null)
            return def;
        return Integer.parseInt(v);
    }

    public boolean get(String key, boolean def)
    {
        String v = params.get(key);
        if (v==null)
            return def;
        return Boolean.parseBoolean(v) || v.equals("1");
    }

    public double get(String key, double def)
    {
        String v = params.get(key);
        if (v==null)
            return def;
        return Double.parseDouble(v);
    }

    public static void main(String args[])
    {
        URLParser u = null;
        if (args.length < 1) {
            String env = System.getenv("LCM_DEFAULT_URL");
            if (null != env)
                u = new URLParser(env);
            else  {
                System.err.println("Must specify URL");
                System.exit(1);
            }
        } else {
            u = new URLParser(args[0]);
        }

        for (String key : u.params.keySet()) {
            System.err.printf("param %15s: %s\n", key, u.params.get(key));
        }
    }
}
