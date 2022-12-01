FROM maven:3-jdk-11 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:11-slim-buster
COPY --from=build /usr/src/app/target/app2*.jar /usr/app/app2.jar
USER daemon
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/usr/app/app2.jar"]