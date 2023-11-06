package vconnection

import (
	"trace-agent/kernel"
	"trace-agent/user/ot/protocol"
	"log"
	"sync"
	"time"
)

type headInfo struct {
	l7len       *uint64
	readOffset  uint16
	l7id        string
	requiredSeq uint32
	sync.Mutex
}

type vDirection struct {
	packets map[uint32]*kernel.PacketEvent
	sync.RWMutex
	heads     map[uint32]*headInfo
	c         chan *kernel.PacketEvent
	timeStamp time.Time
}

func newVDirection() *vDirection {
	return &vDirection{
		packets: make(map[uint32]*kernel.PacketEvent),
		heads:   make(map[uint32]*headInfo),
		c:       make(chan *kernel.PacketEvent, 512),
	}
}

//func newVDirectionWithHeads(e *kernel.PacketEvent, info *protocol.BaseInfo) *vDirection {
//	d := newVDirection()
//	d.packets[e.Seq] = e
//	d.heads[e.Seq] = &headInfo{
//		l7len:       info.L7Len,
//		readOffset:  0,
//		l7id:        info.L7ID,
//		requiredSeq: d.nextSeq(e.Seq),
//	}
//	return d
//}

func (d *vDirection) NewEvent(e *kernel.PacketEvent, proto protocol.TransProtocol) {
	info, err := proto.ReadInfo(e.Payload())
	if err != nil {
		log.Printf("[ERROR] vDirection NewEvent ReadInfo error: %s", err)
		return
	}
	head := len(info.L7ID) > 0
	d.Lock()
	d.timeStamp = time.Now()
	//log.Printf("direction in %d", e.Seq)
	d.packets[e.Seq] = e
	var hSeq *uint32
	var hInfo *headInfo
	if head {
		hInfo = &headInfo{
			l7len:       info.L7Len,
			readOffset:  0,
			l7id:        info.L7ID,
			requiredSeq: d.nextSeq(e.Seq),
		}
		d.heads[e.Seq] = hInfo
		hSeq = &e.Seq

	} else {
		for seq, h := range d.heads {
			if h.requiredSeq == e.Seq {
				hSeq = &seq
				hInfo = h
				log.Printf("vDirection NewEvent find related head %d", *hSeq)
				break
			}
		}
	}
	d.Unlock()
	if hInfo != nil {
		hInfo.Lock()
		defer hInfo.Unlock()
	}
	for hSeq != nil {
		d.updateHeaderRequired(*hSeq)
		if !head && d.heads[*hSeq].l7len == nil {
			//log.Printf("vDirection NewEvent updateL7Len")
			d.updateL7Len(*hSeq, proto)
		}
		if d.heads[*hSeq].l7len == nil {
			break
		}
		if *d.heads[*hSeq].l7len > d.lenOf(*hSeq) {
			//log.Printf("vDirection NewEvent head need len %d read len %d, lack %d", *d.heads[*hSeq].l7len, d.lenOf(*hSeq), *d.heads[*hSeq].l7len-d.lenOf(*hSeq))
			break
		}
		//log.Printf("vDirection NewEvent ReadSpan")
		data, newHead := d.popL7(*hSeq)
		newE := *e
		newE.Raw = data
		d.c <- &newE
		hSeq = newHead
		head = false
	}
}

func (d *vDirection) updateHeaderRequired(seq uint32) {
	//he := d.packets[seq]
	info := d.heads[seq]
	required := info.requiredSeq
	for {
		te, ok := d.packets[required]
		if !ok {
			break
		}
		info.requiredSeq = te.Seq + uint32(te.PayloadLen)
		required = info.requiredSeq
	}
	//log.Printf("required updated to %d", required)
}

func (d *vDirection) updateL7Len(seq uint32, proto protocol.TransProtocol) {
	info, err := proto.ReadInfo(d.readAll(seq))
	if err != nil {
		log.Printf("[ERROR] vDirection updateL7Len ReadInfo error: %s", err)
		return
	}
	if info.L7Len == nil {
		return
	}
	d.heads[seq].l7len = info.L7Len
	if len(d.heads[seq].l7id) == 0 {
		d.heads[seq].l7id = info.L7ID
	}
}

func (d *vDirection) readAll(seq uint32) []byte {
	e := d.packets[seq]
	res := make([]byte, 0, e.PayloadLen)
	res = append(res, d.readHeadRemain(seq)...)
	required := d.nextSeq(e.Seq)
	for {
		te, ok := d.packets[required]
		if !ok {
			break
		}
		required = te.Seq + uint32(te.PayloadLen)
		res = append(res, te.Payload()...)
	}
	return res
}

