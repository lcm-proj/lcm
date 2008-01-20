# Configure paths for lcm
# Albert Huang
# Large parts shamelessly stolen from GIMP

dnl AM_PATH_LCM([MINIMUM-VERSION, [ACTION-IF-FOUND [, ACTION-IF-NOT-FOUND]]])
dnl Test for lcm, and define LCM_CFLAGS and LCM_LIBS
dnl
AC_DEFUN([AM_PATH_LCM],
[dnl 
dnl Get the cflags and libraries from pkg-config
dnl

AC_ARG_ENABLE(lcmtest, [  --disable-lcmtest      do not try to compile and run a test lcm program],, enable_lcmtest=yes)

  pkg_name=lcm
  pkg_config_args="$pkg_name glib-2.0"

  no_lcm=""

  AC_PATH_PROG(PKG_CONFIG, pkg-config, no)

  if test x$PKG_CONFIG != xno ; then
    if pkg-config --atleast-pkgconfig-version 0.7 ; then
      :
    else
      echo *** pkg-config too old; version 0.7 or better required.
      no_lcm=yes
      PKG_CONFIG=no
    fi
  else
    no_lcm=yes
  fi

  min_lcm_version=ifelse([$1], ,0.0.1,$1)
  AC_MSG_CHECKING(for lcm - version >= $min_lcm_version)

  if test x$PKG_CONFIG != xno ; then
    ## don't try to run the test against uninstalled libtool libs
    if $PKG_CONFIG --uninstalled $pkg_config_args; then
	  echo "Will use uninstalled version of lcm found in PKG_CONFIG_PATH"
	  enable_lcmtest=no
    fi

    if $PKG_CONFIG --atleast-version $min_lcm_version $pkg_config_args; then
	  :
    else
	  no_lcm=yes
    fi
  fi

  if test x"$no_lcm" = x ; then
    LCM_CFLAGS=`$PKG_CONFIG $pkg_config_args --cflags`
    LCM_LIBS=`$PKG_CONFIG $pkg_config_args --libs`

    lcm_pkg_major_version=`$PKG_CONFIG --modversion $pkg_name | \
           sed 's/\([[0-9]]*\).\([[0-9]]*\).\([[0-9]]*\)/\1/'`
    lcm_pkg_minor_version=`$PKG_CONFIG --modversion $pkg_name | \
           sed 's/\([[0-9]]*\).\([[0-9]]*\).\([[0-9]]*\)/\2/'`
    lcm_pkg_micro_version=`$PKG_CONFIG --modversion $pkg_name | \
           sed 's/\([[0-9]]*\).\([[0-9]]*\).\([[0-9]]*\)/\3/'`
    if test "x$enable_lcmtest" = "xyes" ; then
      ac_save_CFLAGS="$CFLAGS"
      ac_save_LIBS="$LIBS"
      CFLAGS="$CFLAGS $LCM_CFLAGS"
      LIBS="$LCM_LIBS $LIBS"

dnl
dnl Now check if the installed lcm is sufficiently new. (Also sanity
dnl checks the results of pkg-config to some extent
dnl
      rm -f conf.lcmtest
      AC_TRY_RUN([
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <lcm/lcm.h>

int main ()
{
  int major, minor, micro;
  char *tmp_version;

  lcm_t *lcm;

  system ("touch conf.lcmtest");

  /* HP/UX 9 (%@#!) writes to sscanf strings */
  tmp_version = strdup("$min_lcm_version");
  if (sscanf(tmp_version, "%d.%d.%d", &major, &minor, &micro) != 3) {
     printf("%s, bad version string\n", "$min_lcm_version");
     exit(1);
   }

    if (($lcm_pkg_major_version > major) ||
        (($lcm_pkg_major_version == major) && ($lcm_pkg_minor_version > minor)) ||
        (($lcm_pkg_major_version == major) && ($lcm_pkg_minor_version == minor) && ($lcm_pkg_micro_version >= micro)))
    {
      return 0;
    }
  else
    {
      printf("\n*** 'pkg-config --modversion %s' returned %d.%d.%d, but the minimum version\n", "$pkg_name", $lcm_pkg_major_version, $lcm_pkg_minor_version, $lcm_pkg_micro_version);
      printf("*** of lcm required is %d.%d.%d. If pkg-config is correct, then it is\n", major, minor, micro);
      printf("*** best to upgrade to the required version.\n");
      printf("*** If pkg-config was wrong, set the environment variable PKG_CONFIG_PATH\n");
      printf("*** to point to the correct the correct configuration files\n");
      return 1;
    }
}

],, no_lcm=yes,[echo $ac_n "cross compiling; assumed OK... $ac_c"])
       CFLAGS="$ac_save_CFLAGS"
       LIBS="$ac_save_LIBS"
     fi
  fi
  if test "x$no_lcm" = x ; then
     AC_MSG_RESULT(yes (version $lcm_pkg_major_version.$lcm_pkg_minor_version.$lcm_pkg_micro_version))
     ifelse([$2], , :, [$2])     
  else
     if test "$PKG_CONFIG" = "no" ; then
       echo "*** A new enough version of pkg-config was not found."
       echo "*** See http://www.freedesktop.org/software/pkgconfig/"
     else
       if test -f conf.lcmtest ; then
        :
       else
          echo "*** Could not run lcm test program, checking why..."
          CFLAGS="$CFLAGS $LCM_CFLAGS"
          LIBS="$LIBS $LCM_LIBS"
          AC_TRY_LINK([
#include <stdio.h>
#include <lcm/lcm.h>

lcm_t *lcm;

],      [ return 0; ],
        [ echo "*** The test program compiled, but did not run. This usually means"
          echo "*** that the run-time linker is not finding lcm or finding the wrong"
          echo "*** version of lcm. If it is not finding lcm, you'll need to set your"
          echo "*** LD_LIBRARY_PATH environment variable, or edit /etc/ld.so.conf to point"
          echo "*** to the installed location  Also, make sure you have run ldconfig if that"
          echo "*** is required on your system"
	  echo "***"
          echo "*** If you have an old version installed, it is best to remove it, although"
          echo "*** you may also be able to get things to work by modifying LD_LIBRARY_PATH"],
        [ echo "*** The test program failed to compile or link. See the file config.log for the"
          echo "*** exact error that occurred. This usually means lcm is incorrectly installed."])
          CFLAGS="$ac_save_CFLAGS"
          LIBS="$ac_save_LIBS"
       fi
     fi
     LCM_CFLAGS=""
     LCM_LIBS=""
     ifelse([$3], , :, [$3])
  fi
  AC_SUBST(LCM_CFLAGS)
  AC_SUBST(LCM_LIBS)
  rm -f conf.lcmtest
])
