/*
 *  LabelFormatterDate.java of project jchart2d. A label formatter that uses formatted 
 *  dates. 
 *  Copyright (C) 2005 - 2011 Achim Westermann, created on 20.04.2005, 11:56:36
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
package info.monitorenter.gui.chart.labelformatters;

import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.util.Range;
import info.monitorenter.util.SimpleDateFormatAnalyzer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An ILabelFormatter instance that uses a {@link java.text.DateFormat} to
 * format the labels.
 * <p>
 * <b>Caution: <br>
 * It only makes sense to use this class if the data of the corresponding axis
 * may be interpreted as long number of milliseconds since the standard base
 * time known as "the epoch", namely January 1, 1970, 00:00:00 GMT. </b>
 * <p>
 * <b>Caution: <br>
 * This implementation is not completely conform with the constraint: <code>
 * instance.parse(instance.format(value)) == value
 * </code>
 * </b> This only works for subsequent call: one call to format contains the
 * next value to return from parse to be the same as the format. That value is
 * cached as date / time formatting produces truncation of the internal value
 * (e.g. if no year is displayed). <br>
 * Use: <br>
 * 
 * <pre>
 * 
 *     Chart2D chart = new &lt;Constructor&gt;
 *     Axis axis = new AxisSimple();
 *     axis.setFormatter(new LabelFormatterDate(DateFormat.getDateInstance()));
 *     chart.setAxisX(axis);
 * 
 * </pre>
 * 
 * to use this class.
 * <p>
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 * @version $Revision: 1.11 $
 * 
 * @see java.util.Date
 */
public class LabelFormatterDate extends ALabelFormatter implements IAxisLabelFormatter {

  /** Generated <code>serialVersionUID</code>. */
  private static final long serialVersionUID = -7201853569619240987L;

  /** The cached maximum amount of characters that will be used. */
  private int m_cachedMaxAmountChars = Integer.MAX_VALUE;

  /** The date formatter that is used. */
  private SimpleDateFormat m_dateFormat;

  /**
   * The last value that was formatted - needed for the parse - format contract.
   */
  private double m_lastFormatted = 0;

