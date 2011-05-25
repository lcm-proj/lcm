/*
 *  lcmlite_ios.c
 *  GLPad
 *
 *  Created by Edwin Olson on 8/15/10.
 *  Copyright 2010 Edwin Olson. All rights reserved.
 *
 */

#include "lcmlite_ios.h"

/** Demonstrates the use of LCM-Lite on iOS systems. Heavily based on POSIX port. **/

// needed for MACOS and FreeBSD
#define USE_REUSEPORT

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

void lcmlite_impl_transmit_packet(const void *_buf, int buf_len, void *user)
{
    struct lcmlite_impl *tinfo = (struct lcmlite_impl*) user;

    ssize_t res = sendto(tinfo->send_fd, _buf, buf_len, 0, (struct sockaddr*) &tinfo->send_addr, sizeof(tinfo->send_addr));
    if (res < 0)
        perror("transmit_packet: sendto");
}

int lcmlite_impl_init(struct lcmlite_impl *impl)
{
    uint8_t mc_ttl = 0;
    struct in_addr mc_addr;
    int mc_port = htons(7667);

    if (inet_aton("239.255.76.67", (struct in_addr*) &mc_addr) < 0)
        return 1;

    // create the Multicast UDP socket
    memset(&impl->read_addr, 0, sizeof(impl->read_addr));
	impl->read_addr.sin_family = AF_INET;
	impl->read_addr.sin_addr.s_addr = INADDR_ANY;
    impl->read_addr.sin_port = mc_port;

    memset(&impl->send_addr, 0, sizeof(impl->send_addr));
    impl->send_addr.sin_family = AF_INET;
    impl->send_addr.sin_addr = mc_addr;
    impl->send_addr.sin_port = mc_port;

    impl->read_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (impl->read_fd < 0) {
        perror("socket()");
        return 1;
    }

    int opt = 1;
    if (setsockopt(impl->read_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("setsockopt(SOL_SOCKET, SO_REUSEADDR)");
        return 1;
    }

#ifdef USE_REUSEPORT
    if (setsockopt(impl->read_fd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("setsockopt(SOL_SOCKET, SO_REUSEPORT)");
        return 1;
    }
#endif

    // join the multicast group
    struct ip_mreq mreq;
    mreq.imr_multiaddr = mc_addr;
    mreq.imr_interface.s_addr = INADDR_ANY;

    if (setsockopt(impl->read_fd, IPPROTO_IP, IP_ADD_MEMBERSHIP, &mreq, sizeof(mreq)) < 0) {
        perror ("setsockopt (IPPROTO_IP, IP_ADD_MEMBERSHIP)");
        return -1;
    }

    if (bind(impl->read_fd, (struct sockaddr*) &impl->read_addr, sizeof(impl->read_addr)) < 0) {
        perror("bind");
        return -1;
    }

    if (setsockopt(impl->read_fd, IPPROTO_IP, IP_MULTICAST_TTL, &mc_ttl, sizeof(mc_ttl)) < 0) {
        perror("setsockopt(IPPROTO_IP, IP_MULTICAST_TTL)");
        return 1;
    }

	impl->send_fd = socket(AF_INET, SOCK_DGRAM, 0);

	return 0;
}

void lcmlite_impl_readloop(lcmlite_t *lcm, struct lcmlite_impl *impl)
{
    // read packets, pass them to LCM
    while (1) {
        char buf[65536];
        struct sockaddr_in from_addr; // only IPv4 compatible
        socklen_t from_addr_sz = sizeof(from_addr);

        ssize_t buf_len = recvfrom(impl->read_fd, buf, sizeof(buf), 0, (struct sockaddr*) &from_addr, &from_addr_sz);
        assert(from_addr_sz == sizeof(struct sockaddr_in));

        int res = lcmlite_receive_packet(lcm,
                                         buf,
                                         buf_len,
                                         from_addr.sin_addr.s_addr | ((uint64_t) from_addr.sin_port << 32));
        if (res < 0)
            printf("ERR %d\n", res);
    }
}
