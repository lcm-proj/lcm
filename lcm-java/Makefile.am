JAVAROOT = build
JARFILE = lcm.jar
jchart2d_src_dir = $(srcdir)/jchart2d-code/src
jchart2d_ext_dir = $(srcdir)/jchart2d-code/ext
jide_jar = $(jchart2d_ext_dir)/jide-oss-2.9.7.jar
xmlgraphics_jar = $(jchart2d_ext_dir)/xmlgraphics-commons-1.3.1.jar
JAVACFLAGS = -source 1.5 -target 1.5 -classpath $(jide_jar):$(xmlgraphics_jar):$(jchart2d_src_dir)

jide_extract = $(JAVAROOT)/com/jidesoft/comparator/AlphanumComparator.class
xmlgraphics_extract = $(JAVAROOT)/org/apache/xmlgraphics/java2d/TextHandler.class

.PHONY classfiles: classdist_noinst.stamp classnoinst.stamp

$(JARFILE): $(jchart2d_jar) $(jide_extract) $(xmlgraphics_extract) classfiles
	rm -f $(JARFILE)
	$(JAR) cf $(JARFILE) -C $(JAVAROOT) .

jar_DATA = $(JARFILE) $(jchart2d_jar)

$(jide_extract): $(jide_jar)
	cd $(JAVAROOT) && $(JAR) xf ../$(jide_jar)

$(xmlgraphics_extract): $(xmlgraphics_jar)
	cd $(JAVAROOT) && $(JAR) xf ../$(xmlgraphics_jar)

jchart2d_cleanfiles = $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/rollover_c.gif \
    $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/rollover.gif \
    $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/pressed_c.gif \
    $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/pressed.gif \
    $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/selected_c.gif \
    $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/selected.gif \
    $(JAVAROOT)/com/jidesoft/plaf/xerto/icons/pressed_l.gif \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/refresh.png \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/new.png \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/desktop.png \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/myDocument.png \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/delete.png \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/slider_horizontal.gif \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/slider_vertical.gif \
    $(JAVAROOT)/com/jidesoft/plaf/basic/icons/home.png \
    $(JAVAROOT)/com/jidesoft/plaf/basic/folderChooser.properties \
    $(JAVAROOT)/com/jidesoft/plaf/basic/basic.properties \
    $(JAVAROOT)/com/jidesoft/plaf/office2007/icons/menu_checkbox.png \
    $(JAVAROOT)/com/jidesoft/plaf/office2007/icons/menu_item_bg.png \
    $(JAVAROOT)/com/jidesoft/plaf/office2003/icons/slider_horizontal.gif \
    $(JAVAROOT)/com/jidesoft/plaf/office2003/icons/slider_vertical.gif \
    $(JAVAROOT)/com/jidesoft/plaf/aqua/icons/rollover.gif \
    $(JAVAROOT)/com/jidesoft/plaf/aqua/icons/pressed.gif \
    $(JAVAROOT)/com/jidesoft/plaf/aqua/icons/selected.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_drag.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/file_text.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/direction_down.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/menu_radiobutton_vsnet.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_12.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_north.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_16.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_v_split.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_percentage.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/menu_checkbox_eclipse.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_3.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/direction_left.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_9.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_19.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_4.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/file_html.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/jide_logo.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/jide_logo_small_2.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_13.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/save.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_delete.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/view_html.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_5.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_15.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_move_east.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_20.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_drag_text_stop.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_south.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/jide32.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_vertical.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_blank.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/windows_new_horizontal_tab_group.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_17.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/file_java.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/menu_checkbox_vsnet.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/direction_dot.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_14.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_east.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_7.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_8.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_move_west.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_float.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/menu_radiobutton_eclipse.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/view_design.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_west.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_horizontal.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_6.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_18.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/windows_new_vertical_tab_group.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/direction_right.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/direction_up.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_11.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_drag_stop.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_h_split.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_2.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_tab.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_1.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/jide50.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/jide_logo_small.png \
    $(JAVAROOT)/com/jidesoft/icons/jide/dockableframe_10.gif \
    $(JAVAROOT)/com/jidesoft/icons/jide/cursor_drag_text.gif \
    $(JAVAROOT)/com/jidesoft/dialog/buttons.properties \
    $(JAVAROOT)/com/jidesoft/dialog/icons/selected-b16.gif \
    $(JAVAROOT)/com/jidesoft/dialog/icons/selected-c16.gif \
    $(JAVAROOT)/com/jidesoft/dialog/icons/blank-16.gif \
    $(JAVAROOT)/com/jidesoft/converter/converter.properties \
    $(JAVAROOT)/com/jidesoft/grouper/group.properties \
    $(JAVAROOT)/com/jidesoft/swing/icons/overlay_info.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/close.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/error.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/repeat.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/overlay_correct.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/highlightsS.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/highlightsRS.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/previousR.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/previous.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/highlightsR.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/closeR.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/overlay_question.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/highlights.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/highlightsD.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/nextD.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/nextR.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/previousD.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/next.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/overlay_attention.png \
    $(JAVAROOT)/com/jidesoft/swing/icons/overlay_error.png \
    $(JAVAROOT)/com/jidesoft/swing/swing.properties \
    $(JAVAROOT)/org/apache/xmlgraphics/fonts/zapfdingbats.txt \
    $(JAVAROOT)/org/apache/xmlgraphics/fonts/glyphlist.txt \
    $(JAVAROOT)/org/apache/xmlgraphics/image/writer/default-preferred-order.properties \
    $(JAVAROOT)/META-INF/NOTICE \
    $(JAVAROOT)/META-INF/MANIFEST.MF \
    $(JAVAROOT)/META-INF/LICENSE \
    $(JAVAROOT)/META-INF/services/org.apache.xmlgraphics.image.loader.spi.ImagePreloader \
    $(JAVAROOT)/META-INF/services/org.apache.xmlgraphics.image.writer.ImageWriter \
    $(JAVAROOT)/META-INF/services/org.apache.xmlgraphics.image.loader.spi.ImageConverter \
    $(JAVAROOT)/META-INF/services/org.apache.xmlgraphics.image.loader.spi.ImageLoaderFactory

