# Nexo Search Engine

A robust, high-performance open-source search engine built with Java, Netty, and Apache Lucene. Nexo provides Elasticsearch-like REST API capabilities with focus on simplicity and performance.

## Features

- **High Performance**: Built on Netty for non-blocking I/O and optimized for high concurrency
- **Full-Text Search**: Powered by Apache Lucene for advanced search capabilities
- **REST API**: Elasticsearch-compatible HTTP API for easy integration
- **Configurable**: YAML-based configuration with sensible defaults
- **Robust**: Production-ready with comprehensive logging and error handling
- **Lightweight**: Minimal dependencies and resource footprint

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Building

```bash
mvn clean compile
```

### Running

```bash
# Using Maven
mvn exec:java

# Or using the JAR
mvn package
java -jar target/nexo-1.0.0-SNAPSHOT.jar

# With custom configuration
java -jar target/nexo-1.0.0-SNAPSHOT.jar config/nexo.yml
```

### Basic Usage

#### 1. Health Check

```bash
curl http://localhost:9090/
```

Response:
```json
{
  "status": "ok",
  "version": "1.0.0",
  "timestamp": 1699123456789
}
```

#### 2. Index a Document

```bash
curl -X POST http://localhost:9090/_index \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Getting Started with Nexo",
    "content": "Nexo is a powerful search engine built with Java and Netty",
    "author": "Nexo Team",
    "tags": ["search", "java", "netty"]
  }'
```

Response:
```json
{
  "indexed": true,
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "timestamp": 1699123456789
}
```

#### 3. Search Documents

```bash
curl -X POST http://localhost:9090/_search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "search engine",
    "size": 10,
    "from": 0
  }'
```

Response:
```json
{
  "hits": [
    {
      "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "score": 1.2345,
      "source": {
        "title": "Getting Started with Nexo",
        "content": "Nexo is a powerful search engine built with Java and Netty",
        "author": "Nexo Team"
      }
    }
  ],
  "totalHits": 1,
  "took": 5
}
```

#### 4. Get Statistics

```bash
curl http://localhost:9090/_stats
```

Response:
```json
{
  "totalDocuments": 1,
  "indexedDocuments": 1,
  "indexPath": "data/index",
  "timestamp": 1699123456789
}
```

## Configuration

Edit `config/nexo.yml` to customize settings:

```yaml
serverHost: "0.0.0.0"
serverPort: 9090
workerThreads: 4
searchThreads: 4
maxContentLength: 1048576  # 1MB
indexPath: "data/index"
clusterName: "nexo-cluster"
nodeName: "nexo-node-1"
```

## API Reference

### Endpoints

- `GET /` - Health check
- `POST /_index` - Index a document
- `POST /_search` - Search documents
- `GET /_stats` - Get cluster statistics

### Search Query Parameters

- `query` (string): Lucene query syntax
- `size` (int): Number of results to return (default: 10, max: 10000)
- `from` (int): Starting offset (default: 0)
- `fields` (array): Fields to include in response (default: all)

## Development

### Running Tests

```bash
mvn test
```

### Building Distribution

```bash
mvn package
```

## Architecture

Nexo is built with a modular architecture:

- **Server Layer**: Netty-based HTTP server with async request handling
- **Core Engine**: Lucene-powered search and indexing
- **Configuration**: YAML-based configuration management
- **Utilities**: Logging, error handling, and monitoring

## Performance

Nexo is optimized for:

- High concurrent connections via Netty's event loop
- Fast indexing with configurable commit strategies
- Efficient memory usage with Lucene's index management
- Low latency search with connection pooling

## License

Apache License 2.0

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Support

- GitHub Issues: https://github.com/nexo/nexo/issues
- Documentation: https://github.com/nexo/nexo/wiki