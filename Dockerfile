FROM openjdk:17-ea-jdk-buster
VOLUME /tmp
COPY ./target/*.jar application.jar
ENTRYPOINT ["java","-jar","application.jar"]