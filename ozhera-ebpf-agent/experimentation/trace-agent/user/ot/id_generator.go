package ot

import (
	"context"
	crand "crypto/rand"
	"encoding/binary"
	"go.opentelemetry.io/otel/sdk/trace"
	ottrace "go.opentelemetry.io/otel/trace"
	"math/rand"
	"sync"
)

type bpfIDGenerator struct {
	sync.Mutex
	randSource *rand.Rand
}

func (gen *bpfIDGenerator) NewIDs(ctx context.Context) (ottrace.TraceID, ottrace.SpanID) {
	gen.Lock()
	defer gen.Unlock()
	tid := ottrace.TraceID{}
	_, _ = gen.randSource.Read(tid[:])
	sid := ottrace.SpanID{}
	_, _ = gen.randSource.Read(sid[:])
	return tid, sid
}

func (gen *bpfIDGenerator) NewSpanID(ctx context.Context, traceID ottrace.TraceID) ottrace.SpanID {
	psc := ottrace.SpanContextFromContext(ctx)
	noEmpty := false
	sid := ottrace.SpanID{}
	for i, v := range psc.SpanID() {
		if v > 0 {
			noEmpty = true
		}
		if i == len(psc.SpanID())-1 {
			to := byte(0x0a)
			if (v & 0x0f) == (to & 0x0f) {
				to = byte(0x0b)
			}
			sid[i] = (v & 0xf0) | (to & 0x0f)
			continue
		}
		sid[i] = v
	}
	if noEmpty {
		return sid
	}
	gen.Lock()
	defer gen.Unlock()
	//sid = ottrace.SpanID{}
	_, _ = gen.randSource.Read(sid[:])
	return sid
}

func NewBPFIDGenerator() trace.IDGenerator {
	gen := &bpfIDGenerator{}
	var rngSeed int64
	_ = binary.Read(crand.Reader, binary.LittleEndian, &rngSeed)
	gen.randSource = rand.New(rand.NewSource(rngSeed))
	return gen
}
