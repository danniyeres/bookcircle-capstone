# BookCircle Capstone

BookCircle is a full-stack web platform that helps readers find nearby book clubs and participate in spoiler-free discussions based on their reading progress.

## Problem Statement

Book readers often face two issues:

1. It is hard to find nearby people reading the same book.
2. Group discussions quickly become full of spoilers for people who are behind in reading.

BookCircle solves this by combining map-based room discovery with chapter-aware visibility rules for comments.

## What This Project Is

BookCircle is a full-stack web application where users can:

- register and log in,
- browse books,
- create and join reading rooms,
- discover rooms by geolocation (H3 index),
- update chapter progress,
- see only comments that are safe for their current reading progress.

The project includes:

- backend API (`/backend`) with authentication, business logic, and persistence,
- frontend SPA (`/frontend`) for user interaction,
- project docs (`/docs`) with endpoint details.

## How It Works

1. User signs up or logs in to receive a JWT token.
2. User selects a book and joins or creates a room.
3. Room location is mapped to an H3 index for nearby-room discovery.
4. User posts progress updates by chapter.
5. Comment feed is filtered so each user sees only non-spoiler content based on their progress.

## Repository Structure

```text
bookcircle-capstone/
├── backend/        # Spring Boot API
├── frontend/       # React + Vite client
├── docs/           # API and architecture docs
├── assets/         # static assets
└── docker-compose.yml
```

## Tech Specs

### Backend

- Java 17
- Spring Boot 3.3.2
- Spring Security (JWT-based auth)
- Spring Data JPA + Hibernate
- PostgreSQL (runtime DB)
- H2 (test DB)
- H3 geospatial indexing (`com.uber:h3`)
- OpenAPI/Swagger (`springdoc-openapi`)

### Frontend

- React 19
- Vite 8
- React Router
- Zustand
- Axios
- React Leaflet + Leaflet (map UI)

### Testing & Tooling

- Backend integration tests: Spring Boot Test + MockMvc
- Frontend E2E tests: Playwright
- Docker / Docker Compose (backend container)

## Run The Project (Local)

### Prerequisites

- Node.js 20+ and npm
- Java 17+
- Maven 3.9+
- PostgreSQL 14+

### 1) Backend Setup

```bash
cd backend
cp .env.example .env
```

Set PostgreSQL and JWT values in `.env`, then run:

```bash
mvn spring-boot:run
```

Backend runs on `http://localhost:8080`.

Swagger UI:

- `http://localhost:8080/swagger-ui/index.html`

### 2) Frontend Setup

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Frontend default URL: `http://localhost:5173`.

Default API URL comes from:

- `VITE_API_URL` in `frontend/.env` (example: `http://127.0.0.1:8080`)

### 3) Docker (optional)

```bash
docker compose up --build
```

Current compose file builds and runs the backend service.

## Testing

### Backend tests

```bash
cd backend
mvn test
```

### Frontend tests

```bash
cd frontend
npx playwright test
```

## API Reference

- Detailed API guide: `docs/api-reference.md`
- Additional backend context: `docs/project-overview.md`

## Deployment

- Live website: [https://bookcircle-frontend.onrender.com](https://bookcircle-frontend.onrender.com)

## Student IDs

- Daniyar Yeleussiz — `230103200`
- Nuray Praliyeva — `230103057`
- Anuarbek Zhasulan — `230103166`
