# Python application notes

## Redundant module aliases

At first glance the way that lcm-gen generates an `__init__.py` for the outputted Python module may
seem strange. Symbols are exported using the [redundant alias convention defined by
Python](https://typing.readthedocs.io/en/latest/spec/distributing.html#import-conventions).

This allows tools to see exactly what symbols are exported by the module without having to first
implicitly reexport. For example, consider a module which exported symbols using a convention like
`from .file import Foo` rather than the redundant alias convention of 
`from .file import Foo as Foo`. In this case, running `mypy` with `--no-implicit-reexport` would
cause errors like `error: Module "foo" does not explicitly export attribute "Foo"  [attr-defined]`.