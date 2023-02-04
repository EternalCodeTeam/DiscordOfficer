FROM gradle:7.6.0-jdk-alpine AS build

WORKDIR /app/
COPY --chown=gradle:gradle . /app/
RUN gradle shadowJar

FROM amazoncorretto:19
WORKDIR /home/eternalcode
COPY --from=build /app/build/libs/EternalDiscordOfficer-1.0.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "EternalDiscordOfficer-1.0.0-SNAPSHOT.jar"]