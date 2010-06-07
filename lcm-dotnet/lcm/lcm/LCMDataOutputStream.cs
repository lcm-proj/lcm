using System;

namespace LCM.LCM
{
	public sealed class LCMDataOutputStream : System.IO.BinaryWriter
	{
        private byte[] buf;
        private int pos;

		/// <summary>
        /// Returns the internal buffer, which may be longer than the
		/// buffer that has been written to so far.
        /// </summary>
		public byte[] Buffer
		{
			get { return buf; }
		}

        /// <summary>
        /// Get the number of bytes that have been written to the buffer.
        /// </summary>
        public int Length
        {
            get { return pos; }
        }
		
		public LCMDataOutputStream() : this(512)
		{
		}
		
		public LCMDataOutputStream(int sz)
		{
			this.buf = new byte[sz];
		}
		
		public LCMDataOutputStream(byte[] buf)
		{
			this.buf = buf;
		}
		
		public void Reset()
		{
			pos = 0;
		}

        private void EnsureSpace(int needed)
		{
			if (pos + needed >= buf.Length)
			{
                // compute new power-of-two capacity
                int newlen = buf.Length;
                while (newlen < pos + needed)
                    newlen *= 2;

				byte[] buf2 = new byte[newlen];
				Array.Copy(buf, 0, buf2, 0, pos);
				buf = buf2;
			}
		}
		
		public override void Write(byte[] b)
		{
			EnsureSpace(b.Length);
			Array.Copy(b, 0, buf, pos, b.Length);
			pos += b.Length;
		}
		
		public override void Write(byte[] b, int off, int len)
		{
			EnsureSpace(len);
			Array.Copy(b, off, buf, pos, len);
			pos += len;
		}
		
		public override void Write(byte b)
		{
			EnsureSpace(1);
			buf[pos++] = (byte) b;
		}
		
		public override void Write(bool v)
		{
			EnsureSpace(1);
			buf[pos++] = v ? (byte) 1 : (byte) 0;
		}
		
		public override void Write(sbyte v)
		{
			EnsureSpace(1);
			buf[pos++] = (byte) v;
		}
		
		public void WriteBytes(string s)
		{
			EnsureSpace(s.Length);
			for (int i = 0; i < s.Length; i++)
			{
				buf[pos++] = (byte) s[i];
			}
		}
		
		public override void Write(char v)
		{
			Write((short) v);
		}
		
		public void WriteChars(string s)
		{
			EnsureSpace(2 * s.Length);
			for (int i = 0; i < s.Length; i++)
			{
				int v = s[i];
                buf[pos++] = (byte) (v >> 8);
				buf[pos++] = (byte) (v);
			}
		}
		
		/// <summary>
        /// Write a zero-terminated string consisting of 8 bit characters.
        /// </summary>
		public void WriteStringZ(string s)
		{
			EnsureSpace(s.Length + 1);
			for (int i = 0; i < s.Length; i++)
			{
				buf[pos++] = (byte) s[i];
			}
			buf[pos++] = 0;
		}
		
		public override void Write(double v)
		{
			Write(Util.BitConverter.GetBytes(v));
		}
		
		public override void Write(float v)
        {
            Write(Util.BitConverter.GetBytes(v));
		}
		
		public override void Write(int v)
		{
            EnsureSpace(4);
            Write(Util.BitConverter.GetBytes(v));
		}
		
		public override void Write(long v)
		{
            EnsureSpace(8);
            Write(Util.BitConverter.GetBytes(v));
		}
		
		public override void Write(short v)
		{
            EnsureSpace(2);
            Write(Util.BitConverter.GetBytes(v));
		}
		
		public void WriteUTF(string s)
		{
            System.Diagnostics.Debug.Assert(false);
		}
		
		/// <summary>
        /// Makes a copy of the internal buffer.
        /// </summary>
		public byte[] ToByteArray()
		{
			byte[] b = new byte[pos];
			Array.Copy(buf, 0, b, 0, pos);
			return b;
		}
	}
}