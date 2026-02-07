package io.skjaere.yenc

data class IncrementalDecodeResult(
    val data: ByteArray,
    val end: RapidYencDecoderEnd,
    val state: RapidYencDecoderState,
    val bytesConsumed: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IncrementalDecodeResult) return false
        return data.contentEquals(other.data) &&
            end == other.end &&
            state == other.state &&
            bytesConsumed == other.bytesConsumed
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + bytesConsumed.hashCode()
        return result
    }
}
