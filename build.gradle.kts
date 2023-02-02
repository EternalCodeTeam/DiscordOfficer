plugins {
    `java-library`
    application
}

group = "com.eternalcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("io.github.freya022:BotCommands:2.8.2")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.2.8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("DiscordApp")
}