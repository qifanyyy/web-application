cmake_minimum_required(VERSION 3.16)
project(fuzzy_search)

set(CMAKE_CXX_STANDARD 17)

add_library(fuzzy_search SHARED edit_distance.cpp edit_distance.hpp)
add_executable(main main.cpp)
target_link_libraries(main fuzzy_search)
