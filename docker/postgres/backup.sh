#!/bin/bash
# PostgreSQL Backup Script for Docker Compose Deployment
# This script creates backups of the mobile banking database

set -e

# Configuration
BACKUP_DIR="/backups"
DB_NAME="mobilebanking"
DB_USER="mobilebanking"
DB_HOST="postgres"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/mobilebanking_backup_${TIMESTAMP}.sql"

# Create backup directory if it doesn't exist
mkdir -p "${BACKUP_DIR}"

# Create database backup
echo "Starting database backup at $(date)"
pg_dump -h "${DB_HOST}" -U "${DB_USER}" -d "${DB_NAME}" \
    --verbose \
    --clean \
    --if-exists \
    --create \
    --format=plain \
    --file="${BACKUP_FILE}"

# Compress the backup
gzip "${BACKUP_FILE}"
echo "Backup completed: ${BACKUP_FILE}.gz"

# Clean up old backups (keep last 7 days)
find "${BACKUP_DIR}" -name "mobilebanking_backup_*.sql.gz" -mtime +7 -delete
echo "Old backups cleaned up"

echo "Database backup completed successfully at $(date)"