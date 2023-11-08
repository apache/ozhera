package ot

import "strings"

type xTrans uint8

const (
	noTrans xTrans = iota
	httpReq
	httpResp
	dubbo
)

func checkProto(data []byte) xTrans {
	str := string(data)
	if strings.HasPrefix(str, "GET") || strings.HasPrefix(str, "POST") {
		return httpReq
	}
	if strings.HasPrefix(str, "HTTP") {
		return httpResp
	}
	if len(data) > 2 && data[0] == 0xda && data[1] == 0xbb {
		return dubbo
	}
	return noTrans
}
