  if 'Java_VERSION', 'VERSION_GREATER', '1.9', 'OR', 'Java_VERSION', 'VERSION_EQUAL', '1.9'
    lcm_java_target_version = ['8', 'CACHE', 'STRING', 'Java version to target when building Java components (leave empty to use default)']
    if 'NOT', 'LCM_JAVA_TARGET_VERSION', 'STREQUAL', ''
      cmake_java_compile_flags = ['--release', lcm_java_target_version]
    endif
  elif 'Java_VERSION', 'VERSION_LESS', '1.9'
    lcm_java_target_version = ['1.8', 'CACHE', 'STRING', 'Java version to target when building Java components (leave empty to use default)']
    if 'NOT', 'LCM_JAVA_TARGET_VERSION', 'STREQUAL', ''
      cmake_java_compile_flags = ['-source', lcm_java_target_version, '-target', lcm_java_target_version]
    endif
  endif
  # Use system version of jchart2d if available
  # find_jar(['jchart2d_JAR', 'jchart2d'])
  if 'NOT', 'jchart2d_JAR'
    subdir('jchart2d-code')
    jchart2d_jar = 'jchart2d'
  endif
  lcm_java_sources = ['lcm/util/JImage.java', 'lcm/util/TableSorter.java', 'lcm/util/BufferedRandomAccessFile.java', 'lcm/util/ParameterListener.java', 'lcm/util/ParameterGUI.java', 'lcm/util/ClassDiscoverer.java', 'lcm/util/ColorMapper.java', 'lcm/spy/Spy.java', 'lcm/spy/ObjectPanel.java', 'lcm/spy/ChartData.java', 'lcm/spy/ChannelData.java', 'lcm/spy/ZoomableChartScrollWheel.java', 'lcm/spy/SpyPlugin.java', 'lcm/spy/LCMTypeDatabase.java', 'lcm/logging/LogDiagnostic.java', 'lcm/logging/JScrubber.java', 'lcm/logging/JScrubberListener.java', 'lcm/logging/Log.java', 'lcm/logging/LogPlayer.java', 'lcm/lcm/LCMDataOutputStream.java', 'lcm/lcm/Provider.java', 'lcm/lcm/LCMDataInputStream.java', 'lcm/lcm/UDPMulticastProvider.java', 'lcm/lcm/LCMSubscriber.java', 'lcm/lcm/URLParser.java', 'lcm/lcm/MessageAggregator.java', 'lcm/lcm/MemqProvider.java', 'lcm/lcm/LCMEncodable.java', 'lcm/lcm/LCM.java', 'lcm/lcm/LogFileProvider.java', 'lcm/lcm/TCPProvider.java', 'lcm/lcm/TCPService.java']
  # add_jar(['lcm-java', 'INCLUDE_JARS', jchart2d_jar, 'SOURCES', lcm_java_sources, 'OUTPUT_NAME', 'lcm'])
  # install_jar(['lcm-java', 'DESTINATION', 'share/java'])
  # configure_file([cmake_current_source_dir, '/lcm-spy.sh.in', cmake_current_binary_dir, '/lcm-spy', '@ONLY'])
  # lcm_copy_file_target(['lcm-logplayer-gui-alias', cmake_current_source_dir, '/lcm-logplayer-gui.sh', cmake_current_binary_dir, '/lcm-logplayer-gui'])
  # install(['PROGRAMS', cmake_current_binary_dir, '/lcm-logplayer-gui', cmake_current_binary_dir, '/lcm-spy', 'DESTINATION', 'bin'])
  # install(['FILES', 'lcm-logplayer-gui.1', 'lcm-spy.1', 'DESTINATION', 'share/man/man1'])
  if 'NOT', 'Java_JAVADOC_EXECUTABLE'
    # message(['WARNING', 'javadoc was not found; Java documentation will not be generated'])
    # return()
  endif
  lcm_java_doc_classes = ['lcm/lcm/LCM.java', 'lcm/lcm/LCMEncodable.java', 'lcm/lcm/LCMSubscriber.java', 'lcm/lcm/MessageAggregator.java', 'lcm/logging/Log.java']
  # add_custom_target(['doc-java', 'COMMAND', java_javadoc_executable, '-d', lcm_binary_dir, '/docs/_build/javadocs', '-link', 'http://docs.oracle.com/javase/8/docs/api/', lcm_java_doc_classes, 'WORKING_DIRECTORY', cmake_current_source_dir, 'DEPENDS', 'doc-setup', 'doc-sphinx'])
  # add_dependencies(['doc', 'doc-java'])
