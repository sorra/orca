plugins {
    id("java")
}

group = "com.iostate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(group = "javax.persistence", name = "javax.persistence-api", version = "2.2")
    implementation(group = "org.freemarker", name = "freemarker", version = "2.3.32")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.16.1")
    implementation(group = "com.fasterxml.jackson.dataformat", name = "jackson-dataformat-yaml", version = "2.16.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.h2database:h2:2.2.220")
}

tasks.test {
    useJUnitPlatform()
}