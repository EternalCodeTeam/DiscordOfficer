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
    implementation("net.dv8tion:JDA:5.0.0-beta.3")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.3")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.4.5")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.0-SNAPSHOT")

    // ORMLite Core
    implementation("com.j256.ormlite:ormlite-core:6.1")

    // ORMLite JDBC
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // MySQL JDBC Driver
    implementation("mysql:mysql-connector-java:8.0.32")
    // PostgreSQL JDBC Driver
    implementation("org.postgresql:postgresql:42.5.3")
    // https://mvnrepository.com/artifact/com.h2database/h2
    implementation("com.h2database:h2:2.1.214")
    // MariaDB JDBC Driver
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
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