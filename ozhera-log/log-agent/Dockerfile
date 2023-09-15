FROM openjdk:20-jdk-bullseye

ENV APP_HOME /opt/app

RUN mkdir -p ${APP_HOME}

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY target/log-agent-1.0-SNAPSHOT.jar ${APP_HOME}

WORKDIR ${APP_HOME}

ENTRYPOINT ["sh","-c","java --enable-preview --add-opens java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED  --add-opens=java.base/java.util=ALL-UNNAMED --add-modules=jdk.incubator.concurrent --add-opens java.base/sun.nio.fs=ALL-UNNAMED -jar log-agent-1.0-SNAPSHOT.jar"]