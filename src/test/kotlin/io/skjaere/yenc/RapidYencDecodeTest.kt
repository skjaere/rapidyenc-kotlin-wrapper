package io.skjaere.yenc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

class RapidYencDecodeTest {

    @Test
    fun `decode empty input returns empty output`() {
        val result = RapidYenc.decode(ByteArray(0))
        assertEquals(0, result.size)
    }

    @Test
    fun `decodeEx with state tracking`() {
        val original = ByteArray(100) { (it % 256).toByte() }
        val encoded = RapidYenc.encode(original)
        val result = RapidYenc.decodeEx(encoded, isRaw = false, state = RapidYencDecoderState.CRLF)
        assertArrayEquals(original, result.data)
        assertNotNull(result.state)
    }

    @Test
    fun `decodeEx without state`() {
        val original = "test data for decodeEx".toByteArray()
        val encoded = RapidYenc.encode(original)
        val result = RapidYenc.decodeEx(encoded)
        assertArrayEquals(original, result.data)
        assertNull(result.state)
    }

    @Test
    fun `decodeIncremental with yEnc end sequence`() {
        val original = "some data".toByteArray()
        val encoded = RapidYenc.encode(original)
        // Append yEnc control end sequence: \r\n=yend
        val withEnd = encoded + "\r\n=yend size=9 crc32=abcd1234\r\n".toByteArray()

        val result = RapidYenc.decodeIncremental(withEnd)
        assertEquals(RapidYencDecoderEnd.CONTROL, result.end)
        assertArrayEquals(original, result.data)
        assertTrue(result.bytesConsumed > 0)
        assertTrue(result.bytesConsumed <= withEnd.size)
    }

    @Test
    fun `decodeIncremental with no end sequence`() {
        val original = ByteArray(50) { (it % 256).toByte() }
        val encoded = RapidYenc.encode(original)

        val result = RapidYenc.decodeIncremental(encoded)
        assertEquals(RapidYencDecoderEnd.NONE, result.end)
        assertArrayEquals(original, result.data)
    }

    @Test
    fun `decode with direct ByteBuffers`() {
        val original = "ByteBuffer decode test".toByteArray()
        val encoded = RapidYenc.encode(original)

        val srcBuf = ByteBuffer.allocateDirect(encoded.size)
        srcBuf.put(encoded)
        srcBuf.flip()

        val destBuf = ByteBuffer.allocateDirect(encoded.size)

        val written = RapidYenc.decode(srcBuf, destBuf)
        assertTrue(written > 0)

        destBuf.flip()
        val decoded = ByteArray(destBuf.remaining())
        destBuf.get(decoded)

        assertArrayEquals(original, decoded)
    }

    @Test
    fun `decode roundtrip with all byte values`() {
        val original = ByteArray(256) { it.toByte() }
        val encoded = RapidYenc.encode(original)
        val decoded = RapidYenc.decode(encoded)
        assertArrayEquals(original, decoded)
    }

    @Test
    fun `decodeKernel returns a valid kernel`() {
        val kernel = RapidYenc.decodeKernel()
        assertNotNull(kernel)
    }

    @Test
    fun `version returns expected values`() {
        val (major, minor, patch) = RapidYenc.version()
        assertEquals(1, major)
        assertEquals(1, minor)
        assertEquals(1, patch)
        assertEquals(0x010101, RapidYenc.versionRaw())
    }
}
