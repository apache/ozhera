//go:build ignore

// #include "vmlinux.h" // struct task_struct
#include <linux/bpf.h>
#include <linux/if_ether.h>
#include <linux/ip.h>
#include <linux/tcp.h>
#include "bpf_endian.h"
#include "bpf_helpers.h"
#include <netinet/in.h>
// #include <linux/pkt_cls.h>
// #include <inttypes.h> // print int64
// #define AF_INET 2
// #define ETH_P_IP 0x0800
// #define TC_ACT_OK 0

const int MAX_LOOP = 512; // self defined

char __license[] SEC("license") = "Dual MIT/GPL";

struct event {
    __u64 time;
    __u64 tstamp;
    __u32 saddr;
    __u32 daddr;
    __u16 source;
    __u16 dest;
    __u16 len;
    __u16 start_offset;
    __u32 pid;
    __u32 tgid;
    __s32 egress;
    __u32 ifindex;
    __u32 seq;
    __u16 frag_off;
};

struct {
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __uint(key_size, sizeof(__u32));
    __uint(value_size, sizeof(__u64));
    __uint(max_entries, 1);
} pkt_count SEC(".maps");

struct {
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __uint(key_size, sizeof(__u32));
    __uint(value_size, sizeof(__u64));
    __uint(max_entries, 1);
} fail_count SEC(".maps");

struct {
    __uint(type, BPF_MAP_TYPE_PERF_EVENT_ARRAY);
    __uint(key_size, sizeof(int));
    __uint(value_size, sizeof(int));
} payloads_pb SEC(".maps");

struct {
    __uint(type, BPF_MAP_TYPE_HASH);
    __uint(max_entries, 65535);
    __type(key, __u64); // ip port
    __type(value, __u8);
} filter_map SEC(".maps");

// Force emitting struct event into the ELF.
const struct event *unused __attribute__((unused));

SEC("socket_transit")
int mone_filter(struct __sk_buff *skb) {
    struct event e;
    __builtin_memset(&e, 0, sizeof(e));

    struct ethhdr eth;
    if (bpf_skb_load_bytes(skb, 0, &eth, sizeof(eth)) < 0) {
        return 0;
    }
    if (eth.h_proto != bpf_htons(ETH_P_IP)) {
        // The protocol is not IPv4, so we can't parse an IPv4 source address.
        return 0;
    }

    struct iphdr iph;
    if (bpf_skb_load_bytes(skb, sizeof(eth), &iph, sizeof(struct iphdr)) < 0) {
        return 0;
    }
    // 检查传输层协议是否为TCP
    if (iph.protocol != IPPROTO_TCP) {
        return 0; // 如果不是TCP，则直接通过
    }

    int iphLen = iph.ihl * 4;

    // 获取TCP报文头部
    struct tcphdr tcph;
    if (bpf_skb_load_bytes(skb, sizeof(eth) + iphLen, &tcph, sizeof(struct tcphdr)) < 0) {
        return 0;
    }
    e.start_offset = sizeof(eth) + iphLen + tcph.doff * 4;

    if (e.start_offset >= skb->len) {
        return 0;
    }
    __u64 key = 0;
    void *find = bpf_map_lookup_elem(&filter_map, &key);
    if (find == NULL || *(__u8 *) find == 0) {
        key = bpf_ntohs(tcph.dest) | ((__u64)bpf_ntohl(iph.daddr) << 32);
        find = bpf_map_lookup_elem(&filter_map, &key);
        if (find == NULL) {
            key = bpf_ntohs(tcph.source) | ((__u64)bpf_ntohl(iph.saddr) << 32);
            find = bpf_map_lookup_elem(&filter_map, &key);
            if (find == NULL) {
                key = bpf_ntohs(tcph.source);
                find = bpf_map_lookup_elem(&filter_map, &key);
                if (find == NULL) {
                    key = bpf_ntohs(tcph.dest);
                    find = bpf_map_lookup_elem(&filter_map, &key);
                    if (find == NULL) {
                        return 0;
                    }
                }
            }
        }
    }
    // struct task_struct *task = (struct task_struct *) bpf_get_current_task();
    // if (task != NULL) {
    //     __u32 id = 0;
    //     bpf_probe_read_kernel(&id, sizeof(__u32), &(task->pid));
    //     e.pid = id;
    //     bpf_probe_read_kernel(&id, sizeof(__u32), &(task->tgid));
    //     e.tgid = id;
    // }
//    __u64 tstamp;
//    bpf_probe_read_kernel(&e.tstamp, sizeof(__u64), &(skb->tstamp));
//    bpf_printk("%d", e.tstamp);
    e.time = bpf_ktime_get_ns();
    // e.tstamp = skb->tstamp;
    e.daddr = bpf_ntohl(iph.daddr);
    e.saddr = bpf_ntohl(iph.saddr);
    e.source = bpf_ntohs(tcph.source);
    e.dest = bpf_ntohs(tcph.dest);
    e.seq = bpf_ntohl(tcph.seq);
    e.frag_off = bpf_ntohs(iph.frag_off);

    e.len = bpf_ntohs(iph.tot_len) - 20 - tcph.doff * 4;
    // bpf_printk("%d", skb->local_port);
    // if (skb->sk != NULL)
    // {
    //     bpf_printk("%d", skb->sk->dst_port);
    // }
//    bpf_printk("%d %d", skb->ifindex, skb->ingress_ifindex);

    e.egress = -1;
    e.ifindex = skb->ifindex;
    __u64 len = skb->len;
    __u64 flags = BPF_F_CURRENT_CPU | (len << 32);
    __u32 ckey = 0;
    __u64 init_val = 1;
    __u64 *count = bpf_map_lookup_elem(&pkt_count, &ckey);
    if (!count) {
        bpf_map_update_elem(&pkt_count, &ckey, &init_val, BPF_ANY);
        e.tstamp = 1;
    } else {
        __sync_fetch_and_add(count, 1);
        e.tstamp = *count;
    }
    count = bpf_map_lookup_elem(&fail_count, &ckey);
    if (count) {
        e.pid = *count;
    }
    long ret = bpf_perf_event_output(skb, &payloads_pb, flags, &e, sizeof(e));
    if (ret < 0) {
        if (!count) {
            bpf_map_update_elem(&fail_count, &ckey, &init_val, BPF_ANY);
            e.pid = 1;
        } else {
            __sync_fetch_and_add(count, 1);
        }
        bpf_printk("bpf_perf_event_output error %d times", e.pid);
    }
    return 0;
}