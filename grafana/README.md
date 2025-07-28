# Grafana Configuration

This directory contains the Grafana configuration for the Mobile Banking application observability stack.

## Directory Structure

```
grafana/
├── provisioning/
│   ├── datasources/
│   │   └── prometheus.yml          # Prometheus data source configuration
│   └── dashboards/
│       └── dashboards.yml          # Dashboard provisioning configuration
├── dashboards/
│   ├── spring-boot-dashboard.json  # Spring Boot application metrics
│   ├── jvm-metrics-dashboard.json  # JVM performance metrics
│   ├── postgresql-dashboard.json   # PostgreSQL database metrics
│   └── business-metrics-dashboard.json # Business-specific metrics
└── README.md
```

## Features

### Automatic Data Source Configuration

- Prometheus data source is automatically configured on startup
- No manual configuration required
- Connection to Prometheus service at `http://prometheus:9090`

### Pre-built Dashboards

1. **Spring Boot Dashboard**: HTTP request rates, application status
2. **JVM Metrics Dashboard**: Memory usage, garbage collection
3. **PostgreSQL Dashboard**: Database status, active connections
4. **Business Metrics Dashboard**: User registrations, transaction rates

### Access Information

- **URL**: http://localhost:3000
- **Username**: admin
- **Password**: admin
- **Default refresh**: 30 seconds

## Dashboard Details

### Spring Boot Metrics

- HTTP request rate by method, URI, and status
- Application health status gauge
- Response time percentiles

### JVM Metrics

- Heap and non-heap memory usage
- Garbage collection metrics
- Thread pool statistics

### PostgreSQL Metrics

- Database connection status
- Active connection count
- Query performance metrics

### Business Metrics

- User registration rate
- Transaction volume by type
- Wallet operation timing

## Customization

### Adding New Dashboards

1. Create JSON dashboard file in `grafana/dashboards/`
2. Restart Grafana service to load new dashboard
3. Dashboard will be automatically provisioned

### Modifying Data Sources

1. Edit `grafana/provisioning/datasources/prometheus.yml`
2. Restart Grafana service to apply changes

### Environment Variables

- `GF_SECURITY_ADMIN_USER`: Admin username (default: admin)
- `GF_SECURITY_ADMIN_PASSWORD`: Admin password (default: admin)
- `GF_USERS_ALLOW_SIGN_UP`: Allow user registration (default: false)

## Troubleshooting

### Dashboard Not Loading

- Check Prometheus connectivity: `http://prometheus:9090`
- Verify dashboard JSON syntax
- Check Grafana logs: `docker logs grafana`

### Data Source Issues

- Verify Prometheus service is running
- Check network connectivity between services
- Validate Prometheus configuration

### Performance Issues

- Adjust query intervals in dashboards
- Reduce data retention period
- Monitor Grafana resource usage
