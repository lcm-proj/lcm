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
often be found installed in `/usr/local/share/java/`. 

Separately, `lcm.jar` can also be found in the `lcm-java` subdirectory of the
source distribution after compiling LCM from source.

A `lcm-sources.jar` is also built and installed. This can provide IDE Javadoc integration.

LCM is currently not available on services like Maven Central. Your build system will need to be configured to point to a local jar.

### Gradle project

An example of using LCM in a Gradle project. Assuming you have placed `my-lcm-types.jar` in `./lib`:

```gradle
// build.gradle
String osName = System.getProperty("os.name").toLowerCase();
project.logger.lifecycle(osName)
repositories { 
    mavenCentral()

    flatDir {
        if (osName.contains("linux")) {        
            dirs '/usr/local/share/java'
        } // else TODO
        dirs 'libs'
    }
}

dependencies {
    implementation ':lcm'
    implementation ':my-lcm-types'
}    
```

> Note: `flatDir` is preferred over `implementation files('/usr/local/share/java/lcm.jar')`. Both work, but IDEs may not be able to find `lcm-sources.jar` with the `files` approach.

### Eclipse project
See [SO: How to import a jar in Eclipse?](https://stackoverflow.com/questions/3280353/how-to-import-a-jar-in-eclipse) for an example of how to import a jar in Eclipse.

See [SO: Attach the Source in Eclipse of a jar](https://stackoverflow.com/questions/15180411/attach-the-source-in-eclipse-of-a-jar) for instructions on how to attach a source jar in Eclipse (you might need to look at multiple answers).

### InteliJ project
See [SO: Correct way to add external jars (lib/*.jar) to an IntelliJ IDEA project](https://stackoverflow.com/questions/1051640/correct-way-to-add-external-jars-lib-jar-to-an-intellij-idea-project) for instructions on how to import a jar in IntelliJ.

### Other
Use another build system like Ant or Maven? Please contribute instructions that work for you.

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
