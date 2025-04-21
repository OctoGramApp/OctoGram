set(CMAKE_C_FLAGS "-w -std=c11 -DANDROID -D_LARGEFILE_SOURCE=1 -g")

add_library(ping SHARED
        ping/ping.c
        ping/ping_error.c)

target_link_libraries(ping
        android
        tgcalls
        log)

target_compile_options(ping PUBLIC
        -Wall -Wextra -finline-functions -fno-strict-aliasing -Ofast -frtti -Wno-unknown-pragmas -funroll-loops -fno-math-errno)

set_target_properties(ping PROPERTIES
        ANDROID_ARM_MODE arm)

target_include_directories(ping PUBLIC
        ./)