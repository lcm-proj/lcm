/*
 *  AnnotationContentComponentDataValues.java of project jchart2d, 
 *  annotation that displays the annotated point's data values. 
 *  Copyright (C) 2002 - 2011, Achim Westermann, created on 16.02.2009
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
 * File   : $Source: /cvsroot/jchart2d/jchart2d/src/info/monitorenter/gui/chart/annotations/AnnotationContentComponentDataValues.java,v $
 * Date   : $Date: 2011/01/14 08:36:11 $
 * Version: $Revision: 1.5 $
 */

package info.monitorenter.gui.chart.annotations;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

/**
 * Annotation that displays the annotated point's data values.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 **/
public final class AnnotationContentComponentDataValues extends AAnnotationContentComponent {

  /** Internal padding. */
  private static final int PADDING = 8;

  /** Generated <code>serialVersionUID</code>. **/
  private static final long serialVersionUID = 5288884368670766243L;

  /** Internal text field used to display the value. */
  private JTextField m_textfield;

  /**
   * Creates an instance that is related to the given point to annotate.
   * <p>
   * 
   * @param point
   *          the point to annotate.
   */
  public AnnotationContentComponentDataValues(final ITracePoint2D point) {
    super(point);
    this.m_textfield = new JTextField();
    this.m_textfield.setEditable(false);

    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(Box.createHorizontalStrut(8));
    this.add(this.m_textfield);
    this.add(Box.createHorizontalStrut(8));
    // at least set the text to the text field (perhaps it's get PreferredSize
    // impl. will use it.
    this.updateDataValueTextField();
  }

  /**
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {

    Dimension result = this.m_textfield.getPreferredSize();
    result.height += AnnotationContentComponentDataValues.PADDING * 2;
    result.width += AnnotationContentComponentDataValues.PADDING * 2;
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.annotations.AAnnotationContentComponent#paintAnnotation(Graphics,
   *      info.monitorenter.gui.chart.Chart2D,
   *      info.monitorenter.gui.chart.ITrace2D,
   *      info.monitorenter.gui.chart.ITracePoint2D)
   */
  @Override
  public void paintAnnotation(final Graphics g, final Chart2D chart, final ITrace2D trace,
      final ITracePoint2D point) {
    // this.updateDataValueTextField();

  }

  /**
   * @see javax.swing.JComponent#setBackground(java.awt.Color)
   */
  @Override
  public void setBackground(final Color bg) {
    super.setBackground(bg);
    if (this.m_textfield != null) {
      this.m_textfield.setBackground(bg);
    }
  }

  /**
   * Internally updates the text field value to the data value of the point that
   * is annotated.
   * <p>
   * 
   */
  private void updateDataValueTextField() {
    ITracePoint2D point = this.getAnnotatedPoint();
    StringBuilder textbuffer = new StringBuilder();
    textbuffer.append("(").append(point.getX()).append(", ").append(point.getY()).append(')');
    String text = textbuffer.toString();
    // this.m_textfield.setColumns(text.length());
    // TODO: maybe preferred size of text field is enough?
    FontMetrics fmetrics = this.getFontMetrics(this.m_textfield.getFont());
    int width = fmetrics.stringWidth(text);
    int height = fmetrics.getHeight();

//    Dimension d = new Dimension(width, height);
    this.m_textfield.setText(textbuffer.toString());
    // this.m_textfield.setMaximumSize(d);
    // this.m_textfield.setPreferredSize(d);
    // this.m_textfield.setSize(d);
//    System.out.println("calculated: " + d + ", textfield: " + this.m_textfield.getPreferredSize());
  }

}
