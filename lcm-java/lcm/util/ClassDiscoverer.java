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
        String ps = System.getProperty("path.separator");

        // In order to correctly handle types that reference other
        // types whose definitions are in another JAR file, create a
        // big "master" classpath that contains everything we might
        // want to load.
        String cp = System.getenv("CLASSPATH")+ ps +System.getProperty("java.class.path");
        findClasses(cp, visitor);
    }

    /** Given a colon-delimited list of jar files, iterate over the
     * classes in them.
     * @param cp The colon-deliimited classpath to search
     **/
    public static void findClasses(String cp, ClassVisitor visitor)
    {
        if (cp == null)
            return;

        String ps = System.getProperty("path.separator");
        String[] items = cp.split(ps);

        // Create a class loader that has access to the whole class path.
        URLClassLoader cldr;
        try {
            URL[] urls = new URL[items.length];
            for (int i = 0; i < items.length; i++)
                urls[i] = new File(items[i]).toURL();

            cldr = new URLClassLoader(urls);
        } catch (IOException ex) {
            System.out.println("ClassDiscoverer ERR: "+ex);
            return;
        }

        for (int i = 0; i < items.length; i++) {

            String item = items[i];

            if (!item.endsWith(".jar") || !(new File(item).exists()))
                continue;

            try {
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
                        cn = cn.replace('\\', '.');

                        // try loading that class
                        try {
                            Class cls = cldr.loadClass(cn);

                            if (cls == null)
                                continue;

                            visitor.classFound(item, cls);

                        } catch (Throwable ex) {
                            System.out.println("ClassDiscoverer: "+ex);
                            System.out.println("                 jar: "+item);
                            System.out.println("                 class: "+n);
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
