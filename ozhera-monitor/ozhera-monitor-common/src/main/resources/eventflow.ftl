<!--

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
"size": "1111111111${size}",
"query": {
"constant_score": {
"filter": {
"range": {
"last_updated": {
"gte": ${gte_val},
"lte": ${lte_val}
}
}
}
}
},
"aggs": {
"by_time": {
"date_histogram": {
"field": "last_updated",
"interval": "${interval}",
"extended_bounds": {
"min": "${min_val}",
"max": "${max_val}"
}
},
"aggs": {
"event_status_group": {
"filters": {
"filters": {
"info": {
"match_phrase": {
"alert_status": "info"
}
},
"warning": {
"match_phrase": {
"alert_status": "warning"
}
},
"error": {
"match_phrase": {
"alert_status": "error"
}
},
"success": {
"match_phrase": {
"alert_status": "success"
}
}
}
}
}
}
}
}
}