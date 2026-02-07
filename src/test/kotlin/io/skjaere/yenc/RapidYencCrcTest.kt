package io.skjaere.yenc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.zip.CRC32 as JavaCRC32

class RapidYencCrcTest {

    @Test
    fun `crc32 matches java CRC32 implementation`() {
        val data = "test data for CRC32".toByteArray()
        val javaCrc = JavaCRC32()
        javaCrc.update(data)
        val expected = javaCrc.value.toUInt()
        val actual = RapidYenc.crc32(data)
        assertEquals(expected, actual)
    }

    @Test
    fun `crc32 of empty data returns init crc`() {
        val result = RapidYenc.crc32(ByteArray(0), 0u)
        assertEquals(0u, result)
    }

    @Test
    fun `crc32 incremental matches full computation`() {
        val data = "hello world".toByteArray()
        val part1 = data.copyOfRange(0, 5)
        val part2 = data.copyOfRange(5, data.size)

        val fullCrc = RapidYenc.crc32(data)
        val crc1 = RapidYenc.crc32(part1)
        val crc2 = RapidYenc.crc32(part2, crc1)

        assertEquals(fullCrc, crc2)
    }

    @Test
    fun `crc32Combine produces correct result`() {
        val data1 = "hello ".toByteArray()
        val data2 = "world".toByteArray()
        val combined = data1 + data2

        val crc1 = RapidYenc.crc32(data1)
        val crc2 = RapidYenc.crc32(data2)
        val crcCombined = RapidYenc.crc32Combine(crc1, crc2, data2.size.toLong())
        val crcDirect = RapidYenc.crc32(combined)

        assertEquals(crcDirect, crcCombined)
    }

    @Test
    fun `crc32Zeros and crc32Unzero are inverses`() {
        val data = "test".toByteArray()
        val crc = RapidYenc.crc32(data)
        val length = 100L

        val withZeros = RapidYenc.crc32Zeros(crc, length)
        val recovered = RapidYenc.crc32Unzero(withZeros, length)

        assertEquals(crc, recovered)
    }

    @Test
    fun `crc32 of binary data matches java implementation`() {
        val data = ByteArray(1024) { (it % 256).toByte() }
        val javaCrc = JavaCRC32()
        javaCrc.update(data)
        val expected = javaCrc.value.toUInt()
        val actual = RapidYenc.crc32(data)
        assertEquals(expected, actual)
    }

    @Test
    fun `crc32 of large data matches java implementation`() {
        val data = ByteArray(65536) { (it * 7 % 256).toByte() }
        val javaCrc = JavaCRC32()
        javaCrc.update(data)
        val expected = javaCrc.value.toUInt()
        val actual = RapidYenc.crc32(data)
        assertEquals(expected, actual)
    }

    @Test
    fun `crcKernel returns a valid kernel`() {
        val kernel = RapidYenc.crcKernel()
        assertNotNull(kernel)
    }
}
