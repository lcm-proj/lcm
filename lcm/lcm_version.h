/// LCM release major version - the X in version X.Y.Z
#define LCM_VERSION_MAJOR 1

/// LCM release minor version - the Y in version X.Y.Z
#define LCM_VERSION_MINOR 5

/// LCM release patch version - the Z in version X.Y.Z
#define LCM_VERSION_PATCH 1

/// LCM ABI version
#define LCM_ABI_VERSION 1

// Old symbols provided for compatibility
#define LCM_MAJOR_VERSION LCM_VERSION_MAJOR
#define LCM_MINOR_VERSION LCM_VERSION_MINOR
#define LCM_MICRO_VERSION LCM_VERSION_PATCH

// Macro required indirection.
#define _MACRO_LCM_STRINGIFY(x) #x
/// Surround x in quotes. x may be the result of another macro.
#define MACRO_LCM_STRINGIFY(x) _MACRO_LCM_STRINGIFY(x)

/// "x.y.z"
#define LCM_VERSION_STRING                 \
    MACRO_LCM_STRINGIFY(LCM_VERSION_MAJOR) \
    "." MACRO_LCM_STRINGIFY(LCM_VERSION_MINOR) "." MACRO_LCM_STRINGIFY(LCM_VERSION_PATCH)
