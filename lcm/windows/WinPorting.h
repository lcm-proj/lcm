#ifndef _WINPORTING_H_
#define _WINPORTING_H_

#ifndef mode_t
#define mode_t int
#endif

#ifndef PATH_MAX
#define PATH_MAX MAX_PATH
#endif

#define O_NONBLOCK 0x4000
#define F_GETFL 3
#define F_SETFL 4

#include <Winsock2.h>
#include <direct.h>

#ifdef __cplusplus
extern "C" {
#endif

// Microsoft implementation of these structures has the
// pointer and length in reversed positions.
typedef struct iovec {
    ULONG iov_len;
    char *iov_base;
} iovec;

typedef struct msghdr {
    struct sockaddr *msg_name;
    int msg_namelen;
    iovec *msg_iov;
    ULONG msg_iovlen;
    int msg_controllen;
    char *msg_control;
    ULONG msg_flags;
} msghdr;

int inet_aton(const char *cp, struct in_addr *inp);

int __declspec(dllexport) lcm_internal_pipe_create(int filedes[2]);
size_t __declspec(dllexport) lcm_internal_pipe_write(int fd, const void *buf, size_t count);
size_t __declspec(dllexport) lcm_internal_pipe_read(int fd, void *buf, size_t count);
int __declspec(dllexport) lcm_internal_pipe_close(int fd);

int __declspec(dllexport) fcntl(int fd, int flag1, ...);

size_t recvmsg(SOCKET s, struct msghdr *msg, int flags);
size_t sendmsg(SOCKET s, const struct msghdr *msg, int flags);

#ifdef __cplusplus
}
#endif

#endif
