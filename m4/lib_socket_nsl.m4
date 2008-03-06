##### http://autoconf-archive.cryp.to/lib_socket_nsl.html
#
# SYNOPSIS
#
#   LIB_SOCKET_NSL
#
# DESCRIPTION
#
#   This macro figures out what libraries are required on this platform
#   to link sockets programs.
#
#   The common cases are not to need any extra libraries, or to need
#   -lsocket and -lnsl. We need to avoid linking with libnsl unless we
#   need it, though, since on some OSes where it isn't necessary it
#   will totally break networking. Unisys also includes gethostbyname()
#   in libsocket but needs libnsl for socket().
#
# LAST MODIFICATION
#
#   2005-09-06
#
# COPYLEFT
#
#   Copyright (c) 2005 Russ Allbery <rra@stanford.edu>
#   Copyright (c) 2005 Stepan Kasal <kasal@ucw.cz>
#   Copyright (c) 2005 Warren Young <warren@etr-usa.com>
#
#   Copying and distribution of this file, with or without
#   modification, are permitted in any medium without royalty provided
#   the copyright notice and this notice are preserved.

AC_DEFUN([LIB_SOCKET_NSL],
[
	AC_SEARCH_LIBS([gethostbyname], [nsl])
	AC_SEARCH_LIBS([socket], [socket], [], [
		AC_CHECK_LIB([socket], [socket], [LIBS="-lsocket -lnsl $LIBS"],
		[], [-lnsl])])
])
