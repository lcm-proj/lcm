include(UseJava)

set(LCM_JAVA_TARGET_VERSION "1.6" CACHE STRING
  "Java version to target when building Java components (leave empty to use default)"
)
if(NOT LCM_JAVA_TARGET_VERSION STREQUAL "")
  set(CMAKE_JAVA_COMPILE_FLAGS
    -source ${LCM_JAVA_TARGET_VERSION}
    -target ${LCM_JAVA_TARGET_VERSION}
  )
endif()

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
  OUTPUT_NAME lcm
)

install_jar(lcm-java DESTINATION share/java)

configure_file(
  ${CMAKE_CURRENT_SOURCE_DIR}/lcm-spy.sh.in
  ${CMAKE_CURRENT_BINARY_DIR}/lcm-spy
  @ONLY
)

lcm_copy_file_target(lcm-logplayer-gui-alias
  ${CMAKE_CURRENT_SOURCE_DIR}/lcm-logplayer-gui.sh
  ${CMAKE_CURRENT_BINARY_DIR}/lcm-logplayer-gui
)

install(PROGRAMS
  ${CMAKE_CURRENT_BINARY_DIR}/lcm-logplayer-gui
  ${CMAKE_CURRENT_BINARY_DIR}/lcm-spy
  DESTINATION bin
)

install(FILES
  lcm-logplayer-gui.1
  lcm-spy.1
  DESTINATION share/man/man1
)

if(NOT Java_JAVADOC_EXECUTABLE)
  message(WARNING
    "javadoc was not found; Java documentation will not be generated")
  return()
endif()

set(lcm_java_doc_classes
  lcm/lcm/LCM.java
  lcm/lcm/LCMEncodable.java
  lcm/lcm/LCMSubscriber.java
  lcm/lcm/MessageAggregator.java
  lcm/logging/Log.java
)

add_custom_target(doc-java
  COMMAND  ${Java_JAVADOC_EXECUTABLE}
    -d ${lcm_BINARY_DIR}/docs/html/javadocs
    -link https://docs.oracle.com/javase/1.5.0/docs/api
    ${lcm_java_doc_classes}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  DEPENDS doc-clean)

add_dependencies(doc doc-java)
