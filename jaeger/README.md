# Jaeger Distributed Tracing

This directory contains configuration and documentation for Jaeger distributed tracing in the Mobile Banking Backend application.

## Overview

Jaeger is used to collect, store, and visualize distributed traces from the Spring Boot application. This helps with:

- Request flow visualization across services
- Performance bottleneck identification
- Error tracking and debugging
- Service dependency mapping

## Configuration

### Docker Compose Setup

The Jaeger service is configured in `docker-compose.yml` with the following features:

- **All-in-One deployment**: Includes collector, query, and UI in a single container
- **OTLP support**: Accepts traces via OpenTelemetry Protocol (OTLP)
- **Multiple ports**:
  - `16686`: Jaeger UI
  - `4317`: OTLP gRPC receiver
  - `4318`: OTLP HTTP receiver
  - `14250`: gRPC

### Application Configuration

The Spring Boot application is configured with:

- **OpenTelemetry instrumentation**: Automatic tracing for HTTP requests, database calls
- **Custom trace attributes**: Business-specific metadata
- **Sampling**: 10% trace sampling rate (configurable)
- **Propagation**: Support for W3C Trace Context, B3, and Jaeger formats

## Usage

### Starting Jaeger

```bash
docker-compose up jaeger
```

### Accessing Jaeger UI

1. Open browser to http://localhost:16686
2. Select "mobile-banking-backend" service
3. Click "Find Traces" to view recent traces

### Trace Analysis

Key trace information includes:

- **Service operations**: HTTP endpoints, database queries
- **Custom attributes**: User ID, transaction ID, wallet ID
- **Performance metrics**: Duration, error rates
- **Dependencies**: Service call relationships

### Custom Tracing

The application includes custom tracing helpers:

```java
@Autowired
private TracingHelper tracingHelper;

// Add user context to current trace
tracingHelper.addUserContext("user123");

// Add transaction context
tracingHelper.addTransactionContext("txn456", "TRANSFER");

// Add wallet context
tracingHelper.addWalletContext("wallet789");
```

## Monitoring and Alerting

### Grafana Integration

Jaeger is configured as a Grafana datasource for:

- Trace visualization in dashboards
- Correlation with metrics and logs
- Alert rules based on trace data

### Key Metrics to Monitor

- **Trace volume**: Number of traces per minute
- **Error rates**: Percentage of failed traces
- **Latency percentiles**: P50, P95, P99 response times
- **Service dependencies**: Call patterns and failures

## Troubleshooting

### Common Issues

1. **No traces appearing**:

   - Check OTLP endpoint configuration
   - Verify sampling rate (increase for testing)
   - Check application logs for tracing errors

2. **High memory usage**:

   - Reduce sampling rate
   - Configure trace retention policies
   - Monitor Jaeger container resources

3. **Missing trace context**:
   - Verify propagation headers
   - Check custom instrumentation
   - Review trace correlation logs

### Configuration Options

Environment variables for tuning:

```yaml
environment:
  - OTEL_TRACES_SAMPLER_ARG=0.1 # 10% sampling
  - OTEL_PROPAGATORS=tracecontext,baggage,b3
  - COLLECTOR_OTLP_ENABLED=true
```

## Performance Considerations

- **Sampling**: Use appropriate sampling rates for production
- **Resource limits**: Configure memory and CPU limits
- **Storage**: Monitor trace storage growth
- **Network**: Consider trace data volume impact

## Security

- Jaeger UI should be secured in production
- Consider network policies for trace data
- Sensitive data should not be included in trace attributes
