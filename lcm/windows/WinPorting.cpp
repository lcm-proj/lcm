
#define		_WIN32_WINNT		0x0501
#include	<winsock2.h>
#include	<Mswsock.h>
#include	<stdio.h>

#include	"WinPorting.h"

#define		ARBITRARY_START_PORT		10000
//	The following data allows multiple processes to access the LastSocket variable
//	and always choose a valid new socket to use a UNIX pipe() emulation
#pragma	data_seg ( ".winlcm" )
volatile LONG	LastSocket = ARBITRARY_START_PORT;
#pragma data_seg ()

extern "C"
{
GUID WSARecvMsg_GUID = WSAID_WSARECVMSG;
LPFN_WSARECVMSG WSARecvMsg = NULL;


int inet_aton(const char *cp, struct in_addr *inp)
{
	in_addr a;
	a.S_un.S_addr = inet_addr ( cp );
	if ( a.S_un.S_addr == INADDR_NONE )
		return 0;
	*inp = a;
	return a.S_un.S_addr;
}

int lcm_internal_pipe_create ( int filedes[2] )
{
int				status, SocOpt, rdyCount, nPort;
short			Port;
fd_set          rSSet, wSSet;
SOCKET			listSoc, sendSoc, recvSoc;
sockaddr_in     list_addr, recv_addr;
timeval			tv;

	listSoc = socket ( PF_INET, SOCK_STREAM, 0 );
	recvSoc = socket ( PF_INET, SOCK_STREAM, 0 );

    list_addr.sin_family = AF_INET;
	list_addr.sin_addr.s_addr = inet_addr("127.0.0.1");

//	This loop ensures that we pick up an UNUSED port. If anything else used this port,
//	the entire lcm notification system melts down. The assumption is that we can't bind
//	to an address in use once the SO_EXCLUSIVEADDRUSE has been set. If this isn't true, 
//	another method will need to be implemented.
	do
	{
		nPort = InterlockedIncrement ( &LastSocket );		// Make sure we're using unique port
		if ( nPort > 65500 )								// Wrapping, reset the port #
		{
			InterlockedCompareExchange ( &LastSocket, ARBITRARY_START_PORT, nPort );
		}
		Port = (short) nPort;
		list_addr.sin_port = htons(Port);

		SocOpt = 1;
		status = setsockopt ( listSoc, SOL_SOCKET, SO_EXCLUSIVEADDRUSE,
							  (const char *) &SocOpt,
							  sizeof(SocOpt) );
		if ( status )
			continue;

		status = bind ( listSoc, (LPSOCKADDR) &list_addr, sizeof (list_addr) );

	} while ( status != 0 );

    SocOpt = 1;
    status = ioctlsocket ( listSoc, FIONBIO, (u_long *) &SocOpt );

	status = listen ( listSoc, 1 );

    recv_addr.sin_family = AF_INET;
	recv_addr.sin_addr.s_addr = inet_addr("127.0.0.1");
    recv_addr.sin_port = htons(Port);

    SocOpt = 1;
    status = ioctlsocket ( recvSoc, FIONBIO, (u_long *) &SocOpt );

	status = connect ( recvSoc, (LPSOCKADDR) &recv_addr, sizeof(recv_addr) );

	rdyCount = 0;
	tv.tv_sec = 0;
	tv.tv_usec = 20 * 1000;
	while ( rdyCount < 2 )
	{
		rSSet.fd_count = 1;
		rSSet.fd_array[0] = recvSoc;
		rSSet.fd_array[1] = INVALID_SOCKET;
		wSSet.fd_count = 1;
		wSSet.fd_array[0] = listSoc;
		wSSet.fd_array[1] = INVALID_SOCKET;
		rdyCount = select ( 0, &wSSet, &rSSet, NULL, &tv );
	}
//	Both sockets are ready now to complete the connection.

	sendSoc = accept(listSoc, (LPSOCKADDR) &list_addr, NULL);
	closesocket ( listSoc );	// This port is now in use - clear listener

//	Restore the sockets to blocking (default behavior).

    SocOpt = 0;
    status = ioctlsocket ( recvSoc, FIONBIO, (u_long *) &SocOpt );
    status = ioctlsocket ( sendSoc, FIONBIO, (u_long *) &SocOpt );

	filedes[0] = (int) recvSoc;
	filedes[1] = (int) sendSoc;

	return 0;
}

size_t lcm_internal_pipe_write ( int fd, const void *buf, size_t count )
{
int		status;

	status = send ( (SOCKET) fd, (char *) buf, (int) count, 0 );
	return (size_t) status;
}

size_t lcm_internal_pipe_read ( int fd, void *buf, size_t count )
{
int			status;

	status = recv ( (SOCKET) fd, (char *) buf, int (count), 0 );
	return status;
}

int	lcm_internal_pipe_close ( int fd )
{
	return closesocket ( (SOCKET) fd );
}

int fcntl (int fd, int flag1, ... )			// Fake out F_GETFL, set socket to block or not on F_SETFL
{
int	status, flag2, SocOpt;

	if ( flag1 == F_GETFL )
		return 0;
	if ( flag1 == F_SETFL )
	{
		va_list marker;
		va_start( marker, flag1 );
		flag2 = va_arg( marker, int);
		va_end( marker );

		if ( flag2 == -1 )
			return 0;					// No 3rd arg? Just exit
		if ( (flag2 & O_NONBLOCK) != 0 )		// Non blocking mode
		{
			SocOpt = 1;
			status = ioctlsocket ( (SOCKET) fd, FIONBIO, (u_long *) &SocOpt );
			return status;
		}
		else					// Must have wanted blocking mode
		{	
			SocOpt = 0;
			status = ioctlsocket ( (SOCKET) fd, FIONBIO, (u_long *) &SocOpt );
			return status;
		}
	}
	return 0;
}

size_t recvmsg ( SOCKET s, struct msghdr *msg, int flags )
{
DWORD		nRead, status;

	WSAMSG tmp_wsamsg;
	tmp_wsamsg.name = msg->msg_name;
	tmp_wsamsg.namelen = msg->msg_namelen;
	tmp_wsamsg.lpBuffers = (LPWSABUF) msg->msg_iov;
	tmp_wsamsg.dwBufferCount = msg->msg_iovlen;
	tmp_wsamsg.Control.len = msg->msg_controllen;
	tmp_wsamsg.Control.buf = msg->msg_control;
	tmp_wsamsg.dwFlags = msg->msg_flags;

	if ( WSARecvMsg == NULL )
	{
		status = WSAIoctl ( (SOCKET) s, SIO_GET_EXTENSION_FUNCTION_POINTER,
							&WSARecvMsg_GUID, sizeof WSARecvMsg_GUID,
							&WSARecvMsg, sizeof WSARecvMsg,
							&nRead, NULL, NULL);
		if (status == SOCKET_ERROR)
		{
			errno = WSAGetLastError();
			WSARecvMsg = NULL;
			return -1;
		}
	}
	if ( WSARecvMsg != NULL )
	{
		int status = WSARecvMsg ( (SOCKET) s, &tmp_wsamsg, &nRead, NULL, NULL );
		if ( status != 0 )
		{
			errno = WSAGetLastError();
			return -1;
		}
		return nRead;
	}
	return -1;
}

size_t sendmsg ( SOCKET s, const struct msghdr *msg, int flags )
{
	DWORD		nWritten, status;
	status = WSASendTo ( (SOCKET) s, (WSABUF *) msg->msg_iov, msg->msg_iovlen, 
		&nWritten, 0, msg->msg_name, msg->msg_namelen, NULL, NULL);
	if ( status != 0 )
	{
		return -1;
	}
	return nWritten;
}

int gettimeofday(struct timeval *tv, struct timezone *tz)		// tz is not used in any lcm code
{

_int64          ftUNIXTime, ftNow, secNow, usec;
SYSTEMTIME      UNIXStart;

    UNIXStart.wYear = 1970;
    UNIXStart.wMonth = 1;
    UNIXStart.wDay = 1;
    UNIXStart.wHour = 0;
    UNIXStart.wMinute = 0;
    UNIXStart.wSecond = 0;
    UNIXStart.wMilliseconds = 0;

    BOOL Did = SystemTimeToFileTime ( &UNIXStart, (FILETIME *) &ftUNIXTime );

	GetSystemTimeAsFileTime ( (LPFILETIME) &ftNow );
	ftNow -= ftUNIXTime;

	ftNow /= 10;				// Convert to useconds
	secNow = ftNow / 1000000;	// Get seconds
	usec = ftNow - (secNow * 1000000);

	tv -> tv_sec = (int) secNow;
	tv -> tv_usec = (int) usec;
	return 0;
}

BOOL	winFileExists( char *filename )
{
DWORD       fileAttr;

    fileAttr = GetFileAttributes ( filename );
    if ( 0xFFFFFFFF == fileAttr )
        return false;
    return true;
}


}
