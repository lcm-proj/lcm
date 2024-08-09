lcm_java_sources = ['lcm/util/JImage.java', 'lcm/util/TableSorter.java', 'lcm/util/BufferedRandomAccessFile.java', 'lcm/util/ParameterListener.java', 'lcm/util/ParameterGUI.java', 'lcm/util/ClassDiscoverer.java', 'lcm/util/ColorMapper.java', 'lcm/spy/Spy.java', 'lcm/spy/ObjectPanel.java', 'lcm/spy/ChartData.java', 'lcm/spy/ChannelData.java', 'lcm/spy/ZoomableChartScrollWheel.java', 'lcm/spy/SpyPlugin.java', 'lcm/spy/LCMTypeDatabase.java', 'lcm/logging/LogDiagnostic.java', 'lcm/logging/JScrubber.java', 'lcm/logging/JScrubberListener.java', 'lcm/logging/Log.java', 'lcm/logging/LogPlayer.java', 'lcm/lcm/LCMDataOutputStream.java', 'lcm/lcm/Provider.java', 'lcm/lcm/LCMDataInputStream.java', 'lcm/lcm/UDPMulticastProvider.java', 'lcm/lcm/LCMSubscriber.java', 'lcm/lcm/URLParser.java', 'lcm/lcm/MessageAggregator.java', 'lcm/lcm/MemqProvider.java', 'lcm/lcm/LCMEncodable.java', 'lcm/lcm/LCM.java', 'lcm/lcm/LogFileProvider.java', 'lcm/lcm/TCPProvider.java', 'lcm/lcm/TCPService.java']
lcm_java_doc_classes = ['lcm/lcm/LCM.java', 'lcm/lcm/LCMEncodable.java', 'lcm/lcm/LCMSubscriber.java', 'lcm/lcm/MessageAggregator.java', 'lcm/logging/Log.java']

proceed_build = 'yes'
error_messages = ['Java is not currently supported! Try at your own risk.']

if get_option('lcm_enable_java').disabled()
  proceed_build = disabler()
  error_messages = ['Meson configured with -Dlcm_enable_java=disabled']
elif not add_languages('java', required : get_option('lcm_enable_java').enabled())
  proceed_build = disabler()
  error_messages += ['Missing dependency java']
else
  jchart2d_dep = dependency('jchart2d', required : get_option('lcm_enable_java').enabled())
  if not jchart2d_dep.found()
    proceed_build = disabler()
    error_messages += ['Missing dependency jchart2d']
  endif
endif

if not is_disabler(proceed_build)
  jar('lcm-java',
    lcm_java_sources,
    install_dir : 'share/java')
  jar('doc-java',
    lcm_java_doc_classes,
    install_dir : 'docs/_build/javadocs')
endif

summary(
  {'Build Java bindings and utilities' : not is_disabler(proceed_build)},
  section : 'Options',
  bool_yn : true)
if error_messages.length() > 0
  summary({'- Build Java bindings and utilities error(s)': error_messages},
    section: 'Options')
endif

unset_variable('error_messages')
unset_variable('proceed_build')
