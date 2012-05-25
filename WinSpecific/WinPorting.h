#ifndef _WINPORTING_H_
#define _WINPORTING_H_


#define strtoll   _strtoi64
#define	strdup		_strdup
#define	mode_t		int
#define snprintf	_snprintf
#define	PATH_MAX	MAX_PATH
#define	fseeko		_fseeki64
#define ftello		_ftelli64
//#define socklen_t	int
#define in_addr_t	in_addr
#define	SHUT_RDWR	SD_BOTH
#define	HUGE		HUGE_VAL
#define O_NONBLOCK	0x4000
#define F_GETFL		3
#define	F_SETFL		4


#include <direct.h>
#include <Winsock2.h>

extern "C"
{
// Microsoft implementation of these structures has the 
// pointer and length in reversed positions.
typedef struct iovec
{
    ULONG       iov_len;
    char        *iov_base;
} iovec;

typedef struct msghdr
{
    sockaddr    *msg_name;
    int         msg_namelen;
    iovec       *msg_iov;
    ULONG       msg_iovlen;
    int         msg_controllen;
    char        *msg_control;
    ULONG       msg_flags;
} msghdr;

int inet_aton(const char *cp, struct in_addr *inp);

int	lcm_internal_pipe_create ( int filedes[2] );
size_t lcm_internal_pipe_write ( int fd, const void *buf, size_t count );
size_t lcm_internal_pipe_read ( int fd, void *buf, size_t count );
int	lcm_internal_pipe_close ( int fd );

int fcntl (int fd, int flag1, ...);

size_t recvmsg ( SOCKET s, struct msghdr *msg, int flags );
size_t sendmsg ( SOCKET s, const struct msghdr *msg, int flags );
}

#endif
