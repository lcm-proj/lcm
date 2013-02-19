package = "lcm"
version = "0.9.2-0"
source = {
  url = "http://github.com/tprk77/lcm-tprk77"
}
description = {
  summary = "Lua bindings for LCM.",
  detailed = [[
Lightweight Communications and Marshalling (LCM)

LCM is a set of libraries and tools for interprocess communication.  It
emphasizes simplicity in usage, exhibits high performance under heavy load, and 
runs on a variety of programming languages and operating systems.

The LCM project is located here:
http://lcm.googlecode.com

This rock provides Lua bindings, found here:
http://github.com/tprk77/lcm-tprk77
  ]],
  homepage = "http://github.com/tprk77/lcm-tprk77",
  license = "LGPL v2.1 (same as LCM)"
}
dependencies = {
  "lua >= 5.1"
}
external_dependencies = {
  GLIB = {
    library = "glib-2.0",
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
        "../../lcm/lcm.c",
        "../../lcm/lcm_udp.c",
        "../../lcm/lcm_tcpq.c",
        "../../lcm/lcm_file.c",
        "../../lcm/ringbuffer.c",
        "../../lcm/eventlog.c",
        "../init.c",
        "../lualcm_lcm.c",
        "../lualcm_pack.c",
        "../lualcm_hash.c",
        "../lua_ref_helper.c",
        "../utf8_check.c"
      },
      defines = {},
      libraries = {
        "glib-2.0",
        "gthread-2.0"
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
            "../../lcm/lcm.c",
            "../../lcm/lcm_udp.c",
            "../../lcm/lcm_tcpq.c",
            "../../lcm/eventlog.c",
            "../../lcm/lcm_file.c",
            "../../lcm/ringbuffer.c",
            "../../lcm/windows/WinPorting.cpp",
            "../init.c",
            "../lualcm_lcm.c",
            "../lualcm_pack.c",
            "../lualcm_hash.c",
            "../lua_ref_helper.c",
            "../utf8_check.c"
          },
          defines = {'WIN32="lean_and_mean"'},
          libraries = {
            "Ws2_32",
            "glib-2.0",
            "gthread-2.0"
          }
        }
      }
    }
  }
}