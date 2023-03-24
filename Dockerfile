FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ./target/*.jar application.jar
ENTRYPOINT ["java","-jar","application.jar"]