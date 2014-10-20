/*
 *  IPointPainterConfigurableUI.java of project jchart2d, adds configuration of 
 *  UI like strokes and color to IPointPainter. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on Jun 11, 2011
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 *
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/IPointPainterConfigurableUI.java,v $
 * Date   : $Date: 2011/01/14 08:36:10 $
 * Version: $Revision: 1.4 $
 */

package info.monitorenter.gui.chart;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;

/**
 * Adds configuration of UI like strokes and color to IPointPainter.
 * <p>
 * 
 * @param <T>
 *          needed for generics <code>{@link #compareTo(Object)}</code>.
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public interface IPointPainterConfigurableUI<T extends IPointPainter<T>> extends IPointPainter<T> {

  /**
   * Returns the color to paint with or
   * <code>null</null> if no special color is desired.
   * <p>
   * 
   * @return the color to paint with <code>null</null> if no special color is
   *         desired.
   */
  public Color getColor();

  /**
   * Returns the color to paint fillings with or
   * <code>null</null> if no special color is desired.
   * <p>
   * 
   * @return the color to paint fillings with <code>null</null> if no special
   *         color is desired.
   */
  public Color getColorFill();

  /**
   * Returns the stroke to paint with.
   * <p>
   * 
   * @return the stroke to paint with.
   */
  public Stroke getStroke();

  /**
   * Returns the transparency to use for painting.
   * <p>
   * This value will be computed from the color used. If that color is not
   * configured (null) a value of 0.0 is returned even if the color from the
   * <code>{@link Graphics}</code> used for painting has a different setting!
   * <p>
   * 
   * 
   * @return the transparency used between 0 and 255.
   */
  public int getTransparency();

  /**
   * Returns the transparency to use for fill painting.
   * <p>
   * This value will be computed from the color used. If that color is not
   * configured (null) a value of 0.0 is returned even if the color from the
   * <code>{@link Graphics}</code> used for painting has a different setting!
   * <p>
   * 
   * @return the fill transparency used between 0 and 255.
   */
  public int getTransparencyFill();

  /**
   * Sets the color to paint with or <code>null</code> if no special color is
   * desired.
   * <p>
   * In the latter case the color of the <code>{@link Graphics}</code> provided
   * for paint operations will be used.
   * <p>
   * 
   * @param color
   *          the color to paint with or <code>null</code> if no special color
   *          is desired.
   * 
   * @return the previous color to paint with
   *         <code>null</null> if no special color is
   *         desired.
   */
  public Color setColor(final Color color);

  /**
   * Sets the color to paint fillings with or <code>null</code> if no special
   * fill color is desired.
   * <p>
   * In the latter case the color of the <code>{@link Graphics}</code> provided
   * for paint fill operations will be used.
   * <p>
   * 
   * @param fillColor
   *          the color to paint fillings with or <code>null</code> if no
   *          special color is desired.
   * 
   * @return the previous color to paint fillings with
   *         <code>null</null> if no special color is
   *         desired.
   */
  public Color setColorFill(final Color fillColor);

  /**
   * Sets the stroke to paint with or <code>null</code> if no special color is
   * desired.
   * <p>
   * In the latter case the stroke of the <code>{@link Graphics}</code> provided
   * for paint operations will be used.
   * <p>
   * 
   * @param stroke
   *          the stroke to paint with.
   * 
   * @return the previous color being used or <code>null</code> if none was used
   *         before.
   */
  public Stroke setStroke(final Stroke stroke);

  /**
   * Sets the transparency to use for painting.
   * <p>
   * This value will be fold into color. If color has not been configured before
   * it will not have any effect.
   * <p>
   * Caution: When using a value greater 0 may cost a multiple cpu load!
   * <p>
   * 
   * @param transparency0to255
   *          a transparency value between 0 and 255.
   * 
   * @return the previous transparency used.
   */
  public int setTransparency(final int transparency0to255);

  /**
   * Sets the transparency to use for fill painting.
   * <p>
   * This value will be fold into fill color. If fill color has not been
   * configured before it will not have any effect.
   * <p>
   * <p>
   * Caution: When using a value greater 0 may cost a multiple cpu load!
   * <p>
   * 
   * @param transparency0to255
   *          a transparency value between 0 and 255.
   * 
   * @return the previous fill transparency used.
   */
  public int setTransparencyFill(final int transparency0to255);

}
