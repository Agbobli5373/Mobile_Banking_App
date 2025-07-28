#!/bin/bash

# Test script to verify Prometheus monitoring setup
echo "Testing Prometheus Monitoring Setup..."
echo "======================================"

# Test Prometheus health
echo "1. Testing Prometheus health..."
if curl -s http://localhost:9090/-/healthy | grep -q "Healthy"; then
    echo "✓ Prometheus is healthy"
else
    echo "✗ Prometheus health check failed"
    exit 1
fi

# Test PostgreSQL Exporter
echo "2. Testing PostgreSQL Exporter..."
if curl -s http://localhost:9187/metrics | grep -q "pg_up 1"; then
    echo "✓ PostgreSQL Exporter is working"
else
    echo "✗ PostgreSQL Exporter failed"
    exit 1
fi

# Test Prometheus targets
echo "3. Testing Prometheus targets..."
TARGETS=$(curl -s http://localhost:9090/api/v1/targets | grep -o '"health":"up"' | wc -l)
echo "   Active targets: $TARGETS"

# Test specific metrics
echo "4. Testing key metrics..."

# PostgreSQL metrics
PG_UP=$(curl -s "http://localhost:9090/api/v1/query?query=pg_up" | grep -o '"value":\[.*,"1"\]')
if [ ! -z "$PG_UP" ]; then
    echo "✓ PostgreSQL up metric available"
else
    echo "✗ PostgreSQL up metric not found"
fi

# Database connections
DB_CONN=$(curl -s "http://localhost:9090/api/v1/query?query=pg_stat_database_numbackends" | grep -o '"result":\[.*\]')
if [ ! -z "$DB_CONN" ]; then
    echo "✓ Database connection metrics available"
else
    echo "✗ Database connection metrics not found"
fi

# Test alert rules
echo "5. Testing alert rules..."
RULES=$(curl -s http://localhost:9090/api/v1/rules | grep -o '"name":"[^"]*"' | wc -l)
echo "   Loaded alert rules: $RULES"

if [ $RULES -gt 0 ]; then
    echo "✓ Alert rules loaded successfully"
else
    echo "✗ No alert rules found"
fi

echo ""
echo "Prometheus monitoring setup test completed!"
echo "Access Prometheus UI at: http://localhost:9090"
echo "Access PostgreSQL metrics at: http://localhost:9187/metrics"