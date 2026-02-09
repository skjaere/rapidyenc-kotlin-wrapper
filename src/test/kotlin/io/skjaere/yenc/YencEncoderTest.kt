package io.skjaere.yenc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.zip.CRC32

class YencEncoderTest {

    // ── Single-part tests ──

    @Test
    fun `single-part article has correct ybegin header`() {
        val data = "Hello, world!".toByteArray()
        val article = YencEncoder.encodeSinglePart(data, "test.txt")
        val text = article.data.decodeToString()
        val firstLine = text.lines().first()

        assertTrue(firstLine.startsWith("=ybegin "))
        assertTrue("line=128" in firstLine)
        assertTrue("size=${data.size}" in firstLine)
        assertTrue("name=test.txt" in firstLine)
        assertFalse("part=" in firstLine)
    }

    @Test
    fun `single-part article has correct yend footer`() {
        val data = "Hello, world!".toByteArray()
        val article = YencEncoder.encodeSinglePart(data, "test.txt")
        val text = article.data.decodeToString()
        val lastLine = text.trimEnd('\r', '\n').lines().last()

        assertTrue(lastLine.startsWith("=yend "))
        assertTrue("size=${data.size}" in lastLine)

        val javaCrc = CRC32()
        javaCrc.update(data)
        val expectedCrc = javaCrc.value.toUInt().toString(16).padStart(8, '0')
        assertTrue("crc32=$expectedCrc" in lastLine)
    }

    @Test
    fun `single-part CRC32 matches java util zip CRC32`() {
        val data = "The quick brown fox jumps over the lazy dog".toByteArray()
        val article = YencEncoder.encodeSinglePart(data, "fox.txt")

        val javaCrc = CRC32()
        javaCrc.update(data)
        assertEquals(javaCrc.value.toUInt(), article.crc32)
        assertEquals(article.crc32, article.partCrc32)
    }

    @Test
    fun `single-part roundtrip preserves original data`() {
        val data = "Hello, yEnc!".toByteArray()
        val article = YencEncoder.encodeSinglePart(data, "hello.txt")
        val raw = article.data

        // Find end of =ybegin line
        val headerEnd = findCrlf(raw, 0) + 2
        // Find start of =yend line
        val footerStart = findMarker(raw, "=yend ")
        val payload = raw.copyOfRange(headerEnd, footerStart)
        val decoded = RapidYenc.decode(payload)

        assertArrayEquals(data, decoded)
    }

    @Test
    fun `single-part custom lineSize is reflected in header`() {
        val data = "Some data".toByteArray()
        val article = YencEncoder.encodeSinglePart(data, "custom.bin", lineSize = 64)
        val text = article.data.decodeToString()
        val firstLine = text.lines().first()

        assertTrue("line=64" in firstLine)
    }

    @Test
    fun `single-part handles binary data with all 256 byte values`() {
        val data = ByteArray(256) { it.toByte() }
        val article = YencEncoder.encodeSinglePart(data, "binary.bin")

        val text = article.data.decodeToString()
        assertTrue(text.startsWith("=ybegin "))
        assertTrue(text.trimEnd().endsWith("\r\n") || text.contains("=yend"))

        // Verify metadata
        assertEquals(1, article.partNumber)
        assertEquals(1, article.totalParts)
        assertEquals(article.crc32, article.partCrc32)
    }

    @Test
    fun `single-part rejects empty data`() {
        assertThrows(IllegalArgumentException::class.java) {
            YencEncoder.encodeSinglePart(byteArrayOf(), "empty.txt")
        }
    }

    @Test
    fun `single-part rejects blank filename`() {
        assertThrows(IllegalArgumentException::class.java) {
            YencEncoder.encodeSinglePart("data".toByteArray(), "   ")
        }
    }

    @Test
    fun `single-part rejects filename with CR or LF`() {
        assertThrows(IllegalArgumentException::class.java) {
            YencEncoder.encodeSinglePart("data".toByteArray(), "bad\nname.txt")
        }
        assertThrows(IllegalArgumentException::class.java) {
            YencEncoder.encodeSinglePart("data".toByteArray(), "bad\rname.txt")
        }
    }

    @Test
    fun `single-part metadata fields are correct`() {
        val data = "metadata test".toByteArray()
        val article = YencEncoder.encodeSinglePart(data, "meta.txt")

        assertEquals(1, article.partNumber)
        assertEquals(1, article.totalParts)
        assertEquals(article.crc32, article.partCrc32)
    }

    // ── Multipart tests ──

    @Test
    fun `multipart produces correct number of parts`() {
        val data = ByteArray(1000) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "multi.bin", partSize = 300)

