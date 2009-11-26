using System;
using System.Collections.Generic;
using System.Text;

namespace LCM.Util
{
    /// <summary>
    ///     Converts base data types to an array of bytes, and an array of bytes to base
    ///     data types.
    ///     All info taken from the meta data of System.BitConverter. This implementation
    ///     allows for Endianness consideration.
    ///     
    ///     based on http://snipplr.com/view.php?codeview&id=15179, rewritten
    ///</summary>
    public static class BitConverter
    {
        /// <summary>
        ///     Indicates the byte order ("endianess") in which data is stored in this computer
        ///     architecture.
        ///</summary>
        public static bool IsLittleEndian = false; // in our case, the endiannes is fixed

        ///
        /// <summary>
        ///     Returns the specified double-precision floating point value as an array of
        ///     bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 8.
        ///</summary>
        public static byte[] GetBytes(double value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified single-precision floating point value as an array of
        ///     bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 4.
        ///</summary>
        public static byte[] GetBytes(float value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified 32-bit signed integer value as an array of bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 4.
        ///</summary>
        public static byte[] GetBytes(int value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified 64-bit signed integer value as an array of bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 8.
        ///</summary>
        public static byte[] GetBytes(long value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified 16-bit signed integer value as an array of bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 2.
        ///</summary>
        public static byte[] GetBytes(short value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified 32-bit unsigned integer value as an array of bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 4.
        ///</summary>
        public static byte[] GetBytes(uint value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified 64-bit unsigned integer value as an array of bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 8.
        ///</summary>
        public static byte[] GetBytes(ulong value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns the specified 16-bit unsigned integer value as an array of bytes.
        ///
        /// Parameters:
        ///   value:
        ///     The number to convert.
        ///
        /// Returns:
        ///     An array of bytes with length 2.
        ///</summary>
        public static byte[] GetBytes(ushort value)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.GetBytes(value);
            }
            else
            {
                byte[] result = System.BitConverter.GetBytes(value);
                Array.Reverse(result);
                return result;
            }
        }
        ///
        /// <summary>
        ///     Returns a double-precision floating point number converted from eight bytes
        ///     at a specified position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A double precision floating point number formed by eight bytes beginning
        ///     at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex is greater than or equal to the length of value minus 7, and is
        ///     less than or equal to the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static double ToDouble(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToDouble(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(Double)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(Double));
                Array.Reverse(tmp);
                return System.BitConverter.ToDouble(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a 16-bit signed integer converted from two bytes at a specified position
        ///     in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A 16-bit signed integer formed by two bytes beginning at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex equals the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static short ToInt16(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToInt16(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(Int16)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(Int16));
                Array.Reverse(tmp);
                return System.BitConverter.ToInt16(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a 32-bit signed integer converted from four bytes at a specified
        ///     position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A 32-bit signed integer formed by four bytes beginning at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex is greater than or equal to the length of value minus 3, and is
        ///     less than or equal to the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static int ToInt32(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToInt32(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(Int32)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(Int32));
                Array.Reverse(tmp);
                return System.BitConverter.ToInt32(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a 64-bit signed integer converted from eight bytes at a specified
        ///     position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A 64-bit signed integer formed by eight bytes beginning at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex is greater than or equal to the length of value minus 7, and is
        ///     less than or equal to the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static long ToInt64(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToInt64(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(Int64)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(Int64));
                Array.Reverse(tmp);
                return System.BitConverter.ToInt64(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a single-precision floating point number converted from four bytes
        ///     at a specified position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A single-precision floating point number formed by four bytes beginning at
        ///     startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex is greater than or equal to the length of value minus 3, and is
        ///     less than or equal to the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static float ToSingle(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToSingle(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(Single)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(Single));
                Array.Reverse(tmp);
                return System.BitConverter.ToSingle(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a 16-bit unsigned integer converted from two bytes at a specified
        ///     position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     The array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A 16-bit unsigned integer formed by two bytes beginning at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex equals the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static ushort ToUInt16(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToUInt16(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(UInt16)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(UInt16));
                Array.Reverse(tmp);
                return System.BitConverter.ToUInt16(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a 32-bit unsigned integer converted from four bytes at a specified
        ///     position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A 32-bit unsigned integer formed by four bytes beginning at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex is greater than or equal to the length of value minus 3, and is
        ///     less than or equal to the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static uint ToUInt32(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToUInt32(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(UInt32)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(UInt32));
                Array.Reverse(tmp);
                return System.BitConverter.ToUInt32(tmp, 0);
            }
        }
        ///
        /// <summary>
        ///     Returns a 64-bit unsigned integer converted from eight bytes at a specified
        ///     position in a byte array.
        ///
        /// Parameters:
        ///   value:
        ///     An array of bytes.
        ///
        ///   startIndex:
        ///     The starting position within value.
        ///
        /// Returns:
        ///     A 64-bit unsigned integer formed by the eight bytes beginning at startIndex.
        ///
        /// Exceptions:
        ///   System.ArgumentException:
        ///     startIndex is greater than or equal to the length of value minus 7, and is
        ///     less than or equal to the length of value minus 1.
        ///
        ///   System.ArgumentNullException:
        ///     value is null.
        ///
        ///   System.ArgumentOutOfRangeException:
        ///     startIndex is less than zero or greater than the length of value minus 1.
        ///</summary>
        public static ulong ToUInt64(byte[] value, int startIndex)
        {
            if (IsLittleEndian)
            {
                return System.BitConverter.ToUInt64(value, startIndex);
            }
            else
            {
                byte[] tmp = new byte[sizeof(UInt64)];
                Array.Copy(value, startIndex, tmp, 0, sizeof(UInt64));
                Array.Reverse(tmp);
                return System.BitConverter.ToUInt64(tmp, 0);
            }
        }
    }
}