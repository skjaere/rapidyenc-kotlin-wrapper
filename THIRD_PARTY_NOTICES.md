# Third-party notices

This repository's own code is under the PolyForm Noncommercial License 1.0.0 (see [LICENSE.md](LICENSE.md)). The native libraries it bundles, `src/main/resources/linux-x86-64/librapidyenc.so` and `src/main/resources/linux-aarch64/librapidyenc.so`, are built from the third-party code below, which keeps its own licence.

## rapidyenc

- What: the yEnc encoder, decoder and CRC32 code that `librapidyenc.so` is built from.
- Where from: <https://github.com/animetosho/rapidyenc>, by animetosho.
- Licence: public domain, or CC0 1.0 where public domain isn't recognised (SPDX `CC0-1.0`, <https://creativecommons.org/publicdomain/zero/1.0/legalcode>).

## crcutil 1.0

- What: Google's CRC library, which rapidyenc carries in `crcutil-1.0/` and compiles in for its CRC32 calculation.
- Where from: <https://code.google.com/archive/p/crcutil/>, as included in rapidyenc.
- Copyright 2010 Google Inc. All rights reserved.
- Licence: Apache License 2.0 (SPDX `Apache-2.0`). The full text is in [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt).

## zlib-ng's PCLMUL CRC32 folding

- What: rapidyenc's CRC32 by folding with carry-less multiplication (`src/crc_folding.cc`, and `src/crc_folding_256.cc` built on it), taken from zlib-ng, which has it from Intel's zlib patch.
- Where from: <https://github.com/zlib-ng/zlib-ng>, as included in rapidyenc.
- Copyright (C) 2013 Intel Corporation. All rights reserved.
- Licence: the zlib licence (SPDX `Zlib`). Its text, from zlib-ng's `LICENSE.md`:

```text
(C) 1995-2024 Jean-loup Gailly and Mark Adler

This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software. If you use this software
   in a product, an acknowledgment in the product documentation would be
   appreciated but is not required.

2. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.

3. This notice may not be removed or altered from any source distribution.
```

## Hedley

- What: the portability macros header `src/hedley.h` that rapidyenc includes.
- Where from: <https://nemequ.github.io/hedley>, as included in rapidyenc.
- Licence: CC0 1.0 (SPDX `CC0-1.0`, <https://creativecommons.org/publicdomain/zero/1.0/>).
