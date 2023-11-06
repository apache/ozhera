package socket

import (
	"math/big"
	"net"
	"strings"
	"unsafe"

	"trace-agent/kernel"
	// "github.com/shirou/gopsutil/host"
)

// var bootTime uint64

// func init() {
// 	t, _ := host.BootTime()
// 	bootTime = t * uint64(time.Second/time.Microsecond)
// }

func GetIP(ip uint32) net.IP {
	return big.NewInt(int64(ip)).Bytes()
}

func GetEventInner(i *bpfEvent, data []byte) *kernel.PacketEvent {
	headerSize := uint(unsafe.Sizeof(*i))
	//log.Println("headerSize", headerSize)
	t := monotonicToTime(i.Time)
	// 	t := time.Unix(int64(i.Time/uint64(time.Second)), int64(i.Time%uint64(time.Second)))
	//log.Println(time.Now())
	//log.Println(t)
	//log.Println(time.UnixMicro(int64(bootTime) + int64(i.Time/(uint64(time.Microsecond/time.Nanosecond)))))
	//payload := make([]byte, 0)
	//if headerSize+uint(i.StartOffset) < uint(len(data)) {
	//	payload = data[headerSize+uint(i.StartOffset):]
	//}
	egress := false
	iface, err := net.InterfaceByIndex(int(i.Ifindex))
	if err == nil {
		addrs, err := iface.Addrs()
		if err == nil {
			for _, addr := range addrs {
				addrStr := addr.String()
				addrStr = strings.Split(addrStr, "/")[0]
				if addrStr == GetIP(i.Saddr).String() {
					egress = true
				}
			}
		}
	}

	return &kernel.PacketEvent{
		Saddr:  GetIP(i.Saddr),
		Daddr:  GetIP(i.Daddr),
		SaddrI: i.Saddr,
		DaddrI: i.Daddr,
		Source: i.Source,
		Dest:   i.Dest,
		Time:   t,
		Tstamp: i.Tstamp,
		//Payload:    payload,
		Pid:     i.Pid,
		Tgid:    i.Tgid,
		Egress:  egress,
		Ifindex: i.Ifindex,
		Seq:     i.Seq,
		FragOff: i.FragOff,
		//ProtoCheck: i.ProtoCheck,
		Raw:        data,
		Offset:     headerSize + uint(i.StartOffset),
		PayloadLen: i.Len,
		Metadata:   make(map[string]string),

		ReqSkey: kernel.SocketKey{
			Addr1: i.Saddr,
			Addr2: i.Daddr,
			Port1: i.Source,
			Port2: i.Dest,
		},
		RespSkey: kernel.SocketKey{
			Addr2: i.Saddr,
			Addr1: i.Daddr,
			Port2: i.Source,
			Port1: i.Dest,
		},
	}
}