func (d *vDirection) readHeadRemain(seq uint32) []byte {
	e := d.packets[seq]
	return e.Payload()[d.heads[seq].readOffset:]
}

func (d *vDirection) nextSeq(seq uint32) uint32 {
	e := d.packets[seq]
	return seq + uint32(e.PayloadLen)
}

func (d *vDirection) lenOf(seq uint32) uint64 {
	//e := d.packets[seq]
	//res := uint64(e.PayloadLen - d.heads[seq].readOffset)
	//required := d.nextSeq(e.Seq)
	//for {
	//	te, ok := d.packets[required]
	//	if !ok {
	//		break
	//	}
	//	required = te.Seq + uint32(te.PayloadLen)
	//	res += uint64(te.PayloadLen)
	//}
	//return res
	return uint64(d.heads[seq].requiredSeq - seq - uint32(d.heads[seq].readOffset))
}

func (d *vDirection) readL7(seq uint32) []byte {
	l7Len := *d.heads[seq].l7len
	//defer d.takeL7(seq, l7Len)
	res := make([]byte, 0, l7Len)
	nextSeq := seq
	e := d.packets[nextSeq]
	if e == nil {
		return nil
	}
	headRemain := d.readHeadRemain(seq)
	if uint64(len(headRemain)) >= l7Len {
		//log.Printf("l7 init append %d: %d %d", nextSeq, l7Len, len(headRemain))
		res = append(res, headRemain[:l7Len]...)
		return res
	}
	//log.Printf("l7 init append all %d: %d", nextSeq, len(headRemain))
	res = append(res, headRemain...)
	nextSeq = d.nextSeq(e.Seq)
	for uint64(len(res)) < l7Len {
		e = d.packets[nextSeq]
		//log.Printf("l7 get seq: %d", nextSeq)
		if e == nil {
			return nil
		}
		nextSeq = d.nextSeq(e.Seq)
		//log.Printf("l7 next seq %d", nextSeq)
		currentLen := uint64(len(res))
		need := l7Len - currentLen
		if uint64(e.PayloadLen) < need {
			//log.Printf("l7 append all %d: %d", e.Seq, e.PayloadLen)
			res = append(res, e.Payload()...)
			continue
		}
		//log.Printf("l7 append %d need %d %d", e.Seq, need, e.PayloadLen)
		res = append(res, e.Payload()[:need]...)
	}
	return res
}

func (d *vDirection) takeL7(seq uint32, takeLen uint64) *uint32 {
	nextSeq := seq
	remainReadLen := takeLen
	for remainReadLen > 0 {
		//log.Printf("l7 taking seq: %d", nextSeq)
		e := d.packets[nextSeq]
		if nextSeq == seq {
			if uint64(e.PayloadLen-d.heads[seq].readOffset) > remainReadLen {
				d.heads[seq].readOffset += uint16(remainReadLen)
				d.heads[seq].l7len = nil
				d.heads[seq].l7id = ""
				log.Printf("l7 taking head seq: %d update readOffset to %d\n%s\n===\n%s", nextSeq, d.heads[seq].readOffset, e.Payload()[:d.heads[seq].readOffset], e.Payload()[d.heads[seq].readOffset:])
				return &nextSeq
			}
		} else if uint64(e.PayloadLen) > remainReadLen {
			d.heads[seq].readOffset += uint16(remainReadLen)
			d.heads[seq].l7len = nil
			d.heads[seq].l7id = ""
			log.Printf("l7 taking seq: %d update readOffset to %d", nextSeq, d.heads[seq].readOffset)
			d.heads[nextSeq] = d.heads[seq]
			delete(d.heads, seq)
			return &nextSeq
		}
		remainReadLen -= uint64(e.PayloadLen)
		if remainReadLen > 0 {
			log.Printf("l7 take all seq %d: %d  still need %d", nextSeq, e.PayloadLen, remainReadLen)
		}
		if nextSeq != seq {
			log.Printf("l7 take all none head seq %d: %d  still need %d", nextSeq, e.PayloadLen, remainReadLen)
		}
		//delete(d.packets, nextSeq)
		cSeq := e.Seq
		nextSeq = d.nextSeq(e.Seq)
		//log.Printf("l7 take next seq %d delete %d from packets map", nextSeq, cSeq)
		delete(d.packets, cSeq)
		//if remainReadLen>0{

		//}
	}
	delete(d.heads, seq)
	return nil
}

func (d *vDirection) popL7(seq uint32) ([]byte, *uint32) {
	data := d.readL7(seq)
	newHead := d.takeL7(seq, uint64(len(data)))
	return data, newHead
}
