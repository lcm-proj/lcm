#!/usr/bin/env python
# vim: tabstop=2 shiftwidth=2 expandtab

DESC = """
Tool for bootstrapping, running demos, etc.  

Example Flow:

Drop into a dockerized shell:
$ tool.py --indocker

   NB: We volume-mount your code in the environment, so changes to files
   you make using an editor outside the container are reflected inside
   the container.

Run demos:
$ tool.py --demo

"""

import logging
import os
import subprocess
import sys

DOCKER = os.environ.get('LCM_DOCKER', 'docker')

# Path to the demo source
LCM_ROOT = os.environ.get(
  'LCM_ROOT', os.path.dirname(os.path.abspath(__file__)))


### Utils

# Basic logger just for demo
LOG_FORMAT = "%(asctime)s\t%(name)-4s %(process)d : %(message)s"
log = logging.getLogger("ll")
log.setLevel(logging.INFO)
console_handler = logging.StreamHandler(sys.stderr)
console_handler.setFormatter(logging.Formatter(LOG_FORMAT))
log.addHandler(console_handler)


def run_cmd(cmd):
  """Run the string `cmd` just as if you pasted it into your shell"""
  cmd = cmd.replace('\n', '').strip()
  log.info("Running %s ..." % cmd)
  subprocess.check_call(cmd, shell=True)
  log.info("... done with %s " % cmd)


class DockerEnv(object):
  """Encapsulates our dockerized dev / runtime environment.  For this
  revision of the demo, we do not include a Dockerfile because it's not
  necessary at the time of writing."""

  # Create and use a docker container with this name
  CONTAINER_NAME = os.environ.get('LCM_CONTAINER_NAME', 'lcm')

  DOCKER_IMAGE = os.environ.get('LCM_DOCKER_IMAGE', 'lcm-dev')

  @classmethod
  def start(cls):
    """Start the dockerized environment"""
    
    CMD = """
      cd {root} &&
      {docker} build -t lcm-dev . &&
      {docker} run
        --name {container_name}
        -d -it -P
        -e PYTHONDONTWRITEBYTECODE=1
        --net=host
        -v `pwd`:/opt/lcm:z
        -v /:/outer_root:z
        -w /opt/lcm
          {docker_image} sleep infinity || docker start {container_name} || true
    """.format(
          root=LCM_ROOT,
          container_name=cls.CONTAINER_NAME,
          docker_image=cls.DOCKER_IMAGE,
          docker=DOCKER)
    run_cmd(CMD)

    log.info("Started or re-used container %s" % cls.CONTAINER_NAME)

    BUILD_AND_INSTALL_LCM = """
      rm -rf build && mkdir -p build && cd build &&
      cmake .. && make -j 12 && make test && make install &&
      ldconfig && cd - &&
      cd examples/python && ./gen-types.sh && cd -
    """
    cls.run_cmd(BUILD_AND_INSTALL_LCM, rm=False)
    log.info("Built LCM in container")

  @classmethod
  def shell(cls):
    EXEC_CMD = 'docker exec -it %s bash' % cls.CONTAINER_NAME
    os.execvp("docker", EXEC_CMD.split(' '))

  @classmethod
  def rm_shell(cls):
    try:
      run_cmd('docker rm -f %s' % cls.CONTAINER_NAME)
    except Exception:
      pass
    log.info("Removed container %s" % cls.CONTAINER_NAME)

  @classmethod
  def run_cmd(cls, cmd, rm=True):
    """Run `cmd` inside the dockerized environment (or this environment
    if we're already inside!)"""
    am_i_in_docker = os.path.exists('/opt/lcm')
    if am_i_in_docker:
      run_cmd(cmd)
    else:
      RUN_CMD = "docker exec -it %s bash -c '%s'" % (cls.CONTAINER_NAME, cmd)
      run_cmd(RUN_CMD)
      if rm:
        cls.rm_shell()

def create_arg_parser():
  import argparse
  
  parser = argparse.ArgumentParser(
                      description=DESC,
                      formatter_class=argparse.RawDescriptionHelpFormatter)
  parser.add_argument(
    '--shell', default=False, action='store_true',
    help='Drop into a dockerized dev env shell')
  parser.add_argument(
    '--shell-rm', default=False, action='store_true',
    help='Remove the dockerized dev env shell')
  parser.add_argument(
    '--demo', default=False, action='store_true',
    help='Train demo networks and start Tensorboard')

  return parser

def main(args=None):
  if not args:
    parser = create_arg_parser()
    args = parser.parse_args()
  
  if args.shell:
    DockerEnv.start()
    DockerEnv.shell()
  elif args.shell_rm:
    DockerEnv.rm_shell()
  elif args.demo:
    DockerEnv.start()
    DockerEnv.run_cmd('python demo.py', rm=False)

if __name__ == '__main__':
  main()
