#!/bin/sh
if (perl -e '' 2>/dev/null)
  then mydir="$(dirname "$(perl -MCwd -le 'print Cwd::abs_path(shift)' "$0")")"
  else mydir="$(dirname "$0")"
fi
if [ -e "$mydir/lcm.jar" ]
  then jardir="$mydir"
elif [ -e "$mydir/../share/java/lcm.jar" ]
  then jardir="$mydir/../share/java"
else
  echo "Unable to find 'lcm.jar'; please check your installation" >&2
  exit 1
fi

exec java -server -Djava.net.preferIPv4Stack=true -Xincgc -Xmx128m -Xms64m -ea -cp $jardir/lcm.jar:$jardir/jchart2d-3.2.2.jar lcm.spy.Spy "$@"
