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
    toolVersion = "10.17.0"

    configFile = file("${rootDir}/checkstyle/checkstyle.xml")

    maxErrors = 0
    maxWarnings = 0
}

// https://github.com/JabRef/jabref/pull/10812/files#diff-49a96e7eea8a94af862798a45174e6ac43eb4f8b4bd40759b5da63ba31ec3ef7R267
configurations.named("checkstyle") {
    resolutionStrategy {
        capabilitiesResolution {
            withCapability("com.google.collections:google-collections") {
                select("com.google.guava:guava:33.2.1-jre")
            }
        }
    }
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.0.2")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.5")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.5.7")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.0-SNAPSHOT")

    // Sentry.io integration
    implementation("io.sentry:sentry:7.14.0")

    // ORMLite
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Database drivers
    implementation("com.h2database:h2:2.3.232")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.4.1")

    // Gson
    implementation("com.google.code.gson:gson:2.11.0")

    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")

    // mockwebserver
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")

    // mockito
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.16.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.shadowJar {
    archiveFileName.set("DiscordOfficer v${project.version}.jar")

    dependsOn("checkstyleMain")
    dependsOn("test")

    manifest {
        attributes(
            "Main-Class" to "com.eternalcode.discordapp.DiscordApp",
        )
    }
}

application {
    mainClass.set("com.eternalcode.discordapp.DiscordApp")
}
