FROM openjdk:21-jdk-bookworm

RUN mkdir -p /home/work/hera-demo-server/
RUN mkdir -p /home/work/log/hera-demo-server/
COPY ./target/ozhera-demo-server-server-1.0.0-jdk21-SNAPSHOT.jar /home/work/hera-demo-server/
ENTRYPOINT ["sh","-c","java -XX:+UseZGC --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.math=ALL-UNNAMED --add-opens java.base/sun.reflect=ALL-UNNAMED --add-opens java.base/java.xml=ALL-UNNAMED --add-exports java.base/sun.reflect.annotation=ALL-UNNAMED --add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-exports java.xml/com.sun.org.apache.xerces.internal.impl.dv.util=ALL-UNNAMED -Duser.timezone=Asia/Shanghai -jar /home/work/hera-demo-server/ozhera-demo-server-server-1.0.0-jdk21-SNAPSHOT.jar"]