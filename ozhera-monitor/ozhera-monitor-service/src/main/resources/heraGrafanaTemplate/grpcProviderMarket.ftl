{
"dashboard":{
"annotations": {
"list": [
{
"builtIn": 1,
"datasource": {
"type": "datasource",
"uid": "grafana"
},
"enable": true,
"hide": true,
"iconColor": "rgba(0, 211, 255, 1)",
"name": "Annotations & Alerts",
"target": {
"limit": 100,
"matchAny": false,
"tags": [],
"type": "dashboard"
},
"type": "dashboard"
}
]
},
"editable": true,
"fiscalYearStartMonth": 0,
"graphTooltip": 0,
"id": null,
"links": [],
"liveNow": false,
"panels": [
{
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"description": "",
"fill": 2,
"fillGradient": 7,
"gridPos": {
"h": 9,
"w": 12,
"x": 0,
"y": 0
},
"hiddenSeries": false,
"id": 2,
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 150,
"total": false,
"values": false
},
"lines": true,
"linewidth": 2,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pluginVersion": "9.2.0-pre",
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": true,
"expr": "sum(sum(sum_over_time(${env}_${serviceName}_grpcServer_total{application=\"$application\",serviceName=\"$serviceName\",serverEnv=~\"$env\",serverZone=~\"$zone|\"}[30s])/30) by (methodName,serverIp)) by (methodName)",
"interval": "",
"legendFormat": "{{methodName}}",
"range": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": "gRPC调入-Method QPS",
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"mode": "time",
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:65",
"format": "short",
"logBase": 1,
"show": true
},
{
"$$hashKey": "object:66",
"format": "short",
"logBase": 1,
"show": true
}
],
"yaxis": {
"align": false
}
},
{
"aliasColors": {},
"bars": false,
"dashLength": 10,
"dashes": false,
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fill": 2,
"fillGradient": 7,
"gridPos": {
"h": 9,
"w": 12,
"x": 12,
"y": 0
},
"hiddenSeries": false,
"id": 4,
"legend": {
"alignAsTable": true,
"avg": false,
"current": false,
"max": false,
"min": false,
"rightSide": true,
"show": true,
"sideWidth": 150,
"total": false,
"values": false
},
"lines": true,
"linewidth": 2,
"nullPointMode": "null as zero",
"options": {
"alertThreshold": true
},
"percentage": false,
"pluginVersion": "9.2.0-pre",
"pointradius": 2,
"points": false,
"renderer": "flot",
"seriesOverrides": [],
"spaceLength": 10,
"stack": false,
"steppedLine": false,
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": true,
"expr": "histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_grpcServerTimeCost_bucket{application=\"$application\",serviceName=\"$serviceName\",serverEnv=~\"$env\",serverZone=~\"$zone|\"}[30s])) by (le,methodName))",
"interval": "",
"legendFormat": "{{methodName}}",
"range": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": " gRPC调入-Method P99",
"tooltip": {
"shared": true,
"sort": 0,
"value_type": "individual"
},
"type": "graph",
"xaxis": {
"mode": "time",
"show": true,
"values": []
},
"yaxes": [
{
"$$hashKey": "object:184",
"format": "ms",
"label": "",
"logBase": 1,
"min": "0",
"show": true
},
{
"$$hashKey": "object:185",
"format": "short",
"logBase": 1,
"show": true
}
],
"yaxis": {
"align": false
}
}
],
"schemaVersion": 37,
"style": "dark",
"tags": [],
"templating": {
"list": [
{
"current": {
"selected": false,
"text": "120572_nemesis_algorithm",
"value": "120572_nemesis_algorithm"
},
"hide": 0,
"includeAll": false,
"label": "服务名",
"multi": false,
"name": "application",
"options": [],
"query": "",
"skipUrlSync": false,
"type": "custom"
},
{
"current": {
"isNone": true,
"selected": false,
"text": "None",
"value": ""
},
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"definition": "label_values(${env}_${serviceName}_grpcServer_total{application=\"$application\"},serviceName)",
"hide": 0,
"includeAll": false,
"label": "gRPC服务名",
"multi": false,
"name": "serviceName",
"options": [],
"query": {
"query": "label_values(${env}_${serviceName}_grpcServer_total{application=\"$application\"},serviceName)",
"refId": "StandardVariableQuery"
},
"refresh": 1,
"regex": "",
"skipUrlSync": false,
"sort": 0,
"type": "query"
},
{
"allValue": ".*",
"current": {
"selected": false,
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
"hide": 0,
"includeAll": true,
"label": "环境",
"multi": true,
"name": "env",
"options": [],
"query": {
"query": "label_values(container_last_seen{application=\"$application\"},serverEnv)",
"refId": "StandardVariableQuery"
},
"refresh": 1,
"regex": "",
"skipUrlSync": false,
"sort": 0,
"type": "query"
},
{
"allValue": ".*",
"current": {
"selected": false,
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
"definition": "label_values(container_last_seen{application=\"$application\"},serverZone)",
"hide": 0,
"includeAll": true,
"label": "机房",
"multi": true,
"name": "zone",
"options": [],
"query": {
"query": "label_values(container_last_seen{application=\"$application\"},serverZone)",
"refId": "StandardVariableQuery"
},
"refresh": 1,
"regex": "",
"skipUrlSync": false,
"sort": 0,
"type": "query"
}
]
},
"time": {
"from": "now-30m",
"to": "now"
},
"timepicker": {},
"timezone": "",
"title": "hera-grpcprovider大盘",
"uid": "hera-grpcproviderMarket",
"version": 8,
"weekStart": ""
},
"overwrite":true,
"folderUid":"Hera",
"message":"Hera-gRPC Provider服务大盘V1.0"
}