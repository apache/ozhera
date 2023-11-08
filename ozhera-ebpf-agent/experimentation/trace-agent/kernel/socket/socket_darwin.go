//go:build darwin

package socket

import (
	"context"
	"github.com/rikaaa0928/rtools/rctx"
)

func runSocketFilter(ctx context.Context, tcIface string, objs *bpfObjects) {
	rctx.WorkerAdd(ctx)
	defer rctx.WorkerDone(ctx)

	<-ctx.Done()
}
