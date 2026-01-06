import io.freefair.gradle.plugins.lombok.tasks.LombokConfig

plugins {
    id("java")
    id("org.springframework.boot") version "2.7.10"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("io.freefair.lombok") version "6.6.3"
}

group = "com.ontop.balance"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2021.0.6")
        mavenBom("org.testcontainers:testcontainers-bom:1.19.3")
    }
}

lombok {
    version.set("1.18.30")
    disableConfig.set(true)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("ontop-${version.get()}.jar")
    mainClass.set("com.ontop.balance.BalanceApplication")
}
