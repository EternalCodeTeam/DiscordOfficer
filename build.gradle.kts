import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    application
    idea

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.eternalcode"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.eternalcode.pl/snapshots") }
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.5")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.4")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.4.6")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.0-SNAPSHOT")

    // gson
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

tasks.withType<ShadowJar> {
    archiveFileName.set("DiscordOfficer v${project.version}.jar")

    manifest {
        attributes(
            "Main-Class" to "com.eternalcode.discordapp.DiscordApp",
        )
    }
}

application {
    mainClass.set("com.eternalcode.discordapp.DiscordApp")
}