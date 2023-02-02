cmake_minimum_required(VERSION 3.7 FATAL_ERROR)
project(fork VERSION 1.0.0 LANGUAGES C)
aux_source_directory(. DIR_SRCS)
add_executable(fork fork.c)
