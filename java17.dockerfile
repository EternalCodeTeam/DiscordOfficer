FROM amazoncorretto:17-alpine AS build

WORKDIR /app/
COPY . /app/
RUN chmod +x ./gradlew
RUN ./gradlew shadowJar

FROM amazoncorretto:17-alpine
LABEL org.label-schema.name="eternalcode/discordofficer"
LABEL org.label-schema.description="EternalCode DiscordOfficer official image"
LABEL org.label-schema.url="http://eternalcode.pl/"
LABEL org.label-schema.vcs-url="https://github.com/EternalCodeTeam/DiscordOfficer/issues"
LABEL org.label-schema.docker.cmd="docker run -e OFFICER_TOKEN=TOKEN -e OFFICER_OWNER=OWNER_ID -d eternalcode/discordofficer"

WORKDIR /home/eternalcode
COPY --from=build /app/build/libs/EternalDiscordOfficer-1.0.0-SNAPSHOT.jar .
ENTRYPOINT ["java", "-jar", "EternalDiscordOfficer-1.0.0-SNAPSHOT.jar"]
