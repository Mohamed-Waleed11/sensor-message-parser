plugins {
    id("java")
}

group = "com.hiber"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.yaml:snakeyaml:2.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.21.0")
}

tasks.test {
    useJUnitPlatform()
}