
JAVAROOT = .
JARFILE = lcm$(apiversion).jar

$(JARFILE): classdist_noinst.stamp
	rm -f $(JARFILE)
	$(JAR) cf $(JARFILE) -C $(JAVAROOT) `find $(JAVAROOT) -name "*.class"`

clean-local:
	rm -f `find $(JAVAROOT) -name "*.class"`
	rm -f $(JARFILE)

if HAVE_JAVA
jar_DATA = $(JARFILE)

bin_SCRIPTS = lcm-jlogplayer lcm-lcspy
CLEANFILES = $(bin_SCRIPTS)

lcm-jlogplayer:
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx512m -Xms256m -ea -cp ${jardir}/lcm.jar lcm.logging.LogPlayer \$$*"
	chmod 755 $@

lcm-lcmspy:
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx512m -Xms256m -ea -cp ${jardir}/lcm.jar lcm.lcspy.LCMSpy \$$*"
	chmod 755 $@

install-data-hook:
# install versionless jar symlink
	cd $(DESTDIR)$(jardir) && \
		mv -f $(JARFILE) lcm$(apiversion)-$(VERSION).jar && \
		$(LN_S) lcm$(apiversion)-$(VERSION).jar $(JARFILE)

dist_noinst_JAVA = \
    lcm/lcm/LCMEncodable.java \
    lcm/lcm/LCM.java \
    lcm/lcm/LCMSubscriber.java \
    lcm/lcm/URLParser.java \
    lcm/lcm/Provider.java \
    lcm/lcm/UDPMulticastProvider.java \
    lcm/lcm/LogFileProvider.java \
    lcm/util/BufferedRandomAccessFile.java \
    lcm/util/ColorMapper.java \
    lcm/util/TableSorter.java \
    lcm/util/ParameterListener.java \
    lcm/util/ParameterGUI.java \
    lcm/util/JImage.java \
    lcm/logging/JScrubber.java \
    lcm/logging/JScrubberListener.java \
    lcm/logging/Log.java \
    lcm/logging/LogDiagnostic.java \
    lcm/logging/LogPlayer.java \
    lcm/lcspy/LCMSpyPlugin.java \
    lcm/lcspy/LCMHandlerDatabase.java \
    lcm/lcspy/ObjectPanel.java \
    lcm/lcspy/LCMSpy.java \
    lcm/lcspy/ChannelData.java 

endif

pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lcm-java.pc
EXTRA_DIST = lcm-java.pc.in
