<#--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->
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
"y":197
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
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_dubboConsumerError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0)) / (sum(sum_over_time(${env}_${serviceName}_dubboBisTotalCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "Dubbo调出",
"refId": "D"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_dubboProviderError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0) ) / (sum(sum_over_time(${env}_${serviceName}_dubboMethodCalledCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "Dubbo调入",
"refId": "E"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_grpcClientError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0)) / (sum(sum_over_time(${env}_${serviceName}_grpcClient_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "gRPC调出",
"refId": "F"
},
{
"exemplar": true,
"expr": "clamp_min(1 - ((sum(sum_over_time(${env}_${serviceName}_grpcServerError_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),0) ) / (sum(sum_over_time(${env}_${serviceName}_grpcServer_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (application) or clamp_max( absent(notExists{application=\"$application\"}),1) )),0)",
"hide": false,
"interval": "",
"legendFormat": "gRPC调入",
"refId": "G"
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
"Value",
"pod"
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
"jumpIp": false,
"pod": false
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
"uid": "${prometheusUid}",
"type": "prometheus"
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
"type": "value",
"options": {
"0": {
"text": "宕机"
}
}
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
"expr": "sum(container_last_seen{application=\"$application\",podIp=~\"$instance\"}) by (podIp,ip,pod)",
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
"expr": "sum(sum_over_time(${env}_${serviceName}_dubboBisTotalCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-Dubbo调出",
"refId": "B"
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_dubboMethodCalledCount_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-Dubbo调入",
"refId": "C"
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcClient_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-gRPC调出",
"refId": "D"
},
{
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_grpcServer_total{application=\"$application\",serverIp=~\"$instance\"}[30s])) by (serverIp)",
"hide": false,
"interval": "",
"legendFormat": "{{serverIp}}-gRPC调入",
"refId": "E"
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
"x":0,
"y":66
},
"hiddenSeries":false,
"id":118,
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
"expr":"sum(sum_over_time(${env}_${serviceName}_dubboMethodCalledCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)",
"interval":"",
"legendFormat":"total",
"refId":"A"
},
{
"exemplar":true,
"expr":"sum(sum_over_time(${env}_${serviceName}_dubboMethodCalledCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serviceName,serverIp)",
"hide":false,
"interval":"",
"legendFormat":"{{serverIp}}-{{serviceName}}",
"refId":"B"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Dubbo调入 QPS",
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
"$$hashKey":"object:278",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:279",
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
"y": 74
},
"id": 168,
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
"expr": "topk(10, sum(sum_over_time(${env}_${serviceName}_dubboProviderCount_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName) / sum(sum_over_time(${env}_${serviceName}_dubboProviderCount_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName))",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"interval": "",
"exemplar": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": "Dubbo调入 Top10 RT",
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
"y": 74
},
"id": 169,
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
"expr": "sum(sum_over_time(${env}_${serviceName}_dubboProviderCount_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by(serverIp,serviceName)\n/\nsum(sum_over_time(${env}_${serviceName}_dubboProviderCount_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serverIp,serviceName)",
"legendFormat": "{{serverIp}}-{{serviceName}}",
"interval": "",
"exemplar": true,
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": "Dubbo调入 AVG-RT",
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
"y":50
},
"hiddenSeries":false,
"id":150,
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
"expr":"sum(sum_over_time(${env}_${serviceName}_dubboBisTotalCount_total{application=\"$application\"}[30s])/30)",
"interval":"",
"legendFormat":"total",
"refId":"A"
},
{
"exemplar":true,
"expr":"sum(sum_over_time(${env}_${serviceName}_dubboBisTotalCount_total{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serviceName,serverIp)",
"hide":false,
"interval":"",
"legendFormat":"{{serverIp}}-{{serviceName}}",
"refId":"B"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Dubbo调出 QPS",
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
"$$hashKey":"object:1801",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:1802",
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
"y":58
},
"hiddenSeries":false,
"id":122,
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
"expr":"topk(10, sum(sum_over_time(${env}_${serviceName}_dubboConsumerTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName) / sum(sum_over_time(${env}_${serviceName}_dubboConsumerTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30)by(serverIp,serviceName))",
"interval":"",
"legendFormat":"{{serverIp}}-{{serviceName}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Dubbo调出 Top10 RT",
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
"$$hashKey":"object:474",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:475",
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
"y":50
},
"hiddenSeries":false,
"id":126,
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
"expr":"histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_dubboConsumerTimeCost_without_methodName_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,serviceName,serverIp))",
"interval":"",
"legendFormat":"{{serverIp}}-{{serviceName}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Dubbo调出 P99-RT",
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
"$$hashKey":"object:762",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:763",
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
"y":58
},
"hiddenSeries":false,
"id":130,
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
"expr":"sum(sum_over_time(${env}_${serviceName}_dubboConsumerTimeCost_sum{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by(serverIp,serviceName)/sum(sum_over_time(${env}_${serviceName}_dubboConsumerTimeCost_count{serverIp=~\"$instance\",application=\"$application\"}[30s])/30) by (serverIp,serviceName)",
"interval":"",
"legendFormat":"{{serverIp}}-{{serviceName}}",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Dubbo调出 AVG-RT",
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
"$$hashKey":"object:1418",
"format":"ms",
"label":null,
"logBase":1,
"max":null,
"min":0,
"show":true
},
{
"$$hashKey":"object:1419",
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
"type": "graph",
"title": "Dubbo调入 P99-RT",
"gridPos": {
"x": 12,
"y": 66,
"w": 12,
"h": 8
},
"id": 163,
"options": {
"alertThreshold": true
},
"fieldConfig": {
"defaults": {},
"overrides": []
},
"pluginVersion": "7.5.3",
"renderer": "flot",
"yaxes": [
{
"label": null,
"show": true,
"logBase": 1,
"min": "0",
"max": null,
"format": "ms",
"$$hashKey": "object:121"
},
{
"label": null,
"show": true,
"logBase": 1,
"min": null,
"max": null,
"format": "short",
"$$hashKey": "object:122"
}
],
"xaxis": {
"show": true,
"mode": "time",
"name": null,
"values": [],
"buckets": null
},
"yaxis": {
"align": false,
"alignLevel": null
},
"lines": true,
"fill": 1,
"linewidth": 1,
"dashLength": 10,
"spaceLength": 10,
"pointradius": 2,
"legend": {
"show": true,
"values": true,
"min": false,
"max": true,
"current": true,
"total": false,
"avg": true,
"alignAsTable": true,
"rightSide": true,
"sideWidth": 250
},
"nullPointMode": "null as zero",
"tooltip": {
"value_type": "individual",
"shared": true,
"sort": 2
},
"aliasColors": {},
"seriesOverrides": [],
"thresholds": [],
"timeRegions": [],
"targets": [
{
"expr": "histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_dubboProviderCount_without_methodName_bucket{serverIp=~\"$instance\",application=\"$application\"}[30s])) by (le,serviceName,serverIp))",
"legendFormat":"{{serverIp}}-{{serviceName}}",
"interval": "",
"exemplar": true,
"refId": "A"
}
],
"fillGradient": 0,
"dashes": false,
"hiddenSeries": false,
"points": false,
"bars": false,
"stack": false,
"percentage": false,
"steppedLine": false,
"timeFrom": null,
"timeShift": null,
"description": "",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
}
},
{
"id": 180,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 82
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
"id": 184,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 82
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
"id": 183,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 90
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
"id": 186,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 90
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
"id": 185,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 98
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
"id": 187,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 98
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
"id": 188,
"gridPos": {
"h": 8,
"w": 12,
"x": 0,
"y": 106
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
"id": 182,
"gridPos": {
"h": 8,
"w": 12,
"x": 12,
"y": 106
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
"y":114
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
"y":115
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
"y":131
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
"y":131
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
"y":123
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
"y":139
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
"y":123
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
"y":123
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
"y":139
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
"y":147
},
"id":54,
"panels":[

],
"title":"JVM",
"type":"row"
},
{
"cacheTimeout":null,
"colorBackground":false,
"colorValue":true,
"colors":[
"rgba(245, 54, 54, 0.9)",
"#5195ce",
"rgba(50, 172, 45, 0.97)"
],
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"decimals":1,
"editable":true,
"error":false,
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"format":"s",
"gauge":{
"maxValue":100,
"minValue":0,
"show":false,
"thresholdLabels":false,
"thresholdMarkers":true
},
"gridPos":{
"h":3,
"w":6,
"x":0,
"y":164
},
"height":"",
"id":52,
"interval":null,
"links":[

],
"mappingType":1,
"mappingTypes":[
{
"name":"value to text",
"value":1
},
{
"name":"range to text",
"value":2
}
],
"maxDataPoints":100,
"nullPointMode":"null as zero",
"nullText":null,
"postfix":"",
"postfixFontSize":"50%",
"prefix":"",
"prefixFontSize":"70%",
"rangeMaps":[
{
"from":"null",
"text":"N/A",
"to":"null"
}
],
"sparkline":{
"fillColor":"rgba(31, 118, 189, 0.18)",
"full":false,
"lineColor":"rgb(31, 120, 193)",
"show":false
},
"tableColumn":"",
"targets":[
{
"exemplar":true,
"expr":"process_uptime_seconds{application=\"$application\", serverIp=~\"$instance\"}",
"format":"time_series",
"interval":"",
"intervalFactor":2,
"legendFormat":"",
"metric":"",
"refId":"A",
"step":14400
}
],
"thresholds":"",
"title":"Uptime",
"type":"singlestat",
"valueFontSize":"80%",
"valueMaps":[
{
"op":"=",
"text":"N/A",
"value":"null"
}
],
"valueName":"current"
},
{
"cacheTimeout":null,
"colorBackground":false,
"colorValue":true,
"colors":[
"rgba(50, 172, 45, 0.97)",
"rgba(237, 129, 40, 0.89)",
"rgba(245, 54, 54, 0.9)"
],
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"decimals":1,
"editable":true,
"error":false,
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"format":"percent",
"gauge":{
"maxValue":100,
"minValue":0,
"show":true,
"thresholdLabels":false,
"thresholdMarkers":true
},
"gridPos":{
"h":6,
"w":5,
"x":6,
"y":164
},
"id":58,
"interval":null,
"links":[

],
"mappingType":1,
"mappingTypes":[
{
"name":"value to text",
"value":1
},
{
"name":"range to text",
"value":2
}
],
"maxDataPoints":100,
"nullPointMode":"null as zero",
"nullText":null,
"postfix":"",
"postfixFontSize":"50%",
"prefix":"",
"prefixFontSize":"70%",
"rangeMaps":[
{
"from":"null",
"text":"N/A",
"to":"null"
}
],
"sparkline":{
"fillColor":"rgba(31, 118, 189, 0.18)",
"full":false,
"lineColor":"rgb(31, 120, 193)",
"show":false
},
"tableColumn":"",
"targets":[
{
"exemplar":true,
"expr":"sum(jvm_memory_used_bytes{application=\"$application\", serverIp=~\"$instance\", area=\"heap\"})*100/sum(jvm_memory_max_bytes{application=\"$application\",serverIp=~\"$instance\", area=\"heap\"})",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"",
"refId":"A",
"step":14400
}
],
"thresholds":"70,90",
"title":"Heap Used",
"type":"singlestat",
"valueFontSize":"70%",
"valueMaps":[
{
"op":"=",
"text":"N/A",
"value":"null"
}
],
"valueName":"current"
},
{
"cacheTimeout":null,
"colorBackground":false,
"colorValue":true,
"colors":[
"rgba(50, 172, 45, 0.97)",
"rgba(237, 129, 40, 0.89)",
"rgba(245, 54, 54, 0.9)"
],
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"decimals":1,
"editable":true,
"error":false,
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"format":"percent",
"gauge":{
"maxValue":100,
"minValue":0,
"show":true,
"thresholdLabels":false,
"thresholdMarkers":true
},
"gridPos":{
"h":6,
"w":5,
"x":11,
"y":164
},
"id":60,
"interval":null,
"links":[

],
"mappingType":2,
"mappingTypes":[
{
"name":"value to text",
"value":1
},
{
"name":"range to text",
"value":2
}
],
"maxDataPoints":100,
"nullPointMode":"connected",
"nullText":null,
"postfix":"",
"postfixFontSize":"50%",
"prefix":"",
"prefixFontSize":"70%",
"rangeMaps":[
{
"from":"null",
"text":"N/A",
"to":"null"
},
{
"from":"-99999999999999999999999999999999",
"text":"N/A",
"to":"0"
}
],
"sparkline":{
"fillColor":"rgba(31, 118, 189, 0.18)",
"full":false,
"lineColor":"rgb(31, 120, 193)",
"show":false
},
"tableColumn":"",
"targets":[
{
"exemplar":true,
"expr":"sum(jvm_memory_used_bytes{application=\"$application\", serverIp=~\"$instance\", area=\"nonheap\"})*100/sum(jvm_memory_max_bytes{application=\"$application\",serverIp=~\"$instance\", area=\"nonheap\"})",
"format":"time_series",
"interval":"",
"intervalFactor":2,
"legendFormat":"",
"refId":"A",
"step":14400
}
],
"thresholds":"70,90",
"title":"Non-Heap Used",
"type":"singlestat",
"valueFontSize":"70%",
"valueMaps":[
{
"op":"=",
"text":"N/A",
"value":"null"
},
{
"op":"=",
"text":"x",
"value":""
}
],
"valueName":"current"
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
"h":6,
"w":8,
"x":16,
"y":164
},
"hiddenSeries":false,
"id":66,
"legend":{
"alignAsTable":false,
"avg":false,
"current":false,
"max":false,
"min":false,
"rightSide":false,
"show":true,
"sideWidth":100,
"total":false,
"values":false,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"process_files_open_files{application=\"$application\", serverIp=~\"$instance\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Open Files",
"refId":"A"
},
{
"exemplar":true,
"expr":"process_files_max_files{application=\"$application\", serverIp=~\"$instance\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Max Files",
"refId":"B"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Process Open Files",
"description": "Open Files => 打开文件数       \nMax Files =>  最大文件数     \n",
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
"$$hashKey":"object:435",
"format":"locale",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:436",
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
"cacheTimeout":null,
"colorBackground":false,
"colorValue":true,
"colors":[
"rgba(245, 54, 54, 0.9)",
"#5195ce",
"rgba(50, 172, 45, 0.97)"
],
"datasource":{
"type": "prometheus",
"uid": "${prometheusUid}"
},
"decimals":null,
"editable":true,
"error":false,
"fieldConfig":{
"defaults":{

},
"overrides":[

]
},
"format":"dateTimeAsIso",
"gauge":{
"maxValue":100,
"minValue":0,
"show":false,
"thresholdLabels":false,
"thresholdMarkers":true
},
"gridPos":{
"h":3,
"w":6,
"x":0,
"y":167
},
"height":"",
"id":56,
"interval":null,
"links":[

],
"mappingType":1,
"mappingTypes":[
{
"name":"value to text",
"value":1
},
{
"name":"range to text",
"value":2
}
],
"maxDataPoints":100,
"nullPointMode":"connected",
"nullText":null,
"postfix":"",
"postfixFontSize":"50%",
"prefix":"",
"prefixFontSize":"70%",
"rangeMaps":[
{
"from":"null",
"text":"N/A",
"to":"null"
}
],
"sparkline":{
"fillColor":"rgba(31, 118, 189, 0.18)",
"full":false,
"lineColor":"rgb(31, 120, 193)",
"show":false
},
"tableColumn":"",
"targets":[
{
"exemplar":true,
"expr":"process_start_time_seconds{application=\"$application\", serverIp=~\"$instance\"}*1000",
"format":"time_series",
"interval":"",
"intervalFactor":2,
"legendFormat":"",
"metric":"",
"refId":"A",
"step":14400
}
],
"thresholds":"",
"title":"Start time",
"type":"singlestat",
"valueFontSize":"70%",
"valueMaps":[
{
"op":"=",
"text":"N/A",
"value":"null"
}
],
"valueName":"current"
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
"h":7,
"w":12,
"x":0,
"y":170
},
"hiddenSeries":false,
"id":95,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"system_cpu_usage{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-System CPU Usage",
"refId":"A"
},
{
"exemplar":true,
"expr":"process_cpu_usage{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Process CPU Usage",
"refId":"B"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"宿主机 CPU Usage",
"description": "system.cpu.usage是以纳秒为单位的主机累积 CPU 使用率，包括用户、系统、空闲 CPU 模式。（/proc/stat CPU 行的总和）\n\nprocess.cpu.usage是 JVM 进程的 CPU 使用率",
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
"$$hashKey":"object:607",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:608",
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
"h":7,
"w":12,
"x":12,
"y":170
},
"hiddenSeries":false,
"id":96,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"system_load_average_1m{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Load Average [1m]",
"refId":"A"
},
{
"exemplar":true,
"expr":"system_cpu_count{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-CPU Core Size",
"refId":"B"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"宿主机 Load Average",
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
"$$hashKey":"object:692",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:693",
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
"decimals":0,
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
"y":177
},
"hiddenSeries":false,
"id":50,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"jvm_classes_loaded_classes{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Classes Loaded",
"refId":"A"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Classes Loaded",
"description": "已加载类个数",
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
"$$hashKey":"object:777",
"decimals":0,
"format":"locale",
"label":"",
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:778",
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
"y":177
},
"hiddenSeries":false,
"id":82,
"legend":{
"avg":false,
"current":false,
"max":false,
"min":false,
"show":true,
"total":false,
"values":false,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"jvm_buffer_memory_used_bytes{serverIp=~\"$instance\", application=\"$application\", id=\"direct\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Used Bytes",
"refId":"A"
},
{
"exemplar":true,
"expr":"jvm_buffer_total_capacity_bytes{serverIp=~\"$instance\", application=\"$application\", id=\"direct\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Capacity Bytes",
"refId":"B"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Direct Buffers",
"description": "Used Bytes => 缓冲内存使用大小         \nCapacity Bytes => 缓冲容量大小",
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
"$$hashKey":"object:947",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:948",
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
"y":185
},
"hiddenSeries":false,
"id":68,
"legend":{
"alignAsTable":true,
"avg":true,
"current":true,
"max":true,
"min":true,
"show":true,
"total":false,
"values":true,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"jvm_threads_daemon_threads{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Daemon",
"refId":"A"
},
{
"exemplar":true,
"expr":"jvm_threads_live_threads{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Live",
"refId":"B"
},
{
"exemplar":true,
"expr":"jvm_threads_peak_threads{serverIp=~\"$instance\", application=\"$application\"}",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-Peak",
"refId":"C"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Threads",
"description": "daemon => 守护线程         \nlive =>  存活线程         \npeak => 线程峰值",
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
"$$hashKey":"object:1032",
"format":"short",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:1033",
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
"y":185
},
"hiddenSeries":false,
"id":78,
"legend":{
"avg":false,
"current":false,
"max":false,
"min":false,
"show":true,
"total":false,
"values":false,
"sideWidth": 250
},
"lines":true,
"linewidth":1,
"links":[

],
"nullPointMode":"null as zero",
"options":{
"alertThreshold":true
},
"percentage":false,
"pluginVersion":"7.5.3",
"pointradius":5,
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
"expr":"irate(jvm_gc_memory_allocated_bytes_total{serverIp=~\"$instance\", application=\"$application\"}[$interval])",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-allocated",
"refId":"A"
},
{
"exemplar":true,
"expr":"irate(jvm_gc_memory_promoted_bytes_total{serverIp=~\"$instance\", application=\"$application\"}[$interval])",
"format":"time_series",
"interval":"",
"intervalFactor":1,
"legendFormat":"{{ip}}-promoted",
"refId":"B"
},
{
"exemplar": true,
"expr": "sum(jvm_memory_used_bytes{serverIp=~\"$instance\", application=\"$application\",area=\"nonheap\"}) by (ip)",
"hide": false,
"interval": "",
"legendFormat": "{{ip}}--nonheap-used",
"refId": "C"
},
{
"exemplar": true,
"expr": "sum(jvm_memory_used_bytes{serverIp=~\"$instance\", application=\"$application\",area=\"heap\"}) by (ip)",
"hide": false,
"interval": "",
"legendFormat": "{{ip}}--heap-used",
"refId": "D"
}
],
"thresholds":[

],
"timeFrom":null,
"timeRegions":[

],
"timeShift":null,
"title":"Memory Allocate/Promote",
"description": "allocated  =>  gc分配的内存大小       \npromoted  =>  gc晋升到下一代的内存大小        ",
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
"$$hashKey":"object:1117",
"format":"bytes",
"label":null,
"logBase":1,
"max":null,
"min":null,
"show":true
},
{
"$$hashKey":"object:1118",
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
"id": 74,
"gridPos": {
"h": 10,
"w": 12,
"x": 0,
"y": 177
},
"type": "timeseries",
"title": "GC Count And Rate [1m]",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"pluginVersion": "9.2.0-pre",
"links": [],
"fieldConfig": {
"defaults": {
"custom": {
"drawStyle": "line",
"lineInterpolation": "linear",
"barAlignment": 0,
"lineWidth": 1,
"fillOpacity": 10,
"gradientMode": "none",
"spanNulls": false,
"showPoints": "never",
"pointSize": 5,
"stacking": {
"mode": "none",
"group": "A"
},
"axisPlacement": "auto",
"axisLabel": "",
"axisColorMode": "text",
"scaleDistribution": {
"type": "linear"
},
"axisCenteredZero": false,
"hideFrom": {
"tooltip": false,
"viz": false,
"legend": false
},
"thresholdsStyle": {
"mode": "off"
}
},
"color": {
"mode": "palette-classic"
},
"mappings": [],
"thresholds": {
"mode": "absolute",
"steps": [
{
"value": null,
"color": "green"
},
{
"value": 80,
"color": "red"
}
]
},
"unit": "locale"
},
"overrides": []
},
"options": {
"tooltip": {
"mode": "multi",
"sort": "desc"
},
"legend": {
"showLegend": true,
"displayMode": "table",
"placement": "bottom",
"calcs": [
"mean",
"max",
"min",
"sum"
],
"width": 250
}
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": true,
"expr": "clamp_min(irate(jvm_gc_pause_seconds_count{serverIp=~\"$instance\", application=\"$application\"}[1m]),0)",
"format": "time_series",
"hide": false,
"interval": "10s",
"intervalFactor": 1,
"legendFormat": "{{ip}}-{{action}} --{{cause}} GC变化率",
"range": true,
"refId": "A"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "delta(jvm_gc_pause_seconds_count{serverIp=~\"$instance\", application=\"$application\"}[1m])",
"hide": false,
"legendFormat": "{{ip}}-{{action}} --{{cause}}  -- 1m  GC次数",
"range": true,
"refId": "B"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "delta(jvm_gc_pause_seconds_count{serverIp=~\"$instance\", application=\"$application\",action=~\"end of major GC|endofminorGC\"}[1m])",
"hide": false,
"legendFormat": "{{ip}} -- {{action}} --{{cause}} 1m FullGC次数",
"range": true,
"refId": "C"
}
],
"timeFrom": null,
"timeShift": null
},

