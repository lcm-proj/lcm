package lcm.lcspy;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import lcm.util.*;

import java.lang.reflect.*;

import lcm.lc.*;

/** Searches classpath for objects that implement LCSpyPlugin using reflection. **/
class LCMHandlerDatabase
{
    HashMap<Long, Class>    classes    = findClasses();

    public LCMHandlerDatabase()
    {
    }

    public Class getClassByFingerprint(long fingerprint)
    {
	return classes.get(fingerprint);
    }

    static HashMap<Long, Class> findClasses()
    {
	HashMap<Long, Class> classes = new HashMap<Long, Class>();

       //The jar should be in the classpath.  Look for it there.
        String cp = System.getProperty("java.class.path");

        String[] items = cp.split(":");
	for (int i=0; i<items.length; i++)
            {
                if (items[i].endsWith(".jar") && (new File(items[i])).exists())
                    {
                        try {
			    
                            JarFile jf = new JarFile(items[i]);
			    
                            for (Enumeration<JarEntry> e = jf.entries() ; e.hasMoreElements() ;)
                                {
                                    JarEntry je = e.nextElement();

				    String n = je.getName();

				    if (n.contains("$"))
					continue;

				    if (n.endsWith(".class")) {
					String cn = n.substring(0, n.length()-6);
					cn = cn.replace('/', '.');
					
					try {
					    Class cls = Class.forName(cn);
					    if (cls == null)
						continue;
					    Field[] fields = cls.getFields();

					    for (Field f : fields) {
						if (f.getName().equals("LCM_FINGERPRINT"))
						    {
							// it's a static member, we don't need an instance
							long fingerprint = f.getLong(null);
							classes.put(fingerprint, cls);
							continue;
						    }
					    }
					} catch (ClassNotFoundException ex) {
					    System.out.println("oops "+ex);
					} catch (NoClassDefFoundError ex) {
					    // fires if class has a dependency on an 
					    // unloadable class
					    //	    System.out.println(ex);
					} catch (IllegalAccessException ex) {
					    System.out.println(ex);
					}

				    }
				}
			    
			} catch(IOException ioe)
                            {
                                System.out.println("Error extracting "+items[i]);
                            }
		    }
	    }

	return classes;
    }
}
