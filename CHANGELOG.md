# Changelog

## [0.4.0](https://github.com/skjaere/rapidyenc-kotlin-wrapper/compare/v0.3.0...v0.4.0) (2026-11-12)


### ⚠ BREAKING CHANGES

* RapidYencLibrary's size_t parameters and results are SizeT instead of Long.

### Bug Fixes

* pass size_t as a pointer-width SizeT so 32-bit ABIs work ([1d61dac](https://github.com/skjaere/rapidyenc-kotlin-wrapper/commit/1d61dac1ee8b0b5b25791b1d79a3c745140f2973))
* target JVM 17 and register the native library by class for Android ([f11a80d](https://github.com/skjaere/rapidyenc-kotlin-wrapper/commit/f11a80d914555e40563d7a835d60bb008bf9dd78))

## [0.3.0](https://github.com/skjaere/rapidyenc-kotlin-wrapper/compare/v0.2.0...v0.3.0) (2026-05-20)


### Features

* direct-buffer overloads for decodeIncremental and crc32

## [0.2.0](https://github.com/skjaere/rapidyenc-kotlin-wrapper/compare/v0.1.3...v0.2.0) (2026-04-25)


### Features

* bundle linux-aarch64/librapidyenc.so for arm64 hosts

## 0.1.0 (2026-02-09)


### Features

* first release


### Bug Fixes

* force first release as 0.1.0 via release-as
* trigger release-please on master instead of release
* use bump-minor-pre-major to prevent feat from bumping to 1.0.0
