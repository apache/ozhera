package main

import (
	"context"
	"github.com/rikaaa0928/rtools/rstring"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"strconv"
	"strings"

	_ "net/http/pprof"

	kernel2 "trace-agent/kernel"
	kernel "trace-agent/kernel/socket"
	"trace-agent/user/ot"

	"github.com/rikaaa0928/rtools/rctx"
)

func init() {
	type rDefaultKey string
	var k rDefaultKey = "k"
	rctx.DefaultMapKey(k)
}

func main() {
	if len(os.Args) < 2 {
		log.Fatalf("Please specify a network interface")
	}
	if len(os.Getenv("EBPF_AGENT_LOG_PATH")) > 0 {
		f, err := os.OpenFile(os.Getenv("EBPF_AGENT_LOG_PATH"), os.O_RDWR|os.O_CREATE|os.O_APPEND, 0666)
		if err != nil {
			log.Fatalf("error opening file: %v", err)
		}
		defer f.Close()
		mw := io.MultiWriter(os.Stdout, f)
		log.SetOutput(mw)
	}

	ctx := rctx.InitAppTerm(context.Background(), func() {
		log.Println("stop")
	})
	ctx = rctx.WithWorkerWaiter(ctx)
	// Look up the network interface by name.
	ifaceName := os.Args[1]
	ctx = rctx.DWithMapValue(ctx, "ifaceName", ifaceName)
	kModel := kernel.NewDefaultImp()
	setter, ok := kModel.(kernel2.Setting)
	if ok {
		go func() {
			setter.WaitStarted()
			filters := os.Getenv("EBPF_AGENT_FILTER")
			for _, v := range strings.Split(filters, ",") {
				ip := "0.0.0.0"
				port := v
				kv := strings.SplitN(v, ":", 2)
				if len(kv) == 2 {
					port = kv[1]
					ip = kv[0]
				}
				portI, err := strconv.ParseInt(port, 10, 64)
				if err != nil {
					continue
				}
				setter.PutFilterIPPorts(net.ParseIP(ip), uint16(portI))
			}
			//setter.PutFilterIPPorts(net.ParseIP("172.16.238.133"), 5000)
			//setter.PutFilterIPPorts(net.ParseIP("172.16.238.133"), 20000)
			//setter.PutFilterIPPorts(net.ParseIP("172.16.238.133"), 16686)
			//setter.PutFilterIPPorts(net.ParseIP("172.17.0.2"), 16686)
		}()
	}
	go kModel.Start(ctx)
	uModel := ot.NewOT(ctx)
	go uModel.Init(ctx)
	go uModel.Handle(ctx, kModel.Events())
	go func() {
		err := http.ListenAndServe(rstring.GetFirstAvaliable(os.Getenv("EBPF_AGENT_PPROF_PORT"), ":2809"), nil)
		if err != nil {
			log.Printf("[ERROR] http listen error %s", err)
		}
	}()
	<-ctx.Done()
	rctx.WaitAllWorker(ctx)
}
