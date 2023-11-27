FROM openjdk:21-jdk-bookworm

ENV APP_HOME /opt/app

RUN mkdir -p ${APP_HOME}

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY target/log-manager-*.jar ${APP_HOME}/log-manager.jar

WORKDIR ${APP_HOME}

ENV JAVA_OPTS="--add-opens=java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED  --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar log-manager.jar"]