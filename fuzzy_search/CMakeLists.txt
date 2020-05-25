cmake_minimum_required(VERSION 3.16)
project(fuzzy_search)

set(CMAKE_C_STANDARD 11)

if(NOT CMAKE_BUILD_TYPE)
    set(CMAKE_BUILD_TYPE Release)
endif()

set(CMAKE_C_FLAGS "-Wall -Wextra -Werror")
set(CMAKE_C_FLAGS_DEBUG "-DDEBUG -g ${CMAKE_CXX_FLAGS}")
set(CMAKE_C_FLAGS_RELEASE "-O3 ${CMAKE_CXX_FLAGS}")

if(NOT MYSQL_INCLUDE)
    set(MYSQL_INCLUDE "/usr/include/mysql")
endif()

add_library(lib_fuzzy_search SHARED min_edit_distance.c min_edit_distance.h)
target_include_directories(lib_fuzzy_search PUBLIC ${MYSQL_INCLUDE})
set_target_properties(lib_fuzzy_search PROPERTIES PREFIX "lib_")

add_executable(unit_test unit_test.c unit_test.c)
target_link_libraries(unit_test lib_fuzzy_search)