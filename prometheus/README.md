# Prometheus Monitoring Setup

This directory contains the Prometheus configuration for monitoring the Mobile Banking Backend application.

## Components

### Prometheus Server

- **Port**: 9090
- **Configuration**: `prometheus.yml`
- **Data Retention**: 30 days
- **Storage**: Docker volume `prometheus_data`

### Monitored Services

1. **Mobile Banking Backend** (`mobile-banking-backend:8080`)

   - Metrics endpoint: `/api/actuator/prometheus`
   - Scrape interval: 30 seconds
   - Includes JVM metrics, HTTP request metrics, and custom business metrics

2. **PostgreSQL Database** (`postgres-exporter:9187`)

   - Metrics endpoint: `/metrics`
   - Scrape interval: 30 seconds
   - Includes database connection, query performance, and system metrics

3. **Prometheus Self-Monitoring** (`localhost:9090`)
   - Self-monitoring metrics
   - Scrape interval: 30 seconds

## Alert Rules

The `alert_rules.yml` file contains predefined alert rules for:

### Application Alerts

- **ApplicationDown**: Triggers when the backend is unreachable
- **HighErrorRate**: Triggers when 5xx error rate exceeds 0.1/sec
- **HighResponseTime**: Triggers when 95th percentile response time > 2 seconds
- **HighMemoryUsage**: Triggers when JVM heap usage > 85%
- **HighGCTime**: Triggers when GC time > 0.1 seconds per second

### Database Alerts

- **PostgreSQLDown**: Triggers when PostgreSQL is unreachable
- **HighDatabaseConnections**: Triggers when connection usage > 80%
- **DatabaseSlowQueries**: Triggers when query efficiency is low
- **DatabaseHighIOWait**: Triggers when I/O wait time is high

## Usage

### Starting Prometheus

```bash
docker-compose up prometheus
```

### Accessing Prometheus UI

- URL: http://localhost:9090
- Query interface available for custom metrics exploration
- Alert rules status visible in the Alerts section

### Key Metrics to Monitor

#### Application Metrics

- `http_server_requests_seconds_count`: HTTP request count
- `http_server_requests_seconds_sum`: HTTP request duration
- `jvm_memory_used_bytes`: JVM memory usage
- `jvm_gc_pause_seconds`: Garbage collection metrics
- `custom_user_registrations_total`: Custom business metric
- `custom_transactions_total`: Custom business metric

#### Database Metrics

- `pg_up`: PostgreSQL availability
- `pg_stat_database_numbackends`: Active connections
- `pg_stat_database_tup_returned`: Rows returned by queries
- `pg_stat_database_tup_fetched`: Rows fetched by queries
- `pg_stat_database_blk_read_time`: Block read time

### Configuration Updates

To reload Prometheus configuration without restart:

```bash
curl -X POST http://localhost:9090/-/reload
```

### Troubleshooting

1. **Service Discovery Issues**

   - Verify service names match Docker Compose service names
   - Check network connectivity between services

2. **Metrics Not Available**

   - Verify application exposes metrics at `/api/actuator/prometheus`
   - Check PostgreSQL exporter is running and accessible

3. **Alert Rules Not Working**
   - Validate YAML syntax in `alert_rules.yml`
   - Check Prometheus logs for rule evaluation errors

## Integration with Grafana

This Prometheus setup is designed to work with Grafana dashboards. The metrics collected here will be used by Grafana for visualization and additional alerting capabilities.
