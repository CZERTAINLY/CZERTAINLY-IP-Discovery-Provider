# Build stage
FROM maven:3.8.7-eclipse-temurin-17 as build
COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /root/.m2/settings.xml
ARG SERVER_USERNAME
ARG SERVER_PASSWORD
RUN mvn -f /home/app/pom.xml clean package

# Package stage
#FROM eclipse-temurin
FROM eclipse-temurin:17-jre-alpine
#ARG JAR_FILE=target/*.jar
COPY --from=build /home/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]