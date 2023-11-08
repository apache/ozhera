package pdubbo

import (
	"context"
	"encoding/hex"
	"trace-agent/kernel"
	"trace-agent/user/ot/protocol"
	hessian "github.com/apache/dubbo-go-hessian2"
	"github.com/rikaaa0928/rtools/generic"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/trace"
	"log"
	"math/big"
)

type DubboProto struct {
	propagator propagation.TraceContext
}

const dubboReqMask = uint8(1) << 7
const dubboHeaderLen = 16

// todo: one way check
func (p DubboProto) CheckProtocol(bytes []byte) (bool, bool) {
	if len(bytes) < 3 {
		return false, false
	}
	//log.Println(hex.EncodeToString(bytes[:2]))
	if bytes[0] != 0xda || bytes[1] != 0xbb {
		return false, false
	}
	return true, (bytes[2] & dubboReqMask) == dubboReqMask
}

func (p DubboProto) ReadInfo(bytes []byte) (*protocol.BaseInfo, error) {
	check, _ := p.CheckProtocol(bytes)
	if !check {
		return &protocol.BaseInfo{}, nil
	}
	if len(bytes) < 12 {
		return &protocol.BaseInfo{}, nil
	}
	id := bytes[4:12]
	info := &protocol.BaseInfo{L7ID: hex.EncodeToString(id)}
	if len(bytes) > dubboHeaderLen {
		l := big.NewInt(0)
		l.SetBytes(bytes[12:16])
		info.L7Len = generic.TakeAddr(l.Uint64() + dubboHeaderLen)
	}
	log.Printf("dubbo %s %d", info.L7ID, *info.L7Len)
	return info, nil
}

func (p DubboProto) ReadSpan(bytes []byte, e *kernel.PacketEvent) (trace.Span, error) {
	p.parseData(bytes)

	log.Printf("DubboProto ReadSpan %d", len(bytes))
	att := []attribute.KeyValue{
		attribute.Int("Ifindex", int(e.Ifindex)),
		attribute.String("net.sock.host.addr", func() string {
			if e.Egress {
				return e.Saddr.String()
			}
			return e.Daddr.String()
		}()),
		attribute.String("net.sock.peer.addr", func() string {
			if !e.Egress {
				return e.Saddr.String()
			}
			return e.Daddr.String()
		}()),
		attribute.Int("net.sock.host.port", func() int {
			if e.Egress {
				return int(e.Source)
			}
			return int(e.Dest)
		}()),
		attribute.Int("net.sock.peer.port", func() int {
			if !e.Egress {
				return int(e.Source)
			}
			return int(e.Dest)
		}()),
	}
	_, span := otel.Tracer("ebpf-tracer").Start(
		context.Background(),
		"ebpf-dubbo-tracing",
		trace.WithTimestamp(e.Time),
		trace.WithSpanKind(func() trace.SpanKind {
			if !e.Egress {
				return trace.SpanKindServer
			}
			return trace.SpanKindClient
		}()),
		trace.WithAttributes(att...),
	)
	return span, nil
}

func (p DubboProto) Name() string {
	return "dubbo"
}

var _ protocol.TransProtocol = &DubboProto{}

func (p DubboProto) parseData(bytes []byte) (att map[string]interface{}) {
	log.Printf("dubbo magic %x", bytes[:2])
	decoder := hessian.NewDecoder(bytes[16:])

	dubboVersion, err := decoder.Decode()
	if err != nil {
		log.Printf("[ERROR] hessian decode error %s", err)
		return
	}
	log.Printf("dubbo dubboVersion %v", dubboVersion)

	target, err := decoder.Decode()
	if err != nil {
		log.Printf("[ERROR] hessian decode error %s", err)
		return
	}
	log.Printf("dubbo target %v", target)

	serviceVersion, err := decoder.Decode()
	if err != nil {
		log.Printf("[ERROR] hessian decode error %s", err)
		return
	}
	log.Printf("dubbo serviceVersion %v", serviceVersion)

	method, err := decoder.Decode()
	if err != nil {
		log.Printf("[ERROR] hessian decode error %s", err)
		return
	}
	log.Printf("dubbo method %v", method)

	argsTypes, err := decoder.Decode()
	if err != nil {
		log.Printf("[ERROR] hessian decode error %s", err)
		return
	}
	log.Printf("dubbo argsTypes %v", argsTypes)
	ats := hessian.DescRegex.FindAllString(argsTypes.(string), -1)
	//var arg interface{}
	for i := 0; i < len(ats); i++ {
		_, err = decoder.Decode()
		if err != nil {
			log.Printf("[ERROR] hessian decode error %s", err)
			return
		}
		log.Printf("dubbo arg")
	}
	attachments, err := decoder.Decode()
	if err != nil {
		log.Printf("[ERROR] hessian decode error %s", err)
		return
	}
	log.Printf("dubbo attachments %v", attachments)
	return
}
