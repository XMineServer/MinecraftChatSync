plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
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
    implementation("com.github.hakan-krgn.spigot-injection:injection-core:0.1.5.7")
    implementation("com.github.hakan-krgn.spigot-injection:injection-listener:0.1.5.7")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
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
