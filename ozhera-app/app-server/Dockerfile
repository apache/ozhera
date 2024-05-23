FROM openjdk:21-jdk-bookworm

RUN echo 'Asia/Shanghai' >/etc/timezone

RUN mkdir -p /home/work/app-server/
RUN mkdir -p /home/work/log/app-server/
COPY ./target/app-server-1.4-jdk21-3.jar /home/work/app-server/app-server-1.4-SNAPSHOT.jar

ENV JAVA_OPTS="--enable-preview --add-opens=java.base/java.util.regex=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED  --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/sun.nio.fs=ALL-UNNAMED --add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /home/work/app-server/app-server-1.4-SNAPSHOT.jar"]