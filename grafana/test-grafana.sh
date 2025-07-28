#!/bin/bash

# Test script for Grafana setup
echo "Testing Grafana configuration..."

# Check if Grafana is running
echo "1. Checking if Grafana is accessible..."
curl -f http://localhost:3000/api/health || echo "Grafana health check failed"

# Check if Prometheus data source is configured
echo "2. Checking Prometheus data source..."
curl -u admin:admin http://localhost:3000/api/datasources | grep -q "prometheus" && echo "Prometheus data source found" || echo "Prometheus data source not found"

# Check if dashboards are loaded
echo "3. Checking if dashboards are loaded..."
curl -u admin:admin http://localhost:3000/api/search | grep -q "Mobile Banking" && echo "Dashboards loaded successfully" || echo "Dashboards not found"

# Test Prometheus connectivity from Grafana
echo "4. Testing Prometheus connectivity..."
curl -u admin:admin -X POST http://localhost:3000/api/datasources/proxy/1/api/v1/query?query=up | grep -q "success" && echo "Prometheus connectivity OK" || echo "Prometheus connectivity failed"

echo "Grafana test completed!"