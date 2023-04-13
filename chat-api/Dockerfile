FROM openjdk:11
ARG JAR_FILE=chat-api/build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app.jar"]