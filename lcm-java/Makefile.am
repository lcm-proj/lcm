JAVAROOT = build
JARFILE = lcm.jar
JAVACFLAGS = -source 1.5 -target 1.5

.PHONY classfiles: classdist_noinst.stamp classnoinst.stamp

$(JARFILE): classfiles
	rm -f $(JARFILE)
	$(JAR) cf $(JARFILE) -C $(JAVAROOT) .

jar_DATA = $(JARFILE)

clean-local:
	rm -f `find $(JAVAROOT) -name "*.class"`
	rm -f $(JARFILE)

bin_SCRIPTS = lcm-logplayer-gui lcm-spy
CLEANFILES = $(bin_SCRIPTS)

lcm-logplayer-gui: Makefile
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx64m -Xms32m -ea -cp ${jardir}/lcm.jar lcm.logging.LogPlayer \$$*"
	chmod 755 $@

lcm-spy: Makefile
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx128m -Xms64m -ea -cp ${jardir}/lcm.jar lcm.spy.Spy \$$*"
	chmod 755 $@

dist_noinst_JAVA = \
    lcm/lcm/LCMEncodable.java \
    lcm/lcm/LCM.java \
    lcm/lcm/LCMSubscriber.java \
    lcm/lcm/URLParser.java \
    lcm/lcm/Provider.java \
    lcm/lcm/TCPProvider.java \
    lcm/lcm/UDPMulticastProvider.java \
    lcm/lcm/TCPService.java \
    lcm/lcm/LogFileProvider.java \
	lcm/lcm/TCPProvider.java \
	lcm/lcm/MessageAggregator.java \
	lcm/lcm/LCMDataInputStream.java \
	lcm/lcm/LCMDataOutputStream.java \
    lcm/util/BufferedRandomAccessFile.java \
    lcm/util/ColorMapper.java \
    lcm/util/TableSorter.java \
    lcm/util/ParameterListener.java \
    lcm/util/ParameterGUI.java \
    lcm/util/JImage.java \
    lcm/util/ClassDiscoverer.java \
    lcm/logging/JScrubber.java \
    lcm/logging/JScrubberListener.java \
    lcm/logging/Log.java \
    lcm/logging/LogDiagnostic.java \
    lcm/logging/LogPlayer.java \
    lcm/spy/Spy.java \
    lcm/spy/SpyPlugin.java \
    lcm/spy/LCMTypeDatabase.java \
    lcm/spy/ObjectPanel.java \
    lcm/spy/ChannelData.java

$(dist_noinst_JAVA): build

build:
	mkdir build

man_MANS = lcm-spy.1 lcm-logplayer-gui.1

pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lcm-java.pc
EXTRA_DIST = lcm-java.pc.in lcm-spy.1 lcm-logplayer-gui.1