{
"id": 76,
"gridPos": {
"h": 10,
"w": 12,
"x": 12,
"y": 177
},
"type": "timeseries",
"title": "GC STW And Max Time Cost [1m]",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"pluginVersion": "9.2.0-pre",
"links": [],
"options": {
"tooltip": {
"mode": "multi",
"sort": "desc"
},
"legend": {
"showLegend": true,
"displayMode": "table",
"placement": "bottom",
"calcs": [
"mean",
"max",
"min",
"sum"
],
"width": 250
}
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": true,
"expr": "irate(jvm_gc_pause_seconds_sum{serverIp=~\"$instance\", application=\"$application\"}[1m])",
"format": "time_series",
"interval": "",
"intervalFactor": 1,
"legendFormat": "{{ip}}-{{action}} --{{cause}} STW耗时",
"range": true,
"refId": "A"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "max_over_time(jvm_gc_pause_seconds_max{serverIp=~\"$instance\", application=\"$application\"}[1m])",
"hide": false,
"legendFormat": "{{ip}}-{{action}} --{{cause}} 1m GC最大耗时",
"range": true,
"refId": "B"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "max_over_time(jvm_gc_pause_seconds_max{serverIp=~\"$instance\", application=\"$application\",action=~\"end of major GC|endofminorGC\"}[1m])",
"hide": false,
"legendFormat": "{{ip}}-{{action}} --{{cause}} 1m FullGC最大耗时",
"range": true,
"refId": "C"
}
],
"fieldConfig": {
"defaults": {
"custom": {
"drawStyle": "line",
"lineInterpolation": "linear",
"barAlignment": 0,
"lineWidth": 1,
"fillOpacity": 10,
"gradientMode": "none",
"spanNulls": false,
"showPoints": "never",
"pointSize": 5,
"stacking": {
"mode": "none",
"group": "A"
},
"axisPlacement": "auto",
"axisLabel": "",
"axisColorMode": "text",
"scaleDistribution": {
"type": "linear"
},
"axisCenteredZero": false,
"hideFrom": {
"tooltip": false,
"viz": false,
"legend": false
},
"thresholdsStyle": {
"mode": "off"
}
},
"color": {
"mode": "palette-classic"
},
"mappings": [],
"thresholds": {
"mode": "absolute",
"steps": [
{
"value": null,
"color": "green"
},
{
"value": 80,
"color": "red"
}
]
},
"unit": "s"
},
"overrides": []
},
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
"id": 176,
"gridPos": {
"h": 10,
"w": 12,
"x": 0,
"y": 187
},
"type": "timeseries",
"title": "Heap Used Graph",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {
"custom": {
"drawStyle": "line",
"lineInterpolation": "smooth",
"barAlignment": 0,
"lineWidth": 1,
"fillOpacity": 0,
"gradientMode": "none",
"spanNulls": false,
"showPoints": "auto",
"pointSize": 5,
"stacking": {
"mode": "none",
"group": "A"
},
"axisPlacement": "auto",
"axisLabel": "",
"axisColorMode": "text",
"scaleDistribution": {
"type": "linear"
},
"axisCenteredZero": false,
"hideFrom": {
"tooltip": false,
"viz": false,
"legend": false
},
"thresholdsStyle": {
"mode": "off"
}
},
"color": {
"mode": "palette-classic"
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
}
]
},
"unit": "bytes"
},
"overrides": []
},
"options": {
"tooltip": {
"mode": "single",
"sort": "none"
},
"legend": {
"showLegend": true,
"displayMode": "list",
"placement": "bottom",
"calcs": []
}
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "sum(jvm_memory_used_bytes{application=\"$application\", serverIp=~\"$instance\", area=\"heap\"}) by (id)",
"legendFormat": "__auto",
"range": true,
"refId": "A"
}
]
},


