# Loki Centralized Logging Setup

This directory contains the configuration for Loki centralized logging system integrated with the mobile banking backend application.

## Components

### Loki

- **Purpose**: Log aggregation and storage system
- **Port**: 3100
- **Configuration**: `loki-config.yml`
- **Data retention**: 30 days
- **Storage**: Local filesystem with Docker volume

### Promtail

- **Purpose**: Log collection agent that scrapes logs from Docker containers
- **Port**: 9080 (health check)
- **Configuration**: `promtail-config.yml`
- **Targets**: All Docker containers in the mobile-banking-network

## Features

### Log Collection

- **Application Logs**: Structured JSON logs from mobile-banking-backend
- **Database Logs**: PostgreSQL logs with query information
- **Infrastructure Logs**: Prometheus, Grafana, and Jaeger logs
- **Container Logs**: All Docker container stdout/stderr

### Log Processing

- **JSON Parsing**: Automatic parsing of structured JSON logs
- **Label Extraction**: Container name, log level, trace ID, span ID
- **Timestamp Parsing**: Proper timestamp handling for different log formats
- **Log Correlation**: Trace ID correlation between logs and traces

### Retention and Storage

- **Retention Period**: 30 days (configurable in loki-config.yml)
- **Compression**: Snappy compression for efficient storage
- **Indexing**: BoltDB shipper for log indexing
- **Cleanup**: Automatic cleanup of old logs

## Configuration Details

### Loki Configuration (`loki-config.yml`)

```yaml
# Key settings
retention_period: 30d
ingestion_rate_mb: 16
per_stream_rate_limit: 3MB
max_streams_per_user: 10000
```

### Promtail Configuration (`promtail-config.yml`)

- **Docker Service Discovery**: Automatically discovers containers
- **Label-based Filtering**: Uses Docker labels for log routing
- **Pipeline Stages**: JSON parsing, timestamp extraction, label assignment

## Integration with Grafana

### Data Source

- Loki is automatically configured as a data source in Grafana
- URL: `http://loki:3100`
- Trace correlation with Jaeger enabled

### Dashboard

- **Logs Dashboard**: Pre-configured dashboard for log visualization
- **Log Levels**: Distribution and trends of log levels
- **Container Logs**: Separate views for application and database logs
- **Search and Filter**: Full-text search and label-based filtering

## Usage

### Viewing Logs

1. Access Grafana at `http://localhost:3000`
2. Navigate to the "Mobile Banking Logs Dashboard"
3. Use the Explore feature for ad-hoc log queries

### Log Queries

```logql
# Application logs
{container_name="mobile-banking-backend"}

# Error logs only
{container_name="mobile-banking-backend"} |= "ERROR"

# Logs with specific trace ID
{container_name="mobile-banking-backend"} |= "traceId=abc123"

# Database logs
{container_name="postgres-db"}

# Log rate by level
sum by (level) (rate({container_name="mobile-banking-backend"}[5m]))
```

### Troubleshooting

#### Loki Not Receiving Logs

1. Check Promtail health: `curl http://localhost:9080/ready`
2. Verify Docker socket access in Promtail container
3. Check Promtail logs: `docker logs promtail`

#### High Memory Usage

1. Adjust `ingestion_rate_mb` in loki-config.yml
2. Reduce `retention_period` if needed
3. Monitor with Grafana dashboards

#### Log Parsing Issues

1. Verify JSON format in application logs
2. Check pipeline stages in promtail-config.yml
3. Test regex patterns with sample logs

## Security Considerations

### Access Control

- Loki has no built-in authentication (suitable for internal use)
- Access controlled through Docker network isolation
- Consider adding reverse proxy with authentication for production

### Data Privacy

- Logs may contain sensitive information
- Configure log filtering to exclude sensitive data
- Implement proper log retention policies

### Resource Limits

- Memory limits configured in Docker Compose
- Disk space monitoring recommended
- Log rotation configured to prevent disk exhaustion

## Monitoring

### Health Checks

- Loki: `http://localhost:3100/ready`
- Promtail: `http://localhost:9080/ready`

### Metrics

- Loki exposes Prometheus metrics on `/metrics`
- Monitor ingestion rate, storage usage, and query performance
- Alerts configured for high error rates and resource usage

## Backup and Recovery

### Data Location

- Loki data stored in Docker volume: `loki_data`
- Configuration files in `./loki/` directory

### Backup Strategy

```bash
# Backup Loki data
docker run --rm -v mobile_banking_app_loki_data:/data -v $(pwd):/backup alpine tar czf /backup/loki-backup.tar.gz -C /data .

# Restore Loki data
docker run --rm -v mobile_banking_app_loki_data:/data -v $(pwd):/backup alpine tar xzf /backup/loki-backup.tar.gz -C /data
```

## Performance Tuning

### Ingestion Performance

- Adjust `ingestion_rate_mb` based on log volume
- Configure `per_stream_rate_limit` for high-volume applications
- Use appropriate `chunk_target_size` for storage efficiency

### Query Performance

- Use label-based filtering for better performance
- Avoid full-text search on large time ranges
- Configure appropriate `max_look_back_period`

### Storage Optimization

- Enable compression with `chunk_encoding: snappy`
- Configure appropriate `chunk_idle_period`
- Monitor storage usage and adjust retention policies
