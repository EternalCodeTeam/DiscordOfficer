FROM gradle:jdk17-alpine AS build

WORKDIR /app/
COPY .. /app/
RUN gradle clean shadowJar

FROM amazoncorretto:17-alpine
LABEL org.label-schema.name="eternalcode/discordofficer"
LABEL org.label-schema.description="EternalCode DiscordOfficer official image"
LABEL org.label-schema.url="http://eternalcode.pl/"
LABEL org.label-schema.vcs-url="https://github.com/EternalCodeTeam/DiscordOfficer/issues"

WORKDIR /home/eternalcode
COPY --from=build /app/build/libs/ /home/eternalcode/build
RUN find /home/eternalcode/build -type f -name "DiscordOfficer*.jar" -exec mv {} /home/eternalcode/DiscordOfficer.jar \;
RUN rm -rf /home/eternalcode/build
ENTRYPOINT ["java", "-jar", "DiscordOfficer.jar"]