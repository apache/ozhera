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
"text": "报警静默",
"id": "text_1695109070630"
},
"actionType": "openLink",
"url": {
"all": "${silence_url}"
},
"status": "primary",
"id": "button_1646816888247"
}
]
}
]
}