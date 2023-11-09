FROM openjdk:8

RUN echo 'Asia/Shanghai' >/etc/timezone

RUN mkdir -p /home/work/app-server/
RUN mkdir -p /home/work/log/app-server/
COPY ./target/app-server-1.4-SNAPSHOT.jar /home/work/app-server/app-server-1.4-SNAPSHOT.jar
ENTRYPOINT ["java","-XX:+HeapDumpOnOutOfMemoryError","-XX:HeapDumpPath=/home/work/log/hera-app","-jar","/home/work/app-server/app-server-1.4-SNAPSHOT.jar"]