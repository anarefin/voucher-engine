# Voucher Engine

A Spring Boot application for managing vouchers with RabbitMQ integration for bulk processing.

## Features

- Create, read, update, and delete vouchers
- Support for different voucher types (Debit, Credit)
- Bulk voucher generation and processing
- RabbitMQ integration for asynchronous processing
- Batch processing for improved performance

## Bulk Voucher Processing

The application supports efficient bulk processing of vouchers using RabbitMQ:

### Bulk Voucher Producer

The `BulkDebitVoucherProducer` service can generate and publish a large number of vouchers to RabbitMQ:

- Supports parallel processing with configurable thread count
- Uses batching for improved performance
- Generates random voucher data for testing purposes

### Bulk Voucher Consumer

The `BulkDebitVoucherConsumer` service efficiently processes vouchers from RabbitMQ:

- Supports both individual and batch message consumption
- Processes messages in parallel using a thread pool
- Provides detailed statistics and error reporting
- Can be controlled via REST API endpoints

## API Endpoints

### Bulk Voucher Operations

- `POST /api/bulk-vouchers/generate?count=1000&batchSize=100&threadCount=4` - Generate and publish vouchers
- `GET /api/bulk-vouchers/consumer/stats` - Get consumer processing statistics
- `POST /api/bulk-vouchers/consumer/reset-stats` - Reset consumer statistics
- `POST /api/bulk-vouchers/consumer/shutdown` - Shutdown the consumer
- `POST /api/bulk-vouchers/consumer/init` - Initialize the consumer

## Configuration

Key configuration properties in `application.properties`:

```properties
# RabbitMQ Template Configuration
spring.rabbitmq.template.batch-size=100
spring.rabbitmq.template.receive-timeout=30000
spring.rabbitmq.template.retry.enabled=true
spring.rabbitmq.template.retry.initial-interval=1000
spring.rabbitmq.template.retry.max-attempts=3
spring.rabbitmq.template.retry.multiplier=1.0

# Bulk Voucher Producer Configuration
app.bulk-generation.use-batching=true

# Bulk Voucher Consumer Configuration
app.bulk-consumption.thread-count=4
app.bulk-consumption.batch-size=100
app.bulk-consumption.report-interval=1000
app.bulk-consumption.listener-concurrency=5
```

## Getting Started

1. Ensure you have PostgreSQL and RabbitMQ running
2. Configure the database and RabbitMQ connection in `application.properties`
3. Run the application: `./mvnw spring-boot:run`
4. Access the API at `http://localhost:8181`

## Performance Considerations

- For optimal performance, adjust the thread count and batch size based on your hardware
- Monitor the RabbitMQ queue size to ensure it doesn't grow too large
- Consider using a dedicated database connection pool for bulk operations
- Adjust the JVM heap size if processing very large batches 