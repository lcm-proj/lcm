/*
 * ObjectRecorder, a class that takes records of an objects state using 
 * reflection.
 * Copyright (c) 2004 - 2011  Achim Westermann, Achim.Westermann@gmx.de.
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
 */
package info.monitorenter.reflection;

import info.monitorenter.util.TimeStampedValue;
import info.monitorenter.util.collections.IRingBuffer;
import info.monitorenter.util.collections.RingBufferArrayFast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

import javax.naming.directory.NoSuchAttributeException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * The <code>ObjectRecorder</code> takes records(inspections) of an objects
 * state using reflection and accessibility- framework.
 * <p>
 * 
 * It's strategy is to: <br/>
 * 
 * <pre>
 *  - try to set any field accessible.
 *  - try to get the value of the field.
 *  - if not succeed: try to invoke a bean- conform getter.
 *  - if NoSuchMethod, it's useless (no implementation of MagicClazz here).
 * </pre>
 * 
 * <p>
 * 
 * Furthermore the <code>ObjectRecorder</code> has a history - size (buffer) and
 * an adjustable distance between each inspection.
 * <p>
 * 
 * @author <a href='mailto:Achim.Westermann@gmx.de'>Achim Westermann </a>
 * 
 * @version $Revision: 1.10 $
 */
public class ObjectRecorder extends Thread {

  /**
   * Data container for the inspection of the internal intance.
   * <p>
   * 
   * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
   * 
   * 
   * @version $Revision: 1.10 $
   */
  public final class ObjectInspection {

    /** Time stamp of the inspection. */
    protected long m_time;

    /** The values taken on the inspection. */
    private final LinkedList<Object> m_values;

    /**
     * Creates an instance linked to the outer recorder.
     * <p>
     * 
     */
    protected ObjectInspection() {
      this.m_time = new java.util.Date().getTime();
      this.m_values = new LinkedList<Object>();
    }

    /**
     * Adds an inspected value to this inspection.
     * <p>
     * 
     * @param value
     *          an inspected value of this inspection.
     */
    protected void add(final Object value) {
      this.m_values.add(value);
    }

    /**
     * Get the value for the attribute at the given index.
     * <p>
     * 
     * @param index
     *          the index of the inspected value according to the order it was
     *          found on the instance by {@link Class#getDeclaredFields()}.
     *          <p>
     * 
     * @return the value for the attribute at the given index.
     */
    public Object get(final int index) {
      return this.m_values.get(index);
    }

    /**
     * Returns the time stamp in ms of this inspection.
     * <p>
     * 
     * @return the time stamp in ms of this inspection.
     */
    public long getTime() {
      return this.m_time;
    }

    /**
     * Removes the inspected value from this inspection.
     * <p>
     * 
     * The value is identified by means of
     * {@link Object#equals(java.lang.Object)}.
     * <p>
     * 
     * @param value
     *          the inspected value from this inspection.
     */
    protected void remove(final Object value) {
      this.m_values.remove(value);
    }

    /**
     * Returns a pretty print of this inspection.
     * <p>
     * 
     * @return a pretty print of this inspection.
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      final StringBuffer ret = new StringBuffer("\nObjectInspection:\n");
      ret.append("-----------------\n");
      ret.append("Inspected: ").append(ObjectRecorder.this.getInspected().toString()).append("\n");
      ret.append("time:      ").append(this.m_time).append("\n");
      for (int i = ObjectRecorder.this.m_fields.length - 1; i >= 0; i--) {
        ret.append(ObjectRecorder.this.m_fields[i].getName()).append(": ").append(
            this.m_values.get(i).toString()).append("\n");
      }
      return ret.toString();
    }
  }

  /** Verbosity constant. */
  protected static final boolean VERBOSE = false;

  /** Fast buffer to store recorded fiels. */
  protected IRingBuffer<ObjectRecorder.ObjectInspection> m_buffer = new RingBufferArrayFast<ObjectRecorder.ObjectInspection>(
      100);

  /** The listeners on this recorder. */
  protected EventListenerList m_changeListeners = new EventListenerList();

  /** The fields to inspect on the instance. */
  protected Field[] m_fields;

  /**
   * The time - interval between to inspections of the Object.
   */
  protected long m_interval;

  /** The instance to inspect. */
  protected Object m_toinspect;

  /**
   * Creates an instance that will inspect the given Object in the given time
   * interval.
   * <p>
   * 
   * @param toinspect
   *          the instance to inspect.
   * 
   * @param interval
   *          the interval of inspection in ms.
   */
  public ObjectRecorder(final Object toinspect, final long interval) {
    this.m_interval = interval;
    this.m_toinspect = toinspect;
    this.setDaemon(true);
    // getting the field names.
    this.m_fields = toinspect.getClass().getDeclaredFields();
    this.start();
  }

