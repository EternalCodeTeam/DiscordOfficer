LABEL org.label-schema.name="eternalcode/discordofficer"
LABEL org.label-schema.description="EternalCode DiscordOfficer official image"
LABEL org.label-schema.url="http://eternalcode.pl/"
LABEL org.label-schema.vcs-url="https://github.com/EternalCodeTeam/DiscordOfficer/issues"
LABEL org.label-schema.docker.cmd="docker run -v $(pwd)/eternalofficer:/home/eternalcode/ -d eternalcode/discordofficer"

FROM gradle:7.6.0-jdk-alpine AS build

WORKDIR /app/
COPY --chown=gradle:gradle . /app/
RUN gradle shadowJar

FROM amazoncorretto:19
WORKDIR /home/eternalcode
COPY --from=build /app/build/libs/EternalDiscordOfficer-1.0.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "EternalDiscordOfficer-1.0.0-SNAPSHOT.jar"]