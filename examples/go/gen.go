package exlcm

// This file generates the example types using go generate which invokes lcm-gen
// Run using <go generate>

//go:generate lcm-gen -g ../types/exampleconst_t.lcm
//go:generate lcm-gen -g ../types/example_list_t.lcm
//go:generate lcm-gen -g ../types/example_t.lcm
//go:generate lcm-gen -g ../types/multidim_array_t.lcm
//go:generate lcm-gen -g ../types/node_t.lcm
