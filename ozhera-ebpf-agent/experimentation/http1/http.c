//go:build ignore

#include <linux/bpf.h>
#include <linux/if_ether.h>
#include <linux/ip.h>
#include <linux/tcp.h>
#include "bpf_endian.h"
#include "bpf_helpers.h"
#include <netinet/in.h>
#include <linux/pkt_cls.h>

char __license[] SEC("license") = "Dual MIT/GPL";

#define MAX_MAP_ENTRIES 65535

/* Define an LRU hash map for storing packet count by source IPv4 address */
struct
{
    __uint(type, BPF_MAP_TYPE_LRU_HASH);
    __uint(max_entries, MAX_MAP_ENTRIES);
    __type(key, __u32);      // source IPv4 address
    __type(value, __u8[18]); // packet count
} xdp_stats_map SEC(".maps");

static __always_inline __u8 check_http(char pl[5])
{
    if (!__builtin_memcmp(pl, "HTTP", 4))
    {
        return 2;
    }
    if (!__builtin_memcmp(pl, "POST", 4))
    {
        return 1;
    }
    // 这一行用__builtin_memcmp运行报错？？（未知原因）
    if (pl[0] == 'G' && pl[1] == 'E' && pl[2] == 'T')
    {
        return 1;
    }
    return 0;
}
const int MAX_LOOP = 2048;
static __always_inline void parse_data(void *data, void *data_end)
{
    // First, parse the ethernet header.
    struct ethhdr *eth = data;
    if ((void *)(eth + 1) > data_end)
    {
        return;
    }

    if (eth->h_proto != bpf_htons(ETH_P_IP))
    {
        // The protocol is not IPv4, so we can't parse an IPv4 source address.
        return;
    }

    // Then parse the IP header.
    struct iphdr *iph = (void *)(eth + 1);
    if ((void *)(iph + 1) > data_end)
    {
        return;
    }
    if (iph->protocol != IPPROTO_TCP)
    {
        return;
    }
    
    // Then parse the TCP header.
    struct tcphdr *tcph = data + sizeof(*eth) + sizeof(*iph);
    if (tcph + 1 > (struct tcphdr *)data_end)
    {
        return;
    }

    char *pl = data + sizeof(*eth) + sizeof(*iph) + tcph->doff * 4;
    
    if (data_end <= (void *)pl)
    {
        return;
    }
    if (data_end < (void *)(pl + 4))
    {
        return;
    }
    char pre[5] = {'\0'};
    __builtin_memcpy(&pre, pl, sizeof(char) * 4);
    __u8 check = check_http(pre);
    if (check == 0)
    {
        return;
    }
    char res[18] = {'\0'};
    char *x = res + 8;
    __u32 dataLen = bpf_ntohs(iph->tot_len) - 20 - tcph->doff * 4;
    int j = dataLen;
    if (j >= MAX_LOOP)
    {
        j = MAX_LOOP;
    }
    char pre_check[4] = "t-i:";
    for (int i = 0; i < j; i++)
    {
        char *t = pl + i;
        if ((void *)t + 4 > data_end)
        {
            return;
        }
        if (!__builtin_memcmp(t, pre_check, 4))
        {
            char *s_start = t + 4;
            if ((void *)s_start + 4 > data_end)
            {
                return;
            }
            __builtin_memcpy(x, s_start, sizeof(char) * 4);
            break;
        }
    }
    __u32 key = bpf_htonl((bpf_ntohs(tcph->source) << 16) + bpf_ntohs(tcph->dest));
    __u64 time = bpf_cpu_to_be64(bpf_ktime_get_ns());
    void *time_v = &time;
    __builtin_memcpy(res, time_v, sizeof(__u64));
    bpf_printk("%d %d %x", bpf_ntohs(tcph->source), bpf_ntohs(tcph->dest), key);
    bpf_map_update_elem(&xdp_stats_map, &key, res, BPF_ANY);
}

SEC("xdp")
int xdp_ingress(struct xdp_md *ctx)
{
    void *data = (void *)(long)ctx->data;
    void *data_end = (void *)(long)ctx->data_end;

    parse_data(data, data_end);
    return XDP_PASS;
}

SEC("tc")
int tc_egress(struct __sk_buff *skb)
{
    bpf_skb_pull_data(skb, skb->len); // ref: https://stackoverflow.com/questions/73397220/parsing-non-linear-packet-payload-in-tc-bpf-programs
    void *data = (void *)(long)skb->data;
    void *data_end = (void *)(long)skb->data_end;
    parse_data(data, data_end);
    return TC_ACT_OK;
}