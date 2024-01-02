FROM openjdk:21-jdk-bookworm

ENV APP_HOME /opt/app

RUN mkdir -p ${APP_HOME}

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY mix.4096.client.truststore.jks ${APP_HOME}/mix.4096.client.truststore.jks

COPY target/log-agent-*.jar ${APP_HOME}/log-agent.jar

WORKDIR ${APP_HOME}

ENV JAVA_OPTS="--enable-preview --add-opens=java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED  --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar log-agent.jar"]