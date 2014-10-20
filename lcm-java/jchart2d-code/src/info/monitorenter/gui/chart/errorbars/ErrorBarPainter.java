/*
 * ErrorBarPainter.java of project jchart2d, base class for an error bar painter that allows configuration of the way the segment, start
 * point and end point of an error bar is painted by the use of info.monitorenter.gui.chart.IPointPainter. Copyright (c) 2007 - 2011 Achim
 * Westermann, created on 03.09.2006 19:55:53.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA* If you modify or optimize the code in a useful way please let
 * me know. Achim.Westermann@gmx.de
 */
package info.monitorenter.gui.chart.errorbars;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IErrorBarPainter;
import info.monitorenter.gui.chart.IErrorBarPixel;
import info.monitorenter.gui.chart.IPointPainter;
import info.monitorenter.gui.chart.IPointPainterConfigurableUI;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.pointpainters.PointPainterLine;

import java.awt.Color;
import java.awt.Graphics;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * Implementation of an error bar painter that allows configuration of the way the connection, start point and end point of an error bar is
 * painted by the use of {@link info.monitorenter.gui.chart.IPointPainter}.
 * <p>
 * 
 * Property change events are fired as described in method
 * 
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPainter#addPropertyChangeListener(String, PropertyChangeListener) }</code> . Note that
 * adding property change listeners to the nested access facades of type
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPainter.ISegment}</code> accessible via <code>getXXXSegment()</code> methods will fire
 * the corresponding events for listeners of this instance (as they delegate the calls) while they fire events for properties defined in
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPainter.ISegment}</code> too. If you register for events of this instance and for the
 * retrieved segments you will receive two <code>{@link PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)}</code> for
 * the same value changed.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * @version $Revision: 1.27 $
 */
public class ErrorBarPainter implements IErrorBarPainter {

    /**
     * Base class for <code>ISegment</code> implementations that covers common features such as property change support.
     * <p>
     * 
     * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
     * 
     * 
     * @version $Revision: 1.27 $
     */
    private abstract class ASegment implements ISegment {

        /** Generated <code>serialVersionUID</code>. **/
        private static final long serialVersionUID = 6620706884643200785L;

        /**
         * Defcon.
         * <p>
         */
        protected ASegment() {
            super();
        }

        /**
         * Properties supported are defined in <code>
     * {@link IErrorBarPainter#addPropertyChangeListener(String, PropertyChangeListener)}
     * </code>.
         * <p>
         * 
         * Note that adding property change listeners to the nested access facades of type <code>{@link IErrorBarPainter.ISegment}</code>
         * accessible via <code>getXXXSegment()</code> methods will fire the corresponding events for listeners of this instance (as they
         * delegate the calls) while they fire events for properties defined in <code>{@link IErrorBarPainter.ISegment}</code> too. If you
         * register for events of this instance and for the retrieved segments you will receive two
         * <code>{@link PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)}</code> for the same value changed.
         * <p>
         * 
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#addPropertyChangeListener(java.lang.String,
         *      java.beans.PropertyChangeListener)
         */
        public void addPropertyChangeListener(final String property, final PropertyChangeListener listener) {
            ErrorBarPainter.this.addPropertyChangeListener(property, listener);
        }

    }

    /** Generated <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -4978322492200966266L;

    /**
     * The renderer of the connection (distance between origin and end of error bar) of error bars.
     */
    private IPointPainterConfigurableUI<?> m_connectionPainter;

    /**
     * The renderer of the end point of error bars.
     */
    private IPointPainterConfigurableUI<?> m_endPointPainter;

    /**
     * The instance that add support for firing <code>PropertyChangeEvents</code> and maintaining <code>PropertyChangeListeners</code>.
     * <p>
     */
    protected PropertyChangeSupport m_propertyChangeSupport = new SwingPropertyChangeSupport(this);

