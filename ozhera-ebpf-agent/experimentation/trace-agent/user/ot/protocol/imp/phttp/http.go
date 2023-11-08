package phttp

import (
	"context"
	"fmt"
	"trace-agent/kernel"
	"trace-agent/user/ot/protocol"
	"github.com/rikaaa0928/rtools/rstring"
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/trace"
	"log"
	"net/http"
	"net/textproto"
	"os"
	"strconv"
	"strings"
	"unsafe"
)

type HttpProto struct {
	propagator propagation.TraceContext
}

func (p HttpProto) Name() string {
	return "http"
}

func filter(bytes []byte) bool {
	pathFilter := os.Getenv("EBPF_AGENT_HTTP_PATH_FILTER")
	if len(pathFilter) == 0 {
		return true
	}
	str := unsafe.String(unsafe.SliceData(bytes), len(bytes))
	strs := strings.Split(str, "\r\n")
	parts := strings.SplitN(strs[0], " ", 3)
	if len(parts) < 3 {
		return false
	}
	filters := strings.Split(pathFilter, ",")
	for _, f := range filters {
		if f == strings.TrimSpace(parts[1]) {
			return true
		}
	}
	return false
}

func (p HttpProto) CheckProtocol(bytes []byte) (bool, bool) {
	if len(bytes) < 6 {
		return false, false
	}
	str := unsafe.String(unsafe.SliceData(bytes[:6]), len(bytes[:6]))
	if strings.HasPrefix(str, "HTTP") {
		return true, false
	}
	if strings.HasPrefix(str, http.MethodGet+" ") {
		if !filter(bytes) {
			return false, false
		}
		return true, true
	}
	if strings.HasPrefix(str, http.MethodPost+" ") {
		if !filter(bytes) {
			return false, false
		}
		return true, true
	}
	if strings.HasPrefix(str, http.MethodPut+" ") {
		if !filter(bytes) {
			return false, false
		}
		return true, true
	}
	if strings.HasPrefix(str, http.MethodConnect+" ") {
		if !filter(bytes) {
			return false, false
		}
		return true, true
	}
	if strings.HasPrefix(str, http.MethodDelete+" ") {
		if !filter(bytes) {
			return false, false
		}
		return true, true
	}
	if strings.HasPrefix(str, http.MethodPatch+" ") {
		if !filter(bytes) {
			return false, false
		}
		return true, true
	}
	return false, false
}

func (p HttpProto) ReadInfo(bytes []byte) (*protocol.BaseInfo, error) {
	check, _ := p.CheckProtocol(bytes)
	if !check {
		return &protocol.BaseInfo{}, nil
	}
	info := &protocol.BaseInfo{L7ID: "http"}
	str := unsafe.String(unsafe.SliceData(bytes), len(bytes))
	parts := strings.SplitN(str, "\r\n\r\n", 2)
	if len(parts) == 2 {
		var bodyLen uint64
		//log.Printf("HttpProto ReadInfo head %d body %d", len(parts[0]), len(parts[1]))
		headers := parts[0]
		for _, header := range strings.Split(headers, "\r\n") {
			kv := strings.SplitN(header, ":", 2)
			if len(kv) != 2 {
				continue
			}
			// todo：Transfer-Encoding: chunked
			if strings.EqualFold(kv[0], "Content-Length") {
				var err error
				lenStr := strings.TrimSpace(kv[1])
				bodyLen, err = strconv.ParseUint(lenStr, 10, 64)
				if err != nil {
					return nil, fmt.Errorf("HttpProto ReadInfo ParseUint %s error %s", lenStr, err)
				}
				//log.Printf("HttpProto ReadInfo read http body len %d", bodyLen)
			}
		}
		sumLen := uint64(len(headers+"\r\n\r\n")) + bodyLen
		//log.Printf("HttpProto ReadInfo http parsed sumLen %d headerLen %d bodyLen %d", sumLen, len(headers+"\r\n\r\n"), bodyLen)
		info.L7Len = &sumLen
	}
	return info, nil
}

func (p *HttpProto) ReadSpan(bytes []byte, e *kernel.PacketEvent) (trace.Span, error) {
	headers, att := getHeaders(bytes)
	ctx := p.propagator.Extract(context.Background(), propagation.HeaderCarrier(headers))
	att = append(att,
		attribute.Int("Ifindex", int(e.Ifindex)),
		attribute.String("IfName", e.IFName()),
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
	)
	name := readFromAtts(att, "http.target")
	if e.Egress {
		name = fmt.Sprintf("%s %s",
			"HTTP",
			readFromAtts(att, "http.method"))
	}
	tName := "ebpf-" + rstring.GetFirstAvaliable(os.Getenv("mione.app.name"), "tracer")
	_, span := otel.Tracer(tName).Start(
		ctx,
		name,
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

var _ protocol.TransProtocol = &HttpProto{}

func getHeaders(pl []byte) (textproto.MIMEHeader, []attribute.KeyValue) {
	atts := make([]attribute.KeyValue, 0, 4)
	str := unsafe.String(unsafe.SliceData(pl), len(pl))
	parts := strings.Split(str, "\r\n\r\n")
	res := make(textproto.MIMEHeader)
	headerStr := parts[0]
	headerLines := strings.Split(headerStr, "\r\n")
	if len(headerLines) < 2 {
		log.Printf("header parse error: %s %v", str, headerLines)
		return nil, nil
	}
	params := strings.SplitN(headerLines[0], " ", 3)
	if len(params) >= 1 {
		atts = append(atts, attribute.String("http.method", params[0]))
	}
	if len(params) >= 2 {
		atts = append(atts, attribute.String("http.target", params[1]))
		atts = append(atts, attribute.String("http.route", params[1]))
	}
	if len(params) >= 3 {
		atts = append(atts, attribute.String("http.version", params[2]))
	}
	for i := 1; i < len(headerLines); i++ {
		line := headerLines[i]
		kv := strings.SplitN(line, ":", 2)
		if len(kv) != 2 {
			continue
		}
		res.Add(kv[0], strings.TrimSpace(kv[1]))
	}
	return res, atts
}

func readFromAtts(att []attribute.KeyValue, key string) string {
	for _, v := range att {
		if string(v.Key) == key {
			return v.Value.AsString()
		}
	}
	return ""
}
