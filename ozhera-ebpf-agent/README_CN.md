# eBPF-agent

eBPF-agent是ozhera可观测体系中的一环，目标是为了更方便的对非java项目进行可观测能力的接入，以及对java项目的可观测性能力进行扩展

## 技术选型
我们目前采用[cilium/ebpf](https://github.com/cilium/ebpf)来作为基础，[florianl/go-tc](https://github.com/florianl/go-tc)作为tc功能的补充进行eBPF-agent的开发

## 依赖
参考[cilium/ebpf](https://github.com/cilium/ebpf/discussions?discussions_q=tc#requirements)

## 现状
目前ozhera的eBPF支持处于探索和持续开发阶段，暂时不具备可用性，只在`experimentation`目录下提供一些用于验证的eBPF demo，可以参考demo中的`Makefile`进行测试和体验