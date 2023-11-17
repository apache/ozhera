{
"dashboard":{
"annotations": {
"list": [
{
"builtIn": 1,
"datasource": {
"type": "grafana",
"uid": "-- Grafana --"
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
"description": "",
"editable": true,
"fiscalYearStartMonth": 0,
"graphTooltip": 0,
"id": null,
"links": [],
"liveNow": false,
"panels": [
{
"collapsed": false,
"gridPos": {
"h": 1,
"w": 24,
"x": 0,
"y": 0
},
"id": 10,
"panels": [],
"title": "折线图",
"type": "row"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {
"color": {
"mode": "palette-classic"
},
"custom": {
"axisCenteredZero": false,
"axisColorMode": "text",
"axisLabel": "",
"axisPlacement": "auto",
"barAlignment": 0,
"drawStyle": "line",
"fillOpacity": 0,
"gradientMode": "none",
"hideFrom": {
"legend": false,
"tooltip": false,
"viz": false
},
"lineInterpolation": "linear",
"lineStyle": {
"fill": "solid"
},
"lineWidth": 1,
"pointSize": 5,
"scaleDistribution": {
"type": "linear"
},
"showPoints": "auto",
"spanNulls": 3600000,
"stacking": {
"group": "A",
"mode": "none"
},
"thresholdsStyle": {
"mode": "off"
}
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
}
},
"overrides": []
},
"gridPos": {
"h": 11,
"w": 24,
"x": 0,
"y": 1
},
"id": 14,
"options": {
"legend": {
"calcs": [
"min",
"mean",
"max"
],
"displayMode": "table",
"placement": "right",
"showLegend": true,
"sortBy": "Min",
"sortDesc": false
},
"tooltip": {
"mode": "multi",
"sort": "desc"
}
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "sum(sum_over_time(staging_jaeger_dbError_total{system=\"mione\",application=~\"$application\"}[30s])) by (dataSource,application) ",
"hide": false,
"legendFormat": "{{application}} -- {{dataSource}} 错误数",
"range": true,
"refId": "B"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "sum(sum_over_time(staging_jaeger_dbSlowQuery_total{system=\"mione\",application=~\"$application\"}[30s])) by (dataSource,application) ",
"hide": false,
"legendFormat": "{{application}} -- {{dataSource}} 慢查询数",
"range": true,
"refId": "C"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "sum(sum_over_time(staging_jaeger_sqlTotalCount_total{system=\"mione\",application=~\"$application\"}[30s])) by (dataSource,application) ",
"hide": true,
"legendFormat": "{{application}} -- {{dataSource}} 总数",
"range": true,
"refId": "D"
}
],
"title": "DB 慢查询数量/错误数量",
"type": "timeseries"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {
"color": {
"mode": "palette-classic"
},
"custom": {
"axisCenteredZero": false,
"axisColorMode": "text",
"axisLabel": "",
"axisPlacement": "auto",
"barAlignment": 0,
"drawStyle": "line",
"fillOpacity": 0,
"gradientMode": "none",
"hideFrom": {
"legend": false,
"tooltip": false,
"viz": false
},
"lineInterpolation": "linear",
"lineWidth": 1,
"pointSize": 5,
"scaleDistribution": {
"type": "linear"
},
"showPoints": "auto",
"spanNulls": 3600000,
"stacking": {
"group": "A",
"mode": "none"
},
"thresholdsStyle": {
"mode": "off"
}
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
}
},
"overrides": []
},
"gridPos": {
"h": 11,
"w": 24,
"x": 0,
"y": 12
},
"id": 16,
"options": {
"legend": {
"calcs": [
"min",
"mean",
"max"
],
"displayMode": "table",
"placement": "right",
"showLegend": true
},
"tooltip": {
"mode": "multi",
"sort": "desc"
}
},
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"expr": "sum(sum_over_time(staging_jaeger_redisError_total{system=\"mione\",application=~\"$application\"}[30s])) by (host,application) ",
"legendFormat": "{{application}} -- {{host}}",
"range": true,
"refId": "A"
}
],
"title": "Redis 错误数量",
"type": "timeseries"
},
{
"collapsed": false,
"gridPos": {
"h": 1,
"w": 24,
"x": 0,
"y": 23
},
"id": 12,
"panels": [],
"title": "Table",
"type": "row"
},
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"fieldConfig": {
"defaults": {
"color": {
"mode": "thresholds"
},
"custom": {
"align": "center",
"displayMode": "auto",
"inspect": false
},
"mappings": [],
"thresholds": {
"mode": "absolute",
"steps": [
{
"color": "green",
"value": null
}
]
}
},
"overrides": [
{
"matcher": {
"id": "byName",
"options": "可用性"
},
"properties": [
{
"id": "custom.displayMode",
"value": "color-background"
},
{
"id": "displayName",
"value": "错误数"
}
]
}
]
},
"gridPos": {
"h": 23,
"w": 12,
"x": 0,
"y": 24
},
"id": 2,
"options": {
"footer": {
"fields": "",
"reducer": [
"sum"
],
"show": false
},
"showHeader": true
},
"pluginVersion": "9.2.0-pre",
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": false,
"expr": "sum(sum_over_time(staging_jaeger_redisError_total{system=\"mione\",host!=\"\"}[$time])) by (host)",
"format": "table",
"hide": false,
"instant": true,
"legendFormat": "__auto",
"range": false,
"refId": "A"
}
],
"title": "$time时间内Redis 错误数",
"transformations": [
{
"id": "filterFieldsByName",
"options": {
"include": {
"names": [
"Value",
"host"
]
}
}
},
{
"id": "organize",
"options": {
"excludeByName": {},
"indexByName": {},
"renameByName": {
"Value": "可用性",
"ip": "实例"
}
}
},
{
"id": "sortBy",
"options": {
"fields": {},
"sort": [
{
"field": "可用性"
}
]
}
}
],
"type": "table"
},
{
"datasource": {},
"fieldConfig": {
"defaults": {
"color": {
"mode": "thresholds"
},
"custom": {
"align": "center",
"displayMode": "auto",
"inspect": false
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
}
},
"overrides": [
{
"matcher": {
"id": "byName",
"options": "可用性"
},
"properties": [
{
"id": "color",
"value": {
"mode": "thresholds"
}
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
"color": "red",
"value": 0.8
},
{
"color": "orange",
"value": 0.9
},
{
"color": "green",
"value": 0.95
}
]
}
},
{
"id": "unit",
"value": "percentunit"
}
]
}
]
},
"gridPos": {
"h": 23,
"w": 12,
"x": 12,
"y": 24
},
"id": 4,
"options": {
"footer": {
"fields": "",
"reducer": [
"sum"
],
"show": false
},
"showHeader": true,
"sortBy": []
},
"pluginVersion": "9.2.0-pre",
"targets": [
{
"datasource": {
"type": "prometheus",
"uid": "${prometheusUid}"
},
"editorMode": "code",
"exemplar": false,
"expr": " clamp_max(sum(sum_over_time(staging_jaeger_sqlSuccessCount_total{system=\"mione\"}[30s])) by (dataSource) / sum(sum_over_time(staging_jaeger_sqlTotalCount_total{system=\"mione\"}[30s])) by (dataSource),1)",
"format": "table",
"instant": true,
"interval": "",
"legendFormat": "__auto",
"range": false,
"refId": "A"
}
],
"title": "实时DB SLA",
"transformations": [
{
"id": "filterFieldsByName",
"options": {
"include": {
"names": [
"dataSource",
"Value"
]
}
}
},
{
"id": "organize",
"options": {
"excludeByName": {},
"indexByName": {},
"renameByName": {
"Value": "可用性",
"dataSource": "数据源"
}
}
},
{
"id": "sortBy",
"options": {
"fields": {},
"sort": [
{
"desc": false,
"field": "可用性"
}
]
}
}
],
"type": "table"
}
],
"refresh": false,
"schemaVersion": 37,
"style": "dark",
"tags": [
"Hera",
"SLA"
],
"templating": {
"list": [
{
"auto": true,
"auto_count": 1,
"auto_min": "1s",
"current": {
"selected": false,
"text": "auto",
"value": "$__auto_interval_time"
},
"hide": 2,
"name": "time",
"options": [
{
"selected": true,
"text": "auto",
"value": "$__auto_interval_time"
},
{
"selected": false,
"text": "1m",
"value": "1m"
},
{
"selected": false,
"text": "10m",
"value": "10m"
},
{
"selected": false,
"text": "30m",
"value": "30m"
},
{
"selected": false,
"text": "1h",
"value": "1h"
},
{
"selected": false,
"text": "6h",
"value": "6h"
},
{
"selected": false,
"text": "12h",
"value": "12h"
},
{
"selected": false,
"text": "1d",
"value": "1d"
},
{
"selected": false,
"text": "7d",
"value": "7d"
},
{
"selected": false,
"text": "14d",
"value": "14d"
},
{
"selected": false,
"text": "30d",
"value": "30d"
}
],
"query": "1m,10m,30m,1h,6h,12h,1d,7d,14d,30d",
"refresh": 2,
"skipUrlSync": false,
"type": "interval"
},
{
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
"definition": "label_values(container_last_seen{system=\"mione\"},application)",
"hide": 0,
"includeAll": true,
"multi": true,
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
"type": "query"
}
]
},
"time": {
"from": "now-1h",
"to": "now"
},
"timepicker": {},
"timezone": "",
"title": "hera-sla",
"uid": "hera-sla",
"version": 52,
"weekStart": ""
},
"overwrite":true,
"folderUid":"Hera",
"message":"Hera-SLA V1.0"
}