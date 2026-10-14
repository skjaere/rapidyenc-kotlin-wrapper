package io.skjaere.yenc

import com.sun.jna.Native
import com.sun.jna.Pointer

/** size_t parameters and results are [SizeT], so the mapping holds on 32-bit ABIs too. */
@Suppress("FunctionName")
object RapidYencLibrary {

    init {
        Native.register(RapidYencLibrary::class.java, "rapidyenc")
    }

    // Version
    @JvmStatic external fun rapidyenc_version(): Int

    // Encode
    @JvmStatic external fun rapidyenc_encode_init()
    @JvmStatic external fun rapidyenc_encode(src: Pointer, dest: Pointer, src_length: SizeT): SizeT
    @JvmStatic external fun rapidyenc_encode_ex(
        line_size: Int,
        column: Pointer?,
        src: Pointer,
        dest: Pointer,
        src_length: SizeT,
        is_end: Int
    ): SizeT
    @JvmStatic external fun rapidyenc_encode_max_length(length: SizeT, line_size: Int): SizeT
    @JvmStatic external fun rapidyenc_encode_kernel(): Int

    // Decode
    @JvmStatic external fun rapidyenc_decode_init()
    @JvmStatic external fun rapidyenc_decode(src: Pointer, dest: Pointer, src_length: SizeT): SizeT
    @JvmStatic external fun rapidyenc_decode_ex(
        is_raw: Int,
        src: Pointer,
        dest: Pointer,
        src_length: SizeT,
        state: Pointer?
    ): SizeT
    @JvmStatic external fun rapidyenc_decode_incremental(
        src: Pointer,
        dest: Pointer,
        src_length: SizeT,
        state: Pointer?
    ): Int
    @JvmStatic external fun rapidyenc_decode_kernel(): Int

    // CRC32
    @JvmStatic external fun rapidyenc_crc_init()
    @JvmStatic external fun rapidyenc_crc(src: Pointer, src_length: SizeT, init_crc: Int): Int
    @JvmStatic external fun rapidyenc_crc_combine(crc1: Int, crc2: Int, length2: Long): Int
    @JvmStatic external fun rapidyenc_crc_zeros(init_crc: Int, length: Long): Int
    @JvmStatic external fun rapidyenc_crc_unzero(init_crc: Int, length: Long): Int
    @JvmStatic external fun rapidyenc_crc_multiply(a: Int, b: Int): Int
    @JvmStatic external fun rapidyenc_crc_2pow(n: Long): Int
    @JvmStatic external fun rapidyenc_crc_256pow(n: Long): Int
    @JvmStatic external fun rapidyenc_crc_kernel(): Int
}
