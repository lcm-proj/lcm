#ifndef _LCM_H
#define _LCM_H

#include <stdint.h>
#include <glib.h>

#include "getopt.h"

/////////////////////////////////////////////////

#ifndef g_ptr_array_size
#define g_ptr_array_size(x) ((x)->len)
#endif

/////////////////////////////////////////////////
// lcm_typename_t: represents the name of a type, including package
//
//	Originally, the first field in the lcm_typename was named typename - which is a C++
//	keyword and caused much grief. Renamed to lctypename.
typedef struct lcm_typename lcm_typename_t;

struct lcm_typename
{
  	char *lctypename; // fully-qualified name, e.g., "edu.mit.dgc.laser_t"
    char *package;    // package name, e.g., "edu.mit.dgc"
    char *shortname;  // e.g., "laser_t"
};

/////////////////////////////////////////////////
// lcm_dimension_t: represents the size of a dimension of an
//                  array. The size can be either dynamic (a variable)
//                  or a constant.
//
typedef enum { LCM_CONST, LCM_VAR } lcm_dimension_mode_t;

typedef struct lcm_dimension lcm_dimension_t;

struct lcm_dimension
{
	lcm_dimension_mode_t mode;
	char *size;                // a string containing either a member variable name or a constant
};

/////////////////////////////////////////////////
// lcm_member_t: represents one member of a struct, including (if its
//               an array), its dimensions.
//
typedef struct lcm_member lcm_member_t;

struct lcm_member
{
    lcm_typename_t *type;
	char           *membername;

	// an array of lcm_dimension_t. A scalar is a 1-dimensional array
	// of length 1.
	GPtrArray *dimensions;
};

/////////////////////////////////////////////////
// lcm_struct_t: a first-class LCM object declaration
//
typedef struct lcm_struct lcm_struct_t;

struct lcm_struct
{
    lcm_typename_t *structname; // name of the data type
  
	GPtrArray *members;  // lcm_member_t

       // recursive declaration of structs and enums
	GPtrArray *structs;  // lcm_struct_t
	GPtrArray *enums;    // locally-declared enums  DEPRECATED
	GPtrArray *constants; // lcm_constant_t

	char *lcmfile;       // file/path of function that declared it
	int64_t hash;
};

/////////////////////////////////////////////////
// lcm_constant_: the symbolic name of a constant and its value.
//
typedef struct lcm_constant lcm_constant_t;

struct lcm_constant
{
    char *lctypename;    // int8_t / int16_t / int32_t / int64_t / float / double
    char *membername;
    union {
        int8_t i8;
        int16_t i16;
        int32_t i32;
        int64_t i64;
        float f;
        double d;
    } val;
    char *val_str;   // value as a string, as specified in the .lcm file
};

/////////////////////////////////////////////////
// DEPRECATED
// lcm_enum_value_t: the symbolic name of an enum and its constant
//                   value.
//
typedef struct lcm_enum_value lcm_enum_value_t;

struct lcm_enum_value
{
	char    *valuename;
	int32_t value;
};

/////////////////////////////////////////////////
// DEPRECATED
// lcm_enum_t: an enumeration, also a first-class LCM object.
//
typedef struct lcm_enum lcm_enum_t;
struct lcm_enum
{
    lcm_typename_t *enumname; // name of the enum
	
	GPtrArray *values;   // legal values for the enum
	char *lcmfile;      // file/path of function that declared it

    // hash values for enums are "weak". They only involve the name of the enum,
    // so that new enumerated values can be added without breaking the hash.
    int64_t hash;
};

/////////////////////////////////////////////////
// lcmgen_t: State used when parsing LCM declarations. The gopt is
//           essentially a set of key-value pairs that configure
//           various options. structs and enums are populated
//           according to the parsed definitions.
//
typedef struct lcmgen lcmgen_t;

struct lcmgen
{
    char      *package; // remembers the last-specified package name, which is prepended to other types.
	getopt_t  *gopt;
	GPtrArray *structs; // lcm_struct_t
	GPtrArray *enums;   // lcm_enum_t (declared at top level)
};

/////////////////////////////////////////////////
// Helper functions
/////////////////////////////////////////////////

// Returns 1 if the argument is a built-in type (e.g., "int64_t", "float").
int lcm_is_primitive_type(const char *t);

// Returns 1 if the argument is a legal constant type (e.g., "int64_t", "float").
int lcm_is_legal_const_type(const char *t);

// Returns the member of a struct by name. Returns NULL on error.
lcm_member_t *lcm_find_member(lcm_struct_t *lr, const char *name);

// Returns the constant of a struct by name. Returns NULL on error.
lcm_constant_t *lcm_find_const(lcm_struct_t *lr, const char *name);

// Returns 1 if the "lazy" option is enabled AND the file "outfile" is
// older than the file "declaringfile"
int lcm_needs_generation(lcmgen_t *lcmgen, const char *declaringfile, const char *outfile);

// create a new parsing context.
lcmgen_t *lcmgen_create();

// for debugging, emit the contents to stdout
void lcmgen_dump(lcmgen_t *lcm);

// parse the provided file
int lcmgen_handle_file(lcmgen_t *lcm, const char *path);

// Are all of the dimensions of this array constant?
// (scalars return 1)
int lcm_is_constant_size_array(lcm_member_t *lm);

#endif
