/*
 *  IErrorBarPolicy.java of project jchart2d, interface for a facade 
 *  towards painting error bars that adds a layer of configurability. 
 *  Copyright (c) 2004 - 2011 Achim Westermann.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart;

import info.monitorenter.gui.chart.errorbars.ErrorBarPixel;

import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JComponent;

/**
 * Interface for a facade towards painting error bars that adds a layer of
 * configuration.
 * <p>
 * 
 * It acts as a facade for an
 * <code>{@link info.monitorenter.gui.chart.ITracePainter}</code> with
 * configurable
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code> instances
 * that will be provided with configured
 * <code>{@link info.monitorenter.gui.chart.IErrorBarPixel}</code> instances.
 * <p>
 * 
 * <h3>Property Change events</h3>
 * The following table describes the contract of this interface of
 * <code>{@link java.beans.PropertyChangeEvent}</code> instances that are thrown
 * by methods.
 * <table border="0">
 * <tr>
 * <th>thrown by method</th>
 * <th><code>getPropertyName()</code></th>
 * <th><code>getSource()</code></th>
 * <th><code>getOldValue()</code></th>
 * <th><code>getNewValue()</code></th>
 * </tr>
 * <tr>
 * <th>All mutator methods that would cause different rendering.</th>
 * <td><code>{@link #PROPERTY_CONFIGURATION}</code></td>
 * <td><code>{@link IErrorBarPolicy}</code> that changed</td>
 * <td><code>null</code>, as this event marks a general change</td>
 * <td><code>null</code>, as this event marks a general change</td>
 * </tr>
 * <tr>
 * <th>{@link #addErrorBarPainter(IErrorBarPainter)}</th>
 * <td><code>{@link #PROPERTY_ERRORBARPAINTER}</code></td>
 * <td><code>{@link IErrorBarPolicy}</code> that changed</td>
 * <td><code>null</code>, which marks that a new painter was added.</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code>, the
 * added painter.</td>
 * </tr>
 * <tr>
 * <th>{@link #removeErrorBarPainter(IErrorBarPainter)}</th>
 * <td><code>{@link #PROPERTY_ERRORBARPAINTER}</code></td>
 * <td><code>{@link IErrorBarPolicy}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code>, the
 * removed painter.</td>
 * <td><code>null</code>, which marks that a painter was removed.</td>
 * </tr>
 * <tr>
 * <th>all mutator methods of
 * {@link info.monitorenter.gui.chart.IErrorBarPainter}</th>
 * <td><code>{@link #PROPERTY_ERRORBARPAINTER}</code></td>
 * <td><code>{@link IErrorBarPolicy}</code> that changed</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code>, the
 * changed painter.</td>
 * <td><code>{@link info.monitorenter.gui.chart.IErrorBarPainter}</code>, the
 * changed painter (same as old value).</td>
 * </tr>
 * <tr>
 * </table>
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 * @version $Revision: 1.21 $
 * 
 * @param <T>
 *          needed for generic comparable<T>.
 */
public interface IErrorBarPolicy<T extends IErrorBarPolicy<T>> extends ITracePainter<T> {
  /**
   * The property key defining a general change of an instance.
   * <p>
   * This is fired whenever the internal configuration of the error bar policy
   * changes. This internal configuration should be of no interest for clients
   * of this interface. What counts is that rendering of the error bars will
   * have changed whenever this event is fired. Subclasses might fire this event
   * to tell exactly this: "Rendering has changed. Please repaint."
   * <p>
   * As it is of no interest and knowledge which configuration has changed the
   * {@link java.beans.PropertyChangeEvent#getNewValue()} and the
   * {@link java.beans.PropertyChangeEvent#getOldValue()} of the
   * {@link java.beans.PropertyChangeEvent} given to
   * {@link PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)}
   * of listeners should be null always.
   * <p>
   * 
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * 
   */
  public static final String PROPERTY_CONFIGURATION = "IErrorBarPolicy.PROPERTY_CONFIGURATION";

  /**
   * This is fired whenever the internal set of error bar painters changes.
   * <p>
   * 
   * Namely from <code>{@link  #addErrorBarPainter(IErrorBarPainter)}</code>
   * <code>{@link #setErrorBarPainter(IErrorBarPainter)}</code> and
   * <code>{@link #removeErrorBarPainter(IErrorBarPainter)}</code>.
   * <p>
   * Use in combination with
   * {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
   * <p>
   * 
   * 
   */
  public static final String PROPERTY_ERRORBARPAINTER = "IErrorBarPolicy.PROPERTY_ERRORBARPAINTER";

  /**
   * Adds the given error bar painter to the list of painters of this instance.
   * <p>
   * 
   * @param painter
   *          the painter to use.
   * 
   */
  public void addErrorBarPainter(IErrorBarPainter painter);

  /**
   * Registers a property change listener that will be informed about changes of
   * the property identified by the given <code>propertyName</code>.
   * <p>
   * 
   * @param propertyName
   *          the name of the property the listener is interested in
   * 
   * @param listener
   *          a listener that will only be informed if the property identified
   *          by the argument <code>propertyName</code> changes
   * 
   * 
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

  /**
   * Calculates the errors of the given errorbar according to the point to
   * render and the configuration of this instance.
   * <p>
   * 
   * @param xPixel
   *          the x value in pixel to render an error bar for.
   * 
   * @param yPixel
   *          the y value in pixel to render an error bar for.
   * 
   * @param errorBar
   *          an error bar to use: This is for design reasons as internally this
   *          method is used too with a reused instance.
   * 
   * @param original
   *          the original point, possibly useful for calculations.
   * 
   */
  public void calculateErrorBar(final int xPixel, final int yPixel, final ErrorBarPixel errorBar,
      final ITracePoint2D original);

