# Backend Architecture & API Blueprint
## QuizMaster Spring Boot Application

## Table of Contents
1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Data Model Overview](#data-model-overview)
4. [API Endpoints Summary](#api-endpoints-summary)
5. [Security Architecture](#security-architecture)
6. [Kafka Integration](#kafka-integration)
7. [Deployment Architecture](#deployment-architecture)

---

## Technology Stack

### Core Framework
- **Spring Boot 3.2.x**
- **Java 17+**
- **Maven** for dependency management

### Database
- **PostgreSQL 15+** (Primary Database)
- **Flyway** for database migrations
- **Spring Data JPA** with Hibernate

### Security
- **Spring Security 6.x**
- **OAuth2 Client** (Google, GitHub)
- **JWT (JSON Web Tokens)** for stateless authentication
- **BCrypt** for password hashing (if needed)

### Messaging & Events
- **Apache Kafka** for event streaming
- **Spring Kafka** integration
- Use cases: Analytics, leaderboards, notifications

### Caching
- **Redis** for session management and caching
- **Spring Cache** abstraction

### Documentation
- **SpringDoc OpenAPI 3** (Swagger UI)
- Automatic API documentation generation

### Testing
- **JUnit 5**
- **Mockito**
- **TestContainers** for integration tests
- **REST Assured** for API testing

---

## Project Structure

```
quiz-master-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── quizmaster/
│   │   │           ├── QuizMasterApplication.java
│   │   │           ├── config/
│   │   │           │   ├── SecurityConfig.java
│   │   │           │   ├── OAuth2Config.java
│   │   │           │   ├── JwtConfig.java
│   │   │           │   ├── KafkaConfig.java
│   │   │           │   ├── RedisConfig.java
│   │   │           │   └── OpenApiConfig.java
│   │   │           ├── controller/
│   │   │           │   ├── AuthController.java
│   │   │           │   ├── QuizController.java
│   │   │           │   ├── QuestionController.java
│   │   │           │   ├── CategoryController.java
│   │   │           │   ├── AttemptController.java
│   │   │           │   └── UserController.java
│   │   │           ├── service/
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── QuizService.java
│   │   │           │   ├── QuestionService.java
│   │   │           │   ├── CategoryService.java
│   │   │           │   ├── AttemptService.java
│   │   │           │   ├── UserService.java
│   │   │           │   ├── ScoringService.java
│   │   │           │   └── JwtService.java
│   │   │           ├── repository/
│   │   │           │   ├── UserRepository.java
│   │   │           │   ├── QuizRepository.java
│   │   │           │   ├── QuestionRepository.java
│   │   │           │   ├── CategoryRepository.java
│   │   │           │   └── AttemptRepository.java
│   │   │           ├── entity/
│   │   │           │   ├── User.java
│   │   │           │   ├── Quiz.java
│   │   │           │   ├── Question.java
│   │   │           │   ├── Category.java
│   │   │           │   ├── QuizAttempt.java
│   │   │           │   ├── QuestionAnswer.java
│   │   │           │   └── enums/
│   │   │           │       ├── QuestionType.java
│   │   │           │       ├── OAuthProvider.java
│   │   │           │       └── QuizDifficulty.java
│   │   │           ├── dto/
│   │   │           │   ├── request/
│   │   │           │   │   ├── LoginRequest.java
│   │   │           │   │   ├── QuizCreateRequest.java
│   │   │           │   │   ├── QuizUpdateRequest.java
│   │   │           │   │   ├── QuestionCreateRequest.java
│   │   │           │   │   └── AttemptSubmitRequest.java
│   │   │           │   └── response/
│   │   │           │       ├── AuthResponse.java
│   │   │           │       ├── QuizResponse.java
│   │   │           │       ├── QuestionResponse.java
│   │   │           │       ├── AttemptResponse.java
│   │   │           │       └── ApiResponse.java
│   │   │           ├── mapper/
│   │   │           │   ├── QuizMapper.java
│   │   │           │   ├── QuestionMapper.java
│   │   │           │   ├── UserMapper.java
│   │   │           │   └── AttemptMapper.java
│   │   │           ├── exception/
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   ├── QuizNotFoundException.java
│   │   │           │   ├── UnauthorizedException.java
│   │   │           │   └── ValidationException.java
│   │   │           ├── security/
│   │   │           │   ├── JwtAuthenticationFilter.java
│   │   │           │   ├── JwtTokenProvider.java
│   │   │           │   ├── OAuth2AuthenticationSuccessHandler.java
│   │   │           │   └── CustomUserDetailsService.java
│   │   │           ├── kafka/
│   │   │           │   ├── producer/
│   │   │           │   │   └── QuizEventProducer.java
│   │   │           │   ├── consumer/
│   │   │           │   │   └── AnalyticsConsumer.java
│   │   │           │   └── event/
│   │   │           │       ├── QuizCompletedEvent.java
│   │   │           │       └── QuizCreatedEvent.java
│   │   │           └── util/
│   │   │               ├── JsonUtil.java
│   │   │               └── ValidationUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/
│   │           └── migration/
│   │               ├── V1__create_users_table.sql
│   │               ├── V2__create_categories_table.sql
│   │               ├── V3__create_quizzes_table.sql
│   │               ├── V4__create_questions_table.sql
│   │               └── V5__create_attempts_table.sql
│   └── test/
│       └── java/
│           └── com/
│               └── quizmaster/
│                   ├── integration/
│                   ├── unit/
│                   └── QuizMasterApplicationTests.java
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

---

## Data Model Overview

### Core Entities

1. **User**
   - Authentication via OAuth2 (Google, GitHub)
   - Profile information
   - Quiz creation and attempt history

2. **Quiz**
   - Created by users
   - Belongs to a category
   - Contains multiple questions
   - Has timing and passing score configurations

3. **Question**
   - Supports 7 question types
   - Polymorphic structure using JSON columns
   - Belongs to a quiz

4. **Category**
   - Predefined or custom user categories
   - Azure, AWS, Salesforce, etc.

5. **QuizAttempt**
   - Tracks user quiz completion
   - Stores score, time taken, and detailed answers
   - Links to user and quiz

### Entity Relationships

```
User (1) ----< (M) Quiz
User (1) ----< (M) QuizAttempt
Quiz (1) ----< (M) Question
Quiz (1) ----< (M) QuizAttempt
Category (1) ----< (M) Quiz
QuizAttempt (1) ----< (M) QuestionAnswer
```

---

## API Endpoints Summary

### Authentication
- `POST /api/auth/oauth2/callback` - OAuth2 callback
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/me` - Get current user info
- `POST /api/auth/logout` - Logout user

### Users
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/{userId}/stats` - Get user statistics

### Categories
- `GET /api/categories` - List all categories
- `POST /api/categories` - Create custom category
- `GET /api/categories/{id}` - Get category details
- `GET /api/categories/{id}/quizzes` - List quizzes in category

### Quizzes
- `GET /api/quizzes` - List all quizzes (with filters)
- `POST /api/quizzes` - Create new quiz
- `GET /api/quizzes/{id}` - Get quiz details
- `PUT /api/quizzes/{id}` - Update quiz
- `DELETE /api/quizzes/{id}` - Delete quiz
- `GET /api/quizzes/search` - Search quizzes
- `GET /api/quizzes/featured` - Get featured quizzes

### Questions
- `POST /api/quizzes/{quizId}/questions` - Add question to quiz
- `PUT /api/questions/{id}` - Update question
- `DELETE /api/questions/{id}` - Delete question

### Attempts
- `POST /api/attempts` - Submit quiz attempt
- `GET /api/attempts` - Get user's attempt history
- `GET /api/attempts/{id}` - Get attempt details
- `GET /api/attempts/quiz/{quizId}` - Get attempts for specific quiz

---

## Security Architecture

### OAuth2 + JWT Flow

1. **Frontend initiates OAuth2 flow** → Redirects to Google/GitHub
2. **User authenticates** → OAuth provider redirects to backend callback
3. **Backend receives authorization code** → Exchanges for access token
4. **Backend creates/updates user** → Generates JWT token
5. **Frontend receives JWT** → Stores in localStorage
6. **Subsequent requests** → Include JWT in Authorization header
7. **Backend validates JWT** → Processes request

### JWT Token Structure

```json
{
  "sub": "user-id-uuid",
  "email": "user@example.com",
  "name": "User Name",
  "roles": ["ROLE_USER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Security Headers

```
Authorization: Bearer <jwt-token>
X-CSRF-TOKEN: <csrf-token>
```

---

## Kafka Integration

### Event-Driven Architecture

**Topics:**
- `quiz.created` - When a new quiz is published
- `quiz.completed` - When a user completes a quiz
- `quiz.updated` - When a quiz is edited
- `user.registered` - When a new user signs up

### Use Case: Analytics Service

**Flow:**
1. User completes quiz → AttemptController saves attempt
2. Backend publishes `quiz.completed` event to Kafka
3. Analytics Consumer processes event:
   - Updates global leaderboard
   - Calculates quiz popularity
   - Tracks completion rates
   - Generates insights (average scores, common mistakes)
4. Analytics stored in separate database/cache

**Event Payload Example:**

```json
{
  "eventId": "uuid",
  "eventType": "QUIZ_COMPLETED",
  "timestamp": "2026-05-12T10:30:00Z",
  "userId": "user-uuid",
  "quizId": "quiz-uuid",
  "score": 85,
  "timeTaken": 3600,
  "totalQuestions": 50,
  "correctAnswers": 43
}
```

### Benefits
- **Decoupled Services**: Analytics doesn't slow down quiz submission
- **Scalability**: Multiple consumers can process events
- **Audit Trail**: Complete event history
- **Real-time Notifications**: Send emails/push notifications on completion

---

## Deployment Architecture

### Microservices Architecture (Optional Future)

```
┌─────────────────────┐
│   Load Balancer     │
│    (NGINX/ALB)      │
└──────────┬──────────┘
           │
    ┌──────┴──────┐
    │             │
┌───▼───────┐ ┌──▼──────────┐
│  API      │ │  API        │
│  Gateway  │ │  Gateway    │
│  (Spring) │ │  (Spring)   │
└───┬───────┘ └──┬──────────┘
    │            │
    ├────────────┼────────────┐
    │            │            │
┌───▼──────┐ ┌──▼──────┐ ┌──▼─────────┐
│ Quiz     │ │ User    │ │ Analytics  │
│ Service  │ │ Service │ │ Service    │
└───┬──────┘ └──┬──────┘ └──┬─────────┘
    │           │            │
    └───────────┴────────────┘
                │
    ┌───────────┴────────────┐
    │                        │
┌───▼──────────┐    ┌───────▼──────┐
│  PostgreSQL  │    │  Apache       │
│  (Primary)   │    │  Kafka        │
└──────────────┘    └───────────────┘
         │
    ┌────▼────┐
    │  Redis  │
    │ (Cache) │
    └─────────┘
```

### Container Deployment (Docker)

```yaml
# docker-compose.yml
services:
  app:
    image: quiz-master-backend:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://db:5432/quizmaster
    depends_on:
      - db
      - redis
      - kafka

  db:
    image: postgres:15-alpine
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine

  kafka:
    image: confluentinc/cp-kafka:latest

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
```

---

## Performance Considerations

### Caching Strategy
- **Quiz List**: Cache for 5 minutes
- **Quiz Details**: Cache for 10 minutes, invalidate on update
- **User Profile**: Cache for 30 minutes
- **Categories**: Cache indefinitely (rarely change)

### Database Optimization
- Index on frequently queried fields (quiz category, user email)
- Pagination for large result sets
- Read replicas for heavy read operations
- Connection pooling (HikariCP)

### Rate Limiting
- **Authentication endpoints**: 5 requests/minute per IP
- **Quiz submission**: 10 requests/hour per user
- **General API**: 100 requests/minute per user

---

## Next Steps

1. Review detailed schema in `01-DATABASE_SCHEMA.sql`
2. Check entity definitions in `02-ENTITY_CLASSES.java`
3. Explore API specifications in `03-API_ENDPOINTS.md`
4. Review security setup in `04-SECURITY_CONFIGURATION.md`
5. Understand Kafka integration in `05-KAFKA_INTEGRATION.md`
6. See TypeScript mappings in `06-TYPESCRIPT_TO_JAVA_MAPPING.md`

---

**Document Version**: 1.0  
**Last Updated**: May 12, 2026  
**Author**: QuizMaster Development Team
