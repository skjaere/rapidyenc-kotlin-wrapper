package io.skjaere.yenc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class RapidYencEncodeTest {

    @Test
    fun `encode and decode roundtrip preserves data`() {
        val original = "Hello, yEnc World!".toByteArray()
        val encoded = RapidYenc.encode(original)
        val decoded = RapidYenc.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `encode and decode roundtrip with binary data`() {
        val original = ByteArray(256) { it.toByte() }
        val encoded = RapidYenc.encode(original)
        val decoded = RapidYenc.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `encode empty input returns empty output`() {
        val result = RapidYenc.encode(ByteArray(0))
        assertEquals(0, result.size)
    }

    @Test
    fun `encode single byte`() {
        val original = byteArrayOf(0x41) // 'A'
        val encoded = RapidYenc.encode(original)
        assertTrue(encoded.isNotEmpty())
        val decoded = RapidYenc.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `encode large input roundtrips correctly`() {
        val original = ByteArray(65536) { (it % 256).toByte() }
        val encoded = RapidYenc.encode(original)
        val decoded = RapidYenc.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `encodeMaxLength returns value greater than or equal to actual encoded length`() {
        val src = ByteArray(1000) { (it % 256).toByte() }
        val maxLen = RapidYenc.encodeMaxLength(src.size.toLong())
        val encoded = RapidYenc.encode(src)
        assertTrue(maxLen >= encoded.size, "maxLen=$maxLen should be >= actual=${encoded.size}")
    }

    @Test
    fun `encodeEx with column tracking`() {
        val chunk1 = ByteArray(100) { (it % 256).toByte() }
        val result1 = RapidYenc.encodeEx(chunk1, isEnd = false)
        assertTrue(result1.column >= 0)
        assertTrue(result1.data.isNotEmpty())

        val chunk2 = ByteArray(50) { ((it + 100) % 256).toByte() }
        val result2 = RapidYenc.encodeEx(chunk2, column = result1.column, isEnd = true)
        assertTrue(result2.data.isNotEmpty())
    }

    @Test
    fun `encodeEx incremental roundtrip`() {
        val original = ByteArray(200) { (it % 256).toByte() }
        val chunk1 = original.copyOfRange(0, 100)
        val chunk2 = original.copyOfRange(100, 200)

        val enc1 = RapidYenc.encodeEx(chunk1, isEnd = false)
        val enc2 = RapidYenc.encodeEx(chunk2, column = enc1.column, isEnd = true)

        val fullEncoded = enc1.data + enc2.data
        val decoded = RapidYenc.decode(fullEncoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `encode with direct ByteBuffers`() {
        val original = "ByteBuffer test data".toByteArray()
        val maxLen = RapidYenc.encodeMaxLength(original.size.toLong()).toInt()

        val srcBuf = ByteBuffer.allocateDirect(original.size)
        srcBuf.put(original)
        srcBuf.flip()

        val destBuf = ByteBuffer.allocateDirect(maxLen)

        val written = RapidYenc.encode(srcBuf, destBuf)
        assertTrue(written > 0)

        destBuf.flip()
        val encoded = ByteArray(destBuf.remaining())
        destBuf.get(encoded)

        val decoded = RapidYenc.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `encodeKernel returns a valid kernel`() {
        val kernel = RapidYenc.encodeKernel()
        assertNotNull(kernel)
    }
}
