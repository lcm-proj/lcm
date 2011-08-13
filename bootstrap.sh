#!/bin/sh

echo "running \"autoreconf -i\"..."
autoreconf -i || exit 1

echo
echo "Bootstrap done."
echo
echo "Any time configure.in or a Makefile.am changes, you must run "
echo "    \"autoreconf -i\""
echo ""
echo "To build, run \"./configure ; make ; make install\""
