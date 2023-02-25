plugins {
    `java-library`
    application
    idea
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
    implementation("net.dv8tion:JDA:5.0.0-beta.4")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.3")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.0-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.eternalcode.discordapp.DiscordApp")
}