  /**
   * Allows an implementation to return a <code>JComponent</code> that takes
   * care of custom configuration properties for the UI support of error bar
   * policies.
   * <p>
   * Returns a <code>JComponent</code> that - stand alone - takes care of
   * configuring custom properties or null if nothing is required. This will be
   * integrated in the error bar wizard UI of jchart2d.
   * <p>
   * 
   * @return a <code>JComponent</code> that - stand alone - takes care of
   *         configuring custom properties or null if nothing is required.
   */
  public JComponent getCustomConfigurator();

  /**
   * Returns the set of {@link IErrorBarPainter} to use.
   * <p>
   * 
   * @return the set of {@link IErrorBarPainter} to use.
   */
  public Set<IErrorBarPainter> getErrorBarPainters();

  /**
   * Returns the relative x error (value domain) that is added to / subtracted
   * from the values to display.
   * <p>
   * 
   * @param xValue
   *          the absolute x value (not pixel) to get the error for.
   * 
   * @return the relative x error in value domain that is added to / subtracted
   *         from the values to display.
   */
  public double getXError(final double xValue);

  /**
   * Returns the relative y error (value domain) that is added to / subtracted
   * from the values to display.
   * <p>
   * 
   * @param yValue
   *          the absolute y value (not pixel) to get the error for.
   * 
   * @return the relative y error in value domain that is added to / subtracted
   *         from the values to display.
   */
  public double getYError(final double yValue);

  /**
   * Returns true if negative errors in x dimension are shown.
   * <p>
   * 
   * @return true if negative errors in x dimension are shown.
   */
  public boolean isShowNegativeXErrors();

  /**
   * Returns true if negative errors in y dimension are shown.
   * <p>
   * 
   * @return true if negative errors in y dimension are shown.
   */
  public boolean isShowNegativeYErrors();

  /**
   * Returns true if positive errors in x dimension are shown.
   * <p>
   * 
   * @return true if positive errors in x dimension are shown.
   */
  public boolean isShowPositiveXErrors();

  /**
   * Returns true if positive errors in y dimension are shown.
   * <p>
   * 
   * @return true if positive errors in y dimension are shown.
   */
  public boolean isShowPositiveYErrors();

  /**
   * Removes the given error bar painter.
   * <p>
   * 
   * @param painter
   *          the error bar painter to remove.
   * 
   * @return true if the given error bar painter was removed, comparison by the
   *         means of the equals operation.
   */
  public boolean removeErrorBarPainter(IErrorBarPainter painter);

  /**
   * Deregisters a property change listener that has been registerd for
   * listening on all properties.
   * <p>
   * 
   * @param listener
   *          a listener that will only be informed if the property identified
   *          by the argument <code>propertyName</code> changes
   * 
   * 
   */
  public void removePropertyChangeListener(PropertyChangeListener listener);

  /**
   * Removes a property change listener for listening on the given property.
   * <p>
   * 
   * @param property
   *          one of the constants with teh <code>PROPERTY_</code> prefix
   *          defined in this class or subclasses.
   * 
   * @param listener
   *          the listener for this property change.
   */
  public void removePropertyChangeListener(String property, PropertyChangeListener listener);

  /**
   * Makes the given error bar painter the sole painter for error bars of this
   * instance.
   * <p>
   * 
   * @param painter
   *          the painter to use.
   */
  public void setErrorBarPainter(IErrorBarPainter painter);

  /**
   * Set whether negative errors in x dimension should be shown.
   * <p>
   * 
   * @param showNegativeXErrors
   *          if true negative errors in x dimension will be shown.
   */
  public void setShowNegativeXErrors(final boolean showNegativeXErrors);

  /**
   * Set whether negative errors in y dimension should be shown.
   * <p>
   * 
   * @param showNegativeYErrors
   *          if true negative errors in y dimension will be shown.
   */
  public void setShowNegativeYErrors(final boolean showNegativeYErrors);

  /**
   * Set whether positive errors in x dimension should be shown.
   * <p>
   * 
   * @param showPositiveXErrors
   *          if true positive errors in x dimension will be shown.
   */
  public void setShowPositiveXErrors(final boolean showPositiveXErrors);

  /**
   * Set whether positive errors in y dimension should be shown.
   * <p>
   * 
   * @param showPositiveYErrors
   *          if true positive errors in y dimension will be shown.
   */
  public void setShowPositiveYErrors(final boolean showPositiveYErrors);

  /**
   * Intended for {@link info.monitorenter.gui.chart.traces.ATrace2D} only that
   * will register itself to the instances added to it.
   * <p>
   * This is support for error bar policies that need information about the
   * whole trace (e.g. median value). It has nothing to do with the kind of
   * error bar policy to be used by a trace. See
   * {@link ITrace2D#setErrorBarPolicy(IErrorBarPolicy)} and
   * {@link ITrace2D#addErrorBarPolicy(IErrorBarPolicy)} for this feature
   * instead.
   * <p>
   * 
   * @param trace
   *          the trace error bars are rendered for.
   */
  public void setTrace(final ITrace2D trace);
}
