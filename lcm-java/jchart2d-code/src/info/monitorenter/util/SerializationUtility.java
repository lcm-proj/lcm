/*
 *  SerializationUtility.java of project jchart2d, helpers for missing 
 *  serialization mechanism in java. 
 *  Copyright (C) 2004 - 2011 Achim Westermann.
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
package info.monitorenter.util;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A class containing useful utility methods relating to serialization.
 * <p>
 * This is originally inspired (serialization of trokes) by jfreechart.
 * 
 * @author David Gilbert (original)
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann</a>
 * @version $Revision: 1.6 $
 */
public final class SerializationUtility {

  /**
   * Reads a <code>Stroke</code> object that has been serialized by the
   * {@link SerializationUtility#writeStroke} method.
   * 
   * @param stream
   *            the input stream.
   * @return The stroke object.
   * @throws IOException
   *             if there is an I/O problem.
   * @throws ClassNotFoundException
   *             if there is a problem loading a class.
   */
  public static Stroke readStroke(final ObjectInputStream stream) throws IOException,
      ClassNotFoundException {

    Stroke result = null;
    boolean isNull = stream.readBoolean();
    if (!isNull) {
      Class< ? > c = (Class< ? >) stream.readObject();
      if (c.equals(BasicStroke.class)) {
        float width = stream.readFloat();
        int cap = stream.readInt();
        int join = stream.readInt();
        float miterLimit = stream.readFloat();
        float[] dash = (float[]) stream.readObject();
        float dashPhase = stream.readFloat();
        result = new BasicStroke(width, cap, join, miterLimit, dash, dashPhase);
      } else {
        result = (Stroke) stream.readObject();
      }
    }
    return result;
  }

  /**
   * Serializes a <code>Stroke</code> object.
   * 
   * @param stroke
   *            the stroke object.
   * @param stream
   *            the output stream.
   * @throws IOException
   *             if there is an I/O error.
   */
  public static void writeStroke(final Stroke stroke, final ObjectOutputStream stream)
      throws IOException {

    if (stroke != null) {
      stream.writeBoolean(false);
      if (stroke instanceof BasicStroke) {
        BasicStroke s = (BasicStroke) stroke;
        stream.writeObject(BasicStroke.class);
        stream.writeFloat(s.getLineWidth());
        stream.writeInt(s.getEndCap());
        stream.writeInt(s.getLineJoin());
        stream.writeFloat(s.getMiterLimit());
        stream.writeObject(s.getDashArray());
        stream.writeFloat(s.getDashPhase());
      } else {
        stream.writeObject(stroke.getClass());
        stream.writeObject(stroke);
      }
    } else {
      stream.writeBoolean(true);
    }
  }

  /**
   * Utility class constructor.
   * <p>
   */
  private SerializationUtility() {
    // nop
  }
}
