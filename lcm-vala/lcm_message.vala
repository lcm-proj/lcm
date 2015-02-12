/**
 * Lightweight Communications and Marshalling.
 */

namespace Lcm {
    public errordomain MessageError {
        WRONG_HASH,
        OVERFLOW
    }

    /**
     * Common Lcm message interface
     */
    public interface IMessage {
        public abstract void decode(void[] data) throws MessageError;
        public abstract void[] encode() throws MessageError;

        public abstract size_t _decode_no_hash(void[] data, Posix.off_t offset) throws MessageError;
        public abstract size_t _encode_no_hash(void[] data, Posix.off_t offset) throws MessageError;
        public abstract size_t _encoded_size_no_hash { get; }

        public abstract int64 hash { get; }
    }

    /**
     * Encode and decode helpers for core types.
     */
    namespace CoreTypes {
        // intptr defined in VAPI

        // size constants for size calculation routines.
        const size_t bool_SIZE      = sizeof(int8);
        const size_t int8_SIZE      = sizeof(int8);
        const size_t int16_SIZE     = sizeof(int16);
        const size_t int32_SIZE     = sizeof(int32);
        const size_t int64_SIZE     = sizeof(int64);
        const size_t float_SIZE     = sizeof(float);
        const size_t double_SIZE    = sizeof(double);

        // -*- encoder helpers -*-

        size_t bool_encode_array(void[] data, Posix.off_t offset, bool *val, size_t elements) throws MessageError {
            // gboolean size == gint, convert to byte
            var i8_val = new int8[elements];
            for (size_t idx = 0; idx < elements; idx++)
                i8_val[idx] = val[idx]? 1 : 0;

            return int8_encode_array(data, offset, i8_val, elements);
        }

        size_t int8_encode_array(void[] data, Posix.off_t offset, int8 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < elements)
                throw new MessageError.OVERFLOW("encode");

            Memory.copy(&buf[offset], val, elements);
            return elements;
        }

        size_t int16_encode_array(void[] data, Posix.off_t offset, int16 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < sizeof(int16) * elements)
                throw new MessageError.OVERFLOW("encode");

            for (size_t idx = 0; idx < elements; idx++) {
                int16 be16 = val[idx].to_big_endian();
                Memory.copy(&buf[offset + sizeof(int16) * idx], &be16, sizeof(int16));
            }

            return sizeof(int16) * elements;
        }

        size_t int32_encode_array(void[] data, Posix.off_t offset, int32 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < sizeof(int32) * elements)
                throw new MessageError.OVERFLOW("encode");

            for (size_t idx = 0; idx < elements; idx++) {
                int32 be32 = val[idx].to_big_endian();
                Memory.copy(&buf[offset + sizeof(int32) * idx], &be32, sizeof(int32));
            }

            return sizeof(int32) * elements;
        }

        size_t int64_encode_array(void[] data, Posix.off_t offset, int64 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < sizeof(int64) * elements)
                throw new MessageError.OVERFLOW("encode");

            for (size_t idx = 0; idx < elements; idx++) {
                int64 be64 = val[idx].to_big_endian();
                Memory.copy(&buf[offset + sizeof(int64) * idx], &be64, sizeof(int64));
            }

            return sizeof(int64) * elements;
        }

        size_t float_encode_array(void[] data, Posix.off_t offset, float *val, size_t elements) throws MessageError {
            return int32_encode_array(data, offset, (int32 *) val, elements);
        }

        size_t double_encode_array(void[] data, Posix.off_t offset, double *val, size_t elements) throws MessageError {
            return int64_encode_array(data, offset, (int64 *) val, elements);
        }

        // Note: For _array generated very inefficient code.
        // So string encoding done pre-object.
        size_t string_encode(void[] data, Posix.off_t offset, string val) throws MessageError {
            Posix.off_t pos = 0;

            // string may be null, fallback to empty string
            // unowned prevents strdup()
            unowned string str = val ?? "";
            unowned uint8[] str_data = str.data;
            // length is a strlen() result, we adds '\0'
            int32 length = str_data.length + 1;

            pos += int32_encode_array(data, offset + pos, &length, 1);
            pos += int8_encode_array(data, offset + pos, str_data, length);

            return pos;
        }

        // -*- decode helpers -*-

        size_t bool_decode_array(void[] data, Posix.off_t offset, bool *val, size_t elements) throws MessageError {
            // gboolean size == gint, convert from byte
            var i8_val = new int8[elements];
            var ret = int8_decode_array(data, offset, i8_val, elements);

            for (size_t idx = 0; idx < elements; idx++)
                val[idx] = (i8_val[idx] > 0)? true : false;

            return ret;
        }

        size_t int8_decode_array(void[] data, Posix.off_t offset, int8 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < elements)
                throw new MessageError.OVERFLOW("decode");

            Memory.copy(val, &buf[offset], elements);
            return elements;
        }

        size_t int16_decode_array(void[] data, Posix.off_t offset, int16 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < sizeof(int16) * elements)
                throw new MessageError.OVERFLOW("decode");

            for (size_t idx = 0; idx < elements; idx++) {
                int16 be16 = 0;
                Memory.copy(&be16, &buf[offset + sizeof(int16) * idx], sizeof(int16));
                val[idx] = int16.from_big_endian(be16);
            }

            return sizeof(int16) * elements;
        }

        size_t int32_decode_array(void[] data, Posix.off_t offset, int32 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < sizeof(int32) * elements)
                throw new MessageError.OVERFLOW("decode");

            for (size_t idx = 0; idx < elements; idx++) {
                int32 be32 = 0;
                Memory.copy(&be32, &buf[offset + sizeof(int32) * idx], sizeof(int32));
                val[idx] = int32.from_big_endian(be32);
            }

            return sizeof(int32) * elements;
        }

        size_t int64_decode_array(void[] data, Posix.off_t offset, int64 *val, size_t elements) throws MessageError {
            unowned int8[] buf = (int8[]) data;
            if (buf.length - offset < sizeof(int64) * elements)
                throw new MessageError.OVERFLOW("decode");

            for (size_t idx = 0; idx < elements; idx++) {
                int64 be64 = 0;
                Memory.copy(&be64, &buf[offset + sizeof(int64) * idx], sizeof(int64));
                val[idx] = int64.from_big_endian(be64);
            }

            return sizeof(int64) * elements;
        }

        size_t float_decode_array(void[] data, Posix.off_t offset, float *val, size_t elements) throws MessageError {
            return int32_decode_array(data, offset, (int32 *) val, elements);
        }

        size_t double_decode_array(void[] data, Posix.off_t offset, double *val, size_t elements) throws MessageError {
            return int64_decode_array(data, offset, (int64 *) val, elements);
        }

        // XXX: check that it actualy can work
        // Note: For array generated very inefficient code.
        size_t string_decode(void[] data, Posix.off_t offset, out string val) throws MessageError {
            Posix.off_t pos = 0;

            int32 length = 0;

            pos += int32_decode_array(data, offset + pos, &length, 1);

            // skip null terminator
            var s = string.nfill((length > 0)? length - 1 : 0, '\0');
            pos += int8_decode_array(data, offset + pos, s.data, length);

            val = s;
            return pos;
        }
    }
}
