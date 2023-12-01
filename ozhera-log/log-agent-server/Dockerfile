FROM openjdk:21-jdk-bookworm

ENV APP_HOME /opt/app

RUN mkdir -p ${APP_HOME}

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY target/log-agent-server-*.jar ${APP_HOME}/log-agent-server.jar

WORKDIR ${APP_HOME}

ENV JAVA_OPTS="--add-opens=java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED  --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar log-agent-server.jar"]