set(jchart2d_version 3.2.2)

set(jchart2d_depends
  ext/build/profile.jar
  ext/build/junit-4.8.1.jar
  ext/build/proguard-4.5.1.jar
  ext/build/jipViewer.jar
)

macro(find_ext_jar VAR NAME)
  option(USE_SYSTEM_${VAR} "Use system-provided ${NAME}, if available" ON)
  if(USE_SYSTEM_${VAR})
    find_jar(${VAR}_JAR ${NAME})
  endif()

  if(USE_SYSTEM_${VAR} AND ${VAR}_JAR)
    list(APPEND jchart2d_depends ${${VAR}_JAR})
    set(LCM_EXT_${VAR}_JAR "${${VAR}_JAR}" PARENT_SCOPE)
  else()
    file(GLOB _ext_jar
      RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}/ext
      ext/${NAME}-*.jar)
    list(APPEND jchart2d_depends ext/${_ext_jar})
    set(LCM_EXT_${VAR}_JAR "\$ext/${_ext_jar}" PARENT_SCOPE)

    lcm_copy_file_target(lcm-ext-${NAME}-jar
      ${CMAKE_CURRENT_SOURCE_DIR}/ext/${_ext_jar}
      ${CMAKE_CURRENT_BINARY_DIR}/ext/${_ext_jar}
    )

    install(FILES ext/${_ext_jar} DESTINATION share/java)

    unset(_ext_jar)
  endif()
endmacro()

find_ext_jar(XMLGRAPHICS_COMMONS xmlgraphics-commons)
find_ext_jar(JIDE_OSS jide-oss)

