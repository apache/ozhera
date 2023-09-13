package main

import (
	"encoding/binary"
	"fmt"
	"log"
	"net"
	"os"
	"strings"
	"time"

	"github.com/cilium/ebpf"
	"github.com/cilium/ebpf/link"
	"github.com/florianl/go-tc"
	"github.com/florianl/go-tc/core"
	"github.com/mdlayher/netlink"
	"golang.org/x/sys/unix"
)

// $BPF_CLANG and $BPF_CFLAGS are set by the Makefile.
//go:generate go run github.com/cilium/ebpf/cmd/bpf2go -cc $BPF_CLANG -cflags $BPF_CFLAGS bpf http.c -- -Iheaders

func main() {
	if len(os.Args) < 2 {
		log.Fatalf("Please specify a network interface")
	}

	ifaceName := os.Args[1]

	objs := bpfObjects{}
	spec, err := loadBpf()
	if err != nil {
		fmt.Fprintf(os.Stderr, "loading bpf: %s", err)
	}
	if err := loadBpfObjects(&objs, nil); err != nil {
		for k, v := range spec.Programs {
			fmt.Fprintf(os.Stderr, "%s: %s\n", k, v.Instructions.String())
		}
		fmt.Fprintf(os.Stderr, "loading objects: %s\n", err)
		return
	}
	defer objs.Close()
	go runXDP(ifaceName,&objs)
	runTC(ifaceName,&objs)
}

func runXDP(ifaceName string, objs *bpfObjects) {
	iface, err := net.InterfaceByName(ifaceName)
	if err != nil {
		log.Fatalf("lookup network iface %q: %s", ifaceName, err)
	}


	// Attach the program.
	l, err := link.AttachXDP(link.XDPOptions{
		Program:   objs.XdpIngress,
		Interface: iface.Index,
	})
	if err != nil {
		log.Fatalf("could not attach XDP program: %s", err)
	}
	defer l.Close()
	log.Printf("Attached XDP program to iface %q (index %d)", iface.Name, iface.Index)
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()
	for range ticker.C {
		s, err := formatMapContents(objs.XdpStatsMap)
		if err != nil {
			log.Printf("Error reading map: %s", err)
			continue
		}
		log.Printf("Map contents:\n%s", s)
	}
}

func runTC(tcIface string,objs *bpfObjects) {
	devID, err := net.InterfaceByName(tcIface)
	if err != nil {
		fmt.Fprintf(os.Stderr, "could not get interface ID: %v\n", err)
		return
	}
	tcnl, err := tc.Open(&tc.Config{})
	if err != nil {
		fmt.Fprintf(os.Stderr, "could not open rtnetlink socket: %v\n", err)
		return
	}
	defer func() {
		if err := tcnl.Close(); err != nil {
			fmt.Fprintf(os.Stderr, "could not close rtnetlink socket: %v\n", err)
		}
	}()
	if err := tcnl.SetOption(netlink.ExtendedAcknowledge, true); err != nil {
		fmt.Fprintf(os.Stderr, "could not set option ExtendedAcknowledge: %v\n", err)
		return
	}
	qdisc := tc.Object{
		Msg: tc.Msg{
			Family:  unix.AF_UNSPEC,
			Ifindex: uint32(devID.Index),
			Handle:  core.BuildHandle(tc.HandleRoot, 0x0000),
			Parent:  tc.HandleIngress,
			Info:    0,
		},
		Attribute: tc.Attribute{
			Kind: "clsact",
		},
	}

	// Attach the qdisc/clsact to the networking interface.
	if err := tcnl.Qdisc().Add(&qdisc); err != nil {
		fmt.Fprintf(os.Stderr, "could not assign clsact to %s: %v\n", tcIface, err)
		return
	}
	// When deleting the qdisc, the applied filter will also be gone
	defer tcnl.Qdisc().Delete(&qdisc)

	
	fd := uint32(objs.TcEgress.FD())
	flags := uint32(0x1)

	// Create a tc/filter object that will attach the eBPF program to the qdisc/clsact.
	filter := tc.Object{
		Msg: tc.Msg{
			Family:  unix.AF_UNSPEC,
			Ifindex: uint32(devID.Index),
			Handle:  qdisc.Handle,
			Parent:  core.BuildHandle(tc.HandleRoot, tc.HandleMinEgress),
			Info:    0x300,
		},
		Attribute: tc.Attribute{
			Kind: "bpf",
			BPF: &tc.Bpf{
				FD:    &fd,
				Flags: &flags,
			},
		},
	}

	// Attach the tc/filter object with the eBPF program to the qdisc/clsact.
	if err := tcnl.Filter().Add(&filter); err != nil {
		fmt.Fprintf(os.Stderr, "could not attach filter for eBPF program: %v\n", err)
		return
	}

	fmt.Printf("attach filter for eBPF program")
	str := ""
	fmt.Scan(&str)
}


func formatMapContents(m *ebpf.Map) (string, error) {
	var (
		sb  strings.Builder
		key []byte
		val []byte
	)
	iter := m.Iterate()
	for iter.Next(&key, &val) {
		sb.WriteString(fmt.Sprintf("\t%d -> %d => %d %s\n", binary.BigEndian.Uint16(key[:2]), binary.BigEndian.Uint16(key[2:4]), binary.BigEndian.Uint64(val[:8]), val[8:]))
	}
	return sb.String(), iter.Err()
}
