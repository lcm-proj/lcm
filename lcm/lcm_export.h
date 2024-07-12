#ifndef LCM_EXPORT
#  ifdef _WIN32
#    define LCM_EXPORT __declspec(dllimport)
#  else
#    define LCM_EXPORT __attribute__((visibility("default")))
#  endif /* _WIN32 */
#endif /* LCM_EXPORT */
