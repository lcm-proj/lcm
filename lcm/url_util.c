#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include "url_util.h"
#include "util.h"


const char UNICAST_SCHEME[] = "udp";
const char MULTICAST_SCHEME[] = "udpm";
const char SCHEME_SEPARATOR[] = "://";


bool has_separator(
	const char * url,
	const char * scheme)
{
	if(strlen(url) < strlen(scheme)) {
		return false;
	}

	return starts_with(
		&(url[strlen(scheme)]),
		SCHEME_SEPARATOR);
}


const char* scheme_to_str(
	const Scheme scheme)
{
	if(scheme == UNICAST) {
		return UNICAST_SCHEME;
	} else if(scheme == MULTICAST) {
		return MULTICAST_SCHEME;
	} else {
		assert(false && "unknown scheme identifier");
	}

	return 0;
}


bool parse_scheme(
	const char * url,
	Scheme * scheme)
{
	if(starts_with(url, MULTICAST_SCHEME)) {
		*scheme = MULTICAST;
	} else if(starts_with(url, UNICAST_SCHEME)) {
		*scheme = UNICAST;
	} else {
		return false;
	}

	return has_separator(url, scheme_to_str(*scheme));
}


unsigned int scheme_and_separator_length(
	const Scheme scheme)
{
        if(NOT_SPECIFIED == scheme) {
            return 0;
        }
        
	return strlen(scheme_to_str(scheme)) + strlen(SCHEME_SEPARATOR);
}


/**
 *  \brief Extracts port from string.
 *  \param[in] colon pointer to the host/port separator
 *      THIS_IS_THE_HOST_PART:PORT
 *                           |________ to here
 *  \param[out] port port as an integer; if no port is given (the colon pointer
 *      points to zero), then -1 ist returned.
 *  \return Port successfully read?
 */
bool read_port(
        const char * colon,
        int * port)
{
        static const int BASE = 10;
        
        //read the port
        if(0 == colon) {
                //url contains no port
                *port = -1;
        } else {
                char * end_pointer = 0;
                *port = (int)strtol(&(colon[1]), &end_pointer, BASE);
                
                if(end_pointer != &(colon[strlen(colon)])) {
                    //port contains invalid characters
                    return false;
                }
        }
        
        return true;
}


/**
 *  \brief Calculates the length of the host string.
 *  \param[in] host_and_port string containing host and port
 *  \param[in] colon pointer to host/port separator
 *  \return length of the host string in HOST_PORT
 */
unsigned int host_str_length(
        const char * host_and_port,
        const char * colon)
{
        if(colon == host_and_port) {
                //host part ist empty
                return 0;
        } else if(colon) {
                //the address contains a port
                return colon - host_and_port;
        } else {
                //the address contains no port, use whole address as host
                return strlen(host_and_port);
        }
}


/**
 *  \brief Reads host string from host and port combo.
 *  \param[in] host_and_port string containing host and port as HOST:PORT
 *  \param[in] colon poitner to host/port separator; the colon in HOST:PORT
 *  \param[out] host stored host
 *  \return host successfully read?
 */
bool read_host(
        const char * host_and_port,
        const char * colon,
        char * host)
{
        const unsigned int host_length = host_str_length(host_and_port, colon);
        if(0 == host_length) {
            //the host part is empty
            return false;
        }
        
        strncpy(host, host_and_port, host_length);
        host[host_length] = 0;
        
        return true;
}


bool split_host_and_port(
	const char * host_and_port,
	char * host,
	int * port)
{       
        //read the port
	const char * colon = strchr(host_and_port, ':');
	const bool port_read = read_port(colon, port);
        if(!port_read) {
            return false;
        }
	
	return read_host(host_and_port, colon, host);
}


bool contains_scheme(
    const char * url)
{
    return contains(url, SCHEME_SEPARATOR);
}