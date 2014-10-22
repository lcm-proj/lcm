/*
 * File   : $Source: /cvsroot/jchart2d/jchart2d/history.txt,v $
 * Date   : $Date: 2011/02/13 06:42:53 $
 * Version: $Revision: 1.216 $
 *
 * This file is part of jchart2d -
 * the Open Source real time charting library.
 *
 * Copyright (c) 2002 - 2011 Achim Westermann
 */

Quick intro

Use this library for integration of charts into your Java application. 
To just view a chart open a shell (under windows cmd, penguins
will know), cd to the directory you downloaded this file to 
and type 
"java - jar jchart2d-3.2.1.jar". 
You may also want to look at other demos. The may be launched by typing 
"java -cp <jarfilelist> <fullyqualifiedclassname>". 
E.g.: "java -cp jchart2d-3.2.1.jar  info.monitorenter.gui.chart.demos.MinimalDynamicChart".
For full support (eps output, range chooser) also incorporate the 3rd party libraries to the 
classplath: 
linux: "java -cp jchart2d-3.2.1.jar:xmlgraphics-commons-1.3.1.jar:bislider.jar info.monitorenter.gui.chart.demos.AdvancedDynamicChart" 
Windows: "java -cp jchart2d-3.2.1.jar;xmlgraphics-commons-1.3.1.jar;bislider.jar info.monitorenter.gui.chart.demos.AdvancedDynamicChart" 


History of changes for the jchart2d project.

Changes are chronologically ordered from top (most recent) 
to bottom (least recent).

Legend:
! New Feature
* Bug fixed, (unreleased) points out that this bug was never released. 
- General comment
o API change that requires refactoring. 
  Note that refactoring of protected code intended for library 
  internal details will not be listed.
  
jchart2d-3.2.2 - September, 24th, 2011
  
! New AxisScalePolicyManualTicks for controlling the ticks by hand. 
! Extended IAxis to have pluggable scale policies. See IAxis.setAxisScalePolicy(IAxisScalePolicy). 
! ChartPanel now allows to drop the "adeptUI2Chart" feature of the popup menus. Use the new 
  constructor: public ChartPanel(final Chart2D chart, final boolean adaptUI2Chart). 
  Thx for contribution to: Poik Spirit. 
* Fixed bug #3289727: Dynamic chart with highlighting does not clear points.
* Fixed potential memory leak in case traces with highlighting enabled are removed. 
* Fixed bug #3308052: Test sources missing from 3.2.1 source file download tree.
* Fixed bug #3352480: Chart2D.removeTrace(ITrace2D) does not work after changing it's state (e.g. via setZindex(Integer)).
* Fixed bug #3405539: PointPainterDisc.setColorFill is invoked with the same color as the trace color, the disc fill will not happen. 
  If the color is different, then fill happens.
* Fixed bug #3357215: Stacked vertical charts not aligned.
* Fixed bug #3291886: AAxisTransformation transforms scale values (and duplicate #3406961: Labels on AxisLog10 axis are wrong). Credits for the 
  inspiring contribution by Bill Schoolfield.
* Fixed bug #3307611: IAxis.setPaintScale(false) turns off grid. Both properties work individually now (decoupled).
o Changed Chart2D method signature: 
  public final void removeTrace(final ITrace2D points)
  to 
  public final boolean removeTrace(final ITrace2D points)
  to return if removing was successful. 
o Decoupled IAxis.setPaintGrid(boolean) from IAxis.setPaintScale(boolean). Earlier versions would turn on paintScale in case grid was turned on. 
o IAxis has a new generic parameter. 
  1. Wherever you used 
  IAxis axis = chart.getAxis();
  replace it with: 
  IAxis<?> axis = chart.getAxis();
  2. Whenever you want to instantiate an axis: 
  IAxis<?> axis = new AxisLinear<IAxisScalePolicy>();
  The AAxisTransformation narrows this type (to ensure correct labels): 
  IAxis<?> axisTransformed = new AxisLog10<AxisScalePolicyTransformation>();
  3. If you want to reuse the charts axes and set a scale policy, a dirty cast is needed: 
  IAxis<IAxisScalePolicy> xAxis = (IAxis<IAxisScalePolicy>)chart.getAxisX();
  xAxis.setAxisScalePolicy(new AxisScalePolicyManualTicks()); 
  Without the cast you could not set the scale policy. 
  
jchart2d-3.2.1 - April, 16th, 2011

! Added context menu entry for enabling/disabling antialiasing. 
! Added various context menu entries for setting label formatters to axes.
! Stroke, color and transparency now may be specified for point highlighters or point painters. 
  The latter will override the settings of the trace to paint.  
! Custom IPointFinders to find the nearest point of a chart corresponding to a mouse event 
  may be used.   
! Automatic enabling/disabling of expensive trace highlighting when trace highlighters are 
  added / removed. 
! Added simplified highlighting menu to trace menu (right click on trace name) too.
! Changed color management of error bar painters to use the new configurable point painter's color.
! Updated to Proguard Version 4.5.1.
! Synchronized direct changes to the trace point instances to avoid those not being reflected in UI (due to consumed repaint requests while paint cylce still running). 
  This should improve point highlighting (disallow forgetting to show changed highlighted point). 
! Switched from bislider to JIDE oss for the range slider. 
* Fixed unreported bug when setting a new LabelFormatterAutoUnits not calculating the proper unit. 
* Fixed unreported bug when using currency/percent formatting in label formatter: labels would overwrite each other.
* Fixed issue with wrong equals / hashcode implementations (menu items for highlighters/painters would not update state if API call should cause that). 
* Fixed issue with duplicate property change listeners on for menu items. 
* Fixed issue with ClassCastException when changing the end point painter of an error bar painter (duplicate property constant). 
* Fixed bug #1930893 again: ZoomableChart now interpolates the connections of two 
  invisible points if their connection crosses the zooming window. 
