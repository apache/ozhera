package socket

import (
	"bytes"
	"context"
	"encoding/binary"
	"errors"
	"log"
	"time"
	"unsafe"

	"github.com/cilium/ebpf/perf"
)

var droped uint32

func (k *defaultImp) readMap(ctx context.Context) {
	rd, err := perf.NewReader(k.objs.PayloadsPb, 1<<25)
	if err != nil {
		log.Fatalf("opening ringbuf reader: %s", err)
	}

	defer rd.Close()
	event := &bpfEvent{}
	headerSize := unsafe.Sizeof(*event)
	for {
		record, err := rd.Read()
		if err != nil {
			if errors.Is(err, perf.ErrClosed) {
				log.Println("Received signal, exiting..")
				return
			}
			log.Printf("reading from reader: %s", err)
			continue
		}
		if uintptr(len(record.RawSample)) < headerSize {
			continue
		}
		if err := binary.Read(bytes.NewBuffer(record.RawSample[:headerSize]), binary.LittleEndian, event); err != nil {
			log.Printf("parsing ringbuf event: %s", err)
			continue
		}

		e := GetEventInner(event, record.RawSample)
		// pid 在no CO-RE模式下的含义是丢包数量
		if e.Pid > 0 && e.Pid != droped {
			log.Printf("[ERROR] packet droped %d", e.Pid)
			droped = e.Pid
		}
		if k.count == 0 {
			log.Printf("first packet count %d %d", k.count, e.Tstamp)
			k.count = e.Tstamp
		} else {
			if k.count == e.Tstamp {
				log.Printf("[WARN] packet count dup user %d kernel %d", k.count, e.Tstamp)
			} else if k.count+1 != e.Tstamp {
				log.Printf("[WARN] packet count skipped user %d kernel %d", k.count, e.Tstamp)
			}
			k.count = e.Tstamp
		}
		//log.Printf("%d %d %d %s", len(record.RawSample), len(record.RawSample)-int(headerSize), event.Time, e)
		go func() {
			ctx2, cancel := context.WithTimeout(context.Background(), time.Minute)
			defer cancel()
			select {
			case <-ctx.Done():
				log.Println("[ERROR] write packet event to chanel canceled")
			case <-ctx2.Done():
				log.Println("[ERROR] write packet event to chanel timeout")
			case k.c <- e:
			}
		}()
	}
}
