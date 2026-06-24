plugins {
    `java-library`
    application
    idea
    id("com.gradleup.shadow") version "9.4.2"
}

group = "com.eternalcode"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()

    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven {
        name = "chew"
        url = uri("https://m2.chew.pro/releases")
    }

    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.eternalcode.pl/snapshots") }
    maven { url = uri("https://repo.eternalcode.pl/releases") }
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:6.4.2")  {
        exclude("opus-java", "opus-java")
    }

    // configs
    implementation("net.dzikoysk:cdn:1.14.9")

    // slf4j setup
    implementation("ch.qos.logback:logback-classic:1.5.35")

    // new modern fork of jda-utilities
    implementation("pw.chew:jda-chewtils-command:2.2.1")

    // Sentry.io integration
    implementation("io.sentry:sentry:8.45.0")

    // ORMLite
    implementation("com.j256.ormlite:ormlite-core:6.1")
    implementation("com.j256.ormlite:ormlite-jdbc:6.1")

    // HikariCP
    implementation("com.zaxxer:HikariCP:7.1.0")

    // Database drivers
    implementation("com.h2database:h2:2.4.240")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.9")

    // Gson
    implementation("com.google.code.gson:gson:2.14.0")

    // tests
    testImplementation(platform("org.junit:junit-bom:6.1.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // mockwebserver
    testImplementation("com.squareup.okhttp3:mockwebserver:5.4.0")
    testImplementation("com.squareup.okhttp3:okhttp:5.4.0")

    // mockito
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.20.0")

    implementation("com.eternalcode:eternalcode-commons-shared:1.4.1")

    implementation("dev.skywolfxp:discord-channel-html-transcript:3.0.0")
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
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.shadowJar {
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
