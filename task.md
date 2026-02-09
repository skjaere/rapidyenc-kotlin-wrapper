# Yenc wrapper
We need to create a wrapper for a yenc encoder/decoder c library using jna.
The c libraryi repository is located at https://github.com/animetosho/rapidyenc
it exists locally at /home/william/IdeaProjects/rapidyenc

Requirements:
- Use kotlin 2.3
- Usa JNA
- Create a static class that exposes all the functions in headers.h
- There must not be any memory leaks
- it must be as performant as possible
- it must be an importable library
  - package name: io.skjaere.yenc
- it must have test coverage
- it should use idiomatic kotlin



