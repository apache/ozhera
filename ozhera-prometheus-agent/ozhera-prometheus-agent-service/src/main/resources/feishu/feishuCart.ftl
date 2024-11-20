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
"wide_screen_mode": true
},
"elements": [
{
"tag": "div",
"text": {
"content": "${content}",
"tag": "lark_md"
}
},
{
"actions": [
{
"tag": "button",
"text": {
"content": "Silence the alarm",
"tag": "plain_text"
},
"type": "primary",
"multi_url": {
"url": "http://${silence_url}",
"pc_url": "",
"android_url": "",
"ios_url": ""
}
}
],
"tag": "action"
}
],
"header": {
"template": "orange",
"title": {
"content": "[${priority}][Hera]  ${title} ${alert_op} ${alert_value}",
"tag": "plain_text"
}
}
}