# ebpf-http

```shell
export BPF_CFLAGS='-O2 -g -Wall -Werror -I/usr/include/x86_64-linux-gnu'
export BPF_CFLAGS='-O2 -g -Wall -Werror -I/usr/include/aarch64-linux-gnu'
export BPF_CFLAGS='-O2 -g -Wall -Werror -I/usr/include/aarch64-linux-gnu -I/usr/include/x86_64-linux-gnu'
go generate ./...
go run . ens160
go run -exec sudo . ens160
```


ebpf print的内容:
```shell
cat /sys/kernel/debug/tracing/trace_pipe
```

已经注入的ebpf program
```shell
bpftool prog
```

依赖:
```shell
sudo apt install -y clang
```

Exclusivity flag on, cannot modify:
```shell
tc qdisc del dev ens160 clsact
```

jaeger

```shell
docker run -d --name jaeger   -e COLLECTOR_OTLP_ENABLED=true   -e COLLECTOR_ZIPKIN_HOST_PORT=:9411   -p 5775:5775/udp   -p 6831:6831/udp   -p 6832:6832/udp   -p 5778:5778   -p 16686:16686   -p 14250:14250   -p 14268:14268   -p 14269:14269   -p 4317:4317   -p 4318:4318   -p 9411:9411   jaegertracing/all-in-one:1.49
```

docker
```shell
docker build . -t ebpf-agent
docker run -it --rm --net=host --pid=host --privileged ebpf-agent $IF
```