#!/bin/bash
IFS=',' read -r -a peer_ips <<< "${PEER_ADDRESS}"
str="--cluster.peer="
args=""
listen_port=8001
# shellcheck disable=SC2068
for ip in ${peer_ips[@]}; do
    args=${args}${str}${ip}" "
done
./alertmanager \
    --config.file=/etc/alertmanager/alertmanager.yml \
    --cluster.advertise-address=0.0.0.0:9093 \
    --log.level=debug \
    --storage.path=/data \
    --web.external-url= \
    --cluster.listen-address="${NODE_NAME}":${listen_port} \
    $args