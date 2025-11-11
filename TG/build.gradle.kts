plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

paper {
    main = "ru.hackaton.chatsync.ChatSyncPlugin"
    apiVersion = "1.21"
    authors = listOf("sidey383")
    prefix = project.name + "TG"
    foliaSupported = true
}

dependencies {
    implementation(project(":Core"))

    implementation("com.github.hakan-krgn.spigot-injection:injection-core:0.1.5.7")
    implementation("com.github.hakan-krgn.spigot-injection:injection-listener:0.1.5.7")

    implementation("org.telegram:telegrambots:6.8.0")
    implementation("org.telegram:telegrambots-abilities:6.8.0")

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.98.0")
    testImplementation("org.mockito:mockito-core:5.6.0")

    // JUnit 5 через BOM
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.version}-all.jar")
        mergeServiceFiles()
    }

    assemble {
        dependsOn(shadowJar)
    }

    jar {
        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
