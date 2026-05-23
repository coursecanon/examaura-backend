# QuizMaster Backend - Complete Implementation Guide

## 📚 Overview

This documentation package provides a comprehensive, production-ready backend architecture for the **QuizMaster** quiz application. The backend is designed using **Spring Boot 3.2+**, **PostgreSQL**, **OAuth2/JWT**, and **Apache Kafka** to support the React/TypeScript frontend.

---

## 📦 Package Contents

This documentation package includes:

| File | Description |
|------|-------------|
| `00-BACKEND_ARCHITECTURE_OVERVIEW.md` | High-level architecture, tech stack, project structure, and deployment guide |
| `01-DATABASE_SCHEMA.sql` | Complete PostgreSQL schema with tables, indexes, views, triggers, and sample data |
| `02-ENTITY_CLASSES.java` | JPA entity classes with relationships, enums, and configurations |
| `03-API_ENDPOINTS.md` | Detailed REST API specification with request/response examples |
| `04-SECURITY_CONFIGURATION.md` | OAuth2 + JWT implementation with Spring Security |
| `05-KAFKA_INTEGRATION.md` | Event-driven architecture with Kafka producers and consumers |
| `06-TYPESCRIPT_TO_JAVA_MAPPING.md` | Frontend-backend data model consistency guide |
| `07-POM_XML.xml` | Complete Maven pom.xml with all dependencies |
| `README.md` | This file - Getting started guide |

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+** (JDK 17 or higher)
- **Maven 3.8+**
- **PostgreSQL 15+**
- **Docker & Docker Compose** (for local development)
- **Git**

### Step 1: Create Spring Boot Project

```bash
# Clone or create new Spring Boot project
mkdir quiz-master-backend
cd quiz-master-backend

# Copy the pom.xml from 07-POM_XML.xml
cp path/to/07-POM_XML.xml pom.xml

# Create directory structure
mkdir -p src/main/java/com/quizmaster
mkdir -p src/main/resources/db/migration
mkdir -p src/test/java/com/quizmaster
```

### Step 2: Setup Database

```bash
# Start PostgreSQL using Docker
docker run -d \
  --name quizmaster-postgres \
  -e POSTGRES_DB=quizmaster \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15-alpine

# Wait for PostgreSQL to start
sleep 5

# Create database schema
docker exec -i quizmaster-postgres psql -U postgres -d quizmaster < 01-DATABASE_SCHEMA.sql
```

Alternatively, use Flyway migrations:
```bash
# Copy schema to Flyway migration
cp 01-DATABASE_SCHEMA.sql src/main/resources/db/migration/V1__initial_schema.sql

# Run Flyway migration
mvn flyway:migrate
```

### Step 3: Configure Application

Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: quiz-master-backend
  
  datasource:
    url: jdbc:postgresql://localhost:5432/quizmaster
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: false
  
  flyway:
    enabled: true
    locations: classpath:db/migration
  
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email,profile
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: user:email,read:user

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production}
  access-token-expiration: 3600000
  refresh-token-expiration: 604800000
  issuer: quizmaster-api

server:
  port: 8080
  servlet:
    context-path: /api/v1

logging:
  level:
    com.quizmaster: DEBUG
    org.springframework.security: DEBUG
```

### Step 4: Set Environment Variables

Create `.env` file:

```bash
# OAuth2 Providers
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# JWT
JWT_SECRET=your-very-long-secret-key-at-least-256-bits-for-hs256-algorithm

# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/quizmaster
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Step 5: Implement Core Classes

Copy the entity classes from `02-ENTITY_CLASSES.java` into your project:

```bash
# Create entity package
mkdir -p src/main/java/com/quizmaster/entity
mkdir -p src/main/java/com/quizmaster/entity/enums

# Copy entities (manually or script)
# - User.java
# - Quiz.java
# - Question.java
# - Category.java
# - QuizAttempt.java
# - QuestionAnswer.java
# - Enums (OAuthProvider, QuestionType, QuizDifficulty, AttemptMode)
```