* Fixed bug #3064089: Zoom does not work for multi axes plots (Contribution by Anadi Mishra).
* Fixed bug: Chart2D.setAxis<Dimension><Location>(AAxis, int) was not contained in the jar file (proguard shrinker issue).
* Fixed issue with remaining highlighted point when removing point highlighter from trace. 
* Fixed NPE when calling TracePoint2D.setLocation(double, double) before point has been added to trace. 
* Fixed issue with NPE when highlighting an interpolated point. 
* Fixed issue with point highlighting leaving several highlighted points if there is more than 1 trace and your mouse gets 
  nearer to another trace than the previous trace being highlighted. 
* Fixed issue with changed point highlighters on a trace being reported to the Chart instead of it's point highlighting worker 
  (thus resulting in no proper update of the changed highlighter on the highlighted points). 
* Fixed bug #3127284: Using the ChartPanel wrapper, right click in the label of a trace to change its name. Press "cancel". The name disappears from the GUI.
* Fixed unreleased issue: point highlighting tracking in the chart is not enabled/disabled automatically if the first/last point highlighter was added / removed. 
* Fixed unreported issue: Trying to add points from a separate thread before a trace is added to a chart runs into silent deadlock preventing the chart from 
  becoming visible / reactive. 
* Fixed possible deadlock when launching many threads at startup while ChartPanel is used (Container.getTreelock() issue in addTrace of Chart2D). 
* Fixed bug #3164863: LabelFormatter does not chooses femto range for very small values ( < 10^-16).
* Fixed unreported bug: ChartPanel looses trace labels when setting a new axis. 
* Wiped out duplicate property change events for basic awt properties "font", "background" and "foreground" of Chart2D.
* Let Chart2D relay the event event IAxis.PROPERTY_ADD_REMOVE_TRACE to it's outside listeners: This was documented in javadoc and 
  made sense as outside users don't want to interact with internal object models. Register for this event via: 
  chart.addPropertyChangeListener(Chart2D.PROPERTY_ADD_REMOVE_TRACE, this).
o Renamed info.monitorenter.MathUtil to info.monitorenter.util.math.MathUtil.
o Renamed info.monitorenter.math.IntegerReusable to info.monitorenter.math.IntegerMutable. 
o Simplified API: dropped interface IPointHighlighter. Use IPointPainter wherever you used IPointHighlighter before. 
o Simplified API: dropped class APointHighlighter. Extend APointPainter wherever you used APointHighlighter before. 
o Simplified API: dropped class PointHighlighterConfigurable. Use the raw PointPainter argument from it's constructor instead. 
  Example: "new PointPainterConfigurable(new PointPainterDisc(20), true)" -> "new PointPainterDisc(20)" 
o To prevent deadlocks at startup now every trace has to be first added to a chart before: adding points, setting point highlighters, setting error bar policies to the trace. 
o If a trace label (the combination of property name and property physical units) is set to null or "" the ChartPanel will show an <unnamed> label that still allows
  to access the right-click context menu entry for various trace configurations.
o For users who implemented their own IToolTip that would also control which IPointHighlighter to use: 
  Point highlighting and tool tips were a bit mixed before and are now completely separated. 
  Management of highlighting has changed a lot. This is now all done by the inner PointHighlighter instance of the chart. You should 
  not care for management of point highlighting. By now there is no reason to recode a IToolTip that mixes both functionalities. 
  Have a look at info.monitorenter.gui.chart.demo.AdvancedStaticChart. 

jchart2d-3.2.0 - June, 2nd, 2010

* Fixed bug #2666561: ErrorBarPolicyAbsoluteSummation ignores absolute y value setting.
* Fixed bug #2689824: not enough ticks on Y axis. Check test info.monitorenter.gui.chart.labelformatters.TestLabelFormatterAutoUnits. 
* Fixed ErrorBarPolicies to render the error bars for the previous point on the next. 
* Fixed issue with vertical/horizontal lines interpolated in y / x dimension only from vanishing when 
  using a fixed range policy ( -> zooming).
* Fixed issue with arithmetic mean trace: x values were shifted illegal.
* Fixed unreported bug: NullPointerException when adding first error bar policy. 
* Fixed unreported bug: Chart frame was set to a tiny size after adding error bar painter in modal dialog. 
* Fixed various demos that would not work because points were added to traces that had not been added to the chart first. 
* Fixed bug #2605832: Chart2D.removeTrace() not working properly.
  assigned to a chart before. 
* Fixed bug #2568439: trace.setZIndex() seems to produce random results. A higher z-index will bring 
  a trace more to the front. 
