{
"dashboard":{
"annotations":{
"list":[
{
"builtIn":1,
"datasource":"-- Grafana --",
"enable":true,
"hide":true,
"iconColor":"rgba(0, 211, 255, 1)",
"name":"Annotations &amp; Alerts",
"type":"dashboard"
}
]
},
"description":"mione",
"editable":true,
"gnetId":null,
"graphTooltip":0,
"id":null,
"iteration":1625575780650,
"panels":[
{
"collapsed":false,
"datasource":null,
"gridPos":{
"h":1,
"w":24,
"x":0,
"y":165
},
"id":159,
"panels":[

],
"title":"自定义指标",
"type":"row"
},
{
"collapsed":false,
"datasource":null,
"gridPos":{
"h":1,
"w":24,
"x":0,
"y":0
},
"id":102,
"panels":[

],
"title":"应用健康度",
"type":"row"
},
{
"aliasColors": {},
"dashLength": 10,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 1
},
"id": 110,
"interval": "30s",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": false,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"pluginVersion": "7.5.3",
"pointradius": 2,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"targets": [
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_dbError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application)  or clamp_max( absent(notExists{application=\"$application\"}),0) )/ (sum(sum_over_time(${env}_${serviceName}_sqlTotalCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application)or clamp_max( absent(notExists{application=\"$application\"}),1))),0)",
"hide": false,
"interval": "",
"legendFormat": "sql",
"refId": "A"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_redisError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0)  )/ (sum(sum_over_time(${env}_${serviceName}_RedisTotalCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "redis",
"refId": "B"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_httpError_total{application=\"$application\",serverIp=~\"$instance\"}[30s]))  by (application) or clamp_max( absent(notExists{application=\"$application\"}),0) ) / (sum(sum_over_time(${env}_${serviceName}_aopTotalMethodCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "HTTP",
"refId": "C"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_grpcClientError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0)) / (sum(sum_over_time(${env}_${serviceName}_grpcClient_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "gRPC调出",
"refId": "D"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_grpcServerError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0) ) / (sum(sum_over_time(${env}_${serviceName}_grpcServer_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "gRPC调入",
"refId": "E"
}
],
"thresholds": [],
"timeRegions": [],
"title": "可用性",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:72",
"decimals": 3,
"format": "percentunit",
"label": null,
"logBase": 1,
"max": "1",
"min": "0",
"show": true
},
{
"$$hashKey": "object:73",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"bars": false,
"dashes": false,
"fillGradient": 0,
"hiddenSeries": false,
"percentage": false,
"points": false,
"stack": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null
},

{
"id": 148,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 1
},
"type": "table",
"title": "实例列表 【主机数：$total】可以点击跳转到容器与物理机监控",
"transformations": [
{
"id": "filterFieldsByName",
"options": {
"include": {
"names": [
"ip",
"podIp",
"Value"
]
}
}
},
{
"id": "calculateField",
"options": {
"mode": "reduceRow",
"reduce": {
"reducer": "lastNotNull"
}
}
},
{
"id": "organize",
"options": {
"excludeByName": {
"jumpIp": false
},
"indexByName": {
"Last (not null)": 2,
"Value": 1,
"jumpIp": 3,
"serverIp": 0
},
"renameByName": {
"jumpIp": "宿主机"
}
}
}
],
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"pluginVersion": "9.2.0",
"description": "点击实例下方的IP可以跳转到服务所在的物理机监控\n\n点击容器启动时间下方的时间可以跳转到服务所在的容器监控",
"fieldConfig": {
"defaults": {
"custom": {
"align": "center",
"displayMode": "color-background",
"inspect": false,
"filterable": false
},
"mappings": [],
"thresholds": {
"mode": "absolute",
"steps": [
{
"color": "green",
"value": null
},
{
"color": "red",
"value": 80
},
{
"color": "#EAB839",
"value": 90
}
]
},
"color": {
"mode": "continuous-GrYlRd"
}
},
"overrides": [
{
"matcher": {
"id": "byName",
"options": "ip"
},
"properties": [
{
"id": "custom.width"
},
{
"id": "custom.displayMode",
"value": "color-text"
},
{
"id": "unit"
},
{
"id": "displayName",
"value": "实例（点击跳转到物理机监控）"
},
{
"id": "links",
"value": [
{
"targetBlank": true,
"title": "跳转到物理机监控",
"url": "${hostUrl}"
}
]
}
]
},
{
"matcher": {
"id": "byName",
"options": "Last *"
},
"properties": [
{
"id": "displayName",
"value": "状态"
},
{
"id": "custom.displayMode",
"value": "color-background"
},
{
"id": "color",
"value": {
"fixedColor": "green",
"mode": "thresholds"
}
},
{
"id": "noValue",
"value": "宕机"
},
{
"id": "mappings",
"value": [
{
"options": {
"0": {
"text": "宕机"
}
},
"type": "value"
},
{
"options": {
"from": 1,
"result": {
"text": "存活"
},
"to": 1000000000000000000
},
"type": "range"
}
]
},
{
"id": "custom.displayMode",
"value": "color-background"
},
{
"id": "thresholds",
"value": {
"mode": "absolute",
"steps": [
{
"color": "red",
"value": null
},
{
"color": "green",
"value": 1
}
]
}
}
]
},
{
"matcher": {
"id": "byName",
"options": "podIp"
},
"properties": [
{
"id": "displayName",
"value": "容器启动时间（点击跳转到容器详情)"
},
{
"id": "links",
"value": [
{
"targetBlank": true,
"title": "",
"url": "${containerUrl}"
}
]
},
{
"id": "unit",
"value": "s"
},
{
"id": "custom.displayMode",
"value": "color-text"
},
{
"id": "color",
"value": {
"fixedColor": "blue",
"mode": "fixed"
}
}
]
},
{
"matcher": {
"id": "byName",
"options": "Value"
},
"properties": [
{
"id": "displayName",
"value": "最近探活时间"
},
{
"id": "custom.hidden",
"value": true
}
]
}
]
},
"options": {
"showHeader": true,
"footer": {
"show": false,
"reducer": [
"sum"
],
"fields": ""
},
"sortBy": [
{
"desc": false,
"displayName": "点击ip跳转到容器监控"
}
]
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": true,
"expr": "sum(process_uptime_seconds{application=\"$application\",serverIp=~\"$instance\"}) by (serverIp,jumpIp)",
"format": "table",
"hide": true,
"instant": true,
"interval": "",
"legendFormat": "{{serverIp}}",
"refId": "A"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": true,
"expr": "sum(container_last_seen{application=\"$application\",podIp=~\"$instance\"}) by (podIp,ip)",
"format": "table",
"hide": false,
"instant": true,
"interval": "",
"legendFormat": "",
"refId": "B"
}
]
},

{
"aliasColors": {},
"dashLength": 10,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"gridPos": {
"h": 8,
"w": 24,
"x": 0,
"y": 9
},
"id": 112,
"interval": "30s",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 400,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"pluginVersion": "7.5.3",
"pointradius": 2,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"targets": [
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_aopTotalMethodCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"interval": "",
"legendFormat": "{{serverIp}}-HTTP",
"refId": "A"
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcClient_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-gRPC调出",
"refId": "B"
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcServer_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-gRPC调入",
"refId": "C"
}
],
"thresholds": [],
"timeRegions": [],
"title": "调用量变化(30s内总和)",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:175",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:176",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"bars": false,
"dashes": false,
"fillGradient": 0,
"hiddenSeries": false,
"percentage": false,
"points": false,
"stack": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null
},
{
"collapsed":false,
"datasource":null,
"gridPos":{
"h":1,
"w":24,
"x":0,
"y":17
},
"id":104,
"panels":[

],
"repeat":"application",
"scopedVars":{
"application":{
"selected":true,
"text":"${serviceName}",
"value":"${serviceName}"
}
},
"title":"业务指标",
"type":"row"
},
{
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"fillGradient": 0,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 18
},
"hiddenSeries": false,
"id": 116,
"interval": "15s",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pluginVersion": "7.5.3",
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_aopTotalMethodCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)",
"instant": false,
"interval": "",
"intervalFactor": 1,
"legendFormat": "total",
"refId": "A"
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_aopTotalMethodCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (methodName,serverIp)",
"format": "time_series",
"hide": false,
"instant": false,
"interval": "",
"legendFormat": "{{methodName}}-{{serverIp}}",
"refId": "B"
}
],
"thresholds": [],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"title": "Http调入 QPS",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"transformations": [],
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:180",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:181",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"scopedVars": {
"application": {
"selected": true,
"text": "${serviceName}",
"value": "${serviceName}"
}
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":12,
"y":26
},
"hiddenSeries":false,
"id":120,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"sideWidth":250,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"scopedVars":{
"application":{
"selected":true,
"text":"${serviceName}",
"value":"${serviceName}"
}
},
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"topk(10, sum(sum_over_time(${env}_${serviceName}_aopMethodTimeCount_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,methodName) / sum(sum_over_time(${env}_${serviceName}_aopMethodTimeCount_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,methodName))",
"interval":"",
"legendFormat":"{{serverIp}}-{{methodName}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Http调入 Top10 RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:376",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:377",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":12,
"y":18
},
"hiddenSeries":false,
"id":124,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"scopedVars":{
"application":{
"selected":true,
"text":"${serviceName}",
"value":"${serviceName}"
}
},
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_aopMethodTimeCount_without_methodName_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,serverIp))",
"interval":"",
"legendFormat":"{{serverIp}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Http调入 P99-RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:664",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:665",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":0,
"y":26
},
"hiddenSeries":false,
"id":128,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"scopedVars":{
"application":{
"selected":true,
"text":"${serviceName}",
"value":"${serviceName}"
}
},
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"sum(sum_over_time(${env}_${serviceName}_aopMethodTimeCount_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (serverIp,methodName) / sum(sum_over_time(${env}_${serviceName}_aopMethodTimeCount_count{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (serverIp,methodName) ",
"interval":"",
"legendFormat":"{{serverIp}}-{{methodName}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Http调入 AVG-RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:1320",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:1321",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"id": 150,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 50
},
"type": "graph",
"title": "gRPC调出 QPS",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcClient_total{application=\"$application\",serverIp=~\"$instance\"}[30s])/30)",
"interval": "",
"legendFormat": "total",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcClient_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serviceName,serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "B",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:1801",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:1802",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 126,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 50
},
"type": "graph",
"title": "gRPC调出 P99-RT",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "histogram_quantile(0.99,sum(sum_over_time(staging_hera_grpcClientTimeCost_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,serviceName,serverIp))",
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:762",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:763",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 130,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 58
},
"type": "graph",
"title": "gRPC调出 AVG-RT",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcClientTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by(serverIp,serviceName)/sum(sum_over_time(${env}_${serviceName}_grpcClientTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serverIp,serviceName)",
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:1418",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:1419",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 122,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 58
},
"type": "graph",
"title": "gRPC调出 Top10 RT",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "topk(10, sum(sum_over_time(${env}_${serviceName}_grpcClientTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName) / sum(sum_over_time(${env}_${serviceName}_grpcClientTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName))",
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:474",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:475",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},

