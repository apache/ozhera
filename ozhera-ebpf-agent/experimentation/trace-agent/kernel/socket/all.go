package socket

import (
	"context"
	"fmt"
	"log"
	"math/big"
	"net"
	"os"
	"strings"

	"trace-agent/kernel"
	"github.com/rikaaa0928/rtools/rctx"
)

//go:generate go run github.com/cilium/ebpf/cmd/bpf2go -cc clang-14 -cflags $BPF_CFLAGS -type event bpf http.c -- -Iheaders
type defaultImp struct {
	c       chan *kernel.PacketEvent
	objs    *bpfObjects
	started chan struct{}
	count   uint64
}

func (k *defaultImp) WaitStarted() {
	<-k.started
}

// SetFilterIPPorts implements kernel.Setting.
func (k *defaultImp) PutFilterIPPorts(ip net.IP, port uint16) {
	// log.Printf("PutFilterIPPorts Put %x", ip)
	ipInt := big.NewInt(0)
	ipInt.SetBytes(ip)
	log.Printf("PutFilterIPPorts Put IP %x", uint32(ipInt.Uint64())>>4)
	key := ipInt.Uint64() << 32
	key |= uint64(port)
	err := k.objs.bpfMaps.FilterMap.Put(key, byte(1))
	if err != nil {
		log.Printf("[ERROR] PutFilterIPPorts Put Key %s", err)
	}
	log.Printf("PutFilterIPPorts Put Key %x", key)
}

var _ kernel.Setting = &defaultImp{}

func NewDefaultImp() kernel.PacketKModel {
	return &defaultImp{c: make(chan *kernel.PacketEvent, 10), started: make(chan struct{})}
}

func (k *defaultImp) Start(ctx context.Context) {
	ifaceName := *rctx.DMapValue[string](ctx, "ifaceName")
	k.objs = &bpfObjects{}
	spec, err := loadBpf()
	if err != nil {
		fmt.Fprintf(os.Stderr, "loading bpf: %s", err)
	}
	if err := loadBpfObjects(k.objs, nil); err != nil {
		for k, v := range spec.Programs {
			fmt.Fprintf(os.Stderr, "%s: %s\n", k, v.Instructions.String())
		}
		fmt.Fprintf(os.Stderr, "loading objects: %s\n", err)
		return
	}
	defer k.objs.Close()
	close(k.started)
	ifs := strings.Split(ifaceName, ",")
	for _, ifName := range ifs {
		go runSocketFilter(ctx, ifName, k.objs)
	}

	go k.readMap(ctx)
	<-ctx.Done()
}

func (k *defaultImp) Events() chan *kernel.PacketEvent {
	return k.c
}
