include(UseJava)

if (Java_VERSION VERSION_GREATER 1.9 OR Java_VERSION VERSION_EQUAL 1.9)
  set(LCM_JAVA_TARGET_VERSION "8" CACHE STRING
    "Java version to target when building Java components (leave empty to use default)"
  )
  if(NOT LCM_JAVA_TARGET_VERSION STREQUAL "")
    set(CMAKE_JAVA_COMPILE_FLAGS
      --release ${LCM_JAVA_TARGET_VERSION}
    )
  endif()
elseif (Java_VERSION VERSION_LESS 1.9)
  set(LCM_JAVA_TARGET_VERSION "1.8" CACHE STRING
    "Java version to target when building Java components (leave empty to use default)"
  )
  if(NOT LCM_JAVA_TARGET_VERSION STREQUAL "")
    set(CMAKE_JAVA_COMPILE_FLAGS
      -source ${LCM_JAVA_TARGET_VERSION}
      -target ${LCM_JAVA_TARGET_VERSION}
    )
  endif()
endif()

# Use system version of jchart2d if available
find_jar(jchart2d_JAR jchart2d)
if(NOT jchart2d_JAR)
  add_subdirectory(jchart2d-code)
  set(jchart2d_JAR jchart2d)
endif()

set(top_package lcm)

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
  INCLUDE_JARS ${jchart2d_JAR}
  SOURCES ${lcm_java_sources}
  OUTPUT_NAME lcm
)

install_jar(lcm-java DESTINATION share/java)

# Make a sources jar.
# Starting point. Should probably provide a new answer once something more robust is put together.
# https://stackoverflow.com/a/77086658/8954109
set(sources_jar "lcm-sources.jar")

add_custom_command(
  OUTPUT 
    "${sources_jar}"
  COMMAND 
    # -- Long options are not supported on all java distributions.
    # -- ${Java_JAR_EXECUTABLE} --create --file "${sources_jar}" -C ${CMAKE_CURRENT_SOURCE_DIR} ${top_package}
    ${Java_JAR_EXECUTABLE} cf "${CMAKE_CURRENT_BINARY_DIR}/${sources_jar}" ${top_package}
  WORKING_DIRECTORY
    "${CMAKE_CURRENT_SOURCE_DIR}"
  DEPENDS 
    ${lcm_java_sources}
  VERBATIM
  COMMENT 
    "Creating sources jar"
)

add_custom_target(lcm-java-src
  DEPENDS "${sources_jar}"
)
add_dependencies(lcm-java lcm-java-src)

install(FILES "${CMAKE_CURRENT_BINARY_DIR}/${sources_jar}" DESTINATION share/java)


if(WIN32)

  configure_file(
    ${CMAKE_CURRENT_SOURCE_DIR}/lcm-spy.bat.in
    ${CMAKE_CURRENT_BINARY_DIR}/lcm-spy.bat
    @ONLY
  )

  lcm_copy_file_target(lcm-logplayer-gui-alias
    ${CMAKE_CURRENT_SOURCE_DIR}/lcm-logplayer-gui.bat
    ${CMAKE_CURRENT_BINARY_DIR}/lcm-logplayer-gui.bat
  )

  install(PROGRAMS
    ${CMAKE_CURRENT_BINARY_DIR}/lcm-logplayer-gui.bat
    ${CMAKE_CURRENT_BINARY_DIR}/lcm-spy.bat
    DESTINATION bin
  )

else()

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

endif()

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
    -d ${lcm_BINARY_DIR}/docs/_build/javadocs
    -link http://docs.oracle.com/javase/8/docs/api/
    ${lcm_java_doc_classes}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
  DEPENDS doc-setup doc-sphinx)

add_dependencies(doc doc-java)
