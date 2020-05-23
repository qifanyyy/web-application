# fuzzy_search

## Build

You may use CLion to view, edit, build and run this C++ project.

To build it from command line, make sure you have a modern C++
compiler (we use Clang 10) and CMake (3.16 or newer) installed.

Then, make sure that your current directory contains this `README.md`
file.

Create a build directory named `cmake-build` in side current
working directory.

```shell script
mkdir cmake-build
```

Go to `cmake-build` and start CMake.

```shell script
cd cmake-build
cmake ..
```

Finally, build the library.

```shell script
make
```

You can find `libfuzzy_search.so` (`libfuzzy_search.dylib`
on macOS) inside `cmake-build` if build process succeeded.

## Clean

Just remove the `cmake-build` directory.
