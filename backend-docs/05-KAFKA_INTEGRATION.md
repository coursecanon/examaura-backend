# Kafka Integration Guide
## Event-Driven Architecture for QuizMaster

---

## Table of Contents
1. [Overview](#overview)
2. [Kafka Architecture](#kafka-architecture)
3. [Event Schema](#event-schema)
4. [Producer Implementation](#producer-implementation)
5. [Consumer Implementation](#consumer-implementation)
6. [Use Cases](#use-cases)
7. [Configuration](#configuration)

---

## Overview

### Why Kafka?

Kafka provides an event-driven architecture that enables:

- **Asynchronous Processing**: Quiz completions trigger analytics without blocking the user
- **Scalability**: Multiple consumers can process events independently
- **Reliability**: Events are persisted and can be replayed
- **Decoupling**: Services communicate through events, not direct calls
- **Audit Trail**: Complete history of all quiz-related events

### Event Flow Architecture

```
┌──────────────────┐         ┌─────────────┐         ┌──────────────────┐
│                  │         │             │         │                  │
│  Quiz Service    │────────>│   Kafka     │────────>│  Analytics       │
│  (Producer)      │ events  │   Broker    │ consume │  Service         │
│                  │         │             │         │  (Consumer)      │
└──────────────────┘         └─────────────┘         └──────────────────┘
                                    │
                                    │
                                    ▼
                             ┌─────────────┐
                             │             │
                             │ Notification│
                             │ Service     │
                             │ (Consumer)  │
                             └─────────────┘
```

---

## Kafka Architecture

### Topics

1. **quiz.created** - Published when a new quiz is created
2. **quiz.updated** - Published when a quiz is modified
3. **quiz.deleted** - Published when a quiz is deleted
4. **quiz.completed** - Published when a user completes a quiz
5. **user.registered** - Published when a new user signs up
6. **achievement.unlocked** - Published when a user achieves a milestone

### Consumer Groups

- **analytics-group**: Processes quiz completion events for analytics
- **notification-group**: Sends notifications to users
- **leaderboard-group**: Updates leaderboards in real-time
- **audit-group**: Logs all events for compliance

---

## Event Schema

### Base Event Structure

All events follow this base structure:

```json
{
  "eventId": "uuid",
  "eventType": "QUIZ_COMPLETED",
  "timestamp": "2026-05-12T15:30:00Z",
  "version": "1.0",
  "source": "quiz-service",
  "metadata": {
    "correlationId": "uuid",
    "userId": "uuid"
  },
  "payload": { ... }
}
```

### Quiz Completed Event

**Topic:** `quiz.completed`

```json
{
  "eventId": "950e8400-e29b-41d4-a716-446655440000",
  "eventType": "QUIZ_COMPLETED",
  "timestamp": "2026-05-12T15:30:00Z",
  "version": "1.0",
  "source": "quiz-service",
  "metadata": {
    "correlationId": "abc123-def456",
    "userId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "payload": {
    "attemptId": "950e8400-e29b-41d4-a716-446655440000",
    "quizId": "650e8400-e29b-41d4-a716-446655440000",
    "quizTitle": "AZ-900 Practice Test 1",
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryName": "Azure AZ-900",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "Coursecanon",
    "userEmail": "coursecanon@gmail.com",
    "mode": "REAL",
    "score": 85.5,
    "totalQuestions": 50,
    "correctAnswers": 43,
    "timeTakenSeconds": 3600,
    "passingScore": 75,
    "isPassed": true,
    "completedAt": "2026-05-12T15:30:00Z",
    "questionBreakdown": [
      {
        "questionType": "OBJECTIVE",
        "total": 30,
        "correct": 28
      },
      {
        "questionType": "MULTIPLE_CHOICE",
        "total": 15,
        "correct": 12
      },
      {
        "questionType": "DRAG_MATCH",
        "total": 5,
        "correct": 3
      }
    ]
  }
}
```

### Quiz Created Event

**Topic:** `quiz.created`

```json
{
  "eventId": "abc12345-def6-7890-abcd-ef1234567890",
  "eventType": "QUIZ_CREATED",
  "timestamp": "2026-05-12T14:00:00Z",
  "version": "1.0",
  "source": "quiz-service",
  "metadata": {
    "correlationId": "xyz789",
    "userId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "payload": {
    "quizId": "750e8400-e29b-41d4-a716-446655440000",
    "title": "Azure Security Fundamentals",
    "slug": "azure-security-fundamentals",
    "categoryId": "550e8400-e29b-41d4-a716-446655440001",
    "categoryName": "Azure AZ-900",
    "creatorId": "550e8400-e29b-41d4-a716-446655440000",
    "creatorName": "Coursecanon",
    "difficulty": "INTERMEDIATE",
    "totalQuestions": 45,
    "durationMinutes": 60,
    "isPublished": true,
    "createdAt": "2026-05-12T14:00:00Z"
  }
}
```

### User Registered Event

**Topic:** `user.registered`

```json
{
  "eventId": "def45678-ghi9-0123-defg-hi4567890123",
  "eventType": "USER_REGISTERED",
  "timestamp": "2026-05-12T10:00:00Z",
  "version": "1.0",
  "source": "auth-service",
  "metadata": {
    "correlationId": "reg123"
  },
  "payload": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "coursecanon@gmail.com",
    "name": "Coursecanon",
    "oauthProvider": "GOOGLE",
    "registeredAt": "2026-05-12T10:00:00Z"
  }
}
```

---

## Producer Implementation

### Kafka Configuration

```java
package com.quizmaster.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability settings
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Ordering guarantee
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Exactly-once semantics
        
        // Performance settings
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### Event Models

```java
package com.quizmaster.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizEvent {
    
    private UUID eventId;
    private String eventType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime timestamp;
    
    private String version;
    private String source;
    private EventMetadata metadata;
    private Object payload;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class EventMetadata {
    private String correlationId;
    private UUID userId;
}

// Specific Event Payloads

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizCompletedPayload {
    private UUID attemptId;
    private UUID quizId;
    private String quizTitle;
    private UUID categoryId;
    private String categoryName;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String mode;
    private Double score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer timeTakenSeconds;
    private Integer passingScore;
    private Boolean isPassed;
    private ZonedDateTime completedAt;
    private List<QuestionTypeBreakdown> questionBreakdown;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTypeBreakdown {
    private String questionType;
    private Integer total;
    private Integer correct;
}
```

### Event Producer Service

```java
package com.quizmaster.kafka.producer;

import com.quizmaster.kafka.event.QuizEvent;
import com.quizmaster.kafka.event.QuizCompletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizEventProducer {

    private static final String QUIZ_COMPLETED_TOPIC = "quiz.completed";
    private static final String QUIZ_CREATED_TOPIC = "quiz.created";
    private static final String USER_REGISTERED_TOPIC = "user.registered";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publish quiz completed event
     */
    public void publishQuizCompleted(QuizCompletedPayload payload, UUID userId) {
        QuizEvent event = QuizEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType("QUIZ_COMPLETED")
                .timestamp(ZonedDateTime.now())
                .version("1.0")
                .source("quiz-service")
                .metadata(EventMetadata.builder()
                        .correlationId(UUID.randomUUID().toString())
                        .userId(userId)
                        .build())
                .payload(payload)
                .build();

        sendEvent(QUIZ_COMPLETED_TOPIC, payload.getAttemptId().toString(), event);
    }

    /**
     * Publish quiz created event
     */
    public void publishQuizCreated(UUID quizId, String title, UUID categoryId, UUID creatorId) {
        // Similar implementation
        log.info("Publishing quiz created event for quiz: {}", quizId);
    }

    /**
     * Generic send event method
     */
    private void sendEvent(String topic, String key, QuizEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published event {} to topic {} with offset {}",
                            event.getEventId(),
                            topic,
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event {} to topic {}: {}",
                            event.getEventId(),
                            topic,
                            ex.getMessage());
                }
            });
        } catch (Exception ex) {
            log.error("Error publishing event to Kafka: {}", ex.getMessage(), ex);
            // Implement fallback mechanism (e.g., save to database for retry)
        }
    }
}
```

### Integration in Service Layer

```java
package com.quizmaster.service;

import com.quizmaster.kafka.producer.QuizEventProducer;
import com.quizmaster.kafka.event.QuizCompletedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final QuizEventProducer eventProducer;
    // ... other dependencies

    @Transactional
    public AttemptResponse submitQuizAttempt(AttemptSubmitRequest request, UUID userId) {
        // 1. Validate and score the quiz
        QuizAttempt attempt = scoreAndSaveAttempt(request, userId);

        // 2. Build event payload
        QuizCompletedPayload eventPayload = buildQuizCompletedPayload(attempt);

        // 3. Publish event to Kafka (async)
        eventProducer.publishQuizCompleted(eventPayload, userId);

        // 4. Return response immediately
        return mapToResponse(attempt);
    }

    private QuizCompletedPayload buildQuizCompletedPayload(QuizAttempt attempt) {
        return QuizCompletedPayload.builder()
                .attemptId(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .userId(attempt.getUser().getId())
                .score(attempt.getScore().doubleValue())
                .totalQuestions(attempt.getTotalQuestions())
                .correctAnswers(attempt.getCorrectAnswers())
                .completedAt(attempt.getCompletedAt())
                // ... other fields
                .build();
    }
}
```

---

## Consumer Implementation

### Consumer Configuration

```java
package com.quizmaster.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Consumer group settings
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "quiz-analytics-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        
        // Performance settings
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        
        // Trust all packages for deserialization
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.quizmaster.*");
        
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 concurrent consumer threads
        return factory;
    }
}
```

### Analytics Consumer

```java
package com.quizmaster.kafka.consumer;

import com.quizmaster.kafka.event.QuizEvent;
import com.quizmaster.kafka.event.QuizCompletedPayload;
import com.quizmaster.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(
        topics = "quiz.completed",
        groupId = "analytics-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeQuizCompleted(
            @Payload QuizEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment
    ) {
        log.info("Received quiz completed event: {} from partition: {} with offset: {}",
                event.getEventId(), partition, offset);

        try {
            QuizCompletedPayload payload = (QuizCompletedPayload) event.getPayload();

            // Process analytics
            analyticsService.updateQuizStatistics(payload);
            analyticsService.updateUserStatistics(payload);
            analyticsService.updateCategoryTrends(payload);
            analyticsService.updateLeaderboard(payload);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();

            log.info("Successfully processed quiz completed event: {}", event.getEventId());

        } catch (Exception ex) {
            log.error("Error processing quiz completed event: {}", event.getEventId(), ex);
            // Implement retry logic or dead letter queue
        }
    }
}
```

---

## Use Cases

### 1. Real-time Analytics

**Flow:**
1. User completes quiz → Event published to `quiz.completed`
2. Analytics consumer processes event
3. Updates:
   - Quiz popularity metrics
   - Average scores per quiz
   - Category performance trends
   - Question difficulty analysis
4. Data stored in separate analytics database (e.g., ClickHouse, TimescaleDB)

### 2. Leaderboard Updates

**Flow:**
1. Quiz completion event consumed
2. Calculate user rank based on score and time
3. Update Redis-based leaderboard
4. Trigger WebSocket notification to update frontend

### 3. Achievement System

**Flow:**
1. Monitor quiz completion events
2. Check for achievement criteria:
   - First quiz completed
   - 10 quizzes completed
   - Perfect score (100%)
   - Category master (10 quizzes in one category with 80%+ average)
3. Publish `achievement.unlocked` event
4. Notification service sends congratulations email/push notification

### 4. Personalized Recommendations

**Flow:**
1. Analyze user's quiz history from events
2. Identify weak areas (low scores in specific question types/categories)
3. Recommend targeted quizzes
4. Send weekly digest via email

### 5. Fraud Detection

**Flow:**
1. Monitor quiz completion patterns
2. Detect anomalies:
   - Impossibly fast completion times
   - Perfect scores on difficult quizzes
   - Unusual answer patterns
3. Flag suspicious attempts for review

---

## Configuration

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 1
        compression.type: snappy
    
    consumer:
      group-id: quiz-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: com.quizmaster.*
    
    listener:
      ack-mode: manual
```

### docker-compose.yml (For Local Development)

```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      - kafka
    ports:
      - "8090:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
```

---

## Monitoring & Observability

### Metrics to Track

1. **Producer Metrics**
   - Message send rate
   - Message failure rate
   - Average latency
   - Buffer utilization

2. **Consumer Metrics**
   - Lag (unconsumed messages)
   - Consumption rate
   - Processing time
   - Error rate

3. **Topic Metrics**
   - Message count per topic
   - Topic size
   - Partition count

### Tools

- **Kafka UI**: Web interface for viewing topics, messages, and consumer groups
- **Prometheus + Grafana**: Metrics visualization
- **Spring Boot Actuator**: Expose Kafka metrics

---

**Version:** 1.0  
**Last Updated:** May 12, 2026
