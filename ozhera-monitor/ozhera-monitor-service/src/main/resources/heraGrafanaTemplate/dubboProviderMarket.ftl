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
"exemplar": true,
"expr": "sum(sum_over_time(${env}_${serviceName}_dubboMethodCalledCount_total{application=\"$application\",serviceName=\"$serviceName\",serverEnv=~\"$env\"}[30s])/30) by (methodName)",
"interval": "",
"legendFormat": "{{methodName}}",
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": " Dubbo调入-Method QPS",
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
"exemplar": true,
"expr": "histogram_quantile(0.99,sum(sum_over_time(${env}_${serviceName}_dubboProviderCount_bucket{application=\"$application\",serviceName=\"$serviceName\",serverEnv=~\"$env\"}[30s])) by (le,methodName))",
"interval": "",
"legendFormat": "{{methodName}}",
"refId": "A"
}
],
"thresholds": [],
"timeRegions": [],
"title": " Dubbo调入-Method P99-RT",
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
"tags": [
"mione"
],
"templating": {
"list": [
{
"current": {
"selected": false,
"text": "150742_diamond_push",
"value": "150742_diamond_push"
},
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"definition": "label_values(container_last_seen{system=\"mione\"},application)",
"hide": 0,
"includeAll": false,
"label": "服务名",
"multi": false,
"name": "application",
"options": [],
"query": {
"query": "label_values(container_last_seen{system=\"mione\"},application)",
"refId": "StandardVariableQuery"
},
"refresh": 1,
"regex": "",
"skipUrlSync": false,
"sort": 0,
"tagValuesQuery": "",
"tagsQuery": "",
"type": "query",
"useTags": false
},
{
"allValue": ".*",
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
"current": {
"selected": false,
"text": "com.xiaomi.youpin.diamond.push.api.service.HaishenDataService",
"value": "com.xiaomi.youpin.diamond.push.api.service.HaishenDataService"
},
"hide": 0,
"includeAll": false,
"label": "dubbo服务名",
"multi": false,
"name": "serviceName",
"options": [
{
"selected": true,
"text": "a",
"value": "a"
},
{
"selected": false,
"text": "b",
"value": "b"
},
{
"selected": false,
"text": "c",
"value": "c"
}
],
"query": "a,b,c",
"queryValue": "",
"skipUrlSync": false,
"type": "custom"
}
]
},
"time": {
"from": "now-30m",
"to": "now"
},
"timepicker": {},
"timezone": "",
"title": "Hera-DubboProvider大盘",
"uid": "Hera-DubboProviderMarket",
"version": 1,
"weekStart": ""
},
"overwrite":true,
"folderUid":"Hera",
"message":"Hera-DubboProvider大盘V1.1"
}