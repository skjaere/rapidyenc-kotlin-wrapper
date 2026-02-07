package io.skjaere.yenc

enum class RapidYencDecoderEnd(val value: Int) {
    NONE(0),
    CONTROL(1),
    ARTICLE(2);

    companion object {
        fun fromValue(value: Int): RapidYencDecoderEnd =
            entries.first { it.value == value }
    }
}
