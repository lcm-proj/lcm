include(UseJava)

add_subdirectory(jchart2d-code)

set(lcm_java_sources
  lcm/util/JImage.java
  lcm/util/TableSorter.java
  lcm/util/BufferedRandomAccessFile.java
  lcm/util/ParameterListener.java
  lcm/util/ParameterGUI.java
  lcm/util/ClassDiscoverer.java
  lcm/util/ColorMapper.java
  lcm/spy/Spy.java
  lcm/spy/ObjectPanel.java
  lcm/spy/ChartData.java
  lcm/spy/ChannelData.java
  lcm/spy/ZoomableChartScrollWheel.java
  lcm/spy/SpyPlugin.java
  lcm/spy/LCMTypeDatabase.java
  lcm/logging/LogDiagnostic.java
  lcm/logging/JScrubber.java
  lcm/logging/JScrubberListener.java
  lcm/logging/Log.java
  lcm/logging/LogPlayer.java
  lcm/lcm/LCMDataOutputStream.java
  lcm/lcm/Provider.java
  lcm/lcm/LCMDataInputStream.java
  lcm/lcm/UDPMulticastProvider.java
  lcm/lcm/LCMSubscriber.java
  lcm/lcm/URLParser.java
  lcm/lcm/MessageAggregator.java
  lcm/lcm/MemqProvider.java
  lcm/lcm/LCMEncodable.java
  lcm/lcm/LCM.java
  lcm/lcm/LogFileProvider.java
  lcm/lcm/TCPProvider.java
  lcm/lcm/TCPService.java
)

add_jar(lcm-java
  INCLUDE_JARS jchart2d
  SOURCES ${lcm_java_sources}
)

install_jar(lcm-java DESTINATION share/java)
