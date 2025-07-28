-- Initialize Mobile Banking Database
-- This script runs when the PostgreSQL container starts for the first time

-- Create additional extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Grant necessary permissions to the application user
GRANT ALL PRIVILEGES ON DATABASE mobilebanking TO mobilebanking;

-- Create schema if it doesn't exist (Flyway will handle table creation)
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO mobilebanking;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO mobilebanking;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO mobilebanking;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO mobilebanking;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO mobilebanking;