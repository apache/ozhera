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
"config": {
"autoLayout": true,
"enableForward": true
},
"header": {
"title": {
"type": "text",
"text": "[${priority}][Hera]  ${title} ${alert_op} ${alert_value}"
},
"logo": "@lALPDfJ6V_FPDmvNAfTNAfQ"
},
"contents": [
{
"type": "markdown",
"text": "${content}",
"id": "text_1658220665485"
},
{
"type": "action",
"actions": [
{
"type": "button",
"label": {
"type": "text",
"text": "查看",
"id": "text_1700014087919"
},
"actionType": "openLink",
"url": {
"all": "${detailRedirectUrl}"
},
"status": "normal",
"iconCode": "icon_XDS_Todo2",
"id": "button_1700014087923"
},
{
"type": "button",
"label": {
"type": "text",
"text": "报警静默2h",
"id": "text_1696818602521"
},
"actionType": "request",
"value": "${silence2h}",
"iconCode": "icon_notice_warning",
"status": "normal",
"id": "button_1647246177301"
},
{
"type": "button",
"label": {
"type": "text",
"text": "报警静默1d",
"id": "text_1696818602521"
},
"actionType": "request",
"value": "${silence1d}",
"iconCode": "icon_notice_warning",
"status": "primary",
"id": "button_1647246177302"
},
{
"type": "button",
"label": {
"type": "text",
"text": "报警静默3d",
"id": "text_1696818602521"
},
"actionType": "request",
"value": "${silence3d}",
"iconCode": "icon_notice_warning",
"status": "warning",
"id": "button_1647246177303"
}
],
"id": "action_1696840868884"
}
]
}