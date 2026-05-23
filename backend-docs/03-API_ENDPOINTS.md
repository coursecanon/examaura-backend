# REST API Endpoints Specification
## QuizMaster Backend API

Base URL: `https://api.quizmaster.com/api/v1`

---

## Table of Contents
1. [Authentication Endpoints](#authentication-endpoints)
2. [User Endpoints](#user-endpoints)
3. [Category Endpoints](#category-endpoints)
4. [Quiz Endpoints](#quiz-endpoints)
5. [Question Endpoints](#question-endpoints)
6. [Attempt Endpoints](#attempt-endpoints)
7. [Error Responses](#error-responses)
8. [Pagination & Filtering](#pagination--filtering)

---

## Authentication Endpoints

### 1. OAuth2 Login Initiate

**Endpoint:** `GET /auth/oauth2/authorize/{provider}`

**Description:** Initiates OAuth2 flow with Google or GitHub

**Path Parameters:**
- `provider` (String): `google` or `github`

**Query Parameters:**
- `redirect_uri` (String, optional): Frontend callback URL

**Response:**
```http
HTTP/1.1 302 Found
Location: https://accounts.google.com/o/oauth2/v2/auth?client_id=...
```

---

### 2. OAuth2 Callback

**Endpoint:** `GET /auth/oauth2/callback/{provider}`

**Description:** Handles OAuth2 provider callback and generates JWT

**Path Parameters:**
- `provider` (String): `google` or `github`

**Query Parameters:**
- `code` (String, required): Authorization code from provider
- `state` (String, required): CSRF protection token

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Coursecanon",
    "email": "coursecanon@gmail.com",
    "avatarUrl": "https://...",
    "oauthProvider": "GOOGLE"
  }
}
```

---

### 3. Refresh Token

**Endpoint:** `POST /auth/refresh`

**Headers:**
- `Authorization: Bearer <refresh_token>`

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 4. Get Current User

**Endpoint:** `GET /auth/me`

**Headers:**
- `Authorization: Bearer <access_token>`

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Coursecanon",
  "email": "coursecanon@gmail.com",
  "avatarUrl": "https://...",
  "oauthProvider": "GOOGLE",
  "createdAt": "2026-05-01T10:00:00Z",
  "totalQuizzes": 5,
  "totalAttempts": 15
}
```

---

### 5. Logout

**Endpoint:** `POST /auth/logout`

**Headers:**
- `Authorization: Bearer <access_token>`

**Response:** `200 OK`
```json
{
  "message": "Logged out successfully"
}
```

---

## User Endpoints

### 6. Get User Profile

**Endpoint:** `GET /users/profile`

**Headers:**
- `Authorization: Bearer <access_token>`

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Coursecanon",
  "email": "coursecanon@gmail.com",
  "avatarUrl": "https://...",
  "oauthProvider": "GOOGLE",
  "createdAt": "2026-05-01T10:00:00Z",
  "statistics": {
    "totalQuizzesCreated": 5,
    "totalAttempts": 15,
    "averageScore": 85.5,
    "bestScore": 95.0,
    "passedAttempts": 12,
    "failedAttempts": 3
  }
}
```

---

### 7. Update User Profile

**Endpoint:** `PUT /users/profile`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Coursecanon Kumar",
  "avatarUrl": "https://new-avatar-url.com/image.jpg"
}
```

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Coursecanon Kumar",
  "email": "coursecanon@gmail.com",
  "avatarUrl": "https://new-avatar-url.com/image.jpg",
  "updatedAt": "2026-05-12T14:30:00Z"
}
```

---

## Category Endpoints

### 8. List All Categories

**Endpoint:** `GET /categories`

**Query Parameters:**
- `includeCustom` (Boolean, optional, default: true): Include user custom categories

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "name": "Azure AZ-900",
      "slug": "azure",
      "description": "Microsoft Azure Fundamentals Certification",
      "icon": "☁️",
      "color": "#0078D4",
      "isCustom": false,
      "quizCount": 25
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "name": "AWS Cloud Practitioner",
      "slug": "aws",
      "description": "AWS Certified Cloud Practitioner",
      "icon": "🌩️",
      "color": "#FF9900",
      "isCustom": false,
      "quizCount": 18
    }
  ],
  "total": 6
}
```

---

### 9. Create Custom Category

**Endpoint:** `POST /categories`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Docker & Kubernetes",
  "description": "Container orchestration certification prep",
  "icon": "🐳",
  "color": "#326CE5"
}
```

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "name": "Docker & Kubernetes",
  "slug": "docker-kubernetes",
  "description": "Container orchestration certification prep",
  "icon": "🐳",
  "color": "#326CE5",
  "isCustom": true,
  "createdByUserId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-05-12T15:00:00Z"
}
```

---

### 10. Get Category Details

**Endpoint:** `GET /categories/{id}`

**Path Parameters:**
- `id` (UUID): Category ID

**Response:** `200 OK`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Azure AZ-900",
  "slug": "azure",
  "description": "Microsoft Azure Fundamentals Certification",
  "icon": "☁️",
  "color": "#0078D4",
  "isCustom": false,
  "quizCount": 25,
  "totalAttempts": 450,
  "averageScore": 78.5
}
```

---

## Quiz Endpoints

### 11. List All Quizzes

**Endpoint:** `GET /quizzes`

**Query Parameters:**
- `page` (Integer, optional, default: 0): Page number
- `size` (Integer, optional, default: 20, max: 100): Page size
- `category` (UUID, optional): Filter by category ID
- `difficulty` (String, optional): `BEGINNER`, `INTERMEDIATE`, `ADVANCED`
- `search` (String, optional): Search in title and description
- `sort` (String, optional, default: `createdAt`): Sort field
- `order` (String, optional, default: `DESC`): `ASC` or `DESC`

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440000",
      "title": "AZ-900 Practice Test 1",
      "slug": "az900-practice-test-1",
      "description": "Comprehensive practice test covering Azure fundamentals",
      "category": {
        "id": "550e8400-e29b-41d4-a716-446655440001",
        "name": "Azure AZ-900",
        "slug": "azure"
      },
      "creator": {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Coursecanon"
      },
      "difficulty": "INTERMEDIATE",
      "durationMinutes": 60,
      "passingPercentage": 75,
      "totalQuestions": 50,
      "totalAttempts": 125,
      "averageScore": 82.5,
      "isPublished": true,
      "isFeatured": true,
      "createdAt": "2026-04-15T10:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### 12. Get Quiz Details

**Endpoint:** `GET /quizzes/{id}`

**Path Parameters:**
- `id` (UUID): Quiz ID

**Query Parameters:**
- `includeQuestions` (Boolean, optional, default: false): Include full question data

**Response:** `200 OK`
```json
{
  "id": "650e8400-e29b-41d4-a716-446655440000",
  "title": "AZ-900 Practice Test 1",
  "slug": "az900-practice-test-1",
  "description": "Comprehensive practice test covering Azure fundamentals",
  "category": {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Azure AZ-900",
    "slug": "azure"
  },
  "creator": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Coursecanon",
    "avatarUrl": "https://..."
  },
  "difficulty": "INTERMEDIATE",
  "durationMinutes": 60,
  "passingPercentage": 75,
  "totalQuestions": 50,
  "totalAttempts": 125,
  "averageScore": 82.5,
  "isPublished": true,
  "isFeatured": true,
  "createdAt": "2026-04-15T10:00:00Z",
  "updatedAt": "2026-05-01T14:30:00Z",
  "questions": [] // Empty unless includeQuestions=true
}
```

---

### 13. Create Quiz

**Endpoint:** `POST /quizzes`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "title": "Azure Security Fundamentals",
  "description": "Test your knowledge of Azure security concepts",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "difficulty": "INTERMEDIATE",
  "durationMinutes": 45,
  "passingPercentage": 70,
  "questions": [
    {
      "questionType": "OBJECTIVE",
      "questionText": "Which Azure service provides DDoS protection?",
      "explanation": "Azure DDoS Protection provides defense against distributed denial-of-service attacks.",
      "position": 1,
      "options": [
        "Azure Firewall",
        "Azure DDoS Protection",
        "Azure Security Center",
        "Azure Bastion"
      ],
      "correctAnswer": {
        "answer": 1
      }
    },
    {
      "questionType": "MULTIPLE_CHOICE",
      "questionText": "Which of the following are identity providers supported by Azure AD? (Select all that apply)",
      "explanation": "Azure AD supports multiple identity providers for B2C scenarios.",
      "position": 2,
      "options": [
        "Google",
        "Facebook",
        "LinkedIn",
        "Twitter"
      ],
      "correctAnswer": {
        "answers": [0, 1, 2]
      }
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": "750e8400-e29b-41d4-a716-446655440000",
  "title": "Azure Security Fundamentals",
  "slug": "azure-security-fundamentals",
  "categoryId": "550e8400-e29b-41d4-a716-446655440001",
  "totalQuestions": 2,
  "createdAt": "2026-05-12T16:00:00Z",
  "message": "Quiz created successfully"
}
```

---

### 14. Update Quiz

**Endpoint:** `PUT /quizzes/{id}`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Path Parameters:**
- `id` (UUID): Quiz ID

**Request Body:** (Same structure as Create Quiz)

**Response:** `200 OK`
```json
{
  "id": "750e8400-e29b-41d4-a716-446655440000",
  "title": "Azure Security Fundamentals - Updated",
  "updatedAt": "2026-05-12T17:30:00Z",
  "message": "Quiz updated successfully"
}
```

---

### 15. Delete Quiz

**Endpoint:** `DELETE /quizzes/{id}`

**Headers:**
- `Authorization: Bearer <access_token>`

**Path Parameters:**
- `id` (UUID): Quiz ID

**Response:** `200 OK`
```json
{
  "message": "Quiz deleted successfully"
}
```

---

### 16. Get Featured Quizzes

**Endpoint:** `GET /quizzes/featured`

**Query Parameters:**
- `limit` (Integer, optional, default: 6): Number of quizzes to return

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440000",
      "title": "AZ-900 Practice Test 1",
      "category": {
        "name": "Azure AZ-900"
      },
      "totalQuestions": 50,
      "durationMinutes": 60,
      "totalAttempts": 125,
      "averageScore": 82.5
    }
  ]
}
```

---

## Question Endpoints

### 17. Add Question to Quiz

**Endpoint:** `POST /quizzes/{quizId}/questions`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Path Parameters:**
- `quizId` (UUID): Quiz ID

**Request Body (Objective Example):**
```json
{
  "questionType": "OBJECTIVE",
  "questionText": "What is Azure Virtual Network?",
  "questionImageUrl": "https://example.com/diagram.png",
  "explanation": "Azure Virtual Network enables Azure resources to securely communicate with each other.",
  "position": 1,
  "options": [
    "A physical network",
    "A virtual network in Azure",
    "A VPN connection",
    "A load balancer"
  ],
  "correctAnswer": {
    "answer": 1
  }
}
```

**Request Body (Drag & Drop Matching Example):**
```json
{
  "questionType": "DRAG_MATCH",
  "questionText": "Match Azure services with their descriptions",
  "explanation": "Understanding Azure service purposes is fundamental.",
  "position": 2,
  "matchPairs": [
    {
      "id": "1",
      "term": "Azure VM",
      "definition": "Infrastructure as a Service compute"
    },
    {
      "id": "2",
      "term": "Azure Functions",
      "definition": "Serverless compute platform"
    },
    {
      "id": "3",
      "term": "Azure App Service",
      "definition": "Platform as a Service for web apps"
    }
  ],
  "correctAnswer": [
    {"termId": "1", "definitionId": "1"},
    {"termId": "2", "definitionId": "2"},
    {"termId": "3", "definitionId": "3"}
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": "850e8400-e29b-41d4-a716-446655440000",
  "questionType": "OBJECTIVE",
  "position": 1,
  "createdAt": "2026-05-12T16:30:00Z",
  "message": "Question added successfully"
}
```

---

### 18. Update Question

**Endpoint:** `PUT /questions/{id}`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Path Parameters:**
- `id` (UUID): Question ID

**Request Body:** (Same structure as Add Question)

**Response:** `200 OK`

---

### 19. Delete Question

**Endpoint:** `DELETE /questions/{id}`

**Headers:**
- `Authorization: Bearer <access_token>`

**Path Parameters:**
- `id` (UUID): Question ID

**Response:** `200 OK`

---

## Attempt Endpoints

### 20. Submit Quiz Attempt

**Endpoint:** `POST /attempts`

**Headers:**
- `Authorization: Bearer <access_token>`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "quizId": "650e8400-e29b-41d4-a716-446655440000",
  "mode": "REAL",
  "startedAt": "2026-05-12T14:00:00Z",
  "completedAt": "2026-05-12T15:00:00Z",
  "answers": [
    {
      "questionId": "850e8400-e29b-41d4-a716-446655440000",
      "userAnswer": {
        "answer": 1
      },
      "timeSpentSeconds": 45
    },
    {
      "questionId": "850e8400-e29b-41d4-a716-446655440001",
      "userAnswer": {
        "answers": [0, 2]
      },
      "timeSpentSeconds": 60
    },
    {
      "questionId": "850e8400-e29b-41d4-a716-446655440002",
      "userAnswer": [
        {"termId": "1", "definitionId": "1"},
        {"termId": "2", "definitionId": "3"},
        {"termId": "3", "definitionId": "2"}
      ],
      "timeSpentSeconds": 120
    }
  ]
}
```

**Response:** `201 Created`
```json
{
  "id": "950e8400-e29b-41d4-a716-446655440000",
  "quizId": "650e8400-e29b-41d4-a716-446655440000",
  "quizTitle": "AZ-900 Practice Test 1",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "mode": "REAL",
  "score": 85.5,
  "totalQuestions": 50,
  "correctAnswers": 43,
  "timeTakenSeconds": 3600,
  "passingScore": 75,
  "isPassed": true,
  "completedAt": "2026-05-12T15:00:00Z",
  "questionResults": [
    {
      "questionId": "850e8400-e29b-41d4-a716-446655440000",
      "questionText": "What is Azure Virtual Network?",
      "questionType": "OBJECTIVE",
      "isCorrect": true,
      "userAnswer": {"answer": 1},
      "correctAnswer": {"answer": 1},
      "explanation": "Azure Virtual Network enables Azure resources to securely communicate with each other.",
      "pointsEarned": 1.0
    },
    {
      "questionId": "850e8400-e29b-41d4-a716-446655440001",
      "questionText": "Which are identity providers?",
      "questionType": "MULTIPLE_CHOICE",
      "isCorrect": false,
      "userAnswer": {"answers": [0, 2]},
      "correctAnswer": {"answers": [0, 1, 2]},
      "explanation": "Azure AD supports Google, Facebook, and LinkedIn.",
      "pointsEarned": 0.67
    }
  ]
}
```

---

### 21. Get User Attempt History

**Endpoint:** `GET /attempts`

**Headers:**
- `Authorization: Bearer <access_token>`

**Query Parameters:**
- `page` (Integer, optional, default: 0)
- `size` (Integer, optional, default: 20)
- `quizId` (UUID, optional): Filter by specific quiz
- `categoryId` (UUID, optional): Filter by category

**Response:** `200 OK`
```json
{
  "data": [
    {
      "id": "950e8400-e29b-41d4-a716-446655440000",
      "quizId": "650e8400-e29b-41d4-a716-446655440000",
      "quizTitle": "AZ-900 Practice Test 1",
      "categoryName": "Azure AZ-900",
      "mode": "REAL",
      "score": 85.5,
      "totalQuestions": 50,
      "correctAnswers": 43,
      "isPassed": true,
      "completedAt": "2026-05-12T15:00:00Z"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

### 22. Get Attempt Details

**Endpoint:** `GET /attempts/{id}`

**Headers:**
- `Authorization: Bearer <access_token>`

**Path Parameters:**
- `id` (UUID): Attempt ID

**Response:** `200 OK` (Full attempt details with all question results)

---

## Error Responses

### Standard Error Format

```json
{
  "timestamp": "2026-05-12T16:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for request",
  "path": "/api/v1/quizzes",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    },
    {
      "field": "durationMinutes",
      "message": "Duration must be greater than 0"
    }
  ]
}
```

### Common HTTP Status Codes

- `200 OK` - Successful request
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `422 Unprocessable Entity` - Validation failed
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error

---

## Pagination & Filtering

### Query Parameters

All list endpoints support:
- `page` (default: 0)
- `size` (default: 20, max: 100)
- `sort` (field name, default varies by endpoint)
- `order` (`ASC` or `DESC`, default: `DESC`)

### Response Format

```json
{
  "data": [...],
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

**API Version:** 1.0  
**Last Updated:** May 12, 2026
