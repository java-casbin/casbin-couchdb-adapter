plugins {
    id("java")
    id("io.freefair.lombok") version "8.4"
}

group = "com.zikeyang"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.casbin:jcasbin:1.43.0")
    implementation("org.lightcouch:lightcouch:0.2.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.0")
    implementation("org.slf4j:slf4j-api:1.7.32")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    testImplementation("ch.qos.logback:logback-classic:1.2.6")
    testImplementation("org.mockito:mockito-core:4.0.0")

    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")


}

tasks.test {
    useJUnitPlatform()
}