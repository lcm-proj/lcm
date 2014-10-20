/*
 *  MockGraphics2D.java, a mock for java.awt.Graphics2D.
 *  Copyright (C) Achim Westermann, created on 23.04.2005, 15:17:47
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.chart;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * A mock Object for <code>{@link Graphics2D}</code>.
 * <p>
 * 
 * 
 * A quick-hack mock object to fool Chart2D's paint method. Used for debugging /
 * testing.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * 
 */
public class MockGraphics2D
    extends Graphics2D {

  /**
   * defcon.
   * <p>
   */
  public MockGraphics2D() {
    super();
  }

  /**
   * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
   */
  @Override
  public void addRenderingHints(final Map< ? , ? > hints) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#clearRect(int, int, int, int)
   */
  @Override
  public void clearRect(final int x, final int y, final int width, final int height) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#clip(java.awt.Shape)
   */
  @Override
  public void clip(final Shape s) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#clipRect(int, int, int, int)
   */
  @Override
  public void clipRect(final int x, final int y, final int width, final int height) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
   */
  @Override
  public void copyArea(final int x, final int y, final int width, final int height, final int dx,
      final int dy) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#create()
   */
  @Override
  public Graphics create() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics#dispose()
   */
  @Override
  public void dispose() {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#draw(java.awt.Shape)
   */
  @Override
  public void draw(final Shape s) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
   */
  @Override
  public void drawArc(final int x, final int y, final int width, final int height,
      final int startAngle, final int arcAngle) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float,
   *      float)
   */
  @Override
  public void drawGlyphVector(final GlyphVector g, final float x, final float y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage,
   *      java.awt.image.BufferedImageOp, int, int)
   */
  @Override
  public void drawImage(final BufferedImage img, final BufferedImageOp op, final int x, final int y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawImage(java.awt.Image,
   *      java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final AffineTransform xform, final ImageObserver obs) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color,
   *      java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final int x, final int y, final Color bgcolor,
      final ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawImage(java.awt.Image, int, int,
   *      java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final int x, final int y, final ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
   *      java.awt.Color, java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final int x, final int y, final int width,
      final int height, final Color bgcolor, final ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
   *      java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final int x, final int y, final int width,
      final int height, final ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int,
   *      int, int, int, java.awt.Color, java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2,
      final int dy2, final int sx1, final int sy1, final int sx2, final int sy2,
      final Color bgcolor, final ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int,
   *      int, int, int, java.awt.image.ImageObserver)
   */
  @Override
  public boolean drawImage(final Image img, final int dx1, final int dy1, final int dx2,
      final int dy2, final int sx1, final int sy1, final int sx2, final int sy2,
      final ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics#drawLine(int, int, int, int)
   */
  @Override
  public void drawLine(final int x1, final int y1, final int x2, final int y2) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawOval(int, int, int, int)
   */
  @Override
  public void drawOval(final int x, final int y, final int width, final int height) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawPolygon(int[], int[], int)
   */
  @Override
  public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawPolyline(int[], int[], int)
   */
  @Override
  public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage,
   *      java.awt.geom.AffineTransform)
   */
  @Override
  public void drawRenderableImage(final RenderableImage img, final AffineTransform xform) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage,
   *      java.awt.geom.AffineTransform)
   */
  @Override
  public void drawRenderedImage(final RenderedImage img, final AffineTransform xform) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
   */
  @Override
  public void drawRoundRect(final int x, final int y, final int width, final int height,
      final int arcWidth, final int arcHeight) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
   *      float, float)
   */
  @Override
  public void drawString(final AttributedCharacterIterator iterator, final float x, final float y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawString(java.text.AttributedCharacterIterator,
   *      int, int)
   */
  @Override
  public void drawString(final AttributedCharacterIterator iterator, final int x, final int y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
   */
  @Override
  public void drawString(final String s, final float x, final float y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#drawString(java.lang.String, int, int)
   */
  @Override
  public void drawString(final String str, final int x, final int y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#fill(java.awt.Shape)
   */
  @Override
  public void fill(final Shape s) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
   */
  @Override
  public void fillArc(final int x, final int y, final int width, final int height,
      final int startAngle, final int arcAngle) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#fillOval(int, int, int, int)
   */
  @Override
  public void fillOval(final int x, final int y, final int width, final int height) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#fillPolygon(int[], int[], int)
   */
  @Override
  public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#fillRect(int, int, int, int)
   */
  @Override
  public void fillRect(final int x, final int y, final int width, final int height) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
   */
  @Override
  public void fillRoundRect(final int x, final int y, final int width, final int height,
      final int arcWidth, final int arcHeight) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#getBackground()
   */
  @Override
  public Color getBackground() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics#getClip()
   */
  @Override
  public Shape getClip() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics#getClipBounds()
   */
  @Override
  public Rectangle getClipBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics#getColor()
   */
  @Override
  public Color getColor() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getComposite()
   */
  @Override
  public Composite getComposite() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getDeviceConfiguration()
   */
  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics#getFont()
   */
  @Override
  public Font getFont() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
   */
  @Override
  public FontMetrics getFontMetrics(final Font f) {
    // TODO Auto-generated method stub
    return new MockFontMetrics(new Font("SansSerif", Font.PLAIN, 10));
  }

  /**
   * @see java.awt.Graphics2D#getFontRenderContext()
   */
  @Override
  public FontRenderContext getFontRenderContext() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getPaint()
   */
  @Override
  public Paint getPaint() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
   */
  @Override
  public Object getRenderingHint(final Key hintKey) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getRenderingHints()
   */
  @Override
  public RenderingHints getRenderingHints() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getStroke()
   */
  @Override
  public Stroke getStroke() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#getTransform()
   */
  @Override
  public AffineTransform getTransform() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
   */
  @Override
  public boolean hit(final Rectangle rect, final Shape s, final boolean onStroke) {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see java.awt.Graphics2D#rotate(double)
   */
  @Override
  public void rotate(final double theta) {
    // nop
  }

  /**
   * @see java.awt.Graphics2D#rotate(double, double, double)
   */
  @Override
  public void rotate(final double theta, final double x, final double y) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#scale(double, double)
   */
  @Override
  public void scale(final double sx, final double sy) {
    // nop

  }

  /**
   * @see java.awt.Graphics2D#setBackground(java.awt.Color)
   */
  @Override
  public void setBackground(final Color color) {
    // nop

  }

  /**
   * @see java.awt.Graphics#setClip(int, int, int, int)
   */
  @Override
  public void setClip(final int x, final int y, final int width, final int height) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#setClip(java.awt.Shape)
   */
  @Override
  public void setClip(final Shape clip) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#setColor(java.awt.Color)
   */
  @Override
  public void setColor(final Color c) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
   */
  @Override
  public void setComposite(final Composite comp) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#setFont(java.awt.Font)
   */
  @Override
  public void setFont(final Font font) {
    // TODO Auto-generated method stub
  }

  /**
   * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
   */
  @Override
  public void setPaint(final Paint paint) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#setPaintMode()
   */
  @Override
  public void setPaintMode() {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key,
   *      java.lang.Object)
   */
  @Override
  public void setRenderingHint(final Key hintKey, final Object hintValue) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
   */
  @Override
  public void setRenderingHints(final Map< ? , ? > hints) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
   */
  @Override
  public void setStroke(final Stroke s) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
   */
  @Override
  public void setTransform(final AffineTransform tx) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics#setXORMode(java.awt.Color)
   */
  @Override
  public void setXORMode(final Color c1) {
    // TODO Auto-generated method stub

  }

  /**
   * @see java.awt.Graphics2D#shear(double, double)
   */
  @Override
  public void shear(final double shx, final double shy) {
    // nop
  }

  /**
   * 
   * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
   */
  @Override
  public void transform(final AffineTransform tx) {
    // nop
  }

  /**
   * @see java.awt.Graphics2D#translate(double, double)
   */
  @Override
  public void translate(final double tx, final double ty) {
    // nop
  }

  /**
   * @see java.awt.Graphics#translate(int, int)
   */
  @Override
  public void translate(final int x, final int y) {
    // TODO Auto-generated method stub

  }
}
