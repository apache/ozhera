# eBPF-agent

eBPF-agent is a part of the ozhera observability system. The goal is to more conveniently access the observability capabilities of non-java projects and expand the observability capabilities of java projects.

## Technology selection
We currently use [cilium/ebpf](https://github.com/cilium/ebpf) as the basis and [florianl/go-tc](https://github.com/florianl/go-tc) as the tc function Supplementary development of eBPF-agent.

## Dependencies
Reference[cilium/ebpf](https://github.com/cilium/ebpf/discussions?discussions_q=tc#requirements)

## Status
At present, ozhera's eBPF support is in the exploration and continuous development stage and is not available for the time being. It only provides some eBPF demos for verification in the `experimentation` directory. You can refer to the `Makefile` in the demo for testing and experience.