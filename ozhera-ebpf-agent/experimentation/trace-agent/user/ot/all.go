package ot

import (
	"context"
	"trace-agent/kernel"
	"trace-agent/user"
	"trace-agent/user/ot/vconnection"
	"github.com/rikaaa0928/rtools/rctx"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/trace"
	"log"
	"sync"
)

type otImpl struct {
	spanMap    map[kernel.SocketKey]trace.Span
	mapLock    sync.Mutex
	propagator propagation.TraceContext
	vConnMan   *vConnectionManager
}

func NewOT(ctx context.Context) user.ExportModel {
	u := &otImpl{
		spanMap:  make(map[kernel.SocketKey]trace.Span),
		vConnMan: &vConnectionManager{vConnMap: make(map[kernel.SocketKey]*vconnection.VConnection)},
	}
	go u.vConnMan.Clean(ctx)
	return u
}

func (u *otImpl) Handle(ctx context.Context, c chan *kernel.PacketEvent) {
	for {
		select {
		case <-ctx.Done():
			return
		case e := <-c:
			u.handelEvent(ctx, e)
		}
	}
}

func (u *otImpl) handelEvent(ctx context.Context, e *kernel.PacketEvent) {
	go u.vConnMan.NewEvent(e)
}

func (u *otImpl) Init(ctx context.Context) {
	rctx.WorkerAdd(ctx)
	defer rctx.WorkerDone(ctx)
	log.Printf("Waiting for connection...")

	//ctx, cancel := signal.NotifyContext(ctx, os.Interrupt)
	//defer cancel()

	shutdown, err := initProvider()
	if err != nil {
		log.Println(err)
		return
	}
	defer func() {
		if err := shutdown(context.Background()); err != nil {
			log.Printf("failed to shutdown TracerProvider: %s", err)
		}
	}()

	//u.tracer = otel.Tracer("ebpf-tracer")
	<-ctx.Done()
}
