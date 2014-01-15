#include <stdio.h>
#include <inttypes.h>

#ifndef WIN32
#include <sys/select.h>
typedef int SOCKET;
#else
#include <winsock2.h>
#endif

#include "common.hpp"

#define info(...) do { fprintf(stderr, "cpp_client: "); fprintf(stderr, __VA_ARGS__); } while(0)

#define CHECK_FIELD(field, expected, fmt) \
    if((field) != (expected)) { \
        info("Expected " #field " to be: " fmt ", got " fmt " instead\n", (expected), (field)); \
        return 0; \
    }

int LcmHandleTimeout(lcm::LCM* lcm, int ms) {
  // setup the LCM file descriptor for waiting.
  SOCKET lcm_fd = lcm->getFileno();
  fd_set fds;
  FD_ZERO(&fds);
  FD_SET(lcm_fd, &fds);

  // wait a limited amount of time for an incoming message
  struct timeval timeout = { ms / 1000,           // seconds
  (ms % 1000) * 1000   // microseconds
  };
  int status = select(lcm_fd + 1, &fds, 0, 0, &timeout);
  if (status > 0 && FD_ISSET(lcm_fd, &fds)) {
    lcm->handle();
    return 1;
  }

  // no messages
  return 0;
}

int CheckLcmType(const lcmtest::multidim_array_t* msg, int expected) {
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
      if (strcmp(expected_buf, msg->strarray[i][k].c_str())) {
        info("Expected msg->strarray[%d][%d] to be %s, got %s instead\n", i, k,
             expected_buf, msg->strarray[i][k].c_str());
        return 0;
      }
      n++;
    }
  }

  return 1;
}

void FillLcmType(int num, lcmtest::multidim_array_t* msg) {
  msg->size_a = num;
  msg->size_b = num;
  msg->size_c = num;

  msg->data.resize(num);
  int i, j, k;
  int n = 0;
  // data
  for (i = 0; i < num; i++) {
    msg->data[i].resize(num);
    for (j = 0; j < num; j++) {
      msg->data[i][j].resize(num);
      for (k = 0; k < num; k++) {
        msg->data[i][j][k] = n;
        n++;
      }
    }
  }

  char buf[80];
  // strarray
  msg->strarray.resize(2);
  n = 0;
  for (i = 0; i < 2; i++) {
    msg->strarray[i].resize(msg->size_c);
    for (k = 0; k < msg->size_c; k++) {
      snprintf(buf, 79, "%d", n);
      msg->strarray[i][k] = buf;
      n++;
    }
  }
}

void ClearLcmType(lcmtest::multidim_array_t* msg) {
  int i, j, k;
  // data
  for (i = 0; i < msg->size_a; i++) {
    for (j = 0; j < msg->size_b; j++) {
      msg->data[i][j].clear();
    }
    msg->data[i].clear();
  }
  msg->data.clear();

  // strarray
  for (i = 0; i < 2; i++) {
    for (k = 0; k < msg->size_c; k++) {
      msg->strarray[i][k].clear();
    }
    msg->strarray[i].clear();
  }
}

int CheckLcmType(const lcmtest::node_t* msg, int expected) {
  CHECK_FIELD(msg->num_children, expected, "%d");
  if (!msg->num_children)
    return 1;

  int i;
  for (i = 0; i < msg->num_children; i++) {
    const lcmtest::node_t* child = &msg->children[i];
    if (!CheckLcmType(child, msg->num_children - 1))
      return 0;
  }
  return 1;
}

void FillLcmType(int num_children, lcmtest::node_t* result) {
  result->num_children = num_children;
  if (!num_children) {
    result->children.clear();
    return;
  }
  result->children.resize(num_children);
  for (int i = 0; i < num_children; i++) {
    FillLcmType(num_children - 1, &result->children[i]);
  }
}

void ClearLcmType(lcmtest::node_t* msg) {
  for (int i = 0; i < msg->num_children; i++) {
    ClearLcmType(&msg->children[i]);
  }
  msg->children.clear();
}

