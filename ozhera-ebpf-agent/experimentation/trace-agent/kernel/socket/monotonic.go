package socket

import (
	"time"
)

/*
#include <time.h>
static unsigned long long get_nsecs(void)
{
   struct timespec ts;
   clock_gettime(CLOCK_MONOTONIC, &ts);
   return (unsigned long long)ts.tv_sec * 1000000000UL + ts.tv_nsec;
}
*/
import "C"

func monotonicTime() uint64 {
	monotonic := uint64(C.get_nsecs())
	//fmt.Println(monotonic)
	return monotonic
}

func monotonicToTime(t uint64) time.Time {
	monotonic := monotonicTime()
	now := time.Now()
	d := int64(monotonic - t)
	// log.Printf("monotonicToTime %d %d %d", monotonic, t, d)
	return now.Add(time.Nanosecond * time.Duration(-d))
}
