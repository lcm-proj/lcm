/**
 * Lightweight Communications and Marshalling.
 */

namespace Lcm {
    public errordomain MessageError {
        DECODE,
        OVERFLOW
    }

    /**
     * Common Lcm message interface
     */
    public interface IMessage {
        public abstract void decode(void[] data) throws MessageError;
        public abstract void[] encode() throws MessageError;

        public abstract ssize_t _decode_no_hash(void[] data, Posix.off_t offset) throws MessageError;
        public abstract ssize_t _encode_no_hash(void[] data, Posix.off_t offset) throws MessageError;
        public abstract size_t _encoded_size_no_hash { get; }

        public abstract int64 hash { get; }
    }

    /**
     * Encode and decode helpers for core types.
     */
    namespace CoreTypes {
        // intptr defined in VAPI

        ssize_t bool_encode_array(void[] buf, Posix.off_t offset, bool *val, size_t elements) throws MessageError {
            assert(sizeof(bool) == 1);  // todo: check actual bool size in vala
            return int8_encode_array(buf, offset, (int8 *) val, elements);
        }

        ssize_t int8_encode_array(void[] buf, Posix.off_t offset, int8 *val, size_t elements) throws MessageError {
            if (buf.length - offset < elements)
                throw new MessageError.OVERFLOW();

            Memory.copy(&buf[offset], val, elements);
            return elements;
        }

        ssize_t int16_encode_array(void[] buf, Posix.off_t offset, int16 *val, size_t elements) throws MessageError {
            if (buf.length - offset < sizeof(int16) * elements)
                throw new MessageError.OVERFLOW();

            for (size_t idx = 0; idx < elements; idx++) {
                int16 be16 = val[idx].to_big_endian();
                Memory.copy(&buf[offset + sizeof(int16) * idx], &be16, sizeof(int16));
            }

            return sizeof(int16) * elements;
        }

        ssize_t int32_encode_array(void[] buf, Posix.off_t offset, int32 *val, size_t elements) throws MessageError {
            if (buf.length - offset < sizeof(int32) * elements)
                throw new MessageError.OVERFLOW();

            for (size_t idx = 0; idx < elements; idx++) {
                int32 be32 = val[idx].to_big_endian();
                Memory.copy(&buf[offset + sizeof(int32) * idx], &be32, sizeof(int32));
            }

            return sizeof(int32) * elements;
        }

        ssize_t int64_encode_array(void[] buf, Posix.off_t offset, int64 *val, size_t elements) throws MessageError {
            if (buf.length - offset < sizeof(int64) * elements)
                throw new MessageError.OVERFLOW();

            for (size_t idx = 0; idx < elements; idx++) {
                int16 be64 = val[idx].to_big_endian();
                Memory.copy(&buf[offset + sizeof(int64) * idx], &be64, sizeof(int64));
            }

            return sizeof(int64) * elements;
        }

        ssize_t float_encode_array(void[] buf, Posix.off_t offset, float *val, size_t elements) throws MessageError {
            return int32_encode_array(buf, offset, (int32 *) val, elements);
        }

        ssize_t double_encode_array(void[] buf, Posix.off_t offset, double *val, size_t elements) throws MessageError {
            return int64_encode_array(buf, offset, (int64 *) val, elements);
        }

        ssize_t string_encode_array(void[] buf, Posix.off_t offset, string *val, size_t elements) throws MessageError {
            Posix.off_t pos = 0;

            for (size_t idx = 0; idx < elements; idx++) {
                int32 length = val[idx].length + 1;     // add space for '\0'

                pos += int32_encode_array(buf, offset + pos, &length, 1);
                pos += int8_encode_array(buf, offset + pos, val[idx].data, length);
            }

            return pos;
        }
    }
}
