package io.skjaere.yenc

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.nio.ByteBuffer

object RapidYenc {

    private val initialized: Boolean by lazy {
        RapidYencLibrary.rapidyenc_encode_init()
        RapidYencLibrary.rapidyenc_decode_init()
        RapidYencLibrary.rapidyenc_crc_init()
        true
    }

    private fun ensureInitialized() {
        initialized
    }

    // ── Version ──

    fun version(): Triple<Int, Int, Int> {
        val v = RapidYencLibrary.rapidyenc_version()
        return Triple((v shr 16) and 0xFF, (v shr 8) and 0xFF, v and 0xFF)
    }

    fun versionRaw(): Int = RapidYencLibrary.rapidyenc_version()

    // ── Encode ──

    fun encode(src: ByteArray): ByteArray {
        ensureInitialized()
        if (src.isEmpty()) return ByteArray(0)
        val maxLen = RapidYencLibrary.rapidyenc_encode_max_length(src.size.toLong(), 128)
        val srcMem = Memory(src.size.toLong())
        val destMem = Memory(maxLen)
        srcMem.write(0, src, 0, src.size)
        val written = RapidYencLibrary.rapidyenc_encode(srcMem, destMem, src.size.toLong())
        val result = ByteArray(written.toInt())
        destMem.read(0, result, 0, written.toInt())
        return result
    }

    fun encodeEx(
        src: ByteArray,
        lineSize: Int = 128,
        column: Int = 0,
        isEnd: Boolean = true
    ): EncodeResult {
        ensureInitialized()
        if (src.isEmpty()) return EncodeResult(ByteArray(0), column)
        val maxLen = RapidYencLibrary.rapidyenc_encode_max_length(src.size.toLong(), lineSize)
        val srcMem = Memory(src.size.toLong())
        val destMem = Memory(maxLen)
        srcMem.write(0, src, 0, src.size)
        val columnMem = Memory(4)
        columnMem.setInt(0, column)
        val written = RapidYencLibrary.rapidyenc_encode_ex(
            lineSize, columnMem, srcMem, destMem, src.size.toLong(), if (isEnd) 1 else 0
        )
        val result = ByteArray(written.toInt())
        destMem.read(0, result, 0, written.toInt())
        return EncodeResult(result, columnMem.getInt(0))
    }

    fun encode(src: ByteBuffer, dest: ByteBuffer): Long {
        ensureInitialized()
        require(src.isDirect && dest.isDirect) { "Both buffers must be direct ByteBuffers" }
        val srcPtr = Native.getDirectBufferPointer(src)
        val destPtr = Native.getDirectBufferPointer(dest)
        val written = RapidYencLibrary.rapidyenc_encode(srcPtr, destPtr, src.remaining().toLong())
        dest.position(dest.position() + written.toInt())
        return written
    }

    fun encodeMaxLength(length: Long, lineSize: Int = 128): Long =
        RapidYencLibrary.rapidyenc_encode_max_length(length, lineSize)

    fun encodeKernel(): RapidYencKernel {
        ensureInitialized()
        return RapidYencKernel.fromValue(RapidYencLibrary.rapidyenc_encode_kernel())
    }

    // ── Decode ──

    fun decode(src: ByteArray): ByteArray {
        ensureInitialized()
        if (src.isEmpty()) return ByteArray(0)
        val srcMem = Memory(src.size.toLong())
        val destMem = Memory(src.size.toLong())
        srcMem.write(0, src, 0, src.size)
        val written = RapidYencLibrary.rapidyenc_decode(srcMem, destMem, src.size.toLong())
        val result = ByteArray(written.toInt())
        destMem.read(0, result, 0, written.toInt())
        return result
    }

    fun decodeEx(
        src: ByteArray,
        isRaw: Boolean = false,
        state: RapidYencDecoderState? = null
    ): DecodeExResult {
        ensureInitialized()
        if (src.isEmpty()) return DecodeExResult(ByteArray(0), state)
        val srcMem = Memory(src.size.toLong())
        val destMem = Memory(src.size.toLong())
        srcMem.write(0, src, 0, src.size)
        val stateMem: Memory? = state?.let {
            Memory(4).also { mem -> mem.setInt(0, it.value) }
        }
        val written = RapidYencLibrary.rapidyenc_decode_ex(
            if (isRaw) 1 else 0, srcMem, destMem, src.size.toLong(), stateMem
        )
        val result = ByteArray(written.toInt())
        destMem.read(0, result, 0, written.toInt())
        val newState = stateMem?.let { RapidYencDecoderState.fromValue(it.getInt(0)) }
        return DecodeExResult(result, newState)
    }