  /**
   * Adds a change listener that will be informed about new recordings of the
   * inspected instances.
   * <p>
   * 
   * @param x
   *          the change listener that will be informed about new recordings of
   *          the inspected instances.
   */
  public void addChangeListener(final ChangeListener x) {
    this.m_changeListeners.add(ChangeListener.class, x);
    // x.stateChanged(new ChangeEvent(this)); // Aufruf des neuen
    // ChangeListeners um zu aktualisieren.
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
    final ObjectRecorder other = (ObjectRecorder) obj;
    if (this.m_buffer == null) {
      if (other.m_buffer != null) {
        return false;
      }
    } else if (!this.m_buffer.equals(other.m_buffer)) {
      return false;
    }
    if (this.m_changeListeners == null) {
      if (other.m_changeListeners != null) {
        return false;
      }
    } else if (!this.m_changeListeners.equals(other.m_changeListeners)) {
      return false;
    }
    if (!Arrays.equals(this.m_fields, other.m_fields)) {
      return false;
    }
    if (this.m_interval != other.m_interval) {
      return false;
    }
    if (this.m_toinspect == null) {
      if (other.m_toinspect != null) {
        return false;
      }
    } else if (!this.m_toinspect.equals(other.m_toinspect)) {
      return false;
    }
    return true;
  }

  /**
   * Informs the listeners about a change of this instance.
   * <p>
   * 
   */
  protected void fireChange() {
    final ChangeEvent ce = new ChangeEvent(this);
    final Object[] listeners = this.m_changeListeners.getListenerList();
    for (int i = listeners.length - 1; i >= 0; i -= 2) {
      final ChangeListener cl = (ChangeListener) listeners[i];
      cl.stateChanged(ce);
    }
  }

  /**
   * The History returned by this Method represents the past of the field
   * specified by attributeName. It starts from low index with the newest values
   * taken from the inspected Object and ends with the oldest.
   * 
   * @param attributeName
   *          field name of the internal instance to inspect.
   * 
   * @return An array filled with TimeStampedValues that represent the past of
   *         the last inspections of the field with attributeName.
   * 
   * @throws NoSuchAttributeException
   *           if the attribute / field described by the given argument does not
   *           exist on the internal Object to instpect.
   * 
   * @see ObjectRecorder#getInspected()
   */
  public TimeStampedValue[] getAttributeHistory(final String attributeName)
      throws NoSuchAttributeException {
    // search for the field
    int attribindex = -1;
    for (int i = this.m_fields.length - 1; i >= 0; i--) {
      if (this.m_fields[i].getName().equals(attributeName)) {
        attribindex = i;
        break;
      }
    }
    if (attribindex == -1) {
      throw new NoSuchAttributeException("The Attribute with the name: " + attributeName
          + " does not exist in " + this.m_toinspect.getClass().getName());
    }
    final int stop = this.m_buffer.size();
    final TimeStampedValue[] ret = new TimeStampedValue[stop];
    synchronized (this.m_buffer) {
      for (final ObjectInspection tmp : this.m_buffer) {
        int i = 0;
        ret[i++] = new TimeStampedValue(tmp.getTime(), tmp.get(attribindex));
      }
    }
    return ret;
  }

  /**
   * Returns the names of the fields to inspect.
   * <p>
   * 
   * @return the names of the fields to inspect.
   */
  public String[] getAttributeNames() {
    final String[] ret = new String[this.m_fields.length];
    for (int i = 0; i < this.m_fields.length; i++) {
      ret[i] = this.m_fields[i].getName();
    }
    return ret;

  }

  /**
   * Returns the inspected instance.
   * <p>
   * 
   * @return the inspected instance.
   */
  public Object getInspected() {
    return this.m_toinspect;
  }

  /**
   * Returns the last recorded value taken from the given field along with the
   * time stamp identifying the time this value was recored.
   * <p>
   * 
   * @param fieldname
   *          the field whose value was recorded.
   * 
   * @return the last recorded value taken from the given field along with the
   *         time stamp identifying the time this value was recored.
   * 
   * @throws NoSuchAttributeException
   *           if no such field exists on the Object to inspect.
   * 
   */
  public TimeStampedValue getLastValue(final String fieldname) throws NoSuchAttributeException {
    // search for the field
    int attribindex = -1;
    for (int i = this.m_fields.length - 1; i >= 0; i--) {
      if (this.m_fields[i].getName().equals(fieldname)) {
        attribindex = i;
        break;
      }
    }
    if (attribindex == -1) {
      throw new NoSuchAttributeException("The Attribute with the name: " + fieldname
          + " does not exist in " + this.m_toinspect.getClass().getName());
    }
    final ObjectInspection tmp = this.m_buffer.getYoungest();
    return new TimeStampedValue(tmp.getTime(), tmp.get(attribindex));
  }

