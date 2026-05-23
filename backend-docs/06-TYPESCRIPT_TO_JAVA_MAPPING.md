# TypeScript to Java Mapping
## Data Model Consistency Between Frontend and Backend

---

## Table of Contents
1. [User Types](#user-types)
2. [Quiz Types](#quiz-types)
3. [Question Types](#question-types)
4. [Attempt Types](#attempt-types)
5. [DTO Mappings](#dto-mappings)
6. [Enum Mappings](#enum-mappings)

---

## User Types

### TypeScript (Frontend)

```typescript
// src/app/context/UserContext.tsx
export interface User {
  name: string;
  email: string;
  avatar: string;
}
```

### Java (Backend)

```java
// Entity
@Entity
@Table(name = "users")
public class User {
    private UUID id;
    private String email;
    private String name;
    private String avatarUrl;
    private OAuthProvider oauthProvider;
    private String oauthId;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    // ... getters/setters
}

// Response DTO
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private String oauthProvider;
    private ZonedDateTime createdAt;
    private UserStatistics statistics;
}

public class UserStatistics {
    private Integer totalQuizzesCreated;
    private Integer totalAttempts;
    private BigDecimal averageScore;
    private BigDecimal bestScore;
    private Integer passedAttempts;
    private Integer failedAttempts;
}
```

**Mapping Notes:**
- Frontend `avatar` → Backend `avatarUrl`
- Backend includes additional fields (id, oauthProvider, timestamps)
- Backend separates statistics into dedicated DTO

---

## Quiz Types

### TypeScript (Frontend)

```typescript
// src/app/data/mockData.ts
export interface Quiz {
  id: string;
  title: string;
  category: string;
  difficulty: 'beginner' | 'intermediate' | 'advanced';
  questionCount: number;
  duration: number; // in minutes
  passingScore?: number; // percentage
  description?: string;
  questions: Question[];
}

export interface Category {
  id: string;
  name: string;
  description: string;
  icon: string;
  color: string;
}
```

### Java (Backend)

```java
// Quiz Entity
@Entity
@Table(name = "quizzes")
public class Quiz {
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private Category category;  // ManyToOne relationship
    private User creator;
    private QuizDifficulty difficulty;
    private Integer durationMinutes;
    private Integer passingPercentage;
    private Integer totalQuestions;
    private Boolean isPublished;
    private Boolean isFeatured;
    private Integer totalAttempts;
    private BigDecimal averageScore;
    private List<Question> questions;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}

// Quiz Response DTO
public class QuizResponse {
    private UUID id;
    private String title;
    private String slug;
    private String description;
    private CategorySummary category;
    private UserSummary creator;
    private String difficulty;
    private Integer durationMinutes;
    private Integer passingPercentage;
    private Integer totalQuestions;
    private Integer totalAttempts;
    private BigDecimal averageScore;
    private Boolean isPublished;
    private Boolean isFeatured;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<QuestionResponse> questions;
}

// Category Entity
@Entity
@Table(name = "categories")
public class Category {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private String color;
    private Boolean isCustom;
    private User createdByUser;
    private ZonedDateTime createdAt;
}

// Category Response DTO
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private String color;
    private Boolean isCustom;
    private Integer quizCount;
    private Integer totalAttempts;
    private BigDecimal averageScore;
}
```

**Mapping Notes:**
- Frontend `id: string` → Backend `id: UUID`
- Frontend `category: string` → Backend `category: Category` (full object)
- Frontend `duration` → Backend `durationMinutes`
- Frontend `passingScore` → Backend `passingPercentage`
- Frontend `questionCount` → Backend `totalQuestions`
- Backend includes additional metadata (slug, creator, published status, statistics)

---

## Question Types

### TypeScript (Frontend)

```typescript
// src/app/data/mockData.ts
export interface Question {
  id: string;
  text: string;
  type: 'objective' | 'multiple-choice' | 'yes-no-grid' | 
        'drag-match' | 'drag-classify' | 'inline-dropdown' | 
        'matching-dropdown';
  options?: string[];
  correctAnswer?: number | number[];
  explanation: string;
  questionImage?: string;
  
  // For Yes/No Grid
  statements?: {
    id: string;
    text: string;
    correctAnswer: 'yes' | 'no';
  }[];
  
  // For Drag & Drop Matching
  matchPairs?: {
    id: string;
    term: string;
    definition: string;
  }[];
  
  // For Drag & Drop Classification
  categories?: {
    id: string;
    name: string;
  }[];
  classifyItems?: {
    id: string;
    text: string;
    correctCategoryId: string;
  }[];
  
  // For Inline Dropdown
  sentenceTemplate?: string;
  inlineDropdowns?: {
    id: string;
    options: string[];
    correctAnswer: number;
  }[];
  
  // For Matching Dropdown
  dropdownRows?: {
    id: string;
    label: string;
    options: string[];
    correctAnswer: number;
  }[];
}
```

### Java (Backend)

```java
// Question Entity
@Entity
@Table(name = "questions")
public class Question {
    private UUID id;
    private Quiz quiz;
    private QuestionType questionType;
    private String questionText;
    private String questionImageUrl;
    private String explanation;
    private Integer position;
    
    // JSONB columns for polymorphic data
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode options;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode correctAnswer;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode statements;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode matchPairs;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode categories;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode classifyItems;
    
    private String sentenceTemplate;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode inlineDropdowns;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode dropdownRows;
    
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}

// Question Response DTO
public class QuestionResponse {
    private UUID id;
    private String questionType;
    private String questionText;
    private String questionImageUrl;
    private String explanation;
    private Integer position;
    
    // For OBJECTIVE and MULTIPLE_CHOICE
    private List<String> options;
    private Object correctAnswer;  // Can be Integer or List<Integer>
    
    // For YES_NO_GRID
    private List<StatementDTO> statements;
    
    // For DRAG_MATCH
    private List<MatchPairDTO> matchPairs;
    
    // For DRAG_CLASSIFY
    private List<CategoryDTO> categories;
    private List<ClassifyItemDTO> classifyItems;
    
    // For INLINE_DROPDOWN
    private String sentenceTemplate;
    private List<InlineDropdownDTO> inlineDropdowns;
    
    // For MATCHING_DROPDOWN
    private List<DropdownRowDTO> dropdownRows;
}

// Supporting DTOs
public class StatementDTO {
    private String id;
    private String text;
    private String correctAnswer; // "yes" or "no"
}

public class MatchPairDTO {
    private String id;
    private String term;
    private String definition;
}

public class CategoryDTO {
    private String id;
    private String name;
}

public class ClassifyItemDTO {
    private String id;
    private String text;
    private String correctCategoryId;
}

public class InlineDropdownDTO {
    private String id;
    private List<String> options;
    private Integer correctAnswer;
}

public class DropdownRowDTO {
    private String id;
    private String label;
    private List<String> options;
    private Integer correctAnswer;
}
```

**Mapping Notes:**
- Frontend `text` → Backend `questionText`
- Frontend `questionImage` → Backend `questionImageUrl`
- Backend stores polymorphic data in JSONB columns for flexibility
- Backend adds `position` field for question ordering
- Frontend nested objects map to Java DTOs for type safety

### JSON Structure Examples in Backend

**Objective Question:**
```json
{
  "options": ["Option A", "Option B", "Option C", "Option D"],
  "correctAnswer": {"answer": 1}
}
```

**Multiple Choice:**
```json
{
  "options": ["Option A", "Option B", "Option C"],
  "correctAnswer": {"answers": [0, 2]}
}
```

**Yes/No Grid:**
```json
{
  "statements": [
    {"id": "1", "text": "Statement 1", "correctAnswer": "yes"},
    {"id": "2", "text": "Statement 2", "correctAnswer": "no"}
  ]
}
```

**Drag & Match:**
```json
{
  "matchPairs": [
    {"id": "1", "term": "Azure VM", "definition": "IaaS compute"},
    {"id": "2", "term": "Azure Functions", "definition": "Serverless"}
  ],
  "correctAnswer": [
    {"termId": "1", "definitionId": "1"},
    {"termId": "2", "definitionId": "2"}
  ]
}
```

---

## Attempt Types

### TypeScript (Frontend)

```typescript
// src/app/context/QuizContext.tsx
export interface HistoryEntry {
  quizId: string;
  quizName: string;
  category: string;
  score: number;
  date: string;
}
```

### Java (Backend)

```java
// QuizAttempt Entity
@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
    private UUID id;
    private User user;
    private Quiz quiz;
    private AttemptMode mode;  // REAL or PRACTICE
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer timeTakenSeconds;
    private Integer passingScore;
    private ZonedDateTime startedAt;
    private ZonedDateTime completedAt;
    private ZonedDateTime createdAt;
    private List<QuestionAnswer> questionAnswers;
    
    // Computed
    public Boolean isPassed() {
        return score.compareTo(BigDecimal.valueOf(passingScore)) >= 0;
    }
}

// Attempt Response DTO
public class AttemptResponse {
    private UUID id;
    private UUID quizId;
    private String quizTitle;
    private String categoryName;
    private UUID userId;
    private String userName;
    private String mode;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer timeTakenSeconds;
    private Integer passingScore;
    private Boolean isPassed;
    private ZonedDateTime completedAt;
    private List<QuestionResultDTO> questionResults;
}

// Question Result DTO (for detailed results)
public class QuestionResultDTO {
    private UUID questionId;
    private String questionText;
    private String questionType;
    private Boolean isCorrect;
    private Object userAnswer;
    private Object correctAnswer;
    private String explanation;
    private BigDecimal pointsEarned;
    private Integer timeSpentSeconds;
}

// QuestionAnswer Entity (stores individual answers)
@Entity
@Table(name = "question_answers")
public class QuestionAnswer {
    private UUID id;
    private QuizAttempt attempt;
    private Question question;
    
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private JsonNode userAnswer;
    
    private Boolean isCorrect;
    private BigDecimal pointsEarned;
    private Integer timeSpentSeconds;
    private ZonedDateTime createdAt;
}

// Attempt Submit Request DTO
public class AttemptSubmitRequest {
    private UUID quizId;
    private String mode;  // "REAL" or "PRACTICE"
    private ZonedDateTime startedAt;
    private ZonedDateTime completedAt;
    private List<QuestionAnswerRequest> answers;
}

public class QuestionAnswerRequest {
    private UUID questionId;
    private Object userAnswer;
    private Integer timeSpentSeconds;
}
```

**Mapping Notes:**
- Frontend `quizId: string` → Backend `quizId: UUID`
- Frontend `score: number` → Backend `score: BigDecimal` (for precision)
- Frontend `date: string` → Backend `completedAt: ZonedDateTime`
- Backend separates attempt metadata from detailed question answers
- Backend includes mode (REAL/PRACTICE) not in frontend type

---

## DTO Mappings

### Request DTOs

#### Quiz Create Request

**TypeScript:**
```typescript
// Implicit in QuizCreator component
{
  title: string;
  category: string;
  duration: number;
  description: string;
  questions: Question[];
}
```

**Java:**
```java
@Data
@Builder
public class QuizCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    private String description;
    
    @NotNull(message = "Category is required")
    private UUID categoryId;
    
    @NotNull(message = "Difficulty is required")
    private String difficulty;  // "BEGINNER", "INTERMEDIATE", "ADVANCED"
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;
    
    @Min(value = 0)
    @Max(value = 100)
    private Integer passingPercentage = 70;
    
    @NotNull(message = "At least one question is required")
    @Size(min = 1, message = "Quiz must have at least one question")
    private List<QuestionCreateRequest> questions;
}

@Data
@Builder
public class QuestionCreateRequest {
    
    @NotBlank(message = "Question text is required")
    private String questionText;
    
    @NotNull(message = "Question type is required")
    private String questionType;
    
    private String questionImageUrl;
    
    @NotBlank(message = "Explanation is required")
    private String explanation;
    
    @NotNull(message = "Position is required")
    private Integer position;
    
    // Polymorphic fields based on question type
    private List<String> options;
    private Object correctAnswer;
    private Object statements;
    private Object matchPairs;
    private Object categories;
    private Object classifyItems;
    private String sentenceTemplate;
    private Object inlineDropdowns;
    private Object dropdownRows;
}
```

---

## Enum Mappings

### TypeScript Enums

```typescript
// Question Types
type QuestionType = 
  | 'objective'
  | 'multiple-choice'
  | 'yes-no-grid'
  | 'drag-match'
  | 'drag-classify'
  | 'inline-dropdown'
  | 'matching-dropdown';

// Quiz Difficulty
type QuizDifficulty = 'beginner' | 'intermediate' | 'advanced';

// Attempt Mode (implicit in frontend)
type AttemptMode = 'real' | 'practice';
```

### Java Enums

```java
package com.quizmaster.entity.enums;

public enum QuestionType {
    OBJECTIVE("objective"),
    MULTIPLE_CHOICE("multiple-choice"),
    YES_NO_GRID("yes-no-grid"),
    DRAG_MATCH("drag-match"),
    DRAG_CLASSIFY("drag-classify"),
    INLINE_DROPDOWN("inline-dropdown"),
    MATCHING_DROPDOWN("matching-dropdown");
    
    private final String value;
    
    QuestionType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static QuestionType fromValue(String value) {
        for (QuestionType type : QuestionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown question type: " + value);
    }
}

public enum QuizDifficulty {
    BEGINNER("beginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");
    
    private final String value;
    
    QuizDifficulty(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static QuizDifficulty fromValue(String value) {
        for (QuizDifficulty difficulty : QuizDifficulty.values()) {
            if (difficulty.value.equalsIgnoreCase(value)) {
                return difficulty;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty: " + value);
    }
}

public enum AttemptMode {
    REAL,
    PRACTICE
}

public enum OAuthProvider {
    GOOGLE,
    GITHUB,
    LOCAL
}
```

**Mapping Notes:**
- Frontend uses lowercase kebab-case values
- Backend uses UPPERCASE enum names but stores lowercase values
- Use `fromValue()` methods for conversion from API requests
- Use `getValue()` for serialization to API responses

---

## Complete Request/Response Flow Example

### Frontend Request (Create Quiz)

```typescript
const quizData = {
  title: "Azure Security Fundamentals",
  category: "550e8400-e29b-41d4-a716-446655440001",
  duration: 45,
  description: "Test your Azure security knowledge",
  questions: [
    {
      id: "q1",
      text: "Which service provides DDoS protection?",
      type: "objective",
      options: ["Azure Firewall", "Azure DDoS Protection", "Azure Security Center"],
      correctAnswer: 1,
      explanation: "Azure DDoS Protection provides defense against DDoS attacks."
    }
  ]
};

// POST /api/v1/quizzes
fetch('/api/v1/quizzes', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(quizData)
});
```

### Backend Processing

```java
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {
    
    private final QuizService quizService;
    
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(
            @Valid @RequestBody QuizCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        QuizResponse quiz = quizService.createQuiz(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }
}

@Service
@RequiredArgsConstructor
public class QuizService {
    
    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    
    @Transactional
    public QuizResponse createQuiz(QuizCreateRequest request, UUID creatorId) {
        // Map DTO to Entity
        Quiz quiz = quizMapper.toEntity(request, creatorId);
        
        // Save to database
        Quiz savedQuiz = quizRepository.save(quiz);
        
        // Map Entity to Response DTO
        return quizMapper.toResponse(savedQuiz);
    }
}
```

---

## Validation Mapping

### Frontend Validation

```typescript
// In QuizCreator component
const validateQuiz = () => {
  if (!quizTitle.trim()) {
    toast.error('Please enter a quiz title');
    return false;
  }
  
  if (questions.length === 0) {
    toast.error('Please add at least one question');
    return false;
  }
  
  return true;
};
```

### Backend Validation

```java
// Using Bean Validation
@Data
public class QuizCreateRequest {
    
    @NotBlank(message = "Quiz title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;
    
    @NotNull(message = "Category is required")
    private UUID categoryId;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration cannot exceed 600 minutes")
    private Integer durationMinutes;
    
    @Size(min = 1, message = "Quiz must have at least one question")
    @Valid  // Validates nested QuestionCreateRequest objects
    private List<QuestionCreateRequest> questions;
}

// Global Exception Handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        List<FieldError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new FieldError(
                error.getField(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(ZonedDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request data")
            .errors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

---

## Summary Table

| Frontend (TypeScript) | Backend (Java) | Notes |
|----------------------|----------------|-------|
| `string` | `String` | Direct mapping |
| `number` | `Integer` or `BigDecimal` | Use BigDecimal for scores/prices |
| `boolean` | `Boolean` | Direct mapping |
| `string` (ID) | `UUID` | UUIDs for all IDs |
| `string` (date) | `ZonedDateTime` | ISO 8601 format |
| `type` unions | `enum` | Enums for type safety |
| `interface` | `class` (Entity/DTO) | Separate entity and DTO |
| Nested objects | `@ManyToOne` / `@OneToMany` | JPA relationships |
| Optional fields (`?`) | `@Column(nullable = true)` | Nullable database columns |
| Arrays | `List<T>` | Java collections |
| JSON fields | `JsonNode` (JSONB) | Flexible polymorphic storage |

---

**Version:** 1.0  
**Last Updated:** May 12, 2026
