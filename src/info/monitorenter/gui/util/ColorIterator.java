/*
 *  ColorIterator.java, something that walks over the Hue-Saturation-Brightness 
 *  color space. 
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
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  If you modify or optimize the code in a useful way please let me know.
 *  Achim.Westermann@gmx.de
 *
 */
package info.monitorenter.gui.util;

import java.awt.Color;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator of the color space.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.10 $
 */
public class ColorIterator implements Iterator<Color> {

  /**
   * Just for protected internal float stepping.
   * <p>
   * 
   * 
   */
  public abstract static class ADefaultStepping implements ColorIterator.ISteppingModel {

    /** The internal step width. */
    protected double m_stepping;

    /**
     * Creates a stepper with 100 steps in the color space.
     * <p>
     */
    public ADefaultStepping() {
      this(100);
    }

    /**
     * Creates a stepper with the given step length.
     * <p>
     * 
     * @param steps
     *          the amount of steps to do in the color space.
     * 
     */
    public ADefaultStepping(final int steps) {
      this.setSteps(steps);
    }

    /**
     * Too lazy to implement for each subclass. An overhead for newInstance()
     * (return dynamic sub type) is paid here.
     * <p>
     * 
     * @return a clone of the stepper.
     */
    @Override
    public Object clone() {
      ADefaultStepping result = null;
      try {
        result = (ADefaultStepping) super.clone();
        result.m_stepping = this.m_stepping;
      } catch (final Throwable f) {
        f.printStackTrace();
      }
      return result;
    }

    /**
     * @see info.monitorenter.gui.util.ColorIterator.ISteppingModel#setSteps(int)
     */
    public void setSteps(final int steps) {
      this.m_stepping = 1.0 / steps;
    }
  }

  /**
   * A stepping model that steps on the alpha channel of the HSB color space.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.10 $
   */
  public static class AlphaStepper extends ColorIterator.ADefaultStepping {
    /**
     * Creates an instance with 100 alpha steps.
     * <p>
     */
    public AlphaStepper() {
      super();
    }

    /**
     * Creates an instance with the given stepping to go on the alpha channel of
     * the color space.
     * <p>
     * 
     * @param steps
     *          the amount of steps to take on the saturation line.
     * 
     */
    public AlphaStepper(final int steps) {
      super(steps);
    }