  /**
   * Returns the internal fifo buffer that stores the
   * {@link ObjectRecorder.ObjectInspection} instances that have been done.
   * <p>
   * 
   * @return the internal fifo buffer that stores the
   *         {@link ObjectRecorder.ObjectInspection} instances that have been
   *         done.
   */
  public IRingBuffer<ObjectRecorder.ObjectInspection> getRingBuffer() {
    return this.m_buffer;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.m_buffer == null) ? 0 : this.m_buffer.hashCode());
    result = prime * result
        + ((this.m_changeListeners == null) ? 0 : this.m_changeListeners.hashCode());
    result = prime * result + Arrays.hashCode(this.m_fields);
    result = prime * result + (int) (this.m_interval ^ (this.m_interval >>> 32));
    result = prime * result + ((this.m_toinspect == null) ? 0 : this.m_toinspect.hashCode());
    return result;
  }

  /**
   * Makes a record of the state of the object specified in the constructor. The
   * new record is stored in a RingBuffer and contains all retrieveable values
   * of the Object specified in the constructor. Reflection is used to get the
   * values. If a field is private it's value is tried to be taken from the
   * Object by invoking a getter - method conform with the bean - specification:
   * The name of the method has to be "get" followed by the name of the field
   * with first letter upper case.
   */
  public void inspect() {
    final ObjectInspection newentry = new ObjectInspection();
    for (final Field mField : this.m_fields) {
      if (ObjectRecorder.VERBOSE) {
        System.out.println(this.getClass().getName() + " inpspecting " + mField.getName() + " of "
            + this.m_toinspect.getClass().getName() + ".");
      }
      try {
        mField.setAccessible(true);
        newentry.add(mField.get(this.m_toinspect));
      } catch (final IllegalAccessException e) {
        if (ObjectRecorder.VERBOSE) {
          System.err.println(this.getClass().getName() + ".inspect(): No public access to "
              + mField.getName() + " of " + this.m_toinspect.getClass().getName());
        }
        // Try to invoke bean- conform getter method.
        String fieldname = mField.getName();
        final char[] fieldnm = fieldname.toCharArray();
        fieldnm[0] = Character.toUpperCase(fieldnm[0]);
        fieldname = new String(fieldnm);
        final String methodname = new StringBuffer("get").append(fieldname).toString();
        // name of method constructed. Now invoke it.
        try {
          final Method toinvoke = this.m_toinspect.getClass().getDeclaredMethod(methodname,
              new Class[] {});
          newentry.add(toinvoke.invoke(this.m_toinspect, new Object[] {}));

        } catch (final NoSuchMethodException f) {
          if (ObjectRecorder.VERBOSE) {
            System.err.println(this.getClass().getName() + ".inspect(): Failure at getting field "
                + mField.getName() + " by trying to invoke a method: " + methodname);
          }
        } catch (final SecurityException g) {
          g.printStackTrace();
        } catch (final IllegalAccessException h) {
          h.printStackTrace();
        } catch (final InvocationTargetException l) {
          l.printStackTrace();
        }
      }
    }
    this.m_buffer.add(newentry);
    this.fireChange();
  }

  /**
   * Removes the given listener for changes of the inpsected instance.
   * <p>
   * 
   * @param x
   *          the listener to remove.
   */
  public void removeChangeListener(final ChangeListener x) {
    this.m_changeListeners.remove(ChangeListener.class, x);
  }

  /**
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(this.m_interval);
      } catch (final InterruptedException e) {
        // nop
      }
      this.inspect();
    }
  }

  /**
   * Define the amount of recorded states of the Object to inspect that remain
   * in memory.
   * <p>
   * 
   * Default value is 100.
   * <p>
   * 
   * @param length
   *          the amount of recorded states of the Object to inspect that remain
   *          in memory.
   */
  public void setHistoryLength(final int length) {
    this.m_buffer.setBufferSize(length);
  }

  /**
   * Sets the interval for inpection of the instance to inspect in ms.
   * <p>
   * 
   * @param sleeptime
   *          the interval for inpection of the instance to inspect in ms.
   * 
   * @see ObjectRecorder#ObjectRecorder(Object, long)
   */
  public void setInterval(final long sleeptime) {
    this.m_interval = sleeptime;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.m_buffer.toString();
  }
}
