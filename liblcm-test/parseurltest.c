#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "../lcm/parse_url.h"
#include "../lcm/url_util.h"
#include "../lcm/util.h"


bool equal(
        const char * first,
        const char * second)
{
    return 0 == strcmp(first, second);
}


void msg(
    const char * str)
{
    printf("%s\n", str);
}


void test_starts_with()
{
	assert(starts_with("spamhamandeggs", "spam"));
	assert(!starts_with("foobarbaz", "ham"));
 
        msg("starts_with passed");
}


void test_contains()
{
        assert(contains("spamhamandeggs", "spam"));
        assert(contains("spamhamandeggs", "ham"));
        assert(contains("spamhamandeggs", "and"));
        assert(contains("spamhamandeggs", "eggs"));
        
        assert(!contains("spamhamandeggs", "foo"));
        assert(!contains("spamhamandeggs", "bar"));
        assert(!contains("spamhamandeggs", "baz"));
        
        msg("contains passed");
}


void test_contains_scheme()
{
    assert(contains_scheme("http://spamhamandeggs"));
    assert(contains_scheme("udpm://spamhamandeggs"));
    assert(contains_scheme("tcp://spamhamandeggs"));
    assert(!contains_scheme("spamhamandeggs"));
    
    msg("contains_scheme passed");
}


void test_parse_scheme()
{
	Scheme scheme;
	bool result;

	result = parse_scheme("udpm://", &scheme);
	assert(result);
	assert(scheme == MULTICAST);

	result = parse_scheme("udp://", &scheme);
	assert(result);
	assert(scheme == UNICAST);

	result = parse_scheme("spam://", &scheme);
	assert(!result);
        
        msg("parse_scheme passed");
}


void test_host_and_port()
{
    char host[64];
    int port;
    bool result;
    
    //valid host and port strings
    result = split_host_and_port("test:42", host, &port);
    assert(result);
    assert(42 == port);
    assert(0 == strcmp("test", host));
    
    result = split_host_and_port("spamhamandeggs:23", host, &port);
    assert(result);
    assert(23 == port);
    assert(equal("spamhamandeggs", host));
    
    
    //host without a port
    result = split_host_and_port("foobarbaz", host, &port);
    assert(result);
    assert(-1 == port);
    assert(equal("foobarbaz", host));
    
    
    //an empty string
    result = split_host_and_port("", host, &port);
    assert(!result);
    
    //port without a host
    result = split_host_and_port(":13", host, &port);
    assert(!result);
    
    //neither host nor port
    result = split_host_and_port(":", host, &port);
    assert(!result);
    
    //invalid port
    result = split_host_and_port("test:42spam", host, &port);
    assert(!result);
    
    msg("host_and_port passed");
}


void test_parse_url()
{
    Scheme scheme;
    char host[64];
    int port;
    bool result;
    
    result = parse_url("udp://sebastian:84", &scheme, host, & port);
    assert(result);
    assert(UNICAST == scheme);
    assert(equal("sebastian", host));
    assert(84 == port);
    
    result = parse_url("udpm://johncleese:39", &scheme, host, & port);
    assert(result);
    assert(MULTICAST == scheme);
    assert(equal("johncleese", host));
    assert(39 == port);
    
    result = parse_url("udp://wheelofcheese", &scheme, host, & port);
    assert(result);
    assert(UNICAST == scheme);
    assert(equal("wheelofcheese", host));
    assert(-1 == port);
    
    result = parse_url("wheelofcheese", &scheme, host, & port);
    assert(result);
    assert(NOT_SPECIFIED == scheme);
    assert(equal("wheelofcheese", host));
    assert(-1 == port);
    
    result = parse_url("wheelofcheese:42", &scheme, host, & port);
    assert(result);
    assert(NOT_SPECIFIED == scheme);
    assert(equal("wheelofcheese", host));
    assert(42 == port);
    
    msg("parse_url passed");
}


int main(
	int argc,
	char **argv)
{
    test_starts_with();
    test_contains();
    test_parse_scheme();
    test_contains_scheme();
    test_host_and_port();
    test_parse_url();

    printf("All tests succeeded.\n");

    return 0;
}
