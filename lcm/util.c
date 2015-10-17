#include <stdbool.h>
#include <stdio.h>
#include <string.h>


bool starts_with(
	const char * str,
	const char * sub)
{
	return strncmp(str, sub, strlen(sub)) == 0;
}


bool contains(
        const char * str,
        const char * sub)
{
    return strstr(str, sub) != 0;
}
