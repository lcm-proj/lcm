
JAVAROOT = .
JARFILE = lcm.jar

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

lcm-lcspy:
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx512m -Xms256m -ea -cp ${jardir}/lcm.jar lcm.lcspy.LCSpy \$$*"
	chmod 755 $@

dist_noinst_JAVA = \
    lcm/lc/LCEncodable.java \
    lcm/lc/LC.java \
    lcm/lc/LCSubscriber.java \
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
    lcm/lcspy/LCSpyPlugin.java \
    lcm/lcspy/LCMHandlerDatabase.java \
    lcm/lcspy/ObjectPanel.java \
    lcm/lcspy/LCSpy.java \
    lcm/lcspy/ChannelData.java
endif
