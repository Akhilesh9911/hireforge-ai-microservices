# HireForge AI — Microservices Backend

![CI](https://github.com/Akhilesh9911/hireforge-ai-microservices/actions/workflows/ci.yml/badge.svg)

An AI-powered career platform built with a microservices architecture. Each service is independently deployable, has its own database, and communicates through an API Gateway with JWT-based authentication.

---

## Architecture Overview

```
Client (Postman / Frontend)
            │
            ▼
     API Gateway (8080)
     JWT Validation
     Route + Forward X-User-Id
            │
    ┌───────┼───────────┐──────────────┐
    ▼       ▼           ▼              ▼
Auth    Resume      Job Tracker   Interview
Service Service     Service       Service
(8081)  (8082)      (8083)        (8084)
    │       │           │              │
    ▼       ▼           ▼              ▼
auth_db resume_db   job_db      interview_db

All services register with Eureka Server (8761)
Gateway uses load-balanced routing (lb://SERVICE-NAME)
```

---

## Services

| Service | Port | Database | Description |
|---|---|---|---|
| Eureka Server | 8761 | — | Service discovery and registry |
| API Gateway | 8080 | — | JWT auth, routing, X-User-Id injection |
| Auth Service | 8081 | hireforge_auth_db | Register, login, JWT token generation |
| Resume Service | 8082 | hireforge_resume_db | PDF/DOCX parsing, ATS scoring via Gemini AI |
| Job Tracker Service | 8083 | hireforge_job_db | CRUD for job applications with status tracking |
| Interview Service | 8084 | hireforge_interview_db | AI-generated interview questions from resume + job role |

---

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.5**
- **Spring Cloud 2023.0.3** — Eureka, API Gateway (Reactive)
- **Spring Security + JWT** — jjwt 0.12.6
- **Spring Data JPA + MySQL**
- **Spring WebFlux** — Reactive API Gateway
- **Apache PDFBox 3.0.3** — PDF text extraction
- **Apache POI 5.3.0** — DOCX text extraction
- **Google Gemini AI** — ATS scoring, missing skills, interview question generation
- **Docker + Docker Compose** — containerized local development
- **GitHub Actions** — CI/CD pipeline (Maven build + Docker build verify)
- **RestTemplate** — Gemini API HTTP calls

---

## Key Design Decisions

- **JWT validated at Gateway layer only** — downstream services trust X-User-Id header
- **Each service has its own database** — true microservices data isolation
- **Reactive Gateway** — spring-cloud-starter-gateway (WebFlux), not MVC
- **Java DSL routing** — properties-based routing avoided for reliability
- **Spring Boot 3.3.5 + Spring Cloud 2023.0.3 pinned** — stable combination, never upgrade

---

## CI/CD Pipeline

GitHub Actions pipeline triggers on every push to `main`.

**Matrix Build** — all 6 services build in parallel:
- Maven clean package (`-DskipTests`) for each service
- Docker image build verify for each service

```
push to main
      ↓
Matrix: Build (6 parallel)      →     Matrix: Docker Verify (6 parallel)
├── eureka-server                      ├── eureka-server
├── api-gateway                        ├── api-gateway
├── auth-service                       ├── auth-service
├── resume-service                     ├── resume-service
├── job-tracker-service                ├── job-tracker-service
└── interview-service                  └── interview-service
```

---

## API Endpoints

All requests go through API Gateway on port 8080.

### Auth Service
```
POST /auth-service/api/auth/register    — Register new user (201)
POST /auth-service/api/auth/login       — Login, returns JWT token (200)
```

### Resume Service
```
POST /resume-service/api/resume/analyze — Upload PDF/DOCX, returns ATS score + missing skills (200)
```
Body: `multipart/form-data` — `file` (PDF or DOCX), `jobDescription` (Text)

### Job Tracker Service
```
POST   /job-tracker-service/api/jobs        — Add job application (201)
GET    /job-tracker-service/api/jobs        — Get all applications for user (200)
GET    /job-tracker-service/api/jobs/{id}   — Get single application (200)
PATCH  /job-tracker-service/api/jobs/{id}   — Partial update application (200)
DELETE /job-tracker-service/api/jobs/{id}   — Delete application (204)
```

### Interview Service
```
POST /interview-service/api/interview/generate  — Upload resume + job role, returns 10 AI questions (201)
GET  /interview-service/api/interview/history   — Get all past interview sessions for user (200)
```
Body: `multipart/form-data` — `file` (PDF or DOCX), `jobRole` (Text)

> All endpoints except `/auth-service/api/auth/register` and `/auth-service/api/auth/login` require `Authorization: Bearer <token>` header.

---

## Local Setup

### Prerequisites
- Java 17
- Maven
- Docker Desktop
- Google Gemini API key

---

### Option 1: Docker Compose (Recommended)

Create a `.env` file in the project root:

```env
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
GEMINI_API_KEY=your_gemini_api_key
```

Then run:

```bash
docker-compose up --build
```

All 6 services + MySQL start automatically in the correct order.

Verify all services registered: `http://localhost:8761`

---

### Option 2: Run Individually in IntelliJ

#### Prerequisites
- MySQL running locally
- Environment variables set in IntelliJ Run Configurations for each service

#### Startup Order
```
1. Eureka Server      (8761)
2. Auth Service       (8081)
3. Resume Service     (8082)
4. Job Tracker        (8083)
5. Interview Service  (8084)
6. API Gateway        (8080)  ← always last
```

Databases are created automatically on first startup (`createDatabaseIfNotExist=true`).

---

## Environment Variables

### Auth Service
| Variable | Description |
|---|---|
| DB_USERNAME | MySQL username |
| DB_PASSWORD | MySQL password |
| JWT_SECRET | Secret key for JWT signing |

### API Gateway
| Variable | Description |
|---|---|
| JWT_SECRET | Same secret key as Auth Service |

### Resume Service
| Variable | Description |
|---|---|
| DB_USERNAME | MySQL username |
| DB_PASSWORD | MySQL password |
| GEMINI_API_KEY | Google Gemini API key |

### Job Tracker Service
| Variable | Description |
|---|---|
| DB_USERNAME | MySQL username |
| DB_PASSWORD | MySQL password |

### Interview Service
| Variable | Description |
|---|---|
| DB_USERNAME | MySQL username |
| DB_PASSWORD | MySQL password |
| GEMINI_API_KEY | Google Gemini API key |

---

## Related Repository

**HireForge AI Monolith** (complete, live on Render):
- GitHub: [github.com/Akhilesh9911/hireforge-ai](https://github.com/Akhilesh9911/hireforge-ai)
- Live: [hireforge-ai-mqul.onrender.com](https://hireforge-ai-mqul.onrender.com)
