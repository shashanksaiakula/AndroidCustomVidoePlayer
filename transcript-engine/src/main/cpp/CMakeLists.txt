cmake_minimum_required(VERSION 3.22.1)

project("transcript_engine")

# =========================
# BUILD TYPE
# =========================
set(CMAKE_BUILD_TYPE Release)

# =========================
# C++ VERSION
# =========================
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_C_STANDARD 11)

# =========================
# WHISPER / GGML DEFINES
# =========================
add_definitions(
        -DGGML_USE_K_QUANTS
)

# =========================
# PERFORMANCE OPTIMIZATION
# =========================
set(CMAKE_C_FLAGS_RELEASE "-O3 -DNDEBUG")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG")

# ARM64 Optimization
add_compile_options(
        -O3
        -ffast-math
        -funroll-loops
)

# =========================
# INCLUDE DIRECTORIES
# =========================
include_directories(
        ${CMAKE_SOURCE_DIR}
)

# =========================
# CREATE LIBRARY
# =========================
add_library(
        whisper
        SHARED

        bridge.cpp

        whisper.cpp

        ggml.c
        ggml-alloc.c
        ggml-backend.c
        ggml-quants.c
)

# =========================
# ANDROID LOG LIB
# =========================
find_library(
        log-lib
        log
)

# =========================
# LINK LIBRARIES
# =========================
target_link_libraries(
        whisper

        android
        ${log-lib}
)