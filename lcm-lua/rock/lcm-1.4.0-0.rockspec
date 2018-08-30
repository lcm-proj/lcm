package = "lcm"
version = "1.3.0-0"
source = {
  url = "http://github.com/lcm-proj/lcm"
}
description = {
  summary = "Lua bindings for LCM.",
  detailed = [[
Lightweight Communications and Marshalling (LCM)

LCM is a set of libraries and tools for interprocess communication.  It
emphasizes simplicity in usage, exhibits high performance under heavy load, and
runs on a variety of programming languages and operating systems.

The LCM project is located here:
http://lcm-proj.github.io/
  ]],
  homepage = "http://github.com/lcm-proj/lcm",
  license = "LGPL v2.1"
}
dependencies = {
  "lua >= 5.1"
}
external_dependencies = {
  GLIB = {
    library = "libglib-2.0.so",
    header = "glib.h"
  },
  GLIB_CONFIG = {
    header = "glibconfig.h"
  },
  platforms = {
    win32 = {
      GLIB = {
        library = "glib-2.0.lib"
      }
    }
  }
}
build = {
  type = "builtin",
  modules = {
    lcm = {
      sources = {
        "../../lcm/eventlog.c",
        "../../lcm/lcm.c",
        "../../lcm/lcm_file.c",
        "../../lcm/lcm_memq.c",
        "../../lcm/lcm_mpudpm.c",
        "../../lcm/lcm_tcpq.c",
        "../../lcm/lcm_udpm.c",
        "../../lcm/lcmtypes/channel_port_map_update_t.c",
        "../../lcm/lcmtypes/channel_to_port_t.c",
        "../../lcm/ringbuffer.c",
        "../../lcm/udpm_util.c",
        "../init.c",
        "../lua_ref_helper.c",
        "../lualcm_hash.c",
        "../lualcm_lcm.c",
        "../lualcm_pack.c",
        "../utf8_check.c"
      },
      defines = {},
      libraries = {
        "glib-2.0"
      },
      incdirs = {
        "../..",
        "$(GLIB_INCDIR)",
        "$(GLIB_CONFIG_INCDIR)"
      },
      libdirs = {
        "$(GLIB_LIBDIR)"
      }
    }
  },
  platforms = {
    win32 = {
      modules = {
        lcm = {
          sources = {
            "../../lcm/eventlog.c",
            "../../lcm/lcm.c",
            "../../lcm/lcm_file.c",
            "../../lcm/lcm_memq.c",
            "../../lcm/lcm_mpudpm.c",
            "../../lcm/lcm_tcpq.c",
            "../../lcm/lcm_udpm.c",
            "../../lcm/lcmtypes/channel_port_map_update_t.c",
            "../../lcm/lcmtypes/channel_to_port_t.c",
            "../../lcm/ringbuffer.c",
            "../../lcm/udpm_util.c",
            "../../lcm/windows/WinPorting.cpp",
            "../init.c",
            "../lua_ref_helper.c",
            "../lualcm_hash.c",
            "../lualcm_lcm.c",
            "../lualcm_pack.c",
            "../utf8_check.c"
          },
          defines = {'WIN32="lean_and_mean"'},
          libraries = {
            "Ws2_32",
            "glib-2.0"
          }
        }
      }
    }
  }
}
