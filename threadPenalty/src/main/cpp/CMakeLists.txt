cmake_minimum_required(VERSION 3.6)

project(cppCode)

include_directories(./)

# add_library(cppcode SHARED
add_library(cppcode SHARED
        cppcode.cpp
        threadPenalty.cpp
        plog/plog.cpp
        plogs/plogs.cpp
        plogs/plogfile.cpp
        plogJavaBridge.cpp
        plogTester.cpp)

# Include libraries needed for cppcode lib
target_link_libraries(cppcode
        android
        log)

target_link_options(cppcode PRIVATE "-Wl,-z,max-page-size=16384")

# NOTE(oleksii)
#   The line below contains the proper way of specifying the version of the C++ standard.
#   Unfortunately, that's not supported by the CMake distribution, shipped with Android SDK.
#
#       target_compile_features(${PROJECT_NAME} PRIVATE cxx_std_17)
#
#   That's why we'll explicitly specify compiler flags instead:
# NOTE(dennis)
#   Specifing compiler settings in build.gradle works, so not required here.
## set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++17")
## set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -frtti")
## set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions")
