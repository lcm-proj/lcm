add_subdirectory(gtest)

add_subdirectory(types)

add_subdirectory(c)
add_subdirectory(cpp)

if(LCM_ENABLE_PYTHON)
  add_subdirectory(python)
endif()

# TODO java

if(LCM_ENABLE_LUA)
  add_subdirectory(lua)
endif()
