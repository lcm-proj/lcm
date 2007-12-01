#ifndef _LCM_H
#define _LCM_H

#include <stdint.h>
#include <glib.h>

#include "getopt.h"

/////////////////////////////////////////////////


#ifdef __cplusplus
extern "C" {
#endif

#ifndef g_ptr_array_size
#define g_ptr_array_size(x) ((x)->len)
#endif

typedef struct lcm_type lcm_type_t;

struct lcm_type
{
  	char *typename;
};

/////////////////////////////////////////////////
typedef enum { LCM_CONST, LCM_VAR } lcm_dimension_mode_t;

typedef struct lcm_dimension lcm_dimension_t;

struct lcm_dimension
{
	lcm_dimension_mode_t mode;
	char *size;                // a string containing either a member variable name or a constant
};

/////////////////////////////////////////////////

typedef struct lcm_member lcm_member_t;

struct lcm_member
{
	lcm_type_t *type;
	char       *membername;

	// an array of lcm_dimension_t. A scalar is a 1-dimensional array
	// of length 1.
	GPtrArray *dimensions;
};

/////////////////////////////////////////////////

typedef struct lcm_struct lcm_struct_t;

struct lcm_struct
{
	char *structname;    // name of the data type
  
	GPtrArray *members;  // lcm_member_t

       // recursive declaration of structs and enums
	GPtrArray *structs;  // lcm_struct_t
	GPtrArray *enums;    // locally-declared enums

	char *lcmfile;       // file/path of function that declared it
	int64_t hash;
};

/////////////////////////////////////////////////

typedef struct lcm_enum_value lcm_enum_value_t;

struct lcm_enum_value
{
	char    *valuename;
	int32_t value;
};

/////////////////////////////////////////////////

typedef struct lcm_enum lcm_enum_t;

struct lcm_enum
{
	char *enumname;     // name of the enum
	
	GPtrArray *values;   // legal values for the enum
	char *lcmfile;      // file/path of function that declared it

    // hash values for enums are "weak". They only involve the name of the enum,
    // so that new enumerated values can be added without breaking the hash.
    int64_t hash;
};

/////////////////////////////////////////////////

typedef struct lcm lcm_t;

struct lcm
{
	getopt_t *gopt;
	GPtrArray *structs; // lcm_struct_t
	GPtrArray *enums;   // lcm_enum_t (declared at top level)
};

/////////////////////////////////////////////////
// Helper functions
/////////////////////////////////////////////////

int lcm_is_primitive_type(const char *t);
lcm_member_t *lcm_find_member(lcm_struct_t *lr, const char *name);
int lcm_needs_generation(lcm_t *lcm, const char *declaringfile, const char *outfile);


#ifdef __cplusplus
}
#endif

#endif
