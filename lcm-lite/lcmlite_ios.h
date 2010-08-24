/*
 *  lcmlite_ios.h
 *  GLPad
 *
 *  Created by Edwin Olson on 8/15/10.
 *  Copyright 2010 Edwin Olson. All rights reserved.
 *
 */

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

#ifndef _LCMLITE_IMPL_H
#define _LCMLITE_IMPL_H

struct lcmlite_impl
{
	struct sockaddr_in read_addr;
    struct sockaddr_in send_addr;
    int send_fd, read_fd;
};

int lcmlite_impl_init(struct lcmlite_impl *impl);

void lcmlite_impl_readloop(lcmlite_t *lcm, struct lcmlite_impl *impl);

void lcmlite_impl_transmit_packet(const void *_buf, int buf_len, void *user);

#endif