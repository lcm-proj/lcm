
if HAVE_PYTHON
pylcmexec_PYTHON = \
    lcm/__init__.py

pylcmexec_LTLIBRARIES = _lcm.la
_lcm_la_SOURCES = \
			   module.c \
			   pyeventlog.c \
			   pylcm.c \
			   pylcm.h
_lcm_la_LDFLAGS = -avoid-version -module 
_lcm_la_LIBADD = ../lcm/liblcm.la
_lcm_la_CFLAGS = $(PYTHON_CPPFLAGS) -I$(top_srcdir)

endif
