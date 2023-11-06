package protocol

var pMap map[uint64]TransProtocol

func RegisterProtocol(i uint64, p TransProtocol) {
	if pMap == nil {
		pMap = make(map[uint64]TransProtocol)
	}
	pMap[i] = p
}

func GetProtocols() map[uint64]TransProtocol {
	if pMap == nil {
		pMap = make(map[uint64]TransProtocol)
	}
	return pMap
}

func Identify(data []byte) (*uint64, TransProtocol, bool) {
	for i, v := range GetProtocols() {
		ok, req := v.CheckProtocol(data)
		if ok {
			return &i, v, req
		}
	}
	return nil, nil, false
}