* Fixed unreported issue with unpredictable paint order of trace painters. 
* Fixed unreported issue with chart popup menu (right click) not working any more. 
* Fixed unreported issue with context menus not adapting to foreground and background of chart. 
* Fixed unreported jdk 1.5 incompatibility. 
* Fixed unreported issue with grid paint state lost when setting a new axis (that replaces the previous ones. 
* Fixed bug #2990054: Axis properties change when using the "Chart"-Menu.
* Fixed unreported issue with Axis not rescaling (->LabelFormatterAutoUnits not picking the correct unit for labels) when setting new Axis to chart. 
* Fixed issue #2948043:Tics dont scoll properly in dynamic graphs.
* Fixed issue #1867872: Jumping of first x-axis gridline (contribution by Nobody/Anonymous).
* Fixed issue with Double.NaN points beeing plotted: Traces with this are discontinued now (contribution by Horea Haitonic). 
* Fixed issue with two points not intersecting the chart's viewport at all being painted (dots at edge or wrong error bars).
* Fixed issue #2808772: Zoom with errorbar displayed incorrectly. 
! Antialiasing (smoother edges) may be turned on for Chart2D: Use method setUseAntialiasing(true).
! New Trace2DArithmeticMeanX / Trace2DArithmeticMeanY that build only the mean of latest n x / y values.
! Added feature #2492037: Set visibility of axes. Contribution by Horea Haitonic.
! Extended possible error bar range for absolute errors to 10^6. 
! Extracted interface of TracePoint2D to ITracePoint2D and use it wherever possible (feature request 2097852). 
  Use Char2D.setTracePointProvider(ITracePointProvider) to use your custom implementation.
! Custom Tool Tip Implementations may be used (feature request 2694745). 
  Use the interface IToolTipType, core implementations from Chart2D.ToolTipType still available.
! Context menu entries for selecting tool tip type. 
! Every trace point now may have several additional IPointHighlighter for additional rendering. 
  See ITracePoint.getAdditionalPointHighlighter(). This is an entry point for custom rendering of trace points. 
! Highlighting of tracepoints is turned off by default. Use chart.enablePointHighlighting(true). Also configure 
  the highlighter per trace via:  trace.setPointHighlighter(new PointHighlighterConfigurable(new PointPainterDisc(8),true)).
! New context menu entry for enabling/disabling highlighting of tracepoints at chart level.
! Improved EPS output: Switched from jlibeps to apache-xmlgraphics-commons: 
  License remains free, EPS is created faster and with java 1.6 output is true vectors (no pixelation).  	
! Deactivated the Axis->Axis X/Y -> Range menu item in case bislider.jar is missing on the classpath.
! Added tool tips explaining disabled context menu entries (Save EPS, Range). 
! New context menu entry for adding/removing point highlighters per trace. 
! New property change events for replacing axes via the setAxis<Dimension><Direction>(AAxis, int) fired. 
- Tool tips are disabled by default (performance): Use chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS); 
  or one of the other constants / your custom tool tip type to turn them on. 
- Added more generics. 
- Reordered chart menu to show all grid-relevant controls below a new grid menu. 
o Every trace has to be added to a chart first before adding points (since 3.1.0)!!!
o ITracePoint.setHighlight(boolean) has been removed: Use ITracePoint.getAdditionalPointHighlighters() and add instances to the result instead.
o boolean ITracePoint.isHighlight() has been removed: Use ITracePoint.getAdditionalPointHighlighters().size() > 0 instead.
o Renamed method 
  public JMenu createMenu(final ChartPanel chartPanel, final boolean adaptUI2Chart)
  in class info.monitorenter.gui.chart.controls.LayoutFactory
  to 
  public JMenu createChartMenu(final ChartPanel chartPanel, final boolean adaptUI2Chart)
o Renamed method 
  public JMenuBar createMenuBar(final ChartPanel chartPanel, final boolean adaptUI2Chart)
  in class info.monitorenter.gui.chart.controls.LayoutFactory
  to 
  public JMenuBar createChartMenuBar(final ChartPanel chartPanel, final boolean adaptUI2Chart)
o Renamed method 
  public void createPopupMenu(final ChartPanel chartpanel, final boolean adaptUI2Chart) 
  in class info.monitorenter.gui.chart.controls.LayoutFactory
  to 
  public void createChartPopupMenu(final ChartPanel chartpanel, final boolean adaptUI2Chart) 
o Added method 
  public String getDescription() 
  to interface info.monitorenter.gui.chart.IToolTipType 
o Replaced method 
  public List<IAxis> setAxisXBottom(AAxis) 
  by 
  public IAxis setAxisXBottom(AAxis, int) 
  in class Chart2D. 
o Replaced method 
  public List<IAxis> setAxisXTop(AAxis) 
  by 
  public IAxis setAxisXTop(AAxis, int) 
  in class Chart2D. 
o Replaced method 
  public List<IAxis> setAxisYLeft(AAxis) 
  by 
  public IAxis setAxisYLeft(AAxis, int) 
  in class Chart2D. 
o Replaced method 
  public List<IAxis> setAxisYRight(AAxis) 
  by 
  public IAxis setAxisYRight(AAxis, int) 
  in class Chart2D. 

jchart2d-3.1.0 - December, 2nd, 2008

* Fixed bug #2016296: Creating an instance of Chart2D prevents the application from stopping.
* info.monitorenter.gui.uti.ColorIterator was missing in jar file. 
* Fixed issue with missing menu entries for frame menu bar (compared to items in popup menu).
* Fixed unreported bug: Axis title for y axes on right side were not painted. 
* Fixed unreported unreleased bug: popup menu would not work any more. 
* Fixed bug: missing constants in binary jar. 
! Data points may be highlighted now. Use: TracePoint.setHighlight(true);.
  Configure the way of highlighting (example): chart.setPointHighlighter(new PointPainterDisc(8));
! Traces may be traced visually now by tool tips that mark a data point. 
  Use: chart.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
! Axis titles may now have a custom color. 
! Added method for finding minimum normalized Manhattan distance of points of a trace. 
! Chart may now be saved to an encapsulated postscript file.
! Chart may now be printed via a print dialog. 
! Updated coding conventions (use helpful annotations). 
! It is possible to deactivate labels for traces (by setting name and physical units to "" or null).
! The binary distribution / build has been changed: 3rd party libraries are not contained in the core jar any more. 
! Switched to proguard version 4.2. 
o Deactivated tool tip by default: Use chart.setToolTipType(Chart2D.ToolTipType.DATAVALUES); 
  for classic behavior. But note that this will only work when using one left y axis and 
  one bottom x axis. 