### Step 6: Start Development

```bash
# Install dependencies
mvn clean install

# Run application
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start at: `http://localhost:8080/api/v1`

---

## 🏗️ Project Structure

After implementation, your project should look like this:

```
quiz-master-backend/
├── src/
│   ├── main/
│   │   ├── java/com/quizmaster/
│   │   │   ├── QuizMasterApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── QuizController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   └── AttemptController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── QuizService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── ScoringService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── QuizRepository.java
│   │   │   │   └── AttemptRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Quiz.java
│   │   │   │   └── Question.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   ├── kafka/
│   │   │   │   ├── producer/
│   │   │   │   └── consumer/
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V1__initial_schema.sql
│   └── test/
│       └── java/com/quizmaster/
├── pom.xml
├── .env
├── docker-compose.yml
└── README.md
```

---

## 🔐 Obtaining OAuth2 Credentials

### Google OAuth2

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Navigate to **APIs & Services** > **Credentials**
4. Click **Create Credentials** > **OAuth 2.0 Client ID**
5. Configure OAuth consent screen
6. Add authorized redirect URIs:
   - `http://localhost:8080/api/v1/auth/oauth2/callback/google`
   - `https://your-domain.com/api/v1/auth/oauth2/callback/google`
7. Copy **Client ID** and **Client Secret**

### GitHub OAuth2

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Fill in details:
   - Application name: QuizMaster
   - Homepage URL: `http://localhost:3000`
   - Authorization callback URL: `http://localhost:8080/api/v1/auth/oauth2/callback/github`
4. Click **Register application**
5. Copy **Client ID** and generate **Client Secret**

---

## 🐳 Docker Compose Setup

Create `docker-compose.yml` for local development:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: quizmaster-postgres
    environment:
      POSTGRES_DB: quizmaster
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./01-DATABASE_SCHEMA.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    container_name: quizmaster-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: quizmaster-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: quizmaster-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: quizmaster-kafka-ui
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092

volumes:
  postgres_data:
  redis_data:
```

Start all services:

```bash
docker-compose up -d
```

---

## 🧪 Testing

### Run Unit Tests

```bash
mvn test
```

### Run Integration Tests

```bash
mvn verify -P integration-tests
```

### API Testing with cURL

```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health

# Get all categories
curl http://localhost:8080/api/v1/categories

# Get quizzes
curl http://localhost:8080/api/v1/quizzes

# Create quiz (with authentication)
curl -X POST http://localhost:8080/api/v1/quizzes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d @quiz-request.json
```

---

## 📖 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## 🔍 Troubleshooting

### Database Connection Issues

```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check PostgreSQL logs
docker logs quizmaster-postgres

# Test connection
psql -h localhost -U postgres -d quizmaster
```

### OAuth2 Issues

1. Verify redirect URIs match exactly
2. Check client ID and secret are correct
3. Ensure OAuth consent screen is configured
4. Check Spring Security logs

### Kafka Issues

```bash
# Check Kafka is running
docker ps | grep kafka

# List topics
docker exec quizmaster-kafka kafka-topics --list --bootstrap-server localhost:9092

