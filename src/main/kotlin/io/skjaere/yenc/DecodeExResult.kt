package io.skjaere.yenc

data class DecodeExResult(
    val data: ByteArray,
    val state: RapidYencDecoderState?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DecodeExResult) return false
        return data.contentEquals(other.data) && state == other.state
    }

    override fun hashCode(): Int = 31 * data.contentHashCode() + (state?.hashCode() ?: 0)
}
