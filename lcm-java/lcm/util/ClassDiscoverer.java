package lcm.util;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.net.*;
import java.lang.reflect.*;

public class ClassDiscoverer
{
    public static void findClasses(ClassVisitor visitor)
    {
	findClasses(System.getenv("CLASSPATH"), visitor);
	findClasses(System.getProperty("java.class.path"), visitor);
    }

    /** Given a colon-delimited list of jar files, iterate over the
     * classes in them.
     **/
    public static void findClasses(String cp, ClassVisitor visitor)
    {
	String[] items = cp.split(":");
	for (int i = 0; i < items.length; i++) {
	    
	    String item = items[i];

	    if (!item.endsWith(".jar") || !(new File(item).exists()))
		continue;

	    try {
		URLClassLoader cldr = new URLClassLoader(new URL[] {
			new File(item).toURL() });

		JarFile jf = new JarFile(item);
		
		for (Enumeration<JarEntry> e = jf.entries() ; e.hasMoreElements() ;) {

		    JarEntry je = e.nextElement();

		    String n = je.getName();

		    // skip private classes?
		    //		    if (n.contains("$"))
		    //			continue;

		    if (n.endsWith(".class")) {

			// convert the path into a class name
			String cn = n.substring(0, n.length()-6);
			cn = cn.replace('/', '.');
			
			// try loading that class
			try {
			    Class cls = cldr.loadClass(cn);

			    if (cls == null)
				continue;

			    visitor.classFound(item, cls);

			} catch (ClassNotFoundException ex) {
			    System.out.println("ERR: "+ex);
			} catch (NoClassDefFoundError ex) {
			    // fires if class has a dependency on an 
			    // unloadable class
			    //	    System.out.println(ex);
			}
		    }
		}
		
	    } catch(IOException ioe) {
		System.out.println("Error extracting "+items[i]);
	    }
	}
    }

    public interface ClassVisitor
    {
	public void classFound(String jarfile, Class cls);
    }

    // Just list every class that we can find!
    public static void main(String args[])
    {
	ClassVisitor cv = new ClassVisitor() {
		public void classFound(String jarfile, Class cls) {
		    System.out.printf("%-30s %s\n", jarfile, cls);
		}
	    };

	ClassDiscoverer.findClasses(cv);
    }
}
