LCM Type Specification Language {#type_specification}
====

\brief The usage and features of the LCM type language.

# Introduction {#type_specification_intro}

In addition to providing a set of communications primitives, LCM
includes utilities for generating platform-independent marshalling
and unmarshalling functions for user-defined data types. It is
similar to XDR, though it is written with greater type safety in
mind, and with the goal of first-class support for a variety of
languages including C, Java, and Python. This document describes
the data marshalling facility; the communications facility is
described elsewhere. Note that it is possible to use the data
marshalling features of LCM independently of LCM's communication
facilities.

## Design Goals {#type_specification_design_goals}

The primary design goals of the LCM marshalling facility are:

\li Provide a simple mechanism to define complex types that would be
immediately comfortable to users of C and Java
\li Provide first class support for a wide variety of client languages
\li Abstract away platform-specific details such as byte ordering
\li Maximize the amount of compile-time and run-time type safety
\li Be able to detect message type incompatibilities, such as when two
applications have different versions of the same datatype
\li Produce space-efficient encoded messages
\li Minimize encoding and decoding computational
costs

The current version of LCM achieves these goals with only a few
compromises. In some cases, a least-common-denominator approach
was used to ensure that all platforms supported the features
provided by LCM.

# Type Specifications {#type_specification_spec}

Type specifications are contained in files with an ".lcm" file type. They are
conventionally named in lower case with underscores between words: e.g., the
type "wind_speed_t" is defined in the file "wind_speed_t.lcm". The utility
<tt>lcm-gen</tt> converts an LCM type specification into a
language-dependent implementation.
  

## Structs {#type_specification_structs}
    
LCM structs are compound types consisting of other types. We begin with a
simple struct named "temperature_t" that contains a 64 bit integer named
"utime" and a 64 bit floating point number named "degCelsius". Two types of
comments are also illustrated.
    
\code
struct temperature_t 
{
    int64_t   utime;         // Timestamp, in microseconds

    /* Temperature in degrees Celsius. A "float" would probably
     * be good enough, unless we're measuring temperatures during
     * the big bang. Note that the asterisk on the beginning of this
     * line is not syntactically necessary, it's just pretty.
     */
    double    degCelsius;    
}
\endcode

This declaration must appear in a file named <tt>temperature_t.lcm</tt>.
    
LCM types do not contain pointers (but arrays are supported, see below):
this eliminates the possibility of circular references.
    
Before we go further, let's take a look at the various primitive types
available.

\subsubsection type_specification_primitives Primitive Types
      
LCM supports a number of primitive types: 
  <table>
   <tr><th>type</th><th>Description</th></tr>
   <tr><td><tt>int8_t</tt></td><td>8-bit signed integer</td></tr>
   <tr><td><tt>int16_t</tt></td><td>16-bit signed integer</td></tr>
   <tr><td><tt>int32_t</tt></td><td>32-bit signed integer</td></tr>
   <tr><td><tt>int64_t</tt></td><td>64-bit signed integer</td></tr>
   <tr><td><tt>float</tt></td><td>32-bit IEEE floating point value</td></tr>
   <tr><td><tt>double</tt></td><td>64-bit IEEE floating point value</td></tr>
   <tr><td><tt>string</tt></td><td>UTF-8 string</td></tr>
   <tr><td><tt>boolean</tt></td><td>true/false logical value</td></tr>
   <tr><td><tt>byte</tt></td><td>8-bit value</td></tr>
  </table>

The integer types are all signed (as is necessary to ensure easy
inter-operation with Java, which lacks unsigned types) and are encoded in
network byte order.

The type \c byte is represented in C/C++ as `uint8_t`. Languages with a native
`byte` representation use their respective native byte representations (e.g.,
type `byte` in Java).

Floating point types are encoded using the IEEE 32 and 64 bit formats. An LCM
implementation may not use any other encoding. The 32 and 64 bit quantities
are transmitted in network byte order.

The \c boolean type is encoded as a single byte whose value is either 0 or 1.
An array of N booleans will require N bytes.

The \c string type encodes a NULL-terminated UTF-8 string. The string is sent
as a 32 bit integer comprising the total length of string in bytes (including
terminating NULL character) followed by the bytes of the string (again,
including the NULL character).
      
\subsubsection type_specification_arrays Arrays
 
LCM supports multi-dimensional arrays consisting of primitives, structs, or
constant declarations. The number of dimensions in the array are declared by
the LCM type declaration: you cannot encode an LCM type that consists of a
variable-dimension array. In contrast, variable-sized arrays are fine.
Consider this example:
      
\code
struct point2d_list_t
{
    int32_t npoints;
    double  points[npoints][2];
}
\endcode

This example shows a two-dimensional array declaration consisting of both
variable-length and fixed-length components. In a variable-length declaration,
the variable that contains the length must be declared prior to its use as an
array length. Also note that the length variable (npoints, in the example
above) must be an integer type, and must always have a value greater than or
equal to zero.
      
When arrays are encoded and decoded, each dimension's size is already known:
it is either a constant (given by the LCM type declaration), or it was a
previously encoded/decoded variable. Thus, an array is encoded simply by
recursively encoding each element of the array, with inner-most dimensions
being encoded together. In other words, the array above would be encoded in
the order <tt> points[0][0], points[0][1], points[1][0], points[1][1],
points[2][0], points[2][1],</tt> etc.
      
## Constants {#type_specification_constants}
    
LCM provides a simple way of declaring constants that can subsequently be used
to populate other data fields. Users are free to use these constants in any
way they choose: as magic numbers, enumerations, or bitfields.
    
Constants can be declared by using the const keyword.

\code
struct my_constants_t
{
    const int32_t YELLOW=1, GOLDENROD=2, CANARY=3;
    const double E=2.8718;
}
\endcode
    
Note that types must be declared for constants. All integer and floating point
types are supported.  String constants are not supported.
    
# Namespaces {#type_specification_namespaces}
  
LCM allows types to be defined in a namespace, making it easier for users to
use types from other organizations even if those types have the same name. The
namespace mechanism is closely modeled after that of Java. In languages that
support namespaces (such as Java and Python), the LCM namespace mechanism is
mapped onto the native mechanism. In languages like C, namespaces are
approximated by prepending the package name to the type name. See below for an
example of namespaces. Note that the package keyword identifies the namespace
of the structs defined in that file, and that fully-qualified types are formed
by concatenating the package and type name, with a period between.
  
\code
package mycorp;

struct camera_image_t {
    int64_t      utime;
    string       camera_name;
    jpeg.image_t jpeg_image;
    mit.pose_t   pose;
}
\endcode

LCM users are encouraged to put their types into a unique namespace and to
fully-qualify the types of all the member fields.
  
# Performance Considerations {#type_specification_performance}
  
The runtime costs of encoding and decoding with LCM are generally not a system
bottleneck. The marshalling functions are dramatically faster than an XML
implementation, but since each member must be individually processed (in order
to ensure correct byte ordering, for example), LCM is more expensive than
using raw C structs. That said, LCM's first application used over 40MB/s.
  
# Fingerprint Computation {#type_specification_fingerprints}
  
Fingerprints ensure that the encoding and decoding methods agree on the format
of a data type. The fingerprints are a function, recursively, of all of the
types that a type contains. This creates a potential problem when types could
be mutually recursive: we must avoid an infinite recursion.
  
The basic idea is for each type to have a "base" fingerprint, which we'll
denote for type "A" as "K_A". K_A is a constant derived from the lcm type
description (and it's stored as lcm_struct->hash). We wish to compute the
actual fingerprint (or hash), A(), which is a function of all of A's contained
types.
  
In addition, so that we can recognize a recursion, the A() function takes an
argument, which is a list of the types already visited. E.g., C([A,B])
indicates that we wish to compute the hash of type C, given that C is a member
of type B, which is a member of type A.  We avoid recursions by setting
C([list]) = 0 if [list] contains C. 
  
The contribution of primitive types is handled via the K_A; there is no
recursion for them.
  
A small wrinkle arises from the above definitions: if types A, B, and C are
mutually recursive, we can have two types with the same hash. This is clearly
undesirable. We fix this by making the order of recursion relevant: at each
node in the tree, we rotate the value (bitwise) 1 bit to the left. A type that
is included at recursion depth N has its contribution rotated by N bits.
  
Note that this mechanism is entirely unnecessary for enumerations (they cannot
contain other types); for enumerations, we just use the hash in
lcmenum->hash.
  
  
PSEUDO-CODE 

\code
  v = compute_hash(type, parents)
  
  if type is member of parents
     return 0

  v = K_type;

  for each members m of type
      v += compute_hash(m, [parents, type])

  return rot_left(v);
\endcode


When encoding/decode a type T, we would use compute_hash(T, []) as the hash
function.


EXAMPLE

\code
struct A
{
        B b;
        C c;
}

struct B
{
        A a;
}

struct C
{
        B b;
}
\endcode

  Diagrammatically, we can compute their hashes by showing the children
  of each branch. We use lower case to indicate a terminal leaf (where
  the leaf is the same class as one of its parents).


\verbatim
         A                B                  C
       /   \              |                  |
      B     C             A                  B
      |     |            / \                 |
      a     B           b   C                A
            |               |               / \
            a               b              b   c

A() = R{K_A + R{K_B}} + R{K_C + R{K_B}}}

B() = R{K_B + R{K_A + R{K_C}}}

C() = R{K_C + R{K_B + R{K_A}}}
\endverbatim

Note that without the rotations, B() == C().

## Related Work {#type_specification_related_work}

LCM is most similar to XDR, which is used in RPC and is described by RFC4506.
Both use a C-like syntax (and even C keywords like "struct"). LCM differs in
that its language is smaller: rarely-used features like unions are not
supported. LCM does not support pointers: this eliminates the pointer-chasing
problems that can arise in XDR. Variable-length arrays are supported in a more
natural way in LCM, and LCM includes a type "signature" in the encoded data.
This type signature allows run-time error detection.

Data encoding representations are often compared to XML.
XML and LCM serve very different functions. The verbosity and generic structure
of XML are aids for agents to use information that they understand while safely
skipping over properties that are alien to them. In contrast, LCM is designed
for agents that are tightly coupled but that may not be in the same memory
space. A more rigid type definition, along with space-efficient and
computationally-efficient encodings, are better fits for these types of
applications.

## Development History {#type_specification_history}

LCM's marshalling facilities were created for use on MIT's DARPA
Urban Challenge vehicle, with development starting during the
summer of 2006. Early versions supported many features that have
since been deprecated: reducing the number of extraneous
features has simplified the code base significantly, since most
features typically impact several language back-ends (currently
C, Java, and Python).
