package lcm.lc;

import java.util.*;
import java.util.regex.*;

public class URLParser
{
    String protocol;
    String host;
    String port;

    HashMap<String,String> params = new HashMap<String,String>();
    
    public URLParser(String url)
    {
	Matcher m = Pattern.compile("([^\\:]+)://([a-zA-Z0-9\\.]+)?(:([0-9]+))?(\\?(.*))?").matcher(url);

	if (!m.matches()) {
	    System.out.println("Invalid url: "+url);
	    return;
	}

	params.put("protocol", m.group(1));
	params.put("host", m.group(2));
	params.put("port", m.group(4));

	String paramString = m.group(6);

	if (paramString != null) {
	    String keyvalues[] = paramString.split("&");
	    for (int i = 0; i < keyvalues.length; i++) {
		String toks[] = keyvalues[i].split("=");
		if (toks.length != 2) 
		    System.out.println("Invalid key-value pair in URL : "+keyvalues[i]);
		else
		    params.put(toks[0], toks[1]);
	    }
	}

	/*
	System.out.println(paramString);

	System.out.println("protocol: "+protocol);
	System.out.println("host:     "+host);
	System.out.println("port:     "+port);

	for (String key : params.keySet()) {
	    System.out.printf("param %15s: %s\n", key, params.get(key));
	}
	*/
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
	return Boolean.parseBoolean(v);
    }

    public static void main(String args[])
    {
	new URLParser(args[0]);
    }
}
