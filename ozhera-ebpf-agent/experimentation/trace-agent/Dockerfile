FROM docker.io/library/golang:bookworm as generator

WORKDIR /root

RUN mv /etc/apt/sources.list.d/debian.sources /etc/apt/sources.list.d/debian.sources.bak;touch /etc/apt/sources.list ; \
        echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm main contrib non-free non-free-firmware" > /etc/apt/sources.list;   \
        echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm-updates main contrib non-free non-free-firmware" >> /etc/apt/sources.list;  \
        echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm-backports main contrib non-free non-free-firmware" >> /etc/apt/sources.list;  \
        echo "deb https://mirrors.tuna.tsinghua.edu.cn/debian-security bookworm-security main contrib non-free non-free-firmware" >> /etc/apt/sources.list; \
        apt-get update ; apt-get install -y clang-14;
ADD . ./
RUN go env -w GOPROXY=https://goproxy.cn,direct; \
    go mod download ;export BPF_CFLAGS='-O2 -g -Wall -Werror -I/usr/include/aarch64-linux-gnu'; go generate ./... ; \
    go clean -modcache;go clean -cache  # CGO_ENABLED=1 GOOS=linux GOARCH=amd64 go build -o agent .


FROM docker.io/library/golang:1.21 as builder
WORKDIR /root
COPY --from=generator /root ./

RUN set -x;ls;go env -w GOPROXY=https://goproxy.cn,direct; \
        go mod download; CGO_ENABLED=1 GOOS=linux GOARCH=amd64 go build -o agent .


FROM docker.io/library/debian:bookworm-slim

COPY --from=builder /root/agent /opt/agent

ENTRYPOINT ["/opt/agent"]