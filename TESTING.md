# Running Tests

## All Tests
Run the complete test suite:
```bash
./gradlew test
```

**Note:** Integration tests use Docker to run MongoDB. Ensure Docker is installed and running.

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

### Create bootable JAR
```bash
./gradlew bootJar
```

### View dependency tree
```bash
./gradlew dependencies
```

## Migration Note

This project has been migrated from Maven to Gradle. The old Maven test commands (e.g., `mvn test -Dtest='!*IntegrationTest'`) are no longer supported. All tests can now be run using Gradle commands. Tests are configured to run sequentially to avoid Testcontainers conflicts.

Note: Integration tests use Docker to run MongoDB. Ensure Docker is installed and running. If you're using Podman instead of Docker, you may need to configure TestContainers to work with Podman or set up a Docker compatibility layer.
