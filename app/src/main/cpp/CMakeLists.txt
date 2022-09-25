cmake_minimum_required(VERSION 3.6.0)

project(LogReader)

add_library(logreader
        SHARED

        ../jni/com_sburov_logparser_data_local_logreader_LogReaderImpl.cpp
        logreader/LogReader.cpp)

# Specifies a path to native header files.
include_directories(
        logreader
        ../jni)

target_link_libraries(
        logreader)
