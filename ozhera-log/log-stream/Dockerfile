FROM openjdk:8-jre

ENV APP_HOME /opt/app

RUN mkdir -p ${APP_HOME}

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY target/log-stream-1.1-SNAPSHOT.jar ${APP_HOME}

WORKDIR ${APP_HOME}

ENTRYPOINT ["java", "-jar", "log-stream-1.1-SNAPSHOT.jar"]