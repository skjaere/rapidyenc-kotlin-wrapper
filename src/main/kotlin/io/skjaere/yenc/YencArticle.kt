package io.skjaere.yenc

data class YencArticle(
    val data: ByteArray,
    val partNumber: Int,
    val totalParts: Int,
    val crc32: UInt,
    val partCrc32: UInt
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is YencArticle) return false
        return data.contentEquals(other.data) &&
            partNumber == other.partNumber &&
            totalParts == other.totalParts &&
            crc32 == other.crc32 &&
            partCrc32 == other.partCrc32
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + partNumber
        result = 31 * result + totalParts
        result = 31 * result + crc32.hashCode()
        result = 31 * result + partCrc32.hashCode()
        return result
    }
}
