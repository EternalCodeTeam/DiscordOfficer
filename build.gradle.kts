import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    application
    idea
    checkstyle

    id("com.github.johnrengelman.shadow") version "8.0.0"
}

group = "com.eternalcode"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.eternalcode.pl/snapshots") }
    maven { url = uri("https://repo.eternalcode.pl/releases") }
}

checkstyle {
    toolVersion = "10.8.0"

    configFile = file("/checkstyle/checkstyle.xml")

    maxErrors = 0
    maxWarnings = 0
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.5")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.4")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.0-SNAPSHOT")

    // Database
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("mysql:mysql-connector-java:8.0.32")
    implementation("org.postgresql:postgresql:42.5.3")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.eternalcode.discordapp.DiscordApp")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("DiscordOfficer ${project.version}.jar")

    dependsOn("checkstyleMain")
}