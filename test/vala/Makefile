# This Makefile was tested with GNU Make
#
VALAC=valac
LCM_GEN=../../lcmgen/lcm-gen
LCM_VSRC=../../lcm-vala/lcm_message.vala

VAPIDIR=../../lcm-vala/vapi
VALAEXTRAFLAGS=
VALAFLAGS=--vapidir=$(VAPIDIR) --pkg=lcm $(VALAEXTRAFLAGS)

types1:=bools_t byte_array_t comments_t exampleconst_t multidim_array_t node_t primitives_t primitives_list_t primitives_array_t
types2:=another_type_t cross_package_t
types_src:=$(types1:%=lcmtest.%.vala) $(types2:%=lcmtest2.%.vala)

all: buf-xfer \
	check-hash

buf-xfer: buf-xfer.vala $(LCM_VSRC)
	$(VALAC) $(VALAFLAGS) -o $@ $^

check-hash: check-hash.vala $(LCM_VSRC) $(types_src)
	$(VALAC) $(VALAFLAGS) -o $@ $^

lcmtest.%.vala: ../types/lcmtest/%.lcm $(LCM_GEN)
	$(LCM_GEN) --vala $<

lcmtest2.%.vala: ../types/lcmtest2/%.lcm
	$(LCM_GEN) --vala $<

clean:
	rm -f buf-xfer check-hash
	rm -f lcmtest* lcmtest2*
	rm -f *.o *.c
