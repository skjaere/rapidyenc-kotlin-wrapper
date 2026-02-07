package io.skjaere.yenc

enum class RapidYencDecoderState(val value: Int) {
    CRLF(0),
    EQ(1),
    CR(2),
    NONE(3),
    CRLFDT(4),
    CRLFDTCR(5),
    CRLFEQ(6);

    companion object {
        fun fromValue(value: Int): RapidYencDecoderState =
            entries.first { it.value == value }
    }
}