{
"id": 178,
"gridPos": {
"h": 10,
"w": 12,
"x": 12,
"y": 187
},
"type": "timeseries",
"title": "Non-Heap Used Graph",
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {
"custom": {
"drawStyle": "line",
"lineInterpolation": "smooth",
"barAlignment": 0,
"lineWidth": 2,
"fillOpacity": 1,
"gradientMode": "none",
"spanNulls": false,
"showPoints": "auto",
"pointSize": 6,
"stacking": {
"mode": "none",
"group": "A"
},
"axisPlacement": "auto",
"axisLabel": "",
"axisColorMode": "text",
"scaleDistribution": {
"type": "linear"
},
"axisCenteredZero": false,
"hideFrom": {
"tooltip": false,
"viz": false,
"legend": false
},
"thresholdsStyle": {
"mode": "off"
}
},
"color": {
"mode": "palette-classic"
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
}
]
},
"unit": "bytes"
},
"overrides": []
},
"options": {
"tooltip": {
"mode": "single",
"sort": "none"
},
"legend": {
"showLegend": true,
"displayMode": "list",
"placement": "bottom",
"calcs": []
}
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "sum(jvm_memory_used_bytes{application=\"$application\", serverIp=~\"$instance\", area=\"nonheap\"}) by (id)",
"legendFormat": "__auto",
"range": true,
"refId": "A"
}
]
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
"definition": "query_result(count(count(container_last_seen{application=\"$application\",serverEnv=~\"$serverEnv\"})by(podIp)))",
"description": null,
"error": null,
"hide": 2,
"includeAll": false,
"label": "主机数",
"multi": false,
"name": "total",
"options": [],
"query": {
"query": "query_result(count(count(container_last_seen{application=\"$application\",serverEnv=~\"$serverEnv\"})by(podIp)))",
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
"message":"hera V1.4"
}