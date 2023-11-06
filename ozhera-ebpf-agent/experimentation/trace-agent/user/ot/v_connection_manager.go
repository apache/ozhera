package ot

import (
	"context"
	"trace-agent/kernel"
	"trace-agent/user/ot/protocol"
	"trace-agent/user/ot/protocol/imp/pdubbo"
	"trace-agent/user/ot/protocol/imp/phttp"
	"trace-agent/user/ot/vconnection"
	"log"
	"os"
	"sync"
	"time"
)

func init() {
	protocol.RegisterProtocol(1, &phttp.HttpProto{})
	if os.Getenv("EBPF_AGENT_DUBBO") == "1" {
		protocol.RegisterProtocol(2, &pdubbo.DubboProto{})
	}
}

type vConnectionManager struct {
	sync.RWMutex
	vConnMap map[kernel.SocketKey]*vconnection.VConnection
}

func (m *vConnectionManager) NewEvent(e *kernel.PacketEvent) {
	m.RLock()
	cm, ok := m.vConnMap[e.Key()]
	//log.Printf("VConnection NewEvent %d", e.PayloadLen)
	if e.PayloadLen == 0 || len(e.Payload()) == 0 {
		log.Printf("%d %d", e.PayloadLen, len(e.Payload()))
	}
	//log.Printf("manager new event ok %d %d %s %v", e.Seq, e.PayloadLen, e.Key(), ok)
	m.RUnlock()
	if !ok {
		m.Lock()
		cm, ok = m.vConnMap[e.Key()]
		if !ok {
			cm = vconnection.NewVConnection(e)
			if cm != nil {
				m.vConnMap[e.Key()] = cm
				m.Unlock()
				return
			}
		}
		m.Unlock()
	}
	if cm == nil {
		return
	}
	cm.NewEvent(e)
}

func (m *vConnectionManager) Clean(ctx context.Context) {
	for {
		select {
		case <-ctx.Done():
			return
		default:
			time.Sleep(time.Second * 30)
		}
		func() {
			m.RLock()
			defer m.RUnlock()

		}()
		func() {
			m.Lock()
			defer m.Unlock()
			for k, vc := range m.vConnMap {
				vc.Lock()
				if vc.Clean() {
					vc.Close()
					delete(m.vConnMap, k)
					log.Printf("v connection %s delete", k)
				}
				vc.Unlock()
			}
		}()
	}

}