jchart2d-3.0.0 - June, 1st, 2008

* Don't incorporate empty traces in extremum search.
* Fixed bug #1821361: Output of ATrace2D.setName() subject to race condition?
* Fixed unreleased bug with increase of minimum y value in dynamic mode (add point at runtime). 
* Fixed unreleased NPE when setting Axis X. 
* Fixed unreleased bug for set a new axis on chart forgetting to transfer the traces from the 
  old to the new one. 
* Fixed unreleased issue with missing repaint / property change event -> ChartPanel trace titles 
  in case of removing a trace.
* Fixed unreleased issue with setting new Axis causing NullPointerException due to missing chart member in traces. 
* Fixed unreleased issue with clipped charts causing a scaling bug that would leave clipped 
  charts (e.g. in internal clipped frames) unscaled thus showing wrong data. 
* Fixed unreleased issue with duplicate labels for added traces in ChartPanel. 
* Fixed serious error reported by Robin Weisberg: Maximum Y value change was 
  not detected causing an infinite loop (100 % cpu usage) or an invalid scaling. 
* Fixed issue #1967609: Chart2D is now serializable. 
! Charts may now have multiple x axes on left and right side. 
! Charts may now have multiple y axes on bottom and top side. 
! A minimum repaint latency may be configured that allows to collapse many 
  repaint requests into one (required for high performance in real time applications).
! Added new convenience method to remove all traces from a chart / axis. 
- Traces are now related to an x and an y axis to relate them with other traces on 
  the same axes. 
- Moved scaling and bound routines from chart to axes due to multiple axes support. 
o Extended method 
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics2D g);
  in class 
  info.monitorenter.gui.chart.IPointPainter 
  to   
  public void paintPoint(final int absoluteX, final int absoluteY, final int nextX,
      final int nextY, final Graphics2D g, final TracePoint2D original);
  to provide more information to the point painters. 
o Replaced method 
  public double getMinFromAxis() 
  in class 
  info.monitorenter.gui.chart.axis.AAxis.AChartDataAccessor  
  by new method 
  public double getMinValue() 
  in class 
  info.monitorenter.gui.chart.axis.AAxis. 
o Replaced method 
  public double getMaxFromAxis() 
  in class 
  info.monitorenter.gui.chart.axis.AAxis.AChartDataAccessor  
  by new method 
  public double getMaxValue() 
  in class 
  info.monitorenter.gui.chart.axis.AAxis. 
o Moved method 
  public double getMax() 
  from class 
  info.monitorenter.gui.chart.axis.AAxis.AChartDataAccessor 
  to class 
  info.monitorenter.gui.chart.axis.AAxis.
o Moved method 
  public double getMin() 
  from class 
  info.monitorenter.gui.chart.axis.AAxis.AChartDataAccessor 
  to class 
  info.monitorenter.gui.chart.axis.AAxis.
o Moved method 
  public boolean isDirtyScaling(final int axis) 
  from class 
  info.monitorenter.gui.chart.Chart2D
  to method 
  public boolean isDirtyScaling()
  in interface 
  info.monitorenter.gui.chart.IAxis. 
o Replaced methods: 
  protected double findMinX() 
  protected double findMaxX() 
  protected double findMinY()
  protected double findMaxY() 
  in class 
  info.monitorenter.gui.chart.Chart2D
  by methods: 
  protected double findMin() 
  protected double findMax() 
  in class 
  info.monitorenter.gui.chart.axis.AAxis. 
o Replaced methods 
  public double getMinY()
  public double getMaxY()
  public double getMinX()
  public double getMaxX()
  in class 
  info.monitorenter.gui.chart.Chart2D
  by methods: 
  public double getMinValue() 
  public double getMaxValue()
  in class 
  info.monitorenter.gui.chart.axis.AAxis. 
o Moved the property event PROPERTY_ADD_REMOVE_TRACE and handling 
  from class 
  info.monitorenter.gui.chart.Chart2D
  into class 
  info.monitorenter.gui.chart.AAxis. 
  Traces are now contained in axis instances of traces!
o Changed method: 
  public void paintXTick(final int x, final int y, boolean isMajorTick, final Graphics2D g)
  in class 
  info.monitorenter.gui.chart.IAxisTickPainter 
  to 
  public void paintXTick(final int x, final int y, boolean isMajorTick, boolean isBottomSide, final Graphics2D g);
  in order to add tick support for bottom and top x axes. 
o Changed method: 
  public void paintYTick(final int x, final int y, boolean isMajorTick, final Graphics2D g)
  in class 
  info.monitorenter.gui.chart.IAxisTickPainter 
  to 
  public void paintYTick(final int x, final int y, boolean isMajorTick, boolean isLeftSide, final Graphics2D g);
  in order to add tick support for left and right y axes.
o Refactored design of axis titles: All setter and getter access of IAxisTitlePainter 
  instances has to be changed to IAxis.getAxisTitle() (new AxisTitle entity for separation of 
  data and event handling from painting). 
o Moved TracePoint2D from package info.monitorenter.gui.chart to package info.monitorenter.gui.chart.axis.
 
jchart2d-2.2.3 - May, 30st, 2008

* Fixed bug #1878068: RangePolicyHighestValuesForcedMin is not in released jar. 
* Fixed bug #1930893: ZoomableChart now interpolates the connections of two 
  invisible points if their connection crosses the zooming window. 
* Fixed bug #1941871: Size of image is not correct when taking snapshot.
* Fixed issue with ClassCastException in ErrorBarPolicyMultiAction when using java 1.6.
* Fixed concurrent issue if all points of traces are removed and added (same values) 
  in-between two painting - cycles (wrong saling -> rendering). 
