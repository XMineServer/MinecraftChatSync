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