import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    application
    idea
    checkstyle
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
    maven { url = uri("https://repo.eternalcode.pl/releases") }
}

checkstyle {
    toolVersion = "10.9.3"

    configFile = file("${rootDir}/checkstyle/checkstyle.xml")

    maxErrors = 0
    maxWarnings = 0
}
dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.0-beta.6")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.4")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.4.6")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.0-SNAPSHOT")

    // ORMLite
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Database drivers
    implementation("mysql:mysql-connector-java:8.0.32")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.3")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    // mockwebserver
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0")

    // mockito
    testImplementation("org.mockito:mockito-core:4.0.0")
    testImplementation("org.mockito:mockito-junit-jupiter:4.0.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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