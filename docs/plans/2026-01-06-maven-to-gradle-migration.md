# Maven to Gradle (Kotlin DSL) Migration Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** Migrate build system from Maven (pom.xml) to Gradle with Kotlin DSL (build.gradle.kts), preserving all functionality while adding Gradle wrapper.

**Architecture:** Big Bang migration - create complete Gradle build with wrapper, delete Maven files after verification. Keep Resilience4j dependencies commented out (currently disabled). Use Spring Boot Gradle plugin with dependency management BOM pattern matching Maven's parent POM.

**Tech Stack:** Gradle 8.5, Kotlin DSL, Spring Boot 2.7.10, Java 21, Testcontainers 1.19.3

---

## Task 1: Create settings.gradle.kts

**Files:**
- Create: `settings.gradle.kts`

**Step 1: Create settings file**

Create `settings.gradle.kts`:

```kotlin
rootProject.name = "ontop"
```

**Step 2: Verify file exists**

Run: `cat settings.gradle.kts`
Expected: File displays `rootProject.name = "ontop"`

**Step 3: Commit**

```bash
git add settings.gradle.kts
git commit -m "feat(gradle): add settings.gradle.kts"
```

---

## Task 2: Create gradle.properties

**Files:**
- Create: `gradle.properties`

**Step 1: Create gradle properties file**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=true
version=0.0.1-SNAPSHOT
```

**Step 2: Verify file exists**

Run: `cat gradle.properties`
Expected: File displays properties

**Step 3: Commit**

```bash
git add gradle.properties
git commit -m "feat(gradle): add gradle.properties with JVM config"
```

---

## Task 3: Create build.gradle.kts Core Configuration

**Files:**
- Create: `build.gradle.kts`

**Step 1: Create build file with plugins and basic configuration**

Create `build.gradle.kts`:

```kotlin
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
```

**Step 2: Verify file compiles**

Run: `./gradlew tasks --dry-run` 2>&1 | head -5
Expected: Wrapper downloads, Gradle initializes, or error about wrapper not existing (expected at this stage)

**Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "feat(gradle): add build.gradle.kts with core configuration"
```

---

## Task 4: Add Dependencies to build.gradle.kts

**Files:**
- Modify: `build.gradle.kts` (add dependencies block)

**Step 1: Add dependencies block**

Append to `build.gradle.kts` after the tasks configuration:

```kotlin
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
    // implementation("io.github.resilience4j:resilience4j-spring-boot2:2.1.0")
    // implementation("io.github.resilience4j:resilience4j-feign:2.1.0")
}
```

**Step 2: Verify Gradle can resolve dependencies**

Run: `./gradlew dependencies --configuration compileClasspath` 2>&1 | tail -20
Expected: Dependency tree displayed, no resolution errors

**Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "feat(gradle): add all dependencies to build.gradle.kts"
```

---

## Task 5: Configure Test Task

**Files:**
- Modify: `build.gradle.kts` (add test task configuration)

**Step 1: Add test task configuration**

Add to `build.gradle.kts` after the existing tasks configuration:

```kotlin
tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = 1
    systemProperty("spring.profiles.active", "test")
    systemProperty("java.security.egd", "file:/dev/./urandom")
    jvmArgs("-Xmx2g")
}
```

**Step 2: Verify test task is configured**

Run: `./gradlew test --dry-run`
Expected: Shows test task would run with JUnit platform

**Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "feat(gradle): configure test task for sequential execution"
```

---

## Task 6: Generate Gradle Wrapper

**Files:**
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `gradle/wrapper/gradle-wrapper.jar`
- Create: `gradle/wrapper/gradle-wrapper.properties`

**Step 1: Initialize Gradle wrapper**

If Gradle is installed:
```bash
gradle wrapper --gradle-version 8.5
```

If Gradle is not installed, use the gradlew that will be generated, or create wrapper files manually:

Create `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

Download gradle-wrapper.jar from https://github.com/gradle/gradle/blob/v8.5.0/gradle/wrapper/gradle-wrapper.jar and place in `gradle/wrapper/`

Create `gradlew` script with proper execute permissions.

**Step 2: Make gradlew executable**

Run: `chmod +x gradlew`
Expected: No error

**Step 3: Verify wrapper works**

Run: `./gradlew --version`
Expected: Displays Gradle 8.5 version info

**Step 4: Commit wrapper files**

```bash
git add gradlew gradlew.bat gradle/wrapper/
git commit -m "feat(gradle): generate and commit Gradle wrapper 8.5"
```

---

## Task 7: Run Full Build and Tests

**Files:**
- None (verification step)

**Step 1: Run clean build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESS, JAR created in build/libs/

**Step 2: Run tests**

Run: `./gradlew test`
Expected: Tests run: 137, Failures: 0, Errors: 0, Skipped: 0

**Step 3: Verify JAR is created**

Run: `ls -lh build/libs/ontop-0.0.1-SNAPSHOT.jar`
Expected: File exists, size is reasonable (several MB)

**Step 4: Verify bootRun works**

Run: `./gradlew bootRun --args='--spring.profiles.active=test'` then Ctrl+C after startup
Expected: Application starts successfully

**Step 5: Update TESTING.md**

Append to TESTING.md:

```markdown
## Gradle Commands

