Defining a data type - \c example_t {#tut_lcmgen}
====

When exchanging messages between two applications, you may have
many different types of data.  LCM allows you to define these types as
language-independent data structures.  You can have multiple
fields, each with its own type and name.  Some of these fields may be
structs themselves, or arrays.  LCM supports multiple languages and, types are
defined in a language-neutral specification that looks very similar to C.

Let's define an example type called <tt>example_t</tt>.  Create
a file called <tt>example_t.lcm</tt> with the following contents.  

\include types/example_t.lcm

The file is fairly straightforward, and consists of two parts: a package
name, and a structure definition. The package defines a namespace for the data
strucure, and gets mapped to the appropriate language construct (e.g.,
namespace in C++, package in Java and Python, etc.).  

The structure definition is a list of data fields, each with a name and a
type.  A number of primitive types are available for use, some of which are
shown above.  The \ref type_specification_primitives "LCM type specification"
has a complete listing of primitive types.

The <tt>lcm-gen</tt> tool, distributed with LCM, converts message type
definitions into code for supported programming languages, and maps message
types into language-specific data structures or classes.  Each data field is in
turn mapped into a native data structure.  In C, for example, <tt>boolean</tt>
corresponds to the C type <tt>int8_t</tt> and <tt>string</tt> corresponds to a
NULL-terminated <tt>char *</tt>.  Note that unsigned types are not defined,
since there is no equivalent in Java.

Additionally, you can define fixed-size or variable-length arrays.  In this
example, \p position is a \c double array of length 3, and \p ranges is a
variable-length \c int16_t array.  The length of \p ranges is specified by the
\c num_ranges field.

Although not shown in this example, you can build up more complex types by
referring to any other LCM types in the definition of your struct.  The
<tt>examples/</tt> directory in the LCM source distribution contains more
example type definitions.  This feature, and others are all described in more
detail in the \ref type_specification "LCM type specification".

# Generating language-specific bindings {#tutorial_general_lcmgen}

Run <tt>lcm-gen</tt> with the arguments listed in a row from the following
table to generate bindings for the programming language of your choice.

<table>
<tr><th>Language</th><th>lcm-gen usage</th></tr>
<tr><td>C</td><td><tt>lcm-gen -c example_t.lcm</tt></tr>
<tr><td>C++</td><td><tt>lcm-gen -x example_t.lcm</tt></tr>
<tr><td>Java</td><td><tt>lcm-gen -j example_t.lcm</tt></tr>
<tr><td>Lua</td><td><tt>lcm-gen -l example_t.lcm</tt></tr>
<tr><td>Python</td><td><tt>lcm-gen -p example_t.lcm</tt></tr>
<tr><td>C#</td><td><tt>lcm-gen --csharp example_t.lcm</tt></tr>
<tr><td>MATLAB</td><td>Generate Java code</tr>
<tr><td>Go</td><td>lcm-gen -g example_t.lcm</td></tr>
</table>

You can pass additional arguments to <tt>lcm-gen</tt> to adjust its behavior
for each programming language.  Run <tt>lcm-gen -h</tt> to get a full list of
its available options.