clean-local:
	rm -f `find $(JAVAROOT) -name "*.class"`
	rm -f $(JARFILE)

bin_SCRIPTS = lcm-logplayer-gui lcm-spy
CLEANFILES = $(bin_SCRIPTS) $(jchart2d_cleanfiles)

lcm-logplayer-gui: Makefile
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx64m -Xms32m -ea -cp ${jardir}/lcm.jar lcm.logging.LogPlayer \$$*"
	chmod 755 $@

lcm-spy: Makefile
	@echo > $@ "#!/bin/sh"
	@echo >> $@ "exec java -server -Xincgc -Xmx128m -Xms64m -ea -cp ${jardir}/lcm.jar lcm.spy.Spy \$$*"
	chmod 755 $@

lcm_java_files = \
    lcm/lcm/LCMEncodable.java \
    lcm/lcm/LCM.java \
    lcm/lcm/LCMSubscriber.java \
    lcm/lcm/URLParser.java \
    lcm/lcm/MemqProvider.java \
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
    lcm/spy/ChartData.java \
    lcm/spy/ChannelData.java \
    lcm/spy/ZoomableChartScrollWheel.java \
    lcm/spy/ChannelData.java

jchart2d_java_files = \
    $(jchart2d_src_dir)/info/monitorenter/reflection/ObjectRecorder.java \
    $(jchart2d_src_dir)/info/monitorenter/reflection/ObjRecorder2Trace2DAdapter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/LabeledValue.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IAxisTitlePainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IRangePolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/RandomDataCollectorOffset.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/RandomDataCollectorTimeStamped.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/INameFilter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/FileFilterExtensions.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/PropertyFileStaticDataCollector.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/ADataCollector.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/io/AStaticDataCollector.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/views/ChartPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/views/ChartCoordinateView.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/ICodeBlock.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/ITrace2D.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/ZoomTest.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MultitraceStaticChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MultipleCharts.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/AxisScalePolicyManualTickChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MinimalStaticChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/StaticChartFill.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MultiTracing.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/AdvancedDynamicChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/Showcase.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/StaticCollectorChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/CoordinateViewChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/StaticChartWithBoxAndTitle.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MinimalStaticChartWithNanValues.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MultiAxisZoomTest.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MinimalDynamicChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/StaticChartDiscs.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/AdvancedStaticChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MinimalStaticChartLargeData.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/ZoomChartWithErrorBarsAndLogAxis.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/AntialiasingChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/VerticalStackedChartsWithParametricSpirals.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/Log10AxisChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/TraceTester.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/DynamicChartWithArithmeticMeanTrace.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/MultiAxesStaticChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/Log10AxisChartZoomable.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/LogAxisChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/StaticChartXAxisInverse.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/demos/StaticChartErrorBarLineDisc.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/labelformatters/LabelFormatterDate.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/labelformatters/LabelFormatterUnit.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/labelformatters/LabelFormatterSimple.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/labelformatters/LabelFormatterAutoUnits.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/labelformatters/LabelFormatterNumber.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/labelformatters/ALabelFormatter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/OneStringChooserPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/RangeChooserPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarPaintersPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarPainterEditPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarWizard.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarPolicyPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarDirectionPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/LayoutFactory.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/controls/TwoStringsChooserPanel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IAxisTickPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IErrorBarPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IPointPainterConfigurableUI.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/ITracePainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IAxis.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/TracePointProviderDefault.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IToolTipType.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/ZoomableChart.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IPointPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/layouts/FlowLayoutCorrectMinimumSize.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/ITracePoint2D.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/Chart2D.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ATrace2DActionErrorBarPolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetCustomColor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPainterActionSetSegmentPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionEnableAntialiasing.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSetGridColor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionEnableHighlighting.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetStroke.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionZindexDecrease.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AxisActionSetTitle.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AxisActionSetRange.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ZoomableChartZoomOutAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/JComponentActionSetCustomForeground.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetColor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionRemove.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AZoomableChartAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/JComponentActionSetCustomBackground.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AChartPanelAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ChartActionSetToolTipType.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPolicyMultiAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetZindex.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionZindexIncrease.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionAddRemoveTracePainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSetPaintLabels.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPainterActionEdit.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AxisActionSetFormatter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPolicyActionRemovePainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetName.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AErrorBarPolicyAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSetCustomGridColorSingleton.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AChart2DAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/JComponentActionSetCustomBackgroundSingleton.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionPrintSingleton.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPolicyActionShowWizard.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/PopupListener.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSaveImageSingleton.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionAddRemoveHighlighter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetVisible.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AJComponentAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSaveEpsSingletonApacheFop.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ChartPanelActionAddAnnotation.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/JComponentActionSetBackground.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/JComponentActionSetForeground.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionAddErrorBarPolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSetName.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPainterActionSetSegmentColor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionSetPhysicalUnits.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ErrorBarPolicyActionAddPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/JComponentActionSetCustomForegroundSingleton.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/ATrace2DAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AxisActionSetGrid.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Trace2DActionRemoveErrorBarPolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AxisActionSetTitleFont.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AAxisAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AxisActionSetRangePolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSetAxis.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/AErrorBarPainterAction.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/events/Chart2DActionSetCustomGridColor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IErrorBarPolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/AAxisTransformation.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/AAxis.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/scalepolicy/AxisScalePolicyManualTicks.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/scalepolicy/AxisScalePolicyAutomaticBestFit.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/scalepolicy/AxisScalePolicyTransformation.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/AxisLogE.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/AxisLog10.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/AxisLinear.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axis/AxisInverse.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IErrorBarPixel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IPointFinder.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/errorbars/ErrorBarPolicyRelative.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/errorbars/ErrorBarPolicyAbsoluteSummation.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/errorbars/ErrorBarPixel.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/errorbars/ErrorBarPainterLine.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/errorbars/ErrorBarPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/errorbars/AErrorBarPolicyConfigurable.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/RangePolicyUnbounded.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/ARangePolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/RangePolicyForcedPoint.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/RangePolicyHighestValuesForcedMin.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/RangePolicyHighestValues.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/RangePolicyMinimumViewport.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/rangepolicies/RangePolicyFixedViewport.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/TracePoint2D.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/dialogs/ModalDialog.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/pointpainters/PointPainterLine.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/pointpainters/APointPainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/pointpainters/PointPainterDisc.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/pointpainters/PointPainterVerticalBar.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axistickpainters/AxisTickPainterDefault.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/axistitlepainters/AxisTitlePainterDefault.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/annotations/bubble/AnnotationBubble.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/annotations/bubble/AnnotationCreatorBubble.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/annotations/AnnotationContentComponentDataValues.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/annotations/AAnnotationContentComponent.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/annotations/IAnnotationCreator.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/ITracePointProvider.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IAxisScalePolicy.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/IAxisLabelFormatter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DAxisSwap.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/ATrace2D.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DSimple.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DReplacing.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/computing/Trace2DArithmeticMeanY.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/computing/Trace2DArithmeticMean.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/computing/Trace2DArithmeticMeanX.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/ATracePainter.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/TracePainterDisc.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/TracePainterFill.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/TracePainterVerticalBar.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/TracePainterLine.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/TracePainterConfigurable.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/painters/TracePainterPolyline.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DLtdSorted.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DDebugger.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DLtdReplacing.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DLtd.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DBijective.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/chart/traces/Trace2DSorted.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/util/IColorDistributor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/util/ColorMutable.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/util/ColorIterator.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/util/Messages.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/util/HSBColor.java \
    $(jchart2d_src_dir)/info/monitorenter/gui/util/ColorIcon.java \
    $(jchart2d_src_dir)/info/monitorenter/util/UIUtil.java \
    $(jchart2d_src_dir)/info/monitorenter/util/FileUtil.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/RingBufferArray.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/IComparableProperty.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/TreeSetGreedy.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/RingBufferArrayFast.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/IRingBuffer.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/Entry.java \
    $(jchart2d_src_dir)/info/monitorenter/util/collections/ComparatorToString.java \
    $(jchart2d_src_dir)/info/monitorenter/util/SimpleDateFormatAnalyzer.java \
    $(jchart2d_src_dir)/info/monitorenter/util/TimeStampedValue.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitPeta.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitGiga.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitPico.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitFemto.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitMega.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/IUnitSystem.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitUnchanged.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitMilli.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitNano.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitFactory.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitSystemSI.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/AUnit.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitKilo.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitTera.java \
    $(jchart2d_src_dir)/info/monitorenter/util/units/UnitMicro.java \
    $(jchart2d_src_dir)/info/monitorenter/util/StringUtil.java \
    $(jchart2d_src_dir)/info/monitorenter/util/ExceptionUtil.java \
    $(jchart2d_src_dir)/info/monitorenter/util/Range.java \
    $(jchart2d_src_dir)/info/monitorenter/util/SerializationUtility.java \
    $(jchart2d_src_dir)/info/monitorenter/util/math/IntegerMutable.java \
    $(jchart2d_src_dir)/info/monitorenter/util/math/MathUtil.java

dist_noinst_JAVA = $(lcm_java_files) $(jchart2d_java_files)

$(dist_noinst_JAVA): build

build:
	mkdir build

man_MANS = lcm-spy.1 lcm-logplayer-gui.1

pkgconfigdir = $(libdir)/pkgconfig
pkgconfig_DATA = lcm-java.pc
EXTRA_DIST = lcm-java.pc.in lcm-spy.1 lcm-logplayer-gui.1 jchart2d-code
