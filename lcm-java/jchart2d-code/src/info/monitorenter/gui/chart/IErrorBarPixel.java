/*
 *  IErrorBarValue.java of project jchart2d, interface for an error bar. 
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

import java.io.Serializable;

/**
 * Interface for an error bar in the pixel domain (vs. value domain) for a single
 * {@link info.monitorenter.gui.chart.TracePoint2D}.
 * <p>
 * Errors returned from the getters represent only the error part and not the absolute value. Errors
 * are always absolute values (vs. relative to the original value to add the error to.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.10 $
 */
public interface IErrorBarPixel extends Serializable {

  /** Constant that identifies a resulting error to be non-existant. */
  public static final int ERROR_PIXEL_NONE = Integer.MAX_VALUE;

  /**
   * Returns the negative error (positive value) in X dimension as a pixel value or
   * {@link #ERROR_PIXEL_NONE}.
   * <p>
   * 
   * @return the negative error in X dimension or {@link #ERROR_PIXEL_NONE}.
   */
  public int getNegativeXErrorPixel();

  /**
   * Returns the negative error (positive value) in Y dimension as a pixel value or
   * {@link #ERROR_PIXEL_NONE}.
   * <p>
   * 
   * @return the negative error in Y dimension or {@link #ERROR_PIXEL_NONE}.
   */
  public int getNegativeYErrorPixel();

  /**
   * Returns the positive error in X dimension as a pixel value or {@link #ERROR_PIXEL_NONE}.
   * <p>
   * 
   * @return the positive error in X dimension or {@link #ERROR_PIXEL_NONE}.
   */
  public int getPositiveXErrorPixel();

  /**
   * Returns the positive error in Y dimension as a pixel value or{@link #ERROR_PIXEL_NONE}.
   * <p>
   * 
   * @return the positive error in Y dimension or {@link #ERROR_PIXEL_NONE}.
   */
  public int getPositiveYErrorPixel();

  /**
   * Returns the corresponding trace for this error bar.
   * <p>
   * 
   * @return the corresponding trace for this error bar.
   */
  public ITrace2D getTrace();
}
