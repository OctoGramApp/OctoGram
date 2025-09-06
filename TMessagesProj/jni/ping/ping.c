#include <assert.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>

#include <arpa/inet.h>
#include <netinet/in.h>
#include <netinet/ip_icmp.h>
#include <sys/socket.h>

#include <jni.h>
#include <c_utils.h>

#include "ping.h"

int getSocketFd(void) {
    LOGD("getSocketFd");

    const int fd = socket(AF_INET, SOCK_DGRAM, IPPROTO_ICMP);
    // The descriptor is invalid, do not try to use setsockopt() on it.
    if (fd <= 0) {
        return fd;
    }

    DEFTIMEVAL(read_timeout, PING_DEFTIMEOUT_SECONDS, 0);
    DEFTIMEVAL(send_timeout, PING_DEFTIMEOUT_SECONDS, 0);

    // We don't need non-blocking pipes for a ping, so setting the timeout this way instead of
    // using poll() is fine.

    const int so_sndto = setsockopt(
            fd,
            SOL_SOCKET,
            SO_SNDTIMEO,
            &send_timeout,
            sizeof send_timeout
    );
    assert(so_sndto == 0);

    const int so_rcvto = setsockopt(
            fd,
            SOL_SOCKET,
            SO_RCVTIMEO,
            &read_timeout,
            sizeof read_timeout
    );
    assert(so_rcvto == 0);

    return fd;
}

void setIP(JNIEnv *env, jstring *address, struct in_addr *destination) {
    LOGD("setIP");

    const char *ip = (*env)->GetStringUTFChars(env, *address, JNI_FALSE);

    LOGF(I, 255, "Pinging %s", ip);

    inet_aton(ip, destination);

    LOGD("inet_aton");

    (*env)->ReleaseStringUTFChars(env, *address, ip);
}

void setPingtoAddress(JNIEnv *env, jstring *address, struct sockaddr_in *pingto) {
    LOGD("setPingtoAddress");

    struct in_addr destination;
    {
        bzero(&destination, sizeof destination);

        setIP(env, address, &destination);
    }

    bzero(pingto, sizeof address);

    pingto->sin_family = AF_INET;
    pingto->sin_addr = destination;
}

void setICMPHeader(struct icmphdr *icmp_hdr) {
    LOGD("setICMPHeader");

    bzero(icmp_hdr, sizeof(*icmp_hdr));

    icmp_hdr->type = ICMP_ECHO;
    icmp_hdr->un.echo.id = (__be16) getpid();
}

extern JNIEXPORT jint JNICALL Java_it_octogram_android_utils_network_StandardHTTPRequest_ping(
        JNIEnv *env,
        __attribute__((unused)) jclass clazz,
        jstring address
) {
    const int socketfd = getSocketFd();

    if (handleSocketError(env, socketfd) == EXIT_FAILURE) {
        LOGF(E, 255, "socket(): %d", errno);

        return EXIT_FAILURE;
    }

    struct sockaddr_in pingto;
    setPingtoAddress(env, &address, &pingto);

    struct icmphdr icmp_hdr;
    setICMPHeader(&icmp_hdr);

    struct icmphdr recv_header;
    // Won't increase.
    icmp_hdr.un.echo.sequence = (__be16) 1;

    unsigned char buffer[PING_BUFFER_SIZE];
    {
        memcpy(buffer, &icmp_hdr, sizeof icmp_hdr);
        memcpy(buffer + sizeof icmp_hdr, PING_MSG, PING_MSG_SIZE);
    }

    LOGD("memcpy buffer");

    MEASURE(t0, t1, CLOCK_MONOTONIC,
            {
                const ssize_t sendto_count = sendto(
                        socketfd,
                        buffer,
                        sizeof icmp_hdr + PING_MSG_SIZE,
                        0,
                        (struct sockaddr *) &pingto,
                        sizeof pingto
                );

                if (handleSendtoError(env, sendto_count) == EXIT_FAILURE) {
                    close(socketfd);

                    LOGF(E, 255, "sendto(): %d", errno);

                    return EXIT_FAILURE;
                }

                LOGD("Sent ICMP");

                // We won't need the sender address.
                const socklen_t sender_size = 0;
                const int recv_count = recvfrom(socketfd, buffer, sizeof buffer, 0, NULL,
                                                &sender_size);
                if (handleRecvError(env, recv_count, sizeof recv_header) == EXIT_FAILURE) {
                    close(socketfd);

                    LOGF(E, 255, "recvfrom(): %d", errno);

                    return EXIT_FAILURE;
                }

                LOGD("recvfrom");

                memcpy(&recv_header, buffer, sizeof recv_header);

                if (handleICMPError(env, &recv_header) == EXIT_FAILURE) {
                    close(socketfd);

                    LOGF(E, 255, "Invalid ICMP header type %d", recv_header.type);

                    return EXIT_FAILURE;
                }

                LOGD("memcpy recv");

                LOGF(
                        V,
                        255,
                        "ICMP Reply, id=0x%x, sequence =  0x%x",
                        icmp_hdr.un.echo.id,
                        icmp_hdr.un.echo.sequence
                );
            });

    close(socketfd);

    int delta = (int) ((t1.tv_sec - t0.tv_sec) * 1000L + ((t1.tv_nsec - t0.tv_nsec) / 1000000L));

    LOGF(D, 255, "t0: %ul t1: %ul delta: %d", t0.tv_nsec, t1.tv_nsec, delta);

    return (jint) delta;
}
