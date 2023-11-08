package protocol

import (
	"trace-agent/kernel"
	"go.opentelemetry.io/otel/trace"
)

type BaseInfo struct {
	L7ID  string
	L7Len *uint64
	//Req bool
}

type TransProtocol interface {
	CheckProtocol([]byte) (bool, bool)
	ReadInfo([]byte) (*BaseInfo, error)
	ReadSpan([]byte, *kernel.PacketEvent) (trace.Span, error)
	Name() string
}
