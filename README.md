# Mobile Banking Backend

A production-ready Spring Boot application implementing a mobile banking system using Domain-Driven Design (DDD) principles with comprehensive observability and monitoring capabilities.

## Project Overview

This project demonstrates a complete microservices architecture with full observability stack including metrics, logging, tracing, and monitoring dashboards.

### Architecture

The application follows DDD bounded contexts with a complete observability stack:

#### Bounded Contexts

- **User Context** (`com.mobilebanking.user`) - User management and profiles
- **Auth Context** (`com.mobilebanking.auth`) - Authentication and authorization
- **Transaction Context** (`com.mobilebanking.transaction`) - Money transfers and wallet operations

#### Layer Architecture

- **Domain Layer** - Entities, value objects, domain services
- **Application Layer** - Application services, use cases
- **Infrastructure Layer** - Repositories, external integrations
- **Presentation Layer** - REST controllers, DTOs

#### Shared Kernel

- **Shared Domain** (`com.mobilebanking.shared.domain`) - Common value objects like Money, UserId, TransactionId

## Observability Stack

### 🔍 Monitoring & Metrics

- **Prometheus** - Metrics collection and storage
- **Grafana** - Visualization dashboards and alerting
- **Custom Business Metrics** - User registrations, transactions, wallet operations
- **JVM Metrics** - Memory, GC, thread pools
- **Database Metrics** - PostgreSQL performance monitoring

### 📊 Distributed Tracing

- **Jaeger** - Distributed tracing system
- **OpenTelemetry** - Instrumentation and trace collection
- **Trace Correlation** - Request flow across services
- **Performance Analysis** - Latency and bottleneck identification

### 📝 Centralized Logging

- **Loki** - Log aggregation and storage
- **Promtail** - Log collection agent
- **Structured JSON Logging** - Consistent log format
- **Log Correlation** - Trace ID integration
- **30-day Retention** - Configurable log retention policies

### 🗄️ Database

- **PostgreSQL** - Production database
- **Connection Pooling** - HikariCP configuration
- **Health Checks** - Database connectivity monitoring
- **Backup Strategy** - Automated backup service

## Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 23 (for development)
- Maven (using wrapper)

### Production Deployment

```bash
# Start the complete stack
docker-compose up -d

# Check service health
docker-compose ps

# View logs
docker-compose logs -f mobile-banking-backend
```

### Development Mode

```bash
# Build and run locally
./mvnw clean compile
./mvnw spring-boot:run

# Run tests
./mvnw test

# Package application
./mvnw package
```

## Service Endpoints

### Application

- **API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/actuator/health
- **Metrics**: http://localhost:8080/api/actuator/prometheus

### Observability Stack

- **Grafana Dashboards**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Jaeger Tracing**: http://localhost:16686
- **Loki Logs**: http://localhost:3100

### Database

- **PostgreSQL**: localhost:5432 (mobilebanking/[secret])
- **Database Metrics**: http://localhost:9187/metrics

## Dashboards & Monitoring

### Grafana Dashboards

1. **Spring Boot Dashboard** - Application metrics, HTTP requests, response times
2. **JVM Metrics Dashboard** - Memory usage, garbage collection, thread pools
3. **PostgreSQL Dashboard** - Database performance, connections, queries
4. **Business Metrics Dashboard** - User registrations, transaction volumes
5. **Logs Dashboard** - Centralized log viewing and analysis

### Key Metrics Monitored

- **Application Performance**: Request rates, response times, error rates
- **Business KPIs**: User registrations, transaction success rates, wallet operations
- **Infrastructure**: CPU, memory, disk usage, database connections
- **Security**: Authentication failures, suspicious activities

## Configuration

### Environment Profiles

- **Development**: `application.yml` (H2 database)
- **Docker**: `application-docker.yml` (PostgreSQL, full observability)
- **Test**: `application-test.yml` (Test configurations)

### Key Configuration Files

- `docker-compose.yml` - Complete service orchestration
- `prometheus/prometheus.yml` - Metrics collection configuration
- `loki/loki-config.yml` - Log aggregation settings
- `grafana/provisioning/` - Dashboard and datasource automation

### Security Features

- **Docker Secrets** - Secure credential management
- **Network Isolation** - Service communication via Docker networks
- **Resource Limits** - Container resource constraints
- **Health Checks** - Service availability monitoring

## Development Workflow

### Local Development

```bash
# Start dependencies only
docker-compose up -d postgres prometheus grafana jaeger loki

# Run application locally
./mvnw spring-boot:run -Dspring.profiles.active=docker
```

### Testing

```bash
# Unit tests
./mvnw test

# Integration tests with observability
./mvnw test -Dspring.profiles.active=docker
```

### Monitoring Development

1. Make API calls to generate metrics and traces
2. View real-time dashboards in Grafana
3. Analyze traces in Jaeger UI
4. Search logs in Grafana Explore

## Production Considerations

### Scalability

- **Horizontal Scaling**: Docker Compose scaling support
- **Load Balancing**: Ready for reverse proxy integration
- **Database Scaling**: Connection pooling and monitoring

### Security

- **Secrets Management**: Docker secrets for sensitive data
- **Network Security**: Isolated Docker networks
- **Access Control**: Grafana authentication and authorization

### Reliability

- **Health Checks**: All services have health endpoints
- **Graceful Shutdown**: Proper application lifecycle management
- **Data Persistence**: Docker volumes for data retention
- **Backup Strategy**: Automated database backups

### Performance

- **JVM Optimization**: Container-aware JVM settings
- **Resource Management**: CPU and memory limits
- **Monitoring**: Comprehensive performance metrics

## Troubleshooting

### Common Issues

- **Service Dependencies**: Check health status with `docker-compose ps`
- **Network Connectivity**: Verify service communication
- **Resource Constraints**: Monitor CPU/memory usage in Grafana
- **Log Analysis**: Use Loki queries for debugging

### Useful Commands

```bash
# View service logs
docker-compose logs -f [service-name]

# Check service health
curl http://localhost:8080/api/actuator/health

# Restart specific service
docker-compose restart [service-name]

# Scale application
docker-compose up -d --scale mobile-banking-backend=3
```

## Documentation

- **Grafana Setup**: `grafana/README.md`
- **Loki Configuration**: `loki/README.md`
- **Jaeger Implementation**: `JAEGER_IMPLEMENTATION_SUMMARY.md`
- **Task Progress**: `.kiro/specs/k8s-observability-deployment/tasks.md`

## Contributing

1. Follow DDD principles for domain modeling
2. Add appropriate metrics for new features
3. Include tracing for service interactions
4. Update dashboards for new metrics
5. Maintain comprehensive logging

This project serves as a reference implementation for building observable, production-ready Spring Boot applications with comprehensive monitoring and observability capabilities.
