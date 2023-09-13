.PHONY: all run-amd64 run-arm64
CLANG ?= clang-14
CFLAGS := -O2 -g -Wall -Werror $(CFLAGS)
NET_DEV ?= eth0
all: build_amd64

run-amd64:
	export BPF_CLANG=$(CLANG); \
	export BPF_CFLAGS="$(CFLAGS) -I/usr/include/x86_64-linux-gnu"; \
	go generate; \
	go run -exec sudo . $(NET_DEV)

run-arm64:
	export BPF_CLANG=$(CLANG); \
    export BPF_CFLAGS="$(CFLAGS) -I/usr/include/aarch64-linux-gnu"; \
    go generate; \
    go run -exec sudo . $(NET_DEV)