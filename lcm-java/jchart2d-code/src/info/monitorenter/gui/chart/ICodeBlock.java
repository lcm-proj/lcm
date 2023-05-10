/*
 *  ICodeBlock,java of project jchart2d, interface to allow running certain code in a certain scope. 
 *  Copyright 2011 (C) Achim Westermann.
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

/**
 * Helper to allow running certain code in a certain scope (e.g.: synchronization scope, pre- or postrequisites to meet,...).
 * <p>
 * 
 * @param <T>
 *          the type of result of the callback method.
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */
public interface ICodeBlock<T> {
  /**
   * Executes the code this instance stands for and gives back the result.
   * <p>
   * 
   * @return the result.
   */
  public T execute();
}
