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

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Alarm email</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f9f9f9;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #ffffff;
            border: 1px solid #dddddd;
            border-radius: 5px;
        }

        h3 {
            color: #ff0000;
        }

        p {
            margin: 10px 0;
        }

        strong {
            font-weight: bold;
        }

    </style>
</head>
<body>
<div class="container">
    <h3>Alarm information</h3>
    <p><strong>Application：</strong>${application}</p>
    <p><strong>Threshold：</strong>${alert_value}</p>
    <p><strong>IP：</strong>${ip}</p>
    <p><strong>Pod：</strong>${pod}</p>
    <p><strong>Start Time：</strong>${start_time}</p>
</div>
<p class="container" style="text-align:center;">
    <a href="http://${silence_url}" style="background-color:#4CAF50; color:white; padding:10px 20px; border:none; border-radius:5px; text-decoration:none;">Silence the alarm</a>
</p>
</body>
</html>
