#include <errno.h>
#include <stdlib.h>

#include <jni.h>
#include <c_utils.h>
#include <string.h>

#include "ping.h"

inline jint throwSocketException(JNIEnv *env, char *msg) {
    jclass ex = (*env)->FindClass(env, SOCKET_EXCEPTION_CLASSNAME);

    return (*env)->ThrowNew(env, ex, msg);
}

int handleSocketError(JNIEnv *env, int socketfd) {
    if (socketfd == SOCKET_FAIL) {
        const char *err = strerror(errno);
        throwSocketException(env, err);

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}

int handleSendtoError(JNIEnv *env, int count) {
    if (count < 0) {
        const char *err = strerror(errno);
        throwSocketException(env, err);

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}

int handleRecvError(JNIEnv *env, int count, size_t recv_header_size) {
    if (count < 0) {
        const char *err = strerror(errno);
        throwSocketException(env, err);

        return EXIT_FAILURE;
    } else if (count < recv_header_size) {
        LOGF(E, 255, SOCKET_ERROR_FMT_SHORT_ICMP_REPLY, count, errno);

        throwSocketException(env, SOCKET_ERROR_SHORT_ICMP_PACKET);

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}

int handleICMPError(JNIEnv *env, struct icmphdr *header) {
    if (header->type != ICMP_ECHOREPLY) {
        throwSocketException(env, SOCKET_ERROR_INVALID_ICMP_PACKET);

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