# View Kafka UI
open http://localhost:8090
```

---

## 📚 Implementation Order

Follow this order for implementing the backend:

### Phase 1: Core Setup (Week 1)
1. ✅ Setup Spring Boot project
2. ✅ Configure PostgreSQL database
3. ✅ Implement entity classes
4. ✅ Create repositories
5. ✅ Setup Flyway migrations

### Phase 2: Authentication (Week 2)
1. ✅ Implement OAuth2 configuration
2. ✅ Create JWT token provider
3. ✅ Setup Spring Security
4. ✅ Implement AuthController
5. ✅ Test authentication flow

### Phase 3: Core APIs (Week 3-4)
1. ✅ Implement QuizService and QuizController
2. ✅ Implement CategoryService and CategoryController
3. ✅ Implement QuestionService
4. ✅ Create DTO classes and mappers
5. ✅ Add validation
6. ✅ Write unit tests

### Phase 4: Quiz Attempt & Scoring (Week 5)
1. ✅ Implement ScoringService (complex question types)
2. ✅ Implement AttemptService and AttemptController
3. ✅ Handle quiz submissions
4. ✅ Calculate scores
5. ✅ Store results

### Phase 5: Kafka Integration (Week 6)
1. ✅ Setup Kafka configuration
2. ✅ Implement event producers
3. ✅ Implement analytics consumer
4. ✅ Test event flow

### Phase 6: Testing & Polish (Week 7)
1. ✅ Write integration tests
2. ✅ Setup CI/CD pipeline
3. ✅ Add API documentation
4. ✅ Performance testing
5. ✅ Security audit

---

## 🌐 Deployment

### Docker Build

```bash
# Build JAR
mvn clean package -DskipTests

# Create Dockerfile
cat > Dockerfile <<EOF
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

# Build image
docker build -t quizmaster-backend:1.0.0 .

# Run container
docker run -d \
  --name quizmaster-api \
  -p 8080:8080 \
  --env-file .env \
  quizmaster-backend:1.0.0
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quizmaster-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: quizmaster-backend
  template:
    metadata:
      labels:
        app: quizmaster-backend
    spec:
      containers:
      - name: backend
        image: quizmaster-backend:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: quizmaster-secrets
              key: database-url
```

---

## 📊 Monitoring

### Actuator Endpoints

- Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Info: `/actuator/info`

### Prometheus Integration

Add to `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 🤝 Contributing

This documentation is meant to be a starting point. Customize based on your specific needs:

1. Adjust security requirements
2. Add custom business logic
3. Implement additional features
4. Optimize for your scale

---

## 📝 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

---

## 📧 Support

For questions or issues:
- Review the detailed documentation files
- Check API specifications in `03-API_ENDPOINTS.md`
- Refer to security setup in `04-SECURITY_CONFIGURATION.md`
- See Kafka integration in `05-KAFKA_INTEGRATION.md`

---

**Version**: 1.0  
**Last Updated**: May 12, 2026  
**Author**: QuizMaster Development Team  
**License**: MIT

---

## ✅ Checklist

Use this checklist to track your implementation progress:

### Setup
- [ ] Java 17+ installed
- [ ] Maven configured
- [ ] PostgreSQL running
- [ ] Docker installed
- [ ] OAuth2 credentials obtained

### Database
- [ ] Schema created
- [ ] Flyway migrations setup
- [ ] Sample data loaded
- [ ] Indexes created
- [ ] Views and triggers implemented

### Backend Core
- [ ] Entity classes implemented
- [ ] Repositories created
- [ ] Services implemented
- [ ] Controllers created
- [ ] DTOs and mappers ready

### Security
- [ ] OAuth2 configured
- [ ] JWT implemented
- [ ] Spring Security setup
- [ ] CORS configured
- [ ] Rate limiting added

### Features
- [ ] Quiz CRUD operations
- [ ] Category management
- [ ] Question handling (all 7 types)
- [ ] Quiz attempt submission
- [ ] Scoring algorithm
- [ ] User history tracking

### Kafka
- [ ] Producer configured
- [ ] Consumer implemented
- [ ] Event schemas defined
- [ ] Analytics processing

### Testing
- [ ] Unit tests written
- [ ] Integration tests created
- [ ] API tests added
- [ ] Load testing done

### Documentation
- [ ] API documentation complete
- [ ] Swagger UI working
- [ ] README updated
- [ ] Deployment guide ready

### Deployment
- [ ] Docker image built
- [ ] CI/CD pipeline setup
- [ ] Environment variables configured
- [ ] Monitoring enabled
- [ ] Production deployment successful

---

**Happy Coding! 🚀**
