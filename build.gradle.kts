plugins {
    java
}

group = "com.github.komnazsk"
version = "1.0.0"
val pluginVersion = version

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to pluginVersion)
    }
}

tasks.jar {
    archiveFileName.set("KMDecoratedPot-1.0-MC26.2.jar")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
