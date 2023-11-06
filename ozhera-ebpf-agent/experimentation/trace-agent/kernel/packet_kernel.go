package kernel

import (
	"context"
	"fmt"
	"log"
	"net"
	"strconv"
	"time"
)

// PacketEvent pid Tgid  Egress目前在no CO-RE模式下无法获取,pid替换为丢包数量,FragOff暂时无用
type PacketEvent struct {
	Saddr   net.IP
	Daddr   net.IP
	SaddrI  uint32
	DaddrI  uint32
	Source  uint16
	Dest    uint16
	Time    time.Time
	Tstamp  uint64
	Pid     uint32
	Tgid    uint32
	Egress  bool
	Ifindex uint32
	Seq     uint32
	FragOff uint16
	//ProtoCheck uint8
	//Payload    []byte
	PayloadLen uint16
	Raw        []byte
	Offset     uint
	Metadata   map[string]string

	ReqSkey  SocketKey
	RespSkey SocketKey
}

type PacketKModel interface {
	Start(context.Context)
	Events() chan *PacketEvent
}

func (e *PacketEvent) IFName() string {
	iface, err := net.InterfaceByIndex(int(e.Ifindex))
	ifName := strconv.FormatInt(int64(e.Ifindex), 10)
	if err == nil {
		ifName = iface.Name
	}
	return ifName
}

func (e *PacketEvent) String() string {
	ifName := e.IFName()
	if len(e.Payload()) > 0 && e.Payload()[0] >= 'A' && e.Payload()[0] <= 'Z' {
		return fmt.Sprintf("\n[%s %d %d %d][%v %s] %s:%d -> %s:%d:\n%s\n", e.Time, e.Tstamp, e.Pid, e.Tgid, e.Egress, ifName, e.Saddr, e.Source, e.Daddr, e.Dest, e.Payload())
	}
	return fmt.Sprintf("\n[%s %d %d %d][%v %s] %s:%d -> %s:%d:\n%v\n", e.Time, e.Tstamp, e.Pid, e.Tgid, e.Egress, ifName, e.Saddr, e.Source, e.Daddr, e.Dest, e.Payload())
}

func (e *PacketEvent) ReqKey() SocketKey {
	//return fmt.Sprintf("%s:%d-%s:%d", e.Saddr, e.Source, e.Daddr, e.Dest)
	return e.ReqSkey
}

type SocketKey struct {
	Addr1 uint32
	Addr2 uint32
	Port1 uint16
	Port2 uint16
}

func (s SocketKey) String() string {
	return fmt.Sprintf("%d:%d-%d:%d", s.Addr1, s.Port1, s.Addr2, s.Port2)
}

func (e *PacketEvent) Key() SocketKey {
	if e.SaddrI < e.DaddrI || (e.SaddrI == e.DaddrI && e.Source < e.Dest) {
		// return SocketKey{
		// 	Addr1: e.SaddrI,
		// 	Addr2: e.DaddrI,
		// 	Port1: e.Source,
		// 	Port2: e.Dest,
		// }
		return e.ReqSkey
	}
	// return SocketKey{
	// 	Addr2: e.SaddrI,
	// 	Addr1: e.DaddrI,
	// 	Port2: e.Source,
	// 	Port1: e.Dest,
	// }
	return e.RespSkey
}

func (e *PacketEvent) RespKey() SocketKey {
	//return fmt.Sprintf("%s:%d-%s:%d", e.Daddr, e.Dest, e.Saddr, e.Source)
	return e.RespSkey
}

func (e *PacketEvent) Payload() []byte {
	//payload := make([]byte, 0)
	//if headerSize+uint(i.StartOffset) < uint(len(data)) {
	//	payload = data[headerSize+uint(i.StartOffset):]
	//}
	if e.Offset+uint(e.PayloadLen) > uint(len(e.Raw)) {
		log.Printf("[ERROR] payload len problem %s", e.Raw[e.Offset:])
		return e.Raw[e.Offset:]
	}
	return e.Raw[e.Offset : e.Offset+uint(e.PayloadLen)]
}

type Setting interface {
	WaitStarted()
	PutFilterIPPorts(net.IP, uint16)
}