{
"id": 118,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 66
},
"type": "graph",
"title": "gRPC调入 QPS",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcServer_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)",
"interval": "",
"legendFormat": "total",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcServer_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serviceName,serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "B",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:278",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:279",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 163,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 66
},
"type": "graph",
"title": "gRPC调入 P99-RT",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"description": "",
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_grpcServerTimeCost_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,serviceName,serverIp))",
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:121",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": "0",
"show": true
},
{
"$$hashKey": "object:122",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 169,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 74
},
"type": "graph",
"title": "gRPC调入 AVG-RT",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcServerTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by(serverIp,serviceName)\n/\nsum(sum_over_time(${env}_${serviceName}_grpcServerTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serverIp,serviceName)",
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:1418",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:1419",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 168,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 74
},
"type": "graph",
"title": "gRPC调入 Top10 RT",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"thresholds": [],
"pluginVersion": "9.2.0",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "topk(10, sum(sum_over_time(${env}_${serviceName}_grpcServerTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName) / sum(sum_over_time(${env}_${serviceName}_grpcServerTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName))",
"interval": "",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"refId": "A",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:474",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:475",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"collapsed":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"gridPos":{
"h":1,
"w":24,
"x":0,
"y":82
},
"id":106,
"panels":[

],
"title":"中间件",
"type":"row"
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":12,
"y":83
},
"hiddenSeries":false,
"id":132,
"legend":{
"alignAsTable":true,
"avg":false,
"current":true,
"max":true,
"min":true,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"sum(sum_over_time(${env}_${serviceName}_sqlTotalTimer_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (dataSource,sqlMethod) / sum(sum_over_time(${env}_${serviceName}_sqlTotalTimer_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (dataSource,sqlMethod)",
"interval":"",
"legendFormat":"{{dataSource}}-{{sqlMethod}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"DB AVG-RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:1516",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":"0",
"show":true
},
{
"$$hashKey":"object:1517",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":12,
"y":99
},
"hiddenSeries":false,
"id":134,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"sum(sum_over_time(${env}_${serviceName}_RedisMethodTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (host,port,method) / sum(sum_over_time(${env}_${serviceName}_RedisMethodTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (host,port,method)",
"interval":"",
"legendFormat":"{{host}}:{{port}}-{{method}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Redis AVG-RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:1614",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:1615",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":0,
"y":99
},
"hiddenSeries":false,
"id":136,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_RedisMethodTimeCost_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,method,host,port))",
"interval":"",
"legendFormat":"{{host}}:{{port}}-{{method}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Redis P99-RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:1810",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:1811",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":0,
"y":83
},
"hiddenSeries":false,
"id":138,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_sqlTotalTimer_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,dataSource,sqlMethod))",
"interval":"",
"legendFormat":"{{dataSource}}-{{sqlMethod}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"DB P99-RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:1712",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":"0",
"show":true
},
{
"$$hashKey":"object:1713",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":0,
"y":107
},
"hiddenSeries":false,
"id":140,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"topk(10, sum(sum_over_time(${env}_${serviceName}_RedisMethodTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(le,host,port,method) / sum(sum_over_time(${env}_${serviceName}_RedisMethodTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(le,host,port,method))",
"interval":"",
"legendFormat":"{{host}}:{{port}}-{{method}} {{key}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Redis Top10 RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:2006",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:2007",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"bars":false,
"dashLength":10,
"dashes":false,
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fill":1,
"fillGradient":0,
"gridPos":{
"h":8,
"w":12,
"x":0,
"y":91
},
"hiddenSeries":false,
"id":142,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":false,
"rightSide":true,
"show":true,
"sideWidth":300,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":2,
"points":false,
"renderer":"flot",
"seriesOverrides":[

],
"spaceLength":10,
"stack":false,
"steppedLine":false,
"targets":[
{
"exemplar":true,
"expr":"topk(10, sum(sum_over_time(${env}_${serviceName}_sqlTotalTimer_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(dataSource,sql) / sum(sum_over_time(${env}_${serviceName}_sqlTotalTimer_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(dataSource,sql))",
"interval":"",
"legendFormat":"{{dataSource}}-{{sql}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":" DB Top10 RT",
"tooltip":{
"shared":true,
"sort":2,
"value_type":"individual"
},
"type":"graph",
"xaxis":{
"buckets":null,
"mode":"time",
"name":null,
"show":true,
"values":[

]
},
"yaxes":[
{
"$$hashKey":"object:1908",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":"0",
"show":true
},
{
"$$hashKey":"object:1909",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
}
],
"yaxis":{
"align":false,
"alignLevel":null
}
},
{
"aliasColors":{

},
"breakPoint":"50%",
"cacheTimeout":null,
"combine":{
"label":"Others",
"threshold":0
},
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fontSize":"80%",
"format":"short",
"gridPos":{
"h":8,
"w":12,
"x":12,
"y":91
},
"id":144,
"interval":null,
"legend":{
"header":"",
"percentage":false,
"percentageDecimals":null,
"show":true,
"sideWidth":300,
"sort":"current",
"sortDesc":false,
"values":true,
"sideWidth": 250
},
"legendType":"Under graph",
"links":[

],
"nullPointMode":"null as zero",
"pieType":"pie",
"pluginVersion":"7.5.3",
"strokeWidth":1,
"targets":[
{
"exemplar":true,
"expr":"topk(10,sum(sum_over_time(${env}_${serviceName}_sqlTotalCount_total{serverIp=~\"$instance\",application=\"$application\"}[$interval])) by (dataSource,sql))",
"format":"time_series",
"instant":true,
"interval":"",
"legendFormat":"{{dataSource}}-{{sql}}",
"refId":"A"
}
],
"timeFrom":null,
"timeShift":null,
"title":"$interval DB Top10 Query",
"type":"grafana-piechart-panel",
"valueName":"current"
},
{
"aliasColors":{

},
"breakPoint":"50%",
"cacheTimeout":null,
"combine":{
"label":"Others",
"threshold":0
},
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"fontSize":"80%",
"format":"short",
"gridPos":{
"h":8,
"w":12,
"x":12,
"y":107
},
"id":146,
"interval":null,
"legend":{
"show":true,
"sideWidth":300,
"values":true
},
"legendType":"Under graph",
"links":[

],
"nullPointMode":"null as zero",
"pieType":"pie",
"pluginVersion":"7.5.3",
"strokeWidth":1,
"targets":[
{
"exemplar":true,
"expr":"topk(10,sum(sum_over_time(${env}_${serviceName}_RedisTotalCount_total{serverIp=~\"$instance\",application=\"$application\"}[$interval])) by (host,port,method,key))",
"format":"time_series",
"instant":true,
"interval":"",
"legendFormat":"{{host}}:{{port}}-{{method}} {{key}}",
"refId":"A"
}
],
"timeFrom":null,
"timeShift":null,
"title":"$interval Redis Top10 Query",
"type":"grafana-piechart-panel",
"valueName":"current"
},
{
"collapsed":false,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"gridPos":{
"h":1,
"w":24,
"x":0,
"y":115
},
"id":54,
"panels":[

],
"title":"Runtime Golang",
"type":"row"
},

{
"id": 95,
"gridPos": {
"h": 7,
"w": 12,
"x": 0,
"y": 122
},
"type": "graph",
"title": "Goroutines",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
},
"thresholds": [],
"pluginVersion": "9.2.0-pre",
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": false,
"show": true,
"total": false,
"values": false
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(process_runtime_go_goroutines{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}) by (serverIp)",
"interval": "",
"legendFormat": "{{serverIp}}-goroutines",
"refId": "A",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:493",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
},
{
"$$hashKey": "object:494",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 96,
"gridPos": {
"h": 7,
"w": 12,
"x": 12,
"y": 122
},
"type": "graph",
"title": "Memory in Heap",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
},
"thresholds": [],
"pluginVersion": "9.2.0-pre",
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": false,
"show": true,
"total": false,
"values": false
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fieldConfig": {
"defaults": {
"unit": "bytes"
},
"overrides": []
},
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(process_runtime_go_mem_heap_alloc_bytes{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}) by (serverIp)",
"interval": "",
"legendFormat": "{{serverIp}}-alloc",
"refId": "A",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
},
{
"exemplar": true,
"expr": "sum(process_runtime_go_mem_heap_sys_bytes{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-sys",
"refId": "B",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
},
{
"exemplar": true,
"expr": "sum(process_runtime_go_mem_heap_idle_bytes{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-idle",
"refId": "C",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
},
{
"exemplar": true,
"expr": "sum(process_runtime_go_mem_heap_inuse_bytes{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-inuse",
"refId": "D",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:305",
"format": "bytes",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
},
{
"$$hashKey": "object:306",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 50,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 129
},
"type": "graph",
"title": "Rates of Mem Allocation",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
},
"thresholds": [],
"pluginVersion": "9.2.0-pre",
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": false,
"show": true,
"total": false,
"values": false
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fieldConfig": {
"defaults": {
"unit": "binBps"
},
"overrides": []
},
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(rate(process_runtime_go_mem_heap_alloc_bytes{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}[2m])) by (serverIp)",
"interval": "",
"legendFormat": "{{serverIp}}-alloc",
"refId": "A",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:213",
"format": "binBps",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
},
{
"$$hashKey": "object:214",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 82,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 129
},
"type": "graph",
"title": "GC Pause",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
},
"thresholds": [],
"pluginVersion": "9.2.0-pre",
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": false,
"show": true,
"total": false,
"values": false
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fieldConfig": {
"defaults": {
"unit": "ns"
},
"overrides": []
},
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "\nhistogram_quantile(0.99,\nsum(rate(process_runtime_go_gc_pause_ns_bucket{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}[2m])) by (le, serverIp))",
"interval": "",
"legendFormat": "{{serverIp}}-P99",
"refId": "A",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
},
{
"exemplar": true,
"expr": "\nhistogram_quantile(0.90,\nsum(rate(process_runtime_go_gc_pause_ns_bucket{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}[2m])) by (le, serverIp))",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-P90",
"refId": "B",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
},
{
"exemplar": true,
"expr": "\nhistogram_quantile(0.50,\nsum(rate(process_runtime_go_gc_pause_ns_bucket{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}[2m])) by (le, serverIp))",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-P50",
"refId": "C",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:266",
"format": "ns",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
},
{
"$$hashKey": "object:267",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 68,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 137
},
"type": "graph",
"title": "Number of Live Objects",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
},
"thresholds": [],
"pluginVersion": "9.2.0-pre",
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": false,
"show": true,
"total": false,
"values": false
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(process_runtime_go_mem_live_objects{serverEnv=~\"$serverEnv\", application=\"$application\", serverIp=~\"$instance\"}) by (serverIp)",
"interval": "",
"legendFormat": "{{serverIp}}-live objects",
"refId": "A",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:136",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
},
{
"$$hashKey": "object:137",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},
{
"id": 78,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 137
},
"type": "graph",
"title": "Rate of Objects Allocated",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
},
"thresholds": [],
"pluginVersion": "9.2.0-pre",
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": false,
"show": true,
"total": false,
"values": false
},
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"fieldConfig": {
"defaults": {
"unit": "short"
},
"overrides": []
},
"fill": 1,
"fillGradient": 0,
"hiddenSeries": false,
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"exemplar": true,
"expr": "sum(rate(process_runtime_go_mem_heap_objects{serverEnv=~\"$serverEnv\",  application=\"$application\", serverIp=~\"$instance\"}[2m])) by (serverIp)",
"interval": "",
"legendFormat": "{{serverIp}}-heap objects",
"refId": "A",
"datasource": {
"uid": "${prometheusUid}",
"type": "prometheus"
}
}
],
"timeFrom": null,
"timeRegions": [],
"timeShift": null,
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:189",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
},
{
"$$hashKey": "object:190",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
}
},

