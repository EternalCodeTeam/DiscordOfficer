import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    application
    id("idea")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.eternalcode"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()

    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("io.github.freya022:BotCommands:2.8.2")

    // configs
    implementation("net.dzikoysk:cdn:1.14.3")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.2.8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("EternalDiscordOfficer")
    archiveVersion.set("1.0.0")
    archiveClassifier.set("SNAPSHOT")

    mergeServiceFiles()
    minimize()

    val prefix = "com.eternalcode.discordapp.libs"

    listOf(
        "net.dv8tion",
        "io.github.freya022",
        "org.slf4j",
    ).forEach { pack ->
        relocate(pack, "$prefix.$pack")
    }
}

application {
    mainClass.set("com.eternalcode.discordapp.DiscordApp")
}