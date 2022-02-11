
FROM alpine

RUN apk add openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $PATH:$JAVA_HOME/bin


WORKDIR /root/testDocker
COPY build/libs/TG_Bot-0.0.1-SNAPSHOT.jar /root/testDocker/app.jar

# set the startup command to execute the jar
#CMD java -jar /root/testDocker/app.jar --server.port $PORT

CMD java $JAVA_OPTS -jar -Dserver.port=$PORT /root/testDocker/app.jar