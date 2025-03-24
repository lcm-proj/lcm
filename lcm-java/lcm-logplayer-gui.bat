:: Determine our canonical location
set mydir=%~dp0

:: Find our JAR
if exist %mydir%\lcm.jar (
    set jars=%mydir%\lcm.jar
) else (
    if exist %mydir%\..\share\java\lcm.jar (
      set jars=%mydir%\..\share\java\lcm.jar
    ) else (
      echo "Unable to find 'lcm.jar'; please check your installation"
      exit 1
    )
)

:: Add user's CLASSPATH, if set
IF NOT "%CLASSPATH%"=="" set jars=%jars%;%CLASSPATH%

:: Launch the applet
java -server -Djava.net.preferIPv4Stack=true -Xmx64m -Xms32m -ea -cp "%jars%" lcm.logging.LogPlayer %*
