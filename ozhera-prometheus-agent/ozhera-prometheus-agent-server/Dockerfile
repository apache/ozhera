FROM openjdk:21-jdk-bookworm
MAINTAINER youpin-biz-arch@xiaomi.com
RUN mkdir -p /home/work/prometheus-agent/
RUN mkdir -p /home/work/log/prometheus-agent/
COPY ./target/ozhera-prometheus-agent-server-1.0.0-jdk21.jar /home/work/prometheus-agent/
ENTRYPOINT ["sh","-c","java -Xms512M -Xmx512M -XX:+UseZGC --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/sun.reflect=ALL-UNNAMED --add-opens java.base/java.xml=ALL-UNNAMED --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-exports java.xml/com.sun.org.apache.xerces.internal.impl.dv.util=ALL-UNNAMED -Duser.timezone=Asia/Shanghai -jar /home/work/prometheus-agent/ozhera-prometheus-agent-server-1.0.0-jdk21-SNAPSHOT.jar"]