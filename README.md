# F1 Season Champions Application

A Spring Boot application that provides F1 race data and season champions information with configurable data seeding capabilities.

## 🚀 Quick Start

### Default Setup (Full Historical Data)
```bash
cd infrastructure
docker-compose up
```

This will seed F1 data from **2005 to current year** (2024).

### Fast Testing Setup (Limited Years)
```bash
cd infrastructure
F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up
```

This will only seed **2022, 2023, and 2024** - much faster for development!

## ⚙️ F1 Data Configuration

### Environment Variables

The application supports configurable F1 data seeding through environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `F1_SEASON_START_YEAR` | `2005` | First year to seed F1 data |
| `F1_SEASON_END_YEAR` | Current Year | Last year to seed F1 data |

### Configuration Methods

#### Method 1: Direct Environment Variables (Recommended for one-time use)
```bash
# Fast testing (3 years only)
F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up

# Single year testing
F1_SEASON_START_YEAR=2024 F1_SEASON_END_YEAR=2024 docker-compose up

# Custom range
F1_SEASON_START_YEAR=2010 F1_SEASON_END_YEAR=2015 docker-compose up
```

#### Method 2: Using f1-config.env File (Recommended for persistent configuration)

1. **Edit the configuration file:**
   ```bash
   cd infrastructure
   cp f1-config.env my-config.env
   # Edit my-config.env and uncomment desired values
   ```

2. **Example f1-config.env content:**
   ```env
   # For fast testing (only 3 years) - RECOMMENDED FOR DEVELOPMENT
   F1_SEASON_START_YEAR=2022
   F1_SEASON_END_YEAR=2024

   # For custom range:
   # F1_SEASON_START_YEAR=2010
   # F1_SEASON_END_YEAR=2020
   ```

3. **Run with the configuration:**
   ```bash
   docker-compose --env-file my-config.env up
   ```

#### Method 3: Docker Compose Environment Section
You can also uncomment and modify the environment variables in `docker-compose.yml`:

```yaml
app:
  environment:
    # Uncomment and modify these lines:
    F1_SEASON_START_YEAR: 2022
    F1_SEASON_END_YEAR: 2024
```

## 🐳 Docker Usage

### Prerequisites
- Docker
- Docker Compose

### Services
- **PostgreSQL**: Database for F1 data
- **Redis**: Caching layer
- **App**: Spring Boot application

### Common Commands

```bash
# Start all services (default configuration)
cd infrastructure
docker-compose up

# Start with custom F1 data range
F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild application
docker-compose up --build

# Clean start (remove volumes)
docker-compose down -v
docker-compose up
```

### Development Configurations

#### Fast Development (3 years)
```bash
# Quick setup for development
F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up
```

#### Single Year Testing
```bash
# Test with only current year
F1_SEASON_START_YEAR=2024 F1_SEASON_END_YEAR=2024 docker-compose up
```

#### Full Historical Data
```bash
# All data from 2005 to current year (takes longer)
docker-compose up
```

## 📊 Data Seeding Behavior

### Startup Seeding
When the application starts:

1. **Checks existing data** - Skips years that already have complete data
2. **Seeds missing years** - Only downloads data for years in the configured range
3. **Logs progress** - Shows which years are being processed

### Performance Considerations

| Configuration | Years | Approximate Time | Use Case |
|---------------|-------|------------------|----------|
| Single year (2024) | 1 | ~30 seconds | Unit testing |
| Recent years (2022-2024) | 3 | ~1-2 minutes | Development |
| Medium range (2015-2024) | 10 | ~3-5 minutes | Integration testing |
| Full historical (2005-2024) | 20 | ~8-15 minutes | Production |

## 🔗 API Endpoints

Once running, the application provides:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Race Winners**: `GET /f1/race-winners/{season}`
- **Season Metadata**: `GET /f1/race-winners/{season}/metadata`
- **All Seasons**: `GET /f1/seasons`

### API Validation

The API includes validation for season parameters:
- **Minimum year**: 2005
- **Maximum year**: Current year
- **Example error**: `GET /f1/race-winners/2027` returns validation error

## 🛠️ Development

### Local Development
```bash
# Backend only (requires local PostgreSQL and Redis)
cd backend
mvn spring-boot:run

# With Docker dependencies
cd infrastructure
docker-compose up postgres redis
cd ../backend
mvn spring-boot:run
```

### Environment Variables for Local Development
```bash
export F1_SEASON_START_YEAR=2022
export F1_SEASON_END_YEAR=2024
cd backend
mvn spring-boot:run
```

## 📝 Configuration Examples

### Team Development
```bash
# Create team config
echo "F1_SEASON_START_YEAR=2022" > infrastructure/team.env
echo "F1_SEASON_END_YEAR=2024" >> infrastructure/team.env

# Everyone uses:
docker-compose --env-file team.env up
```

### CI/CD Pipeline
```bash
# Minimal config for fast CI builds
echo "F1_SEASON_START_YEAR=2024" > infrastructure/ci.env
echo "F1_SEASON_END_YEAR=2024" >> infrastructure/ci.env

# In pipeline:
docker-compose --env-file ci.env up -d
```

### Multiple Environments
```bash
# Development (fast)
F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up

# Staging (medium)
F1_SEASON_START_YEAR=2015 F1_SEASON_END_YEAR=2024 docker-compose up

# Production (full)
docker-compose up  # Uses defaults: 2005 to current year
```

## 📋 Logs and Monitoring

### Checking Configuration
When the application starts, you'll see:
```
F1DataSchedulerService initialized with start year: 2022, end year: 2024
Starting historical race sync from 2022 to 2024
```

### Monitoring Progress
```bash
# Follow application logs
docker-compose logs -f app

# Check specific year processing
docker-compose logs app | grep "Syncing races for year"
```

## 🚨 Troubleshooting

### Common Issues

1. **Empty environment variable error**:
   ```
   Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: ""
   ```
   **Solution**: Don't set empty environment variables. Either omit them or set valid values.

2. **Year validation error**:
   ```
   Season must be 2005 or later
   ```
   **Solution**: Use years between 2005 and current year.

3. **Slow startup**:
   **Solution**: Use a smaller year range for development:
   ```bash
   F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up
   ```

### Reset Data
```bash
# Clear all data and restart
docker-compose down -v
docker-compose up
```

## 🤝 Contributing

1. Use fast configuration for development:
   ```bash
   F1_SEASON_START_YEAR=2022 F1_SEASON_END_YEAR=2024 docker-compose up
   ```
2. Test with single year for unit tests:
   ```bash
   F1_SEASON_START_YEAR=2024 F1_SEASON_END_YEAR=2024 docker-compose up
   ```
3. Ensure full historical data works before production deployment
