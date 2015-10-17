#pragma once

#include <stdbool.h>


/**
 *  Supported protocols.
 */
typedef enum {
	UNICAST = 1,
	MULTICAST = 2,
        NOT_SPECIFIED = 3
} Scheme;


/**
 *  \brief Parses an URL an splits it into protocol, path and port.
 *  \param[in] url url as a string
 *  \param[out] scheme scheme of the connection; defaults to UNICAST
 *  \param[out] host host part of the URL
 *  \param[out] port parsed port
 *  \return Valid address given?
 */
bool parse_url(
	const char * url,
	Scheme * scheme,
        char * host,
	int * port);
	