    /**
     * Performs a alpha step on the given ColorIterator's HSBColor.
     * <p>
     * The bounds are watched: if a step would cross 255, it will be continued
     * beginning from 0. if a step would cross the alpha value of the
     * ColorIterator's start alpha, the step will only go as far as this value.
     * Else there would be problems with finding the end of the iteration.
     * <p>
     * 
     * @param tostep
     *          the color iterator to perform the step on.
     */
    public void doStep(final ColorIterator tostep) {
      double increment = tostep.m_iterate.m_alpha;
      final double bound = tostep.m_startColor.m_alpha;
      // + operations:
      if (tostep.isAscendingDirection()) {
        // naive step without watching bounds:
        increment += this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_alpha < bound) && (increment > bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment > 255) {
            // 2.b) if so, shift by the value range (overflow)
            increment -= 255;
            // 2.c) check if we crossed the bound now:
            if (increment > bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      } else {
        // - operations:
        // naive step without watching bounds:
        increment -= this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_alpha > bound) && (increment < bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment < 0) {
            // 2.b) if so, shift by the value range (overflow)
            increment += 255;
            // 2.c) check if we crossed the bound now:
            if (increment < bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      }
      tostep.m_iterate.m_alpha = increment;
    }

    /**
     * @see info.monitorenter.gui.util.ColorIterator.ADefaultStepping#setSteps(int)
     */
    @Override
    public void setSteps(final int steps) {
      this.m_stepping = 255.0 / steps;
    }
  }

  /**
   * Base class for stepping models that may step in each direction of the Hue
   * Saturation Luminance color space.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.10 $
   */
  public abstract static class APiggyBackStepper implements ColorIterator.ISteppingModel {
    /** The hue stepper to use. */
    protected HueStepper m_huestep;

    /** The luminance stepper to use. */
    protected LuminanceStepper m_lumstep;

    /** The saturation stepper to use. */
    protected SaturationStepper m_satstep;

    /**
     * Creates an instance with an amount of steps of 100 for hue, saturation
     * and luminance.
     * <p>
     */
    public APiggyBackStepper() {
      this(100, 100, 100);
    }

    /**
     * Creates an instance that uses the given amount of steps for hue,
     * luminance and saturation.
     * <p>
     * 
     * @param hueSteps
     *          the amount of steps on the hue line of the HSB color space.
     * 
     * @param satSteps
     *          the amount of steps on the saturation line of the HSB color
     *          space.
     * 
     * @param lumSteps
     *          the amount of steps on the luminance line of the HSB color
     *          space.
     * 
     */
    public APiggyBackStepper(final int hueSteps, final int satSteps, final int lumSteps) {
      this.m_huestep = new HueStepper(hueSteps);
      this.m_satstep = new SaturationStepper(satSteps);
      this.m_lumstep = new LuminanceStepper(lumSteps);
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
      try {
        final APiggyBackStepper ret = (APiggyBackStepper) super.clone();
        ret.m_huestep = (HueStepper) this.m_huestep.clone();
        ret.m_satstep = (SaturationStepper) this.m_satstep.clone();
        ret.m_lumstep = (LuminanceStepper) this.m_lumstep.clone();
        return ret;
      } catch (final CloneNotSupportedException cne) {
        cne.printStackTrace(System.err);
        // this should never happen!
        throw new RuntimeException(cne);
      }
    }

    /**
     * @see info.monitorenter.gui.util.ColorIterator.ISteppingModel#setSteps(int)
     */
    public void setSteps(final int steps) {
      this.m_huestep.setSteps(steps);
      this.m_lumstep.setSteps(steps);
      this.m_satstep.setSteps(steps);
    }
  }

  /**
   * Performs hue steps until it has walked the whole hue line, then performs a
   * saturation step to start with hue steps again. If the saturation steps have
   * walked the whole saturation line, a luminance step is done before starting
   * with hue steps again.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.10 $
   */
  public static class HSBStepper extends ColorIterator.APiggyBackStepper {
    /**
     * Creates an instance that will perform 100 steps on the hue line then
     * perform 100 steps on the saturation line and then 100 steps on the
     * luminance line.
     * <p>
     */
    public HSBStepper() {
      super();
    }

    /**
     * @see info.monitorenter.gui.util.ColorIterator.ISteppingModel#doStep(info.monitorenter.gui.util.ColorIterator)
     */
    public void doStep(final ColorIterator tostep) {
      // technique: without testing the step is done
      // this allows to restart with hue step even if start.hue==iterate.hue
      // after having performed a step of different kind
      this.m_huestep.doStep(tostep);
      if (tostep.m_iterate.m_hue == tostep.m_startColor.m_hue) {
        this.m_satstep.doStep(tostep);
        if (tostep.m_iterate.m_sat == tostep.m_startColor.m_sat) {
          this.m_lumstep.doStep(tostep);
        }
      }
    }
  }

  /**
   * Performs hue steps until it has walked the whole hue line, then performs a
   * saturation step to start with hue steps again.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.10 $
   */
  public static class HSStepper extends ColorIterator.APiggyBackStepper {
    /**
     * Creates an instance that will perform 100 steps on the hue line and then
     * 100 steps on the saturation line.
     * <p>
     */
    public HSStepper() {
      // nop
    }

    /**
     * @see info.monitorenter.gui.util.ColorIterator.ISteppingModel#doStep(info.monitorenter.gui.util.ColorIterator)
     */
    public void doStep(final ColorIterator tostep) {
      // technique: without testing the step is done
      // this allows to restart with huestep even if start.hue==iterate.hue
      // after having performed a step of different kind
      this.m_huestep.doStep(tostep);
      if (tostep.m_iterate.m_hue == tostep.m_startColor.m_hue) {
        this.m_satstep.doStep(tostep);
      }
    }
  }

  /**
   * A stepper that walks along the hue line of the color space.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.10 $
   */
  public static class HueStepper extends ColorIterator.ADefaultStepping {
    /**
     * Creates an instance with 100 steps left.
     * <p>
     */
    public HueStepper() {
      super();
    }

    /**
     * Creates a stepper with the given step length.
     * <p>
     * 
     * @param steps
     *          the amount of steps to take in the hue direction.
     * 
     */
    public HueStepper(final int steps) {
      super(steps);
    }

    /**
     * Performs a hue step on the given ColorIterator's HSBColor.
     * <p>
     * 
     * The bounds are watched: if a hue step would cross 1.0 it will be
     * continued beginning from 0. if a hue step would cross the hue value of
     * the ColorIterator's start hue value, the step will only go as far as this
     * value. Else there would be problems with finding the end of the
     * iteration.
     * <p>
     * 
     * @param tostep
     *          the iterator to perform the step on.
     */
    public void doStep(final ColorIterator tostep) {
      double increment = tostep.m_iterate.m_hue;
      final double bound = tostep.m_startColor.m_hue;
      // + operations:
      if (tostep.isAscendingDirection()) {
        // naive step without watching bounds:
        increment += this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_hue < bound) && (increment > bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment > 1.0) {
            // 2.b) if so, shift by the value range (overflow)
            increment -= 1.0;
            // 2.c) check if we crossed the bound now:
            if (increment > bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      } else {
        // - operations:
        // naive step without watching bounds:
        increment -= this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_hue > bound) && (increment < bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment < 0) {
            // 2.b) if so, shift by the value range (overflow)
            increment += 1.0;
            // 2.c) check if we crossed the bound now:
            if (increment < bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      }
      tostep.m_iterate.m_hue = increment;
    }
  }

  /**
   * Defines the strategy of walking through the HSB color space.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.10 $
   */
  public static interface ISteppingModel extends Cloneable {
    /**
     * Creates a clone of this stepper.
     * <p>
     * 
     * @return a clone of this stepper.
     */
    public Object clone();

    /**
     * Performs a step on the given color iterator.
     * <p>
     * 
     * @param tostep
     *          the color iterator to perform a step on.
     */
    public void doStep(final ColorIterator tostep);

    /**
     * Sets the amount of steps in the color space.
     * <p>
     * 
     * @param steps
     *          the amount of steps in the color space.
     */
    public void setSteps(final int steps);
  }

  /**
   * A stepping model that steps on the luminance line of the HSB color space.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.10 $
   */
  public static class LuminanceStepper extends ColorIterator.ADefaultStepping {
    /**
     * Creates an instance with 100 luminance steps.
     * <p>
     */
    public LuminanceStepper() {
      super();
    }

    /**
     * Creates an instance with the given stepping to go on the luminance line
     * of the color space.
     * <p>
     * 
     * @param steps
     *          the amount of steps to take in the luminance space.
     * 
     */
    public LuminanceStepper(final int steps) {
      super(steps);
    }

    /**
     * Performs a luminance step on the given ColorIterator's HSBColor.
     * <p>
     * 
     * The bounds are watched: if a step would cross 1.0, it will be continued
     * beginning from 0. if a step would cross the luminance value of the
     * ColorIterator's start luminance, the step will only go as far as this
     * value. Else there would be problems with finding the end of the
     * iteration.
     * <p>
     * 
     * @param tostep
     *          the color iterator to perform the step on.
     */
    public void doStep(final ColorIterator tostep) {
      double increment = tostep.m_iterate.m_lum;
      final double bound = tostep.m_startColor.m_lum;
      // + operations:
      if (tostep.isAscendingDirection()) {
        // naive step without watching bounds:
        increment += this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_lum < bound) && (increment > bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment > 1.0) {
            // 2.b) if so, shift by the value range (overflow)
            increment -= 1.0;
            // 2.c) check if we crossed the bound now:
            if (increment > bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      } else {
        // - operations:
        // naive step without watching bounds:
        increment -= this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_lum > bound) && (increment < bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment < 0) {
            // 2.b) if so, shift by the value range (overflow)
            increment += 1.0;
            // 2.c) check if we crossed the bound now:
            if (increment < bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      }
      tostep.m_iterate.m_lum = increment;
    }
  }

  /**
   * A stepping model that steps on the saturation line of the HSB color space.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * @version $Revision: 1.10 $
   */
  public static class SaturationStepper extends ColorIterator.ADefaultStepping {
    /**
     * Creates an instance with 100 saturation steps.
     * <p>
     */
    public SaturationStepper() {
      super();
    }

    /**
     * Creates an instance with the given stepping to go on the saturation line
     * of the color space.
     * <p>
     * 
     * @param steps
     *          the amount of steps to take on the saturation line.
     * 
     */
    public SaturationStepper(final int steps) {
      super(steps);
    }

    /**
     * Performs a saturation step on the given ColorIterator's HSBColor.
     * <p>
     * The bounds are watched: if a step would cross 1.0, it will be continued
     * beginning from 0. if a step would cross the saturation value of the
     * ColorIterator's start saturation, the step will only go as far as this
     * value. Else there would be problems with finding the end of the
     * iteration.
     * <p>
     * 
     * @param tostep
     *          the color iterator to perform the step on.
     */
    public void doStep(final ColorIterator tostep) {
      double increment = tostep.m_iterate.m_sat;
      final double bound = tostep.m_startColor.m_sat;
      if (tostep.isAscendingDirection()) {
        // + operations
        // naive step without watching bounds:
        increment += this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_sat < bound) && (increment > bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment > 1.0) {
            // 2.b) if so, shift by the value range (overflow)
            increment -= 1.0;
            // 2.c) check if we crossed the bound now:
            if (increment > bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }
      } else {
        // - operations
        // naive step without watching bounds:
        increment -= this.m_stepping;
        // 1) check if we crossed the bound:;
        if ((tostep.m_iterate.m_sat > bound) && (increment < bound)) {
          increment = bound;
        } else {
          // 2.a) check if we crossed the value range:
          if (increment < 0.0) {
            // 2.b) if so, shift by the value range (overflow)
            increment += 1.0;
            // 2.c) check if we crossed the bound now:
            if (increment < bound) {
              // this test is sufficient as we know that we
              // started from lowest value due to the overflow
              // and still are higher than bound.
              increment = bound;
            }
          } else {
            // Nothing because we were bigger than bound before
            // the step and still did not hit the overflow!
          }
        }

      }
      tostep.m_iterate.m_sat = increment;
    }
  }

  /**
   * Main entry for a test application.
   * <p>
   * 
   * @param args
   *          ignored.
   */
  public static void main(final String[] args) {
    final javax.swing.JFrame frame = new javax.swing.JFrame(Messages.getString("ColorIterator.0")); //$NON-NLS-1$

    final javax.swing.JPanel panel = new javax.swing.JPanel() {
      /**
       * Generated <code>serialVersionUID</code>.
       */
      private static final long serialVersionUID = 3258408422146715703L;

      private final ColorIterator m_color = new ColorIterator();
      {
        // System.out.println("start: " + color.start.toString());
        // System.out.println("iterate: " + color.iterate.toString());
        int wdt = 0;
        while (this.m_color.hasNext()) {
          wdt++;
          this.m_color.next();
        }
        System.out
            .println(Messages.getString("ColorIterator.1") + wdt + Messages.getString("ColorIterator.2")); //$NON-NLS-1$ //$NON-NLS-2$
        System.out.println(Messages.getString("ColorIterator.3") + wdt); //$NON-NLS-1$
        this.setSize(wdt, 100);
        this.setPreferredSize(new java.awt.Dimension(wdt, 100));
        this.setMinimumSize(new java.awt.Dimension(wdt, 100));
      }

      /**
       * @see java.awt.Component#paint(java.awt.Graphics)
       */
      @Override
      public void paint(final java.awt.Graphics g) {
        super.paint(g);
        // refresh iterator
        this.m_color.reset();
        final int width = this.getWidth();
        final int height = this.getHeight();
        int pxdrawn = 0;
        while (this.m_color.hasNext()) {
          if (pxdrawn == width) {
            break;
          }
          g.setColor(this.m_color.next());
          g.drawLine(pxdrawn, 0, pxdrawn, height);
          pxdrawn++;
        }
      }
    };

    final javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(panel);
    final java.awt.Container contentPane = frame.getContentPane();
    contentPane.setLayout(new java.awt.BorderLayout());
    contentPane.add(scroll, java.awt.BorderLayout.CENTER);

    frame.setLocation(200, 200);
    frame.setSize(new java.awt.Dimension(400, 100));
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      /**
       * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
       */
      @Override
      public void windowClosing(final java.awt.event.WindowEvent e) {
        System.exit(0);
      }
    });
    frame.setResizable(true);
    frame.setVisible(true);
  }

  /** Flag to control the direction of iteration. */
  private boolean m_ascendingDirection = true;

  /** Flag to show if more colors are iterateable. */
  private boolean m_hasnext = true;

  /**
   * Flag to allow return the start color for the first step instead of stepping
   * forward.
   * <p>
   */
  private boolean m_firstTime = true;

  /** Reference to the currently iterated color. */
  protected HSBColor m_iterate;

  /**
   * To allow clean reset of ColorIterator, also the SteppingModel has to be
   * reset. This is done by a deep copy at construction time.
   */
  private final ColorIterator.ISteppingModel m_resetModel;

  /**
   * The starting color which is also used to detect if a whole iteration has
   * been performed.
   */
  protected HSBColor m_startColor;

  /** The stepping model that defines the path through the color space. */
  private ColorIterator.ISteppingModel m_stepModel;

  /**
   * Creates an instance that starts with a red color and walks the hue line
   * with a {@link ColorIterator.HueStepper}.
   * <p>
   */
  public ColorIterator() {
    this(Color.RED, new HueStepper(1000));
  }

  /**
   * Creates an instance that starts with the given color and uses the given
   * stepper for iteration.
   * <p>
   * 
   * @param startColor
   *          the color to start the iteration with.
   * 
   * @param stepper
   *          the stepping model to use.
   */
  public ColorIterator(final Color startColor, final ColorIterator.ISteppingModel stepper) {
    this.setStartColor(startColor);
    this.m_stepModel = stepper;
    this.m_resetModel = (ColorIterator.ISteppingModel) this.m_stepModel.clone();
    this.m_firstTime = true;
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
    final ColorIterator other = (ColorIterator) obj;
    if (this.m_ascendingDirection != other.m_ascendingDirection) {
      return false;
    }
    if (this.m_firstTime != other.m_firstTime) {
      return false;
    }
    if (this.m_hasnext != other.m_hasnext) {
      return false;
    }
    if (this.m_iterate == null) {
      if (other.m_iterate != null) {
        return false;
      }
    } else if (!this.m_iterate.equals(other.m_iterate)) {
      return false;
    }
    if (this.m_resetModel == null) {
      if (other.m_resetModel != null) {
        return false;
      }
    } else if (!this.m_resetModel.equals(other.m_resetModel)) {
      return false;
    }
    if (this.m_startColor == null) {
      if (other.m_startColor != null) {
        return false;
      }
    } else if (!this.m_startColor.equals(other.m_startColor)) {
      return false;
    }
    if (this.m_stepModel == null) {
      if (other.m_stepModel != null) {
        return false;
      }
    } else if (!this.m_stepModel.equals(other.m_stepModel)) {
      return false;
    }
    return true;
  }

  /**
   * Returns the starting color which is also used to detect if a whole
   * iteration has been performed.
   * <p>
   * 
   * @return the starting color which is also used to detect if a whole
   *         iteration has been performed.
   */
  public final Color getStartColor() {
    return this.m_startColor.getRGBColor();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.m_ascendingDirection ? 1231 : 1237);
    result = prime * result + (this.m_firstTime ? 1231 : 1237);
    result = prime * result + (this.m_hasnext ? 1231 : 1237);
    result = prime * result + ((this.m_iterate == null) ? 0 : this.m_iterate.hashCode());
    result = prime * result + ((this.m_resetModel == null) ? 0 : this.m_resetModel.hashCode());
    result = prime * result + ((this.m_startColor == null) ? 0 : this.m_startColor.hashCode());
    result = prime * result + ((this.m_stepModel == null) ? 0 : this.m_stepModel.hashCode());
    return result;
  }

  /**
   * Returns true if more colors are available.
   * <p>
   * 
   * @return true if more colors are available.
   * 
   * @see java.util.Iterator#hasNext()
   */
  public boolean hasNext() {
    return this.m_hasnext;
  }

  /**
   * Returns the ascendingDirection.
   * <p>
   * 
   * @see #setAscendingDirection(boolean)
   * 
   * @return the ascendingDirection
   */
  public final boolean isAscendingDirection() {
    return this.m_ascendingDirection;
  }

  /**
   * Returns instances of java.awt.Color or throws a NoSuchElementException, if
   * iterator has finished.
   * <p>
   * 
   * @return the next available Color.
   * 
   * @throws NoSuchElementException
   *           if {@link #hasNext()} returns false.
   */
  public Color next() throws NoSuchElementException {
    if (!this.m_hasnext) {
      throw new java.util.NoSuchElementException(Messages.getString("ColorIterator.4")); //$NON-NLS-1$
    }
    if (!this.m_firstTime) {
      this.m_stepModel.doStep(this);
      if (this.m_iterate.equals(this.m_startColor)) {
        this.m_hasnext = false;
      }
    } else {
      this.m_firstTime = false;
    }
    return this.m_iterate.getRGBColor();
  }

  /**
   * Nothing is done here. Do you really want to remove a color from the color
   * circle model?
   * <p>
   */
  public void remove() {
    // nop
  }

  /**
   * Resets the ColorIterator. It will be able to start a new iteration over the
   * color space.
   * <p>
   */
  public void reset() {
    this.m_iterate = (HSBColor) this.m_startColor.clone();
    // also reset the SteppingModel!!!!
    this.m_stepModel = (ColorIterator.ISteppingModel) this.m_resetModel.clone();
    this.m_hasnext = true;
    this.m_firstTime = true;
  }

  /**
   * Sets whether the color space should be iterated in ascending direction (+
   * operations) or descending direction(- operations).
   * <p>
   * 
   * @param ascendingDirection
   *          if true the color space will be iterated in ascending direction.
   */
  public final void setAscendingDirection(final boolean ascendingDirection) {
    this.m_ascendingDirection = ascendingDirection;
  }

  /**
   * Sets the starting color which is also used to detect if a whole iteration
   * has been performed.
   * <p>
   * 
   * @param startColor
   *          the starting color which is also used to detect if a whole
   *          iteration has been performed.
   */
  public final void setStartColor(final Color startColor) {
    this.m_startColor = HSBColor.rgbToHSB(startColor);
    this.m_iterate = (HSBColor) this.m_startColor.clone();
  }

  /**
   * Sets the amount of colors to iterate over.
   * <p>
   * 
   * @param steps
   *          the amount of steps to take in the color space.
   */
  public void setSteps(final int steps) {
    this.m_resetModel.setSteps(steps);
    this.m_stepModel.setSteps(steps);

  }
}
