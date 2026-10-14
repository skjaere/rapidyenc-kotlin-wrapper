package io.skjaere.yenc

import com.sun.jna.IntegerType
import com.sun.jna.Native

/**
 * A C `size_t`: 32 bits on 32-bit ABIs (armeabi-v7a, x86) and 64 bits on 64-bit ones. JNA
 * passes a Kotlin [Long] as 64 bits everywhere, which shifts every later argument of a
 * `size_t` function on a 32-bit ABI.
 */
class SizeT(value: Long = 0) : IntegerType(Native.SIZE_T_SIZE, value, true) {
    override fun toByte(): Byte = toInt().toByte()
    override fun toShort(): Short = toInt().toShort()
}
