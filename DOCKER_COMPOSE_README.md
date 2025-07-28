# Mobile Banking Backend - Docker Compose Deployment

This document provides instructions for deploying the Mobile Banking Backend application using Docker Compose with comprehensive observability capabilities.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 4GB of available RAM
- At least 10GB of available disk space

## Quick Start

1. **Clone the repository and navigate to the project directory**

2. **Set up secrets** (already configured):

   ```bash
   # Secrets are already created in the secrets/ directory
   ls secrets/
   # Should show: db_password.txt jwt_secret.txt
   ```

3. **Start the development environment**:

   ```bash
   ./deploy.sh
   # Or manually:
   docker-compose up -d
   ```

4. **Verify the deployment**:

   ```bash
   # Check service status
   docker-compose ps

   # Check application health
   curl http://localhost:8080/api/actuator/health
   ```

## Architecture Overview

The Docker Compose setup includes:

- **Mobile Banking Backend**: Spring Boot application with observability features
- **PostgreSQL Database**: Persistent data storage with initialization scripts
- **Docker Secrets**: Secure management of sensitive credentials
- **Docker Networks**: Isolated network for service communication
- **Health Checks**: Automated health monitoring for all services

## Services

### Mobile Banking Backend

- **Port**: 8080
- **Health Check**: `/api/actuator/health`
- **Metrics**: `/api/actuator/prometheus`
- **API Documentation**: `/api/swagger-ui.html`

### PostgreSQL Database

- **Port**: 5432 (development only)
- **Database**: mobilebanking
- **User**: mobilebanking
- **Password**: Managed via Docker secrets

## Configuration Files

### Core Files

- `docker-compose.yml`: Main Docker Compose configuration
- `docker-compose.override.yml`: Development-specific overrides
- `docker-compose.prod.yml`: Production-specific configuration

### Environment Files

- `.env.example`: Template for environment variables
- `secrets/db_password.txt`: Database password (Docker secret)
- `secrets/jwt_secret.txt`: JWT signing key (Docker secret)

### Application Configuration

- `src/main/resources/application-docker.yml`: Docker-specific Spring Boot configuration

## Deployment Commands

### Using the Deployment Script

```bash
# Start development environment
./deploy.sh

# Start production environment
./deploy.sh -e prod

# Stop all services
./deploy.sh -a down

# Restart services
./deploy.sh -a restart

# View logs
./deploy.sh -a logs

# Build and start with fresh images
./deploy.sh -b
```

### Manual Docker Compose Commands

```bash
# Development
docker-compose up -d
docker-compose down
docker-compose logs -f

# Production
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
docker-compose -f docker-compose.yml -f docker-compose.prod.yml down

# Build images
docker-compose build
```

## Environment Variables

Key environment variables used in the Docker Compose setup:

| Variable                      | Description            | Default              |
| ----------------------------- | ---------------------- | -------------------- |
| `SPRING_PROFILES_ACTIVE`      | Spring Boot profile    | `docker`             |
| `DB_HOST`                     | Database hostname      | `postgres`           |
| `DB_PORT`                     | Database port          | `5432`               |
| `DB_NAME`                     | Database name          | `mobilebanking`      |
| `DB_USERNAME`                 | Database username      | `mobilebanking`      |
| `JWT_EXPIRATION`              | JWT token expiration   | `86400000`           |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry endpoint | `http://jaeger:4317` |

## Secrets Management

Docker secrets are used for sensitive data:

- **Database Password**: `secrets/db_password.txt`
- **JWT Secret**: `secrets/jwt_secret.txt`

Secrets are mounted as files in containers at `/run/secrets/` and accessed via environment variables.

## Health Checks

All services include comprehensive health checks:

### Application Health Check

- **Endpoint**: `http://localhost:8080/api/actuator/health`
- **Interval**: 30 seconds
- **Timeout**: 10 seconds
- **Retries**: 3

### Database Health Check

- **Command**: `pg_isready -U mobilebanking -d mobilebanking`
- **Interval**: 10 seconds
- **Timeout**: 5 seconds
- **Retries**: 5

## Resource Management

### Development Environment

- **Application**: 1GB memory limit, 0.5 CPU limit
- **Database**: 512MB memory limit, 0.3 CPU limit

### Production Environment

- **Application**: 2GB memory limit, 1.0 CPU limit, 2 replicas
- **Database**: 1GB memory limit, 0.5 CPU limit

## Networking

Services communicate through a custom bridge network (`mobile-banking-network`) with subnet `172.20.0.0/16`.

## Data Persistence

- **PostgreSQL Data**: Stored in Docker volume `postgres_data`
- **Application Logs**: Mounted to `./logs` directory (development)

## Troubleshooting

### Common Issues

1. **Port conflicts**:

   ```bash
   # Check if ports are in use
   netstat -tulpn | grep :8080
   netstat -tulpn | grep :5432
   ```

2. **Permission issues with secrets**:

   ```bash
   # Fix secret file permissions
   chmod 600 secrets/*.txt
   ```

3. **Database connection issues**:

   ```bash
   # Check database logs
   docker-compose logs postgres

   # Test database connectivity
   docker-compose exec postgres psql -U mobilebanking -d mobilebanking -c "SELECT 1;"
   ```

4. **Application startup issues**:

   ```bash
   # Check application logs
   docker-compose logs mobile-banking-backend

   # Check health status
   curl http://localhost:8080/api/actuator/health
   ```

### Useful Commands

```bash
# View service status
docker-compose ps

# Follow logs for all services
docker-compose logs -f

# Follow logs for specific service
docker-compose logs -f mobile-banking-backend

# Execute commands in containers
docker-compose exec mobile-banking-backend bash
docker-compose exec postgres psql -U mobilebanking -d mobilebanking

# Restart specific service
docker-compose restart mobile-banking-backend

# Remove all containers and volumes
docker-compose down -v

# Remove all containers, volumes, and images
docker-compose down -v --rmi all
```

## Security Considerations

1. **Secrets Management**: Sensitive data is managed through Docker secrets
2. **Network Isolation**: Services communicate through isolated Docker network
3. **Non-root User**: Application runs as non-root user in container
4. **Resource Limits**: All services have defined resource limits
5. **Health Checks**: Automated health monitoring prevents unhealthy services

## Next Steps

After successful deployment, you can:

1. **Add Monitoring**: Extend with Prometheus, Grafana, and Jaeger (Tasks 7-9)
2. **Add Logging**: Integrate with Loki for centralized logging (Task 10)
3. **Add Reverse Proxy**: Configure Nginx for load balancing (Task 11)
4. **Scale Services**: Use Docker Compose scaling features (Task 13)

## Support

For issues or questions:

1. Check the troubleshooting section above
2. Review Docker Compose logs
3. Verify all prerequisites are met
4. Ensure all required files are present