  /**
   * Default constructor that uses the local datetime (
   * <code>{@link DateFormat#getDateTimeInstance(int, int)}</code>) with
   * <code>{@link DateFormat#SHORT}</code> style.
   * <p>
   * 
   */
  public LabelFormatterDate() {
    this((SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
  }

  /**
   * Creates a formatter that uses the given date format.
   * <p>
   * 
   * @param dateFormat
   *          the date format to use.
   */
  public LabelFormatterDate(final SimpleDateFormat dateFormat) {
    super();
    this.m_dateFormat = dateFormat;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final LabelFormatterDate other = (LabelFormatterDate) obj;
    if (this.m_cachedMaxAmountChars != other.m_cachedMaxAmountChars) {
      return false;
    }
    if (this.m_dateFormat == null) {
      if (other.m_dateFormat != null) {
        return false;
      }
    } else if (!this.m_dateFormat.equals(other.m_dateFormat)) {
      return false;
    }
    if (Double.doubleToLongBits(this.m_lastFormatted) != Double
        .doubleToLongBits(other.m_lastFormatted)) {
      return false;
    }
    return true;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#format(double)
   */
  public String format(final double value) {
    this.m_lastFormatted = value;
    return this.m_dateFormat.format(new Date((long) value));
  }

  /**
   * Returns the date formatter that is used.
   * <p>
   * 
   * @return the date formatter that is used.
   */
  final SimpleDateFormat getDateFormate() {
    return this.m_dateFormat;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getMaxAmountChars()
   */
  @Override
  public int getMaxAmountChars() {
    final Range range = this.getAxis().getRange();
    final double dRange = range.getExtent();
    if (this.m_cachedMaxAmountChars == Integer.MAX_VALUE) {
      this.m_cachedMaxAmountChars = this.m_dateFormat.format(new Date((long) dRange)).length();
    }
    return this.m_cachedMaxAmountChars;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getMinimumValueShiftForChange()
   */
  public double getMinimumValueShiftForChange() {
    double ret = 0;
    if (SimpleDateFormatAnalyzer.displaysMillisecond(this.m_dateFormat)) {
      ret = 1;
    } else if (SimpleDateFormatAnalyzer.displaysSecond(this.m_dateFormat)) {
      ret = 1000;
    } else if (SimpleDateFormatAnalyzer.displaysMinute(this.m_dateFormat)) {
      ret = 60000;
    } else if (SimpleDateFormatAnalyzer.displaysHour(this.m_dateFormat)) {
      ret = 360000;
    } else if (SimpleDateFormatAnalyzer.displaysDay(this.m_dateFormat)) {
      ret = 24 * 360000;
    } else if (SimpleDateFormatAnalyzer.displaysMonth(this.m_dateFormat)) {
      ret = 31 * 24 * 360000;
    } else {
      ret = 12 * 31 * 24 * 60000;
    }
    return ret;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#getNextEvenValue(double,
   *      boolean)
   */
  public double getNextEvenValue(final double value, final boolean ceiling) {
    final Date d = new Date((long) value);
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(d);
    if (ceiling) {
      if (!SimpleDateFormatAnalyzer.displaysMillisecond(this.m_dateFormat)) {
        calendar.set(Calendar.MILLISECOND, 0);
        if (!SimpleDateFormatAnalyzer.displaysSecond(this.m_dateFormat)) {
          calendar.set(Calendar.SECOND, 0);
          if (!SimpleDateFormatAnalyzer.displaysMinute(this.m_dateFormat)) {
            calendar.set(Calendar.MINUTE, 0);
            if (!SimpleDateFormatAnalyzer.displaysHour(this.m_dateFormat)) {
              calendar.set(Calendar.HOUR, 0);
              if (!SimpleDateFormatAnalyzer.displaysDay(this.m_dateFormat)) {
                calendar.set(Calendar.DAY_OF_YEAR, 0);
                if (!SimpleDateFormatAnalyzer.displaysMonth(this.m_dateFormat)) {
                  calendar.set(Calendar.MONTH, 0);
                  if (!SimpleDateFormatAnalyzer.displaysYear(this.m_dateFormat)) {
                    calendar.set(Calendar.YEAR, 0);
                  }
                }
              }
            }
          }
        }
      }
    } else {
      if (!SimpleDateFormatAnalyzer.displaysMillisecond(this.m_dateFormat)) {
        calendar.set(Calendar.MILLISECOND, 1000);
        if (!SimpleDateFormatAnalyzer.displaysSecond(this.m_dateFormat)) {
          calendar.set(Calendar.SECOND, 60);
          if (!SimpleDateFormatAnalyzer.displaysMinute(this.m_dateFormat)) {
            calendar.set(Calendar.MINUTE, 60);
            if (!SimpleDateFormatAnalyzer.displaysHour(this.m_dateFormat)) {
              calendar.set(Calendar.HOUR, 24);
              if (!SimpleDateFormatAnalyzer.displaysDay(this.m_dateFormat)) {
                calendar.set(Calendar.DAY_OF_YEAR, 365);
                if (!SimpleDateFormatAnalyzer.displaysMonth(this.m_dateFormat)) {
                  calendar.set(Calendar.MONTH, 12);
                  if (!SimpleDateFormatAnalyzer.displaysYear(this.m_dateFormat)) {
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);
                  }
                }
              }
            }
          }
        }
      }
    }
    return calendar.getTimeInMillis();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + this.m_cachedMaxAmountChars;
    result = prime * result + ((this.m_dateFormat == null) ? 0 : this.m_dateFormat.hashCode());
    long temp;
    temp = Double.doubleToLongBits(this.m_lastFormatted);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisLabelFormatter#parse(java.lang.String)
   */
  public Number parse(final String formatted) throws NumberFormatException {
    return new Double(this.m_lastFormatted);
  }

  /**
   * Sets the date formatter to use.
   * <p>
   * 
   * @param df
   *          the date formatter to use.
   */
  final void setDateFormate(final SimpleDateFormat df) {
    this.m_dateFormat = df;
  }
}
