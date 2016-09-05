#include "parse_url.h"
#include "url_util.h"


bool parse_url(
	const char * url,
	Scheme * scheme,
        char * host,
	int * port)
{
        if(!contains_scheme(url)) {
            *scheme = NOT_SPECIFIED;
        } else {
            //valid scheme given?
            const bool validScheme = parse_scheme(url, scheme);
            if(!validScheme) {
                    return false;
            }
        }
        

	//valid host and port given?
	const unsigned int n = scheme_and_separator_length(*scheme);
	const char * host_and_port = &(url[n]);
        return split_host_and_port(host_and_port, host, port);
}
	
