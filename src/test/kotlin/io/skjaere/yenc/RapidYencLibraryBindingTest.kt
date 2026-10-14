package io.skjaere.yenc

import com.sun.jna.Pointer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier

/**
 * The JNA declarations must match rapidyenc.h. A 64-bit host passes a Long where the C side
 * wants a size_t without complaint, so only the declared types can catch a mismatch here.
 */
class RapidYencLibraryBindingTest {

    // From rapidyenc.h: the result type, then the parameter types.
    private val prototypes = mapOf(
        "rapidyenc_version" to listOf("int"),
        "rapidyenc_encode_init" to listOf("void"),
        "rapidyenc_encode" to listOf("size_t", "ptr", "ptr", "size_t"),
        "rapidyenc_encode_ex" to listOf("size_t", "int", "ptr", "ptr", "ptr", "size_t", "int"),
        "rapidyenc_encode_max_length" to listOf("size_t", "size_t", "int"),
        "rapidyenc_encode_kernel" to listOf("int"),
        "rapidyenc_decode_init" to listOf("void"),
        "rapidyenc_decode" to listOf("size_t", "ptr", "ptr", "size_t"),
        "rapidyenc_decode_ex" to listOf("size_t", "int", "ptr", "ptr", "size_t", "ptr"),
        "rapidyenc_decode_incremental" to listOf("enum", "ptr", "ptr", "size_t", "ptr"),
        "rapidyenc_decode_kernel" to listOf("int"),
        "rapidyenc_crc_init" to listOf("void"),
        "rapidyenc_crc" to listOf("uint32_t", "ptr", "size_t", "uint32_t"),
        "rapidyenc_crc_combine" to listOf("uint32_t", "uint32_t", "uint32_t", "uint64_t"),
        "rapidyenc_crc_zeros" to listOf("uint32_t", "uint32_t", "uint64_t"),
        "rapidyenc_crc_unzero" to listOf("uint32_t", "uint32_t", "uint64_t"),
        "rapidyenc_crc_multiply" to listOf("uint32_t", "uint32_t", "uint32_t"),
        "rapidyenc_crc_2pow" to listOf("uint32_t", "int64_t"),
        "rapidyenc_crc_256pow" to listOf("uint32_t", "uint64_t"),
        "rapidyenc_crc_kernel" to listOf("int"),
    )

    private val kotlinTypes: Map<String, Class<*>> = mapOf(
        "void" to Void.TYPE,
        "int" to Int::class.java,
        "enum" to Int::class.java,
        "uint32_t" to Int::class.java,
        "int64_t" to Long::class.java,
        "uint64_t" to Long::class.java,
        "size_t" to SizeT::class.java,
        "ptr" to Pointer::class.java,
    )

    @Test
    fun `every native function is declared with the types of its C prototype`() {
        val declared = RapidYencLibrary::class.java.declaredMethods
            .filter { Modifier.isNative(it.modifiers) }
            .associateBy { it.name }
        assertEquals(prototypes.keys, declared.keys)
        prototypes.forEach { (name, types) ->
            val method = declared.getValue(name)
            assertEquals(types.map { kotlinTypes.getValue(it) }, listOf(method.returnType) + method.parameterTypes, name)
        }
    }
}