    /**
     * The facade instance for accessing the connection segment of this configurable error bar painter.
     * <p>
     */
    private final ISegment m_segmentConnection = new ASegment() {

        /** Generated <code>serialVersionUID</code>. */
        private static final long serialVersionUID = 2582262217019921050L;

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getColor()
         */
        public Color getColor() {
            return ErrorBarPainter.this.getConnectionColor();
        }

        /**
         * 
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getName()
         */
        public String getName() {
            return "Connection segment";
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPointPainter()
         */
        public IPointPainterConfigurableUI<?> getPointPainter() {
            return ErrorBarPainter.this.getConnectionPainter();
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPropertySegmentColor()
         */
        public final String getPropertySegmentColor() {
            return IErrorBarPainter.PROPERTY_CONNECTION_COLOR;
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPropertySegmentPointPainter()
         */
        public final String getPropertySegmentPointPainter() {
            return IErrorBarPainter.PROPERTY_CONNECTION;
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#setColor(java.awt.Color)
         */
        public void setColor(final Color color) {
            // fires the property change event:
            ErrorBarPainter.this.setConnectionColor(color);
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#setPointPainter(IPointPainterConfigurableUI)
         */
        public void setPointPainter(final IPointPainterConfigurableUI<?> pointPainter) {
            // fires the property change event:
            ErrorBarPainter.this.setConnectionPainter(pointPainter);
        }

    };

    /**
     * The facade instance for accessing the end segment of this configurable error bar painter.
     * <p>
     */
    private final ISegment m_segmentEnd = new ASegment() {
        /** Generated <code>serialVersionUID</code>. */
        private static final long serialVersionUID = -5655272957651523988L;

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getColor()
         */
        public Color getColor() {
            return ErrorBarPainter.this.getEndPointColor();
        }

        public String getName() {
            return "End segment";
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPointPainter()
         */
        public IPointPainterConfigurableUI<?> getPointPainter() {
            return ErrorBarPainter.this.getEndPointPainter();
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPropertySegmentColor()
         */
        public String getPropertySegmentColor() {
            return IErrorBarPainter.PROPERTY_ENDPOINT_COLOR;
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPropertySegmentPointPainter()
         */
        public String getPropertySegmentPointPainter() {
            return IErrorBarPainter.PROPERTY_ENDPOINT;
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#setColor(java.awt.Color)
         */
        public void setColor(final Color color) {
            // fires the property change event:
            ErrorBarPainter.this.setEndPointColor(color);
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#setPointPainter(IPointPainterConfigurableUI)
         */
        public void setPointPainter(final IPointPainterConfigurableUI<?> pointPainter) {
            // fires the property change event:
            ErrorBarPainter.this.setEndPointPainter(pointPainter);
        }
    };

    /**
     * The facade instance for accessing the start segment of this configurable error bar painter.
     * <p>
     */
    private final ISegment m_segmentStart = new ASegment() {

        /** Generated <code>serialVersionUID</code>. */
        private static final long serialVersionUID = -1547300597027982211L;

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getColor()
         */
        public Color getColor() {
            return ErrorBarPainter.this.getStartPointColor();
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getName()
         */
        public String getName() {
            return "Start segment";
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPointPainter()
         */
        public IPointPainterConfigurableUI<?> getPointPainter() {
            return ErrorBarPainter.this.getStartPointPainter();
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPropertySegmentColor()
         */
        public String getPropertySegmentColor() {
            return IErrorBarPainter.PROPERTY_STARTPOINT_COLOR;
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#getPropertySegmentPointPainter()
         */
        public String getPropertySegmentPointPainter() {
            return IErrorBarPainter.PROPERTY_STARTPOINT;
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#setColor(java.awt.Color)
         */
        public void setColor(final Color color) {
            // fires the property change event:
            ErrorBarPainter.this.setStartPointColor(color);
        }

        /**
         * @see info.monitorenter.gui.chart.IErrorBarPainter.ISegment#setPointPainter(IPointPainterConfigurableUI)
         */
        public void setPointPainter(final IPointPainterConfigurableUI<?> pointPainter) {
            // fires the property change event:
            ErrorBarPainter.this.setStartPointPainter(pointPainter);
        }
    };

    /**
     * The renderer of the start point of error bars.
     */
    private IPointPainterConfigurableUI<?> m_startPointPainter;

    /**
     * Creates an instance that by default will not render any error bar.
     * <p>
     * 
     * It then has to be configured with the remaining methods as desired.
     * <p>
     * 
     * @see #setEndPointColor(Color)
     * @see #setEndPointPainter(IPointPainterConfigurableUI)
     * @see #setConnectionColor(Color)
     * @see #setConnectionPainter(IPointPainterConfigurableUI)
     * @see #setStartPointColor(Color)
     * @see #setStartPointPainter(IPointPainterConfigurableUI)
     */
    public ErrorBarPainter() {
        super();
        // set default values:
        this.m_startPointPainter = null;
        this.m_connectionPainter = new PointPainterLine();
        this.m_endPointPainter = new PointPainterDisc(4);

    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.m_propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ErrorBarPainter other = (ErrorBarPainter) obj;
        if (this.m_connectionPainter == null) {
            if (other.m_connectionPainter != null) {
                return false;
            }
        } else if (!this.m_connectionPainter.equals(other.m_connectionPainter)) {
            return false;
        }
        if (this.m_endPointPainter == null) {
            if (other.m_endPointPainter != null) {
                return false;
            }
        } else if (!this.m_endPointPainter.equals(other.m_endPointPainter)) {
            return false;
        }
        if (this.m_propertyChangeSupport == null) {
            if (other.m_propertyChangeSupport != null) {
                return false;
            }
        } else if (!this.m_propertyChangeSupport.equals(other.m_propertyChangeSupport)) {
            return false;
        }
        if (this.m_segmentConnection == null) {
            if (other.m_segmentConnection != null) {
                return false;
            }
        } else if (!this.m_segmentConnection.equals(other.m_segmentConnection)) {
            return false;
        }
        if (this.m_segmentEnd == null) {
            if (other.m_segmentEnd != null) {
                return false;
            }
        } else if (!this.m_segmentEnd.equals(other.m_segmentEnd)) {
            return false;
        }
        if (this.m_segmentStart == null) {
            if (other.m_segmentStart != null) {
                return false;
            }
        } else if (!this.m_segmentStart.equals(other.m_segmentStart)) {
            return false;
        }
        if (this.m_startPointPainter == null) {
            if (other.m_startPointPainter != null) {
                return false;
            }
        } else if (!this.m_startPointPainter.equals(other.m_startPointPainter)) {
            return false;
        }
        return true;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getConnectionColor()
     */
    public final Color getConnectionColor() {
        Color result = this.getDefaultedColor(this.m_connectionPainter);
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getConnectionPainter()
     */
    public final IPointPainterConfigurableUI<?> getConnectionPainter() {
        return this.m_connectionPainter;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getEndPointColor()
     */
    public final Color getEndPointColor() {
        Color result = this.getDefaultedColor(this.m_endPointPainter);
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getEndPointPainter()
     */
    public final IPointPainterConfigurableUI<?> getEndPointPainter() {
        return this.m_endPointPainter;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(final String property) {
        return this.m_propertyChangeSupport.getPropertyChangeListeners(property);
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getSegmentConnection()
     */
    public ISegment getSegmentConnection() {
        return this.m_segmentConnection;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getSegmentEnd()
     */
    public ISegment getSegmentEnd() {
        return this.m_segmentEnd;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getSegmentStart()
     */
    public ISegment getSegmentStart() {
        return this.m_segmentStart;
    }

    /**
     * Default color for all segments in case their <code>{@link IPointPainterConfigurableUI}</code> has not been configured with a color.
     */
    public static final Color DEFAULT_SEGMENT_COLOR = Color.GRAY;

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getStartPointColor()
     */
    public final Color getStartPointColor() {
        Color result = this.getDefaultedColor(this.m_startPointPainter);
        return result;
    }

    /**
     * Internal routine that returns the default color in case the argument or it's color is null (factored out code).
     * <p>
     * 
     * @param painter
     *            the painter to check for color.
     * 
     * @return the color of the given painter or <code> {@link #DEFAULT_SEGMENT_COLOR}</code> in case the color or the painter was null.
     */
    private Color getDefaultedColor(final IPointPainterConfigurableUI<?> painter) {

        Color result = null;
        if (painter != null) {
            result = painter.getColor();
        }
        if (result == null) {
            result = DEFAULT_SEGMENT_COLOR;
        }
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#getStartPointPainter()
     */
    public final IPointPainterConfigurableUI<?> getStartPointPainter() {
        return this.m_startPointPainter;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.m_connectionPainter == null) ? 0 : this.m_connectionPainter.hashCode());
        result = prime * result + ((this.m_endPointPainter == null) ? 0 : this.m_endPointPainter.hashCode());
        result = prime * result + ((this.m_propertyChangeSupport == null) ? 0 : this.m_propertyChangeSupport.hashCode());
        result = prime * result + ((this.m_segmentConnection == null) ? 0 : this.m_segmentConnection.hashCode());
        result = prime * result + ((this.m_segmentEnd == null) ? 0 : this.m_segmentEnd.hashCode());
        result = prime * result + ((this.m_segmentStart == null) ? 0 : this.m_segmentStart.hashCode());
        result = prime * result + ((this.m_startPointPainter == null) ? 0 : this.m_startPointPainter.hashCode());
        return result;
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#paintErrorBar(int, int, ITracePoint2D, Graphics, IErrorBarPixel)
     */
    public void paintErrorBar(final int absoluteX, final int absoluteY, final ITracePoint2D original, final Graphics g,
                    final IErrorBarPixel errorBar) {

        final Chart2D chart = errorBar.getTrace().getRenderer();
        // If some range policy is used that restricts the viewport ensure,
        // that we don't paint offscreen:
        final int xStart = chart.getXChartStart();
        final int xEnd = chart.getXChartEnd();
        final int yStart = chart.getYChartStart();
        final int yEnd = chart.getYChartEnd();
        int x1;
        int y1;
        int x2;
        int y2;
        // x1
        if (absoluteX < xStart) {
            x1 = xStart;
        } else {
            x1 = absoluteX;
        }
        if (absoluteX > xEnd) {
            x1 = xEnd;
        } else {
            x1 = absoluteX;
        }

        // y1
        if (absoluteY > yStart) {
            y1 = yStart;
        } else {
            y1 = absoluteY;
        }
        if (absoluteY < yEnd) {
            y1 = yEnd;
        } else {
            y1 = absoluteY;
        }

        // negative x error:
        int error = errorBar.getNegativeXErrorPixel();
        if (error != IErrorBarPixel.ERROR_PIXEL_NONE) {
            y2 = y1;
            if (error < xStart) {
                x2 = xStart;
            } else {
                x2 = error;
            }
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_connectionPainter, g);
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_startPointPainter, g);
            // don't paint end point if bounds were exceeded:
            if (x2 == error) {
                this.paintErrorBarPart(x2, y1, x1, y1, original, this.m_endPointPainter, g);
            }
        }
        // positive x error:
        error = errorBar.getPositiveXErrorPixel();
        if (error != IErrorBarPixel.ERROR_PIXEL_NONE) {
            y2 = y1;
            if (error > xEnd) {
                x2 = xEnd;
            } else {
                x2 = error;
            }

            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_connectionPainter, g);
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_startPointPainter, g);
            // don't paint end point if bounds were exceeded:
            if (x2 == error) {
                this.paintErrorBarPart(x2, y1, x1, y1, original, this.m_endPointPainter, g);
            }
        }

        // negative y error:
        error = errorBar.getNegativeYErrorPixel();
        if (error != IErrorBarPixel.ERROR_PIXEL_NONE) {
            x2 = x1;
            if (error > yStart) {
                y2 = yStart;
            } else {
                y2 = error;
            }
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_connectionPainter, g);
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_startPointPainter, g);
            // don't paint end point if bounds were exceeded:
            if (y2 == error) {
                this.paintErrorBarPart(x1, y2, x1, y1, original, this.m_endPointPainter, g);
            }
        }
        // positive y error:
        error = errorBar.getPositiveYErrorPixel();
        if (error != IErrorBarPixel.ERROR_PIXEL_NONE) {
            x2 = x1;
            if (error < yEnd) {
                y2 = yEnd;
            } else {
                y2 = error;
            }
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_connectionPainter, g);
            this.paintErrorBarPart(x1, y1, x2, y2, original, this.m_startPointPainter, g);
            // don't paint end point if bounds were exceeded:
            if (y2 == error) {
                this.paintErrorBarPart(x1, y2, x1, y1, original, this.m_endPointPainter, g);
            }
        }
    }

    /**
     * Internally renders the given part of the error bar with support for color and invisibility (null painter) management.
     * <p>
     * Factored out code to keep calling method {@link #paintErrorBar(int, int, ITracePoint2D, Graphics, IErrorBarPixel)} smaller.
     * <p>
     * 
     * @param absoluteX
     *            the x coordinate in px of the error bar part.
     * 
     * @param absoluteY
     *            the y coordinate in px of the error bar part.
     * 
     * @param nextX
     *            the next x coordinate in px of the error bar part (only relevant for connection).
     * 
     * @param nextY
     *            the next y coordinate in px of the error bar part (only relevant for connection).
     * 
     * @param pointPainter
     *            the painter to use.
     * 
     * @param g2d
     *            needed for painting.
     * 
     * @param original
     *            the original trace point this error bar is painted for.
     */
    private final void paintErrorBarPart(final int absoluteX, final int absoluteY, final int nextX, final int nextY,
                    final ITracePoint2D original, final IPointPainter<?> pointPainter, final Graphics g2d) {
        if (pointPainter != null) {
            pointPainter.paintPoint(absoluteX, absoluteY, nextX, nextY, g2d, original);
        }
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        this.m_propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(final String property, final PropertyChangeListener listener) {
        this.m_propertyChangeSupport.removePropertyChangeListener(property, listener);
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#setConnectionColor(java.awt.Color)
     */
    public final void setConnectionColor(final Color connectionColor) {

        if (this.m_connectionPainter != null) {
            final Color old = this.m_connectionPainter.getColor();
            this.m_connectionPainter.setColor(connectionColor);
            this.m_propertyChangeSupport.firePropertyChange(IErrorBarPainter.PROPERTY_CONNECTION_COLOR, old, connectionColor);
        }
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#setConnectionPainter(info.monitorenter.gui.chart.IPointPainterConfigurableUI)
     */
    public final void setConnectionPainter(final IPointPainterConfigurableUI<?> connectionPainter) {
        final IPointPainter<?> old = this.m_connectionPainter;
        if (this.m_connectionPainter != null) {
            final Color oldColor = this.m_connectionPainter.getColor();
            if (oldColor != null) {
                if (connectionPainter != null) {
                    connectionPainter.setColor(oldColor);
                }
            }
        }
        this.m_connectionPainter = connectionPainter;
        this.m_propertyChangeSupport.firePropertyChange(IErrorBarPainter.PROPERTY_CONNECTION, old, connectionPainter);
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#setEndPointColor(java.awt.Color)
     */
    public final void setEndPointColor(final Color endPointColor) {
        if (this.m_endPointPainter != null) {
            final Color old = this.m_endPointPainter.getColor();
            this.m_endPointPainter.setColor(endPointColor);
            this.m_propertyChangeSupport.firePropertyChange(IErrorBarPainter.PROPERTY_ENDPOINT_COLOR, old, endPointColor);
        }
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#setEndPointPainter(info.monitorenter.gui.chart.IPointPainterConfigurableUI)
     */
    public final void setEndPointPainter(final IPointPainterConfigurableUI<?> endPointPainter) {
        final IPointPainter<?> old = this.m_endPointPainter;
        if (this.m_endPointPainter != null) {
            final Color oldColor = this.m_endPointPainter.getColor();
            if (oldColor != null) {
                if (endPointPainter != null) {
                    endPointPainter.setColor(oldColor);
                }
            }
        }
        this.m_endPointPainter = endPointPainter;
        this.m_propertyChangeSupport.firePropertyChange(IErrorBarPainter.PROPERTY_ENDPOINT, old, endPointPainter);
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#setStartPointColor(java.awt.Color)
     */
    public final void setStartPointColor(final Color startPointColor) {
        if (this.m_startPointPainter != null) {
            final Color old = this.m_startPointPainter.getColor();
            this.m_startPointPainter.setColor(startPointColor);
            this.m_propertyChangeSupport.firePropertyChange(IErrorBarPainter.PROPERTY_STARTPOINT_COLOR, old, startPointColor);
        }
    }

    /**
     * @see info.monitorenter.gui.chart.IErrorBarPainter#setStartPointPainter(IPointPainterConfigurableUI)
     */
    public final void setStartPointPainter(final IPointPainterConfigurableUI<?> startPointPainter) {
        final IPointPainter<?> old = this.m_startPointPainter;
        if (this.m_startPointPainter != null) {
            final Color oldColor = this.m_startPointPainter.getColor();
            if (oldColor != null) {
                if (startPointPainter != null) {
                    startPointPainter.setColor(oldColor);
                }
            }
        }
        this.m_startPointPainter = startPointPainter;
        this.m_propertyChangeSupport.firePropertyChange(IErrorBarPainter.PROPERTY_STARTPOINT, old, this.m_startPointPainter);
    }

}