int CheckLcmType(const lcmtest::primitives_list_t* msg, int expected)
{
    CHECK_FIELD(msg->num_items, expected, "%d");
    for(int n=0; n<expected; n++)
    {
        const lcmtest::primitives_t* ex = &msg->items[n];
        CHECK_FIELD(ex->i8, -(n % 100), "%d");
        CHECK_FIELD(ex->i16, -n * 10, "%d");
        CHECK_FIELD(ex->i64, (int64_t)(-n * 10000), "%jd");
        CHECK_FIELD(ex->position[0], (double)-n, "%f");
        CHECK_FIELD(ex->position[1], (double)-n, "%f");
        CHECK_FIELD(ex->position[2], (double)-n, "%f");
        CHECK_FIELD(ex->orientation[0], (double)-n, "%f");
        CHECK_FIELD(ex->orientation[1], (double)-n, "%f");
        CHECK_FIELD(ex->orientation[2], (double)-n, "%f");
        CHECK_FIELD(ex->orientation[3], (double)-n, "%f");
        CHECK_FIELD(ex->num_ranges, n, "%d");
        int i;
        for(i=0; i<n; i++)
            CHECK_FIELD(ex->ranges[i], -i, "%d");
        char expected_name[100];
        sprintf(expected_name, "%d", -n);
        if(strcmp(expected_name, ex->name.c_str())) {
            info("Expected msg->items[%d].name to be %s, got %s instead\n", n, expected_name, ex->name.c_str());
            return 0;
        }
        CHECK_FIELD(ex->enabled, (int)((n+1)%2), "%d");
    }
    return 1;
}

void FillLcmType(int num, lcmtest::primitives_list_t* msg)
{
    int n;
    msg->num_items = num;
    msg->items.resize(msg->num_items);
    for(n=0; n<num; n++) {
        lcmtest::primitives_t* ex = &msg->items[n];
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
        ex->ranges.resize(n);
        int i;
        for(i=0; i<n; i++) {
            ex->ranges[i] = -i;
        }
        char name[100];
        snprintf(name, 99, "%d", -n);
        ex->name = name;
        ex->enabled = (n+1) % 2;
    }
}

void ClearLcmType(lcmtest::primitives_list_t* msg)
{
    msg->items.clear();
}

int CheckLcmType(const lcmtest::primitives_t* msg, int expected) {
  int n = expected;
  CHECK_FIELD(msg->i8, n % 100, "%d");
  CHECK_FIELD(msg->i16, n * 10, "%d");
  CHECK_FIELD(msg->i64, (int64_t )(n * 10000), "%jd");
  CHECK_FIELD(msg->position[0], (double )n, "%f");
  CHECK_FIELD(msg->position[1], (double )n, "%f");
  CHECK_FIELD(msg->position[2], (double )n, "%f");
  CHECK_FIELD(msg->orientation[0], (double )n, "%f");
  CHECK_FIELD(msg->orientation[1], (double )n, "%f");
  CHECK_FIELD(msg->orientation[2], (double )n, "%f");
  CHECK_FIELD(msg->orientation[3], (double )n, "%f");
  CHECK_FIELD(msg->num_ranges, n, "%d");
  int i;
  for (i = 0; i < n; i++)
    CHECK_FIELD(msg->ranges[i], i, "%d");
  char expected_name[100];
  sprintf(expected_name, "%d", n);
  if (strcmp(expected_name, msg->name.c_str())) {
    info("Expected msg->expected_name to be %s, got %s instead\n",
         expected_name, msg->name.c_str());
    return 0;
  }
  CHECK_FIELD(msg->enabled, (int )(n % 2), "%d");
  return 1;
}

void FillLcmType(int n, lcmtest::primitives_t* msg) {
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
  msg->ranges.resize(n);
  int i;
  for (i = 0; i < n; i++)
    msg->ranges[i] = i;
  char name_buf[80];
  snprintf(name_buf, 79, "%d", n);
  msg->name = name_buf;
  msg->enabled = n % 2;
}

void ClearLcmType(lcmtest::primitives_t* msg) {
  msg->ranges.clear();
  msg->name.clear();
}

int CheckLcmType(const lcmtest2::cross_package_t* msg, int expected) {
  return CheckLcmType(&msg->primitives, expected) &&
      CheckLcmType(&msg->another, expected);
}

void FillLcmType(int n, lcmtest2::cross_package_t* msg) {
  FillLcmType(n, &msg->primitives);
  FillLcmType(n, &msg->another);
}

void ClearLcmType(lcmtest2::cross_package_t* msg) {
  ClearLcmType(&msg->primitives);
  ClearLcmType(&msg->another);
}

int CheckLcmType(const lcmtest2::another_type_t* msg, int expected) {
    int n = expected;
    CHECK_FIELD(msg->val, n, "%d");
    return 1;
}

void FillLcmType(int n, lcmtest2::another_type_t* msg)
{
    msg->val = n;
}

void ClearLcmType(lcmtest2::another_type_t* msg)
{
    return;
}
