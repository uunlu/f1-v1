# PostgreSQL Table Mapping Integration Tests

## Overview

The `PostgreSQLTableMappingIntegrationTest` class provides comprehensive integration tests for PostgreSQL table mappings using TestContainers. These tests ensure that:

1. **Basic entity persistence** works correctly with PostgreSQL
2. **Custom repository methods** function as expected
3. **Schema evolution resilience** - tests won't break when columns are added/removed
4. **JPA relationships** between entities are properly mapped

## Test Coverage

### Entity Persistence Tests

- **Driver Entity**: Tests basic CRUD operations and field mappings
- **Constructor Entity**: Tests auto-generated IDs and custom `findByConstructorId` method
- **SeasonChampion Entity**: Tests relationships with Driver and Constructor entities
- **RaceWinner Entity**: Tests relationships and unique constraints

### Custom Repository Method Tests

- **SeasonChampionRepository**:
  - `findBySeasonBetweenOrderBySeason()` - Range queries with ordering
  - `findAllSeasonChampionListItems()` - Native query projections

- **RaceWinnerRepository**:
  - `findBySeasonAndOptionalRound()` - Parameterized queries with optional filters
  - `getRaceWinnerBySeason()` - Default interface methods
  - `findRaceWinnersWithConstructors()` - Complex JPQL with joins and projections

- **ConstructorRepository**:
  - `findByConstructorId()` - Custom finder methods

### Schema Evolution Resilience

Tests ensure that:
- Field-based mapping works correctly (not position-based)
- Named projections in native queries use column names
- Adding/removing columns won't break existing code
- JPA annotations properly map to PostgreSQL table structure

### Database Constraints Testing

- **Unique Constraints**: Tests that `(season, round)` uniqueness is enforced in `race_winners`
- **Foreign Key Relationships**: Verifies proper JPA relationship mappings
- **Auto-Generated IDs**: Tests BIGSERIAL primary key generation

## Technology Stack

- **TestContainers**: Provides real PostgreSQL instances for testing
- **PostgreSQL 15**: Using Alpine image for lightweight testing
- **Spring Boot Test**: Full application context with transaction support
- **JUnit 5**: Modern testing framework with AssertJ for fluent assertions
- **Flyway**: Database migrations are applied automatically

## Test Configuration

The tests use:
- **PostgreSQL Container**: `postgres:15-alpine` with test database
- **Dynamic Properties**: Configures Spring to use the TestContainer database
- **Transactional**: Each test runs in a transaction that's rolled back
- **Minimal Test Data**: Uses builder patterns for clean test setup

## Running the Tests

### Prerequisites
- Docker must be running (for TestContainers)
- Java 21+
- Maven 3.6.3+

### Run All Integration Tests
```bash
mvn test -Dtest=PostgreSQLTableMappingIntegrationTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=PostgreSQLTableMappingIntegrationTest#testDriverTableMapping
```

### Run with Test Output
```bash
mvn test -Dtest=PostgreSQLTableMappingIntegrationTest -Dspring.jpa.show-sql=true
```

## Benefits

### 1. **Real Database Testing**
- Tests against actual PostgreSQL (not in-memory H2)
- Catches database-specific issues early
- Validates actual SQL generation and constraints

### 2. **Schema Evolution Safety**
- Field-based assertions prevent breakage when schema changes
- Named projections ensure query stability
- Tests verify that entity mappings work with real database constraints

### 3. **CI/CD Ready**
- No external database dependencies
- TestContainers handles database lifecycle
- Fast execution with minimal overhead

### 4. **Comprehensive Coverage**
- Tests all custom repository methods
- Validates complex JPQL queries with joins
- Ensures relationship mappings work correctly

## Test Structure

Each test follows this pattern:
1. **Setup**: Create minimal test data using builders
2. **Action**: Execute the functionality under test
3. **Verification**: Assert expected behavior using fluent assertions
4. **Cleanup**: Automatic transaction rollback

## Future Extensibility

When adding new entities or repository methods:
1. Add corresponding test methods following the existing patterns
2. Use field-based assertions for schema evolution resilience
3. Test both positive and negative scenarios
4. Verify any custom constraints or relationships

The tests are designed to be maintainable and serve as living documentation of the database schema and repository contracts. 