    fun decodeIncremental(
        src: ByteArray,
        state: RapidYencDecoderState = RapidYencDecoderState.CRLF
    ): IncrementalDecodeResult {
        ensureInitialized()
        if (src.isEmpty()) {
            return IncrementalDecodeResult(ByteArray(0), RapidYencDecoderEnd.NONE, state, 0)
        }
        val srcMem = Memory(src.size.toLong())
        srcMem.write(0, src, 0, src.size)
        val destMem = Memory(src.size.toLong())

        val ptrSize = Native.POINTER_SIZE.toLong()
        val srcPtrMem = Memory(ptrSize)
        srcPtrMem.setPointer(0, srcMem)
        val destPtrMem = Memory(ptrSize)
        destPtrMem.setPointer(0, destMem)

        val stateMem = Memory(4)
        stateMem.setInt(0, state.value)

        val endResult = RapidYencLibrary.rapidyenc_decode_incremental(
            srcPtrMem, destPtrMem, src.size.toLong(), stateMem
        )

        val updatedDestPtr = destPtrMem.getPointer(0)
        val bytesWritten = Pointer.nativeValue(updatedDestPtr) - Pointer.nativeValue(destMem)
        val result = ByteArray(bytesWritten.toInt())
        if (bytesWritten > 0) {
            destMem.read(0, result, 0, bytesWritten.toInt())
        }

        val updatedSrcPtr = srcPtrMem.getPointer(0)
        val bytesConsumed = Pointer.nativeValue(updatedSrcPtr) - Pointer.nativeValue(srcMem)

        return IncrementalDecodeResult(
            data = result,
            end = RapidYencDecoderEnd.fromValue(endResult),
            state = RapidYencDecoderState.fromValue(stateMem.getInt(0)),
            bytesConsumed = bytesConsumed
        )
    }

    fun decode(src: ByteBuffer, dest: ByteBuffer): Long {
        ensureInitialized()
        require(src.isDirect && dest.isDirect) { "Both buffers must be direct ByteBuffers" }
        val srcPtr = Native.getDirectBufferPointer(src)
        val destPtr = Native.getDirectBufferPointer(dest)
        val written = RapidYencLibrary.rapidyenc_decode(srcPtr, destPtr, src.remaining().toLong())
        dest.position(dest.position() + written.toInt())
        return written
    }

    fun decodeKernel(): RapidYencKernel {
        ensureInitialized()
        return RapidYencKernel.fromValue(RapidYencLibrary.rapidyenc_decode_kernel())
    }

    // ── CRC32 ──

    fun crc32(data: ByteArray, initCrc: UInt = 0u): UInt {
        ensureInitialized()
        if (data.isEmpty()) return initCrc
        val mem = Memory(data.size.toLong())
        mem.write(0, data, 0, data.size)
        return RapidYencLibrary.rapidyenc_crc(mem, data.size.toLong(), initCrc.toInt()).toUInt()
    }

    fun crc32Combine(crc1: UInt, crc2: UInt, length2: Long): UInt {
        ensureInitialized()
        return RapidYencLibrary.rapidyenc_crc_combine(crc1.toInt(), crc2.toInt(), length2).toUInt()
    }

    fun crc32Zeros(initCrc: UInt, length: Long): UInt {
        ensureInitialized()
        return RapidYencLibrary.rapidyenc_crc_zeros(initCrc.toInt(), length).toUInt()
    }

    fun crc32Unzero(initCrc: UInt, length: Long): UInt {
        ensureInitialized()
        return RapidYencLibrary.rapidyenc_crc_unzero(initCrc.toInt(), length).toUInt()
    }

    fun crc32Multiply(a: UInt, b: UInt): UInt {
        ensureInitialized()
        return RapidYencLibrary.rapidyenc_crc_multiply(a.toInt(), b.toInt()).toUInt()
    }

    fun crc32TwoPow(n: Long): UInt {
        ensureInitialized()
        return RapidYencLibrary.rapidyenc_crc_2pow(n).toUInt()
    }

    fun crc32TwoFiftySixPow(n: Long): UInt {
        ensureInitialized()
        return RapidYencLibrary.rapidyenc_crc_256pow(n).toUInt()
    }

    fun crcKernel(): RapidYencKernel {
        ensureInitialized()
        return RapidYencKernel.fromValue(RapidYencLibrary.rapidyenc_crc_kernel())
    }
}
