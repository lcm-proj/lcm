FROM ubuntu:bionic

RUN apt-get update && apt-get install -y \
	build-essential \
	cmake \
	libglib2.0-dev \
	openjdk-8-jre-headless \
	python-dev \
	python-numpy \
	python-pip \
	vim && \
	pip install \
		numpy \
		pathlib \
		pyspark==2.4.2

ENV PYTHONPATH $PYTHONPATH:/opt/lcm/examples/python

