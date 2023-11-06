//go:build linux

package socket

import (
	"context"
	"encoding/binary"
	"fmt"
	"net"
	"os"
	"syscall"
	"unsafe"

	"github.com/rikaaa0928/rtools/rctx"
)

const SO_ATTACH_BPF = 50

func runSocketFilter(ctx context.Context, tcIface string, objs *bpfObjects) {
	rctx.WorkerAdd(ctx)
	defer rctx.WorkerDone(ctx)

	devID, err := net.InterfaceByName(tcIface)
	if err != nil {
		fmt.Fprintf(os.Stderr, "could not get interface id: %v\n", err)
		return
	}

	sock, err := openRawSock(devID.Index)
	if err != nil {
		fmt.Fprintf(os.Stderr, "openRawSock error: %v\n", err)
		return
	}
	defer syscall.Close(sock)

	if err = syscall.SetsockoptInt(sock, syscall.SOL_SOCKET, SO_ATTACH_BPF, objs.bpfPrograms.MoneFilter.FD()); err != nil {
		fmt.Fprintf(os.Stderr, "SetsockoptInt error: %v\n", err)
		return
	}

	<-ctx.Done()
}

func openRawSock(index int) (int, error) {
	sock, err := syscall.Socket(syscall.AF_PACKET, syscall.SOCK_RAW|syscall.SOCK_NONBLOCK|syscall.SOCK_CLOEXEC, int(htons(syscall.ETH_P_ALL)))
	if err != nil {
		return 0, err
	}
	sll := syscall.SockaddrLinklayer{
		Ifindex:  index,
		Protocol: htons(syscall.ETH_P_ALL),
	}
	if err := syscall.Bind(sock, &sll); err != nil {
		return 0, err
	}
	return sock, nil
}

// htons converts the unsigned short integer hostshort from host byte order to network byte order.
func htons(i uint16) uint16 {
	b := make([]byte, 2)
	binary.BigEndian.PutUint16(b, i)
	return *(*uint16)(unsafe.Pointer(&b[0]))
}
