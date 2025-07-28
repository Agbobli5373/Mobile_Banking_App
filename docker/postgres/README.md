# PostgreSQL Configuration for Mobile Banking Backend

This directory contains the PostgreSQL database configuration for the mobile banking backend application deployed with Docker Compose.

## Components

### Database Service

- **Image**: postgres:15-alpine
- **Container**: postgres-db
- **Port**: 5432
- **Database**: mobilebanking
- **User**: mobilebanking

### PostgreSQL Exporter

- **Image**: prometheuscommunity/postgres-exporter:latest
- **Container**: postgres-exporter
- **Port**: 9187
- **Purpose**: Exports PostgreSQL metrics for Prometheus monitoring

### Backup Service

- **Image**: postgres:15-alpine
- **Container**: postgres-backup
- **Purpose**: Creates automated database backups
- **Profile**: backup (optional service)

## Directory Structure

```
docker/postgres/
├── README.md                 # This file
├── backup.sh                 # Database backup script
├── conf/
│   └── postgresql.conf       # PostgreSQL configuration
└── init/
    ├── 01-init.sql          # Database initialization
    └── 02-monitoring-setup.sql # Monitoring setup
```

## Configuration Files

### postgresql.conf

- Optimized for container environment
- Enables pg_stat_statements for monitoring
- Configured for observability and performance

### Initialization Scripts

- **01-init.sql**: Creates extensions, sets timezone, grants permissions
- **02-monitoring-setup.sql**: Sets up monitoring permissions and views

## Secrets Management

The following secrets are used:

- `db_password`: Database password (from secrets/db_password.txt)
- `pgpass`: PostgreSQL password file for exporter (from secrets/pgpass)

## Volumes

- `postgres_data`: Persistent storage for database data
- `postgres_backups`: Storage for database backups

## Health Checks

### Database Health Check

```bash
pg_isready -U mobilebanking -d mobilebanking
```

### Exporter Health Check

```bash
wget --no-verbose --tries=1 --spider http://localhost:9187/metrics
```

## Monitoring

### Metrics Available

- Database connection statistics
- Query performance metrics
- Table and index statistics
- Background writer statistics
- Replication statistics (if applicable)

### Prometheus Metrics Endpoint

- URL: http://localhost:9187/metrics
- Scrape interval: 15s (recommended)

## Backup and Recovery

### Manual Backup

```bash
# Run backup service
docker-compose --profile backup run --rm postgres-backup

# Or run backup script directly
docker-compose exec postgres pg_dump -U mobilebanking mobilebanking > backup.sql
```

### Automated Backup

The backup service can be scheduled using cron or external orchestration tools.

### Restore from Backup

```bash
# Restore from backup file
docker-compose exec -T postgres psql -U mobilebanking -d mobilebanking < backup.sql
```

## Performance Tuning

### Connection Pool Settings

- Max connections: 100
- Shared buffers: 128MB
- Effective cache size: 256MB

### Monitoring Queries

```sql
-- Top slow queries
SELECT query, calls, total_time, mean_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;

-- Database size
SELECT pg_size_pretty(pg_database_size('mobilebanking'));

-- Active connections
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';
```

## Troubleshooting

### Common Issues

1. **Connection refused**

   - Check if postgres service is healthy
   - Verify network connectivity
   - Check credentials in secrets

2. **Exporter not working**

   - Verify pgpass file format
   - Check exporter logs: `docker-compose logs postgres-exporter`
   - Ensure monitoring permissions are granted

3. **Performance issues**
   - Check pg_stat_statements for slow queries
   - Monitor connection pool usage
   - Review PostgreSQL logs

### Useful Commands

```bash
# Check database logs
docker-compose logs postgres

# Check exporter metrics
curl http://localhost:9187/metrics

# Connect to database
docker-compose exec postgres psql -U mobilebanking -d mobilebanking

# Check database size and statistics
docker-compose exec postgres psql -U mobilebanking -d mobilebanking -c "\l+"
```

## Security Considerations

- Database runs as non-root user
- Secrets are managed through Docker secrets
- Network isolation through Docker networks
- Authentication using SCRAM-SHA-256
- Monitoring user has read-only permissions
