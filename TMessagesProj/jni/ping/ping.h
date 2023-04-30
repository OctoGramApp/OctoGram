#pragma once

#include <time.h>

#include <netinet/ip_icmp.h>

#define LOG_TAG "octogram/native/ping"

#define PING_MSG "Echo Hello 12345\0"
#define PING_MSG_SIZE strlen(PING_MSG) + 1
#define PING_BUFFER_SIZE 2048

#define PING_DEFTIMEOUT_SECONDS 2

#define SOCKET_FAIL -1

#define SOCKET_EXCEPTION_CLASSNAME "java/net/SocketException"

#define SOCKET_ERROR_SHORT_ICMP_PACKET "ICMP packet too short"
#define SOCKET_ERROR_INVALID_ICMP_PACKET "Invalid ICMP packet"

#define SOCKET_ERROR_FMT_SHORT_ICMP_REPLY "Error, got short ICMP packet, %d bytes %d"

#define DEFTIMEVAL(name, s, u) struct timeval name = { .tv_sec = s, .tv_usec = u }

#define MEASURE(t0, t1, clock_type, block) \
    struct timespec t0; \
    clock_gettime(clock_type, &t0);\
    { \
        block \
    } \
    struct timespec t1; \
    clock_gettime(clock_type, &t1)

#ifndef LOG_DISABLED
#define LOGF(level, size, fmt, ...) \
    { \
        const char buffer[size]; \
        sprintf(buffer, fmt, __VA_ARGS__); \
        LOG##level(buffer); \
    } NULL
#else
#define LOGF(level, size, fmt, ...) NULL
#endif

inline jint throwSocketException(JNIEnv *env, char *msg);

int handleSocketError(JNIEnv *env, int socketfd);
int handleSendtoError(JNIEnv *env, int count);
int handleRecvError(JNIEnv *env, int count, size_t recv_header_size);
int handleICMPError(JNIEnv *env, struct icmphdr *header);
