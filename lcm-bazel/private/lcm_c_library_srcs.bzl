# For documentation, refer to the public alias in our parent directory.

load(":lcm_info.bzl", "LcmInfo")

def _impl(ctx):
    info = ctx.attr.src[LcmInfo]

    # Declare the output files.
    output_files = [
        ctx.actions.declare_file("_".join(info.lcm_package_split + [x + ".c"]))
        for x in info.lcm_message_names
    ] + [
        ctx.actions.declare_file("_".join(info.lcm_package_split + [x + ".h"]))
        for x in info.lcm_message_names
    ]

    # Choose the base dir to output into.
    output_dir = output_files[0].dirname + "/"
    arguments = [
        "--c",
        "--c-cpath=" + output_dir,
        "--c-hpath=" + output_dir,
    ]

    # Run lcm-gen.
    ctx.actions.run(
        inputs = info.lcm_srcs,
        outputs = output_files,
        arguments = arguments + [
            lcm_src.path
            for lcm_src in info.lcm_srcs.to_list()
        ],
        executable = ctx.executable.lcmgen,
    )

    # Return the '*.c' and '*.h' files.
    return [
        DefaultInfo(files = depset(output_files)),
    ]

lcm_c_library_srcs = rule(
    implementation = _impl,
    attrs = {
        "src": attr.label(
            providers = [LcmInfo],
        ),
        "lcmgen": attr.label(
            cfg = "host",
            executable = True,
            default = Label("//lcmgen:lcm-gen"),
        ),
    },
)
