#include <inttypes.h>
#include <stdio.h>

#ifndef WIN32
#include <sys/select.h>
typedef int SOCKET;
#else
#include <winsock2.h>
#endif

#include "common.h"

#define info(...)                     \
    do {                              \
        fprintf(stderr, "server: ");  \
        fprintf(stderr, __VA_ARGS__); \
    } while (0)

#define CHECK_FIELD(field, expected, fmt)                                                       \
    if ((field) != (expected)) {                                                                \
        info("Expected " #field " to be: " fmt ", got " fmt " instead\n", (expected), (field)); \
        return 0;                                                                               \
    }

#if defined(_MSC_VER) && _MSC_VER < 1900

#define snprintf c99_snprintf
#define vsnprintf c99_vsnprintf

inline int c99_vsnprintf(char *outBuf, size_t size, const char *format, va_list ap)
{
    int count = -1;

    if (size != 0)
        count = _vsnprintf_s(outBuf, size, _TRUNCATE, format, ap);
    if (count == -1)
        count = _vscprintf(format, ap);

    return count;
}

inline int c99_snprintf(char *outBuf, size_t size, const char *format, ...)
{
    int count;
    va_list ap;

    va_start(ap, format);
    count = c99_vsnprintf(outBuf, size, format, ap);
    va_end(ap);

    return count;
}

#endif

// Avoid clashing with predefined _strdup
#ifndef _MSC_VER
char *_strdup(const char *src)
{
    int len = strlen(src);
    char *result = malloc(len + 1);
    memcpy(result, src, len + 1);
    return result;
}
#endif

int check_lcmtest_multidim_array_t(const lcmtest_multidim_array_t *msg, int expected)
{
    CHECK_FIELD(msg->size_a, expected, "%d");
    CHECK_FIELD(msg->size_b, expected, "%d");
    CHECK_FIELD(msg->size_c, expected, "%d");

    int i, j, k;
    int n = 0;
    // data
    for (i = 0; i < msg->size_a; i++) {
        for (j = 0; j < msg->size_b; j++) {
            for (k = 0; k < msg->size_c; k++) {
                CHECK_FIELD(msg->data[i][j][k], n, "%d");
                n++;
            }
        }
    }

    // strarray
    char expected_buf[80];
    n = 0;
    for (i = 0; i < 2; i++) {
        for (k = 0; k < msg->size_c; k++) {
            snprintf(expected_buf, 79, "%d", n);
            if (strcmp(expected_buf, msg->strarray[i][k])) {
                info("Expected msg->strarray[%d][%d] to be %s, got %s instead\n", i, k,
                     expected_buf, msg->strarray[i][k]);
                return 0;
            }
            n++;
        }
    }

    return 1;
}

void fill_lcmtest_multidim_array_t(int num, lcmtest_multidim_array_t *msg)
{
    msg->size_a = num;
    msg->size_b = num;
    msg->size_c = num;

    msg->data = (int32_t ***) malloc(num * sizeof(int32_t ***));
    int i, j, k;
    int n = 0;
    // data
    for (i = 0; i < num; i++) {
        msg->data[i] = (int32_t **) malloc(num * sizeof(int32_t **));
        for (j = 0; j < num; j++) {
            msg->data[i][j] = (int32_t *) malloc(num * sizeof(int32_t));
            for (k = 0; k < num; k++) {
                msg->data[i][j][k] = n;
                n++;
            }
        }
    }

    char buf[80];
    // strarray
    msg->strarray = (char ***) malloc(2 * sizeof(char ***));
    n = 0;
    for (i = 0; i < 2; i++) {
        msg->strarray[i] = (char **) malloc(msg->size_c * sizeof(char **));
        for (k = 0; k < msg->size_c; k++) {
            snprintf(buf, 79, "%d", n);
            msg->strarray[i][k] = _strdup(buf);
            n++;
        }
    }
}

void clear_lcmtest_multidim_array_t(lcmtest_multidim_array_t *msg)
{
    int i, j, k;
    // data
    for (i = 0; i < msg->size_a; i++) {
        for (j = 0; j < msg->size_b; j++) {
            free(msg->data[i][j]);
        }
        free(msg->data[i]);
    }
    free(msg->data);

    // strarray
    for (i = 0; i < 2; i++) {
        for (k = 0; k < msg->size_c; k++) {
            free(msg->strarray[i][k]);
        }
        free(msg->strarray[i]);
    }
    free(msg->strarray);
}

int check_lcmtest_node_t(const lcmtest_node_t *msg, int expected)
{
    CHECK_FIELD(msg->num_children, expected, "%d");
    if (!msg->num_children)
        return 1;

    int i;
    for (i = 0; i < msg->num_children; i++) {
        const lcmtest_node_t *child = &msg->children[i];
        if (!check_lcmtest_node_t(child, msg->num_children - 1))
            return 0;
    }
    return 1;
}

void fill_lcmtest_node_t(int num_children, lcmtest_node_t *result)
{
    result->num_children = num_children;
    if (!num_children) {
        result->children = NULL;
        return;
    }
    result->children = (lcmtest_node_t *) malloc(num_children * sizeof(lcmtest_node_t));
    for (int i = 0; i < num_children; i++) {
        fill_lcmtest_node_t(num_children - 1, &result->children[i]);
    }
}

void clear_lcmtest_node_t(lcmtest_node_t *msg)
{
    for (int i = 0; i < msg->num_children; i++) {
        clear_lcmtest_node_t(&msg->children[i]);
    }
    free(msg->children);
}

int check_lcmtest_primitives_list_t(const lcmtest_primitives_list_t *msg, int expected)
{
    CHECK_FIELD(msg->num_items, expected, "%d");
    for (int n = 0; n < expected; n++) {
        const lcmtest_primitives_t *ex = &msg->items[n];
        CHECK_FIELD(ex->i8, -(n % 100), "%d");
        CHECK_FIELD(ex->i16, -n * 10, "%d");
        CHECK_FIELD(ex->i64, (int64_t)(-n * 10000), "%" PRId64);
        CHECK_FIELD(ex->position[0], (double) -n, "%f");
        CHECK_FIELD(ex->position[1], (double) -n, "%f");
        CHECK_FIELD(ex->position[2], (double) -n, "%f");
        CHECK_FIELD(ex->orientation[0], (double) -n, "%f");
        CHECK_FIELD(ex->orientation[1], (double) -n, "%f");
        CHECK_FIELD(ex->orientation[2], (double) -n, "%f");
        CHECK_FIELD(ex->orientation[3], (double) -n, "%f");
        CHECK_FIELD(ex->num_ranges, n, "%d");
        int i;
        for (i = 0; i < n; i++)
            CHECK_FIELD(ex->ranges[i], -i, "%d");
        char expected_name[100];
        sprintf(expected_name, "%d", -n);
        if (strcmp(expected_name, ex->name)) {
            info("Expected msg->items[%d].name to be %s, got %s instead\n", n, expected_name,
                 ex->name);
            return 0;
        }
        CHECK_FIELD(ex->enabled, (int) ((n + 1) % 2), "%d");
    }
    return 1;
}

void fill_lcmtest_primitives_list_t(int num, lcmtest_primitives_list_t *msg)
{
    int n;
    msg->num_items = num;
    msg->items = (lcmtest_primitives_t *) malloc(msg->num_items * sizeof(lcmtest_primitives_t));
    for (n = 0; n < num; n++) {
        lcmtest_primitives_t *ex = &msg->items[n];
        ex->i8 = -(n % 100);
        ex->i16 = -n * 10;
        ex->i64 = -n * 10000;
        ex->position[0] = -n;
        ex->position[1] = -n;
        ex->position[2] = -n;
        ex->orientation[0] = -n;
        ex->orientation[1] = -n;
        ex->orientation[2] = -n;
        ex->orientation[3] = -n;
        ex->num_ranges = n;
        ex->ranges = (int16_t *) malloc(n * sizeof(int16_t));
        int i;
        for (i = 0; i < n; i++) {
            ex->ranges[i] = -i;
        }
        char name[100];
        snprintf(name, 99, "%d", -n);
        ex->name = _strdup(name);
        ex->enabled = (n + 1) % 2;
    }
}

void clear_lcmtest_primitives_list_t(lcmtest_primitives_list_t *msg)
{
    int n;
    for (n = 0; n < msg->num_items; n++) {
        free(msg->items[n].ranges);
        free(msg->items[n].name);
    }
    free(msg->items);
}

int check_lcmtest_primitives_t(const lcmtest_primitives_t *msg, int expected)
{
    int n = expected;
    CHECK_FIELD(msg->i8, n % 100, "%d");
    CHECK_FIELD(msg->i16, n * 10, "%d");
    CHECK_FIELD(msg->i64, (int64_t)(n * 10000), "%" PRId64);
    CHECK_FIELD(msg->position[0], (double) n, "%f");
    CHECK_FIELD(msg->position[1], (double) n, "%f");
    CHECK_FIELD(msg->position[2], (double) n, "%f");
    CHECK_FIELD(msg->orientation[0], (double) n, "%f");
    CHECK_FIELD(msg->orientation[1], (double) n, "%f");
    CHECK_FIELD(msg->orientation[2], (double) n, "%f");
    CHECK_FIELD(msg->orientation[3], (double) n, "%f");
    CHECK_FIELD(msg->num_ranges, n, "%d");
    int i;
    for (i = 0; i < n; i++)
        CHECK_FIELD(msg->ranges[i], i, "%d");
    char expected_name[100];
    sprintf(expected_name, "%d", n);
    if (strcmp(expected_name, msg->name)) {
        info("Expected msg->expected_name to be %s, got %s instead\n", expected_name, msg->name);
        return 0;
    }
    CHECK_FIELD(msg->enabled, (int) (n % 2), "%d");
    return 1;
}

void fill_lcmtest_primitives_t(int n, lcmtest_primitives_t *msg)
{
    msg->i8 = n % 100;
    msg->i16 = n * 10;
    msg->i64 = n * 10000;
    msg->position[0] = n;
    msg->position[1] = n;
    msg->position[2] = n;
    msg->orientation[0] = n;
    msg->orientation[1] = n;
    msg->orientation[2] = n;
    msg->orientation[3] = n;
    msg->num_ranges = n;
    msg->ranges = (int16_t *) malloc(n * sizeof(int16_t));
    int i;
    for (i = 0; i < n; i++)
        msg->ranges[i] = i;
    char name_buf[80];
    snprintf(name_buf, 79, "%d", n);
    msg->name = _strdup(name_buf);
    msg->enabled = n % 2;
}

void clear_lcmtest_primitives_t(lcmtest_primitives_t *msg)
{
    free(msg->ranges);
    free(msg->name);
}

int check_lcmtest2_cross_package_t(const lcmtest2_cross_package_t *msg, int expected)
{
    return check_lcmtest_primitives_t(&msg->primitives, expected) &&
           check_lcmtest2_another_type_t(&msg->another, expected);
}

void fill_lcmtest2_cross_package_t(int n, lcmtest2_cross_package_t *msg)
{
    fill_lcmtest_primitives_t(n, &msg->primitives);
    fill_lcmtest2_another_type_t(n, &msg->another);
}

void clear_lcmtest2_cross_package_t(lcmtest2_cross_package_t *msg)
{
    clear_lcmtest_primitives_t(&msg->primitives);
    clear_lcmtest2_another_type_t(&msg->another);
}

int check_lcmtest2_another_type_t(const lcmtest2_another_type_t *msg, int expected)
{
    int n = expected;
    CHECK_FIELD(msg->val, n, "%d");
    return 1;
}

void fill_lcmtest2_another_type_t(int n, lcmtest2_another_type_t *msg)
{
    msg->val = n;
}

void clear_lcmtest2_another_type_t(lcmtest2_another_type_t *msg)
{
    return;
}

char *make_tmpnam()
{
#ifndef WIN32
    return tmpnam(NULL);
#else
    return _tempnam(NULL, NULL);
#endif
}

void free_tmpnam(char *tmpnam)
{
#ifdef WIN32
    free(tmpnam);
#endif
}