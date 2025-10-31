# Semantic Search Service

A Java 24 Spring Boot application that provides semantic search capabilities for code chunks using vector embeddings and PostgreSQL with pgvector extension.

## Features

- 🔍 **Semantic Code Search**: Search code using natural language queries
- 📦 **Batch Indexing**: Index multiple code chunks efficiently
- 🎯 **Filtered Search**: Filter by language, repository, file path
- 🚀 **High Performance**: Uses HNSW indexing for fast similarity search
- 📊 **RESTful API**: Easy-to-use REST endpoints
- 🔧 **Configurable**: Flexible configuration via application.yml

## Technology Stack

- **Java 24**: Latest Java features
- **Spring Boot 3.3.0**: Modern Spring framework
- **Spring AI**: AI integration framework
- **PostgreSQL + pgvector**: Vector database storage
- **Gradle**: Build automation
- **Lombok**: Reduce boilerplate code

## Prerequisites

- Java 24 or higher
- PostgreSQL 14+ with pgvector extension
- Gradle 8.x

## Database Setup

1. Install PostgreSQL and the pgvector extension:

```bash
# On macOS with Homebrew
brew install postgresql@14
brew install pgvector

# On Ubuntu/Debian
sudo apt-get install postgresql-14 postgresql-14-pgvector
```

2. Create the database:

```sql
CREATE DATABASE vectordb;
\c vectordb
CREATE EXTENSION vector;
```

3. Update database credentials in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectordb
    username: your_username
    password: your_password
```

## Building the Application

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create executable JAR
./gradlew bootJar
```

## Running the Application

```bash
# Run with Gradle
./gradlew bootRun

# Or run the JAR directly
java -jar build/libs/semantic-search-service-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## API Endpoints

### Health Check

```bash
GET /api/v1/vector-store/health
```

### Index a Code Chunk

```bash
POST /api/v1/vector-store/index
Content-Type: application/json

{
  "content": "public class Example { ... }",
  "filePath": "src/main/java/Example.java",
  "language": "java",
  "startLine": 1,
  "endLine": 10,
  "repository": "my-repo",
  "branch": "main",
  "description": "Example class implementation"
}
```

### Batch Index Code Chunks

```bash
POST /api/v1/vector-store/index/batch
Content-Type: application/json

[
  {
    "content": "function example() { ... }",
    "filePath": "src/example.js",
    "language": "javascript",
    ...
  },
  ...
]
```

### Search Code

```bash
POST /api/v1/vector-store/search
Content-Type: application/json

{
  "query": "function to calculate fibonacci numbers",
  "topK": 10,
  "threshold": 0.7,
  "language": "java",
  "repository": "my-repo",
  "includeContent": true
}
```

**Response:**

```json
{
  "results": [
    {
      "id": "uuid",
      "content": "...",
      "filePath": "...",
      "language": "java",
      "score": 0.95,
      ...
    }
  ],
  "totalResults": 5,
  "query": "function to calculate fibonacci numbers",
  "executionTimeMs": 45,
  "timestamp": "2024-01-01T12:00:00",
  "filtered": true,
  "appliedFilters": {
    "language": "java",
    "threshold": 0.7,
    "topK": 10
  }
}
```

### Delete Code Chunk

```bash
DELETE /api/v1/vector-store/index/{id}
```

### Delete All Code Chunks

```bash
DELETE /api/v1/vector-store/index
```

### Get Statistics

```bash
GET /api/v1/vector-store/stats
```

## Configuration

Key configuration options in `application.yml`:

```yaml
spring:
  ai:
    vectorstore:
      pgvector:
        dimensions: 384              # Vector dimensions
        distance-type: COSINE_DISTANCE
        index-type: HNSW            # HNSW or IVFFLAT
    embedding:
      transformer:
        model-name: sentence-transformers/all-MiniLM-L6-v2

app:
  vector-store:
    max-batch-size: 100
    default-top-k: 10
    default-threshold: 0.7
```

## Project Structure

```
semantic-search-service/
├── build.gradle.kts
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── vectordb/
│       │               ├── VectorDbApplication.java
│       │               ├── config/
│       │               │   └── VectorStoreConfig.java
│       │               ├── controller/
│       │               │   └── VectorStoreController.java
│       │               ├── service/
│       │               │   └── VectorStoreService.java
│       │               ├── model/
│       │               │   └── CodeChunk.java
│       │               └── dto/
│       │                   ├── SearchRequest.java
│       │                   └── SearchResponse.java
│       └── resources/
│           └── application.yml
└── README.md
```

## Usage Examples

### Example 1: Index Java Code

```bash
curl -X POST http://localhost:8080/api/v1/vector-store/index \
  -H "Content-Type: application/json" \
  -d '{
    "content": "public int fibonacci(int n) { if (n <= 1) return n; return fibonacci(n-1) + fibonacci(n-2); }",
    "filePath": "src/main/java/Utils.java",
    "language": "java",
    "startLine": 15,
    "endLine": 20,
    "repository": "algorithms",
    "branch": "main"
  }'
```

### Example 2: Search for Similar Code

```bash
curl -X POST http://localhost:8080/api/v1/vector-store/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "recursive function to calculate fibonacci",
    "topK": 5,
    "threshold": 0.75,
    "language": "java"
  }'
```

## Performance Tuning

### HNSW Index Parameters

- **m**: Number of connections per layer (default: 16)
  - Higher values = better recall, more memory
- **ef_construction**: Size of dynamic candidate list (default: 200)
  - Higher values = better index quality, slower indexing

### Distance Metrics

- **COSINE_DISTANCE**: Best for normalized vectors (recommended)
- **EUCLIDEAN_DISTANCE**: L2 distance
- **NEGATIVE_INNER_PRODUCT**: For maximum inner product search

## Monitoring

The application exposes Actuator endpoints:

- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Troubleshooting

### Common Issues

1. **pgvector extension not found**
   ```sql
   CREATE EXTENSION vector;
   ```

2. **Out of memory errors**
   - Reduce `dimensions` in configuration
   - Adjust JVM heap size: `-Xmx4g`

3. **Slow search performance**
   - Increase HNSW `m` parameter
   - Use HNSW instead of IVFFLAT
   - Add more specific filters

## Development

### Running Tests

```bash
./gradlew test
```

### Code Style

The project uses:
- Lombok for reducing boilerplate
- SLF4J for logging
- Jakarta validation for input validation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue in the repository
- Check existing documentation
- Review Spring AI documentation

## Roadmap

- [ ] Add support for more embedding models
- [ ] Implement caching layer
- [ ] Add batch delete operations
- [ ] Support for incremental updates
- [ ] Add GraphQL API
- [ ] Implement rate limiting
- [ ] Add authentication/authorization

## Acknowledgments

- Spring AI Team
- PostgreSQL pgvector extension
- HuggingFace for transformer models