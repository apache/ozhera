package user

import (
	"context"

	"trace-agent/kernel"
)

type ExportModel interface {
	Handle(context.Context, chan *kernel.PacketEvent)
	Init(context.Context)
}