set(jchart2d_sources
  src/info/monitorenter/reflection/ObjRecorder2Trace2DAdapter.java
  src/info/monitorenter/reflection/ObjectRecorder.java
  src/info/monitorenter/util/math/IntegerMutable.java
  src/info/monitorenter/util/math/MathUtil.java
  src/info/monitorenter/util/TimeStampedValue.java
  src/info/monitorenter/util/ExceptionUtil.java
  src/info/monitorenter/util/SerializationUtility.java
  src/info/monitorenter/util/SimpleDateFormatAnalyzer.java
  src/info/monitorenter/util/units/UnitFactory.java
  src/info/monitorenter/util/units/UnitTera.java
  src/info/monitorenter/util/units/UnitFemto.java
  src/info/monitorenter/util/units/UnitKilo.java
  src/info/monitorenter/util/units/UnitUnchanged.java
  src/info/monitorenter/util/units/IUnitSystem.java
  src/info/monitorenter/util/units/UnitGiga.java
  src/info/monitorenter/util/units/UnitMega.java
  src/info/monitorenter/util/units/UnitPico.java
  src/info/monitorenter/util/units/UnitMicro.java
  src/info/monitorenter/util/units/UnitPeta.java
  src/info/monitorenter/util/units/UnitNano.java
  src/info/monitorenter/util/units/AUnit.java
  src/info/monitorenter/util/units/UnitMilli.java
  src/info/monitorenter/util/units/UnitSystemSI.java
  src/info/monitorenter/util/collections/TreeSetGreedy.java
  src/info/monitorenter/util/collections/RingBufferArrayFast.java
  src/info/monitorenter/util/collections/IComparableProperty.java
  src/info/monitorenter/util/collections/IRingBuffer.java
  src/info/monitorenter/util/collections/ComparatorToString.java
  src/info/monitorenter/util/collections/Entry.java
  src/info/monitorenter/util/collections/RingBufferArray.java
  src/info/monitorenter/util/FileUtil.java
  src/info/monitorenter/util/StringUtil.java
  src/info/monitorenter/util/Range.java
  src/info/monitorenter/util/UIUtil.java
  src/info/monitorenter/gui/chart/dialogs/ModalDialog.java
  src/info/monitorenter/gui/chart/axistitlepainters/AxisTitlePainterDefault.java
  src/info/monitorenter/gui/chart/TracePointProviderDefault.java
  src/info/monitorenter/gui/chart/ITracePoint2D.java
  src/info/monitorenter/gui/chart/labelformatters/LabelFormatterDate.java
  src/info/monitorenter/gui/chart/labelformatters/LabelFormatterNumber.java
  src/info/monitorenter/gui/chart/labelformatters/LabelFormatterAutoUnits.java
  src/info/monitorenter/gui/chart/labelformatters/ALabelFormatter.java
  src/info/monitorenter/gui/chart/labelformatters/LabelFormatterSimple.java
  src/info/monitorenter/gui/chart/labelformatters/LabelFormatterUnit.java
  src/info/monitorenter/gui/chart/IErrorBarPainter.java
  src/info/monitorenter/gui/chart/rangepolicies/RangePolicyHighestValues.java
  src/info/monitorenter/gui/chart/rangepolicies/ARangePolicy.java
  src/info/monitorenter/gui/chart/rangepolicies/RangePolicyFixedViewport.java
  src/info/monitorenter/gui/chart/rangepolicies/RangePolicyHighestValuesForcedMin.java
  src/info/monitorenter/gui/chart/rangepolicies/RangePolicyUnbounded.java
  src/info/monitorenter/gui/chart/rangepolicies/RangePolicyMinimumViewport.java
  src/info/monitorenter/gui/chart/rangepolicies/RangePolicyForcedPoint.java
  src/info/monitorenter/gui/chart/IAxis.java
  src/info/monitorenter/gui/chart/IErrorBarPixel.java
  src/info/monitorenter/gui/chart/IAxisTickPainter.java
  src/info/monitorenter/gui/chart/IAxisLabelFormatter.java
  src/info/monitorenter/gui/chart/TracePoint2D.java
  src/info/monitorenter/gui/chart/ITracePainter.java
  src/info/monitorenter/gui/chart/ICodeBlock.java
  src/info/monitorenter/gui/chart/axis/scalepolicy/AxisScalePolicyTransformation.java
  src/info/monitorenter/gui/chart/axis/scalepolicy/AxisScalePolicyAutomaticBestFit.java
  src/info/monitorenter/gui/chart/axis/scalepolicy/AxisScalePolicyManualTicks.java
  src/info/monitorenter/gui/chart/axis/AxisLinear.java
  src/info/monitorenter/gui/chart/axis/AxisLog10.java
  src/info/monitorenter/gui/chart/axis/AAxisTransformation.java
  src/info/monitorenter/gui/chart/axis/AxisLogE.java
  src/info/monitorenter/gui/chart/axis/AxisInverse.java
  src/info/monitorenter/gui/chart/axis/AAxis.java
  src/info/monitorenter/gui/chart/events/AxisActionSetGrid.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSetGridColor.java
  src/info/monitorenter/gui/chart/events/ChartActionSetToolTipType.java
  src/info/monitorenter/gui/chart/events/Trace2DActionZindexDecrease.java
  src/info/monitorenter/gui/chart/events/Trace2DActionRemoveErrorBarPolicy.java
  src/info/monitorenter/gui/chart/events/Chart2DActionEnableAntialiasing.java
  src/info/monitorenter/gui/chart/events/Chart2DActionEnableHighlighting.java
  src/info/monitorenter/gui/chart/events/ErrorBarPolicyActionRemovePainter.java
  src/info/monitorenter/gui/chart/events/ErrorBarPainterActionSetSegmentPainter.java
  src/info/monitorenter/gui/chart/events/JComponentActionSetCustomBackgroundSingleton.java
  src/info/monitorenter/gui/chart/events/Trace2DActionZindexIncrease.java
  src/info/monitorenter/gui/chart/events/ATrace2DAction.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetPhysicalUnits.java
  src/info/monitorenter/gui/chart/events/AxisActionSetRange.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSaveImageSingleton.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetName.java
  src/info/monitorenter/gui/chart/events/Trace2DActionAddErrorBarPolicy.java
  src/info/monitorenter/gui/chart/events/ATrace2DActionErrorBarPolicy.java
  src/info/monitorenter/gui/chart/events/ErrorBarPainterActionSetSegmentColor.java
  src/info/monitorenter/gui/chart/events/JComponentActionSetCustomForeground.java
  src/info/monitorenter/gui/chart/events/JComponentActionSetBackground.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSetCustomGridColor.java
  src/info/monitorenter/gui/chart/events/Chart2DActionPrintSingleton.java
  src/info/monitorenter/gui/chart/events/JComponentActionSetForeground.java
  src/info/monitorenter/gui/chart/events/ErrorBarPolicyActionAddPainter.java
  src/info/monitorenter/gui/chart/events/AAxisAction.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSetCustomGridColorSingleton.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetColor.java
  src/info/monitorenter/gui/chart/events/Trace2DActionAddRemoveHighlighter.java
  src/info/monitorenter/gui/chart/events/AxisActionSetTitle.java
  src/info/monitorenter/gui/chart/events/AChartPanelAction.java
  src/info/monitorenter/gui/chart/events/AZoomableChartAction.java
  src/info/monitorenter/gui/chart/events/ErrorBarPolicyMultiAction.java
  src/info/monitorenter/gui/chart/events/AxisActionSetFormatter.java
  src/info/monitorenter/gui/chart/events/JComponentActionSetCustomForegroundSingleton.java
  src/info/monitorenter/gui/chart/events/AErrorBarPolicyAction.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSaveEpsSingletonApacheFop.java
  src/info/monitorenter/gui/chart/events/JComponentActionSetCustomBackground.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetCustomColor.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSetPaintLabels.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetZindex.java
  src/info/monitorenter/gui/chart/events/ZoomableChartZoomOutAction.java
  src/info/monitorenter/gui/chart/events/AxisActionSetRangePolicy.java
  src/info/monitorenter/gui/chart/events/PopupListener.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSetAxis.java
  src/info/monitorenter/gui/chart/events/ErrorBarPainterActionEdit.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetVisible.java
  src/info/monitorenter/gui/chart/events/AJComponentAction.java
  src/info/monitorenter/gui/chart/events/Chart2DActionSetName.java
  src/info/monitorenter/gui/chart/events/Trace2DActionAddRemoveTracePainter.java
  src/info/monitorenter/gui/chart/events/Trace2DActionRemove.java
  src/info/monitorenter/gui/chart/events/ChartPanelActionAddAnnotation.java
  src/info/monitorenter/gui/chart/events/Trace2DActionSetStroke.java
  src/info/monitorenter/gui/chart/events/AChart2DAction.java
  src/info/monitorenter/gui/chart/events/AErrorBarPainterAction.java
  src/info/monitorenter/gui/chart/events/AxisActionSetTitleFont.java
  src/info/monitorenter/gui/chart/events/ErrorBarPolicyActionShowWizard.java
  src/info/monitorenter/gui/chart/IAxisTitlePainter.java
  src/info/monitorenter/gui/chart/IAxisScalePolicy.java
  src/info/monitorenter/gui/chart/LabeledValue.java
  src/info/monitorenter/gui/chart/Chart2D.java
  src/info/monitorenter/gui/chart/ZoomableChart.java
  src/info/monitorenter/gui/chart/IPointPainterConfigurableUI.java
  src/info/monitorenter/gui/chart/IRangePolicy.java
  src/info/monitorenter/gui/chart/IPointPainter.java
  src/info/monitorenter/gui/chart/axistickpainters/AxisTickPainterDefault.java
  src/info/monitorenter/gui/chart/ITrace2D.java
  src/info/monitorenter/gui/chart/controls/LayoutFactory.java
  src/info/monitorenter/gui/chart/controls/TwoStringsChooserPanel.java
  src/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarWizard.java
  src/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarPainterEditPanel.java
  src/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarPolicyPanel.java
  src/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarDirectionPanel.java
  src/info/monitorenter/gui/chart/controls/errorbarwizard/ErrorBarPaintersPanel.java
  src/info/monitorenter/gui/chart/controls/RangeChooserPanel.java
  src/info/monitorenter/gui/chart/controls/OneStringChooserPanel.java
  src/info/monitorenter/gui/chart/IPointFinder.java
  src/info/monitorenter/gui/chart/traces/Trace2DLtd.java
  src/info/monitorenter/gui/chart/traces/ATrace2D.java
  src/info/monitorenter/gui/chart/traces/Trace2DBijective.java
  src/info/monitorenter/gui/chart/traces/Trace2DLtdSorted.java
  src/info/monitorenter/gui/chart/traces/Trace2DReplacing.java
  src/info/monitorenter/gui/chart/traces/computing/Trace2DArithmeticMean.java
  src/info/monitorenter/gui/chart/traces/computing/Trace2DArithmeticMeanY.java
  src/info/monitorenter/gui/chart/traces/computing/Trace2DArithmeticMeanX.java
  src/info/monitorenter/gui/chart/traces/Trace2DLtdReplacing.java
  src/info/monitorenter/gui/chart/traces/Trace2DAxisSwap.java
  src/info/monitorenter/gui/chart/traces/Trace2DSorted.java
  src/info/monitorenter/gui/chart/traces/Trace2DSimple.java
  src/info/monitorenter/gui/chart/traces/painters/TracePainterConfigurable.java
  src/info/monitorenter/gui/chart/traces/painters/TracePainterVerticalBar.java
  src/info/monitorenter/gui/chart/traces/painters/TracePainterPolyline.java
  src/info/monitorenter/gui/chart/traces/painters/ATracePainter.java
  src/info/monitorenter/gui/chart/traces/painters/TracePainterLine.java
  src/info/monitorenter/gui/chart/traces/painters/TracePainterDisc.java
  src/info/monitorenter/gui/chart/traces/painters/TracePainterFill.java
  src/info/monitorenter/gui/chart/traces/Trace2DDebugger.java
  src/info/monitorenter/gui/chart/pointpainters/PointPainterLine.java
  src/info/monitorenter/gui/chart/pointpainters/PointPainterDisc.java
  src/info/monitorenter/gui/chart/pointpainters/PointPainterVerticalBar.java
  src/info/monitorenter/gui/chart/pointpainters/APointPainter.java
  src/info/monitorenter/gui/chart/IToolTipType.java
  src/info/monitorenter/gui/chart/layouts/FlowLayoutCorrectMinimumSize.java
  src/info/monitorenter/gui/chart/demos/Showcase.java
  src/info/monitorenter/gui/chart/demos/StaticCollectorChart.java
  src/info/monitorenter/gui/chart/demos/MultitraceStaticChart.java
  src/info/monitorenter/gui/chart/demos/AdvancedStaticChart.java
  src/info/monitorenter/gui/chart/demos/VerticalStackedChartsWithParametricSpirals.java
  src/info/monitorenter/gui/chart/demos/MinimalDynamicChart.java
  src/info/monitorenter/gui/chart/demos/StaticChartErrorBarLineDisc.java
  src/info/monitorenter/gui/chart/demos/AxisScalePolicyManualTickChart.java
  src/info/monitorenter/gui/chart/demos/StaticChartXAxisInverse.java
  src/info/monitorenter/gui/chart/demos/StaticChartDiscs.java
  src/info/monitorenter/gui/chart/demos/ZoomTest.java
  src/info/monitorenter/gui/chart/demos/Log10AxisChart.java
  src/info/monitorenter/gui/chart/demos/ZoomChartWithErrorBarsAndLogAxis.java
  src/info/monitorenter/gui/chart/demos/DynamicChartWithArithmeticMeanTrace.java
  src/info/monitorenter/gui/chart/demos/LogAxisChart.java
  src/info/monitorenter/gui/chart/demos/StaticChartWithBoxAndTitle.java
  src/info/monitorenter/gui/chart/demos/AdvancedDynamicChart.java
  src/info/monitorenter/gui/chart/demos/MinimalStaticChartLargeData.java
  src/info/monitorenter/gui/chart/demos/AntialiasingChart.java
  src/info/monitorenter/gui/chart/demos/Log10AxisChartZoomable.java
  src/info/monitorenter/gui/chart/demos/MinimalStaticChart.java
  src/info/monitorenter/gui/chart/demos/StaticChartFill.java
  src/info/monitorenter/gui/chart/demos/MultipleCharts.java
  src/info/monitorenter/gui/chart/demos/MultiAxesStaticChart.java
  src/info/monitorenter/gui/chart/demos/MultiTracing.java
  src/info/monitorenter/gui/chart/demos/TraceTester.java
  src/info/monitorenter/gui/chart/demos/MinimalStaticChartWithNanValues.java
  src/info/monitorenter/gui/chart/demos/CoordinateViewChart.java
  src/info/monitorenter/gui/chart/demos/MultiAxisZoomTest.java
  src/info/monitorenter/gui/chart/ITracePointProvider.java
  src/info/monitorenter/gui/chart/errorbars/ErrorBarPainterLine.java
  src/info/monitorenter/gui/chart/errorbars/AErrorBarPolicyConfigurable.java
  src/info/monitorenter/gui/chart/errorbars/ErrorBarPolicyRelative.java
  src/info/monitorenter/gui/chart/errorbars/ErrorBarPainter.java
  src/info/monitorenter/gui/chart/errorbars/ErrorBarPolicyAbsoluteSummation.java
  src/info/monitorenter/gui/chart/errorbars/ErrorBarPixel.java
  src/info/monitorenter/gui/chart/views/ChartCoordinateView.java
  src/info/monitorenter/gui/chart/views/ChartPanel.java
  src/info/monitorenter/gui/chart/IErrorBarPolicy.java
  src/info/monitorenter/gui/chart/annotations/IAnnotationCreator.java
  src/info/monitorenter/gui/chart/annotations/bubble/AnnotationCreatorBubble.java
  src/info/monitorenter/gui/chart/annotations/bubble/AnnotationBubble.java
  src/info/monitorenter/gui/chart/annotations/AnnotationContentComponentDataValues.java
  src/info/monitorenter/gui/chart/annotations/AAnnotationContentComponent.java
  src/info/monitorenter/gui/chart/io/INameFilter.java
  src/info/monitorenter/gui/chart/io/RandomDataCollectorTimeStamped.java
  src/info/monitorenter/gui/chart/io/RandomDataCollectorOffset.java
  src/info/monitorenter/gui/chart/io/FileFilterExtensions.java
  src/info/monitorenter/gui/chart/io/ADataCollector.java
  src/info/monitorenter/gui/chart/io/PropertyFileStaticDataCollector.java
  src/info/monitorenter/gui/chart/io/AStaticDataCollector.java
  src/info/monitorenter/gui/util/HSBColor.java
  src/info/monitorenter/gui/util/ColorIterator.java
  src/info/monitorenter/gui/util/ColorIcon.java
  src/info/monitorenter/gui/util/IColorDistributor.java
  src/info/monitorenter/gui/util/ColorMutable.java
  src/info/monitorenter/gui/util/Messages.java
  src/AffineTransformBug.java
)

add_jar(jchart2d
  OUTPUT_NAME jchart2d-${jchart2d_version}
  INCLUDE_JARS ${jchart2d_depends}
  SOURCES ${jchart2d_sources}
)

install_jar(jchart2d share/java)
