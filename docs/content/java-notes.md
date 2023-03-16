# Java application notes

## API docs

Java API documentation is maintained with Javadoc.  To build the documentation,
[build LCM](build-instructions) and then run the script:

    $ lcm-java/make-javadocs.sh

## Finding lcm.jar

The core LCM Java implementation is built to `lcm.jar`.  To use LCM in your
application, `lcm.jar` must be in your Java classpath.

On Linux, OS/X, and other UNIX-like systems, `lcm.jar` is typically built
automatically and installed along with the rest of LCM.  The exact location
depends on the operating system and any configuration parameters, but it can
often be found installed in <tt>/usr/local/share/java/</tt>. 

Separately, `lcm.jar` can also be found in the `lcm-java` subdirectory of the
source distribution after compiling LCM from source.

## Namespace issues

LCM supports namespaces for data types, making it easier for users to use the
types defined by others without worry that those types will conflict with
other users' types. 

When defining a type, the name of the type can include a namespace, e.g.,
"struct examples.temperature_t { ... }". When compiled with lcm-gen, this will
result in a class named "temperature_t" in package "examples". If lcm-gen is
given the root of a source tree with the --jpath flag, it will automatically
put temperature_t.class in the examples subdirectory. You will need to
remember to import that type before you use it, e.g., "import examples.*", or
else you will need to refer to types by their fully-qualified names (e.g.,
"new examples.temperature_t()").

Note that if you do not specify a package name in your LCM type definition
file, lcm-gen will (by default) put those types into the "lcmtypes" Java
package. This is necessary because Java does not officially support packageless
classes.
