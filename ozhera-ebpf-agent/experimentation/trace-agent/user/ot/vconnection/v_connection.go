package vconnection

import (
	"trace-agent/kernel"
	"trace-agent/user/ot/protocol"
	"go.opentelemetry.io/otel/trace"
	"log"
	"sync"
	"time"
)

type VConnection struct {
	spanMap    map[string][]trace.Span
	reqDirect  *vDirection
	respDirect *vDirection
	proto      protocol.TransProtocol // 并发安全
	ProtoID    uint64
	reqKey     kernel.SocketKey // 确定请求方向
	sync.RWMutex
}

// 第一个包必须要确定请求方向,等第一个请求头置顶的包
func NewVConnection(e *kernel.PacketEvent) *VConnection {
	pID, proto, req := protocol.Identify(e.Payload())
	//log.Printf("NewVConnection Identify %v %v", pID, req)
	if pID == nil || proto == nil || !req {
		return nil
	}
	info, err := proto.ReadInfo(e.Payload())
	if err != nil || info == nil /*info should not be nil*/ {
		log.Printf("[%d proto] ReadInfo error: %s", *pID, err)
		return nil
	}
	c := &VConnection{
		spanMap:    make(map[string][]trace.Span),
		respDirect: newVDirection(),
		reqDirect:  newVDirection(),
		proto:      proto,
		ProtoID:    *pID,
		reqKey:     e.ReqKey(),
	}
	log.Printf("new v connection %s", e.ReqSkey)
	go c.HandelPackets(c.reqDirect.c, c.respDirect.c)
	c.reqDirect.NewEvent(e, proto)
	return c
}

func (c *VConnection) NewEvent(e *kernel.PacketEvent) {
	req := e.ReqKey() == c.reqKey
	if req {
		log.Printf("new req %s", e.ReqKey())
		c.reqDirect.NewEvent(e, c.proto)
		return
	}
	log.Printf("new resp %s", e.ReqKey())
	c.respDirect.NewEvent(e, c.proto)
}

func (c *VConnection) Close() {
	close(c.reqDirect.c)
	close(c.respDirect.c)
}

func (c *VConnection) HandelPackets(reqC <-chan *kernel.PacketEvent, respC <-chan *kernel.PacketEvent) {
	// response流程会阻塞循环，所以request和response分两个循环处理
	go func() {
	LOOP:
		for {
			select {
			case l7p, ok := <-reqC:
				if !ok {
					break LOOP
				}
				func(l7p *kernel.PacketEvent) {
					data := l7p.Raw
					info, err := c.proto.ReadInfo(data)
					if err != nil {
						log.Printf("[ERROR] HandelPackets ReadInfo error: %s", err)
						return
					}
					span, err := c.proto.ReadSpan(data, l7p)
					if err != nil {
						log.Printf("[ERROR] HandelPackets ReadSpan error: %s", err)
						return
					}
					c.Lock()
					defer c.Unlock()
					spans, ok := c.spanMap[info.L7ID]
					if !ok {
						spans = make([]trace.Span, 0, 1)
					}
					//var t [8]byte = span.SpanContext().SpanID()
					//spanID := append([]byte{}, t[:]...)
					// todo: sort by time
					spans = append(spans, span)
					log.Printf("span in %s %s %s %d", l7p.ReqSkey, span.SpanContext().TraceID(), time.Now().Sub(l7p.Time), len(spans))
					// todo: remove debug code
					//if len(spans) > 1 {
					//	log.Printf("[WARN] req span len %d", len(spans))
					//	//for k, v := range c.respDirect.heads {
					//	//	log.Printf("[WARN] resp heads seq:%d l7len: %d", k, v.l7len)
					//	//}
					//	for k, v := range c.reqDirect.packets {
					//		_, ok = c.reqDirect.heads[k]
					//		log.Printf("[WARN] req packets seq:%d head?:%v payload:%d: %s ", k, ok, v.PayloadLen, v.Payload())
					//	}
					//	for k, v := range c.respDirect.packets {
					//		_, ok = c.respDirect.heads[k]
					//		log.Printf("[WARN] resp packets seq:%d head?:%v payload:%d: %s", k, ok, v.PayloadLen,
					//			v.Payload())
					//	}
					//}
					c.spanMap[info.L7ID] = spans
				}(l7p)
			}
		}
	}()

	func() {
	LOOP:
		for {
			select {
			case l7p, ok := <-respC:
				if !ok {
					break LOOP
				}
				//go func() {
				data := l7p.Raw
				//log.Println("resp find span", len(data))
				info, err := c.proto.ReadInfo(data)
				if err != nil {
					log.Printf("[ERROR] HandelPackets ReadInfo error: %s", err)
					continue
				}
				refindNum := 0
				// 这里阻塞，保证response消费span的顺序
				for {
					b := func(c *VConnection) bool {
						c.Lock()
						defer c.Unlock()
						//log.Printf("span find %s", info.L7ID)
						spans, ok := c.spanMap[info.L7ID]
						if !ok {
							if refindNum%10 == 0 {
								log.Printf("[ERROR] span not found map %s %s %d", c.reqKey, info.L7ID, refindNum)
							}
							return false
						}
						if len(spans) == 0 {
							log.Printf("[ERROR] span not found slice %s %s %d", c.reqKey, info.L7ID, refindNum)
							return false
						}
						log.Printf("span found %s %s %s %d", spans[0].SpanContext().TraceID(), time.Now().Sub(l7p.Time), l7p.ReqSkey, len(spans))
						go spans[0].End(trace.WithTimestamp(l7p.Time))
						spans = spans[1:]
						if len(spans) > 0 {
							c.spanMap[info.L7ID] = spans
							// todo: remove debug code
							//if len(spans) > 0 {
							//	log.Printf("[WARN] resp span len %d", len(spans))
							//	//for k, v := range c.reqDirect.heads {
							//	//	log.Printf("[WARN] req heads seq:%d l7len: %d", k, v.l7len)
							//	//}
							//	for k, v := range c.reqDirect.packets {
							//		_, ok = c.reqDirect.heads[k]
							//		log.Printf("[WARN] req packets seq:%d head?:%v payload:%d: %s ", k, ok, v.PayloadLen, v.Payload())
							//	}
							//	for k, v := range c.respDirect.packets {
							//		_, ok = c.respDirect.heads[k]
							//		log.Printf("[WARN] resp packets seq:%d head?:%v payload:%d: %s", k, ok, v.PayloadLen,
							//			v.Payload())
							//	}
							//}
						} else {
							delete(c.spanMap, info.L7ID)
						}
						return true
					}(c)
					if b {
						refindNum = 0
						break
					}
					refindNum += 1
					if refindNum > 30 {
						log.Printf("[ERROR] span refind more than 30 times,skip. %s %s", c.reqKey, info.L7ID)
						break
					}
					//log.Printf("[WARN] span refind %s %s", c.reqKey, info.L7ID)
					time.Sleep(time.Millisecond * 100)
				}
				//}()
			}
		}
	}()

}

// todo：清理废包
func (c *VConnection) Clean() bool {
	now := time.Now()
	if now.After(c.reqDirect.timeStamp.Add(time.Hour)) &&
		now.After(c.respDirect.timeStamp.Add(time.Hour)) {
		return true
	}
	return false
}