{
"aliasColors": {},
"dashLength": 10,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 34
},
"id": 171,
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"pluginVersion": "7.5.3",
"pointradius": 2,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"targets": [
{
"expr": "histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_aopClientMethodTimeCount_without_methodName_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,serverIp))",
"legendFormat": "{{serverIp}}",
"interval": "",
"exemplar": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": "Http调出 P99-RT",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:664",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:665",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"bars": false,
"dashes": false,
"fillGradient": 0,
"hiddenSeries": false,
"percentage": false,
"points": false,
"stack": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null
},

{
"aliasColors": {},
"dashLength": 10,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 34
},
"id": 172,
"interval": "15s",
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"pluginVersion": "7.5.3",
"pointradius": 2,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"targets": [
{
"expr": "sum(sum_over_time(${env}_${serviceName}_aopClientTotalMethodCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)",
"legendFormat": "total",
"interval": "",
"exemplar": true,
"instant": false,
"intervalFactor": 1,
"refId": "A"
},
{
"expr": "sum(sum_over_time(${env}_${serviceName}_aopClientTotalMethodCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (methodName,serverIp)",
"legendFormat": "{{methodName}}-{{serverIp}}",
"interval": "",
"exemplar": true,
"format": "time_series",
"hide": false,
"instant": false,
"refId": "B"
}
],
"thresholds": [],
"timeRegions": [],
"title": "Http调出 QPS",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"transformations": [],
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:180",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:181",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"bars": false,
"dashes": false,
"fillGradient": 0,
"hiddenSeries": false,
"percentage": false,
"points": false,
"stack": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null
},

