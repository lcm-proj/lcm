#!/bin/sh

# Determine our canonical location
if (perl -e '' 2>/dev/null)
  then mydir="$(dirname "$(perl -MCwd -le 'print Cwd::abs_path(shift)' "$0")")"
  else mydir="$(dirname "$0")"
fi

# Find dependency JARs
if [ -e "$mydir/lcm.jar" ]; then
  jars="$mydir/lcm.jar"
  jars+=":$mydir/jchart2d-code/jchart2d-3.2.2.jar"
  jars+=":$mydir/jchart2d-code/ext/xmlgraphics-commons-1.3.1.jar"
  jars+=":$mydir/jchart2d-code/ext/jide-oss-2.9.7.jar"
elif [ -e "$mydir/../share/java/lcm.jar" ]; then
  jars="$mydir/../share/java/lcm.jar"
  jars+=":$mydir/../share/java/jchart2d-3.2.2.jar"
  jars+=":$mydir/../share/java/xmlgraphics-commons-1.3.1.jar"
  jars+=":$mydir/../share/java/jide-oss-2.9.7.jar"
else
  echo "Unable to find 'lcm.jar'; please check your installation" >&2
  exit 1
fi

# Launch the applet
exec java -server -Djava.net.preferIPv4Stack=true -Xincgc -Xmx128m -Xms64m -ea -cp "$jars" lcm.spy.Spy "$@"
