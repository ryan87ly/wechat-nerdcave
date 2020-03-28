FROM openjdk:11.0-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY ./build/libs/*-all.jar /app/nerdcave.jar

CMD ["java", "-jar", "/app/nerdcave.jar"]