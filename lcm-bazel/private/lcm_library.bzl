# For documentation, refer to the public alias in our parent directory.

load(":lcm_info.bzl", "LcmInfo")

def _impl(ctx):
    lcm_package = ctx.attr.lcm_package
    if not lcm_package:
        lcm_package_split = []
    else:
        lcm_package_split = lcm_package.split(".")
    lcm_srcs = depset(ctx.files.srcs)
    lcm_message_names = []
    for lcm_src in ctx.files.srcs:
        lcm_message_names.append(lcm_src.basename[:-len(".lcm")])
    return [
        DefaultInfo(files = lcm_srcs),
        LcmInfo(
            lcm_package = lcm_package,
            lcm_package_split = lcm_package_split,
            lcm_srcs = lcm_srcs,
            lcm_message_names = lcm_message_names,
        ),
    ]

lcm_library = rule(
    implementation = _impl,
    provides = [LcmInfo],
    attrs = {
        "lcm_package": attr.string(
            mandatory = True,
        ),
        "srcs": attr.label_list(
            allow_files = [".lcm"],
            mandatory = True,
        ),
        # TODO(jwnimmer-tri) This isn't actually used for anything yet ...
        # still, it seems worthwhile to let people write it down.
        "deps": attr.label_list(
            providers = [LcmInfo],
        ),
    },
)
