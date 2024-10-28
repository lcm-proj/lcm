# Installing LCM

A limited set of package managers on certain systems provide prebuilt packages for LCM. This page
enumerates those.

## System packages

LCM is also available on some system package managers, although there is not widespread coverage.

### Ubuntu (via apt)

To install the main LCM package run:

```shell
sudo apt install liblcm-dev
```

To install Java-based components, run:

```shell
sudo apt install liblcm-java
```

Some versions of Ubuntu also have an LCM Python package available via apt. Please check your version
of Ubuntu before running any of the below commands:

```shell
# On 18.04 (Bionic Beaver) only
sudo apt install python-liblcm
# On 24.04 (Noble Numbat) only
sudo apt install python3-lcm
```

### macOS (via homebrew)

To install the LCM package run:

```shell
brew install lcm
```

### Arch Linux (AUR)

LCM is available in the [lcm](https://aur.archlinux.org/packages/lcm) package.

For more information on how to install a package using AUR, see [the AUR
docs](https://wiki.archlinux.org/title/Arch_User_Repository).

Note: in order to get Java-based components (like `lcm-logplayer-gui`) you'll need to install
`java-environment` before running `makepkg`.

### NixOS

LCM is available in the `lcm` package. Please see the [NixOS package
index](https://search.nixos.org/packages) for more information.


## Python packages (via pip)

LCM can be installed via the Python package manager (`pip`) on many systems. To do so, run:

```shell
pip3 install lcm
```

This package contains:

- The LCM Python module
- LCM executables (for example, `lcm-logplayer`)
    - Note: Java-based executables (like `lcm-logplayer-gui`) are not included for musl-based linux
      distributions
- Development files (headers and libraries)

Note: this package has a hard runtime dependency on GLib 2.0. If you have not already, please
install this dependency before using the Python package.
