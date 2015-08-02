#!/bin/sh

[ -d html ] && rm -rf html

[ -d lcm-dotnet ] & rm -rf lcm-dotnet

[ -d html ] || mkdir html

# Python
# Build python modules locally so that Epydoc does not need to rely on a
# system-installed version of LCM
cd ../lcm-python
python setup.py install --prefix build
cd ../docs
pyver=$(python -c "import platform; print platform.python_version()[:3]")
PYTHONPATH=../lcm-python/build/python${pyver}/site-packages/:$PYTHONPATH epydoc --config epydoc.cfg

# Java
cd ../lcm-java
CLASSES="lcm/lcm/LCM.java lcm/logging/Log.java lcm/lcm/LCMSubscriber.java lcm/lcm/LCMEncodable.java lcm/lcm/MessageAggregator.java"

javadoc -d ../docs/html/javadocs -link http://java.sun.com/j2se/1.5.0/docs/api $CLASSES

# Doxygen
cd ../docs
doxygen

# .NET
cd ../lcm-dotnet
doxygen
mv lcm-dotnet/html ../docs/html/lcm-dotnet
rm -rf lcm-dotnet
