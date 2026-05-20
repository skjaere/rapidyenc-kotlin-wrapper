package io.skjaere.yenc

/**
 * Result of the direct-buffer [RapidYenc.decodeIncremental] overload. Position
 * advances are applied in-place on the caller's buffers; this struct surfaces the
 * deltas + control state without forcing the caller to track positions across
 * the call.
 */
data class IncrementalDecodeBufferResult(
    val bytesConsumed: Long,
    val bytesWritten: Long,
    val end: RapidYencDecoderEnd,
    val state: RapidYencDecoderState
)
