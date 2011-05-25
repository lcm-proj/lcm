
/** Demonstrates the use of LCM-Lite on POSIX systems **/

// needed for MACOS and FreeBSD
// #define USE_REUSEPORT

#include <stdint.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <assert.h>

#include "lcmlite.h"

struct transmit_info
{
    struct sockaddr_in send_addr;
    int send_fd;
};

void transmit_packet(const void *_buf, int buf_len, void *user)
{
    struct transmit_info *tinfo = (struct transmit_info*) user;

    ssize_t res = sendto(tinfo->send_fd, _buf, buf_len, 0, (struct sockaddr*) &tinfo->send_addr, sizeof(tinfo->send_addr));
    if (res < 0)
        perror("transmit_packet: sendto");
}

void abc_callback(lcmlite_t *lcm, const char *channel, const void *buf, int buf_len, void *user)
{
    int8_t v = 17;
    printf("%d\n", buf_len);

    for (int i = 0; i < buf_len; i++) {
        if (((char*) buf)[i] != v) {
            printf("ERROR!\n");
            exit(1);
        }
        v += 113;
    }

    printf("Got abc!\n");

    lcmlite_publish(lcm, "ECHO", buf, buf_len);
}

int main(int argc, char *argv[])
{
    uint8_t mc_ttl = 0;
    struct in_addr mc_addr;
    int mc_port = htons(7667);

    if (inet_aton("239.255.76.67", (struct in_addr*) &mc_addr) < 0)
        return 1;

    // create the Multicast UDP socket
    struct sockaddr_in read_addr, send_addr;

    memset(&read_addr, 0, sizeof(read_addr));
    read_addr.sin_family = AF_INET;
    read_addr.sin_addr.s_addr = INADDR_ANY;
    read_addr.sin_port = mc_port;

    memset(&send_addr, 0, sizeof(read_addr));
    send_addr.sin_family = AF_INET;
    send_addr.sin_addr = mc_addr;
    send_addr.sin_port = mc_port;

    int read_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (read_fd < 0) {
        perror("socket()");
        return 1;
    }

    int opt = 1;
    if (setsockopt(read_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("setsockopt(SOL_SOCKET, SO_REUSEADDR)");
        return 1;
    }

    #ifdef USE_REUSEPORT
    if (setsockopt(read_fd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("setsockopt(SOL_SOCKET, SO_REUSEPORT)");
        return 1;
    }
    #endif

    // join the multicast group
    struct ip_mreq mreq;
    mreq.imr_multiaddr = mc_addr;
    mreq.imr_interface.s_addr = INADDR_ANY;

    if (setsockopt(read_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)) < 0) {
        perror ("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        return -1;
    }

    if (bind(read_fd, (struct sockaddr*) &read_addr, sizeof(read_addr)) < 0) {
        perror("bind");
        return -1;
    }

    int send_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (setsockopt(read_fd, IPPROTO_IP, IP_MULTICAST_TTL, &mc_ttl, sizeof(mc_ttl)) < 0) {
        perror("setsockopt(IPPROTO_IP, IP_MULTICAST_TTL)");
        return 1;
    }

    ////////////////////////////////////////////////////////////////
    lcmlite_t lcm;
    struct transmit_info tinfo;
    tinfo.send_addr = send_addr;
    tinfo.send_fd = send_fd;
    lcmlite_init(&lcm, transmit_packet, &tinfo);

    // subscribe to LCM messages
    lcmlite_subscription_t sub1;
    sub1.channel = "FOOBAR";
    sub1.callback = abc_callback;
    sub1.user = NULL;

    lcmlite_subscribe(&lcm, &sub1);

    // read packets, pass them to LCM
    while (1) {
        char buf[65536];
        struct sockaddr_in from_addr; // only IPv4 compatible
        int from_addr_sz = sizeof(from_addr);

        ssize_t buf_len = recvfrom(read_fd, buf, sizeof(buf), 0, (struct sockaddr*) &from_addr, &from_addr_sz);
        assert(from_addr_sz == sizeof(struct sockaddr_in));

        int res = lcmlite_receive_packet(&lcm,
                                         buf,
                                         buf_len,
                                         from_addr.sin_addr.s_addr | ((uint64_t) from_addr.sin_port << 32));
        if (res < 0)
            printf("ERR %d\n", res);
    }
}
