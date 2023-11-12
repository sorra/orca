plugins {
    id("java")
}

group = "com.iostate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.h2database:h2:1.4.200")
}

tasks.test {
    useJUnitPlatform()
}