package com.xiaomi.youpin.prometheus.agent.operators.ali;

public class AliAlertMessageTemplate {
    public static final String ALERT_MESSAGE_TEMPLATE = "{\n" +
            "\t\"alerts\":[\n" +
            "\t\t{{ for .alerts }}\n" +
            "\t\t{\n" +
            "\t\t\t\"annotations\": {\n" +
            "\t\t\t\t\"title\": \"{{ .annotations.title }}\"\n" +
            "\t\t\t},\n" +
            "\t\t\t\"endsAt\": \"{{ .endsAt }}\",\n" +
            "\t\t\t\"fingerprint\": \"{{ .fingerprint }}\",\n" +
            "\t\t\t\"labels\": {\n" +
            "\t\t\t\t\"alert_op\": \"{{ .labels.alert_op }}\",\n" +
            "\t\t\t\t\"alert_value\": \"{{ .labels.alert_value }}\",\n" +
            "\t\t\t\t\"alertname\": \"{{ .labels.alertname }}\",\n" +
            "\t\t\t\t\"application\": \"{{ .labels.application }}\",\n" +
            "\t\t\t\t\"methodName\": \"{{ .labels.methodName }}\",\n" +
            "\t\t\t\t\"metrics\": \"{{ .labels.metrics }}\",\n" +
            "\t\t\t\t\"metrics_flag\": \"{{ .labels.metrics_flag }}\",\n" +
            "\t\t\t\t\"serverEnv\": \"{{ .labels.serverEnv }}\",\n" +
            "\t\t\t\t\"serverIp\": \"{{ .labels.serverIp }}\",\n" +
            "\t\t\t\t\"ip\": \"{{ .labels.ip }}\",\n" +
            "\t\t\t\t\"job\": \"{{ .labels.job }}\",\n" +
            "\t\t\t\t\"namespace\": \"{{ .labels.namespace }}\",\n" +
            "\t\t\t\t\"pod\": \"{{ .labels.pod }}\",\n" +
            "\t\t\t\t\"detailRedirectUrl\": \"{{ .labels.detailRedirectUrl }}\",\n" +
            "\t\t\t\t\"serviceName\":\"{{ .labels.serviceName }}\",\n" +
            "\t\t\t\t\"send_interval\":\"{{ .labels.send_interval }}\"\n" +
            "\t\t\t},\n" +
            "\t\t\t\"startsAt\": \"{{ .startsAt }}\",\n" +
            "\t\t\t\"status\": \"{{ .status }}\"\n" +
            "\t\t}\n" +
            "\t\t{{ end }}\n" +
            "\t],\n" +
            "\"commonAnnotations\": {\n" +
            "\t\"title\": \"{{ .commonAnnotations.title }}\"\n" +
            "},\n" +
            "\"groupKey\":null,\n" +
            "\"receiver\":null,\n" +
            "\"truncatedAlerts\":0,\n" +
            "\"version\":4,\n" +
            "  \"status\": \"{{ .status }}\",\n" +
            "  \"startTime\":\"{{ .startTime }}\",\n" +
            "  \"endTime\":\"{{ .endTime }}\",\n" +
            "  \"level\":\"{{ .level }}\",\n" +
            "  \"dispatchRuleName\":\"{{ .dispatchRuleName }}\",\n" +
            "  \"alarmId\":\"{{ .alarmId }}\"\n" +
            "}";
}
