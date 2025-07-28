-- Setup monitoring user and permissions for PostgreSQL Exporter
-- This script creates necessary permissions for metrics collection

-- Create monitoring user for PostgreSQL Exporter (if not using main user)
-- Note: We're using the main mobilebanking user for simplicity in Docker Compose
-- In production, you might want a separate monitoring user

-- Grant necessary permissions for metrics collection
GRANT SELECT ON pg_stat_database TO mobilebanking;
GRANT SELECT ON pg_stat_user_tables TO mobilebanking;
GRANT SELECT ON pg_stat_user_indexes TO mobilebanking;
GRANT SELECT ON pg_statio_user_tables TO mobilebanking;
GRANT SELECT ON pg_statio_user_indexes TO mobilebanking;
GRANT SELECT ON pg_stat_activity TO mobilebanking;
GRANT SELECT ON pg_stat_replication TO mobilebanking;
GRANT SELECT ON pg_stat_bgwriter TO mobilebanking;
GRANT SELECT ON pg_stat_archiver TO mobilebanking;

-- Grant access to system views for connection and lock monitoring
GRANT SELECT ON pg_locks TO mobilebanking;
GRANT SELECT ON pg_stat_statements TO mobilebanking;

-- Create a view for easier metrics collection (PostgreSQL 15 compatible)
CREATE OR REPLACE VIEW monitoring.pg_stat_statements_summary AS
SELECT 
    query,
    calls,
    total_exec_time,
    mean_exec_time,
    rows
FROM pg_stat_statements
ORDER BY total_exec_time DESC
LIMIT 100;

-- Grant access to the monitoring schema
CREATE SCHEMA IF NOT EXISTS monitoring;
GRANT USAGE ON SCHEMA monitoring TO mobilebanking;
GRANT SELECT ON ALL TABLES IN SCHEMA monitoring TO mobilebanking;