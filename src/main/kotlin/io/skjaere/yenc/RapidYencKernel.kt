package io.skjaere.yenc

enum class RapidYencKernel(val value: Int) {
    GENERIC(0x0),
    ARMCRC(0x8),
    ZBC(0x10),
    ARMPMULL(0x48),
    SSE2(0x100),
    SSSE3(0x200),
    PCLMUL(0x340),
    AVX(0x381),
    AVX2(0x403),
    VPCLMUL(0x440),
    VBMI2(0x603),
    NEON(0x1000),
    RVV(0x10000),
    UNKNOWN(-1);

    companion object {
        fun fromValue(value: Int): RapidYencKernel =
            entries.find { it.value == value } ?: UNKNOWN
    }
}
