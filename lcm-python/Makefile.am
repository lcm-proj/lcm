INCLUDES = $(PYTHON_INCLUDES)
pylcmexec_PYTHON = \
    lcm/__init__.py

pylcmexec_LTLIBRARIES = _lcm.la
_lcm_la_SOURCES = \
			   module.c \
			   pyeventlog.c \
			   pylcm.c \
			   pylcm.h \
			   pylcm_subscription.c \
			   pylcm_subscription.h

# linker flags
_lcm_la_LDFLAGS = -module -avoid-version
#if PLATFORM_WIN32
#_lcm_la_LDFLAGS += -no-undefined
#endif

_lcm_la_LIBADD = ../lcm/liblcm.la
_lcm_la_CFLAGS = -I$(top_srcdir)

EXTRA_DIST = setup.py
