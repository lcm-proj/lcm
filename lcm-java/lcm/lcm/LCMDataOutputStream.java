package lcm.lcm;

import java.io.*;

public final class LCMDataOutputStream implements DataOutput
{
    byte buf[];
    int pos;

    public LCMDataOutputStream()
    {
        this(512);
    }

    public LCMDataOutputStream(int sz)
    {
        this.buf = new byte[sz];
    }

    public LCMDataOutputStream(byte buf[])
    {
        this.buf = buf;
    }

    public void reset()
    {
        pos = 0;
    }

    void ensureSpace(int needed)
    {
        if (pos+needed >= buf.length) {
            // compute new power-of-two capacity
            int newlen = buf.length;
            while (newlen < pos+needed)
                newlen *= 2;

            byte buf2[] = new byte[newlen];
            System.arraycopy(buf, 0, buf2, 0, pos);
            buf = buf2;
        }
    }

    public void write(byte b[])
    {
        ensureSpace(b.length);
        System.arraycopy(b, 0, buf, pos, b.length);
        pos += b.length;
    }

    public void write(byte b[], int off, int len)
    {
        ensureSpace(len);
        System.arraycopy(b, off, buf, pos, len);
        pos += len;
    }

    /** Writes one byte per char **/
    public void writeCharsAsBytes(char c[])
    {
        ensureSpace(c.length);
        for (int i = 0; i < c.length; i++)
            write(c[i]);
    }

    public void write(int b)
    {
        ensureSpace(1);
        buf[pos++] = (byte) b;
    }

    public void writeBoolean(boolean v)
    {
        ensureSpace(1);
        buf[pos++] = v ? (byte) 1 : (byte) 0;
    }

    public void writeByte(int v)
    {
        ensureSpace(1);
        buf[pos++] = (byte) v;
    }

    public void writeBytes(String s)
    {
        ensureSpace(s.length());
        for (int i = 0; i < s.length(); i++) {
            buf[pos++] = (byte) s.charAt(i);
        }
    }

    public void writeChar(int v)
    {
        writeShort(v);
    }

    public void writeChars(String s)
    {
        ensureSpace(2*s.length());
        for (int i = 0; i < s.length(); i++) {
            int v = s.charAt(i);
            buf[pos++] = (byte) (v>>>8);
            buf[pos++] = (byte) (v>>>0);
        }
    }

    /** Write a zero-terminated string consisting of 8 bit characters. **/
    public void writeStringZ(String s)
    {
        ensureSpace(s.length()+1);
        for (int i = 0; i < s.length(); i++) {
            buf[pos++] = (byte) s.charAt(i);
        }
        buf[pos++] = 0;
    }

    public void writeDouble(double v)
    {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeFloat(float v)
    {
        writeInt(Float.floatToIntBits(v));
    }

    public void writeInt(int v)
    {
        ensureSpace(4);
        buf[pos++] = (byte) (v>>>24);
        buf[pos++] = (byte) (v>>>16);
        buf[pos++] = (byte) (v>>>8);
        buf[pos++] = (byte) (v>>>0);
    }

    public void writeLong(long v)
    {
        ensureSpace(8);
        buf[pos++] = (byte) (v>>>56);
        buf[pos++] = (byte) (v>>>48);
        buf[pos++] = (byte) (v>>>40);
        buf[pos++] = (byte) (v>>>32);
        buf[pos++] = (byte) (v>>>24);
        buf[pos++] = (byte) (v>>>16);
        buf[pos++] = (byte) (v>>>8);
        buf[pos++] = (byte) (v>>>0);
    }

    public void writeShort(int v)
    {
        ensureSpace(2);
        buf[pos++] = (byte) (v>>>8);
        buf[pos++] = (byte) (v>>>0);
    }

    public void writeUTF(String s)
    {
        assert(false);
    }

    /** Makes a copy of the internal buffer. **/
    public byte[] toByteArray()
    {
        byte b[] = new byte[pos];
        System.arraycopy(buf, 0, b, 0, pos);
        return b;
    }

    /** Returns the internal buffer, which may be longer than the
     * buffer that has been written to so far.
     **/
    public byte[] getBuffer()
    {
        return buf;
    }

    /** Get the number of bytes that have been written to the buffer. **/
    public int size()
    {
        return pos;
    }
}
