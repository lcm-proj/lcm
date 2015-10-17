#pragma once

#include "parse_url.h"


/**
 *  \brief Extracts scheme for an URL with the format:
 *      scheme://XYZ
 *  \param[in] url a complete URL
 *  \param[out] scheme found scheme
 *  \return Supported scheme found?
 */
bool parse_scheme(
	const char * url,
	Scheme * scheme);


/**
 *  \brief Splits a host and a port.
 *  \param[in] host_and_port host and port part of the URL, without scheme.
 *  \param[out] host found host name
 *  \param[out] found port; if no port is given, -1 is returned
 */
bool split_host_and_port(
        const char * host_and_port,
        char * host,
        int * port);


bool contains_scheme(
    const char * url);


unsigned int scheme_and_separator_length(
        const Scheme scheme);