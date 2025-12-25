plugins {
    id("java")
    id("application")
}

group = "org.lab"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
    options.release = 26
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

application {
    mainClass = "org.lab.Main"
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}