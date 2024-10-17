cmake_minimum_required (VERSION 3.29.0) # cmake version

project(fork) # project name

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/jniLibs/$ENV{ABI}) # set output path

aux_source_directory(${PROJECT_SOURCE_DIR}/../src SRC_FILES) # scan source code files

add_executable(fork ${SRC_FILES}) # add source code files to library, and set build type to dynamic library.
