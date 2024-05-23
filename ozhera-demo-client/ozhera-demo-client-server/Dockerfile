FROM openjdk:21-jdk-bookworm

RUN echo 'Asia/Shanghai' >/etc/timezone

COPY ./target/ozhera-demo-client-server-1.0.0-jdk21.jar /home/work/ozhera-demo-client-server-1.0.0-jdk21-SNAPSHOT.jar
COPY ./opentelemetry-javaagent.jar /opt/soft/opentelemetry-javaagent-0.1.0-SNAPSHOT-all.jar

ENTRYPOINT ["sh","-c","java -javaagent:/opt/soft/opentelemetry-javaagent-0.1.0-SNAPSHOT-all.jar -Dotel.exporter.prometheus.nacos.addr=nacos:80 -Dotel.exporter.log.pathprefix=/home/work/log/ -XX:+UseZGC --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/sun.reflect=ALL-UNNAMED --add-opens java.base/java.xml=ALL-UNNAMED --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-exports java.xml/com.sun.org.apache.xerces.internal.impl.dv.util=ALL-UNNAMED -Duser.timezone=Asia/Shanghai -jar /home/work/ozhera-demo-client-server-1.0.0-jdk21-SNAPSHOT.jar"]