### Run all tests
```bash
./gradlew test
```

### Build application
```bash
./gradlew clean build
```

### Run application
```bash
./gradlew bootRun
```

### View dependency tree
```bash
./gradlew dependencies
```

Note: Integration tests use Docker to run MongoDB. Ensure Docker is installed and running.
```

**Step 6: Commit documentation update**

```bash
git add TESTING.md
git commit -m "docs: add Gradle commands to TESTING.md"
```

---

## Task 8: Delete Maven Files

**Files:**
- Delete: `pom.xml`
- Delete: `mvnw` (if exists)
- Delete: `mvnw.cmd` (if exists)
- Delete: `.mvn/` directory (if exists)

**Step 1: Verify Gradle build still works**

Run: `./gradlew clean build`
Expected: BUILD SUCCESS (confirmation before deletion)

**Step 2: Delete Maven files**

```bash
rm -f pom.xml mvnw mvnw.cmd
rm -rf .mvn/
```

**Step 3: Verify Maven files are gone**

Run: `ls -la | grep -E "(pom\.xml|mvnw|\.mvn)"` || echo "No Maven files found"
Expected: "No Maven files found"

**Step 4: Final build verification**

Run: `./gradlew clean build`
Expected: BUILD SUCCESS

**Step 5: Final test run**

Run: `./gradlew test`
Expected: Tests run: 137, Failures: 0, Errors: 0, Skipped: 0

**Step 6: Commit Maven removal**

```bash
git add -A
git commit -m "feat(gradle): remove Maven build files, migration complete"
```

---

## Task 9: Final Verification and Documentation

**Files:**
- Modify: `README.md` (update build instructions if present)

**Step 1: Verify all Gradle commands work**

Run each command and verify success:
- `./gradlew clean build` - Should create JAR
- `./gradlew test` - All 137 tests pass
- `./gradlew bootJar` - Creates bootable JAR
- `./gradlew dependencies` - Shows dependency tree

**Step 2: Check for any remaining Maven references**

Run: `grep -r "mvn" --include="*.md" --include="*.sh" --include="*.yml" --include="*.yaml" .`
Expected: No Maven references in documentation or scripts (update if found)

**Step 3: Create migration completion summary**

Create `docs/plans/2026-01-06-gradle-migration-complete.md`:

```markdown
# Maven to Gradle Migration Complete

## Migration Summary

Successfully migrated from Maven to Gradle with Kotlin DSL on 2026-01-06.

## Changes Made

1. **Build System**: Maven (pom.xml) → Gradle (build.gradle.kts)
2. **Wrapper**: Generated Gradle wrapper 8.5
3. **Dependencies**: All 137 dependencies migrated with correct configurations
4. **Test Configuration**: Sequential execution (maxParallelForks=1) for Testcontainers
5. **Lombok**: Using io.freefair.lombok plugin for better integration
6. **Spring Boot**: Version 2.7.10 with dependency management BOM
7. **Spring Cloud**: Version 2021.0.6 via BOM
8. **Testcontainers**: Version 1.19.3 via BOM
9. **Resilience4j**: Kept commented out (compatibility issue documented)

## Verification

- ✅ All 137 tests passing
- ✅ Build creates bootable JAR
- ✅ Integration tests work with Testcontainers
- ✅ Application starts successfully with bootRun
- ✅ No Maven files remaining

## Migration Notes

- Used Big Bang approach (complete replacement, not side-by-side)
- Gradle 8.5 selected for Java 21 compatibility
- Kotlin DSL chosen for type safety and IDE support
- Resilience4j dependencies preserved in comments for future re-enabling

## Next Steps

- Update CI/CD pipelines to use `./gradlew` instead of `mvn`
- Consider re-enabling Resilience4j with proper version alignment
- Monitor build performance and optimize Gradle caching if needed
```

**Step 4: Commit completion documentation**

```bash
git add docs/plans/2026-01-06-gradle-migration-complete.md
git commit -m "docs: add Gradle migration completion summary"
```

**Step 5: Final summary**

Display migration complete message with verification results.

---

## Execution Notes

**Important:** Follow TDD rigorously:
1. Each task creates/modifies files
2. Verify with dry-run or actual command
3. Commit after each task
4. Move to next task only after success

**Git Commit Strategy:**
- Each task is a separate commit
- Commit messages follow conventional commits format
- Build and test between commits

**Testing Strategy:**
- All tests must pass after Task 7 (first full build)
- All tests must pass after Task 8 (after Maven deletion)
- Final verification in Task 9 confirms migration success

**Rollback Strategy:**
If Task 8 (Maven deletion) fails:
```bash
git reset --hard HEAD~1  # Undo deletion
# Can still use Maven to build
```

If earlier tasks fail:
```bash
git reset --hard HEAD~1  # Undo last commit
# Investigate and fix issue
```

**Key Migration Points:**
- Maven `scope="test"` → Gradle `testImplementation()`
- Maven `optional=true` → Gradle `compileOnly()` + Lombok plugin config
- Maven parent POM version management → Gradle `dependencyManagement` BOM imports
- Maven `-DforkCount=1` → Gradle `maxParallelForks = 1`
- Maven `spring-boot-maven-plugin` → Gradle `org.springframework.boot` plugin
