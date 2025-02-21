# For documentation, refer to the public alias in our parent directory.

load(":lcm_info.bzl", "LcmInfo")

def _impl(ctx):
    info = ctx.attr.src[LcmInfo]

    # Declare the output files.
    output_files = [
        ctx.actions.declare_file("/".join(info.lcm_package_split + [x + ".java"]))
        for x in info.lcm_message_names
    ]

    # Choose the base dir to output into.
    output_dir = output_files[0].dirname + "/"
    for _ in info.lcm_package_split:
        output_dir = output_dir + "../"
    arguments = [
        "--java",
        "--jpath=" + output_dir,
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

    # Return the '*.java' files.
    return [
        DefaultInfo(files = depset(output_files)),
    ]

lcm_java_library_srcs = rule(
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