* Fixed issue with limitation of 1000 points per trace in default mode 
  (TracePainterPolyline used). 
- Code style cleanup. 
- Improved performance for charts without error bars. 

  
jchart2d-2.2.1 - October, 22nd, 2007

* Fixed bug that prevented repainting when points are added / changed 
  that do not change bounds and no UI interaction has taken place. 
! Removed internal Painter Thread for faster UI updates and increased stability. 
* Fixed unreported bug with error bars exceeding drawing area in zoomed mode 
  of ZoomableChart.   
* Fixed bug related to zooming with ZoomableChart that uses at least one 
  AxisTransformation subtype (reported and fixed by Andrea Plotegher). 
* Fixed performance bug: Each error bar was painted / calculated twice per frame. 
* Fixed bug with wrong errorbars when using AAxisTransformation subclasses as axis. 
  This was a serious rounding issue and is fixed by a redesign: Transformation of 
  pixel to value and to pixel again for error bars was dropped, so performance 
  has increased too. 
! Switched to proguard version 4.0: Binary distribution is a bit smaller. 
! Added test case for bug report #1803342: Deadlock into Chard2D.paint: Succeeds. 
! Started to use findbugs. 
* Fixed bug #1814786: Lowering maximum via TracePoint2D.setLocation fails 
  by contribution of Gerald Struck (tankex). 
* Fixed unreported bug with modal dialogs dropping behind if main window 
  is minimized and then maximized again. 
! New UI action for setting titles of axis. 
! New UI action for setting title fonts of axis. 
! Extended API for different types of tool tips for chart. 

jchart2d-2.2.0 - September, 16th, 2007

! Added method in ITrace2D: public void addComputingTrace(ITrace2D trace) 
  for support of traces that compute their values based upon other 
  traces (e.g. average).
! Added info.monitorenter.gui.chart.traces.computing.Trace2DArithmeticMean 
  that displays the arithemtic mean of the last n (contrcutor - given) 
  added data points. See 
  info.monitorenter.gui.chart.demosDynamicChartWithArithmeticMeanTrace 
  for a demo. 
- Changed internal API of traces for supporting chains of computational traces.
! Support for stacking charts vertically with same x coordinate start 
  via Chart2D.setSynchronizedXStartChart(Chart2D) 
  (contribution by Karl Piteira). 
o Renamed net.monitorenter.gui.chart.ILabelFormatter to 
  net.monitorenter.gui.chart.IAxisLabelFormatter.
o Renamed net.monitorenter.gui.chart.ILabelPainter to 
  net.monitorenter.gui.chart.IAxisTickPainter. 
- Moved logic for finding chart bounds to axis classes. 
! Support for axis titles via IAxis.setTitlePainer(IAxisTitlePainter) 
  and IAxis.setTitle(String). 
! New AxisInverse allows to display values in descending order (10, 9, 8, ...0). 
  Contribution by Andrea Plotegher while working on IR spectroscopy. 
* Fixed issue  #1769652: zoom all menu item doesn't repaint the chart when selected. 
o Renamed net.monitorenter.gui.chart.ILabelPainter to 
  net.monitorenter.gui.chart.IAxisTickPainter. 
o Renamed package info.monitorenter.gui.chart.labelpainters 
  to info.monitorenter.gui.chart.axistickpainters.
o Renamed info.monitorenter.gui.chart.labelpainters.LabelPainterDefault 
  to info.monitorenter.gui.chart.axistickpainters.AxisTickPainterDefault.
o Renamed info.monitorenter.gui.chart.Chart2D.getLabelPainter() 
  to info.monitorenter.gui.chart.Chart2D.getAxisTickPainter()
o Renamed info.monitorenter.gui.chart.Chart2D.setLabelPainter(IAxisTickPainter) 
  to info.monitorenter.gui.chart.Chart2D.setAxisTickPainter(IAxisTickPainter)
* Fixed unreported deadlock when using ChartPanel with multiple traces. 
* Fixed unreleased issue that painted a connection from the last point of 
  a trace to the first point of the next trace (multiple traces), reported 
  by Andrea Plotegher. 
! Extended info.monitorenter.gui.chart.errorbars.ErrorBarPolicyRelative by 
  allowing to configure relative x and y errors (vs. only one relative error 
  for alll dimensions). 
! Finished integration of error bar wizard into GUI controls of ChartPanel. 
* Fixed bug with chart bounds upon decreasing error bar expansion. 
* Fixed bug with non - UI property adapting strokes menu in LayoutFactory 
  (fixed by Andrea Plotegher).
* Fixed bug #1790622: using trace.setLocation(..) fails. Now changed points will 
  be reflected in the UI without exception and correct display. 
* Fixed unreported issue with incorrect repaint if partial repaint is triggered by another 
  Thread than the painter (e.g. if a modal dialog is closed). 
* Fixed unreported display issue with LabelFormatterNumber using a format that 
  enforces digits (e.g.:  new DecimalFormat("000.000");) and overwrites the y axis. 


jchart2d-2.1.2 - July, 25th, 2007

* Fixed issue #1711148: LayoutFactory does not provide documented methods. 
! Configurable context menu entry for zooming out for ChartPanel when wrapping 
  a ZoomableChart. Contribution by Vasilis Pappas. 
! Only use lower unit in LabelFormatterAutoUnits when data range is 
  smaller 1.5 of a whole unit. 
! New error bar policy for summation of absolute values. 
! Added default constructors and additional setters for bean compliance 
  of label formatters.   
* Fixed issue with UI not reacting upon changed label formatter of Axis at runtime.
* Fixed issue with UI not reacting upon changed number format of 
  LabelFormatterNumber at runtime.
