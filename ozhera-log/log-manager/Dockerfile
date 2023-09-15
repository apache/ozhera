FROM openjdk:8-jre

ENV APP_HOME /opt/app

RUN mkdir -p ${APP_HOME}

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY target/log-manager-1.0-SNAPSHOT.jar ${APP_HOME}
COPY target/opentelemetry-javaagent-0.1.0-SNAPSHOT-all.jar /opt/soft/opentelemetry-javaagent-0.1.0-SNAPSHOT-all.jar

WORKDIR ${APP_HOME}

ENTRYPOINT ["java", "-javaagent:/opt/soft/opentelemetry-javaagent-0.1.0-SNAPSHOT-all.jar","-Dotel.resource.attributes=service.name=12-log-manager","-Dotel.traces.exporter=log4j2","-Dotel.metrics.exporter=prometheus","-Dotel.javaagent.debug=false","-Dotel.exporter.prometheus.nacos.addr=nacos:80","-Dotel.javaagent.exclude-classes=com.dianping.cat.*","-Dotel.exporter.log.isasync=true","-Dotel.exporter.log.pathprefix=/home/work/log/","-Dotel.propagators=tracecontext", "-jar", "log-manager-1.0-SNAPSHOT.jar"]