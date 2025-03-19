load("@bazel_skylib//rules:common_settings.bzl", "BuildSettingInfo")

def _impl(ctx):
    # Build a dictionary of the flags' current values.
    substitutions = {}
    for key, label in ctx.attr.flag_substitutions.items():
        substitutions[key] = label[BuildSettingInfo].value

    # Add an action to expand the template using those flags.
    out = ctx.actions.declare_file(ctx.label.name)
    ctx.actions.expand_template(
        template = ctx.file.template,
        output = out,
        substitutions = substitutions,
    )
    return DefaultInfo(files = depset([out]))

expand_flag_template = rule(
    implementation = _impl,
    attrs = {
        "template": attr.label(
            allow_single_file = True,
            doc = "The template file to expand.",
        ),
        "flag_substitutions": attr.string_keyed_label_dict(
            doc = "A dictionary mapping strings to the label for the flag to substitute.",
        ),
    },
    doc = "Generates a file based on a template file and textual substitutions.",
)
