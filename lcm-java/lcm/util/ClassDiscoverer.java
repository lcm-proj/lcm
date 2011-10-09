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

    private static void visitDirectory(ClassVisitor visitor, URLClassLoader cldr, 
            String classpath_entry, File dir, String visiting_classpath) {
        if(!dir.canRead())
            return;
        for(File f : dir.listFiles()) {
            if(!f.canRead())
                continue;
            String fname = f.getName();
            if(f.isDirectory()) {
                // found a directory. recursively traverse the directory and
                // search for .class files
                if(fname.contains("."))
                    continue;

                visitDirectory(visitor, cldr, classpath_entry, f, fname + ".");
            } else if(f.isFile() && fname.endsWith(".class")) {
                // found a .class file.  Construct its full classname and pass
                // it to the class visitor
                String cn = visiting_classpath + fname.substring(0, fname.length()-6);
                try {
                    Class cls = cldr.loadClass(cn);
                    if (cls != null)
                        visitor.classFound(classpath_entry, cls);
                } catch (Throwable ex) { }
            }
        }
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

            if (item.endsWith(".jar")) {
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
            } else {
                File f = new File(item);
                if(!f.isDirectory())
                    continue;
                visitDirectory(visitor, cldr, item, f, "");
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