* Fixed issue with concurrent painting / potential deadlock in addErrorBarPolicy 
  and setErrorBarPolicy of traces. 
* Fixes issue with traces not updating in case the error bar policies / their 
  configuration changes at runtime. 
* Fixed issue with color icon of error bar painter edit panel not updating when 
  corresponding error bar segment is changed at runtime.
* Fixed minor rounding bug in value to pixel transformation of AAxis.
* Fixed issue with axis scale values that appear out of the chart area (too low).
* Fixed issue with traces being rescaled in both dimensions even if only one 
  dimension changed (Performance improvement). 
! Updated profiling support to Java Interactive Profiler (JIP) Version 1.1(final).
! Major performance improvement for updateScaling: JProfiler measured ~42 % total of paint 
  runtime before, now takes ~ 16 %.
! New info.monitorenter.gui.chart.labelformatters.LabelFormatterUnit allows to specify 
  a fixed unit for an axis. 

   
jchart2d-2.1.1 - March, 30st, 2007

* Fixed issue: When adding a trace that does not exceed bounds it 
  is not scaled in the corresponding dimension (x=0 or y=0 lines appeared).
* Fixed issue #1584637: Trace back when displaying Chart2D on Java 6.
* Fixed issue with zooming all in ZoomableChart that contains several traces. 
* Show range policy selection on x axis for chart panels by default (was hidden).
* Fixed issue with NullPointerException in custom layout manager 
  of chart panel in case the component count of the target layout container is 0.
* Fixed issue #1578136: Trace2DLtdSorted(int) has private access. 
* Fixed issue #1586616: Removing trace from chart dos not delete label in ChartPanel.
! New UI action for setting physical units for traces in ChartPanel. 
! New UI action for removing trace in ChartPanel (disabled by default, enable by 
  LayoutFactory.getInstance().setShowRemoveTraceMenu(true)). 
* Fixed issue #1601947: image export with snaphot() when chart is not visible (not sized).
! New method IErrorBarPolicy.removeErrorBarPainter(IErrorBarPainter). 
! Improved property change handling for IErrorBarPolicy implementations 
  along with reusable Action implementations. 
* Fixed issue #1644055: Bounds check in ATrace2D doesn't catch removed extremum.
! Added constructors for all AAxis types that take an info.monitorenter.gui.chart.ILabelFormatter. 
! Decreased calls to repaint by using the axis range as the repaint criterium vs. the chart data bounds. 
! Improved property change event handling in the Chart2D. 
* Fixed issue #1652154: ATRace2D.setName has conditional/delayed visibility. 
! Added visual human-judged operation tests. 
* Fixed issue #1657003: javadoc error in IAxis (setMinorTickSpacing(double)).
* Fixed issue #1684854 (unreleased):  Phantom line appears while adding trace point.
* Fixed issue #1663833 Bug in legend print: Contribution by Horea Haitonic.

jchart2d-2.1.0 - October, 12th, 2006

o Renamed 
  info.monitorenter.gui.chart.controls.LayoutFactory.createContextMenuLable(Chart2D, ITrace2D, boolean) 
  to 
  info.monitorenter.gui.chart.controls.LayoutFactory.createContextMenuLabel(Chart2D, ITrace2D, boolean) 
o Renamed 
  info.monitorenter.chart.ITrace2D.getLable() 
  to 
  info.monitorenter.chart.ITrace2D.getLabel() 
o Renamed 
  info.monitorenter.chart.axis.AAxis.getMinimumValueDistanceForLables() 
  to 
  info.monitorenter.chart.axis.AAxis.getMinimumValueDistanceForLabels()
o Renamed 
  info.monitorenter.chart.axis.AAxis.getMaximumPixelForLable() 
  to 
  info.monitorenter.chart.axis.AAxis.getMaximumPixelForLabel()
* Chartpanel does not claim the correct height for an amount 
  of trace labels (below chart) that exceeds available width.
* Bugfix #1573885:  Range dialog has misspelled "Chancel" button.
* Fixed obfuscated method names in ILabelFormatter of previous binary release.
* Recalculate bound in case a trace gets visible or invisible 
  at runtime to zoom to the optimal bounds upon visibility changes. 
- Redesigned re- / transformation from px values to chart data 
  values.
o Moved AAxis from package info.monitorenter.gui.chart to 
  package info.monitorenter.gui.chart.axis.
* Repaint chart in static mode if trace is set visible / invible 
  to reflect that change (discovered by Anton Kratz).
! ZoomableChart supports zooming in both directions 
  (contribution by Klaus Pesendorfer, Fabalabs). 
! First error bar policy implementation: 
  info.monitorenter.gui.chart.errorbars.ErrorBarPolicyRelative. 
  See info.monitorenter.gui.chart.demos.StaticChartErrorBarLineDisc 
  for a demonstration of the error bar API. 
! Highly configurable error bar API supported in core (vs.: UI): 
  Choose the direction (x/y,positive/negative), 
  kind of error bar policy and the way of rendering 
  (end point -, start point - and segment - painter). 
- Separated code for painting a point from code to manage painting a whole trace 
  and reused this for error bar rendering: 
  Factored out duplications, increased modularization and maintainability. 
o Refactored 
  info.monitorenter.gui.chart.ITracePainter.discontinue()
  to
  info.monitorenter.gui.chart.ITracePainter.discontinue(java.awt.Graphics2D)
o Refactored 
  info.monitorenter.gui.chart.ITracePainter.startPaintIteration()
  to
  info.monitorenter.gui.chart.ITracePainter.startPaintIteration(java.awt.Graphics2D)
o Refactored 
  info.monitorenter.gui.chart.ITracePainter.endPaintIteration()
  to
  info.monitorenter.gui.chart.ITracePainter.endPaintIteration(java.awt.Graphics2D)
