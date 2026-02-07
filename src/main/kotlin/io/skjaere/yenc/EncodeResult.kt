package io.skjaere.yenc

data class EncodeResult(
    val data: ByteArray,
    val column: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncodeResult) return false
        return data.contentEquals(other.data) && column == other.column
    }

    override fun hashCode(): Int = 31 * data.contentHashCode() + column
}
