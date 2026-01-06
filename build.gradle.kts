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
    mainClass.set("com.ontop.OntopApplication")
}

dependencies {
    // Spring Boot Core
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Cloud OpenFeign
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // JWT Token
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Spring Security Crypto (BCrypt)
    implementation("org.springframework.security:spring-security-crypto")

    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")

    // Test Dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mongodb")

    // Resilience4j - DISABLED due to compatibility issues
    // Will be re-enabled with proper version alignment
    // implementation("io.github.resilience4j:resilience4j-spring-boot2:1.7.1")
    // implementation("io.github.resilience4j:resilience4j-feign:1.7.1")
}
