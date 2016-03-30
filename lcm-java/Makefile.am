JAVAROOT = $(abs_builddir)/build
JARFILE = lcm.jar
jchart2d_src_dir = $(srcdir)/jchart2d-code/src
jide_jar = $(srcdir)/jchart2d-code/ext/jide-oss-2.9.7.jar
xmlgraphics_jar = $(srcdir)/jchart2d-code/ext/xmlgraphics-commons-1.3.1.jar
abs_jchart2d_src_dir = $(abs_srcdir)/jchart2d-code/src
abs_jide_jar = $(abs_srcdir)/jchart2d-code/ext/jide-oss-2.9.7.jar
abs_xmlgraphics_jar = $(abs_srcdir)/jchart2d-code/ext/xmlgraphics-commons-1.3.1.jar
JAVACFLAGS = -source 1.5 -target 1.5 -classpath $(abs_jide_jar):$(abs_xmlgraphics_jar):$(abs_jchart2d_src_dir)

.PHONY classfiles: classdist_noinst.stamp classnoinst.stamp

$(JARFILE): $(jchart2d_jar) $(jide_jar) $(xmlgraphics_jar) $(JAVAROOT) classfiles
	rm -f $(JARFILE)
	cd $(JAVAROOT) && $(JAR) xf $(abs_jide_jar) # && mv $(JAVAROOT)/META-INF $(JAVAROOT)/META-INF.jide
	cd $(JAVAROOT) && $(JAR) xf $(abs_xmlgraphics_jar) # && mv $(JAVAROOT)/META-INF $(JAVAROOT)/META-INF.xmlgraphics
	$(JAR) cf $(JARFILE) -C $(JAVAROOT) .

jar_DATA = $(JARFILE) $(jchart2d_jar)

lcm-logplayer-gui: Makefile
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx64m -Xms32m -ea -cp ${jardir}/lcm.jar lcm.logging.LogPlayer \$$*"
	chmod 755 $@

lcm-spy: Makefile
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Djava.net.preferIPv4Stack=true -Xincgc -Xmx128m -Xms64m -ea -cp ${jardir}/lcm.jar lcm.spy.Spy \$$*"
	chmod 755 $@

javaroot_cleanfiles = $(shell find $(JAVAROOT) -type f)

bin_SCRIPTS = lcm-logplayer-gui lcm-spy
CLEANFILES = $(bin_SCRIPTS) $(javaroot_cleanfiles) $(JARFILE)

clean-local:
	rm -rf $(JAVAROOT)

lcm_java_files = $(shell find $(srcdir)/lcm -type f -name "*.java" -not -path "*/\.*")

jchart2d_java_files = $(shell find $(jchart2d_src_dir) -type f -name "*.java"  -not -path "*/\.*")

dist_noinst_JAVA = $(lcm_java_files) $(jchart2d_java_files)

$(dist_noinst_JAVA): $(abs_builddir)/build

$(JAVAROOT):
	mkdir -p $(abs_builddir)/build

man_MANS = lcm-spy.1 lcm-logplayer-gui.1

pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lcm-java.pc
EXTRA_DIST = lcm-java.pc.in lcm-spy.1 lcm-logplayer-gui.1 jchart2d-code
