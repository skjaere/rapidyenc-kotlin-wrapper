package io.skjaere.yenc

import java.io.ByteArrayOutputStream

object YencEncoder {

    fun encodeSinglePart(data: ByteArray, filename: String, lineSize: Int = 128): YencArticle {
        require(data.isNotEmpty()) { "Data must not be empty" }
        require(filename.isNotBlank()) { "Filename must not be blank" }
        require('\r' !in filename && '\n' !in filename) { "Filename must not contain CR or LF" }

        val crc = RapidYenc.crc32(data)
        val encoded = RapidYenc.encodeEx(data, lineSize).data

        val header = "=ybegin line=$lineSize size=${data.size} name=$filename\r\n".toByteArray()
        val footer = "=yend size=${data.size} crc32=${crc.toHexCrc()}\r\n".toByteArray()

        val article = assembleBytes(header, encoded, if (encoded.endsWithCrlf()) byteArrayOf() else "\r\n".toByteArray(), footer)

        return YencArticle(
            data = article,
            partNumber = 1,
            totalParts = 1,
            crc32 = crc,
            partCrc32 = crc
        )
    }

    fun encodeMultiPart(data: ByteArray, filename: String, partSize: Int, lineSize: Int = 128): List<YencArticle> {
        require(data.isNotEmpty()) { "Data must not be empty" }
        require(filename.isNotBlank()) { "Filename must not be blank" }
        require('\r' !in filename && '\n' !in filename) { "Filename must not contain CR or LF" }
        require(partSize > 0) { "Part size must be greater than zero" }

        if (data.size <= partSize) {
            return listOf(encodeSinglePart(data, filename, lineSize))
        }

        val fullCrc = RapidYenc.crc32(data)
        val totalParts = (data.size + partSize - 1) / partSize
        val articles = mutableListOf<YencArticle>()

        for (partIndex in 0 until totalParts) {
            val begin = partIndex * partSize
            val end = minOf(begin + partSize, data.size)
            val partData = data.copyOfRange(begin, end)
            val partNum = partIndex + 1

            val partCrc = RapidYenc.crc32(partData)
            val encoded = RapidYenc.encodeEx(partData, lineSize).data

            val header = "=ybegin part=$partNum total=$totalParts line=$lineSize size=${data.size} name=$filename\r\n".toByteArray()
            val ypartLine = "=ypart begin=${begin + 1} end=$end\r\n".toByteArray()
            val footer = "=yend size=${partData.size} part=$partNum pcrc32=${partCrc.toHexCrc()} crc32=${fullCrc.toHexCrc()}\r\n".toByteArray()

            val article = assembleBytes(header, ypartLine, encoded, if (encoded.endsWithCrlf()) byteArrayOf() else "\r\n".toByteArray(), footer)

            articles.add(
                YencArticle(
                    data = article,
                    partNumber = partNum,
                    totalParts = totalParts,
                    crc32 = fullCrc,
                    partCrc32 = partCrc
                )
            )
        }

        return articles
    }

    private fun UInt.toHexCrc(): String = this.toString(16).padStart(8, '0')

    private fun ByteArray.endsWithCrlf(): Boolean =
        size >= 2 && this[size - 2] == '\r'.code.toByte() && this[size - 1] == '\n'.code.toByte()

    private fun assembleBytes(vararg parts: ByteArray): ByteArray {
        val out = ByteArrayOutputStream(parts.sumOf { it.size })
        for (part in parts) {
            out.write(part)
        }
        return out.toByteArray()
    }
}
