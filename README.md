# rapidyenc-kotlin-wrapper

[![CI](https://github.com/skjaere/rapidyenc-kotlin-wrapper/actions/workflows/ci.yml/badge.svg)](https://github.com/skjaere/rapidyenc-kotlin-wrapper/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/skjaere/rapidyenc-kotlin-wrapper/graph/badge.svg)](https://codecov.io/gh/skjaere/rapidyenc-kotlin-wrapper)

Kotlin JNA wrapper for [rapidyenc](https://github.com/animetosho/rapidyenc), a high-performance yEnc encoder/decoder with SIMD acceleration.

## Features

- yEnc encoding and decoding with SIMD-accelerated kernels (SSE2, AVX2, NEON, etc.)
- Incremental encoding/decoding for streaming use cases
- CRC32 computation, combination, and field operations
- Zero-copy `ByteBuffer` API for high-throughput paths
- Thread-safe after automatic one-time initialization
- No manual memory management required

## Installation

Add JitPack and the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("com.github.skjaere:rapidyenc-kotlin-wrapper:TAG")
}
```

Replace `TAG` with a release tag, commit hash, or branch name (e.g. `v0.1.0`, `main-SNAPSHOT`).

## Usage

All functions are accessed through the `RapidYenc` object. Initialization is automatic and thread-safe.

### Encoding

```kotlin
import io.skjaere.yenc.RapidYenc

// Basic encode
val encoded = RapidYenc.encode(data)

// Incremental encode with column tracking
val result1 = RapidYenc.encodeEx(chunk1, lineSize = 128, column = 0, isEnd = false)
val result2 = RapidYenc.encodeEx(chunk2, column = result1.column, isEnd = true)

// Zero-copy with direct ByteBuffers
val maxLen = RapidYenc.encodeMaxLength(src.remaining().toLong()).toInt()
val dest = ByteBuffer.allocateDirect(maxLen)
RapidYenc.encode(src, dest)
```

### Decoding

```kotlin
// Basic decode
val decoded = RapidYenc.decode(encoded)

// Decode with NNTP dot unstuffing and state tracking
val result = RapidYenc.decodeEx(encoded, isRaw = true, state = RapidYencDecoderState.CRLF)

// Incremental decode with end-sequence detection
val result = RapidYenc.decodeIncremental(data)
when (result.end) {
    RapidYencDecoderEnd.CONTROL -> println("Found =yend at byte ${result.bytesConsumed}")
    RapidYencDecoderEnd.ARTICLE -> println("Found article end")
    RapidYencDecoderEnd.NONE    -> println("No end sequence found")
}
```

### CRC32

```kotlin
// Compute CRC32
val crc = RapidYenc.crc32(data)

// Incremental CRC32
val crc1 = RapidYenc.crc32(part1)
val crc2 = RapidYenc.crc32(part2, initCrc = crc1)

// Combine two independent CRC32 values
val combined = RapidYenc.crc32Combine(crc1, crc2, part2.size.toLong())
```

## API Reference

### Encoding

| Function | Description |
|----------|-------------|
| `encode(ByteArray): ByteArray` | Encode bytes using yEnc |
| `encodeEx(ByteArray, lineSize, column, isEnd): EncodeResult` | Incremental encode with column tracking |
| `encode(ByteBuffer, ByteBuffer): Long` | Zero-copy encode with direct buffers |
| `encodeMaxLength(Long, Int): Long` | Maximum output buffer size for a given input |
| `encodeKernel(): RapidYencKernel` | ISA kernel selected for encoding |

### Decoding

| Function | Description |
|----------|-------------|
| `decode(ByteArray): ByteArray` | Decode yEnc-encoded bytes |
| `decodeEx(ByteArray, isRaw, state): DecodeExResult` | Decode with NNTP dot unstuffing and state tracking |
| `decodeIncremental(ByteArray, state): IncrementalDecodeResult` | Decode with end-sequence detection |
| `decode(ByteBuffer, ByteBuffer): Long` | Zero-copy decode with direct buffers |
| `decodeKernel(): RapidYencKernel` | ISA kernel selected for decoding |

### CRC32

| Function | Description |
|----------|-------------|
| `crc32(ByteArray, UInt): UInt` | Compute CRC32 (supports incremental via `initCrc`) |
| `crc32Combine(UInt, UInt, Long): UInt` | Combine two CRC32 values |
| `crc32Zeros(UInt, Long): UInt` | CRC32 after appending zero bytes |
| `crc32Unzero(UInt, Long): UInt` | Inverse of `crc32Zeros` |
| `crc32Multiply(UInt, UInt): UInt` | Multiply in CRC32 field |
| `crc32TwoPow(Long): UInt` | 2^n in CRC32 field |
| `crc32TwoFiftySixPow(Long): UInt` | 2^(8n) in CRC32 field |
| `crcKernel(): RapidYencKernel` | ISA kernel selected for CRC32 |

### Other

| Function | Description |
|----------|-------------|
| `version(): Triple<Int, Int, Int>` | Library version as (major, minor, patch) |
| `versionRaw(): Int` | Raw version in `0xMMmmpp` format |

## Low-Level Access

For direct access to the C API, use `RapidYencLibrary` which exposes all 20 native functions via JNA direct mapping:

```kotlin
import io.skjaere.yenc.RapidYencLibrary

RapidYencLibrary.rapidyenc_encode_init()
val version = RapidYencLibrary.rapidyenc_version()
```

## Build

Requires Java 25+ and uses the Gradle wrapper (Gradle 9.0):

```bash
./gradlew build              # Build and run tests
./gradlew publishToMavenLocal  # Install to ~/.m2
```

## Platform Support

Currently ships with the native library for **Linux x86-64**. The `librapidyenc.so` is bundled in the JAR and loaded automatically by JNA.

## License

The rapidyenc C library is created by [animetosho](https://github.com/animetosho/rapidyenc).
