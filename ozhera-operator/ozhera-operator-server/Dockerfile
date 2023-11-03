FROM herahub/opensource-pub:ozhera-base-jdk8-alpine-v1

COPY ./target/ozhera-operator-server-1.0-SNAPSHOT.jar /home/work/ozhera-operator-server-1.0-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","/home/work/ozhera-operator-server-1.0-SNAPSHOT.jar","&&","tail","-f","/dev/null"]