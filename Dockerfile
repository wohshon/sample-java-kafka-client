FROM openjdk:11

# Refer to Maven build -> finalName
ARG JAR_FILE=target/kafkaclient.jar
# ARG JAR_FILE=build-artifacts/app3-1.0.jar

# cd /opt/app
WORKDIR /opt/app

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${JAR_FILE} app.jar

#EXPOSE $PORT
EXPOSE $BOOTSTRAP_SERVERS
EXPOSE $TOPIC_NAME
EXPOSE $CONSUMER_GROUP_ID
EXPOSE $PRODUCER_NUM_MSG
EXPOSE $MODE
# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]
