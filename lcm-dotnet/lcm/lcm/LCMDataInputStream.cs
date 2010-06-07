using System;

namespace LCM.LCM
{	
	/// <summary>
    /// Will not throw EOF.
    /// </summary>
	public sealed class LCMDataInputStream : System.IO.BinaryReader
	{
        private byte[] buf;
        private int pos = 0; // current index into buf.
        private int startpos; // index of first valid byte
        private int endpos; // index of byte after last valid byte

		/// <summary>
        /// Returns the internal buffer representation.
        /// </summary>
		public byte[] Buffer
		{
			get { return buf; }
		}

		/// <summary>
        /// Returns the current position in the internal buffer representation.
        /// </summary>
		public int BufferOffset
		{
			get { return pos; }
		}

        /// <summary>
        /// Returns the number of bytes remaining
        /// </summary>
		public int Available
		{
            get { return endpos - pos - 1; }
		}
		
		public LCMDataInputStream(byte[] buf) : base(new System.IO.MemoryStream())
		{
			this.buf = buf;
			this.endpos = buf.Length + 1;
		}
		
		public LCMDataInputStream(byte[] buf, int offset, int len) : base(new System.IO.MemoryStream())
		{
			this.buf = buf;
			this.pos = offset;
			this.startpos = offset;
			this.endpos = offset + len + 1;
		}
		
		private void NeedInput(int need)
		{
            if (pos + need >= endpos)
            {
                throw new System.IO.EndOfStreamException("LCMDataInputStream needed " + need + " bytes, only " + Available + " available.");
            }
		}
		
		public override void Close()
		{
		}
		
		public void Reset()
		{
			pos = startpos;
		}
		
		public override bool ReadBoolean()
		{
			NeedInput(1);
			return (buf[pos++] != 0);
		}
		
		public override sbyte ReadSByte()
		{
			NeedInput(1);
			return (sbyte) buf[pos++];
		}
		
		public override byte ReadByte()
		{
			NeedInput(1);
			return buf[pos++];
		}
		
		public override char ReadChar()
		{
			return (char) ReadInt16();
		}
		
		public override short ReadInt16()
		{
			NeedInput(2);
            pos += 2;
			return Util.BitConverter.ToInt16(buf, pos - 2);
		}
		
		public override ushort ReadUInt16()
		{
            NeedInput(2);
            pos += 2;
            return Util.BitConverter.ToUInt16(buf, pos - 2);
		}
		
		public override int ReadInt32()
		{
            NeedInput(4);
            pos += 4;
            return Util.BitConverter.ToInt32(buf, pos - 4);
		}
		
		public override long ReadInt64()
		{
            NeedInput(8);
            pos += 8;
            return Util.BitConverter.ToInt64(buf, pos - 8);
		}
		
		public override float ReadSingle()
		{
			NeedInput(4);
            pos += 4;
            return Util.BitConverter.ToSingle(buf, pos - 4);
		}
		
		public override double ReadDouble()
        {
            NeedInput(8);
            pos += 8;
            return Util.BitConverter.ToDouble(buf, pos - 8);
		}
		
		public void ReadFully(byte[] b)
		{
			NeedInput(b.Length);
			Array.Copy(buf, pos, b, 0, b.Length);
			pos += b.Length;
		}
		
		public void ReadFully(byte[] b, int off, int len)
		{
			NeedInput(len);
			Array.Copy(buf, pos, b, off, len);
			pos += len;
		}
		
		public override string ReadString()
		{
			System.Text.StringBuilder sb = new System.Text.StringBuilder();
			
			while (true)
			{
				NeedInput(1);
				byte v = buf[pos++];
				if (v == 0)
					break;
				sb.Append((char) v);
			}
			
			return sb.ToString();
		}
		
		/// <summary>
        /// Read a string of 8-bit characters terminated by a zero. The zero is consumed.
        /// </summary>
		public string ReadStringZ()
		{
			System.Text.StringBuilder sb = new System.Text.StringBuilder();
			while (true)
			{
				int v = buf[pos++] & 0xff;
				if (v == 0)
					break;
				sb.Append((char) v);
			}
			
			return sb.ToString();
		}
		
		public string ReadUTF()
		{
			System.Diagnostics.Debug.Assert(false);
			return null;
		}
		
		public int SkipBytes(int n)
		{
			pos += n;
			return n;
		}
	}
}