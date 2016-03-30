AC_PREREQ(2.50)
AC_INIT([lcm], 1.3.1)
AC_CONFIG_SRCDIR([lcm/lcm.h])
AC_CANONICAL_SYSTEM
AM_INIT_AUTOMAKE([foreign tar-ustar])
AC_CONFIG_MACRO_DIR([m4])

AC_PROG_CC
PKG_CHECK_MODULES(GLIB,[glib-2.0 >= 2.0.0 gthread-2.0 >= 2.0.0],,[AC_MSG_ERROR([LCM requires GLib >= 2.0])])
AC_PATH_PROGS(JAR, [jar])
AC_PATH_PROGS(JAVAC, [javac])

dnl enable silent build if possible (automake >= 1.11)
m4_ifdef([AM_SILENT_RULES], [AM_SILENT_RULES([yes])])

dnl ---------------------------------------------------------------------------
dnl When making a release:
dnl  1. If the library source code has changed at all since the last release,
dnl     then increment REVISION.
dnl  2. If the interface has been changed since the last release, increment
dnl     CURRENT, and set REVISION to 0.
dnl  3. If the interface changes consist solely of additions, increment AGE.
dnl  4. If the interface has removed or changed elements, set AGE to 0.
dnl ---------------------------------------------------------------------------
LIBLCM_AGE=3
LIBLCM_REVISION=4
LIBLCM_CURRENT=4
CHART2D_VERSION=3.2.2
AC_SUBST(LIBLCM_AGE)
AC_SUBST(LIBLCM_REVISION)
AC_SUBST(LIBLCM_CURRENT)
AC_SUBST(CHART2D_VERSION)
LIBLCM_VERSION_INFO=$LIBLCM_CURRENT:$LIBLCM_REVISION:$LIBLCM_AGE
AC_SUBST(LIBLCM_VERSION_INFO)

AM_DISABLE_STATIC
AM_PROG_LIBTOOL

# link against the socket and nsl libraries if needed
LIB_SOCKET_NSL

# nanosleep might need special linkage
AC_SEARCH_LIBS([nanosleep], [rt])

# inet_aton might need special linkage
AC_SEARCH_LIBS([inet_aton], [resolv])

dnl ------------------
dnl Python support
dnl ------------------
AC_ARG_WITH(python,
            [AS_HELP_STRING([--without-python],
             [Do not compile lcm-python bindings even if Python is available])],
            [], [with_python=yes])

AS_IF([test "x$with_python" != xno],
      [AM_PATH_PYTHON(2.3)
       AM_CHECK_PYTHON_HEADERS(with_python=yes,with_python=no)])
AM_CONDITIONAL(HAVE_PYTHON, test "x$with_python" = "xyes")

pylcmexecdir="$pyexecdir/$PACKAGE"
AC_SUBST(pylcmexecdir)

dnl ------------------
dnl Java support
dnl ------------------
AC_ARG_WITH(java,
            [AS_HELP_STRING([--without-java],
                [Do not compile lcm-java bindings even if Java is available])],
            [], [with_java=yes])
if test "x$with_java" = xno -o "x$JAR" = x -o "x$JAVAC" = x; then
    AC_MSG_WARN([Java support disabled])
    with_java="no"
fi

AM_CONDITIONAL(HAVE_JAVA, test "x$with_java" = "xyes")
java_apiversion="1.0"
AC_ARG_WITH(jardir,
            [AS_HELP_STRING([--with-jardir=DIR],
                [Installation directory for lcm.jar (default=DATADIR/java)].  Ignored if Java is not enabled.)],
            [jardir="$with_jardir"], [jardir=$datadir/java])

AC_SUBST(jardir)
INSTALLED_CLASSPATH=\${jardir}/lcm.jar
AC_SUBST(INSTALLED_CLASSPATH)
AC_SUBST(java_apiversion)

dnl ---------------
dnl Lua support
dnl ---------------
AC_ARG_WITH(lua,
            [AS_HELP_STRING([--without-lua],
                [Do not compile lcm-lua bindings even if Lua is available])],
            [], [with_lua=yes])

AS_IF([test "x$with_lua" != 'xno'],
  [ AX_PROG_LUA([5.1], [], [lua_interp='yes'], [lua_interp='no'])

    AS_IF([test "x$lua_interp" = 'xyes'],
      [ AX_LUA_HEADERS([lua_headers='yes'], [lua_headers='no'])
        AX_LUA_LIBS([lua_libs='yes'], [lua_libs='no'])
      ])

    AS_IF([test "x$lua_interp" = 'xyes' &&
           test "x$lua_headers" = 'xyes' &&
           test "x$lua_libs" = 'xyes'],
      [with_lua='yes'],
      [with_lua='no'])
  ])

AM_CONDITIONAL(HAVE_LUA, test "x$with_lua" = "xyes")

dnl ---------------------------------------------------------------------------
dnl Warnings
dnl ---------------------------------------------------------------------------
if test "x$GCC" = "xyes"; then
    AM_CFLAGS="$AM_CFLAGS -g -std=gnu99 -Wall -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE -D_REENTRANT -Wno-unused-parameter -Wno-format-zero-length -Wshadow"
    AC_SUBST(AM_CFLAGS)
fi

AC_OUTPUT([
  Makefile
  lcm/Makefile
  lcm/lcm.pc
  liblcm-test/Makefile
  lcm-logger/Makefile
  lcmgen/Makefile
  lcm-java/Makefile
  lcm-java/lcm-java.pc
  lcm-python/Makefile
  lcm-lua/Makefile
  examples/Makefile
  m4/Makefile
  m4macros/Makefile
])

if test "x$with_java" = "xyes"; then
    JAVA_STR="Enabled"
else
    JAVA_STR="Disabled"
fi

if test "$with_python" = "yes"; then
    PYTHON_STR="Enabled"
else
    PYTHON_STR="Disabled"
fi

if test "$with_lua" = "yes"; then
    LUA_STR="Enabled"
else
    LUA_STR="Disabled"
fi

echo "
"

echo "Configuration (LCM v${PACKAGE_VERSION}):

  Source code location:  ${srcdir}
  Compiler:              ${CC}
  Java Support:          ${JAVA_STR}
  Python Support:        ${PYTHON_STR}
  Lua Support:           ${LUA_STR}
"
