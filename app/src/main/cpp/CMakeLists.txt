cmake_minimum_required(VERSION 3.22.1)

project(mymedia_libtorrent)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(LIBTORRENT_ROOT "/Users/yangchenglin/Documents/bt/libtorrent-rasterbar-2.0.13" CACHE PATH "Official libtorrent source root" FORCE)
set(BOOST_ROOT "/Users/yangchenglin/Documents/bt/boost_1_85_0" CACHE PATH "Boost source root" FORCE)
set(Boost_INCLUDE_DIR "${BOOST_ROOT}" CACHE PATH "Boost headers" FORCE)

set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(build_tests OFF CACHE BOOL "" FORCE)
set(build_examples OFF CACHE BOOL "" FORCE)
set(build_tools OFF CACHE BOOL "" FORCE)
set(build_python_binding OFF CACHE BOOL "" FORCE)
set(build_webtorrent OFF CACHE BOOL "" FORCE)
set(logging OFF CACHE BOOL "" FORCE)
set(dht ON CACHE BOOL "" FORCE)
set(deprecated-functions OFF CACHE BOOL "" FORCE)
set(encryption ON CACHE BOOL "" FORCE)
set(openssl OFF CACHE BOOL "" FORCE)

add_library(mymedia_libtorrent SHARED
        native_libtorrent_jni.cpp
)

target_include_directories(mymedia_libtorrent PRIVATE ${LIBTORRENT_ROOT}/include)

if (EXISTS "${BOOST_ROOT}/boost/version.hpp")
    add_subdirectory(${LIBTORRENT_ROOT} ${CMAKE_CURRENT_BINARY_DIR}/libtorrent)
    target_compile_definitions(mymedia_libtorrent PRIVATE MYMEDIA_HAS_LIBTORRENT=1)
    target_link_libraries(mymedia_libtorrent
            PRIVATE
            torrent-rasterbar
            android
            log
    )
else()
    message(WARNING "Boost headers not found at ${BOOST_ROOT}; native libtorrent is disabled.")
    target_compile_definitions(mymedia_libtorrent PRIVATE MYMEDIA_HAS_LIBTORRENT=0)
    target_link_libraries(mymedia_libtorrent
            PRIVATE
            android
            log
    )
endif()
