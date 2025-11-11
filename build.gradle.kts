plugins {
    id("org.gradle.kotlin.kotlin-dsl") version "2.1.0" apply false
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    group = "ru.hackaton"
    version = "1.0-SNAPSHOT"

}