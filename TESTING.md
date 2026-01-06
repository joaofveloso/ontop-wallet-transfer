# Running Tests

## Unit Tests
Unit tests run without external dependencies:
```bash
mvn test -Dtest='!*IntegrationTest'
```

## Integration Tests
Integration tests use Testcontainers to spin up MongoDB automatically:
```bash
mvn test -Dtest='*IntegrationTest'
```

## All Tests
Run the complete test suite:
```bash
mvn test
```

**Note:** Integration tests use Docker to run MongoDB. Ensure Docker is installed and running.

## Troubleshooting

If you encounter "Broken pipe" errors when running the full test suite, try running tests sequentially:
```bash
mvn test -DforkCount=1 -DreuseForks=false
```

This is a known issue with parallel test execution and Testcontainers container management. All 137 tests pass successfully when run sequentially.
