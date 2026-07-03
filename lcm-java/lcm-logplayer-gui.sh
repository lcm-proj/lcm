#!/bin/sh

# Determine our canonical location
if (perl -e '' 2>/dev/null)
  then mydir="$(dirname "$(perl -MCwd -le 'print Cwd::abs_path(shift)' "$0")")"
  else mydir="$(dirname "$0")"
fi

# Find our JAR
if [ -e "$mydir/lcm.jar" ]
  then jars="$mydir/lcm.jar"
elif [ -e "$mydir/../share/java/lcm.jar" ]
  then jars="$mydir/../share/java/lcm.jar"
else
  echo "Unable to find 'lcm.jar'; please check your installation" >&2
  exit 1
fi

# Add user's CLASSPATH, if set
[ -n "$CLASSPATH" ] && jars="$jars:$CLASSPATH"

[ -z "$LCM_JVM_OPT" ] && LCM_JVM_OPT="-Xmx128m -Xms64m"

# Launch the applet
exec java -server ${LCM_JVM_OPT} -ea -cp "$jars" lcm.logging.LogPlayer "$@"