{
"aliasColors": {},
"dashLength": 10,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 42
},
"id": 173,
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"pluginVersion": "7.5.3",
"pointradius": 2,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"targets": [
{
"expr": "sum(sum_over_time(${env}_${serviceName}_aopClientMethodTimeCount_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (serverIp,methodName) / sum(sum_over_time(${env}_${serviceName}_aopClientMethodTimeCount_count{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (serverIp,methodName) ",
"legendFormat": "{{serverIp}}-{{methodName}}",
"interval": "",
"exemplar": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": "Http调出 AVG-RT",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:1320",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:1321",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"bars": false,
"dashes": false,
"fillGradient": 0,
"hiddenSeries": false,
"percentage": false,
"points": false,
"stack": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null
},

{
"aliasColors": {},
"dashLength": 10,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"fill": 1,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 42
},
"id": 174,
"legend": {
"alignAsTable": true,
"avg": true,
"current": true,
"max": true,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 250,
"total": false,
"values": true
},
"lines": true,
"linewidth": 1,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"pluginVersion": "7.5.3",
"pointradius": 2,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"targets": [
{
"expr": "topk(10, sum(sum_over_time(${env}_${serviceName}_aopClientMethodTimeCount_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,methodName) / sum(sum_over_time(${env}_${serviceName}_aopClientMethodTimeCount_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,methodName))",
"legendFormat": "{{serverIp}}-{{methodName}}",
"interval": "",
"exemplar": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": "Http调出 Top10 RT",
"tooltip": {
"shared": true,
"sort": 2,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"buckets": null,
"mode": "time",
"name": null,
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:376",
"format": "ms",
"label": null,
"logBase": 1,
"max": null,
"min": 0,
"show": true
},
{
"$$hashKey": "object:377",
"format": "short",
"label": null,
"logBase": 1,
"max": null,
"min": null,
"show": true
}
],
"yaxis": {
"align": false,
"alignLevel": null
},
"bars": false,
"dashes": false,
"fillGradient": 0,
"hiddenSeries": false,
"percentage": false,
"points": false,
"stack": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null
}

],
"refresh":"5m",
"schemaVersion":27,
"style":"dark",
"tags":[
"templated"
],
"templating":{
"list":[
{
"description":null,
"error":null,
"hide":2,
"label":"Application",
"name":"application",
"query":"${title}",
"skipUrlSync":false,
"type":"constant"
},
{
"allValue": null,
"current": {
"selected": true,
"tags": [],
"text": [
"All"
],
"value": [
"$__all"
]
},
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"definition": "label_values(container_last_seen{application=\"$application\"},serverEnv)",
"description": null,
"error": null,
"hide": 0,
"includeAll": true,
"label": "环境",
"multi": true,
"name": "serverEnv",
"options": [],
"query": {
"query": "label_values(container_last_seen{application=\"$application\"},serverEnv)",
"refId": "StandardVariableQuery"
},
"refresh": 1,
"regex": "",
"skipUrlSync": false,
"sort": 0,
"tagValuesQuery": "",
"tags": [],
"tagsQuery": "",
"type": "query",
"useTags": false
},
{
"allValue": null,
"current": {
"selected": true,
"text": [
"All"
],
"value": [
"$__all"
]
},
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"definition": "label_values(container_last_seen{application=\"$application\",serverEnv=~\"$serverEnv\"},podIp)",
"description": null,
"error": null,
"hide": 0,
"includeAll": true,
"label": "Instance",
"multi": true,
"name": "instance",
"options": [],
"query": {
"query": "label_values(container_last_seen{application=\"$application\",serverEnv=~\"$serverEnv\"},podIp)",
"refId": "StandardVariableQuery"
},
"refresh": 2,
"regex": "",
"skipUrlSync": false,
"sort": 1,
"tagValuesQuery": "",
"tags": [],
"tagsQuery": "",
"type": "query",
"useTags": false
},
{
"allValue": null,
"current": {
"selected": false,
"text": "2",
"value": "2"
},
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"definition": "query_result(count(container_last_seen{application=\"$application\",serverEnv=~\"$serverEnv\"}))",
"description": null,
"error": null,
"hide": 2,
"includeAll": false,
"label": "主机数",
"multi": false,
"name": "total",
"options": [],
"query": {
"query": "query_result(count(container_last_seen{application=\"$application\",serverEnv=~\"$serverEnv\"}))",
"refId": "StandardVariableQuery"
},
"refresh": 2,
"regex": "/{} (.*) .*/",
"skipUrlSync": false,
"sort": 0,
"tagValuesQuery": "",
"tags": [],
"tagsQuery": "",
"type": "query",
"useTags": false
},
{
"auto":true,
"auto_count":1,
"auto_min":"10s",
"current":{
"selected":false,
"text":"auto",
"value":"$__auto_interval_interval"
},
"description":null,
"error":null,
"hide":2,
"label":"时间间隔",
"name":"interval",
"options":[
{
"selected":true,
"text":"auto",
"value":"$__auto_interval_interval"
},
{
"selected":false,
"text":"1m",
"value":"1m"
},
{
"selected":false,
"text":"2m",
"value":"2m"
},
{
"selected":false,
"text":"5m",
"value":"5m"
},
{
"selected":false,
"text":"10m",
"value":"10m"
},
{
"selected":false,
"text":"20m",
"value":"20m"
},
{
"selected":false,
"text":"30m",
"value":"30m"
},
{
"selected":false,
"text":"1h",
"value":"1h"
},
{
"selected":false,
"text":"2h",
"value":"2h"
},
{
"selected":false,
"text":"3h",
"value":"3h"
},
{
"selected":false,
"text":"4h",
"value":"4h"
},
{
"selected":false,
"text":"5h",
"value":"5h"
},
{
"selected":false,
"text":"6h",
"value":"6h"
},
{
"selected":false,
"text":"7h",
"value":"7h"
},
{
"selected":false,
"text":"8h",
"value":"8h"
},
{
"selected":false,
"text":"9h",
"value":"9h"
},
{
"selected":false,
"text":"10h",
"value":"10h"
},
{
"selected":false,
"text":"12h",
"value":"12h"
},
{
"selected":false,
"text":"24h",
"value":"24h"
},
{
"selected":false,
"text":"2d",
"value":"2d"
},
{
"selected":false,
"text":"3d",
"value":"3d"
},
{
"selected":false,
"text":"4d",
"value":"4d"
},
{
"selected":false,
"text":"5d",
"value":"5d"
},
{
"selected":false,
"text":"6d",
"value":"6d"
},
{
"selected":false,
"text":"7d",
"value":"7d"
}
],
"query":"1m,2m,5m,10m,20m,30m,1h,2h,3h,4h,5h,6h,7h,8h,9h,10h,12h,24h,2d,3d,4d,5d,6d,7d",
"queryValue":"",
"refresh":2,
"skipUrlSync":false,
"type":"interval"
},
{
"description": null,
"error": null,
"hide": 2,
"label": "容器名",
"name": "containerName",
"query": "${containerName}",
"skipUrlSync": false,
"type": "constant"
}
]
},
"time":{
"from":"now-5m",
"to":"now"
},
"timepicker":{
"refresh_intervals":[
"5s",
"10s",
"30s",
"1m",
"5m",
"15m",
"30m",
"1h",
"2h",
"1d"
],
"time_options":[
"5m",
"15m",
"1h",
"6h",
"12h",
"24h",
"2d",
"7d",
"30d"
]
},
"timezone":"browser",
"title":"业务监控-${title}",
"uid":"${uid}",
"version":222
},
"overwrite":false,
"folderId":${folderId},
"folderUid":"${folderUid}",
"message":"hera golang V1.1"
}