* Bugfix #1550773: Don't overwrite the range of a range policy in AAxis.setRangePolicy(IRangePolicy)
* Only zoom graph with left mouse-button (Contribution by Klaus Pesendorfer, Fabalabs).
! New trace painter for vertical filled bars: info.monitorenter.gui.chart.traces.painters.TracePainterVerticalBar
* Bugfix #1523799: LabelFormatter property overwritten for new AAxis.
- Refactored axis actions to always be based on the current chart (stability, consistancy). 
! LayoutFactory supports the option for standard menus 
  (vs. UI-property adapting menus [foreground color, background color,...].
- Modularized the creation of trace menus in LayoutFactory. 
! Configurable visibility of menu entries for Popup and Menubar menus. 
o Moved info.monitorenter.gui.chart.layout.LayoutFactory to 
  info.monitorenter.gui.chart.controls.LayoutFactory.
o Moved info.monitorenter.gui.chart.layout.ChartPanel to 
  info.monitorenter.gui.chart.views.ChartPanel.
o Moved info.monitorenter.gui.chart.layout.demos.CoordinateViewChart to 
  info.monitorenter.gui.chart.demos.CoordinateViewChart.
o Renamed package info.monitorenter.gui.chart.layout.controls to 
  info.monitorenter.gui.chart.controls. 
! Improved quality of saved images significantly by use of anti-aliasing. 

jchart2d-2.0.1 - June, 5th, 2006

- Binaries are now compiled with 1.4 option against java 1.5.0_07-b03.
* Catch exceptions caused by illegal data for axis implementations 
  that transform values - Return 0 and only output a warning 
  (innapropriate axis implementation for this data) every 30 seconds.
! Menu item for saving chart image to file. 
* JMenus that are submenus did not use background color. 
  (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5097866).
* Trace labels in ChartPanel did not adapt text and color of corresponding 
  trace.
* All menu items linked to axis would not function any more after setting 
  a new axis to the chart. 
* Don't loose state information and overtake listeners when setting 
  a new axis to a chart. This bug showed strange behaviour in UI 
  after a new axis type had been set: E.g. grid was not painted 
  any more. 
* Scale value to 0 and print a warning on the console if 
  a logarithmic axis is used and a value is < 1. 
  (Instead of letting an exception go to the awt paint thread).
  
jchart2d-2.0.0 - May, 31th, 2006

! Added profiling support in build.xml with Java Interactive 
  Profiler (JIP).
* Started to use a new sourceforge package for each new version 
  on sourceforge (bugreport by Ken Webb).
! Optional use of standard UI menu items for menubar and popup menu 
  (instead of thos that adapt their UI to the chart). 
! New menu bar available in LayoutFactory 
  (see info.monitorenter.chart.layout.ChartPanel). 
* Change of grid color of chart would not fire the expected event. 
* Ignore color choice if dialog for choosing color is cancelled. 
! Smaller distributed jar file by shrinking to the needed classes 
  (exclusion of unused classes in 3rd party jars).
* Update context menu label states (for radio button or checkbox menu items) 
  if chart configuration changes from other sources than the menu itself. 
* Repaint upon change of grid color in static mode 
  (no points are added).
* Don't unselect checkbox menu item for trace painter if removal failed 
  due to "at least leave one painter" strategy. 
* Repaint upon change of color of a trace in static mode 
  (no points are added).
* Repaint upon change of painter of a trace in static mode 
  (no points are added).
* Repaint upon change of stroke of a trace in static mode 
  (no points are added).
! Initial support for zooming in x dimension by mouse gestures in 
  info.monitorenter.gui.chart.ZoomabelChart, contribution by Alessio Sambarino.
! Don't use bounds of invisible traces 
  (Contribution by Klaus Pesendorfer, fabasoft).
o Renamed ITrace2D.getVisible() to ITrace2D.isVisible() 
  (bean convention).
o Renamed package info.monitorenter.gui.chart.demo to 
  info.monitorenter.gui.chart.demos.
o Renamed package info.monitorenter.gui.chart.event to 
  info.monitorenter.gui.chart.events.
o Renamed package info.monitorenter.gui.chart.layout.control to 
  info.monitorenter.gui.chart.layout.controls.
o Moved ADataCollector implementations to package 
  info.monitorenter.gui.chart.io.
o Moved ILabelPainter implementations to subpackage 
  info.monitorenter.gui.chart.labelpainters.
o Moved ADataCollector to package info.monitorenter.chart.io.
o Moved ITracePainter implementations to subpackage 
  info.monitorenter.gui.chart.traces.painters.
o Moved ITrace2D implementations to subpackage 
  info.monitorenter.gui.chart.traces.
o Moved AAxis implementations to subpackage 
  info.monitorenter.gui.chart.axis.
o Removed  public final double getScaledValueY(double). 
  Use Chart2D.getAxisY().getScaledValue(double).
o Removed  public final double getScaledValueX(double). 
  Use Chart2D.getAxisX().getScaledValue(double).
o Removed public final double Chart2D.getOffsetY(). 
  Use Chart2D.getAxisY().getMin();
o Moved IRangePolicy implementations to subpackage 
  info.monitorenter.gui.chart.rangepolies.
o Moved ILabelFormatter implementations to subpackage 
  info.monitorenter.gui.chart.labelformatters.
* Bugfix: Wrong space for the chart on right edge as wrong label length 
  (of y labels) was used for calculation.
o Removed AAxis.getUnit(). Use AAxis.getFormatter().getUnit(). 
o Changed package structure from "aw." to "info.monitorenter.".
! Ensure that context menu remains readable if background 
  color is set to current foreground color or foreground color 
  is set to current background color by switching them. 
o Renamed all interfaces with prefix I.
o Renamed all abstract classes with prefix A.
! Added tool tip text to context menu for range policy of axis. 
* Bugfix: RingBuffer lost last element if buffer size was increased. 
* Bugfix for unreleased version (29.04.06): 
  Wrong minY search routine in traces.
! Added RangePolicyMinimumViewport to context menu of ChartPanel.
! Axis type (linear, log e, log 10) selectable from popup menu 
  of ChartPanel.
* Bugfix: setting new range with only one bound changed to rangepolicy 
  would not cause rescaling. 
o Chart2D.setScaleX(final boolean show) has been removed. 
  Use Chart2D.getAxisX().setPaintGrid(final boolean grid) instead.
o Chart2D.getScaleX() has been removed. 
  Use Chart2D.getAxisX().isPaintGrid() instead.
o Chart2D.setScaleY(final boolean show) has been removed. 
  Use Chart2D.getAxisY().setPaintGrid(final boolean grid) instead.
o Chart2D.getScaleY() has been removed. 
  Use Chart2D.getAxisY().isPaintGrid() instead.
o Chart2D.setGridY() has been removed. 
  Use Chart2D.getAxisY().setPaintScale(final boolean show) instead.
o Chart2D.getGridY() has been removed. 
  Use Chart2D.getAxisY().isPaintScale() instead.
o Chart2D.setGridX() has been removed. 
  Use Chart2D.getAxisX().setPaintScale(final boolean show) instead.
o Chart2D.getGridX() has been removed. 
  Use Chart2D.getAxisX().isPaintScale() instead.
o Chart2D.setDecimalsX(final int decimals) has been removed. 
  Use LabelFormatters instead.
o Chart2D.setDecimalsY(final int decimals) has been removed. 
  Use LabelFormatters instead.
o Axis.setFractionsDigits(int) has been removed. 
  Use LabelFormatters instead.
! Experimental version for logarithmic axis (base 10 and e), 
  initial contribution by Pieter-Jan Busschaert. 
* Changing paint grid state would not cause a full repaint thus leaving unpainted grid in space 
  when nothing changes in the traces of the chart. 
* Grid paint selection in context menu of ChartPanel was 
  not initalized to checked state correctly. 
* TracePainter selection in context menu of ChartPanel was 
  not initialized to checked state correctly. 
! Added tooltip for displaying chart value coordinates 
  along with methods to turn on and off (default: on).
! Added panel for display of chart coordinates 
  triggered by mouse motion.
! Translation from mouse events to chart coordinates in chart. 
* No more repaint if no update has happened to the chart data model. 


jchart2d-1.1.1 - April 13th, 2006

* Bugfix 1468612: memory leak when using ChartPanel and adding / removing traces.  
* Corrected file headers to LGPL 2.1.
! Allow direct edit of range bounds for range choice with keyboard. 
! Integrated dual slider of the invofis toolkit for range choice. 
! Update to most recent version of proguard. 
! Shrinkinig of shipped binary jar.
! Allow choosing viewport and it's range from context menu. 
! Restructured context menus of ChartPanel. 
* Update scaling of chart when a new RangePolicy is set 
  to the axis at runtime. 
* Update scaling of chart when a new Range is set 
  to the range policy of an axis. 
* Javadoc correction: Missing space after {@link type}.
* Changed wrong license file in jar: jchart2d is LGPL.
! Axis.setStartMajorTick(boolean) allows to force 
  starting with a major tick scale value.
! New RangePolicyForcedPoint to enforce always 
  showing a point on the axis.
! Control of trace painters by popup menu.
! Multiple trace painters for traces.
* Close the polygon of TracePainterFill correctly.
* Bufix: NPE when setting trace name with null argument.
* Bugfix #1430180: Update scaling of points when range policy is changed at runtime.
! Set display of grid x and grid y seperately from context menu.

jchart2d-1.1.0 - February 12th, 2006

! New viewports: zoomed, maximum values only, ... .
! Linear interpolation towards bounds for zoomed viewports.
! Number formatted labels.
! Restarting of data collectors after stop.
! Clear traces.
! Date formatted labels.
! Rendering with strokes for traces
  (dashed, dotted, thick,...).
! Z-index property for traces.
! Traces may be set invisible.
- Refactored core sourcecode conforming to checkstyle
  configuration.
* new Thread(anAbstractDataCollector).start() is permitted,
  anAbstractDataCollector.start() has to be used.
! Tracepainter for discs.
! Tracepainter for filled polygons.
! Tracepainter that uses polyline.
* Use JComponent foreground color (setForeground(Color))
  for Axis and scale labels.
! Support for fixed viewports (zooming).
! Rounding for scale labels to minor and major ticks.
! More controls in popups of ChartPanel: foreground color.
- Warning all action classes in info.monitorenter.gui.chart.event
  have been renamed.
  Those were never released in previous versions but
  available in cvs.
! Refactored interface for IRangepolicy to allow viewports
  that react upon the range of the chart.

1.03:
 - New feature that allows saving the Chart2D to an image.
 - New feature that allows to stop data collection.
 ! New Showcase application that demonstrates the new
   feature.  Invoke with:
   java -cp jchart2d-1.03.jar info.monitorenter.gui.chart.demo.Showcase
 ! Improved build for source release.

1.02:
 * Deadlock when explicitly invoking Chart2D.paint(..)
   [BTW no swing/awt program should do this]. Dropped
   unneccessary monitor.

1.01:
 * A major bug in the Trace2DSimple implementation,
   that caused a scaling error.
 * Weird "line from last point to (0,0)" - bug.
 ! Complete new synchronization design:
   Client Threads (your code) are now not involved in
   scaling operations any more.
