{
"region": "chn-tianjin",
"zone": "ksywq",
"env": "online",
"job_name": "mione-golang-runtime",
"scrape_interval": "1m",
"scrape_timeout": "10s",
"metrics_path": "/metrics",
"honor_labels": false,
"honor_timestamps": false,
"scheme": "http",
"relabel_configs": [
{
"regex": "(.*)",
"target_label": "system",
"replacement": "mione",
"action": "replace",
"separator": ";"
},
{
"source_labels": [
"__address__"
],
"regex": "(\\d+\\.\\d+\\.\\d+\\.\\d+).*",
"target_label": "ip",
"replacement": "$1",
"action": "replace"
}
],
"http_sd_configs": [
{
"url": "http://prometheus-agent.ozhera-namespace:8080/prometheus/getHeraAppPodIp"
}
]
}