        assertEquals(4, articles.size) // 300 + 300 + 300 + 100
    }

    @Test
    fun `multipart each ybegin contains part and total`() {
        val data = ByteArray(500) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "parts.bin", partSize = 200)

        assertEquals(3, articles.size)
        for ((index, article) in articles.withIndex()) {
            val text = article.data.decodeToString()
            val firstLine = text.lines().first()
            assertTrue("part=${index + 1}" in firstLine)
            assertTrue("total=${articles.size}" in firstLine)
        }
    }

    @Test
    fun `multipart each ypart has correct begin and end offsets`() {
        val data = ByteArray(500) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "offsets.bin", partSize = 200)

        val expectedRanges = listOf(1 to 200, 201 to 400, 401 to 500)
        for ((index, article) in articles.withIndex()) {
            val text = article.data.decodeToString()
            val ypartLine = text.lines().first { it.startsWith("=ypart ") }
            val (begin, end) = expectedRanges[index]
            assertTrue("begin=$begin" in ypartLine, "Part ${index + 1} begin mismatch: $ypartLine")
            assertTrue("end=$end" in ypartLine, "Part ${index + 1} end mismatch: $ypartLine")
        }
    }

    @Test
    fun `multipart each yend has correct part size pcrc32 and full crc32`() {
        val data = ByteArray(500) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "crc.bin", partSize = 200)

        val javaCrc = CRC32()
        javaCrc.update(data)
        val fullCrcHex = javaCrc.value.toUInt().toString(16).padStart(8, '0')

        val partSizes = listOf(200, 200, 100)
        for ((index, article) in articles.withIndex()) {
            val text = article.data.decodeToString()
            val yendLine = text.trimEnd('\r', '\n').lines().last { it.startsWith("=yend ") }

            assertTrue("size=${partSizes[index]}" in yendLine, "Part ${index + 1} size mismatch: $yendLine")
            assertTrue("part=${index + 1}" in yendLine, "Part ${index + 1} part number mismatch: $yendLine")
            assertTrue("pcrc32=" in yendLine, "Part ${index + 1} missing pcrc32: $yendLine")
            assertTrue("crc32=$fullCrcHex" in yendLine, "Part ${index + 1} full crc32 mismatch: $yendLine")
        }
    }

    @Test
    fun `multipart roundtrip preserves original data`() {
        val data = ByteArray(1000) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "roundtrip.bin", partSize = 300)

        val reconstructed = java.io.ByteArrayOutputStream()
        for (article in articles) {
            val raw = article.data
            // Skip =ybegin line
            var payloadStart = findCrlf(raw, 0) + 2
            // Skip =ypart line
            payloadStart = findCrlf(raw, payloadStart) + 2
            // Find =yend line
            val footerStart = findMarker(raw, "=yend ")
            val payload = raw.copyOfRange(payloadStart, footerStart)
            val decoded = RapidYenc.decode(payload)
            reconstructed.write(decoded)
        }

        assertArrayEquals(data, reconstructed.toByteArray())
    }

    @Test
    fun `multipart data fitting exactly in partSize produces no remainder`() {
        val data = ByteArray(600) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "exact.bin", partSize = 200)

        assertEquals(3, articles.size)
        // Verify the last part has full size
        val lastYend = articles.last().data.decodeToString().trimEnd('\r', '\n').lines().last { it.startsWith("=yend ") }
        assertTrue("size=200" in lastYend)
    }

    @Test
    fun `multipart data smaller than partSize produces single-part format`() {
        val data = "small".toByteArray()
        val articles = YencEncoder.encodeMultiPart(data, "small.txt", partSize = 1000)

        assertEquals(1, articles.size)
        val text = articles.first().data.decodeToString()
        val firstLine = text.lines().first()

        // Should be single-part format (no part= or total=)
        assertFalse("part=" in firstLine)
        assertFalse("total=" in firstLine)
        assertEquals(1, articles.first().partNumber)
        assertEquals(1, articles.first().totalParts)
    }

    @Test
    fun `multipart metadata fields are correct`() {
        val data = ByteArray(500) { (it % 256).toByte() }
        val articles = YencEncoder.encodeMultiPart(data, "meta.bin", partSize = 200)

        val javaCrc = CRC32()
        javaCrc.update(data)
        val expectedFullCrc = javaCrc.value.toUInt()

        for ((index, article) in articles.withIndex()) {
            assertEquals(index + 1, article.partNumber)
            assertEquals(3, article.totalParts)
            assertEquals(expectedFullCrc, article.crc32)

            // Verify part CRC matches java.util.zip
            val begin = index * 200
            val end = minOf(begin + 200, data.size)
            val partData = data.copyOfRange(begin, end)
            val partCrc = CRC32()
            partCrc.update(partData)
            assertEquals(partCrc.value.toUInt(), article.partCrc32)
        }
    }

    @Test
    fun `multipart rejects zero partSize`() {
        assertThrows(IllegalArgumentException::class.java) {
            YencEncoder.encodeMultiPart("data".toByteArray(), "test.txt", partSize = 0)
        }
    }

    // ── Helpers ──

    /** Find the index of the first \r\n at or after [from]. */
    private fun findCrlf(data: ByteArray, from: Int): Int {
        for (i in from until data.size - 1) {
            if (data[i] == '\r'.code.toByte() && data[i + 1] == '\n'.code.toByte()) return i
        }
        error("CRLF not found")
    }

    /** Find the byte offset where [marker] starts in [data]. */
    private fun findMarker(data: ByteArray, marker: String): Int {
        val target = marker.toByteArray()
        outer@ for (i in 0..data.size - target.size) {
            for (j in target.indices) {
                if (data[i + j] != target[j]) continue@outer
            }
            return i
        }
        error("Marker '$marker' not found")
    }
}
